package top.youzix.nekoneko;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 轻量级 morphing 加载动画 Drawable：花形 <-> 三角形 <-> 十字形 <-> 菱形 循环变形。
 * 零额外依赖，可直接用于 ImageView。
 */
public class MorphingLoaderDrawable extends Drawable implements Animatable, ValueAnimator.AnimatorUpdateListener {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private ValueAnimator animator;

    private static final float[][] SHAPE_FLOWER = {
            {1.0f, 0},     {0.5f, 45},   {1.0f, 90},    {0.5f, 135},
            {1.0f, 180},   {0.5f, 225},  {1.0f, 270},   {0.5f, 315}
    };
    private static final float[][] SHAPE_TRIANGLE = {
            {1.0f, 270},   {0.5f, 270},  {0.85f, 330},  {0.0f, 0},
            {0.85f, 30},   {0.5f, 90},   {0.85f, 150},  {0.0f, 180}
    };
    private static final float[][] SHAPE_CROSS = {
            {0.6f, 0},     {0.3f, 45},   {0.6f, 90},    {0.3f, 135},
            {0.6f, 180},   {0.3f, 225},  {0.6f, 270},   {0.3f, 315}
    };
    private static final float[][] SHAPE_DIAMOND = {
            {1.0f, 0},     {0.5f, 45},   {0.5f, 90},    {0.5f, 135},
            {1.0f, 180},   {0.5f, 225},  {0.5f, 270},   {0.5f, 315}
    };
    private static final float[][][] SHAPES = {SHAPE_FLOWER, SHAPE_TRIANGLE, SHAPE_CROSS, SHAPE_DIAMOND};

    private final float[][] current = new float[8][2];
    private int color = 0xFF4C662B;

    public MorphingLoaderDrawable() {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        for (int i = 0; i < 8; i++) {
            current[i][0] = SHAPES[0][i][0];
            current[i][1] = SHAPES[0][i][1];
        }
    }

    public void setLoaderColor(int color) {
        this.color = color;
        paint.setColor(color);
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float cx = getBounds().width() / 2f;
        float cy = getBounds().height() / 2f;
        float radius = Math.min(cx, cy) * 0.85f;

        path.reset();
        for (int i = 0; i < 8; i++) {
            float r = current[i][0] * radius;
            float angleRad = (float) Math.toRadians(current[i][1] - 90);
            float x = cx + r * (float) Math.cos(angleRad);
            float y = cy + r * (float) Math.sin(angleRad);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(@Nullable ColorFilter cf) { paint.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    @Override
    public void start() {
        if (animator != null && animator.isRunning()) return;
        animator = ValueAnimator.ofFloat(0, SHAPES.length);
        animator.setDuration(2400);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(this);
        animator.start();
    }

    @Override
    public void stop() {
        if (animator != null) { animator.cancel(); animator = null; }
    }

    @Override
    public boolean isRunning() {
        return animator != null && animator.isRunning();
    }

    @Override
    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        int idx = (int) value % SHAPES.length;
        int next = (idx + 1) % SHAPES.length;
        float fraction = value - (int) value;
        float t = fraction < 0.5f
                ? 2f * fraction * fraction
                : 1f - (float) Math.pow(-2f * fraction + 2f, 2) / 2f;

        for (int i = 0; i < 8; i++) {
            current[i][0] = SHAPES[idx][i][0] + (SHAPES[next][i][0] - SHAPES[idx][i][0]) * t;
            current[i][1] = SHAPES[idx][i][1] + (SHAPES[next][i][1] - SHAPES[idx][i][1]) * t;
        }
        invalidateSelf();
    }
}
