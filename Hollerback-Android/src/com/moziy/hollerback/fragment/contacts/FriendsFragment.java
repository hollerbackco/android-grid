package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
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
import com.moziy.hollerback.util.SmsUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class FriendsFragment extends AbsContactListFragment implements ActionMode.Callback, View.OnClickListener, OnContactBookSelectionsDone {
    private static final String TAG = FriendsFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    // type - serializable/enum
    public static final String NEXT_ACTION_BUNDLE_ARG_KEY = "NEXT_ACTION";

    public enum NextAction {
        START_CONVERSATION, INVITE_FRIENDS
    };

    protected NextAction mAction;
    protected ActionMode mActionMode;
    protected View mBottomBarLayout;
    protected Button mNextButton;

    private boolean mRebuildList;

    private ContactListSegmentData mRecentSegmentData;
    private ContactListSegmentData mFriendsSegmentData;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        mBottomBarLayout = v.findViewById(R.id.bottom_bar_layout);
        mNextButton = (Button) v.findViewById(R.id.bt_next);
        mNextButton.setOnClickListener(this);

        return v;
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
        inflater.inflate(R.menu.add_friends, menu);

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
        if (clearSelection)
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
        mRecentSegmentData = new ContactListSegmentData();
        mRecentSegmentData.mSegmentTitle = getString(R.string.recents);
        mRecentSegmentData.mContacts = ci.getRecentContacts();
        mRecentSegmentData.mTextPlaceHolderMsg = getString(R.string.no_recents);
        listData.add(mRecentSegmentData);

        mFriendsSegmentData = new ContactListSegmentData();
        mFriendsSegmentData.mSegmentTitle = getString(R.string.my_friends);
        mFriendsSegmentData.mContacts = ci.getFriends();
        mFriendsSegmentData.mTextPlaceHolderMsg = getString(R.string.no_friends);
        listData.add(mFriendsSegmentData);

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

}
