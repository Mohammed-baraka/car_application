package com.example.carapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carapplication.DataBase.AppDatabase;
import com.example.carapplication.DataBase.UserDao;
import com.example.carapplication.Modle.User;
import com.example.carapplication.Notifications.NotificationHelper;
import com.example.carapplication.databinding.ActivityRegisterBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,13}$"
    );
    private ActivityRegisterBinding binding;
    private AppDatabase appDatabase;
    private UserDao userDao;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appDatabase = AppDatabase.getInstance(this);
        userDao = appDatabase.userDao();


        binding.btnRegister.setOnClickListener(v -> registerUser());

        binding.tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        binding.tvTerms.setOnClickListener(v -> {
            Toast.makeText(this, "سيتم فتح الشروط والأحكام", Toast.LENGTH_SHORT).show();
        });

        binding.tvPrivacy.setOnClickListener(v -> {
            Toast.makeText(this, "سيتم فتح سياسة الخصوصية", Toast.LENGTH_SHORT).show();
        });

        binding.btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "التسجيل عبر جوجل (قريباً)", Toast.LENGTH_SHORT).show();
        });

        binding.btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "التسجيل عبر فيسبوك (قريباً)", Toast.LENGTH_SHORT).show();
        });

        setupTextWatchers();
    }


    private void setupTextWatchers() {
        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });

        binding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePassword();
            }
        });

        binding.etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateConfirmPassword();
            }
        });

        binding.etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePhone();
            }
        });

        binding.etFullName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateFullName();
            }
        });
    }

    private void registerUser() {
        if (!validateFullName() | !validateEmail() | !validatePhone() |
                !validatePassword() | !validateConfirmPassword() | !validateTerms()) {
            return;
        }

        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        executorService.execute(() -> {
            User existingUser = userDao.getUserByEmail(email);

            runOnUiThread(() -> {
                if (existingUser != null) {
                    binding.tilEmail.setError("البريد الإلكتروني مسجل مسبقاً");
                    binding.tilEmail.requestFocus();
                } else {
                    createNewUser(fullName, email, password, phone);
                }
            });
        });
    }

    private void createNewUser(String fullName, String email, String password, String phone) {
        executorService.execute(() -> {
            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setPassword(password);
            newUser.setProfileImage("");
            long userId = userDao.insert(newUser);
            newUser.setId((int) userId);

            saveUserSession(newUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "اهلا وسهلا بك في تطبيق حجز السيارات", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    private void saveUserSession(User user) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", true)
                .putInt("user_id", user.getId())
                .putString("user_name", user.getFullName())
                .putString("user_email", user.getEmail())
                .putString("user_phone", user.getPhone())
                .apply();
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

    private boolean validateEmail() {
        String email = binding.etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            binding.tilEmail.setError("البريد الإلكتروني مطلوب");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            binding.tilEmail.setError("البريد الإلكتروني غير صحيح");
            return false;
        }

        binding.tilEmail.setError(null);
        return true;
    }

    private boolean validatePhone() {
        String phone = binding.etPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            binding.tilPhone.setError("رقم الهاتف مطلوب");
            return false;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            binding.tilPhone.setError("رقم الهاتف غير صحيح (10-13 رقم)");
            return false;
        }

        binding.tilPhone.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = binding.etPassword.getText().toString().trim();

        if (password.isEmpty()) {
            binding.tilPassword.setError("كلمة المرور مطلوبة");
            return false;
        }

        if (password.length() < 8) {
            binding.tilPassword.setError("كلمة المرور يجب أن تكون 8 أحرف على الأقل");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            binding.tilPassword.setError("كلمة المرور يجب أن تحتوي على حرف كبير وصغير ورقم ورمز خاص");
            return false;
        }

        binding.tilPassword.setError(null);
        return true;
    }

    private boolean validateConfirmPassword() {
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError("تأكيد كلمة المرور مطلوب");
            return false;
        }

        if (!confirmPassword.equals(password)) {
            binding.tilConfirmPassword.setError("كلمة المرور غير متطابقة");
            return false;
        }

        binding.tilConfirmPassword.setError(null);
        return true;
    }

    private boolean validateTerms() {
        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(this, "يجب الموافقة على الشروط والأحكام", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        binding = null;
    }
}