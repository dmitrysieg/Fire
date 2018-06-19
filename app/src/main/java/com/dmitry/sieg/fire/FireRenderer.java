package com.dmitry.sieg.fire;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_REPEAT;

public class FireRenderer implements GLSurfaceView.Renderer {

    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    private static float vertices[];
    private static short indices[];
    private static float uvs[];
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer uvBuffer;

    private float mScreenWidth;
    private float mScreenHeight;
    private float sqrSize = 100.0f;

    private Context context;

    private static final String IMAGE_PLANE = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAG9SURBVEhLtZRhdsMwCINz9B1tN8sEwjLGJG3Tt+95FLBASX7sOP+ZS4OfQdRP6Q3yXtqAqDfub6tBUefrdoua7S1YDIro16GCEQLB0tsGlOpnGoO8ETF75PldTI/jYBVYga6Y3TSmnIKcgyxGszdgAXRZxkB2koxNRsqqAVqRjjGo8xhLgY5kSBS5HVwasK3Lq/m9zzySdTvo34DRBtbvA1gKdZg0BvbnXW4EU+2ROWOBU4hM2KyfKH4cKgSvuAKlmSckwAp1QG9Asg1ngEobxe2I6Fjm2xEpA96bLAWBQnsntmxuZ5RHaLbtoDEANj/gjHXwd+GRlYWlBQkOc99g5FJ7c5QHIsWZ2cI91nBTtFb8JvayLB6UCVo2Bu94INd2xt0jDIruxgO3PLiVnnE/ptcb+K91Ge9OQo8STzMO1uJVEaHxC8dTWwEkLYcDUroLK2NobDsNrOnF/Ppgzvthf7+dcX2VYmDQoByx95voLzKPfZzo2499IDiusAmiXvG5Nc4HDjC7GLwPx5btjON/n3hioCe13JN7D/CZge8xSinYz3xmUOALRXHBc4P8uW746g1oAKLu+MrgNef5B199EBMNu5y/AAAAAElFTkSuQmCC";

    FireRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        SetupTriangle();
        SetupImage();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        int vertexShader = FireGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, FireGraphicTools.vs_Image);
        int fragmentShader = FireGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, FireGraphicTools.fs_Image);

        FireGraphicTools.sp_Image = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(FireGraphicTools.sp_Image, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(FireGraphicTools.sp_Image, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(FireGraphicTools.sp_Image);                  // creates OpenGL ES program executables

        GLES20.glUseProgram(FireGraphicTools.sp_Image);
    }

    private float[] createVertices() {
        return new float[] {
                sqrSize,    sqrSize,    0.0f,
                sqrSize,    0.0f,       0.0f,
                0.0f,       0.0f,       0.0f,
                0.0f,       sqrSize,    0.0f,
        };
    }

    private void SetupTriangle() {
        vertices = createVertices();

        indices = new short[] {0, 1, 2, 0, 2, 3};

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    private void SetupImage() {
        uvs = new float[] {
                0.0f,   0.0f,
                0.0f,   1.0f,
                1.0f,   1.0f,
                1.0f,   0.0f
        };

        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);

        int id = context.getResources().getIdentifier("drawable/plane", null, context.getPackageName());

        //Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        int w = 32;
        int h = 32;

        byte[] image_bytes = Base64.decode(IMAGE_PLANE, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(image_bytes, 0, image_bytes.length);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        bmp.recycle();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Render(mtrxProjectionAndView);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
        sqrSize = Math.min(width, height);
        SetupTriangle();

        GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);

        for(int i=0;i<16;i++) {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }

    private void Render(float[] m) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int mPositionHandle = GLES20.glGetAttribLocation(FireGraphicTools.sp_Image, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        int mTexCoordLoc = GLES20.glGetAttribLocation(FireGraphicTools.sp_Image, "a_texCoord");

        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

        int mtrxhandle = GLES20.glGetUniformLocation(FireGraphicTools.sp_Image, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        int mSamplerLoc = GLES20.glGetUniformLocation (FireGraphicTools.sp_Image, "s_texture" );
        GLES20.glUniform1i(mSamplerLoc, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }
}
