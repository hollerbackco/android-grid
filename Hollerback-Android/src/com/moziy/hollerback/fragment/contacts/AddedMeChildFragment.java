package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactListSegmentData;
import com.moziy.hollerback.util.contacts.ContactsInterface;

public class AddedMeChildFragment extends FriendsFragment {

    @Override
    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        List<ContactListSegmentData> listData = new ArrayList<ContactListSegmentData>();

        ContactListSegmentData segmentData = new ContactListSegmentData();
        segmentData.mSegmentTitle = getString(R.string.users_who_have_added_me);
        segmentData.mContacts = ci.getRecentContacts();
        listData.add(segmentData);

        // segmentData = new ContactListSegmentData();
        // segmentData.mSegmentTitle = getString(R.string.my_friends);
        // segmentData.mContacts = ci.getFriends();
        // listData.add(segmentData);

        return listData;
    }

}
