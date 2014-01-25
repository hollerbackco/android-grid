package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.VideoView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.AppEnvironment;

public class VideoPlaybackFragment extends BaseFragment {
    private static final String TAG = VideoPlaybackFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    public static final String PLAYBACK_INDEX_BUNDLE_ARG_KEY = "PLAYBACK_INDEX";
    private VideoView mVideoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversation_layout, container, false);
        mVideoView = (VideoView) v.findViewById(R.id.vv_preview);
        LayoutParams params = new LayoutParams(AppEnvironment.OPTIMAL_VIDEO_SIZE.x, AppEnvironment.OPTIMAL_VIDEO_SIZE.y, Gravity.CENTER);
        mVideoView.setLayoutParams(params);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        ((ConvoHistoryTwo) getTargetFragment()).getVideoDelegate().onVideoViewReady(getView());
    }

    @Override
    protected String getScreenName() {
        // TODO Auto-generated method stub
        return AnalyticsUtil.ScreenNames.VIDEO_VIEW;
    }

    public interface OnVideoViewReadyListener {
        public void onVideoViewReady(View layout);
    }

}
