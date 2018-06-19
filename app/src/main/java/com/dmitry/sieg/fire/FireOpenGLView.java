package com.dmitry.sieg.fire;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class FireOpenGLView extends GLSurfaceView {

    private static final int VERSION_OPENGL_ES_2_0 = 2;

    public FireOpenGLView(Context context) {
        super(context);

        setEGLContextClientVersion(VERSION_OPENGL_ES_2_0);

        Renderer renderer = new FireRenderer(context);
        setRenderer(renderer);
    }
}
