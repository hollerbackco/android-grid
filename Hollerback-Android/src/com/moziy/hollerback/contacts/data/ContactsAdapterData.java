package com.moziy.hollerback.contacts.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.Item;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.view.StickyHeaderListView.HeaderIndexer;

public class ContactsAdapterData extends ArrayAdapter<Item> implements HeaderIndexer {

    private AbsItemManager mItemManager;

    public ContactsAdapterData(Context context) {
        super(context, R.layout.contact_list_item, R.id.tv_contact_name);
        // mItemManager = new ItemManager(hbContacts, others);
        // addAll(mItemManager.mItems);
    }

    public void setItemManager(AbsItemManager manager) {
        mItemManager = manager;
        clear();
        addAll(mItemManager.getItems());
    }

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
        return mItemManager.getItemTypeCount();
    }

    @Override
    public int getHeaderPositionFromItemPosition(int position) {

        return getItem(position).getHeaderPosition();
    }

    @Override
    public int getHeaderItemsNumber(int headerPosition) {

        return ((HeaderItem) getItem(headerPosition)).getNumberOfItems();
    }

    public interface Item {

        public View getView(int position, View convertView, ViewGroup parent);

        public int getItemViewType();

        public int getHeaderPosition();

        public Contact getContact();

    }

    public static abstract class AbsContactItem implements Item {
        protected boolean mIsSelected = false;
        protected LayoutInflater mInflater;

        public void setInflater(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public void setSelected(boolean selected) {
            mIsSelected = selected;
        }

        public boolean getSelected() {
            return mIsSelected;
        }
    }

    /**
     * An item representing a header in the list
     * @author sajjad
     *
     */
    public interface HeaderItem extends Item {
        public int getNumberOfItems();

        public void setNumberOfItems(int num);
    }

    public static abstract class AbsItemManager {

        public abstract int getItemTypeCount();

        public abstract List<Item> getItems();

    }

}
