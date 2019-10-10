package datacomprojects.com.camerafocus.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;

public class ErrorAlert {


    private boolean needToShow;

    private ViewGroup parent;

    private TextView alertCameraErrorPositiveTextView;
    private TextView alertCameraErrorTitleTextView;
    private TextView alertCameraErrorBodyTextView;

    private String cameraErrorPositiveText = "Ok";
    private String cameraErrorTitle = "Camera initialization error!";
    private String cameraErrorBody = "Please remove from background any application which is using camera.";
    private String cameraUnknownErrorTitle = "Error";
    private String cameraUnknownErrorBody = "Something was wrong. Please restart camera";


    public ErrorAlert() {
        needToShow = false;
    }

    public ErrorAlert(boolean needToShow) {
        this.needToShow = needToShow;
    }


    private ErrorAlert bindErrorAlert(AlertErrorType alertErrorType) {
        alertCameraErrorPositiveTextView.setText(cameraErrorPositiveText);
        switch (alertErrorType){

            case ERROR:
                alertCameraErrorTitleTextView.setText(cameraErrorTitle);
                alertCameraErrorBodyTextView.setText(cameraErrorBody);
                break;
            case UNKNOWN_ERROR:
                alertCameraErrorTitleTextView.setText(cameraUnknownErrorTitle);
                alertCameraErrorBodyTextView.setText(cameraUnknownErrorBody);
                break;
        }
        return this;
    }

    public void showErrorAlert() {
        bindErrorAlert(AlertErrorType.ERROR).show();
    }

    private void show() {
        parent.setVisibility(View.VISIBLE);
    }

    public void showUnknownErrorAlert() {
        bindErrorAlert(AlertErrorType.UNKNOWN_ERROR).show();
    }

    public boolean isNeedToShow() {
        return needToShow;
    }

    public void setNeedToShow(boolean needToShow) {
        this.needToShow = needToShow;
    }

    public void setParent(ViewGroup parent) {
        this.parent = parent;
    }

    public void setAlertCameraErrorPositiveID(@IdRes int alertCameraErrorPositiveID) {
        alertCameraErrorPositiveTextView = parent.findViewById(alertCameraErrorPositiveID);
    }

    public void setAlertCameraErrorTitleID(@IdRes int alertCameraErrorTitleID) {
        alertCameraErrorTitleTextView = parent.findViewById(alertCameraErrorTitleID);
    }

    public void setAlertCameraErrorBodyID(@IdRes int alertCameraErrorBodyID) {
        alertCameraErrorBodyTextView = parent.findViewById(alertCameraErrorBodyID);
    }

    public void shake() {
        new Shaker(parent).shake();
    }

    public boolean isVisible() {
        return parent.getVisibility() == View.VISIBLE;
    }

    public void setVisibility(int visibility) {
        parent.setVisibility(visibility);
    }

    public void setPositiveClickListener(View.OnClickListener onClickListener) {
        alertCameraErrorPositiveTextView.setOnClickListener(onClickListener);
    }

    private enum AlertErrorType {
        ERROR,
        UNKNOWN_ERROR
    }
}
