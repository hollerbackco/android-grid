package com.moziy.hollerback.adapter;

import java.util.ArrayList;

import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.MessageModel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationListAdapter extends BaseAdapter {

	ArrayList<ConversationModel> mConversations;

	LayoutInflater inflater;

	public ConversationListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		mConversations = new ArrayList<ConversationModel>();
	}

	public void setConversations(ArrayList<ConversationModel> conversations) {
		mConversations = conversations;
		this.notifyDataSetChanged();
	}

	public ArrayList<ConversationModel> getConversations() {
		return mConversations;
	}

	public void clearConversations() {
		if (mConversations != null) {
			mConversations = new ArrayList<ConversationModel>();
		}
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mConversations.size();
	}

	@Override
	public ConversationModel getItem(int position) {
		// TODO Auto-generated method stub
		return mConversations.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.message_list_item, null);
			viewHolder.conversationName = (TextView) convertView
					.findViewById(R.id.tv_convoname);
			viewHolder.newMessagesIndicator = (ImageView) convertView
					.findViewById(R.id.iv_green_dot);
			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (mConversations.get(position).getConversationUnreadCount() > 0) {
			viewHolder.newMessagesIndicator.setVisibility(View.VISIBLE);
		} else {
			viewHolder.newMessagesIndicator.setVisibility(View.INVISIBLE);
		}

		viewHolder.conversationName.setText(mConversations.get(position)
				.getConversationName());

		LogUtil.i("Conv " + mConversations.get(position).getConversationName());

		return convertView;
	}

	static class ViewHolder {
		TextView conversationName;
		ImageView newMessagesIndicator;
	}

}
