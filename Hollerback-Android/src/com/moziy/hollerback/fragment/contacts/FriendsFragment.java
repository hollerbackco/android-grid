package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsDelegate.Transaction;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.ContactsInterface.LOADING_STATE;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.contacts.data.ContactViewHolder;
import com.moziy.hollerback.contacts.data.ContactsAdapterData;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.AbsContactItem;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.Item;
import com.moziy.hollerback.fragment.AbsContactListFragment;
import com.moziy.hollerback.fragment.StartConversationFragment;
import com.moziy.hollerback.fragment.contacts.ContactBookFragment.OnContactBookSelectionsDone;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.SmsUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class FriendsFragment extends AbsContactListFragment implements ActionMode.Callback, View.OnClickListener, OnContactBookSelectionsDone {
    private static final String TAG = FriendsFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    // type - serializable/enum
    public static final String NEXT_ACTION_BUNDLE_ARG_KEY = "NEXT_ACTION";
    private static final int ALPHABETICAL_HEADER_BOUND = 25;

    public enum NextAction {
        START_CONVERSATION, INVITE_FRIENDS
    };

    protected NextAction mAction;
    protected ActionMode mActionMode;
    protected android.view.ActionMode mMultiActionMode;

    protected View mBottomBarLayout;
    protected Button mNextButton;
    protected boolean mShowAlphabeticalHeaders;

    private boolean mRebuildList;
    private SearchView mSearchView;

    private Set<Contact> mContactRemovalSet;
    private Transaction mFriendRemovalTransaction;

    private GridView mGridView;
    private ArrayAdapter<String> mRecipientAdapter;

    public static FriendsFragment newInstance() {
        return newInstance(NextAction.START_CONVERSATION);
    }

    public static FriendsFragment newInstance(NextAction action) {
        FriendsFragment f = new FriendsFragment();
        Bundle arg = new Bundle();
        arg.putSerializable(NEXT_ACTION_BUNDLE_ARG_KEY, action);
        f.setArguments(arg);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInflater = LayoutInflater.from(getActivity());

        mAction = (NextAction) getArguments().getSerializable(NEXT_ACTION_BUNDLE_ARG_KEY);

        switch (mAction) {
            case START_CONVERSATION:
                getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.start_conversation));
                showIntroDialog();
                break;
            case INVITE_FRIENDS:
                getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.invite_friends_title));
                break;
        }

        mContactRemovalSet = new HashSet<Contact>();
        mFriendRemovalTransaction = mContactsInterface.beginTransaction();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        mBottomBarLayout = v.findViewById(R.id.bottom);
        mNextButton = (Button) mBottomBarLayout.findViewById(R.id.bt_next);
        mNextButton.setOnClickListener(this);

        mGridView = (GridView) mBottomBarLayout.findViewById(R.id.gl_recipients);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContactsList.setMultiChoiceModeListener(mMultiChoiceLisenter);
        mContactsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mContactsList.setOnItemLongClickListener(this);

        mRecipientAdapter = new ArrayAdapter<String>(getActivity(), R.layout.chips_adapter, R.id.textView1);
        if (savedInstanceState != null) {
            for (Contact c : mSelected) {
                mRecipientAdapter.add(c.mName);
            }
        }
        mGridView.setAdapter(mRecipientAdapter);

    }

    @Override
    public int getLayoutId() {
        return R.layout.friends_fragment_layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSelected != null && !mSelected.isEmpty()) {
            showBottomBar();
        }

        if (mRebuildList) {
            rebuildList(false);
            mRebuildList = false;

            for (Item i : mItemManager.getItems()) {
                if (i.getContact() == null) {
                    continue;
                }

                for (Contact selected : mSelected) {
                    if (i.getContact() == selected) {
                        ((AbsContactItem) i).setSelected(true);
                    }
                }
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRemoving()) {
            if (mActionMode != null) {
                mActionMode.finish();
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.friend_list_menu, menu);
        final MenuItem item = menu.findItem(R.id.mi_search);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        // mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
        //
        // @Override
        // public void onFocusChange(View v, boolean hasFocus) {
        // if (!hasFocus) {
        // item.collapseActionView();
        // mSearchView.setQuery("", false);
        // }
        //
        // }
        // });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (mStickyListView != null) {
                    if (newText.length() > 0) {
                        mStickyListView.disableStickyHeader();
                    } else {
                        mStickyListView.enableStickyHeader();
                    }
                    mAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mi_next) {

            if (mAction == NextAction.START_CONVERSATION) {
                StartConversationFragment f = StartConversationFragment.newInstance(mSelected);
                getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                        .replace(R.id.fragment_holder, f).addToBackStack(FRAGMENT_TAG).commit();
            } else {
                // send an sms and then pop the backstack
                SmsUtil.invite(mActivity, new ArrayList<Contact>(mSelected), HollerbackApplication.getInstance().getString(R.string.sms_invite_friends), null, null);
                getFragmentManager().popBackStack();
            }

            return true;
        } else if (item.getItemId() == R.id.mi_add_friends) {

            ContactBookFragment fragment = ContactBookFragment.newInstance();
            fragment.setTargetFragment(this, 0);
            mActivity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                    .replace(R.id.fragment_holder, fragment).addToBackStack(FRAGMENT_TAG).commit();

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method will rebuild the list on onResume
     */
    protected void rebuildList(boolean clearSelection) {

        mItemManager = new ItemManager();
        mItemManager.setItems(buildSegmentData(mContactsInterface));
        mAdapter = new ContactsAdapterData(mActivity);
        mAdapter.setItemManager(mItemManager);
        mContactsList.setAdapter(mAdapter);
        mStickyListView.setAdapter(mAdapter);
        mStickyListView.setIndexer(mAdapter);

        if (clearSelection)
            mSelected.clear();
        getSherlockActivity().invalidateOptionsMenu();
    }

    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        if (ci.getFriends() != null)
            mShowAlphabeticalHeaders = (ci.getFriends().size() > ALPHABETICAL_HEADER_BOUND ? true : false);

        List<ContactListSegmentData> listData = new ArrayList<ContactListSegmentData>();

        ContactListSegmentData segmentData = null;

        // if there are no recents, don't show them
        if (ci.getRecentContacts() != null && !ci.getRecentContacts().isEmpty()) {
            Log.d(TAG, "setting renents");
            segmentData = new ContactListSegmentData();
            segmentData.mSegmentTitle = getString(R.string.recents);
            segmentData.mContacts = ci.getRecentContacts();
            segmentData.mTextPlaceHolderMsg = getString(R.string.no_recents);
            listData.add(segmentData);
        }

        // Friends
        segmentData = new ContactListSegmentData();
        segmentData.mSegmentTitle = getString(R.string.my_friends);
        segmentData.mContacts = new ArrayList<Contact>();
        if (!mShowAlphabeticalHeaders) {
            segmentData.mContacts = ci.getFriends();
            segmentData.mTextPlaceHolderMsg = getString(R.string.no_friends);
        }
        listData.add(segmentData);

        if (mShowAlphabeticalHeaders) {
            char firstChar = 0;
            for (Contact c : ci.getFriends()) {
                if (c.mName.charAt(0) != firstChar) {
                    firstChar = c.mName.toUpperCase().charAt(0);
                    segmentData = new ContactListSegmentData();
                    segmentData.mSegmentTitle = String.valueOf(firstChar);
                    segmentData.mContacts = new ArrayList<Contact>();
                    listData.add(segmentData);
                }

                segmentData.mContacts.add(c);
            }
        }

        if (ci.getInviteList() != null && !ci.getInviteList().isEmpty()) {
            // Ask to Join
            segmentData = new ContactListSegmentData();
            segmentData.mSegmentTitle = getString(R.string.from_contacts_title);
            segmentData.mContacts = new ArrayList<Contact>(ci.getInviteList());
            listData.add(segmentData);
        }

        return listData;
    }

    private void showIntroDialog() {
        boolean seenIntroDialog = PreferenceManagerUtil.getPreferenceValue(HBPreferences.SEEN_START_CONVO_DIALOG, false);
        if (!seenIntroDialog) {
            AlertDialog.Builder builder = new Builder(getActivity());
            builder.setTitle(getString(R.string.start_convo_intro_title));
            builder.setMessage(getString(R.string.start_convo_intro_body));
            builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.SEEN_START_CONVO_DIALOG, true);
                    if (isAdded()) {
                        dialog.dismiss();
                    }

                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }

    }

    @Override
    protected void initializeView(View view) {

        if (mContactsInterface.getDeviceContactsLoadState() == LOADING_STATE.LOADING || mContactsInterface.getHbContactsLoadState() == LOADING_STATE.LOADING) {
            // startLoading();
        }
    }

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.send_to);
    }

    @Override
    protected String getScreenName() {
        return AnalyticsUtil.ScreenNames.FRIENDS_LIST;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Log.d(TAG, "action mode: " + mMultiActionMode);
        // if (mMultiActionMode != null) {
        //
        // Item item = (Item) parent.getItemAtPosition(position);
        // if (item.getContact() != null) {
        // Contact c = item.getContact();
        // ((AbsListView) parent).setItemChecked(position, mContactRemovalSet.contains(c) ? false : true);
        // }
        // return;
        // }

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
                    mRecipientAdapter.add(c.mName);
                    mGridView.smoothScrollToPosition(mRecipientAdapter.getCount() - 1);
                } else {
                    mSelected.remove(c);
                    mRecipientAdapter.remove(c.mName);
                }
            }

            if (mSelected.size() == 1) {
                showBottomBar();
            } else if (mSelected.size() == 0) {
                hideBottomBar();
            }

            // if keyboard is showing hide it
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);

        }

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.remove_friend_menu, menu);
        mActionMode = mode;

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.mi_next) {
            StartConversationFragment f = StartConversationFragment.newInstance(mSelected);
            getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                    .replace(R.id.fragment_holder, f).addToBackStack(FRAGMENT_TAG).commit();
        }
        return false;
    }

    protected void showBottomBar() {
        if (mBottomBarLayout.getVisibility() == View.GONE) {
            mBottomBarLayout.setVisibility(View.VISIBLE);
            mBottomBarLayout.setAlpha(0.0f);
            mBottomBarLayout.animate().alpha(1.0f).setListener(null);
        }
    }

    protected void hideBottomBar() {
        if (mBottomBarLayout.getVisibility() == View.VISIBLE) {
            mBottomBarLayout.animate().alpha(0.0f).setListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBottomBarLayout.setVisibility(View.GONE);

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // TODO Auto-generated method stub

                }
            });

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_next) {
            if (mSelected != null && !mSelected.isEmpty()) {
                StartConversationFragment f = StartConversationFragment.newInstance(mSelected);
                getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                        .replace(R.id.fragment_holder, f).addToBackStack(FRAGMENT_TAG).commit();
            }
        }
    }

    @Override
    public void onContactBookSelectionsDone(Set<Contact> selectedContacts) {
        mSelected.addAll(selectedContacts);
        StringBuilder sb = new StringBuilder();
        for (Contact c : selectedContacts) {
            Log.d(TAG, "selected from contact book: " + c.toString());
        }
        mRebuildList = true;

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Item item = (Item) parent.getItemAtPosition(position);

        if (item.getContact() != null) {
            getActivity().startActionMode(mMultiChoiceLisenter);
            return true;
        }

        return false;
    }

    /**
     * Handle removing friends with multi choice
     * Uses standard android menu and not actionbar sherlock
     */
    private AbsListView.MultiChoiceModeListener mMultiChoiceLisenter = new AbsListView.MultiChoiceModeListener() {

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            mMultiActionMode = null;
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) {
            android.view.MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.remove_friend_menu, menu);
            mMultiActionMode = mode;

            Log.d(TAG, "menu size: " + menu.size());

            return true;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) {
            if (item.getItemId() == R.id.mi_remove) {
                Log.d(TAG, "removing friends");
                for (Contact c : mContactRemovalSet) {
                    mFriendRemovalTransaction.removeFromFriends(c);
                }
                mFriendRemovalTransaction.commit();
                mContactRemovalSet.clear();

            }

            rebuildList(true); // rebuild the list

            mode.finish();

            return true;
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            Item item = (Item) mContactsList.getItemAtPosition(position);
            if (item.getContact() != null) {
                Log.d(TAG, "checked " + checked);
                if (checked) {
                    mContactRemovalSet.add(item.getContact());
                } else {
                    mContactRemovalSet.remove(item.getContact());
                }

                mode.setTitle(String.format(getString(R.string.remove_selected), mContactRemovalSet.size()));
            }

        }
    };

}
