package com.moziy.hollerback.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.moziy.hollerback.R;

public class RoundImageView extends ImageView {
    private static final String TAG = RoundNetworkImageView.class.getSimpleName();
    private Bitmap mMask;
    private int mRadius;
    private int mCenterX;
    private int mCenterY;
    private Paint mPaint;
    private int mHaloBorderColor = -1;
    private Paint mBorderPaint;
    private RectF mHaloBounds = new RectF();
    private int mPadding;

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);

        mRadius = a.getDimensionPixelSize(R.styleable.RoundImageView_radius, 0);
        mCenterX = a.getDimensionPixelSize(R.styleable.RoundImageView_xPosCenter, 0);
        mCenterY = a.getDimensionPixelSize(R.styleable.RoundImageView_yPosCenter, 0);
        a.recycle();

        mPadding = getPaddingTop();

        if (!isInEditMode())
            mMask = createCircleBitmap(mRadius * 2, mRadius * 2, mPadding);

        initPaint();
    }

    public RoundImageView(Context context) {
        super(context);
    }

    // create a bitmap with a circle, used for the "dst" image
    static Bitmap createCircleBitmap(int w, int h, int padding) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(0xFF770055);
        c.drawOval(new RectF(padding, padding, w - padding * 2, h - padding * 2), p);
        return bm;
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setFilterBitmap(false); // todo play with this flag
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    public void setHaloBorderColor(int color) {

        // if (color == mHaloBorderColor)
        // return;
        mHaloBorderColor = color;
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(color);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth(getResources().getDimension(R.dimen.dim_4dp)); // set to 5dp width

        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), 255, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);

        super.onDraw(canvas);

        if (!isInEditMode()) {
            canvas.drawBitmap(mMask, 0, 0, mPaint);
        }

        if (mHaloBorderColor != -1 && mBorderPaint != null) {
            int pix = getResources().getDimensionPixelSize(R.dimen.dim_2dp);
            mHaloBounds.set(pix, pix, getWidth() - pix, getHeight() - pix);
            canvas.drawOval(mHaloBounds, mBorderPaint);
        }

    }

    @Override
    public String toString() {
        return "RoundImageView [ , mRadius=" + mRadius + "]";
    }
}
