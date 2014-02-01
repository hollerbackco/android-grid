package com.moziy.hollerback.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.moziy.hollerback.R;

public class SquareImageView extends ImageView {
    private static final String TAG = SquareImageView.class.getSimpleName();

    private boolean mDrawTint;
    private int mTintColor;

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTintColor = getResources().getColor(R.color.gray_disabled);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareImageView(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        height = width;

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        width = resolveSizeAndState(width, widthMeasureSpec, 1);
        height = resolveSizeAndState(height, MeasureSpec.EXACTLY, 0);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawTint) {
            canvas.drawColor(mTintColor);
        }

    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (isPressed()) {
            mDrawTint = true;
            invalidate();
        } else {
            mDrawTint = false;
            invalidate();
        }
    }

}
