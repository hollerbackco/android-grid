package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.ContactsDelegate.Transaction;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.fragment.contacts.ContactBookFragment.ContactBookChild;

public class AddedMeChildFragment extends FriendsFragment implements ContactBookChild {

    public static AddedMeChildFragment newInstance() {
        return newInstance(FriendsFragment.NextAction.START_CONVERSATION);
    }

    public static AddedMeChildFragment newInstance(NextAction action) {
        AddedMeChildFragment f = new AddedMeChildFragment();
        Bundle arg = new Bundle();
        arg.putSerializable(NEXT_ACTION_BUNDLE_ARG_KEY, action);
        f.setArguments(arg);

        return f;
    }

    @Override
    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        List<ContactListSegmentData> listData = new ArrayList<ContactListSegmentData>();

        ContactListSegmentData segmentData = new ContactListSegmentData();
        segmentData.mSegmentTitle = getString(R.string.users_who_have_added_me);
        segmentData.mContacts = ci.getRecentContacts();
        segmentData.mTextPlaceHolderMsg = getString(R.string.nobody_has_added_you);
        listData.add(segmentData);

        // segmentData = new ContactListSegmentData();
        // segmentData.mSegmentTitle = getString(R.string.my_friends);
        // segmentData.mContacts = ci.getFriends();
        // listData.add(segmentData);

        return listData;
    }

    @Override
    public Transaction getContactTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

}
