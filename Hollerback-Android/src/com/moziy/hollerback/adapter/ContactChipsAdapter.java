package com.moziy.hollerback.adapter;

import java.util.ArrayList;

import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.UserModel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ContactChipsAdapter extends BaseAdapter {

	ArrayList<UserModel> users;
	LayoutInflater inflater;

	public void addUserModel(UserModel user) {
		users.add(user);
	}

	public ArrayList<UserModel> getUsers() {
		return users;
	}

	public void clearUsers() {
		users.clear();
	}

	public ContactChipsAdapter(Context context) {
		users = new ArrayList<UserModel>();
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return users.size();
	}

	@Override
	public UserModel getItem(int position) {
		// TODO Auto-generated method stub
		return users.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.contact_chips, parent,
					false);
			holder.nameTextView = (TextView) convertView
					.findViewById(R.id.tv_chips_name);
			holder.parentView = (RelativeLayout) convertView
					.findViewById(R.id.rl_chips_layout);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.nameTextView.setText(getItem(position).name);
		holder.parentView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LogUtil.i("Clicking on item");
				users.remove(position);
				ContactChipsAdapter.this.notifyDataSetChanged();
			}
		});

		return convertView;
	}

	private static class ViewHolder {
		TextView nameTextView;
		RelativeLayout parentView;
	}

}
