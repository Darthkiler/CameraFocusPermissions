package datacomprojects.com.camerafocus.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.Locale;

import datacomprojects.com.camerafocus.R;

public class AlertUtils {

    private static AlertDialog simpleAlertDialog;
    private static Lifecycle mLifecycle;
    private static LifecycleObserver lifecycleObserver;

    public static void showCameraPostPermissionAlert(Context context, Runnable positiveRunnable, LifecycleOwner lifecycleOwner) {
        showPostPermissionAlert(context, positiveRunnable, new PostPermissionUtils("Camera", "%s needs access to %s. To permit %s access, please go to settings", "Settings", "Cancel"));

        setLifecycleOwner(lifecycleOwner);
    }

    public static void showCameraPostPermissionAlert(Context context, Runnable positiveRunnable, LifecycleOwner lifecycleOwner, PostPermissionUtils permissionUtils) {
        if(permissionUtils!=null)
            showPostPermissionAlert(context, positiveRunnable,permissionUtils);
        else
            showCameraPostPermissionAlert(context, positiveRunnable, lifecycleOwner);
    }

    private static void showPostPermissionAlert(Context context, Runnable positiveRunnable, PostPermissionUtils permissionUtils) {
        String body = String.format(Locale.getDefault(),
                permissionUtils.bodyFormat,
                context.getString(R.string.app_name),
                permissionUtils.permission,
                permissionUtils.permission);

        showAlertWithTwoButtons(context,
                body,
                permissionUtils.posButton,
                positiveRunnable,
                permissionUtils.negButton,
                null);
    }

    private static void showAlertWithTwoButtons(Context context, String body, final String posButton,
                                                @NonNull final Runnable posRunnable, String negButton, final Runnable negRunnable) {
        View view = createView(context);

        TextView positiveButton = view.findViewById(R.id.positiveButton);
        TextView negativeButton = view.findViewById(R.id.negativeButton);

        negativeButton.setVisibility(View.VISIBLE);

        ((TextView) view.findViewById(R.id.body)).setText(body);
        positiveButton.setText(posButton);
        negativeButton.setText(negButton);

        positiveButton.setOnClickListener(v -> {
            simpleAlertDialog.dismiss();
            posRunnable.run();
        });

        negativeButton.setOnClickListener(v -> {
            simpleAlertDialog.dismiss();
            if (negRunnable != null)
                negRunnable.run();
        });

        if (!((Activity) context).isFinishing())
            simpleAlertDialog.show();
    }

    private static View createView(Context context) {
        //dismissAlerts();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.alert_custom_body, null);
        builder.setView(view);
        simpleAlertDialog = builder.create();
        return view;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private static void dismissAlerts() {
        try {
            if (simpleAlertDialog != null && simpleAlertDialog.isShowing())
                simpleAlertDialog.dismiss();
        } catch (Exception ignore) {
        }

        dismissLifeCycle();
    }

    private static void dismissLifeCycle() {
        mLifecycle = null;
    }

    private static void setLifecycleOwner(LifecycleOwner owner) {
        if (mLifecycle != null)
            mLifecycle.removeObserver(lifecycleObserver);
        if (owner != null)
            mLifecycle = owner.getLifecycle();
        lifecycleObserver = (LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_PAUSE)
                dismissAlerts();
        };
        if (mLifecycle != null)
            mLifecycle.addObserver(lifecycleObserver);
    }

    public static class PostPermissionUtils {
        String permission;
        String bodyFormat;
        String posButton;
        String negButton;

        public PostPermissionUtils(String permission, String bodyFormat, String posButton, String negButton) {
            this.permission = permission;
            this.bodyFormat = bodyFormat;
            this.posButton = posButton;
            this.negButton = negButton;
        }
    }
}
