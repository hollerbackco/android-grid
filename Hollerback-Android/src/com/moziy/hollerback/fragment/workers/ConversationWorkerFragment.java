package com.moziy.hollerback.fragment.workers;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;
import com.activeandroid.query.Select;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.model.ConversationModel;

public class ConversationWorkerFragment extends SherlockFragment {
    private static final String TAG = ConversationWorkerFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;

    private OnConversationsUpdated mCallback;

    // store the data associated with contacts, conversations, and videos
    private List<ConversationModel> mConversations;

    // this will only be called once when the fragment is first created
    // accross activity recreations, on creatview will be called however
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // // launch the intent service to download conversations
        // Intent intent = new Intent();
        // intent.setClass(getActivity(), SyncService.class);
        // getActivity().startService(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (OnConversationsUpdated) getActivity();
        if (mConversations != null) {
            mCallback.onUpdate(mConversations);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mConversations != null && mCallback != null) {
            Log.d(TAG, "deliver results to the target fragment");
            mCallback.onUpdate(mConversations);
            Log.d(TAG, "delivering conversations in onactivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;

    }

    // always have an updated list of conversations
    private void syncConversations() {

        new AsyncTask<Void, Void, List<ConversationModel>>() {

            @Override
            protected List<ConversationModel> doInBackground(Void... params) {

                return new Select().all().from(ConversationModel.class).execute();
            }

            @Override
            protected void onPostExecute(List<ConversationModel> result) {
                Log.d(TAG, "delivering conversations from a sync");
                mConversations = result;
                if (mCallback != null)
                    mCallback.onUpdate(mConversations);
            }
        }.execute();

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive - sync" + intent.getAction());
            if (intent.filterEquals(new Intent(IABIntent.NOTIFY_SYNC))) {
                boolean newData = intent.getBooleanExtra(IABIntent.PARAM_SYNC_RESULT, true);

                if (newData) { // if we have new data
                    Log.d(TAG, "new data was reported, lets sync");
                    syncConversations();
                } else {

                    if (mConversations == null) {
                        Log.d(TAG, "new data was NOT reported, but we don't have any data");
                        syncConversations();
                    }

                }
            } else {
                Log.w(TAG, "there was a sync failure");
                if (mConversations == null) {
                    syncConversations();
                }
            }

        }
    };

    public static interface OnConversationsUpdated {

        public void onUpdate(List<ConversationModel> conversations);

    }
}
