package com.moziy.hollerback.view.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class SquarePreview extends SurfaceView {
    public static enum FixedAlong {
        width, height
    }

    private FixedAlong fixedAlong = FixedAlong.width;

    public SquarePreview(Context context) {
        super(context);
    }

    public SquarePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquarePreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    int squareDimen = 1;

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int square = (fixedAlong == FixedAlong.width) ? getMeasuredWidth() : getMeasuredHeight();

        if (square > squareDimen) {
            squareDimen = square;
        }

        setMeasuredDimension(squareDimen, squareDimen);
    }

}
