package com.dmitry.sieg.fire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {

    private FireCanvasView view;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new FireCanvasView(this);
        setContentView(view);

        view.setOnTouchListener(new FireTouchListener(this));

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long t = System.currentTimeMillis();
                try {
                    while (true) {
                        final long ct = System.currentTimeMillis();
                        if (ct - t > 1000 / 24) {
                            t = ct;
                            view.postInvalidate();
                        }
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    // exit
                }
            }
        });
        thread.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }

    private class FireTouchListener implements View.OnTouchListener {

        private final Activity activity;

        FireTouchListener(Activity activity) {
            this.activity = activity;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            activity.finishAffinity();
            return false;
        }
    }
}
