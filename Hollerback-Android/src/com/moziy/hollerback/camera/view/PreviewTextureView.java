package com.moziy.hollerback.camera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

public class PreviewTextureView extends TextureView implements Preview {
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
    public void setAspectRatio(double ratio) {
        mAspectRatio = ratio;
        Log.d(TAG, "new aspect ratio: " + ratio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);

        boolean isHeightLongSide = (previewHeight > previewWidth ? true : false);
        Log.d(TAG, "original width: " + previewWidth + " height: " + previewHeight);
        if (isHeightLongSide) {

            if (((double) previewHeight / (double) previewWidth) > mAspectRatio) {
                previewWidth = (int) ((double) previewHeight / mAspectRatio); // make the width wider
            } else { //
                previewHeight = (int) ((double) previewWidth * mAspectRatio);
            }

        } else if (((double) previewHeight / (double) previewWidth) < mAspectRatio) { // width is the longer side

            if ((double) previewHeight / (double) previewWidth > mAspectRatio) {
                previewHeight = (int) (previewHeight * mAspectRatio);
            } else {
                previewWidth = (int) (previewWidth * mAspectRatio);
            }

        }
        Log.d(TAG, "new width: " + previewWidth + " new height: " + previewHeight);
        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
    }
}
