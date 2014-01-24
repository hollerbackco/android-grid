package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.activeandroid.query.Update;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.RecordVideoFragment.RecordingInfo;
import com.moziy.hollerback.fragment.delegates.ConvoHistoryDelegate;
import com.moziy.hollerback.fragment.delegates.ConvoLoaderDelegate;
import com.moziy.hollerback.fragment.delegates.VideoPlayerDelegate;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.VideoDownloadTask;
import com.moziy.hollerback.util.date.TimeUtil;
import com.squareup.picasso.Picasso;

public class ConvoHistoryTwo extends SherlockFragment implements TaskClient, RecordingInfo {

    private static final String TAG = ConversationFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    public static final String CONVO_ID_BUNDLE_ARG_KEY = "CONVO_ID";
    public static final String CONVO_ID_INSTANCE_STATE = "CONVO_ID_INSTANCE_STATE";
    public static final String TASK_QUEUE_INSTANCE_STATE = "TASK_QUEUE_INSTANCE_STATE";
    public static final String RECORDING_INFO_INSTANCE_STATE = "RECORDING_INFO_INSTANCE_STATE";

    public static ConversationFragment newInstance(long conversationId) {
        ConversationFragment c = new ConversationFragment();
        Bundle args = new Bundle();
        args.putLong(CONVO_ID_BUNDLE_ARG_KEY, conversationId);
        c.setArguments(args);
        return c;
    }

    public static final int HISTORY_LIMIT = 5;

    private long mConvoId;
    private LinkedList<Task> mTaskQueue; // queue of tasks such as fetching the model and fetching the videos
    private Bundle mRecordingInfo;
    private boolean mHasNew;

    private ConvoLoaderDelegate mConvoDelegate;
    private ConvoHistoryDelegate mHistoryDelegate;
    private VideoPlayerDelegate mVideoPlayerDelegate;

    @Override
    public void onAttach(Activity activity) {

        if (mConvoDelegate == null) {
            mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);
            mConvoDelegate = new ConvoLoaderDelegate(mConvoId);
            mVideoPlayerDelegate = new VideoPlayerDelegate(mConvoId);
            mConvoDelegate.setOnModelLoadedListener(mVideoPlayerDelegate);
            mHistoryDelegate = new ConvoHistoryDelegate(mConvoId, mConvoDelegate);
            mHistoryDelegate.setOnHistoryVideoDownloadListener(mVideoPlayerDelegate);
        }

        mVideoPlayerDelegate.onPreSuperAttach(this);
        mConvoDelegate.onPreSuperAttach(this);
        mHistoryDelegate.onPreSuperAttach(this);

        super.onAttach(activity);

        mVideoPlayerDelegate.onPostSuperAttach(this);
        mConvoDelegate.onPostSuperAttach(this);
        mHistoryDelegate.onPostSuperAttach(this);

    }

    @Override
    public void onDetach() {
        mVideoPlayerDelegate.onPreSuperDetach(this);
        mConvoDelegate.onPreSuperDetach(this);
        mHistoryDelegate.onPreSuperDetach(this);

        super.onDetach();

        mVideoPlayerDelegate.onPostSuperDetach(this);
        mConvoDelegate.onPostSuperDetach(this);
        mHistoryDelegate.onPostSuperDetach(this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);

        // mPlaybackIndex = -1;

        if (savedInstanceState != null) { // TODO: we probably don't need to worry about this because setRetainInstance is set to true
            Log.d(TAG, "restoring instance");

            if (savedInstanceState.containsKey(TASK_QUEUE_INSTANCE_STATE)) {
                mTaskQueue = new LinkedList<Task>((ArrayList<Task>) savedInstanceState.getSerializable(TASK_QUEUE_INSTANCE_STATE));
            }

            if (savedInstanceState.containsKey(RECORDING_INFO_INSTANCE_STATE)) {
                mRecordingInfo = savedInstanceState.getBundle(RECORDING_INFO_INSTANCE_STATE);
            }

        }

        if (mTaskQueue == null) {
            mTaskQueue = new LinkedList<Task>();
        }

        mVideoPlayerDelegate.init(savedInstanceState);
        mConvoDelegate.init(savedInstanceState);
        mHistoryDelegate.init(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Add a task to the queue with an executer
     * @param t
     * @param workerName
     * @return
     */
    public boolean addTaskToQueue(Task t, String workerName) {
        Fragment f = getFragmentManager().findFragmentByTag(workerName);
        if (f == null) {
            mTaskQueue.add(t);
            FragmentTaskWorker worker = FragmentTaskWorker.newInstance(true);
            worker.setTargetFragment(this, 0);
            getFragmentManager().beginTransaction().add(worker, workerName).commit();

            return true;
        }
        return false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversation_layout, container, false);
        mVideoPlayerDelegate.onViewCreated(v);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mVideoPlayerDelegate.onPreSuperActivityCreated(savedInstanceState);
        mConvoDelegate.onPreSuperActivityCreated(savedInstanceState);

        super.onActivityCreated(savedInstanceState);

        mVideoPlayerDelegate.onPostSuperActivityCreated(savedInstanceState);
        mConvoDelegate.onPostSuperActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {

        mVideoPlayerDelegate.onPreSuperResume(this);
        mConvoDelegate.onPreSuperResume(this);

        super.onResume();

        if (mRecordingInfo != null) { // so we got our result from the recording fragment, time to go back
            getFragmentManager().popBackStack();
            return;
        }

        mVideoPlayerDelegate.onPostSuperResume(this);
        mConvoDelegate.onPostSuperResume(this);
    }

    @Override
    public void onPause() {
        if (isRemoving()) {
            Log.d(TAG, "isRemoving = true");
        }

        mVideoPlayerDelegate.onPreSuperPause(this);
        mConvoDelegate.onPreSuperPause(this);
        mHistoryDelegate.onPreSuperPause(this);

        super.onPause();

        mVideoPlayerDelegate.onPostSuperPause(this);
        mConvoDelegate.onPostSuperPause(this);
        mHistoryDelegate.onPostSuperPause(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(CONVO_ID_INSTANCE_STATE, mConvoId);

        if (mTaskQueue != null) {
            outState.putSerializable(TASK_QUEUE_INSTANCE_STATE, new ArrayList<Task>(mTaskQueue));
        }

        if (mRecordingInfo != null) {
            outState.putBundle(RECORDING_INFO_INSTANCE_STATE, mRecordingInfo);
        }

        mVideoPlayerDelegate.onSaveInstanceState(outState);
        mConvoDelegate.onSaveInstanceState(outState);
        mHistoryDelegate.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onTaskComplete(Task t) {
        mConvoDelegate.onTaskComplete(t);
        mHistoryDelegate.onTaskComplete(t);

    }

    @Override
    public void onTaskError(Task t) {

        Log.d(TAG, "there was a problem with a task");
        mConvoDelegate.onTaskError(t);
        // retry the task
        mHistoryDelegate.onTaskError(t);

    }

    @Override
    public Task getTask() {
        return mTaskQueue.poll();
    }

    public LinkedList<Task> getTaskQueue() {
        return mTaskQueue;
    }

    // public void addHistoryVideo(VideoModel video) {
    // mPlayBackQueue.add(0, video);
    // ++mPlaybackIndex; // update the playback index
    // }

    // public void startHistoryPlayback() {
    // mPlaybackIndex = mPlayBackQueue.size() - 1;
    // mProgress.setVisibility(View.INVISIBLE);
    // playVideo(mPlayBackQueue.get(mPlaybackIndex)); // get the last element
    // }

    public boolean hasNewVideos() {
        return mHasNew;
    }

    @Override
    public void onRecordingFinished(Bundle info) {
        mRecordingInfo = info;

        if (!info.getBoolean(RecordingInfo.STATUS_BUNDLE_ARG_KEY, true)) {
            return;
        }

        // if the recording was successfull, then update the conversation, and set the last message time
        Task t = new ActiveAndroidUpdateTask(new Update(ConversationModel.class) //
                .set(ActiveRecordFields.C_CONV_LAST_MESSAGE_AT + "='" + TimeUtil.FORMAT_ISO8601(new Date()) + "'") //
                .where(ActiveRecordFields.C_CONV_ID + "=?", mConvoId));
        t.setTaskListener(new Task.Listener() {

            @Override
            public void onTaskError(Task t) {
                Log.w(TAG, "error updating database in case of recording finished");
            }

            @Override
            public void onTaskComplete(Task t) {
                Log.d(TAG, "updated the conversation");
                IABroadcastManager.sendLocalBroadcast(new Intent(IABIntent.CONVERSATION_UPDATED));
            }
        });

        Context c = HollerbackApplication.getInstance();
        Toast.makeText(c, c.getString(R.string.message_sent_simple), Toast.LENGTH_LONG).show();

        // UPDATE: this is being done in RecordVideoFragment.updateConversationTime
        // new TaskExecuter().executeTask(t);
    }

    /**
     * A class used to differentiate a video download and a history video download
     * @author sajjad
     *
     */
    public static class HistoryVideoDownloadTask extends VideoDownloadTask {

        public HistoryVideoDownloadTask(VideoModel model) {
            super(model);
        }

    }

    public static class ConvoHistoryAdapter extends ArrayAdapter<VideoModel> {

        private LayoutInflater mInflater;

        public ConvoHistoryAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.convo_history_list_item, parent, false);
                holder = new ViewHolder();
                holder.mSquareImageView = (ImageView) convertView.findViewById(R.id.iv_square);
                holder.mDateTextView = (TextView) convertView.findViewById(R.id.tv_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            VideoModel v = getItem(position);
            holder.mDateTextView.setText(v.getCreateDate());
            Picasso.with(getContext()).load(v.getThumbUrl()).into(holder.mSquareImageView);

            return convertView;
        }

        private class ViewHolder {
            public ImageView mSquareImageView;
            public TextView mDateTextView;
        }

    }

}
