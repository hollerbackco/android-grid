package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactListSegmentData;
import com.moziy.hollerback.util.contacts.ContactsInterface;

public class ContactsChildFragment extends FriendsFragment {

    @Override
    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        List<ContactListSegmentData> listData = new ArrayList<ContactListSegmentData>();

        ContactListSegmentData segmentData = new ContactListSegmentData();
        segmentData.mSegmentTitle = getString(R.string.users_in_my_contacts);
        segmentData.mContacts = ci.getHollerbackContacts();
        listData.add(segmentData);

        segmentData = new ContactListSegmentData();
        segmentData.mSegmentTitle = getString(R.string.invite_contacts);
        segmentData.mContacts = ci.getDeviceContacts();
        listData.add(segmentData);

        return listData;

    }
}
