package ir.markazandroid.advertiser.view.gold;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * Coded by Ali on 11/4/2018.
 */
public class CameraOverlayView extends View {

    private static final String DIMENS_TAG = "DIMENS";

    private Size size;
    private float scaleX;
    private float scaleY;
    private FirebaseVisionFace face;
    private Paint paint;
    private Bitmap bitmap;
    private Rect rect;
    private Bitmap gold;
    private Rect bodyRect;
    private float decidedX, decidedY, decidedScaleFactor;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mPosX = 0;
    private float mPosY = 0;
    private int rotation = 0;
    private GoldShowAdapter adapter;
    private Drawable drawable;
    private int position;
    private ProgressBar progressBar;
    private volatile boolean shouldDecideGoldPosition;
    private float goldWidth;


    public CameraOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(dpToPx(context, 2));
        paint.setStyle(Paint.Style.STROKE);
        //gold=BitmapFactory.decodeResource(context.getResources(), R.drawable.no);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        //canvas.rotate(rotation);
        Log.e("drawn sscale", mScaleFactor + "");
        canvas.scale(mScaleFactor, mScaleFactor);

        /*if (bitmap!=null)
            canvas.drawBitmap(bitmap,null,new Rect(0,0,canvas.getWidth(),canvas.getHeight()),paint);
        else if (face != null){
            calculateScales();
            scaleRect(face.getBoundingBox());
            flipRect(face.getBoundingBox(),getWidth());
            makeBodyRect();

            //canvas.drawRect(face.getBoundingBox(),paint);
            *//*List<FirebaseVisionPoint> points=face.getContour(FirebaseVisionFaceContour.ALL_POINTS).getPoints();
            Path path = new Path();
            path.moveTo(points.get(0).getX()*scaleX,points.get(0).getY()*scaleY);
            for (int i=1;i<points.size();i++){
                FirebaseVisionPoint p=points.get(i);
                path.lineTo(getWidth()-p.getX()*scaleX,p.getY()*scaleY);
            }
            canvas.drawPath(path,paint);*//*
        }*/
        //Log.e("draw shode",mPosX+"-"+mPosY);
        if (gold != null)
            canvas.drawBitmap(gold, null, new RectF(mPosX / mScaleFactor, mPosY / mScaleFactor, gold.getWidth() + mPosX / mScaleFactor, gold.getHeight() + mPosY / mScaleFactor), paint);

        canvas.restore();
    }

    private void decideGoldPositionAndSize() {
        if (gold == null) return;

        if (face == null) {
            if (mPosX == 0 && mPosY == 0) {
                mScaleFactor = Math.min((float) getWidth() / 2f / (float) gold.getWidth(), (float) getHeight() / 2f / (float) gold.getHeight());
                mPosX = getWidth() / 4/*/mScaleFactor*/;
                mPosY = getHeight() / 4/*/mScaleFactor*/;
            }
        } else {
            calculateScales();
            scaleRect(face.getBoundingBox());
            makeBodyRect();

            mScaleFactor = (float) bodyRect.width() / (float) gold.getWidth();

            mPosX = bodyRect.left/*/mScaleFactor*/;
            mPosY = bodyRect.top/*/mScaleFactor*/;

            Log.e(DIMENS_TAG, String.format("final: mScaleFactor=%f - mPosX=%f - mPosY=%f", mScaleFactor, mPosX, mPosY));

        }

        goldWidth = mScaleFactor * (float) gold.getWidth();
    }

    private void makeBodyRect() {
        //width for women - 325 is body_abstract_img_shoulder_length/2.2
        float ratio = ((float) face.getBoundingBox().width()) * 1.1F / gold.getWidth();
        Log.e(DIMENS_TAG, String.format("ratio: %f", ratio));

        //based on neck size
        /*List<FirebaseVisionPoint> points=face.getContour(FirebaseVisionFaceContour.FACE).getPoints();
        if (points.isEmpty()){
            makeBodyRect0();
            Log.e(" no contours","kk");
            return;
        }
        FirebaseVisionPoint p13= points.get(13);
        FirebaseVisionPoint p24= points.get(24);
        float ratio = (p13.getX()-p24.getX())*scaleX / 258F;*/

        bodyRect = new Rect();

        bodyRect.top = face.getBoundingBox().top + face.getBoundingBox().height()/*-Math.round(64*ratio)*/;
        bodyRect.left = (face.getBoundingBox().left + face.getBoundingBox().width() / 2) - Math.round(gold.getWidth() * ratio) / 2;
        bodyRect.right = Math.round(gold.getWidth() * ratio) + bodyRect.left;
        bodyRect.bottom = bodyRect.top + Math.round(gold.getHeight() * ratio);

        Log.e(DIMENS_TAG, String.format("bodyRect: %s", bodyRect.toShortString()));

    }

    private void makeBodyRect0() {
        bodyRect.top = bodyRect.left = bodyRect.right = bodyRect.bottom = 0;
    }

    private void flipRect(Rect boundingBox, int width) {
        int l1 = boundingBox.left;
        boundingBox.left = width - boundingBox.right;
        boundingBox.right = width - l1;
    }

    private Target target;

    boolean changeGold(boolean refresh) {
        if (gold != null) gold.recycle();
        gold = null;
        invalidate();

        if (progressBar != null)
            progressBar.setVisibility(VISIBLE);

        if (adapter == null || position > adapter.getCount() - 1 || position < 0) {
            if (progressBar != null)
                progressBar.setVisibility(INVISIBLE);
            return false;
        }

        Log.e("loading", "position=" + position);
        adapter.getItemBitmap(adapter.getGoldTarget(position, target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.e("called", "called");
                if (gold != null) gold.recycle();
                gold = bitmap.copy(bitmap.getConfig(), true);

                if (refresh || shouldDecideGoldPosition) {
                    setShouldDecideGoldPosition(false);
                    decideGoldPositionAndSize();
                } else {
                    if (goldWidth != 0) {
                        // float r = mScaleFactor;
                        mScaleFactor = goldWidth / gold.getWidth();
                        //r= mScaleFactor-r;
                        //mPosX+=mPosX*r;
                        //mPosY+=mPosY*r;
                    }
                }
                if (progressBar != null)
                    progressBar.setVisibility(INVISIBLE);
                postInvalidate();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                //e.printStackTrace();
                changeGold(refresh);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (gold != null) gold.recycle();
                gold = null;
            }
        }));
        return true;
    }

  /*  public class Im extends AppCompatImageView {

        public Im(Context context) {
            super(context);
        }

        @Override
        public void setImageDrawable(@Nullable Drawable drawable) {
            //super.setImageDrawable(drawable);
            if (drawable!=null){
                try {
                    Field field=Drawable.class.getDeclaredField("mBitmap");
                    field.setAccessible(true);
                    CameraOverlayView.this.bitmap= (Bitmap) field.get(drawable);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void setImageBitmap(Bitmap bm) {
            super.setImageBitmap(bm);
        }
    }*/


/*
    public void setNewFace(FirebaseVisionFace face, com.otaliastudios.cameraview.Size frameSize){

        this.face=face;
        this.size=frameSize;
        //invalidate();
    }*/

    public void setNewFace(FirebaseVisionFace face, Size size) {

        this.face = face;
        this.size = size;

        if (gold == null)
            setShouldDecideGoldPosition(true);
        else
            decideGoldPositionAndSize();

        invalidate();
    }

    private void refreshCurrentImage() {

    }

    public void showBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    private void calculateScales() {
        scaleX = ((float) getWidth()) / ((float) size.getWidth());
        scaleY = ((float) getHeight()) / ((float) size.getHeight());
        Log.e(DIMENS_TAG, String.format("cs: swidth=%d - sheight=%d _ width=%d - height=%d", size.getWidth(), size.getHeight(), getWidth(), getHeight()));
    }

    private void scaleRect(Rect boundingBox) {
        Log.e(DIMENS_TAG, "before scaleRect" + boundingBox.toShortString());
        boundingBox.top = Math.round(boundingBox.top * scaleY);
        boundingBox.bottom = Math.round(boundingBox.bottom * scaleY);
        boundingBox.left = Math.round(boundingBox.left * scaleX);
        boundingBox.right = Math.round(boundingBox.right * scaleX);
        Log.e(DIMENS_TAG, "after scaleRect" + boundingBox.toShortString());
    }


    public static float dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return dp * ((float) displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public void refresh() {
        position = 0;
        changeGold(true);
    }

    public void goNext() {
        position++;
        if (!changeGold(false)) position--;
    }

    public void goToPosition(int toPos) {
        int lastPos = position;
        position = toPos;
        if (!changeGold(false)) position = lastPos;
    }

    public void goBack() {
        position--;
        if (!changeGold(false)) position++;
    }

    public boolean getNextAvailable() {
        if (adapter == null) return false;
        return position < adapter.getCount() - 1;
    }

    public boolean getBackAvailable() {
        if (adapter == null) return false;
        return position != 0;
    }

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Remember where we started (for dragging)
                mLastTouchX = x;
                mLastTouchY = y;
                // Save the ID of this pointer (for dragging)
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex =
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Calculate the distance moved
                //TODO
                final float dx = (x - mLastTouchX)/*/mScaleFactor*/;
                final float dy = (y - mLastTouchY)/*/mScaleFactor*/;

                mPosX += dx;
                mPosY += dy;

                /*if (ev.getPointerCount()==2)
                    updateRotation(dx,dy);*/

                invalidate();

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
                    mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private void updateRotation(float x, float y) {

        float centerX = mPosX + gold.getWidth() / 2;
        float centerY = mPosY + gold.getHeight() / 2;
        double r = Math.atan2(x, y);
        rotation = (int) Math.toDegrees(r);
    }

    public GoldShowAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(GoldShowAdapter adapter) {
        this.adapter = adapter;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }


    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float r1 = mScaleFactor;
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            //mScaleFactor = Math.max(0.01f, Math.min(mScaleFactor, 5.0f));
            //mPosX/=mScaleFactor;
            //mPosY/=mScaleFactor;

            if (gold != null && !gold.isRecycled())
                goldWidth = mScaleFactor * (float) gold.getWidth();


            //mPosX-=((mScaleFactor)*gold.getWidth())/2;
            //mPosY-=((mScaleFactor)*gold.getHeight())/2;

            //Log.e("Mohasebe shode",(((mScaleFactor-r1)*gold.getWidth())/2)+"-"+(((mScaleFactor-r1)*gold.getHeight())/2));

            invalidate();
            return true;
        }
    }


    public static class Size {
        private int width;
        private int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public boolean isShouldDecideGoldPosition() {
        return shouldDecideGoldPosition;
    }

    public synchronized void setShouldDecideGoldPosition(boolean shouldDecideGoldPosition) {
        this.shouldDecideGoldPosition = shouldDecideGoldPosition;
    }

    public int getPosition() {
        return position;
    }
}
