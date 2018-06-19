package com.dmitry.sieg.fire;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;

public class FireCanvasView extends View {

    private static final int S_WIDTH = 32;
    private static final int S_HEIGHT = 32;
    private static final int I_HEIGHT = 1; // height of noise initiator;

    private Bitmap[] bitmap = new Bitmap[2];
    private Matrix matrix;
    private Paint paint;
    private int b_index; // each onDraw it points to the current (old) bitmap.

    public FireCanvasView(Context context) {
        super(context);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bitmap[0] = Bitmap.createBitmap(S_WIDTH, S_HEIGHT + I_HEIGHT, conf);
        bitmap[1] = Bitmap.createBitmap(S_WIDTH, S_HEIGHT  + I_HEIGHT, conf);
        b_index = 0;

        matrix = new Matrix();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        matrix.setRectToRect(new RectF(0.0f, 0.0f, (float) S_WIDTH, (float) S_HEIGHT),
                new RectF(0.0f, 0.0f, (float) dm.widthPixels, (float) dm.heightPixels),
                Matrix.ScaleToFit.END);

        paint = new Paint();
        paint.setDither(false);

        initiateNoise(bitmap[b_index]);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final Bitmap oldBitmap = bitmap[b_index];
        final int new_index = (b_index + 1) % 2;
        final Bitmap newBitmap = bitmap[new_index];

        processFire(oldBitmap, newBitmap);

        for (int i = 0; i < S_WIDTH; i++) {
            newBitmap.setPixel(i, 0, indexToColor(1.0f / ((float) S_WIDTH) * ((float) i)));
        }

        canvas.drawBitmap(newBitmap, matrix, paint);

        initiateNoise(newBitmap);
        b_index = new_index;
    }

    private void initiateNoise(final Bitmap bitmap) {
        for (int y = 0; y < I_HEIGHT; y++) {
            for (int x = 0; x < S_WIDTH; x++) {
                if (Math.random() > 0.2) {
                    bitmap.setPixel(x, S_HEIGHT + y, indexToColor(1.0f));
                }
            }
        }
    }

    private void processFire(final Bitmap oldBitmap, final Bitmap newBitmap) {
        for (int y = S_HEIGHT - 1; y >=0; y--) {
            for (int x = 0; x < S_WIDTH; x++) {

                float sum = 0.0f;

                // summing by pattern:
                //
                //  S
                // +++
                //  +
                //
                // and dividing it by the value slightly > than 4.
                if (y < S_HEIGHT + I_HEIGHT - 2) {
                    sum += colorToIndex(oldBitmap.getPixel(x, y + 2));
                }
                if (y < S_HEIGHT + I_HEIGHT - 1) {
                    if (x > 0) {
                        sum += colorToIndex(oldBitmap.getPixel(x - 1, y + 1));
                    }
                    if (x < S_WIDTH - 1) {
                        sum += colorToIndex(oldBitmap.getPixel(x + 1, y + 1));
                    }
                    sum += colorToIndex(oldBitmap.getPixel(x, y + 1));
                }

                newBitmap.setPixel(x, y, indexToColor(sum * 4.0f / 17.0f));
            }
        }
    }

    private float colorToIndex(final int color) {
        final float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv[2];
    }

    /**
     * index = [0, 1].
     * @param index
     * @return
     */
    private int indexToColor(final float index) {
        final float inv = 1.0f - index;
        return Color.HSVToColor(new float[]{360.0f / 3.0f * index, 1.0f, Math.min(1.0f, index * 2.0f)});
    }
}
