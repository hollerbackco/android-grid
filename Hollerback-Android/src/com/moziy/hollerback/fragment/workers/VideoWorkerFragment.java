package com.moziy.hollerback.fragment.workers;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;
import com.moziy.hollerback.model.VideoModel;

public class VideoWorkerFragment extends SherlockFragment {

    private static final String TAG = VideoWorkerFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    private void getVideos(long convoId) {

        new AsyncTask<Long, Void, List<VideoModel>>() {

            @Override
            protected List<VideoModel> doInBackground(Long... params) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected void onPostExecute(List<VideoModel> result) {
                Log.d(TAG, "loaded video");
            }

        };

    }
}
