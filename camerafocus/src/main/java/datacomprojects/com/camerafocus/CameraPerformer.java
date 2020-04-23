package datacomprojects.com.camerafocus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Preview;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;


import darthkilersprojects.com.log.L;
import datacomprojects.com.camerafocus.utils.AlertUtils;
import datacomprojects.com.camerafocus.utils.CameraFocusView;
import datacomprojects.com.camerafocus.utils.CameraResultCallBack;
import datacomprojects.com.camerafocus.utils.ErrorAlert;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CameraPerformer {

    //default value for all inactive elements
    private static final float INACTIVE_ALPHA_VALUE = 0.3f;

    //constants for request permissions
    public static final int CAMERA_PERMISSION = 3;

    public static final int GALLERY_PERMISSION = 4;

    public static final int BROWSE_REQUEST_CODE = 5;

    //public static final int CAMERA_GALLERY_PERMISSION = 6;

    //value for all inactive elements defined by user (default = -1)
    private static float USER_INACTIVE_ALPHA_VALUE = -1f;

    /*
        The variable is responsible for the type of image
        If the variable takes the value true, then a snapshot will be created
        If the variable takes a false value, a normal photo will be created
        Initial value is false
     */
    private boolean takeSnapshot = false;

    /*
        The path where the picture will be saved
        If the value is not specified, the path will be created randomly 1 time per instance of the object
        If you specify the path to external storage, in the absence of permission to read and write to external storage, an exception will be thrown
     */
    private String saveImageFilePath;

    //Instance of the camera object that is being processed
    private CameraView camera;

    //View for take photo event
    private ImageView takePicture;

    //View for import image event
    private ImageView browseImageView;

    //ImageView for torch
    private ImageView flashButton;

    //View for focus
    private CameraFocusView cameraFocusView;

    //context
    private Context context;

    //Activity context
    private AppCompatActivity appCompatActivity;

    //Activity lifecycle owner
    private LifecycleOwner lifecycleOwner;

    //Callback for important camera events
    private CameraResultCallBack cameraResultCallBack;

    //the variable is responsible for blocking some processes in the image processing
    private boolean takenPhoto = false;

    //object for working with a camera error alert (not shown by default)
    private ErrorAlert errorAlert = new ErrorAlert(false);

    //a fragment, if not equal to NULL, intercepts all lifecycle methods
    private Fragment fragment;

    private AlertUtils.PostPermissionUtils permissionUtils;

    //
    private boolean isBrowse = false;

    public boolean canTakePhoto = true;

    /*
        constructor with parameters - context, activate lifecycle and fragment
        if the fragment is != null, the fragment lifecycle will be used
        also initializes view for focus
     */
    public CameraPerformer(@NonNull Context context, @NonNull AppCompatActivity appCompatActivity, @NonNull LifecycleOwner lifecycleOwner,@Nullable Fragment fragment) {
        this.context = context;
        this.appCompatActivity = appCompatActivity;
        this.lifecycleOwner = lifecycleOwner;
        this.fragment = fragment;

        cameraFocusView = new CameraFocusView(context);
    }

    //set inactive alpha value for flash button
    public CameraPerformer setInactiveAlphaValue(@FloatRange(from = 0.0, to = 1.0) float inactiveAlphaValue) {
        USER_INACTIVE_ALPHA_VALUE = inactiveAlphaValue;
        if(flashButton.getAlpha() != 1)
            flashButton.setAlpha(USER_INACTIVE_ALPHA_VALUE);
        return this;
    }

    //set the path where the picture will be saved
    public CameraPerformer setSaveImageFilePath(@NonNull String saveImageFilePath) {
        this.saveImageFilePath = saveImageFilePath;
        return this;
    }

    public CameraPerformer setTakeSnapshot(boolean takeSnapshot) {
        this.takeSnapshot = takeSnapshot;
        return this;
    }

    public CameraPerformer setCamera(CameraView camera) {
        this.camera = camera;
        return this;
    }

    public CameraPerformer setAlertCameraError(ViewGroup alertCameraError) {
        this.errorAlert.setParent(alertCameraError);
        return this;
    }

    public CameraPerformer setTakePicture(ImageView takePicture) {
        this.takePicture = takePicture;
        this.takePicture.setOnClickListener(this::onClick);
        return this;
    }

    public CameraPerformer setFlashButton(ImageView flashButton) {
        this.flashButton = flashButton;
        this.flashButton.setOnClickListener(this::onClick);
        return this;
    }

    public CameraPerformer setAlertCameraErrorTitle(@IdRes int textViewId) {
        errorAlert.setAlertCameraErrorTitleID(textViewId);
        return this;
    }

    public CameraPerformer setAlertCameraErrorBody(@IdRes int textViewId) {
        errorAlert.setAlertCameraErrorBodyID(textViewId);
        return this;
    }

    public CameraPerformer setAlertCameraErrorPositive(@IdRes int textViewId) {
        errorAlert.setAlertCameraErrorPositiveID(textViewId);
        errorAlert.setPositiveClickListener(v -> ((Activity)context).finish());
        return this;
    }

    public CameraPerformer setBrowseImageView(ImageView browseImageView) {
        this.browseImageView = browseImageView;
        browseImageView.setOnClickListener(this::onClick);
        return this;
    }

    public CameraPerformer setPermissionUtils(AlertUtils.PostPermissionUtils permissionUtils) {
        this.permissionUtils = permissionUtils;
        return this;
    }

    public CameraPerformer setShowErrorAlert(boolean showErrorAlert) {
        errorAlert.setNeedToShow(showErrorAlert);
        return this;
    }

    public CameraPerformer setCameraResultCallBack(CameraResultCallBack cameraResultCallBack) {
        this.cameraResultCallBack = cameraResultCallBack;
        return this;
    }

    public CameraPerformer setCameraFocusViewDrawable(Drawable drawable) {
        cameraFocusView.setImageDrawable(drawable);
        return this;
    }

    public CameraPerformer build() {
        if (saveImageFilePath == null)
            saveImageFilePath = context.getApplicationInfo().dataDir + "/" + new Random().nextInt() + new Random().nextBoolean() + "___" + new Random().nextDouble();

        flashButton.setAlpha(INACTIVE_ALPHA_VALUE);
        if (isPermissionsCamera() && (!needStoragePermission() || isPermissionsStorage()))
            photographerInitialize();
        else
            /*if(needStoragePermission())
                requestCameraStoragePermission();
            else*/
                requestPermissionCamera();
        return this;
    }

    private void makeFlashUnavailable() {
        if (flashButton.hasOnClickListeners()) {
            flashButton.setOnClickListener(null);
            flashButton.setAlpha(USER_INACTIVE_ALPHA_VALUE == -1f ? INACTIVE_ALPHA_VALUE : USER_INACTIVE_ALPHA_VALUE);
        }
    }

    public void take() {
        onClick(takePicture);
    }

    private void takePhoto() {
        if (takenPhoto && canTakePhoto)
            return;

        if (isPermissionsCamera() && (!needStoragePermission() || isPermissionsStorage())) {
            if (errorAlert.isNeedToShow() && errorAlert.isVisible())
                errorAlert.shake();
            else {
                cameraResultCallBack.onStartTakePhoto();
                new Thread(() -> {
                    if (takeSnapshot)
                        camera.takePictureSnapshot();
                    else
                        camera.takePicture();
                    takenPhoto = true;
                }).start();
            }
        } else
            /*if(needStoragePermission())
                requestCameraStoragePermission();
            else*/
                requestPermissionCamera();
    }

    private void onClick(View v) {
        if (takePicture != null && v.getId() == takePicture.getId()) {
            if (camera.isOpened()) {
                takePhoto();
                return;
            }
        }

        if (flashButton != null && v.getId() == flashButton.getId()) {
            flash();
            return;
        }

        if (browseImageView != null && v.getId() == browseImageView.getId()) {
            onBrowse();
        }
    }

    private void flash() {
        if (isPermissionsCamera()) {
            if (errorAlert.isNeedToShow() && errorAlert.isVisible())
                errorAlert.shake();
            else {
                camera.setFlash(camera.getFlash().equals(Flash.OFF) ? Flash.TORCH : Flash.OFF);
                callTorchChangeCallback();
            }
        } else
            requestPermissionCamera();
    }

    private void photographerInitialize() {

        camera.setLifecycleOwner(lifecycleOwner);
        camera.setPreview(Preview.TEXTURE);
        if(errorAlert.isNeedToShow())
            errorAlert.setVisibility(View.GONE);

        callTorchChangeCallback();

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
                    flashButton.setOnClickListener(CameraPerformer.this::onClick);
                }

                camera.setZoom(0);

                takePicture.setOnClickListener(CameraPerformer.this::onClick);
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

                switch (reason) {
                    case CameraException.REASON_FAILED_TO_CONNECT:
                    case CameraException.REASON_FAILED_TO_START_PREVIEW:
                        if(errorAlert.isNeedToShow())
                            errorAlert.showErrorAlert();
                        break;
                    case CameraException.REASON_PICTURE_FAILED:
                        Toast.makeText(context,"Take picture error",Toast.LENGTH_SHORT).show();
                        break;
                    case CameraException.REASON_UNKNOWN:
                        camera.close();
                        camera.destroy();
                    default:
                        if(errorAlert.isNeedToShow())
                            errorAlert.showUnknownErrorAlert();
                }

                takePicture.setOnClickListener(CameraPerformer.this::onClick);
                if(errorAlert.isNeedToShow())
                    errorAlert.setVisibility(View.VISIBLE);
                takenPhoto = false;

                cameraResultCallBack.onCameraError(exception);
            }

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                cameraResultCallBack.onPictureTaken(result, takeSnapshot);
                File file1 = new File(saveImageFilePath);
                if(file1.exists())
                    L.show(file1.delete());
                result.toFile(new File(saveImageFilePath), file -> {
                    cameraResultCallBack.onImageSaved(saveImageFilePath, file != null && file.exists());
                    takenPhoto = false;
                });
            }

            @Override
            public void onAutoFocusStart(@NonNull PointF point) {
                super.onAutoFocusStart(point);
                cameraFocusView.pointFocus(point);
                if (cameraFocusView.getParent() == null)
                    camera.addView(cameraFocusView);

                cameraResultCallBack.onFocusStart(point);
            }

            @Override
            public void onAutoFocusEnd(boolean successful, @NonNull PointF point) {
                super.onAutoFocusEnd(successful, point);
                camera.removeView(cameraFocusView);

                cameraResultCallBack.onFocusEnd(successful, point);
            }

        });

    }

    private void callTorchChangeCallback() {
        if(cameraResultCallBack!=null)
            cameraResultCallBack.onTorchStateChanged(camera.getFlash().equals(Flash.TORCH));
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length == 0)
            return;

        switch (requestCode) {
            //case CAMERA_GALLERY_PERMISSION:
            case CAMERA_PERMISSION:
                if (grantResults[0] == PERMISSION_GRANTED) {
                    takePicture.setOnClickListener(null);
                    photographerInitialize();
                } else {
                    takePicture.setOnClickListener(CameraPerformer.this::onClick);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(!appCompatActivity.shouldShowRequestPermissionRationale(permissions[0]))
                            AlertUtils.showCameraPostPermissionAlert(context, () -> {
                                Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + context.getPackageName()));
                                if(fragment!=null)
                                    fragment.startActivityForResult(appSettingsIntent, CAMERA_PERMISSION);
                                else
                                    appCompatActivity.startActivityForResult(appSettingsIntent, CAMERA_PERMISSION);

                            },lifecycleOwner,permissionUtils);
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
        isBrowse = false;
        switch (requestCode) {

            case BROWSE_REQUEST_CODE:
                if (resultCode != RESULT_OK || data == null)
                    break;

                takenPhoto = false;
                Uri uri = data.getData();

                try {
                    FileDescriptor filePath = getRealPathFromURI(context, uri);
                    copyFileUsingStream(filePath, saveImageFilePath);
                    appCompatActivity.runOnUiThread(() -> {
                        cameraResultCallBack.onBrowseEnd(true,saveImageFilePath);
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    cameraResultCallBack.onBrowseEnd(false,saveImageFilePath);
                }
                break;

            case CAMERA_PERMISSION:
                if (needStoragePermission()) {
                    if (isPermissionsCamera() && isPermissionsStorage())
                        photographerInitialize();
                } else
                    if (isPermissionsCamera())
                        photographerInitialize();
        }

    }

    private void onBrowse() {
        if (isPermissionsStorage()) {
            if(canTakePhoto && !isBrowse) {
                cameraResultCallBack.onBrowse();
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_PICK);
                chooseFile.setType("image/*");
                intent = Intent.createChooser(chooseFile, "choose_a_file");
                if (fragment != null)
                    fragment.startActivityForResult(intent, BROWSE_REQUEST_CODE);
                else
                    appCompatActivity.startActivityForResult(intent, BROWSE_REQUEST_CODE);
                isBrowse = true;
            }
        } else
            requestPermissionStorage();
    }

    private void requestPermissionCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(fragment != null)
                fragment.requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            else
                appCompatActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }

    private void requestPermissionStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(fragment != null)
                fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
            else
                appCompatActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
        }
    }

    /*private void requestCameraStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(fragment != null)
                fragment.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_GALLERY_PERMISSION);
            else
                appCompatActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_GALLERY_PERMISSION);
        }
    }*/

    private boolean isPermissionsCamera() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PERMISSION_GRANTED;
    }

    private boolean isPermissionsStorage() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private static FileDescriptor getRealPathFromURI(Context context, Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        if(parcelFileDescriptor != null) {
            return parcelFileDescriptor.getFileDescriptor();
        }
        else
            return null;
    }

    private static void copyFileUsingStream(FileDescriptor source, String dest) throws IOException {
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

    public void addLifecycle() {
        if(isPermissionsCamera())
            camera.setLifecycleOwner(lifecycleOwner);
    }

    private boolean needStoragePermission() {
        return !saveImageFilePath.contains(context.getFilesDir().getParent());
    }

    public void closeCameraAndRemoveLifecycle() {
        camera.close();
        lifecycleOwner.getLifecycle().removeObserver(camera);
    }
}
