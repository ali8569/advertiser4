package ir.markazandroid.advertiser.view.gold;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

/**
 * Coded by Ali on 1/6/2019.
 */
public class ScannerAnimationView extends ViewGroup {

    private float endY;
    //private int rectWidth, rectHeight;
    private RectF rectF;
    private Rect rect;
    private static final int frames = 6;
    private boolean revAnimation;
    private Paint eraser, line;

    public ScannerAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        line = new Paint();
        line.setColor(Color.parseColor("#7323DC"));
        line.setStrokeWidth(6f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        rectF = new RectF(0, 0, w, h);
        rect = new Rect(0, 0, w, h);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw transparent rect

        //canvas.drawRoundRect(rectF, (float) cornerRadius, (float) cornerRadius, eraser);

        // draw horizontal line


        // draw the line to product animation
        if (endY >= rect.height() + frames) {
            revAnimation = true;
        } else if (endY == frames) {
            revAnimation = false;
        }

        // check if the line has reached to bottom
        if (revAnimation) {
            endY -= frames;
        } else {
            endY += frames;
        }
        canvas.drawLine(0, endY, rect.width(), endY, line);
        invalidate();
    }
}
