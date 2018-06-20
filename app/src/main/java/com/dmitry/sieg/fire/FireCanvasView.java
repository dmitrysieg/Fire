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
    private int[][][] fireIndex = new int[2][S_HEIGHT + I_HEIGHT][S_WIDTH];
    private Matrix matrix;
    private Paint paint;
    private int b_index;

    public FireCanvasView(Context context) {
        super(context);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bitmap[0] = Bitmap.createBitmap(S_WIDTH, S_HEIGHT + I_HEIGHT, conf);
        bitmap[1] = Bitmap.createBitmap(S_WIDTH, S_HEIGHT  + I_HEIGHT, conf);
        b_index = 0;

        init2DArray(fireIndex[0]);
        init2DArray(fireIndex[1]);

        matrix = new Matrix();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        matrix.setRectToRect(new RectF(0.0f, 0.0f, (float) S_WIDTH, (float) S_HEIGHT),
                new RectF(0.0f, 0.0f, (float) dm.widthPixels, (float) dm.heightPixels),
                Matrix.ScaleToFit.END);

        paint = new Paint();
        paint.setDither(false);

        initiateNoise(fireIndex[b_index]);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int[][] oldFireIndex = fireIndex[b_index];

        final int new_index = (b_index + 1) % 2;
        final Bitmap newBitmap = bitmap[new_index];
        final int[][] newFireIndex = fireIndex[new_index];

        processFire(newBitmap, oldFireIndex, newFireIndex);

        canvas.drawBitmap(newBitmap, matrix, paint);

        initiateNoise(newFireIndex);
        b_index = new_index;
    }

    private void initiateNoise(final int[][] newFireIndex) {
        for (int y = 0; y < I_HEIGHT; y++) {
            for (int x = 0; x < S_WIDTH; x++) {
                final double r = Math.random();
                if (r > 0.5 && r < 0.8) {
                    newFireIndex[S_HEIGHT + y][x] = (int)(255.0f * r);
                } else {
                    newFireIndex[S_HEIGHT + y][x] = 0;
                }
            }
        }
    }

    private void processFire(final Bitmap newBitmap,
                             final int[][] oldFireIndex,
                             final int[][] newFireIndex) {
        for (int y = S_HEIGHT - 1; y >=0; y--) {
            for (int x = 0; x < S_WIDTH; x++) {

                int sum = 0;

                // summing by pattern:
                //
                //  S
                // +++
                //  +
                //
                // and dividing it by the value slightly > than 4.
                if (y < S_HEIGHT + I_HEIGHT - 2) {
                    sum += oldFireIndex[y + 2][x];
                }
                if (y < S_HEIGHT + I_HEIGHT - 1) {
                    if (x > 0) {
                        sum += oldFireIndex[y + 1][x - 1];
                    }
                    if (x < S_WIDTH - 1) {
                        sum += oldFireIndex[y + 1][x + 1];
                    }
                    sum += oldFireIndex[y + 1][x];
                }

                final int index = (int)(sum * 0.249);
                newFireIndex[y][x] = index;
                newBitmap.setPixel(x, y, indexToColor(index));
            }
        }
    }

    /**
     * index = [0, 255].
     */
    private int indexToColor(final int index) {
        final float nindex = ((float) index) / 256.0f;
        return Color.HSVToColor(new float[]{360.0f / 3.0f * nindex, 1.0f, Math.min(1.0f, nindex * 2.0f)});
    }

    private void init2DArray(final int arr[][]) {
        for (int i = 0; i < S_HEIGHT; i++) {
            for (int j = 0; j < S_WIDTH; j++) {
                arr[i][j] = 0;
            }
        }
    }
}
