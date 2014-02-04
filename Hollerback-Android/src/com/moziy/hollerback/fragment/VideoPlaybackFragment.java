package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.util.AnalyticsUtil;

public class VideoPlaybackFragment extends BaseFragment {
    private static final String TAG = VideoPlaybackFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    public static final String PLAYBACK_INDEX_BUNDLE_ARG_KEY = "PLAYBACK_INDEX";
    private VideoView mVideoView;

    private GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGestureDetector = new GestureDetector(getActivity(), mGestureListener);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getSherlockActivity().getSupportActionBar().hide();
    }

    private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {

        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isResumed()) {
                getFragmentManager().popBackStack();
                return true;
            }
            return false;
        }

        public boolean onDown(MotionEvent e) {
            return true;
        };

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversation_layout, container, false);
        mVideoView = (VideoView) v.findViewById(R.id.vv_preview);

        mVideoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return mGestureDetector.onTouchEvent(event);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((VideoViewStatusListener) getTargetFragment()).onVideoViewReady(getView());

    }

    @Override
    public void onPause() {

        if (isRemoving() && getTargetFragment() != null) {
            mActivity.getSupportActionBar().show();
            ((HollerbackMainActivity) mActivity).getCustomActionBarTitle().setText(((BaseFragment) getTargetFragment()).getActionBarTitle());
            ((HollerbackMainActivity) mActivity).setCustomActionBarSubTitle(((BaseFragment) getTargetFragment()).getActionBarSubTitle());
            ((VideoViewStatusListener) getTargetFragment()).onVideoViewFinish();
        }

        super.onPause();
    }

    @Override
    protected String getScreenName() {
        // TODO Auto-generated method stub
        return AnalyticsUtil.ScreenNames.VIDEO_VIEW;
    }

    public interface VideoViewStatusListener {
        public void onVideoViewReady(View layout);

        public void onVideoViewFinish();
    }

}
