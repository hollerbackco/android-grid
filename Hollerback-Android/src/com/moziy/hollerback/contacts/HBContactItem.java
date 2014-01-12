package com.moziy.hollerback.contacts;

import android.view.View;
import android.view.ViewGroup;

import com.moziy.hollerback.model.Contact;

public class HBContactItem extends ContactItem {

    public HBContactItem(Contact c, int headerPosition, int itemType) {
        super(c, headerPosition, itemType);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);

        ContactViewHolder holder = (ContactViewHolder) convertView.getTag();
        holder.username.setVisibility(View.VISIBLE);
        holder.username.setText(mContact.mUsername);
        holder.icon.setVisibility(View.VISIBLE);

        return convertView;
    }

}
