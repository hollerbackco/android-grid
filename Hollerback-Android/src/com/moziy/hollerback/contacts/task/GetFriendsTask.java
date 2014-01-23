package com.moziy.hollerback.contacts.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.database.ActiveRecordFields;
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

                // lets delete the friends from the db
                new Delete().from(Friend.class).execute();

                ActiveAndroid.beginTransaction();
                try {
                    // lets save the friends
                    for (Contact c : mFriends) {
                        c.save();
                    }

                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }

                // sort the friends
                Collections.sort(mFriends, Contact.COMPARATOR);

                Log.d(TAG, "user has " + mFriends.size() + " friends and " + mRecentFriends.size() + " recents");

                mIsSuccess = true;
            }

            @Override
            public void onApiFailure(Metadata metaData) {

                // just load from the local database
                List<Friend> recentFriends = new Select().from(Friend.class).where(ActiveRecordFields.C_FRIENDS_LAST_CONTACT_TIME + " IS NOT NULL ")
                        .orderBy("strftime('%s'," + ActiveRecordFields.C_FRIENDS_LAST_CONTACT_TIME + ") DESC").limit(3).execute();

                mRecentFriends = new ArrayList<Contact>(Contact.getContactsFor(recentFriends));

                // get the list of friends
                List<Friend> friends = new Select().from(Friend.class).orderBy(ActiveRecordFields.C_FRIENDS_NAME).execute();
                mFriends = new ArrayList<Contact>(Contact.getContactsFor(friends));

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
