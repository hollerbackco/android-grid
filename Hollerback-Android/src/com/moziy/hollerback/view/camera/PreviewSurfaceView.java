package com.moziy.hollerback.view.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class PreviewSurfaceView extends SurfaceView {
    private static final String TAG = PreviewSurfaceView.class.getSimpleName();
    private static final double DEFAULT_ASPECT_RATIO = (4.0 / 3.0);
    private double mAspectRatio = DEFAULT_ASPECT_RATIO;

    public PreviewSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public PreviewSurfaceView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public void setAspectRatio(double ratio) {
        mAspectRatio = ratio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);

        boolean isHeightLongSide = (previewHeight > previewWidth ? true : false);

        if (isHeightLongSide) {

            if (((double) previewHeight / (double) previewWidth) > mAspectRatio) {
                previewWidth = (int) (previewWidth * mAspectRatio); // make the width wider
            } else { //
                previewHeight = (int) (previewHeight * mAspectRatio);
            }

        } else { // width is the longer side

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
