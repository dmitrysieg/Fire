package com.dmitry.sieg.fire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {

    private GLSurfaceView mGLView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLView = new FireView(this);
        setContentView(mGLView);

        mGLView.setOnTouchListener(new FireTouchListener(this));
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
