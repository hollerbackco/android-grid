package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.Toast;


import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.moziy.hollerback.R;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.util.HollerbackAPI;
import com.moziy.hollerback.util.JSONUtil;
import com.moziy.hollerbacky.connection.HBRequestManager;

public class ContactsInviteFragment extends ContactsFragment {
	private String INVITE = "INVITE";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    	menu.add(INVITE)
		.setActionView(R.layout.button_invite)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        ;
    	
    	for(int i = 0; i < menu.size(); i++)
    	{
    		if(menu.getItem(i).getTitle().toString().equalsIgnoreCase(INVITE))
    		{	//Finding the button from custom View
    			menu.getItem(i).getActionView().findViewById(R.id.btnSignUp)
    			.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(mSelectedSMSContactsAdapterData.isEmpty())
						{
							Toast.makeText(mActivity, R.string.contacts_minimum_required, Toast.LENGTH_LONG).show();
							return;
						}
						sendInvite();
					}
				});
    			break;
    		}
    	}
    }
    
	public static ContactsInviteFragment newInstance() {
		ContactsInviteFragment f = new ContactsInviteFragment();
		return f;
	}
	
	private void sendInvite()
	{
		String[] phones = mSelectedSMSContactsAdapterData.keySet().toArray(new String[mSelectedSMSContactsAdapterData.size()]);
		ArrayList<String> contacts = new ArrayList<String>();
		contacts.addAll(Arrays.asList(phones));
		
		this.startLoading();
		HBRequestManager.conversationInvite(contacts, new JsonHttpResponseHandler() {

			@Override
			protected Object parseResponse(String arg0)
					throws JSONException {
				LogUtil.i(arg0);
				return super.parseResponse(arg0);

			}

			@Override
			public void onFailure(Throwable arg0, JSONObject arg1) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1);
				LogUtil.e(HollerbackAPI.API_CONVERSATION
						+ "FAILURE");
			}

			@Override
			public void onSuccess(int arg0, JSONObject arg1) {
				// TODO Auto-generated method stub
				super.onSuccess(arg0, arg1);
				LogUtil.i("ON SUCCESS API CONVO");
				ContactsInviteFragment.this.stopLoading();
				mActivity.getSupportFragmentManager().popBackStack();
			}

		});
	}
	
	@Override
	protected void bindData()
	{
		this.startLoading();
		//Now runs to get data
		HBRequestManager.getContacts(TempMemoryStore.users.array, 
				new JsonHttpResponseHandler() {

			@Override
			protected Object parseResponse(String arg0)
					throws JSONException {
				LogUtil.i("RESPONSE: " + arg0);
				return super.parseResponse(arg0);

			}

			@Override
			public void onFailure(Throwable arg0, JSONObject arg1) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1);
				LogUtil.e(HollerbackAPI.API_CONTACTS + "FAILURE");
			}

			@Override
			public void onSuccess(int statusId, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusId, response);
				LogUtil.i("ON SUCCESS API CONTACTS");
				SortedArray data  = JSONUtil.processGetContacts(response, true);
				if(data != null)
				{
					int phone = 0;
					
					//brute force, do a better logic than me next time
					for(int i = 0; i < TempMemoryStore.users.sortedKeys.size(); i++)
					{
						UserModel user = TempMemoryStore.users.mUserModelHash.get(TempMemoryStore.users.sortedKeys.get(i));
						if(user.isHollerbackUser)
						{
							TempMemoryStore.users.mUserModelHash.remove(TempMemoryStore.users.sortedKeys.get(i));
						}
						else phone++;
					}
					
					mAdapter.setContacts(TempMemoryStore.users.sortedKeys, mSelectedSMSContactsAdapterData, mListener, 0, phone);
					mAdapter.notifyDataSetChanged();
					
			        for(int i = 0; i < mAdapter.getGroupCount(); i++)
			        {
			        	mSMSList.expandGroup(i);
			        }
				}
				ContactsInviteFragment.this.stopLoading();

			}

		});
	}
}
