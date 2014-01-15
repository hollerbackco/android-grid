package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsDelegate.Transaction;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.contacts.data.ContactViewHolder;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.AbsContactItem;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.Item;
import com.moziy.hollerback.fragment.contacts.ContactBookFragment.ContactBookChild;
import com.moziy.hollerback.model.Contact;

public class ContactsChildFragment extends FriendsFragment implements ContactBookChild {
    private static final String TAG = ContactsChildFragment.class.getSimpleName();

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

    private Transaction mTransaction;

    @Override
    protected void rebuildList() {
    }

    @Override
    public void onPause() {

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
        segmentData.mContacts = ci.getContactsExcludingHBContacts();
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

                if (mTransaction == null) {
                    mTransaction = mContactsInterface.beginTransaction();
                }

                if (selected) {
                    Log.d(TAG, "adding to friends; " + c.toString());
                    mSelected.add(c);
                    mTransaction.addToFriends(c);

                } else {

                    mSelected.remove(c);
                    mTransaction.removeFromFriends(c);
                }
            }

            // if keyboard is showing hide it
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);

        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        if (item.getItemId() == R.id.mi_next) {
            getFragmentManager().popBackStack();
            return true;
        }
        return super.onActionItemClicked(mode, item);
    }

    @Override
    public Transaction getContactTransaction() {
        return mTransaction;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTransaction = null; // nullify transaction when destroyed so we don't hol on to the context
    }
}
