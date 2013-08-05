package com.moziy.hollerback.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.moziy.hollerback.R;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.UserModel;

public class ContactsListAdapter extends BaseAdapter implements
		StickyListHeadersAdapter {

	// private String[] contacts;
	private LayoutInflater inflater;
	private int[] sectionId;
	public ArrayList<String> contactitems;
	public ArrayList<Integer> indexes;
	public String invitedUsers;

	public ContactsListAdapter(Context context) {
		contactitems = new ArrayList<String>();
		invitedUsers = "";
		inflater = LayoutInflater.from(context);
		sectionId = new int[2];
		sectionId[0] = 0;
		sectionId[1] = 0;
		// sectionId[2] = 0;

	}

	public void setContacts(ArrayList<String> keys, ArrayList<Integer> index) {
		contactitems = keys;
		indexes = index;
		if (indexes != null) {
			sectionId[0] = indexes.get(1);
			sectionId[1] = indexes.get(2);
			// sectionId[2] = indexes.get(2);
		}
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return contactitems.size();
	}

	@Override
	public Object getItem(int position) {
		return contactitems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.contact_list_item, parent,
					false);
			holder.text = (TextView) convertView
					.findViewById(R.id.tv_contact_name);
			holder.mContactStateImage = (ImageView) convertView
					.findViewById(R.id.iv_contact_type);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		UserModel user = TempMemoryStore.users.mUserModelHash.get(contactitems
				.get(position));
		holder.text.setText(user.getName());

		if (invitedUsers.contains(user.phone)) {
			holder.text.setTextColor(Color.GRAY);
		} else {
			holder.text.setTextColor(Color.BLACK);
		}

		holder.mContactStateImage
				.setBackgroundResource(user.isHollerbackUser ? R.drawable.banana_img
						: R.drawable.phone_img);

		return convertView;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = inflater.inflate(R.layout.header, parent, false);
			holder.text1 = (TextView) convertView.findViewById(R.id.text1);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}
		// set header text as first char in name

		if (getHeaderId(position) == sectionId[0]) {
			holder.text1.setText("Hollerback Friends");
		} else if (getHeaderId(position) == sectionId[1]) {
			holder.text1.setText("Address Book Contacts");
		}
		return convertView;
	}

	// remember that these have to be static, postion=1 should walys return the
	// same Id that is.
	@Override
	public long getHeaderId(int position) {
		// return the first character of the country as ID because this is what
		// headers are based upon
		if (position >= sectionId[sectionId.length - 1]) {
			return sectionId[sectionId.length - 1];
		}
		return sectionId[0];

	}

	class HeaderViewHolder {
		TextView text1;
	}

	public class ViewHolder {
		public TextView text;
		public ImageView mContactStateImage;
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
}
