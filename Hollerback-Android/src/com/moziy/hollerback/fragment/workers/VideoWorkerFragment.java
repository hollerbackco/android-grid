package com.moziy.hollerback.fragment.workers;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.moziy.hollerback.model.VideoModel;

// a worker fragment that retrieves unseen videos from the database
public class VideoWorkerFragment extends SherlockFragment {

    private static final String TAG = VideoWorkerFragment.class.getSimpleName();
    public static final String CONVO_ID_BUNDLE_ARG_KEY = "convo_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        long convoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);
        getVideos(convoId);

    }

    private void getVideos(long convoId) {

        new AsyncTask<Long, Void, List<VideoModel>>() {

            @Override
            protected List<VideoModel> doInBackground(Long... params) {

                return null;
            }

            @Override
            protected void onPostExecute(List<VideoModel> result) {

            }

        }.execute(convoId);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
