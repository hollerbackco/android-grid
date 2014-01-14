package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.activeandroid.ActiveAndroid;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactListSegmentData;
import com.moziy.hollerback.contacts.ContactViewHolder;
import com.moziy.hollerback.contacts.ContactsAdapterData.AbsContactItem;
import com.moziy.hollerback.contacts.ContactsAdapterData.Item;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.contacts.ContactsInterface;

public class ContactsChildFragment extends FriendsFragment {

    public static ContactsChildFragment newInstance() {
        return newInstance(FriendsFragment.NextAction.START_CONVERSATION);
    }

    public static ContactsChildFragment newInstance(NextAction action) {
        ContactsChildFragment f = new ContactsChildFragment();
        Bundle arg = new Bundle();
        arg.putSerializable(NEXT_ACTION_BUNDLE_ARG_KEY, action);
        f.setArguments(arg);

        return f;
    }

    @Override
    public void onPause() {

        if (isRemoving()) {
            // TOOD: put in background task
            ActiveAndroid.beginTransaction();
            try {
                for (Contact c : mContactsInterface.getFriends()) {
                    c.save();
                }

                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
        super.onPause();

    }

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = (Item) parent.getItemAtPosition(position);
        if (item.getContact() != null) {

            Contact c = item.getContact();

            if (item instanceof AbsContactItem) {
                boolean selected = !((AbsContactItem) item).getSelected();
                ((AbsContactItem) item).setSelected(selected);
                ContactViewHolder holder = (ContactViewHolder) view.getTag();
                holder.checkbox.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);

                if (selected) {
                    mSelected.add(c);
                } else {
                    mSelected.remove(c);
                }
            }

            // if (mSelected.size() == 1) {
            // // getSherlockActivity().invalidateOptionsMenu();
            // } else if (mSelected.size() == 0) {
            //
            // }

            mContactsInterface.getFriends().add(c);
            mContactsInterface.getDeviceContacts().remove(c);
            mContactsInterface.getHollerbackContacts().remove(c);

            // StartConversationFragment f = StartConversationFragment.newInstance(new String[] {
            // c.mPhone
            // }, c.mName, new boolean[] {
            // c.mIsOnHollerback
            // });

            // if keyboard is showing hide it
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);

        }
    }
}
