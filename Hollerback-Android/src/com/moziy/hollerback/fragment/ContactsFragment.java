package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

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
import com.moziy.hollerback.fragment.contacts.ContactBookFragment;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.SmsUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;
import com.moziy.hollerback.view.StickyHeaderListView;
import com.moziy.hollerback.view.StickyHeaderListView.HeaderIndexer;
import com.moziy.hollerback.widget.CustomEditText;
import com.moziy.hollerback.widget.CustomTextView;

public class ContactsFragment extends BaseFragment {
    private static final String TAG = ContactsFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    // type - serializable/enum
    public static final String NEXT_ACTION_BUNDLE_ARG_KEY = "NEXT_ACTION";

    public enum NextAction {
        START_CONVERSATION, INVITE_FRIENDS
    };

    private ContactsInterface mContactsInterface;
    private LayoutInflater mInflater;
    private StickyHeaderListView mStickyListView;
    private ListView mContactsList;
    private ContactsAdapter mAdapter;
    private InternalReceiver mReceiver;
    private CustomEditText mSearchBar;
    private HashSet<Contact> mSelected;
    private NextAction mAction;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public static ContactsFragment newInstance() {
        return newInstance(NextAction.START_CONVERSATION);
    }

    public static ContactsFragment newInstance(NextAction action) {
        ContactsFragment f = new ContactsFragment();
        Bundle arg = new Bundle();
        arg.putSerializable(NEXT_ACTION_BUNDLE_ARG_KEY, action);
        f.setArguments(arg);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mIsChildFragment = true;
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mAction = (NextAction) getArguments().getSerializable(NEXT_ACTION_BUNDLE_ARG_KEY);

        // switch (mAction) {
        // case START_CONVERSATION:
        // getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.start_conversation));
        // showIntroDialog();
        // break;
        // case INVITE_FRIENDS:
        // getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.invite_friends_title));
        // break;
        // }

        mContactsInterface = ((HollerbackMainActivity) getActivity()).getContactsInterface();
        mInflater = LayoutInflater.from(getActivity());

        mReceiver = new InternalReceiver();
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONTACTS_UPDATED);

        mSelected = new HashSet<Contact>();
        Log.d(TAG, "oncreate");
        mAdapter = new ContactsAdapter(mContactsInterface.getHollerbackContacts(), mContactsInterface.getDeviceContacts());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contacts_layout, container, false);
        Log.d(TAG, "onCreateView");
        mContactsList = (ListView) v.findViewById(R.id.lv_contacts_list);
        mContactsList.setOnItemClickListener(mOnContactClick);
        mContactsList.setAdapter(mAdapter);

        mStickyListView = (StickyHeaderListView) v.findViewById(R.id.stick_listview);
        mStickyListView.setIndexer(mAdapter);

        mSearchBar = (CustomEditText) v.findViewById(R.id.txtSearch);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mSelected != null && !mSelected.isEmpty()) {
            Log.d(TAG, "inflating menu");
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

    private AdapterView.OnItemClickListener mOnContactClick = new AdapterView.OnItemClickListener() {

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

                if (mSelected.size() == 1 || mSelected.size() == 0) {
                    getSherlockActivity().invalidateOptionsMenu();
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
    };

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
            if (mContactsList != null) { // the view has been initialized

                // if we were able to load both contacts list, then just set the adapter
                if (mContactsInterface.getDeviceContactsLoadState() == LOADING_STATE.DONE) {
                    // if getting the hb contacts failed or its done already then just display the contacts - displaying something is better than nothing
                    if (mContactsInterface.getHbContactsLoadState() == LOADING_STATE.DONE || mContactsInterface.getHbContactsLoadState() == LOADING_STATE.FAILED) {
                        // stopLoading();
                        mAdapter.setContacts(mContactsInterface.getHollerbackContacts(), mContactsInterface.getDeviceContacts());
                        mAdapter.notifyDataSetChanged();
                    }

                }

            }

        }
    }

    private class ContactsAdapter extends ArrayAdapter<Item> implements HeaderIndexer {

        private ItemManager mItemManager;

        public ContactsAdapter(List<Contact> hbContacts, List<Contact> others) {
            super(mActivity, R.layout.contact_list_item, R.id.tv_contact_name);
            mItemManager = new ItemManager(hbContacts, others);
            addAll(mItemManager.mItems);
        }

        public void setContacts(List<Contact> hbContacts, List<Contact> others) {
            clear();

            mItemManager = new ItemManager(hbContacts, others);
            addAll(mItemManager.mItems);
        }

        // @Override
        // public Item getItem(int position) {
        // return mItemManager.mItems.get(position);
        // }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(position, convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getItemViewType();
        }

        @Override
        public int getViewTypeCount() {
            return mItemManager.itemTypeCount;
        }

        @Override
        public int getHeaderPositionFromItemPosition(int position) {

            return getItem(position).getHeaderPosition();
        }

        @Override
        public int getHeaderItemsNumber(int headerPosition) {

            return ((HeaderItem) getItem(headerPosition)).getNumberOfItems();
        }

    }

    private class ItemManager {

        public static final int HB_HEADER = 0;
        public static final int HB_CONTACT = 1;
        public static final int CONTACT_HEADER = 2;
        public static final int CONTACT = 3;

        List<Item> mItems;
        final int itemTypeCount = 4;
        int mHbHeaderPosition = 0;
        int mContactsHeaderPosition = 0;

        public ItemManager(List<Contact> hbFriends, List<Contact> contacts) {

            mItems = new ArrayList<ContactsFragment.Item>();
            if (hbFriends != null && !hbFriends.isEmpty()) {

                mHbHeaderPosition = mItems.size();
                HBHeaderItem hbHeader = new HBHeaderItem(mHbHeaderPosition);

                hbHeader.setNumberOfItems(hbFriends.size());
                mItems.add(hbHeader);
                Log.d(TAG, "hb header pos: " + mHbHeaderPosition + " size: " + hbFriends.size());
                for (Contact c : hbFriends) {
                    mItems.add(new HBFriendItem(c, mHbHeaderPosition));

                }

            }

            if (contacts != null && !contacts.isEmpty()) {

                mContactsHeaderPosition = mItems.size();
                ContactHeaderItem contactHeader = new ContactHeaderItem(mContactsHeaderPosition);
                contactHeader.setNumberOfItems(contacts.size());
                mItems.add(contactHeader);
                Log.d(TAG, "cn header pos: " + mContactsHeaderPosition + " size: " + contacts.size());
                for (Contact c : contacts) {
                    mItems.add(new ContactItem(c, mContactsHeaderPosition));
                }

            }
        }

    }

    private static class ContactViewHolder {
        public CustomTextView name;
        public CustomTextView username;
        public ImageView icon;
        public ImageView checkbox;
    }

    private class HBFriendItem extends AbsContactItem {

        private Contact mContact;
        private int mHeaderPosition;

        public HBFriendItem(Contact c, int headerPosition) {
            mContact = c;
            mHeaderPosition = headerPosition;
            mIsSelected = false;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactViewHolder holder;
            if (convertView == null) {
                holder = new ContactViewHolder();

                convertView = mInflater.inflate(R.layout.contact_list_item, parent, false);
                holder.name = (CustomTextView) convertView.findViewById(R.id.tv_contact_name);
                holder.username = (CustomTextView) convertView.findViewById(R.id.tv_contact_username);
                holder.checkbox = (ImageView) convertView.findViewById(R.id.iv_contact_selected);
                convertView.setTag(holder);
            } else {
                holder = (ContactViewHolder) convertView.getTag();
            }

            holder.name.setText(mContact.mName);
            holder.username.setText(mContact.mUsername);
            if (mIsSelected) {
                holder.checkbox.setVisibility(View.VISIBLE);
            } else {
                holder.checkbox.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        @Override
        public int getItemViewType() {
            return ItemManager.HB_CONTACT;
        }

        @Override
        public Contact getContact() {
            return mContact;
        }

        @Override
        public int getHeaderPosition() {
            return this.mHeaderPosition;
        }

        @Override
        public String toString() {
            return mContact.mName.toLowerCase();
        }

    }

    private class ContactItem extends AbsContactItem {

        private Contact mContact;
        private int mHeaderPosition;

        public ContactItem(Contact c, int headerPosition) {
            mContact = c;
            mHeaderPosition = headerPosition;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactViewHolder holder;
            if (convertView == null) {
                holder = new ContactViewHolder();

                convertView = mInflater.inflate(R.layout.contact_list_item, parent, false);
                holder.name = (CustomTextView) convertView.findViewById(R.id.tv_contact_name);
                holder.username = (CustomTextView) convertView.findViewById(R.id.tv_contact_username);
                holder.icon = (ImageView) convertView.findViewById(R.id.iv_contact_type);
                holder.checkbox = (ImageView) convertView.findViewById(R.id.iv_contact_selected);
                convertView.setTag(holder);
            } else {
                holder = (ContactViewHolder) convertView.getTag();
            }

            holder.name.setText(mContact.mName);
            holder.username.setVisibility(View.GONE);
            holder.icon.setVisibility(View.INVISIBLE);

            if (mIsSelected) {
                holder.checkbox.setVisibility(View.VISIBLE);
            } else {
                holder.checkbox.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        @Override
        public int getItemViewType() {
            return ItemManager.CONTACT;
        }

        @Override
        public String toString() {
            return mContact.mName.toLowerCase();
        }

        @Override
        public Contact getContact() {
            return mContact;
        }

        @Override
        public int getHeaderPosition() {
            return this.mHeaderPosition;
        }

    }

    private class HBHeaderItem implements HeaderItem {

        private int mPosition;
        private int mNumItems = 0;

        public HBHeaderItem(int position) {
            mPosition = position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.contact_header_item, parent, false);

                holder = new ViewHolder();
                holder.mHeader = (CustomTextView) convertView.findViewById(R.id.tv_header);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mHeader.setText(getString(R.string.send_to_hb_friend));

            return convertView;
        }

        @Override
        public int getItemViewType() {

            return ItemManager.HB_HEADER;
        }

        private class ViewHolder {
            public CustomTextView mHeader;
        }

        @Override
        public Contact getContact() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getHeaderPosition() {

            return mPosition;
        }

        @Override
        public int getNumberOfItems() {

            return mNumItems;
        }

        @Override
        public void setNumberOfItems(int num) {
            mNumItems = num;

        }

    }

    private class ContactHeaderItem implements HeaderItem {

        private int mPosition;
        private int mNumItems;

        public ContactHeaderItem(int position) {
            mPosition = position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.contact_header_item, parent, false);

                holder = new ViewHolder();
                holder.mHeader = (CustomTextView) convertView.findViewById(R.id.tv_header);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mHeader.setText(getString(R.string.send_to_contact));

            return convertView;
        }

        private class ViewHolder {
            public CustomTextView mHeader;
        }

        @Override
        public int getItemViewType() {
            // TODO Auto-generated method stub
            return ItemManager.CONTACT_HEADER;
        }

        @Override
        public Contact getContact() {
            return null;
        }

        @Override
        public int getHeaderPosition() {
            return mPosition;
        }

        @Override
        public int getNumberOfItems() {
            return mNumItems;
        }

        @Override
        public void setNumberOfItems(int num) {
            mNumItems = num;
        }

    }

    private interface Item {

        public View getView(int position, View convertView, ViewGroup parent);

        public int getItemViewType();

        public int getHeaderPosition();

        public Contact getContact();

    }

    private abstract class AbsContactItem implements Item {
        protected boolean mIsSelected = false;

        public void setSelected(boolean selected) {
            mIsSelected = selected;
        }

        public boolean getSelected() {
            return mIsSelected;
        }
    }

    private interface HeaderItem extends Item {
        public int getNumberOfItems();

        public void setNumberOfItems(int num);
    }

    @Override
    protected String getFragmentName() {
        return TAG;
    }

}
