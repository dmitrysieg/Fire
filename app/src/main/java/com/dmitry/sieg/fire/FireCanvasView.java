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

import java.util.concurrent.atomic.AtomicInteger;

public class FireCanvasView extends View {

    private static final int BASE_S_WIDTH = 32;
    private static final int BASE_S_HEIGHT = 32;

    private static final float random_lower = 0.5f;
    private static final float random_upper = 0.8f;

    private static int s_width = 32;
    private static int s_height = 32;
    private static float s_block_size;
    private static final int I_HEIGHT = 1; // height of noise initiator;

    private Bitmap[] bitmap = new Bitmap[2];
    private int[][][] fireIndex;
    private Matrix matrix;
    private Paint paint;
    private int b_index;

    private AtomicInteger temperature = new AtomicInteger(255);

    public FireCanvasView(Context context) {
        super(context);

        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        s_block_size = Math.min(
                ((float) dm.widthPixels) / ((float) BASE_S_WIDTH),
                ((float) dm.heightPixels) / ((float) BASE_S_HEIGHT)
        );

        // System.out.println(s_block_size);
        s_width = (int) Math.floor( ((float) dm.widthPixels) / s_block_size);
        s_height = (int) Math.floor( ((float) dm.heightPixels) / s_block_size);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bitmap[0] = Bitmap.createBitmap(s_width, s_height + I_HEIGHT, conf);
        bitmap[1] = Bitmap.createBitmap(s_width, s_height + I_HEIGHT, conf);
        b_index = 0;

        fireIndex = new int[2][s_height + I_HEIGHT][s_width];
        init2DArray(fireIndex[0]);
        init2DArray(fireIndex[1]);
        initiateNoise(fireIndex[b_index]);

        matrix = new Matrix();
        matrix.setRectToRect(new RectF(0.0f, 0.0f, (float) s_width, (float) s_height),
                new RectF(0.0f, 0.0f, (float) dm.widthPixels, (float) dm.heightPixels),
                Matrix.ScaleToFit.END);

        paint = new Paint();
        paint.setDither(false);
    }

    public void incTemperature(final int delta) {
        temperature.addAndGet(delta);
    }

    public void decTemperature(final int delta) {
        temperature.addAndGet(-delta);
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
            for (int x = 0; x < s_width; x++) {
                final double r = Math.random();
                if (r > random_lower && r < random_upper) {
                    final double normal = (r - random_lower) / (random_upper - random_lower);
                    newFireIndex[s_height + y][x] = (int)(temperature.get() * normal);
                } else {
                    newFireIndex[s_height + y][x] = 0;
                }
            }
        }
    }

    private void processFire(final Bitmap newBitmap,
                             final int[][] oldFireIndex,
                             final int[][] newFireIndex) {
        for (int y = s_height - 1; y >=0; y--) {
            for (int x = 0; x < s_width; x++) {

                int sum = 0;

                // summing by pattern:
                //
                //  S
                // +++
                //  +
                //
                // and dividing it by the value slightly > than 4.
                if (y < s_height + I_HEIGHT - 2) {
                    sum += oldFireIndex[y + 2][x];
                }
                if (y < s_height + I_HEIGHT - 1) {
                    if (x > 0) {
                        sum += oldFireIndex[y + 1][x - 1];
                    }
                    if (x < s_width - 1) {
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
     * index = [0, inf].
     */
    private int indexToColor(final int index) {
        final int saturatedIndex = index <= 255 ? index : 255;
        final float nindex = ((float) saturatedIndex) / 256.0f;
        return Color.HSVToColor(new float[]{180.0f / 3.0f * nindex, nindex < 0.5f ? 1.0f : 1.0f - (nindex - 0.5f) * 2.0f, Math.min(1.0f, nindex * 2.0f)});
    }

    private void init2DArray(final int arr[][]) {
        for (int i = 0; i < s_height; i++) {
            for (int j = 0; j < s_width; j++) {
                arr[i][j] = 0;
            }
        }
    }
}
