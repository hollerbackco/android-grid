package com.moziy.hollerback.contacts.task;

import java.util.ArrayList;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.Friend;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.GetFriendsResponse;
import com.moziy.hollerback.service.task.AbsTask;

public class GetFriendsTask extends AbsTask {
    private static final String TAG = GetFriendsTask.class.getSimpleName();
    private ArrayList<Contact> mFriends;
    private ArrayList<Contact> mRecentFriends;
    private volatile boolean isDone = false;

    @Override
    public void run() {
        Log.d(TAG, "getting friends");
        HBRequestManager.getFriends(new HBSyncHttpResponseHandler<Envelope<GetFriendsResponse>>(new TypeReference<Envelope<GetFriendsResponse>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<GetFriendsResponse> response) {
                Log.d(TAG, "fetched friends successfully");

                mFriends = new ArrayList<Contact>();
                for (Friend f : response.data.friends) {
                    mFriends.add(new Contact(f));
                }

                mRecentFriends = new ArrayList<Contact>();
                for (Friend recent : response.data.recent_friends) {
                    for (Contact friend : mFriends) {
                        if (friend.mUsername.equals(recent.mUsername)) {
                            mRecentFriends.add(friend);
                        }
                    }
                }

                Log.d(TAG, "user has " + mFriends.size() + " friends and " + mRecentFriends.size() + " recents");

                mIsSuccess = true;
            }

            @Override
            public void onApiFailure(Metadata metaData) {
                mIsSuccess = false;
                Log.w(TAG, "failure getting friends");
            }

            @Override
            public void onPostResponse() {
                mIsFinished = true;
                isDone = true;
            }
        });

        while (!isDone) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Contact> getFriends() {
        return mFriends;
    }

    public ArrayList<Contact> getRecentFriends() {
        return mRecentFriends;
    }

}
