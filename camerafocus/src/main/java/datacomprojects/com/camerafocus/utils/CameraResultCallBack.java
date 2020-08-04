package datacomprojects.com.camerafocus.utils;

import android.graphics.PointF;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;

public class CameraResultCallBack {

    @CallSuper
    public void onStartTakePhoto() {

    }

    @CallSuper
    public void onCameraOpened(@NonNull CameraOptions options) {

    }

    @CallSuper
    public void onCameraClosed() {

    }

    @CallSuper
    public void onCameraError(@NonNull CameraException exception) {

    }

    @CallSuper
    public void onPictureTaken(@NonNull PictureResult result,boolean isSnapshot) {

    }


    @CallSuper
    public void onVideoTaken(@NonNull VideoResult result) {

    }

    @CallSuper
    public void onOrientationChanged(int orientation) {

    }

    @CallSuper
    public void onFocusStart(@NonNull PointF point) {

    }

    @CallSuper
    public void onFocusEnd(boolean successful, @NonNull PointF point) {

    }

    @CallSuper
    public void onZoomChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {

    }

    @CallSuper
    public void onExposureCorrectionChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {

    }

    @CallSuper
    public void onImageSaved(String filePath, boolean success) {

    }

    @CallSuper
    public void onBrowse() {

    }

    @CallSuper
    public void onBrowseEnd(boolean success, String fileName) {

    }

    @CallSuper
    public void onBrowseCancel() {

    }

    @CallSuper
    public void onTorchStateChanged(boolean torchOn) {

    }
}
