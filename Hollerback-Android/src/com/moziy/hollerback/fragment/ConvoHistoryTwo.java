package com.moziy.hollerback.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.activeandroid.query.Update;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.RecordVideoFragment.RecordingInfo;
import com.moziy.hollerback.fragment.delegates.ConvoHistoryDelegate;
import com.moziy.hollerback.fragment.delegates.ConvoLoaderDelegate;
import com.moziy.hollerback.fragment.delegates.VideoPlayerDelegateTwo;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.Friend;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.task.AbsTask;
import com.moziy.hollerback.service.task.ActiveAndroidUpdateTask;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.ConversionUtil;
import com.moziy.hollerback.util.date.TimeUtil;
import com.squareup.picasso.Picasso;

public class ConvoHistoryTwo extends BaseFragment implements TaskClient, RecordingInfo {

    private static final String TAG = ConvoHistoryTwo.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    public static final String CONVO_ID_BUNDLE_ARG_KEY = "CONVO_ID";
    public static final String CONVO_TITLE_BUNDLE_ARG_KEY = "CONVO_TITLE";
    public static final String CONVO_MODEL_BUNDLE_ARG_KEY = "CONVO_MODEL";
    public static final String CONVO_ID_INSTANCE_STATE = "CONVO_ID_INSTANCE_STATE";
    public static final String TASK_QUEUE_INSTANCE_STATE = "TASK_QUEUE_INSTANCE_STATE";
    public static final String RECORDING_INFO_INSTANCE_STATE = "RECORDING_INFO_INSTANCE_STATE";

    public static ConvoHistoryTwo newInstance(long conversationId, String title) {
        ConvoHistoryTwo c = new ConvoHistoryTwo();
        Bundle args = new Bundle();
        args.putLong(CONVO_ID_BUNDLE_ARG_KEY, conversationId);
        args.putString(CONVO_TITLE_BUNDLE_ARG_KEY, title);
        c.setArguments(args);
        return c;
    }

    public static final int HISTORY_LIMIT = -1; // fetch all the history

    private long mConvoId;
    private String mConvoTitle;
    private LinkedList<Task> mTaskQueue; // queue of tasks such as fetching the model and fetching the videos
    private Bundle mRecordingInfo;
    private boolean mHasNew;

    private ConvoLoaderDelegate mConvoDelegate;
    private ConvoHistoryDelegate mHistoryDelegate;
    private VideoPlayerDelegateTwo mVideoPlayerDelegateTwo;

    private ConvoHistoryAdapter mAdapter;
    private ListView mConvoListView;
    private TextView mMembersTv;
    private String mMembersMessage;

    private List<Contact> mMembers;
    private static final String MEMBERS_WORKER = "MEMBERS_WORKER";

    @Override
    public void onAttach(Activity activity) {

        if (mAdapter == null) {
            mAdapter = new ConvoHistoryAdapter(getActivity(), R.layout.convo_history_list_item, R.id.tv_date);
        }

        if (mConvoDelegate == null) {
            mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);
            mConvoDelegate = new ConvoLoaderDelegate(mConvoId);
            mVideoPlayerDelegateTwo = new VideoPlayerDelegateTwo(mConvoId, mConvoDelegate);
            mVideoPlayerDelegateTwo.setAdapter(mAdapter);
            mConvoDelegate.setOnModelLoadedListener(mVideoPlayerDelegateTwo);
            mHistoryDelegate = new ConvoHistoryDelegate(mConvoId, mConvoDelegate);
            mHistoryDelegate.setOnHistoryVideoDownloadListener(mVideoPlayerDelegateTwo);

        }

        mVideoPlayerDelegateTwo.onPreSuperAttach(this);
        mConvoDelegate.onPreSuperAttach(this);
        mHistoryDelegate.onPreSuperAttach(this);

        super.onAttach(activity);

        mVideoPlayerDelegateTwo.onPostSuperAttach(this);
        mConvoDelegate.onPostSuperAttach(this);
        mHistoryDelegate.onPostSuperAttach(this);

    }

    @Override
    public void onDetach() {
        mVideoPlayerDelegateTwo.onPreSuperDetach(this);
        mConvoDelegate.onPreSuperDetach(this);
        mHistoryDelegate.onPreSuperDetach(this);

        super.onDetach();

        mVideoPlayerDelegateTwo.onPostSuperDetach(this);
        mConvoDelegate.onPostSuperDetach(this);
        mHistoryDelegate.onPostSuperDetach(this);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.convo_history_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.mi_about) {
            mMembersTv.setVisibility((mMembersTv.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().show();
        setHasOptionsMenu(true);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mConvoId = getArguments().getLong(CONVO_ID_BUNDLE_ARG_KEY);
        mConvoTitle = getArguments().getString(CONVO_TITLE_BUNDLE_ARG_KEY);

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

        addTaskToQueue(new GetMembersTask(mConvoId), MEMBERS_WORKER);

        mVideoPlayerDelegateTwo.init(savedInstanceState);
        mConvoDelegate.init(savedInstanceState);
        mHistoryDelegate.init(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private AdapterView.OnItemClickListener mClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // getView().findViewById(R.id.convo_include).setVisibility(View.VISIBLE);
            mVideoPlayerDelegateTwo.play(position);
        }
    };

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
        View v = inflater.inflate(R.layout.convo_history_layout, container, false);
        mConvoListView = (ListView) v.findViewById(R.id.lv_convo_history);
        mConvoListView.setOnItemClickListener(mClickListener);
        mConvoListView.setAdapter(mAdapter);

        mMembersTv = (TextView) v.findViewById(R.id.tv_members);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mVideoPlayerDelegateTwo.onPreSuperActivityCreated(savedInstanceState);
        mConvoDelegate.onPreSuperActivityCreated(savedInstanceState);

        super.onActivityCreated(savedInstanceState);

        mVideoPlayerDelegateTwo.onPostSuperActivityCreated(savedInstanceState);
        mConvoDelegate.onPostSuperActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {

        mVideoPlayerDelegateTwo.onPreSuperResume(this);
        mConvoDelegate.onPreSuperResume(this);

        super.onResume();

        if (mRecordingInfo != null) { // so we got our result from the recording fragment, time to go back
            getFragmentManager().popBackStack();
            return;
        }

        mVideoPlayerDelegateTwo.onPostSuperResume(this);
        mConvoDelegate.onPostSuperResume(this);
    }

    @Override
    public void onPause() {
        if (isRemoving()) {
            Log.d(TAG, "isRemoving = true");

        }

        mVideoPlayerDelegateTwo.onPreSuperPause(this);
        mConvoDelegate.onPreSuperPause(this);
        mHistoryDelegate.onPreSuperPause(this);

        super.onPause();

        mVideoPlayerDelegateTwo.onPostSuperPause(this);
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

        mVideoPlayerDelegateTwo.onSaveInstanceState(outState);
        mConvoDelegate.onSaveInstanceState(outState);
        mHistoryDelegate.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onTaskComplete(Task t) {
        mConvoDelegate.onTaskComplete(t);
        mHistoryDelegate.onTaskComplete(t);

        if (t instanceof GetMembersTask) {

            mMembers = ((GetMembersTask) t).getMembers();

            StringBuilder sb = new StringBuilder();
            sb.append("Members: ");
            for (Contact c : mMembers) {
                sb.append(c.mName).append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());
            mMembersTv.setText(sb.toString());

            if (isResumed()) {
                Fragment worker = getFragmentManager().findFragmentByTag(MEMBERS_WORKER);
                if (worker != null)
                    getFragmentManager().beginTransaction().remove(worker).commit();
            }

        }

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

    public VideoPlayerDelegateTwo getVideoDelegate() {
        return mVideoPlayerDelegateTwo;
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

    @Override
    protected String getScreenName() {
        return AnalyticsUtil.ScreenNames.CONVO_HISTORY;
    }

    @Override
    protected String getActionBarTitle() {
        return mConvoTitle;
    }

    public static class GetMembersTask extends AbsTask {

        private long mConvoId;
        boolean isDone = false;

        private List<Contact> mMembers;

        public GetMembersTask(long convoId) {
            mConvoId = convoId;
        }

        public List<Contact> getMembers() {
            return mMembers;
        }

        @Override
        public void run() {

            HBRequestManager.getMembers(mConvoId, new HBSyncHttpResponseHandler<Envelope<ArrayList<Friend>>>(new TypeReference<Envelope<ArrayList<Friend>>>() {
            }) {

                @Override
                public void onResponseSuccess(int statusCode, Envelope<ArrayList<Friend>> response) {

                    mMembers = Contact.getContactsFor(response.data);
                    mIsSuccess = true;
                }

                @Override
                public void onApiFailure(Metadata metaData) {
                    mIsSuccess = false;
                }

                @Override
                public void onPostResponse() {
                    mIsFinished = true;
                    isDone = true;
                }
            });

            while (!isDone) {
                Log.d(TAG, "not done");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    public static class ConvoHistoryAdapter extends ArrayAdapter<VideoModel> {
        private LruCache<String, Bitmap> mFileCache = new LruCache<String, Bitmap>(10); // up to 10 new videos
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
                holder.mName = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            VideoModel v = getItem(position);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
            try {
                Date date = df.parse(v.getCreateDate());
                holder.mDateTextView.setText(ConversionUtil.timeAgo(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.mName.setText(v.getSenderName());

            if (v.getThumbUrl() == null) {
                Log.d(TAG, "fill in");
            } else {
                Picasso.with(getContext()).load(v.getThumbUrl()).into(holder.mSquareImageView);
            }

            return convertView;
        }

        private class ViewHolder {
            public ImageView mSquareImageView;
            public TextView mDateTextView;
            public TextView mName;
        }

    }

}
