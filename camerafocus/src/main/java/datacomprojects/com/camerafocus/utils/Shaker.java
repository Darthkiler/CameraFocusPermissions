package datacomprojects.com.camerafocus.utils;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class Shaker {

    private AnimatorSet set = new AnimatorSet();

    public Shaker(View view){
        float f=-3;

        ObjectAnimator[] shaker = new ObjectAnimator[20];
        for(int i = 0; i< shaker.length-1; i++) {
            shaker[i]=ObjectAnimator.ofFloat(view, "translationX", i%2==0?a(f):-a(f));
            shaker[i].setDuration(50);
            f+=((float)2/ shaker.length);
        }

        shaker[shaker.length-1] = ObjectAnimator.ofFloat(view, "translationX", 0);
        shaker[shaker.length-1].setDuration(75);
        set.playSequentially(shaker);
    }

    private int a(float f){
        int i=15;
        i=(int) (i*f(f));
        return i;
    }

    private float f(float x)
    {
        return -(x*x)-4*x-3;
    }

    public void shake(){
        set.start();
    }
}
