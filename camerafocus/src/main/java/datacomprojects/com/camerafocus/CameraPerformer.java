package datacomprojects.com.camerafocus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CameraPerformer implements View.OnClickListener {
    private static final float INACTIVE_ALPHA_VALUE = 0.3f;
    private static final int CAMERA_PERMISSION = 3;
    private static final int GALLERY_PERMISSION = 4;
    private static float USER_INACTIVE_ALPHA_VALUE = -1f;
    private boolean takeSnapshot = false;
    private String saveImageFilePath;
    private CameraView camera;
    private ViewGroup alertCameraError;
    private ImageView takePicture;
    private ImageView flashButton;
    private ImageView browseImageView;
    private TextView alertCameraErrorRefresh;
    private TextView alertCameraErrorTitle;
    private TextView alertCameraErrorBody;
    private CameraFocusView cameraFocusView;
    private Context context;
    private AppCompatActivity appCompatActivity;
    private LifecycleOwner lifecycleOwner;
    private CameraResultCallBack cameraResultCallBack;
    private boolean takenPhoto = false;
    private String cameraErrorTitle = "Camera initialization error!";
    private String cameraErrorBody = "Please remove from background any application which is using camera.";
    private String cameraUnknownErrorTitle = "Error";
    private String cameraunknownErrorBody = "Something was wrong. Please restart camera";
    private String cameraRefreshText = "Refresh";

    public CameraPerformer(@NonNull Context context, @NonNull AppCompatActivity appCompatActivity, @NonNull LifecycleOwner lifecycleOwner) {

        this.context = context;
        this.appCompatActivity = appCompatActivity;
        this.lifecycleOwner = lifecycleOwner;

        cameraFocusView = new CameraFocusView(context);

    }

    public CameraPerformer setInactiveAlphaValue(@FloatRange(from = 0.0, to = 1.0) float inactiveAlphaValue) {
        USER_INACTIVE_ALPHA_VALUE = inactiveAlphaValue;
        flashButton.setAlpha(USER_INACTIVE_ALPHA_VALUE);
        return this;
    }

    public void setSaveImageFilePath(@NonNull String saveImageFilePath) {
        this.saveImageFilePath = saveImageFilePath;
    }

    public CameraPerformer setTakeSnapshot(boolean takeSnapshot) {
        this.takeSnapshot = takeSnapshot;
        return this;
    }

    public void setFlashImageResource(@DrawableRes int idRes) {
        flashButton.setImageResource(idRes);
    }

    public CameraPerformer setCamera(CameraView camera) {
        this.camera = camera;
        return this;
    }

    public CameraPerformer setAlertCameraError(ViewGroup alertCameraError) {
        this.alertCameraError = alertCameraError;
        return this;
    }

    public CameraPerformer setTakePicture(ImageView takePicture) {
        this.takePicture = takePicture;
        return this;
    }

    public CameraPerformer setFlashButton(ImageView flashButton) {
        this.flashButton = flashButton;
        return this;
    }

    public CameraPerformer setAlertCameraErrorTitle(@IdRes int textViewId) {
        if (alertCameraError == null)
            throw new RuntimeException("Please, indicate ID for error layout");
        TextView textView = alertCameraError.findViewById(textViewId);
        if (textView == null)
            throw new RuntimeException("This layout does not contain textView with this ID");
        this.alertCameraErrorTitle = textView;
        return this;
    }

    public CameraPerformer setAlertCameraErrorBody(@IdRes int textViewId) {
        if (alertCameraError == null)
            throw new RuntimeException("Please, indicate ID for error layout");
        TextView textView = alertCameraError.findViewById(textViewId);
        if (textView == null)
            throw new RuntimeException("This layout does not contain textView with this ID");
        this.alertCameraErrorBody = textView;
        return this;
    }

    public CameraPerformer setAlertCameraErrorRefresh(@IdRes int textViewId) {
        if (alertCameraError == null)
            throw new RuntimeException("Please, indicate ID for error layout");
        TextView textView = alertCameraError.findViewById(textViewId);
        if (textView == null)
            throw new RuntimeException("This layout does not contain textView with this ID");
        this.alertCameraErrorRefresh = textView;
        this.alertCameraErrorRefresh.setOnClickListener(this);
        if(this.alertCameraErrorRefresh.getText()=="")
            this.alertCameraErrorRefresh.setText(cameraRefreshText);
        return this;
    }

    public CameraPerformer setBrowseImageView(ImageView browseImageView) {
        this.browseImageView = browseImageView;
        browseImageView.setOnClickListener(this);
        return this;
    }

    public CameraPerformer build() {
        if (saveImageFilePath == null)
            saveImageFilePath = context.getApplicationInfo().dataDir + "/" + new Random().nextInt() + new Random().nextBoolean() + "___" + new Random().nextDouble();
        Field[] attributes = CameraPerformer.class.getDeclaredFields();
        for (Field field : attributes) {
            try {
                if (field.get(this) == null) {
                    throw new RuntimeException("Please, initialize all parameters (" + field.getName() + ")");

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        flashButton.setAlpha(INACTIVE_ALPHA_VALUE);
        if (isPermissionsCamera())
            photographerInitialize();
        else
            requestPermissionCamera();
        return this;
    }

    private void makeFlashUnavailable() {
        if (flashButton.hasOnClickListeners()) {
            flashButton.setOnClickListener(null);
            if (USER_INACTIVE_ALPHA_VALUE == -1f)
                flashButton.setAlpha(INACTIVE_ALPHA_VALUE);
            else
                flashButton.setAlpha(USER_INACTIVE_ALPHA_VALUE);
        }
    }

    private void takePhoto() {
        if (takenPhoto)
            return;

        if (isPermissionsCamera()) {

            if (alertCameraError.getVisibility() == View.VISIBLE)
                new Shaker(alertCameraError).shake();
            else {
                if (takeSnapshot)
                    camera.takePictureSnapshot();
                else
                    camera.takePicture();
                takenPhoto = true;
            }
        } else
            requestPermissionCamera();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == takePicture.getId()) {
            takePhoto();
            return;
        }

        if (v.getId() == flashButton.getId()) {
            flash();
            return;
        }

        if (v.getId() == browseImageView.getId()) {
            onBrowse();
            return;
        }

        if (v.getId() == alertCameraErrorRefresh.getId()) {
            photographerInitialize();
        }
    }

    private void flash() {
        if (alertCameraError.getVisibility() == View.VISIBLE)
            new Shaker(alertCameraError).shake();
        else
            camera.setFlash(camera.getFlash().equals(Flash.OFF) ? Flash.TORCH : Flash.OFF);
    }

    public CameraPerformer setCameraResultCallBack(CameraResultCallBack cameraResultCallBack) {
        this.cameraResultCallBack = cameraResultCallBack;
        return this;
    }

    private void photographerInitialize() {

        camera.setLifecycleOwner(lifecycleOwner);

        alertCameraError.setVisibility(View.GONE);

        camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                cameraResultCallBack.onVideoTaken(result);
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
                cameraResultCallBack.onOrientationChanged(orientation);
            }

            @Override
            public void onZoomChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {
                super.onZoomChanged(newValue, bounds, fingers);
                cameraResultCallBack.onZoomChanged(newValue, bounds, fingers);
            }

            @Override
            public void onExposureCorrectionChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {
                super.onExposureCorrectionChanged(newValue, bounds, fingers);
                cameraResultCallBack.onExposureCorrectionChanged(newValue, bounds, fingers);
            }

            @Override
            public void onCameraOpened(@NonNull CameraOptions options) {
                super.onCameraOpened(options);

                if (options.getSupportedFlash().contains(Flash.TORCH)) {
                    flashButton.setAlpha(1f);
                    flashButton.setOnClickListener(CameraPerformer.this);
                }

                camera.setZoom(0);

                takePicture.setOnClickListener(CameraPerformer.this);
                cameraResultCallBack.onCameraOpened(options);

            }

            @Override
            public void onCameraClosed() {
                super.onCameraClosed();
                takePicture.setOnClickListener(null);
                takenPhoto = false;

                makeFlashUnavailable();
                cameraResultCallBack.onCameraClosed();
            }

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                super.onCameraError(exception);

                int reason = exception.getReason();

                if (reason == CameraException.REASON_FAILED_TO_CONNECT || reason == CameraException.REASON_FAILED_TO_START_PREVIEW) {
                    alertCameraErrorTitle.setText(cameraErrorTitle);
                    alertCameraErrorBody.setText(cameraErrorBody);
                } else {
                    alertCameraErrorTitle.setText(cameraUnknownErrorTitle);
                    alertCameraErrorBody.setText(cameraunknownErrorBody);
                }

                switch (reason) {
                    case CameraException.REASON_PICTURE_FAILED:
                        //AlertUtils.showErrorAlert(context, AlertUtils.TYPE_TAKE_PICTURE_ERROR);
                        break;
                    case CameraException.REASON_UNKNOWN:
                        camera.close();
                        camera.destroy();
                    default:
                        takePicture.setOnClickListener(CameraPerformer.this);
                        alertCameraError.setVisibility(View.VISIBLE);
                        takenPhoto = false;
                }

                cameraResultCallBack.onCameraError(exception);
            }

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                cameraResultCallBack.onPictureTaken(result, takeSnapshot);

                result.toFile(new File(saveImageFilePath), file -> {
                    cameraResultCallBack.onImageSaved(saveImageFilePath, file != null && file.exists());
                    takenPhoto = false;
                });

            }

            @Override
            public void onFocusStart(@NonNull PointF point) {
                super.onFocusStart(point);
                cameraFocusView.pointFocus(point);
                if (cameraFocusView.getParent() == null)
                    camera.addView(cameraFocusView);

                cameraResultCallBack.onFocusStart(point);
            }

            @Override
            public void onFocusEnd(boolean successful, @NonNull PointF point) {
                super.onFocusEnd(successful, point);
                camera.removeView(cameraFocusView);

                cameraResultCallBack.onFocusEnd(successful, point);
            }

        });

    }

    public void setCameraFocusViewResource(@DrawableRes int resId) {
        cameraFocusView.setImageResource(resId);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length == 0)
            return;


        switch (requestCode) {

            case CAMERA_PERMISSION:
                if (grantResults[0] == PERMISSION_GRANTED) {
                    takePicture.setOnClickListener(null);
                    photographerInitialize();
                } else {
                    takePicture.setOnClickListener(CameraPerformer.this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(!appCompatActivity.shouldShowRequestPermissionRationale(permissions[0]))
                            AlertUtils.showCameraPostPermissionAlert(context, () -> {
                                Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + context.getPackageName()));
                                appCompatActivity.startActivityForResult(appSettingsIntent, CAMERA_PERMISSION);
                            },lifecycleOwner);
                    }
                }
                break;

            case GALLERY_PERMISSION:
                if (grantResults[0] == PERMISSION_GRANTED)
                    onBrowse();
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case 1:
                if (resultCode != RESULT_OK || data == null)
                    break;

                takenPhoto = true;
                Uri uri = data.getData();
                String filePath = getRealPathFromURI(context, uri);

                new Thread(() -> {
                    try {
                        copyFileUsingStream(filePath,saveImageFilePath);
                        cameraResultCallBack.onBrowseEnd(true,saveImageFilePath);
                    }
                    catch (Exception e)
                    {
                        cameraResultCallBack.onBrowseEnd(false,saveImageFilePath);

                    }

                }).start();
                break;

            case CAMERA_PERMISSION:
                if (isPermissionsCamera())
                    photographerInitialize();
        }

    }

    private void onBrowse() {
        if (isPermissionsStorage()) {
            cameraResultCallBack.onBrowse();
            Intent chooseFile;
            Intent intent;
            chooseFile = new Intent(Intent.ACTION_PICK);
            chooseFile.setType("image/*");
            intent = Intent.createChooser(chooseFile, "choose_a_file");
            appCompatActivity.startActivityForResult(intent, 1);
        } else
            requestPermissionStorage();
    }

    private void requestPermissionCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appCompatActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }

    private void requestPermissionStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appCompatActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
        }
    }

    private boolean isPermissionsCamera() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PERMISSION_GRANTED;
    }

    private boolean isPermissionsStorage() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private <T extends View> T findViewById(@IdRes int id) {
        return appCompatActivity.findViewById(id);
    }

    private static String getRealPathFromURI(Context context, Uri contentUri) {
        try {
            Cursor cursor = context.getContentResolver().query(contentUri, new String[] {MediaStore.Images.Media.DATA}, null, null, null);
            if (Objects.requireNonNull(cursor).moveToFirst()) {
                int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                String path = column_index != -1 ? cursor.getString(column_index) : null;
                cursor.close();
                return path;
            } else {
                cursor.close();
                return null;
            }
        } catch (Exception ignore){
            return null;
        }
    }

    private static void copyFileUsingStream(String source, String dest) throws Exception {
        InputStream is = new FileInputStream(source);
        OutputStream os = new FileOutputStream(dest);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.close();
    }
}
