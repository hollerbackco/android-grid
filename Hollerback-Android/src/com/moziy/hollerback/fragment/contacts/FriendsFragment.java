package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.ContactsInterface.LOADING_STATE;
import com.moziy.hollerback.contacts.data.ContactItem;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.contacts.data.ContactTextPlaceHolder;
import com.moziy.hollerback.contacts.data.ContactViewHolder;
import com.moziy.hollerback.contacts.data.ContactsAdapterData;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.AbsContactItem;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.AbsItemManager;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.Item;
import com.moziy.hollerback.contacts.data.GeneralHeaderItem;
import com.moziy.hollerback.contacts.data.HBContactItem;
import com.moziy.hollerback.contacts.data.PlaceHolder;
import com.moziy.hollerback.fragment.BaseFragment;
import com.moziy.hollerback.fragment.StartConversationFragment;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.SmsUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;
import com.moziy.hollerback.view.StickyHeaderListView;
import com.moziy.hollerback.widget.CustomEditText;

public class FriendsFragment extends BaseFragment implements AdapterView.OnItemClickListener, ActionMode.Callback, View.OnClickListener {
    private static final String TAG = FriendsFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    // type - serializable/enum
    public static final String NEXT_ACTION_BUNDLE_ARG_KEY = "NEXT_ACTION";

    public enum NextAction {
        START_CONVERSATION, INVITE_FRIENDS
    };

    protected ContactsInterface mContactsInterface;
    protected LayoutInflater mInflater;
    protected StickyHeaderListView mStickyListView;
    protected ListView mContactsList;
    protected ContactsAdapterData mAdapter;
    protected InternalReceiver mReceiver;
    protected CustomEditText mSearchBar;
    protected HashSet<Contact> mSelected;
    protected NextAction mAction;
    protected ItemManager mItemManager;
    protected ActionMode mActionMode;
    protected View mBottomBarLayout;
    protected Button mNextButton;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContactsInterface = ((HollerbackMainActivity) getActivity()).getContactsInterface();
        if (mReceiver == null)
            mReceiver = new InternalReceiver();
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONTACTS_UPDATED);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mReceiver != null)
            IABroadcastManager.unregisterLocalReceiver(mReceiver);
    }

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
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
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

        mInflater = LayoutInflater.from(getActivity());

        mSelected = new HashSet<Contact>();
        mItemManager = new ItemManager();
        mItemManager.setItems(buildSegmentData(mContactsInterface));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.friends_layout, container, false);
        mContactsList = (ListView) v.findViewById(R.id.lv_contacts_list);
        mContactsList.setOnItemClickListener(this);

        mStickyListView = (StickyHeaderListView) v.findViewById(R.id.stick_listview);

        mBottomBarLayout = v.findViewById(R.id.bottom_bar_layout);
        mNextButton = (Button) v.findViewById(R.id.bt_next);
        mNextButton.setOnClickListener(this);

        mSearchBar = (CustomEditText) v.findViewById(R.id.txtSearch);
        mSearchBar.setVisibility(View.GONE);
        mSearchBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mStickyListView.disableStickyHeader();
                } else {
                    mStickyListView.enableStickyHeader();
                }
                mAdapter.getFilter().filter(s.toString());

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        initializeView(v);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ContactsAdapterData(mActivity);
        mAdapter.setItemManager(mItemManager);

        mContactsList.setAdapter(mAdapter);
        mStickyListView.setIndexer(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuildList();

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
        if (mSelected != null && !mSelected.isEmpty()) {
            inflater.inflate(R.menu.send_to_contacts, menu);
        } else {
            inflater.inflate(R.menu.add_friends, menu);
        }

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
            mActivity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                    .replace(R.id.fragment_holder, fragment).addToBackStack(FRAGMENT_TAG).commit();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IABroadcastManager.unregisterLocalReceiver(mReceiver);
    }

    protected void rebuildList() {
        mItemManager = new ItemManager();
        mItemManager.setItems(buildSegmentData(mContactsInterface));
        mAdapter = new ContactsAdapterData(mActivity);
        mAdapter.setItemManager(mItemManager);
        mContactsList.setAdapter(mAdapter);
        mSelected.clear();
        getSherlockActivity().invalidateOptionsMenu();
    }

    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        List<ContactListSegmentData> listData = new ArrayList<ContactListSegmentData>();

        // ContactListSegmentData segmentData = null;
        // char firstChar = 0;
        // for (Contact c : ci.getRecentContacts()) {
        // if (c.mName.charAt(0) != firstChar) {
        // firstChar = c.mName.toUpperCase().charAt(0);
        // segmentData = new ContactListSegmentData();
        // segmentData.mSegmentTitle = String.valueOf(firstChar);
        // segmentData.mContacts = new ArrayList<Contact>();
        // listData.add(segmentData);
        // }
        //
        // segmentData.mContacts.add(c);
        // }
        // // build recents
        ContactListSegmentData segmentData = new ContactListSegmentData();
        segmentData.mSegmentTitle = getString(R.string.recents);
        segmentData.mContacts = ci.getRecentContacts();
        segmentData.mTextPlaceHolderMsg = getString(R.string.no_recents);
        listData.add(segmentData);

        segmentData = new ContactListSegmentData();
        segmentData.mSegmentTitle = getString(R.string.my_friends);
        segmentData.mContacts = ci.getFriends();
        segmentData.mTextPlaceHolderMsg = getString(R.string.no_friends);
        listData.add(segmentData);

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

    /**
     * Listens to CONTACTS_UPDATED
     * @author sajjad
     *
     */
    private class InternalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // if we were able to load both contacts list, then just set the adapter
            if (mContactsInterface.getDeviceContactsLoadState() == LOADING_STATE.DONE) {
                // if getting the hb contacts failed or its done already then just display the contacts - displaying something is better than nothing
                if (mContactsInterface.getHbContactsLoadState() == LOADING_STATE.DONE || mContactsInterface.getHbContactsLoadState() == LOADING_STATE.FAILED) {

                    if (mAdapter != null && isAdded()) {

                        mAdapter = new ContactsAdapterData(mActivity);
                        mItemManager = new ItemManager();
                        mItemManager.setItems(buildSegmentData(mContactsInterface));
                        mAdapter.setItemManager(mItemManager);
                        mContactsList.setAdapter(mAdapter);
                    }
                }

            }

        }
    }

    public class ItemManager extends AbsItemManager {

        private int mItemType;
        private List<Item> mItems;
        int mHbHeaderPosition = 0;
        int mContactsHeaderPosition = 0;

        @Override
        public int getItemTypeCount() {
            return mItemType;
        }

        public void setItems(List<ContactListSegmentData> segmentData) {

            mItemType = 0; // clear the item types

            mItems = new ArrayList<ContactsAdapterData.Item>();

            for (ContactListSegmentData data : segmentData) {
                GeneralHeaderItem headerItem = null;
                if (data.mSegmentTitle != null) {

                    headerItem = new GeneralHeaderItem(mItems.size(), mItems.size(), data.mSegmentTitle, mItemType);
                    headerItem.setInflater(LayoutInflater.from(mActivity));
                    ++mItemType;
                    mItems.add(headerItem);
                    headerItem.setNumberOfItems((data.mContacts == null ? 0 : data.mContacts.size()));

                }

                if (data.mContacts != null && !data.mContacts.isEmpty()) {

                    for (Contact c : data.mContacts) {

                        ContactItem ci;
                        if (c.mUsername != null && !c.mUsername.isEmpty()) {
                            // create an hb contact
                            ci = new HBContactItem(c, (headerItem != null ? headerItem.getHeaderPosition() : -1), mItemType);

                        } else {
                            ci = new ContactItem(c, (headerItem != null ? headerItem.getHeaderPosition() : -1), mItemType);
                        }
                        ci.setInflater(LayoutInflater.from(mActivity));
                        mItems.add(ci);

                    }

                } else if (data.mContacts == null) {
                    // create a place holder item
                    PlaceHolder item = new PlaceHolder(mItemType, headerItem.getHeaderPosition());
                    mItems.add(item);
                } else {
                    if (data.mTextPlaceHolderMsg != null) {
                        ContactTextPlaceHolder ph = new ContactTextPlaceHolder(mItemType, headerItem.getHeaderPosition(), data.mTextPlaceHolderMsg);
                        mItems.add(ph);
                    }
                }

                ++mItemType;

            }

        }

        @Override
        public List<Item> getItems() {
            return mItems;
        }

    }

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.send_to);
    }

    @Override
    protected String getFragmentName() {
        return TAG;
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

            if (mSelected.size() == 1) {
                showBottomBar();
            } else if (mSelected.size() == 0) {
                hideBottomBar();
            }

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
        inflater.inflate(R.menu.send_to_contacts, menu);
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

    public Set<Contact> getSelectedContacts() {
        return mSelected;
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

}
