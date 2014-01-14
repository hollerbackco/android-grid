package com.moziy.hollerback.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsAdapterData.Item;
import com.moziy.hollerback.model.Contact;

public class ContactTextPlaceHolder implements Item {

    private final int mItemType;
    private final int mHeaderPos;
    private final LayoutInflater mInflater;
    private String mText;

    public ContactTextPlaceHolder(int type, int headerPosition, String text) {
        mItemType = type;
        mHeaderPos = headerPosition;
        mInflater = LayoutInflater.from(HollerbackApplication.getInstance());
        mText = text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.contact_text_placeholder, parent, false);
        ((TextView) v.findViewById(R.id.tv_title)).setText(mText);

        return v;
    }

    @Override
    public int getItemViewType() {
        // TODO Auto-generated method stub
        return mItemType;
    }

    @Override
    public int getHeaderPosition() {
        // TODO Auto-generated method stub
        return mHeaderPos;
    }

    @Override
    public Contact getContact() {
        // TODO Auto-generated method stub
        return null;
    }

}
