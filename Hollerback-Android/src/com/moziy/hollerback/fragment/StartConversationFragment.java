package com.moziy.hollerback.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.fragment.RecordVideoFragment.RecordingInfo;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.VideoUploadIntentService;
import com.moziy.hollerback.util.HBFileUtil;
import com.moziy.hollerback.util.ImageUtil;
import com.moziy.hollerback.util.SmsUtil;
import com.moziy.hollerback.widget.CustomEditText;

public class StartConversationFragment extends BaseFragment implements RecordingInfo {

    private static final String ON_SAVE_ARG_RECORDING_INFO = "recording_info";
    private static final String ON_SAVE_ARG_WAITING = "waiting";
    public static final String FRAGMENT_TAG = StartConversationFragment.class.getSimpleName();
    private static final String TAG = FRAGMENT_TAG;
    private static final String PHONES_BUNDLE_ARG_KEY = "phones";
    private static final String TITLE_BUNDLE_ARG_KEY = "title";
    private static final String IS_HB_USERS_BUNDLE_ARG_KEY = "IS_HB_USERS";
    public static final String CONTACTS_BUNDLE_ARG_KEY = "CONTACTS";

    public static StartConversationFragment newInstance(String[] phones, String title, boolean[] isHbUser) {
        StartConversationFragment f = new StartConversationFragment();
        Bundle params = new Bundle();
        params.putStringArray(PHONES_BUNDLE_ARG_KEY, phones);
        params.putString(TITLE_BUNDLE_ARG_KEY, title);
        params.putBooleanArray(IS_HB_USERS_BUNDLE_ARG_KEY, isHbUser);

        f.setArguments(params);
        return f;
    }

    public static StartConversationFragment newInstance(HashSet<Contact> contacts) {
        Bundle b = new Bundle();
        b.putSerializable(CONTACTS_BUNDLE_ARG_KEY, contacts);
        StartConversationFragment f = new StartConversationFragment();
        f.setArguments(b);
        return f;

    }

    private ProgressBar mProgressSpinner;
    private String[] mPhones;
    private String mTitle;
    private Bundle mRecordingInfo = null;
    private CustomEditText mTitleEt;

    private boolean mIsWaiting = false; // whether we're waiting on an event
    private HashSet<Contact> mRecipients;
    private ArrayList<Contact> mNonHBContacts;

    private InternalReceiver mReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Bundle args = getArguments();

        mRecipients = (HashSet<Contact>) args.getSerializable(CONTACTS_BUNDLE_ARG_KEY);

        // create the title string and get all the phones
        StringBuilder sb = new StringBuilder();
        List<String> allPhones = new ArrayList<String>();
        mNonHBContacts = new ArrayList<Contact>();
        for (Contact c : mRecipients) {
            sb.append(c.mName).append(", ");

            for (String phone : c.mPhones) {
                allPhones.add(phone);
            }

            if (!c.mIsOnHollerback) {
                mNonHBContacts.add(c);
            }

        }
        sb.delete(sb.length() - 2, sb.length() - 1); // there should always be a recipient
        mTitle = sb.toString();
        mPhones = allPhones.toArray(new String[allPhones.size()]);
        Log.d(TAG, "title: " + mTitle);

        // mTitle = args.getString(TITLE_BUNDLE_ARG_KEY);
        // mPhones = args.getStringArray(PHONES_BUNDLE_ARG_KEY);

        // mIsHBUsers = args.getBooleanArray(IS_HB_USERS_BUNDLE_ARG_KEY);
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

        mTitleEt = ((CustomEditText) v.findViewById(R.id.tv_title));
        mTitleEt.setHint(mTitle);
        mTitleEt.setSelection(0);

        mProgressSpinner = (ProgressBar) v.findViewById(R.id.pb_spinner);

        ((Button) v.findViewById(R.id.bt_edit)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mTitleEt.requestFocus();
                InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                im.showSoftInput(mTitleEt, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        v.findViewById(R.id.ib_start_video).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isResumed()) { // only if resumed
                    mIsWaiting = true; // mark this fragment as waiting for broadcasts
                    // register for inapp events
                    IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONVERSATION_CREATED);
                    IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONVERSATION_CREATE_FAILURE);
                    IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.RECORDING_FAILED);
                    IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.RECORDING_CANCELLED);

                    if (mTitleEt.getText().length() > 0) {
                        mTitle = mTitleEt.getText().toString();
                    }

                    Log.d(TAG, "sending message with title: " + mTitle);
                    RecordVideoFragment f = RecordVideoFragment.newInstance(mPhones, mTitle);
                    f.setTargetFragment(StartConversationFragment.this, 0);

                    // go to the video fragment
                    getFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in_scale_up, R.anim.fade_out, R.anim.slide_in_from_top, R.anim.slide_out_to_bottom).addToBackStack(null)
                            .replace(R.id.fragment_holder, f).commit();
                }
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
                    String guid = mRecordingInfo.getString(RecordingInfo.RESOURCE_GUID);

                    Intent uploadIntent = new Intent();
                    uploadIntent.setClass(getActivity(), VideoUploadIntentService.class);
                    uploadIntent.putExtra(VideoUploadIntentService.INTENT_ARG_RESOURCE_ID, resourceId);
                    uploadIntent.putExtra(VideoUploadIntentService.INTENT_ARG_TOTAL_PARTS, totalParts);
                    uploadIntent.putExtra(VideoUploadIntentService.INTENT_ARG_PART, totalParts); // not used anymore
                    getActivity().startService(uploadIntent);

                    // Fragment f = getFragmentManager().findFragmentByTag(ConversationListFragment.FRAGMENT_TAG);
                    // // TODO - Sajjad: Delay the popping until after we've shown the sent icon
                    // getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // go back to the conversation fragment, popping everything
                    // if (f == null)
                    // f = ConversationListFragment.newInstance();
                    // getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom).replace(R.id.fragment_holder, f).commit();

                    Fragment f = getFragmentManager().findFragmentByTag(ConversationListFragment.FRAGMENT_TAG);

                    if (f == null) {
                        Log.d(TAG, "ConversationListFragment not found");
                        // TODO - Sajjad: Delay the popping until after we've shown the sent icon
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // go back to the conversation fragment, popping everything

                        f = ConversationListFragment.newInstance();
                        getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom).replace(R.id.fragment_holder, f).commit();
                    } else {
                        Log.d(TAG, "ConversationListFragment found!");
                        getFragmentManager().popBackStack(ConversationListFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }

                    Context c = HollerbackApplication.getInstance();
                    Toast.makeText(c, c.getString(R.string.message_sent_simple), Toast.LENGTH_LONG).show();

                    if (!mNonHBContacts.isEmpty())
                        sendSMSInvite(guid);

                } else {
                    // TODO: if it's a conversation creation failure, display a dialog
                    mIsWaiting = false;
                    mProgressSpinner.setVisibility(View.INVISIBLE);
                    // go back to contacts or back to the conversation list?
                    Toast.makeText(getActivity(), "couldn't send message, try again", Toast.LENGTH_SHORT).show();
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

        private void sendSMSInvite(String guid) {

            ImageUtil.generatePngThumbnailFromVideo(0, guid);
            Uri uri = Uri.fromFile(new File(HBFileUtil.getLocalVideoFile(0, guid, "png")));

            SmsUtil.invite(mActivity, mNonHBContacts, HollerbackApplication.getInstance().getString(R.string.start_convo_sms_body), uri, "image/png");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mReceiver.unregister();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onRecordingFinished(Bundle info) {
        mRecordingInfo = info;
    }

}
