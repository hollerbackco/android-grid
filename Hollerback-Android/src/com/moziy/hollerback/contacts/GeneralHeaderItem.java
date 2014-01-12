package com.moziy.hollerback.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsAdapterData.HeaderItem;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.widget.CustomTextView;

public class GeneralHeaderItem implements HeaderItem {

    private int mPosition;
    private int mNumItems = 0;
    private String mTitle;
    private LayoutInflater mInflater;
    private int mItemType;

    public GeneralHeaderItem(int position, String title, int itemType) {
        mPosition = position;
        mTitle = title;
        mItemType = itemType;
    }

    public void setInflater(LayoutInflater inflater) {
        mInflater = inflater;
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

        holder.mHeader.setText(mTitle);

        return convertView;
    }

    @Override
    public int getItemViewType() {

        return mItemType;
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
