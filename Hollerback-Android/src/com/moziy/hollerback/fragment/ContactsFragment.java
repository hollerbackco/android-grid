package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.contacts.ContactsInterface;
import com.moziy.hollerback.util.contacts.ContactsInterface.LOADING_STATE;
import com.moziy.hollerback.widget.CustomTextView;

public class ContactsFragment extends BaseFragment {
    private static final String TAG = ContactsFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;

    private ContactsInterface mContactsInterface;
    private LayoutInflater mInflater;
    private ListView mContactsList;
    private ContactsAdapter mAdapter;
    private InternalReceiver mReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public static ContactsFragment newInstance() {
        ContactsFragment f = new ContactsFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.start_conversation));
        mContactsInterface = ((HollerbackMainActivity) getActivity()).getContactsInterface();
        mInflater = LayoutInflater.from(getActivity());

        mReceiver = new InternalReceiver();
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONTACTS_UPDATED);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contacts_layout, container, false);

        mContactsList = (ListView) v.findViewById(R.id.lv_contacts_list);
        mContactsList.setOnItemClickListener(mOnContactClick);
        mAdapter = new ContactsAdapter(mContactsInterface.getHollerbackContacts(), mContactsInterface.getDeviceContacts());
        mContactsList.setAdapter(mAdapter);

        initializeView(v);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IABroadcastManager.unregisterLocalReceiver(mReceiver);
    }

    private AdapterView.OnItemClickListener mOnContactClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Item item = (Item) parent.getItemAtPosition(position);
            if (item.getContact() != null) {
                Contact c = item.getContact();
                StartConversationFragment f = StartConversationFragment.newInstance(new String[] {
                    c.mPhone
                }, getString(R.string.start_conversation));

                getFragmentManager().beginTransaction().replace(R.id.fragment_holder, f).addToBackStack(FRAGMENT_TAG).commit();
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
                        mAdapter = new ContactsAdapter(mContactsInterface.getHollerbackContacts(), mContactsInterface.getDeviceContacts());
                        mContactsList.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }

                }

            }

        }
    }

    private class ContactsAdapter extends BaseAdapter {

        private ItemManager mItemManager;

        public ContactsAdapter(List<Contact> hbContacts, List<Contact> others) {
            mItemManager = new ItemManager(hbContacts, others);
        }

        @Override
        public int getCount() {

            return mItemManager.mItems.size();
        }

        @Override
        public Object getItem(int position) {

            return mItemManager.mItems.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return mItemManager.mItems.get(position).getView(position, convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            return mItemManager.mItems.get(position).getItemViewType();
        }

        @Override
        public int getViewTypeCount() {
            return mItemManager.itemTypeCount;
        }

    }

    private class ItemManager {

        public static final int HB_HEADER = 0;
        public static final int HB_CONTACT = 1;
        public static final int CONTACT_HEADER = 2;
        public static final int CONTACT = 3;

        List<Item> mItems;
        final int itemTypeCount = 4;

        public ItemManager(List<Contact> hbFriends, List<Contact> contacts) {
            mItems = new ArrayList<ContactsFragment.Item>();
            if (hbFriends != null && !hbFriends.isEmpty()) {

                mItems.add(new HBHeaderItem());
                for (Contact c : hbFriends) {
                    mItems.add(new HBFriendItem(c));
                }

                // we officially have two item types
                // itemTypeCount += 2;
            }

            if (contacts != null && !contacts.isEmpty()) {
                mItems.add(new ContactHeaderItem());
                for (Contact c : contacts) {
                    mItems.add(new ContactItem(c));
                }

                // itemTypeCount += 2;
            }
        }
    }

    private class HBFriendItem implements Item {

        private Contact mContact;

        public HBFriendItem(Contact c) {
            mContact = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.contact_list_item, parent, false);
                holder.name = (CustomTextView) convertView.findViewById(R.id.tv_contact_name);
                holder.username = (CustomTextView) convertView.findViewById(R.id.tv_contact_username);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(mContact.mName);
            holder.username.setText(mContact.mUsername);

            return convertView;
        }

        @Override
        public int getItemViewType() {
            return ItemManager.HB_CONTACT;
        }

        private class ViewHolder {
            public CustomTextView name;
            public CustomTextView username;

        }

        @Override
        public Contact getContact() {
            return mContact;
        }

    }

    private class ContactItem implements Item {

        private Contact mContact;

        public ContactItem(Contact c) {
            mContact = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.contact_list_item, parent, false);
                holder.name = (CustomTextView) convertView.findViewById(R.id.tv_contact_name);
                holder.username = (CustomTextView) convertView.findViewById(R.id.tv_contact_username);
                holder.icon = (ImageView) convertView.findViewById(R.id.iv_contact_type);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(mContact.mName);
            holder.username.setVisibility(View.GONE);
            holder.icon.setVisibility(View.INVISIBLE);

            return convertView;
        }

        @Override
        public int getItemViewType() {
            return ItemManager.CONTACT;
        }

        private class ViewHolder {
            public CustomTextView name;
            public CustomTextView username;
            public ImageView icon;

        }

        @Override
        public Contact getContact() {
            return mContact;
        }

    }

    private class HBHeaderItem implements Item {

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

    }

    private class ContactHeaderItem implements Item {

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

    }

    private interface Item {

        public View getView(int position, View convertView, ViewGroup parent);

        public int getItemViewType();

        public Contact getContact();
    }

}
