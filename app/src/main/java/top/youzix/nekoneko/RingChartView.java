package top.youzix.nekoneko;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 简洁环形图 View：显示 token 分布（输入/输出/缓存）。
 */
public class RingChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int promptTokens;
    private int completionTokens;
    private int cachedTokens;

    private final int colorPrompt;
    private final int colorCompletion;
    private final int colorCached;
    private final int colorBackground;

    public RingChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        colorPrompt = context.getResources().getColor(R.color.colorPrimary);
        colorCompletion = context.getResources().getColor(R.color.colorTertiary);
        colorCached = context.getResources().getColor(R.color.colorSecondaryContainer);
        colorBackground = context.getResources().getColor(R.color.colorSurfaceContainerHigh);
    }

    public void setData(int prompt, int completion, int cached) {
        this.promptTokens = prompt;
        this.completionTokens = completion;
        this.cachedTokens = cached;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float size = Math.min(w, h);
        float stroke = size * 0.12f;
        float padding = stroke / 2;

        rect.set(padding, padding, size - padding, size - padding);

        int total = promptTokens + completionTokens;
        if (total == 0) {
            // 空状态：画灰色底环
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(colorBackground);
            canvas.drawArc(rect, 0, 360, false, paint);
            return;
        }

        float sweepPrompt = 360f * promptTokens / total;
        float sweepCached = 360f * cachedTokens / total;
        float sweepCompletion = 360f * completionTokens / total;

        // 底环
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(colorBackground);
        canvas.drawArc(rect, 0, 360, false, paint);

        // Prompt tokens
        paint.setColor(colorPrompt);
        canvas.drawArc(rect, -90, sweepPrompt, false, paint);

        // Cached tokens (叠加在 prompt 上)
        if (cachedTokens > 0) {
            paint.setColor(colorCached);
            canvas.drawArc(rect, -90, sweepCached, false, paint);
        }

        // Completion tokens
        paint.setColor(colorCompletion);
        canvas.drawArc(rect, -90 + sweepPrompt, sweepCompletion, false, paint);
    }
}
