package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filter.FilterListener;
import android.widget.SearchView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsDelegate.Transaction;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.contacts.data.ContactViewHolder;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.AbsContactItem;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.Item;
import com.moziy.hollerback.fragment.AbsContactListFragment;
import com.moziy.hollerback.fragment.contacts.ContactBookFragment.ContactBookChild;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.AnalyticsUtil;

public class ContactsChildFragment extends AbsContactListFragment implements ContactBookChild {
    private static final String TAG = ContactsChildFragment.class.getSimpleName();

    private SearchView mSearchView;

    public static ContactsChildFragment newInstance() {
        return new ContactsChildFragment();
    }

    private Transaction mTransaction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ContactBookFragment) getParentFragment()).mCurrentPage = 0;
        getSherlockActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.contact_book_child_menu, menu);
        final MenuItem item = menu.findItem(R.id.mi_search);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        // mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
        //
        // @Override
        // public void onFocusChange(View v, boolean hasFocus) {
        // mSearchView.setQuery("", false);
        // }
        // });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "submitting query");
                mSearchView.setQuery("", false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                Log.d(TAG, "has focus: " + mSearchView.hasFocus());
                if (mStickyListView != null) {

                    mAdapter.getFilter().filter(newText, new FilterListener() {

                        @Override
                        public void onFilterComplete(int count) {
                            if (newText.length() > 0) {
                                mStickyListView.disableStickyHeader();
                            } else {
                                mStickyListView.enableStickyHeader();
                            }

                        }
                    });

                }
                return true;
            }
        });

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
        segmentData.mContacts = ci.getHBContactsExcludingFriends();
        segmentData.mTextPlaceHolderMsg = getString(R.string.no_friends_in_contacts);
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

                if (mTransaction == null) { // contact/friend update transaction
                    mTransaction = mContactsInterface.beginTransaction();
                }

                if (selected) {
                    Log.d(TAG, "adding to friends; " + c.toString());

                    mSelected.add(c);
                    if (c.mIsOnHollerback) {
                        mTransaction.addToFriends(c);
                    } else {
                        mContactsInterface.getInviteList().add(c);
                    }

                } else {
                    Log.d(TAG, "removing from friends; " + c.toString());

                    Log.d(TAG, "removed: " + mSelected.remove(c));

                    for (Contact sel : mSelected) {
                        Log.d(TAG, "still selected: " + sel.toString());
                    }

                    if (!c.mIsOnHollerback) {
                        mContactsInterface.getInviteList().remove(c);
                    } else {
                        mTransaction.removeFromFriends(c);
                    }
                }
            }

            // if keyboard is showing hide it
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);

        }
    }

    @Override
    public Transaction getContactTransaction() {
        return mTransaction;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected String getScreenName() {
        return AnalyticsUtil.ScreenNames.CONTACT_BOOK_CHILD;
    }
}
