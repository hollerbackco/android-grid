package com.moziy.hollerback.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.moziy.hollerback.HollerbackInterfaces.OnContactSelectedListener;
import com.moziy.hollerback.R;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.util.StringUtil;

public class ContactsListAdapter extends BaseExpandableListAdapter {

    // private String[] contacts;
    private LayoutInflater inflater;

    public ArrayList<String> contactitems;
    private HashMap<String, String> mSelected;
    public String invitedUsers;

    private boolean mIncludeHollerbackContact = false;
    private boolean mIncludePhone = false;

    private int mHollerBackCount;
    private int mPhoneCount;

    private OnContactSelectedListener mListener;

    public ContactsListAdapter(Context context) {
        contactitems = new ArrayList<String>();
        invitedUsers = "";
        inflater = LayoutInflater.from(context);

    }

    public void setContacts(ArrayList<String> keys, HashMap<String, String> selected, OnContactSelectedListener listener, int hollerBackCount, int phoneCount) {
        contactitems = keys;
        mSelected = selected;
        mListener = listener;
        mHollerBackCount = hollerBackCount;
        if (mHollerBackCount > 0) {
            mIncludeHollerbackContact = true;
        } else {
            mIncludeHollerbackContact = false;
        }

        mPhoneCount = phoneCount;
        if (mPhoneCount > 0) {
            mIncludePhone = true;
        } else {
            mIncludePhone = false;
        }
        this.notifyDataSetChanged();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * For food logger, only 4 when other is not there
     */
    @Override
    public int getGroupCount() {
        if (mIncludeHollerbackContact && mIncludePhone) {
            return 2;
        } else if (mIncludeHollerbackContact || mIncludePhone) {
            return 1;
        }
        return 0;
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_invitecontactresults, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.lazyTitle);
            holder.phone = (TextView) convertView.findViewById(R.id.lazySubTitle);
            holder.profileImage = (ImageView) convertView.findViewById(R.id.lazyIcon);
            holder.stateImage = (ImageView) convertView.findViewById(R.id.lazyContactType);
            holder.chkSelected = (CheckBox) convertView.findViewById(R.id.chkSelected);
            holder.wrapperContact = (ViewGroup) convertView.findViewById(R.id.wrapperContact);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // crappy logic here, you can do better ;)
        int realposition = 0;
        if (groupPosition == 0) {
            realposition = childPosition;
        } else {
            realposition = childPosition + mHollerBackCount;
        }

        final UserModel user = TempMemoryStore.users.mUserModelHash.get(contactitems.get(realposition));
        holder.name.setText(user.getName());

        if (invitedUsers.contains(user.phone)) {
            holder.name.setTextColor(Color.GRAY);
        } else {
            holder.name.setTextColor(Color.BLACK);
        }

        if (StringUtil.isEmptyOrNull(user.photourl)) {
            holder.profileImage.setImageResource(R.drawable.icon_male);
        } else {
            Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, Long.parseLong(user.photourl));
            holder.profileImage.setImageURI(photoUri);
        }

        holder.phone.setText(user.phone);

        holder.stateImage.setBackgroundResource(user.isHollerbackUser ? R.drawable.banana_img : R.drawable.phone_img);

        holder.chkSelected.setChecked(false);

        if (mSelected.containsKey(user.phone)) {
            holder.chkSelected.setChecked(true);
        }

        holder.wrapperContact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelected.containsKey(user.phone)) {
                    mSelected.remove(user.phone);
                } else {
                    mSelected.put(user.phone, user.name);
                }
                mListener.onItemClicked(childPosition);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    /**
     * This logic can get pretty complex, we are pretty much splitting the entire phone book right here
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        if (mIncludeHollerbackContact && groupPosition == 0) {
            return mHollerBackCount;
        } else {
            return mPhoneCount;
        }
    }

    public class ViewHolder {
        public TextView name;
        public TextView phone;
        public ImageView profileImage;
        public ImageView stateImage;
        public CheckBox chkSelected;
        public ViewGroup wrapperContact;
    }

    public void clear() {
        // contacts = new String[0];
        notifyDataSetChanged();
    }

    public void restore() {
        // contacts = new String[TempMemoryStore.contacts.size()];

        // names.toArray(contacts);

        notifyDataSetChanged();
    }

    public void updateInvitedUsers(String invites) {
        invitedUsers = invites;
        this.notifyDataSetChanged();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.view_contactheader, parent, false);
        TextView txtHeader = (TextView) convertView.findViewById(R.id.txtHeader);
        if (mIncludeHollerbackContact && groupPosition == 0) {
            txtHeader.setText(R.string.contact_hollerback);
        } else {
            // this is else if, assuming there are no hollerback friends
            txtHeader.setText(R.string.contact_phone);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return false;
    }
}
