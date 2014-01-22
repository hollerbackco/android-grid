package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ProgressBar;
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

    private static final String SEARCH_RESULT_INSTANCE_STATE = "SEARCH_RESULT_INSTANCE_STATE";

    public static SearchForUserFragment newInstance() {
        return new SearchForUserFragment();
    }

    private Contact mFriend;
    private ProgressBar mProgress;
    private Transaction mContactTransaction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(SEARCH_RESULT_INSTANCE_STATE)) {
            mFriend = (Contact) savedInstanceState.getSerializable(SEARCH_RESULT_INSTANCE_STATE);
        }
        super.onCreate(savedInstanceState);
        mContactTransaction = mContactsInterface.beginTransaction();

        if (savedInstanceState != null) {
            for (Contact selected : mSelected)
                mContactTransaction.addToFriends(selected);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSearchBar.setVisibility(View.VISIBLE);
        mSearchBar.setImeActionLabel(getString(R.string.action_search), EditorInfo.IME_ACTION_DONE);
        mSearchBar.setHint(getString(R.string.name));
        mSearchBar.removeTextChangedListener(mDefaultTextWatcher);
        mSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    String username = v.getText().toString().trim();
                    if (!mContactsInterface.hasFriend(username)) {
                        mProgress.setVisibility(View.VISIBLE);
                        HBRequestManager.findFriend(username, mResponseHandler);
                    } else {
                        Toast.makeText(HollerbackApplication.getInstance(), String.format(getString(R.string.already_a_friend), username), Toast.LENGTH_LONG).show();
                    }

                }

                return false;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mFriend != null)
            outState.putSerializable(SEARCH_RESULT_INSTANCE_STATE, mFriend);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        List<ContactListSegmentData> segmentList = new ArrayList<ContactListSegmentData>();

        ContactListSegmentData segmentData = new ContactListSegmentData();

        segmentData.mSegmentTitle = "User";
        segmentData.mTextPlaceHolderMsg = "Search for a friend by entering their username";

        List<Contact> contacts = new ArrayList<Contact>();
        if (mFriend != null && !mSelected.contains(mFriend)) { // don't want to readd a user
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
                    mContactTransaction.addToFriends(c); // add to friends
                } else {
                    mSelected.remove(c);
                    mContactTransaction.removeFromFriends(c); // add to friends
                }
            }

        }
    }

    private HBAsyncHttpResponseHandler<Envelope<Friend>> mResponseHandler = new HBAsyncHttpResponseHandler<Envelope<Friend>>(new TypeReference<Envelope<Friend>>() {
    }) {

        @Override
        public void onResponseSuccess(int statusCode, Envelope<Friend> response) {

            mProgress.setVisibility(View.GONE);

            // Log.d(TAG, "friend " + response.data.mUsername + " added as a friend");
            if (response.data != null) {
                mFriend = new Contact(response.data);

                rebuildList();
            } else {
                Toast.makeText(HollerbackApplication.getInstance(), "no user with that name found", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onApiFailure(Metadata metaData) {
            Log.d(TAG, "network error");
            mProgress.setVisibility(View.GONE);
        }

    };

    private void rebuildList() {
        mAdapter = new ContactsAdapterData(mActivity);
        mItemManager = new ItemManager();
        mItemManager.setItems(buildSegmentData(mContactsInterface));
        setItemSelections(); // ensure previously selected items get reselected
        mAdapter.setItemManager(mItemManager);
        mContactsList.setAdapter(mAdapter);
        mStickyListView.setAdapter(mAdapter);
        mStickyListView.setIndexer(mAdapter);

    }

    @Override
    public int getLayoutId() {
        return R.layout.search_for_user_fragment;
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
