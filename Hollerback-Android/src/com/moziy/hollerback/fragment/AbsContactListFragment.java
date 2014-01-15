package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

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
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.view.StickyHeaderListView;
import com.moziy.hollerback.widget.CustomEditText;

public abstract class AbsContactListFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    protected ContactsInterface mContactsInterface;
    protected LayoutInflater mInflater;
    protected StickyHeaderListView mStickyListView;
    protected ListView mContactsList;
    protected ContactsAdapterData mAdapter;
    protected InternalReceiver mReceiver;
    protected CustomEditText mSearchBar;
    protected HashSet<Contact> mSelected;
    protected ItemManager mItemManager;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

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
    public void onDestroy() {
        super.onDestroy();
        IABroadcastManager.unregisterLocalReceiver(mReceiver);
    }

    /**
     * This method will rebuild the list on onResume
     */

    protected abstract List<ContactListSegmentData> buildSegmentData(ContactsInterface ci);

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

    /**
     * The specific contacts data adapter
     * @author sajjad
     *
     */
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

            // if keyboard is showing hide it
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);

        }

    }

    public Set<Contact> getSelectedContacts() {
        return mSelected;
    }

}
