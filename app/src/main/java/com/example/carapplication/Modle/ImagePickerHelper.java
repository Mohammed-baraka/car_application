package com.example.carapplication.Modle;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.carapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickerHelper {

    private AppCompatActivity activity;
    private OnImageSelectedListener listener;
    private Uri cameraImageUri;
    private static final String FILE_PROVIDER_AUTHORITY = "com.carrental.fileprovider";

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public interface OnImageSelectedListener {
        void onImageSelected(String imagePath, Uri imageUri);
        void onError(String error);
    }

    public ImagePickerHelper(AppCompatActivity activity, OnImageSelectedListener listener) {
        this.activity = activity;
        this.listener = listener;
        registerLaunchers();
    }

    private void registerLaunchers() {
        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        handleCameraResult();
                    } else {
                        listener.onError("تم إلغاء التقاط الصورة");
                    }
                });

        galleryLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        handleGalleryResult(selectedImageUri);
                    } else {
                        listener.onError("تم إلغاء اختيار الصورة");
                    }
                });

        requestPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        listener.onError("تم رفض إذن الكاميرا");
                        showPermissionDeniedDialog();
                    }
                });
    }

    public void showImagePickerDialog() {
        String[] options = {"📷 التقاط صورة", "🖼️ اختيار من المعرض", "❌ إلغاء"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("اختر صورة الملف الشخصي");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    checkCameraPermission();
                    break;
                case 1:
                    openGallery();
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(activity,
                        FILE_PROVIDER_AUTHORITY, photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                cameraLauncher.launch(intent);
            }
        } else {
            listener.onError("لا توجد تطبيقات كاميرا متاحة");
        }
    }

    private void openGallery() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
        }

        galleryLauncher.launch(intent);
    }

    private void handleCameraResult() {
        if (cameraImageUri != null) {
            try {
                Bitmap bitmap = getBitmapFromUri(cameraImageUri);

                if (bitmap != null) {
                    bitmap = rotateImageIfNeeded(bitmap, getRealPathFromUri(cameraImageUri));

                    String savedPath = saveBitmapToFile(bitmap, generateFileName());

                    if (savedPath != null) {
                        Uri savedUri = Uri.fromFile(new File(savedPath));
                        listener.onImageSelected(savedPath, savedUri);

                        deleteTempImageFile();
                    } else {
                        listener.onError("فشل في حفظ الصورة");
                    }
                } else {
                    listener.onError("فشل في معالجة الصورة");
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onError("خطأ: " + e.getMessage());
            }
        }
    }

    private void handleGalleryResult(Uri imageUri) {
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri);

            if (bitmap != null) {
                String savedPath = saveBitmapToFile(bitmap, generateFileName());

                if (savedPath != null) {
                    Uri savedUri = Uri.fromFile(new File(savedPath));
                    listener.onImageSelected(savedPath, savedUri);
                } else {
                    listener.onError("فشل في حفظ الصورة");
                }
            } else {
                listener.onError("فشل في قراءة الصورة");
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError("خطأ: " + e.getMessage());
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ContentResolver resolver = activity.getContentResolver();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        try (InputStream inputStream = resolver.openInputStream(uri)) {
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream, null, options);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return MediaStore.Images.Media.getBitmap(resolver, uri);
        }

        return null;
    }

    private String getRealPathFromUri(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try {
            ContentResolver resolver = activity.getContentResolver();
            android.database.Cursor cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                path = cursor.getString(columnIndex);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    private Bitmap rotateImageIfNeeded(Bitmap bitmap, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return bitmap;

        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) return bitmap;

            ExifInterface ei = new ExifInterface(imagePath);
            int orientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateBitmap(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError("فشل في إنشاء ملف الصورة");
            return null;
        }
    }

    private String saveBitmapToFile(Bitmap bitmap, String fileName) {
        File picturesDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) return null;

        File destinationFile = new File(picturesDir, fileName + ".jpg");

        try (FileOutputStream out = new FileOutputStream(destinationFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
            return destinationFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteTempImageFile() {
        if (cameraImageUri != null) {
            try {
                String path = getRealPathFromUri(cameraImageUri);
                if (path != null) {
                    File tempFile = new File(path);
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String generateFileName() {
        return "PROFILE_" + System.currentTimeMillis();
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("الإذن مطلوب")
                .setMessage("تطبيق حجز السيارات يحتاج إلى إذن الكاميرا لالتقاط صورة الملف الشخصي")
                .setPositiveButton("فتح الإعدادات", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    public static void loadImage(AppCompatActivity activity, String imagePath, ImageView imageView) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Uri imageUri = Uri.fromFile(imageFile);
                loadImage(activity, imageUri, imageView);
            } else {
                loadDefaultImage(activity, imageView);
            }
        } else {
            loadDefaultImage(activity, imageView);
        }
    }

    public static void loadImage(AppCompatActivity activity, Uri imageUri, ImageView imageView) {
        if (imageUri != null) {
            try {
                Glide.with(activity)
                        .load(imageUri)
                        .apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
                loadDefaultImage(activity, imageView);
            }
        } else {
            loadDefaultImage(activity, imageView);
        }
    }

    private static void loadDefaultImage(AppCompatActivity activity, ImageView imageView) {
        try {
            Glide.with(activity)
                    .load(R.drawable.ic_profile_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    public static void deleteOldImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File oldFile = new File(imagePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
    }
}