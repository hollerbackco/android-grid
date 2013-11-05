package com.moziy.hollerback.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class VideoPreview extends VideoView {
    public static enum FixedAlong {
        width, height
    }

    private FixedAlong fixedAlong = FixedAlong.width;

    public VideoPreview(Context context) {
        super(context);
    }

    public VideoPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPreview(Context context, AttributeSet attrs, int defStyle) {
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
