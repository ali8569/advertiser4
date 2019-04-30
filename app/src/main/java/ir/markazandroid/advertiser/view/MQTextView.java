package ir.markazandroid.advertiser.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Coded by Ali on 3/13/2018.
 */

public class MQTextView extends AppCompatTextView {



    public interface MarqueeListener{
        void onMarqueeFinish();
    }

    private Field mStatus;
    private Object mMarquee;
    private MarqueeListener listener;


    public MarqueeListener getListener() {
        return listener;
    }

    public void setListener(MarqueeListener listener) {
        this.listener = listener;
    }

    private void init(){
        try {
            Field marq = TextView.class.getDeclaredField("mMarquee");
            marq.setAccessible(true);
            mMarquee = marq.get(this);
            if (mMarquee !=null) {
                mStatus = mMarquee
                        .getClass()
                        .getDeclaredField("mStatus");
                mStatus.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMarquee ==null){
            init();
        }
        if (listener != null &&  !getMarqueeStatus()) {
            listener.onMarqueeFinish();
        }
    }

    public boolean getMarqueeStatus(){
        if (mMarquee != null) {
            try {
                return mStatus.getByte(mMarquee) == 0x2;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }
        else return false;
    }

    public MQTextView(Context context) {
        super(context);
    }

    public MQTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MQTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
