package datacomprojects.com.camerafocusexample;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otaliastudios.cameraview.PictureResult;

import darthkilersprojects.com.log.L;
import datacomprojects.com.camerafocus.AlertUtils;
import datacomprojects.com.camerafocus.CameraPerformer;
import datacomprojects.com.camerafocus.CameraResultCallBack;


public class MainActivity extends AppCompatActivity {

    CameraPerformer cameraPerformer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPerformer = new CameraPerformer(this, this, this, null)
                .setCamera(findViewById(R.id.camera))
                .setFlashButton(findViewById(R.id.camera_fragment_flash))
                .setTakePicture(findViewById(R.id.camera_fragment_take_photo))
                .setAlertCameraError(findViewById(R.id.alert_camera_error))
                .setAlertCameraErrorTitle(R.id.alertCameraErrorTitle)
                .setAlertCameraErrorBody(R.id.alertCameraErrorBody)
                .setAlertCameraErrorRefresh(R.id.camera_fragment_error_alert_refresh)
                .setBrowseImageView(findViewById(R.id.camera_fragment_browse))
                .setPermissionUtils(new AlertUtils.PermissionUtils("qwe","asd","zxc","rty"))
                //.setUserInactiveAlphaValue(0.8f)
                .setTakeSnapshot(true)
                .setCameraResultCallBack(new CameraResultCallBack() {

                    @Override
                    public void onImageSaved(String filePath, boolean success) {
                        super.onImageSaved(filePath, success);
                        L.show(filePath);
                    }

                    @Override
                    public void onPictureTaken(@NonNull PictureResult result, boolean isSnapshot) {
                        super.onPictureTaken(result, isSnapshot);
                        L.show(result);
                    }

                    @Override
                    public void onBrowseEnd(boolean success, String fileName) {
                        super.onBrowseEnd(success, fileName);
                        L.show(fileName);
                    }

                })
                .build();
        //cameraPerformer.setCameraFocusViewResource(R.drawable.ic_launcher_background);
        //cameraPerformer.setSaveImageFilePath();
        cameraPerformer.setFlashImageResource(R.drawable.ic_launcher_background);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cameraPerformer.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraPerformer.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
