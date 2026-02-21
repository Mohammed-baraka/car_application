package com.example.carapplication.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.DataBase.UserDao;
import com.example.carapplication.Modle.ImagePickerHelper;
import com.example.carapplication.Modle.User;
import com.example.carapplication.Notifications.NotificationHelper;
import com.example.carapplication.R;
import com.example.carapplication.databinding.ActivityProfileBinding;
import com.example.carapplication.databinding.DialogChangePasswordBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final int PASSWORD_MIN_LENGTH = 8;
    private static String newProfileImagePath = null;
    private ActivityProfileBinding binding;
    private AppDatabase appDatabase;
    private UserDao userDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private User currentUser;
    private int userId;
    private ImagePickerHelper imagePickerHelper;
    private Uri newProfileImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appDatabase = AppDatabase.getInstance(this);
        userDao = appDatabase.userDao();


        userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "الرجاء تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }
        loadUserProfile();
        setupImagePicker();


        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.btnSave.setOnClickListener(v -> {
            saveProfileChanges();
        });

        binding.btnChangePassword.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            DialogChangePasswordBinding binding = DialogChangePasswordBinding.bind(dialogView);
            builder.setView(dialogView);


            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);

            binding.btnCancel.setOnClickListener(v -> dialog.dismiss());

            binding.btnConfirm.setOnClickListener(v -> {
                String currentPassword = binding.etCurrentPassword.getText().toString().trim();
                String newPassword = binding.etNewPassword.getText().toString().trim();
                String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

                if (currentPassword.isEmpty()) {
                    binding.etCurrentPassword.setError("كلمة المرور الحالية مطلوبة");
                    return;
                }

                if (!currentPassword.equals(currentUser.getPassword())) {
                    binding.etCurrentPassword.setError("كلمة المرور الحالية غير صحيحة");
                    return;
                }

                if (newPassword.isEmpty()) {
                    binding.etCurrentPassword.setError("كلمة المرور الجديدة مطلوبة");
                    return;
                }

                if (newPassword.length() < PASSWORD_MIN_LENGTH) {
                    binding.etCurrentPassword.setError("كلمة المرور يجب أن تكون 8 أحرف على الأقل");
                    return;
                }

                if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")) {
                    binding.etCurrentPassword.setError("كلمة المرور يجب أن تحتوي على حرف كبير وصغير ورقم ورمز خاص");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    binding.etConfirmPassword.setError("كلمة المرور غير متطابقة");
                    return;
                }

                executorService.execute(() -> {
                    currentUser.setPassword(newPassword);
                    userDao.update(currentUser);

                    runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(ProfileActivity.this,
                                "✓ تم تغيير كلمة المرور بنجاح", Toast.LENGTH_LONG).show();
                    });
                });
            });

            dialog.show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("تسجيل الخروج")
                    .setMessage("هل أنت متأكد من تسجيل الخروج؟")
                    .setPositiveButton("نعم", (dialog, which) -> logout())
                    .setNegativeButton("إلغاء", null)
                    .show();
        });

        binding.ivProfile.setOnClickListener(v -> {
            imagePickerHelper.showImagePickerDialog();
        });
        binding.ivProfile.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                showImagePreviewDialog();
                return true;
            }
        });

        setupTextWatchers();
    }


    private void setupImagePicker() {
        imagePickerHelper = new ImagePickerHelper(this, new ImagePickerHelper.OnImageSelectedListener() {
            @Override
            public void onImageSelected(String imagePath, Uri imageUri) {
                newProfileImagePath = imagePath;
                newProfileImageUri = imageUri;
                runOnUiThread(() -> {
                    ImagePickerHelper.loadImage(ProfileActivity.this, imageUri, binding.ivProfile);
                    ImagePickerHelper.loadImage(ProfileActivity.this, currentUser.getProfileImage(), binding.ivProfile);
                    loadUserImage();
                    Toast.makeText(ProfileActivity.this, "✅ يرجى حفظ التغيرات حتى تتغير الصورة", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadUserProfile() {
        executorService.execute(() -> {
            currentUser = userDao.getUserById(userId);
            runOnUiThread(() -> {
                if (currentUser != null) {
                    displayUserProfile();
                } else {
                    Toast.makeText(ProfileActivity.this,
                            "خطأ في تحميل بيانات المستخدم", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void displayUserProfile() {
        binding.etFullName.setText(currentUser.getFullName());
        binding.etEmail.setText(currentUser.getEmail());
        binding.etPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        binding.etEmail.setEnabled(false);
        binding.tilEmail.setHelperText("لا يمكن تغيير البريد الإلكتروني");
        binding.tvEmailVerified.setVisibility(View.GONE); // يمكن تفعيلها لاحقاً

        loadUserImage();
    }

    private void loadUserImage() {
        if (currentUser.getProfileImageUri() != null && !currentUser.getProfileImageUri().isEmpty()) {
            ImagePickerHelper.loadImage(this, Uri.parse(currentUser.getProfileImageUri()), binding.ivProfile);
        } else if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            ImagePickerHelper.loadImage(this, currentUser.getProfileImage(), binding.ivProfile);
        } else {
            binding.ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }


    private void setupTextWatchers() {
        binding.etFullName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateFullName();
            }
        });

        binding.etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePhone();
            }
        });
    }

    private boolean validateFullName() {
        String fullName = binding.etFullName.getText().toString().trim();

        if (fullName.isEmpty()) {
            binding.tilFullName.setError("الاسم الكامل مطلوب");
            return false;
        }

        if (fullName.length() < 3) {
            binding.tilFullName.setError("الاسم يجب أن يكون 3 أحرف على الأقل");
            return false;
        }

        if (!fullName.matches("^[\\u0600-\\u06FF\\sA-Za-z]+$")) {
            binding.tilFullName.setError("الاسم يجب أن يحتوي على أحرف فقط");
            return false;
        }

        binding.tilFullName.setError(null);
        return true;
    }

    private boolean validatePhone() {
        String phone = binding.etPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            binding.tilPhone.setError("رقم الهاتف مطلوب");
            return false;
        }

        if (!phone.matches("^\\+?[0-9]{10,13}$")) {
            binding.tilPhone.setError("رقم الهاتف غير صحيح (10-13 رقم)");
            return false;
        }

        binding.tilPhone.setError(null);
        return true;
    }

    private void saveProfileChanges() {
        if (!validateFullName() | !validatePhone()) {
            return;
        }
        String fullName = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        if (fullName.equals(currentUser.getFullName()) &&
                phone.equals(currentUser.getPhone()) &&
                newProfileImagePath == null &&
                newProfileImageUri == null) {
            Toast.makeText(this, "لا توجد تغييرات للحفظ", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            currentUser.setFullName(fullName);
            currentUser.setPhone(phone);
            if (newProfileImagePath != null) {
                ImagePickerHelper.deleteOldImage(currentUser.getProfileImage());
                currentUser.setProfileImage(newProfileImagePath);
                userDao.updateProfileImagePath(currentUser.getId(), newProfileImagePath);
            }

            if (newProfileImageUri != null) {
                currentUser.setProfileImageUri(newProfileImageUri.toString());
                userDao.updateProfileImageUri(currentUser.getId(), newProfileImageUri.toString());
            }
            userDao.update(currentUser);

            updateUserSession(currentUser);

            runOnUiThread(() -> {
                Toast.makeText(ProfileActivity.this,
                        "✓ تم حفظ التغييرات بنجاح يرجى اعادة تشغيل التطبيق لعرض التغيرات", Toast.LENGTH_LONG).show();

                newProfileImagePath = null;
                newProfileImageUri = null;
            });
        });
    }

    private void updateUserSession(User user) {
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.putString("user_name", user.getFullName());
        editor.putString("user_phone", user.getPhone());

        if (user.getProfileImage() != null) {
            editor.putString("user_image", user.getProfileImage());
        }

        if (user.getProfileImageUri() != null) {
            editor.putString("user_image_uri", user.getProfileImageUri());
        }

        editor.apply();
    }


    private void showImagePreviewDialog() {
        if (currentUser.getProfileImageUri() == null &&
                currentUser.getProfileImage() == null) {
            Toast.makeText(this, "لا توجد صورة للمعاينة", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_preview, null);
        builder.setView(dialogView);

        de.hdodenhof.circleimageview.CircleImageView ivPreview = dialogView.findViewById(R.id.ivPreview);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnClose = dialogView.findViewById(R.id.btnClose);
        if (currentUser.getProfileImageUri() != null && !currentUser.getProfileImageUri().isEmpty()) {
            ImagePickerHelper.loadImage(this, Uri.parse(currentUser.getProfileImageUri()), ivPreview);
        } else if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            ImagePickerHelper.loadImage(this, currentUser.getProfileImage(), ivPreview);
        }

        AlertDialog dialogDelete = builder.create();

        btnClose.setOnClickListener(v -> dialogDelete.dismiss());

        btnDelete.setOnClickListener(v -> {
            dialogDelete.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("حذف الصورة")
                    .setMessage("هل أنت متأكد من حذف صورة الملف الشخصي؟")
                    .setPositiveButton("نعم", (dialog, which) -> deleteProfileImage())
                    .setNegativeButton("إلغاء", null)
                    .show();
        });

        dialogDelete.show();
    }


    private void deleteProfileImage() {
        executorService.execute(() -> {
            if (currentUser.getProfileImage() != null) {
                ImagePickerHelper.deleteOldImage(currentUser.getProfileImage());
            }
            currentUser.setProfileImage("");
            currentUser.setProfileImageUri("");
            userDao.updateProfileImage(currentUser.getId(), "", "");
            runOnUiThread(() -> {
                binding.ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
                updateUserSession(currentUser);
                Toast.makeText(ProfileActivity.this,
                        "✓ تم حذف الصورة بنجاح", Toast.LENGTH_SHORT).show();
            });
        });
    }


    private void logout() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Toast.makeText(this, "تم تسجيل الخروج,نتمنى لك يوماً سعيداً!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        binding = null;
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (currentUser != null) {
            executorService.execute(() -> {
                User updatedUser = userDao.getUserById(userId);
                if (updatedUser != null) {
                    currentUser = updatedUser;
                    runOnUiThread(() -> {
                        displayUserProfile();
                    });
                }
            });
        }
    }
}