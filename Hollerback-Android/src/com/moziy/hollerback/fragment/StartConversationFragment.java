package com.moziy.hollerback.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.fragment.RecordVideoFragment.RecordingInfo;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.VideoUploadIntentService;

public class StartConversationFragment extends BaseFragment implements RecordingInfo {

    private static final String ON_SAVE_ARG_RECORDING_INFO = "recording_info";
    private static final String ON_SAVE_ARG_WAITING = "waiting";
    public static final String FRAGMENT_TAG = StartConversationFragment.class.getSimpleName();
    private static final String TAG = FRAGMENT_TAG;
    private static final String PHONES_BUNDLE_ARG_KEY = "phones";
    private static final String TITLE_BUNDLE_ARG_KEY = "title";

    public static StartConversationFragment newInstance(String[] phones, String title) {
        StartConversationFragment f = new StartConversationFragment();
        Bundle params = new Bundle();
        params.putStringArray(PHONES_BUNDLE_ARG_KEY, phones);
        params.putString(TITLE_BUNDLE_ARG_KEY, title);
        f.setArguments(params);
        return f;
    }

    private ProgressBar mProgressSpinner;
    private String[] mPhones;
    private String mTitle;
    private Bundle mRecordingInfo = null;

    private boolean mIsWaiting = false; // whether we're waiting on an event

    private InternalReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTitle = args.getString(TITLE_BUNDLE_ARG_KEY);
        mPhones = args.getStringArray(PHONES_BUNDLE_ARG_KEY);
        Log.d(TAG, "onCreate");
        // New Conversation Created Intent
        mReceiver = new InternalReceiver();
        if (savedInstanceState != null) {

            mIsWaiting = savedInstanceState.getBoolean(ON_SAVE_ARG_WAITING);
            mRecordingInfo = savedInstanceState.getBundle(ON_SAVE_ARG_RECORDING_INFO);

            if (savedInstanceState.getBoolean(ON_SAVE_ARG_WAITING)) {

                Log.d(TAG, "reregistering broadcasts in configuration changes");
                mReceiver.register();
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.start_conversation_layout, container, false);

        ((TextView) v.findViewById(R.id.tv_title)).setText(mTitle);

        mProgressSpinner = (ProgressBar) v.findViewById(R.id.pb_spinner);

        v.findViewById(R.id.ib_start_video).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mIsWaiting = true; // mark this fragment as waiting for broadcasts
                // register for inapp events
                IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONVERSATION_CREATED);
                IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONVERSATION_CREATE_FAILURE);
                IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.RECORDING_FAILED);
                IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.RECORDING_CANCELLED);

                RecordVideoFragment f = RecordVideoFragment.newInstance(mPhones, mTitle);
                f.setTargetFragment(StartConversationFragment.this, 0);

                // go to the video fragment
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_holder, f).commitAllowingStateLoss();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsWaiting) {
            mProgressSpinner.setVisibility(View.VISIBLE);
        } else {
            mProgressSpinner.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ON_SAVE_ARG_WAITING, mIsWaiting);
        outState.putBundle(ON_SAVE_ARG_RECORDING_INFO, mRecordingInfo);
        // save the state we were in
    }

    @Override
    protected void initializeView(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // receive broadcasts on the status of conversations
    private class InternalReceiver extends BroadcastReceiver {

        private final String TAG = InternalReceiver.class.getSimpleName();

        public void register() {
            // register for inapp events
            IABroadcastManager.registerForLocalBroadcast(this, IABIntent.CONVERSATION_CREATED);
            IABroadcastManager.registerForLocalBroadcast(this, IABIntent.CONVERSATION_CREATE_FAILURE);
            IABroadcastManager.registerForLocalBroadcast(this, IABIntent.RECORDING_FAILED);
            IABroadcastManager.registerForLocalBroadcast(this, IABIntent.RECORDING_CANCELLED);
        }

        public void unregister() {
            IABroadcastManager.unregisterLocalReceiver(mReceiver);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");
            if (isAdded()) { // only do work if the fragment is added

                if (intent.filterEquals(new Intent(IABIntent.CONVERSATION_CREATED))) {
                    mIsWaiting = false;
                    Log.d(TAG, "conversation created!, let's upload");
                    mProgressSpinner.setVisibility(View.INVISIBLE); // TODO: add transition animation

                    if (mRecordingInfo == null) {
                        throw new IllegalStateException("no recording info found: expected bundle from recording fragment");
                    }

                    long resourceId = mRecordingInfo.getLong(RecordingInfo.RESOURCE_ROW_ID);
                    int totalParts = mRecordingInfo.getInt(RecordingInfo.RECORDED_PARTS);
                    for (int i = 0; i < totalParts; i++) {
                        Intent uploadIntent = new Intent();
                        uploadIntent.setClass(getActivity(), VideoUploadIntentService.class);
                        uploadIntent.putExtra(VideoUploadIntentService.INTENT_ARG_RESOURCE_ID, resourceId);
                        uploadIntent.putExtra(VideoUploadIntentService.INTENT_ARG_TOTAL_PARTS, totalParts);
                        uploadIntent.putExtra(VideoUploadIntentService.INTENT_ARG_PART, i);
                        getActivity().startService(uploadIntent);
                    }

                    // TODO - Sajjad: Delay the popping until after we've shown the sent icon
                    getFragmentManager().popBackStack(ContactsFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE); // go back to the conversation fragment, popping everything

                } else {
                    // TODO: if it's a conversation creation failure, display a dialog
                    mIsWaiting = false;
                    mProgressSpinner.setVisibility(View.INVISIBLE);
                    // go back to contacts or back to the conversation list?
                    Toast.makeText(getActivity(), "couldn't create conversation", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "conversation failed");

                    if (mRecordingInfo != null) {

                        long rowId = mRecordingInfo.getLong(RESOURCE_ROW_ID);
                        Log.d(TAG, "deleting row: " + rowId);
                        new Delete().from(VideoModel.class).where("Id = ?", rowId).executeSingle();

                    }
                    // cleanup and remove data from sql?
                }
            } else {
                Log.w(TAG, "received broadcast when not added");
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.unregister();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onRecordingFinished(Bundle info) {
        mRecordingInfo = info;

    }

}
