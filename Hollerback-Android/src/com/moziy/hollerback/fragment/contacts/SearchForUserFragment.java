package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.connection.HBAsyncHttpResponseHandler;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.contacts.ContactsDelegate.Transaction;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.contacts.data.ContactViewHolder;
import com.moziy.hollerback.contacts.data.ContactsAdapterData;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.AbsContactItem;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.Item;
import com.moziy.hollerback.fragment.AbsContactListFragment;
import com.moziy.hollerback.fragment.contacts.ContactBookFragment.ContactBookChild;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.model.Friend;
import com.moziy.hollerback.model.web.Envelope;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.util.AnalyticsUtil;

public class SearchForUserFragment extends AbsContactListFragment implements ContactBookChild {
    private static final String TAG = SearchForUserFragment.class.getSimpleName();

    public static SearchForUserFragment newInstance() {
        return new SearchForUserFragment();
    }

    private Contact mFriend;
    private ProgressDialog mProgressDialog;
    private Transaction mContactTransaction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContactTransaction = mContactsInterface.beginTransaction();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSearchBar.setVisibility(View.VISIBLE);
        mSearchBar.setImeActionLabel(getString(R.string.action_search), 0);
        mSearchBar.removeTextChangedListener(mDefaultTextWatcher);
        mSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == 0) {

                    HBRequestManager.findFriend(v.getText().toString().trim(), mResponseHandler);

                    return true;
                }

                return false;
            }
        });
    }

    @Override
    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        List<ContactListSegmentData> segmentList = new ArrayList<ContactListSegmentData>();

        ContactListSegmentData segmentData = new ContactListSegmentData();

        segmentData.mSegmentTitle = "User";
        segmentData.mTextPlaceHolderMsg = "Search for a friend by entering their username";

        List<Contact> contacts = new ArrayList<Contact>();
        if (mFriend != null) {
            contacts.add(mFriend);
        }

        contacts.addAll(mSelected);

        segmentData.mContacts = contacts;
        segmentList.add(segmentData);

        return segmentList;
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
                    mContactTransaction.addToFriends(mFriend); // add to friends
                } else {
                    mSelected.remove(c);
                    mContactTransaction.removeFromFriends(mFriend); // add to friends
                }
            }

        }
    }

    private HBAsyncHttpResponseHandler<Envelope<Friend>> mResponseHandler = new HBAsyncHttpResponseHandler<Envelope<Friend>>(new TypeReference<Envelope<Friend>>() {
    }) {

        @Override
        public void onResponseSuccess(int statusCode, Envelope<Friend> response) {
            // Log.d(TAG, "friend " + response.data.mUsername + " added as a friend");
            if (response.data != null) {
                mFriend = new Contact(response.data);
                mFriend.mName = mFriend.mUsername;

                rebuildList();
            } else {
                Toast.makeText(HollerbackApplication.getInstance(), "no user with that name found", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onApiFailure(Metadata metaData) {
            Log.d(TAG, "network error");
        }

    };

    private void rebuildList() {
        mAdapter = new ContactsAdapterData(mActivity);
        mItemManager = new ItemManager();
        mItemManager.setItems(buildSegmentData(mContactsInterface));
        mAdapter.setItemManager(mItemManager);
        mContactsList.setAdapter(mAdapter);
        mStickyListView.setAdapter(mAdapter);
        mStickyListView.setIndexer(mAdapter);

    }

    @Override
    protected String getScreenName() {

        return AnalyticsUtil.ScreenNames.SEARCH_FOR_USERNAME;
    }

    @Override
    public Transaction getContactTransaction() {
        return mContactTransaction;
    }

}
