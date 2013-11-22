package com.moziy.hollerback.view.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PreviewSurfaceView extends SurfaceView {
    private static final String TAG = PreviewSurfaceView.class.getSimpleName();
    private static final double DEFAULT_ASPECT_RATIO = (4.0 / 3.0);
    private double mAspectRatio = DEFAULT_ASPECT_RATIO;

    public PreviewSurfaceView(Context context) {
        super(context);
        if (!isInEditMode())
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            setZOrderMediaOverlay(true);
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

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
        setMeasuredDimension(previewWidth, previewHeight);
        Log.d(TAG, "new width: " + previewWidth + " new height: " + previewHeight);

    }
}
