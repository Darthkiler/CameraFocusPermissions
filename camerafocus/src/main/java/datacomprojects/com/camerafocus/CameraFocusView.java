package datacomprojects.com.camerafocus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;


public class CameraFocusView extends androidx.appcompat.widget.AppCompatImageView {

    AnimatorSet animatorSet;

    AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            ViewParent parent = getParent();
            if(parent != null)
                ((ViewGroup) parent).removeView(CameraFocusView.this);
        }
    };

    public CameraFocusView(Context context) {
        super(context);
        setImageResource(R.drawable.ic_focus);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        animatorSet = new AnimatorSet();

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.7f);
        alphaAnim.setRepeatMode(ValueAnimator.REVERSE);
        alphaAnim.setRepeatCount(5);

        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.1f);
        scaleXAnim.setRepeatMode(ValueAnimator.REVERSE);
        scaleXAnim.setRepeatCount(5);

        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.1f);
        scaleYAnim.setRepeatMode(ValueAnimator.REVERSE);
        scaleYAnim.setRepeatCount(5);

        animatorSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim);
        animatorSet.setDuration(300);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animatorSet.addListener(animatorListenerAdapter);
        animatorSet.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animatorSet.removeListener(animatorListenerAdapter);
        animatorSet.cancel();
    }

    public void pointFocus(PointF pointF){
        setTranslationX(pointF.x - getWidth()/2f);
        setTranslationY(pointF.y - getHeight()/2f);
        animatorListenerAdapter.onAnimationEnd(animatorSet);
    }
}
