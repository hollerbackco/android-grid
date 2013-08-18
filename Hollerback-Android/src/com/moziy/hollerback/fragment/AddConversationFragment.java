package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.kpbird.chipsedittextlibrary.ChipsItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackCameraActivity;
import com.moziy.hollerback.adapter.ContactsListAdapter;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.helper.ContactSpannableHelper;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.util.CollectionOpUtils;
import com.moziy.hollerback.util.HollerbackConstants;
import com.moziy.hollerbacky.connection.HBRequestManager;


import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class AddConversationFragment extends BaseFragment {

	StickyListHeadersListView stickyList;
	ContactsListAdapter mAdapter;

	EditText mEditText;

	ArrayList<ChipsItem> mContactChips;

	Button mCreateConversationBtn;

	private int mInvitesCount;

	HashMap<String, String> nameKeys;

	ContactSpannableHelper mContactSpannableHelper;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);

    	switch(item.getItemId())
    	{
	    	case R.id.action_settings:
	    		
	    		break;
	    	case android.R.id.home:
	    		this.getFragmentManager().popBackStack();
	    		break;
	    }
    	
    	return super.onOptionsItemSelected(item);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.add_convo_fragment, null);

		stickyList = (StickyListHeadersListView) fragmentView
				.findViewById(R.id.list);
		// stickyList.addHeaderView(inflater.inflate(R.layout.list_header,
		// null));
		stickyList.addFooterView(inflater.inflate(R.layout.list_footer, null));
		initializeView(fragmentView);
		//mAdapter.setContacts(TempMemoryStore.users.sortedKeys, null);
		HBRequestManager.getContacts(TempMemoryStore.users.array);
		stickyList.setOnItemClickListener(mContactClickListener);

		// CollectionOpUtils.setChipItems(TempMemoryStore.users.array);

		// mEditText.setAdapter(mContactChipsAdapter);

		mContactSpannableHelper = new ContactSpannableHelper();

		return fragmentView;
	}

	OnItemClickListener mContactClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			UserModel user = TempMemoryStore.users.mUserModelHash
					.get(mAdapter.contactitems.get(position));

			if (mEditText.getText().toString().contains(user.phone)) {
				return;
			}

			int start = mEditText.getText().toString()
					.lastIndexOf(HollerbackConstants.PHONE_SUF);

			LogUtil.i("last index of " + start);
			if (start > 0 && mEditText.getText().length() > start
					|| start == -1) {
				mEditText.getEditableText().delete(start + 1,
						mEditText.getText().length());
			}

			LogUtil.i("Cursor: " + mEditText.getSelectionEnd() + " length: "
					+ mEditText.getText().length());
			//
			// if(mEditText.getSelectionEnd()!=mEditText.getText().length()){
			//
			// mEditText.getEditableText().insert(where, text);
			// } else {
			//
			// }

			mContactSpannableHelper.addContactName(mEditText, getActivity(),
					user.name, user.phone);

			mAdapter.updateInvitedUsers(mEditText.getText().toString());

			searchForContact("");

		}
	};

	@Override
	public void onResume() {

		super.onResume();
		IABroadcastManager.registerForLocalBroadcast(receiver,
				IABIntent.INTENT_GET_CONTACTS);
	}

	@Override
	protected void initializeView(View view) {
		mAdapter = new ContactsListAdapter(getActivity());
		//stickyList.setAdapter(mAdapter);
		mEditText = (EditText) view.findViewById(R.id.et_add_contacts);

		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				int indexChar = mEditText.getText().toString()
						.lastIndexOf(HollerbackConstants.PHONE_SUF);

				LogUtil.i("last index of " + start);
				if (indexChar > 0
						&& mEditText.getText().toString().trim().length() > indexChar
						|| indexChar == -1) {
					searchForContact(mEditText
							.getText()
							.toString()
							.subSequence(indexChar + 1,
									mEditText.getText().toString().length())
							.toString());
				} else {
					searchForContact("");
				}

				LogUtil.i(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
				mAdapter.updateInvitedUsers(mEditText.getText().toString());
			}
		});

		mCreateConversationBtn = (Button) view.findViewById(R.id.b_hollerback);
		mCreateConversationBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createNewConversation();

			}
		});

	}

	protected void searchForContact(String searchString) {
		ArrayList<UserModel> searchItems;
		if (!searchString.trim().isEmpty()) {
			searchItems = new ArrayList<UserModel>();

			for (UserModel contact : TempMemoryStore.users.array) {
				if (contact.getName().toLowerCase()
						.contains(searchString.trim().toLowerCase())) {
					searchItems.add(contact);
				}
			}

			SortedArray tempSort = CollectionOpUtils.sortContacts(searchItems);

			//mAdapter.setContacts(tempSort.sortedKeys, tempSort.indexes);
		} else {
			//mAdapter.setContacts(TempMemoryStore.users.sortedKeys,TempMemoryStore.users.indexes);
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		IABroadcastManager.unregisterLocalReceiver(receiver);
	}

	/*
	@Override
	protected void onActionBarIntialized(CustomActionBarHelper viewHelper) {
		viewHelper.getLeftBtn().setVisibility(View.GONE);
		viewHelper.getRightBtn().setVisibility(View.GONE);
		viewHelper.setHeaderText(HollerbackApplication.getInstance().s(
				R.string.new_conversation));
	}*/

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (IABIntent.isIntent(intent, IABIntent.INTENT_GET_CONTACTS)) {
				//mAdapter.setContacts(TempMemoryStore.users.sortedKeys, TempMemoryStore.users.indexes);
				mAdapter.notifyDataSetChanged();
			}

		}
	};

	public void createNewConversation() {

		if (generateInvitedUsers().size() > 0) {
			Intent intent = new Intent(getActivity(),
					HollerbackCameraActivity.class);
			getActivity().startActivityForResult(intent,
					IABIntent.REQUEST_NEW_CONVERSATION);
		} else {
			Toast.makeText(getActivity(),
					"Add some contacts to start a conversation",
					Toast.LENGTH_SHORT).show();
		}
	}

	public ArrayList<String> generateInvitedUsers() {
		String text = mEditText.getText().toString();
		LogUtil.i("invited users: " + text);
		ArrayList<String> mPhoneNumbers = new ArrayList<String>();

		Pattern p = Pattern.compile(Pattern.quote("@") + "(.*?)"
				+ Pattern.quote("^"));
		Matcher m = p.matcher(text);
		while (m.find()) {
			String number = m.group(1);
			LogUtil.i("phone: " + number);
			mPhoneNumbers.add(number);
		}

		TempMemoryStore.invitedUsers = mPhoneNumbers;
		return mPhoneNumbers;
	}
}
