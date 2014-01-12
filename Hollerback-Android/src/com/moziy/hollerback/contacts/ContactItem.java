package com.moziy.hollerback.contacts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsAdapterData.AbsContactItem;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.widget.CustomTextView;

public class ContactItem extends AbsContactItem {

    protected Contact mContact;
    protected int mHeaderPosition;
    protected int mItemType;

    public ContactItem(Contact c, int headerPosition, int itemType) {
        mContact = c;
        mHeaderPosition = headerPosition;
        mItemType = itemType;
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
        return mItemType;
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
