package com.moziy.hollerback.contacts.task;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HBSyncHttpResponseHandler;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.Friend;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.service.task.AbsTask;

public class GetUnaddedFriendsTask extends AbsTask {

    private boolean mIsDone;
    private List<Contact> mUnaddedFriends;

    public List<Contact> getUnaddedFriends() {
        return mUnaddedFriends;
    }

    @Override
    public void run() {

        HBRequestManager.getUnaddedFriends(new HBSyncHttpResponseHandler<Envelope<List<Friend>>>(new TypeReference<Envelope<List<Friend>>>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, Envelope<List<Friend>> response) {

                mUnaddedFriends = Contact.getContactsFor(response.data);
                mIsSuccess = true;
            }

            @Override
            public void onApiFailure(Metadata metaData) {
                mIsSuccess = false;
            }

            @Override
            public void onPostResponse() {
                mIsDone = true;
                mIsFinished = true;

            }
        });

        while (!mIsDone) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
