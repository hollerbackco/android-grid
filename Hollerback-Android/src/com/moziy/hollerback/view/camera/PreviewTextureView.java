package com.moziy.hollerback.view.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

public class PreviewTextureView extends TextureView {
    private static final String TAG = PreviewTextureView.class.getSimpleName();
    private static final double DEFAULT_ASPECT_RATIO = (4.0 / 3.0);
    private double mAspectRatio = DEFAULT_ASPECT_RATIO;

    public PreviewTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public PreviewTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public PreviewTextureView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "preview width: " + previewWidth + " previewHeight: " + previewHeight);

        boolean isHeightLongSide = (previewHeight > previewWidth ? true : false);

        if (isHeightLongSide) {
            if (((double) previewHeight / (double) previewWidth) > mAspectRatio) {
                // make the width wider
                previewWidth = (int) (previewWidth * mAspectRatio);
            }

        }

        Log.d(TAG, "new width: " + previewWidth + " new height: " + previewHeight);

        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
    }
}
