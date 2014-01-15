package com.moziy.hollerback.contacts.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.data.ContactsAdapterData.Item;
import com.moziy.hollerback.model.Contact;

public class PlaceHolder implements Item {

    private final int mItemType;
    private final int mHeaderPos;
    private final LayoutInflater mInflater;

    public PlaceHolder(int type, int headerPosition) {
        mItemType = type;
        mHeaderPos = headerPosition;
        mInflater = LayoutInflater.from(HollerbackApplication.getInstance());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return mInflater.inflate(R.layout.contact_place_holder_item, parent, false);
    }

    @Override
    public int getItemViewType() {
        return mItemType;
    }

    @Override
    public int getHeaderPosition() {
        // TODO Auto-generated method stub
        return mHeaderPos;
    }

    @Override
    public Contact getContact() {
        return null;
    }

}
