package com.moziy.hollerback.fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.moziy.hollerback.R;
import com.moziy.hollerback.HollerbackInterfaces.OnContactSelectedListener;
import com.moziy.hollerback.adapter.ContactsListAdapter;
import com.moziy.hollerback.cache.memory.TempMemoryStore;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.model.SortedArray;
import com.moziy.hollerback.model.UserModel;
import com.moziy.hollerback.service.VideoUploadService;
import com.moziy.hollerback.util.CollectionOpUtils;
import com.moziy.hollerback.util.FileUtil;
import com.moziy.hollerback.util.HollerbackAPI;
import com.moziy.hollerback.util.JSONUtil;
import com.moziy.hollerback.util.NumberUtil;
import com.moziy.hollerback.util.UploadCacheUtil;
import com.moziy.hollerbacky.connection.HBRequestManager;

public class ContactsFragment extends BaseFragment {
	private String NEXT = "NEXT";

	private ViewGroup mRootView;
    protected ExpandableListView mSMSList;

    //private SMSAdapter mContactSMSAdapter;
	protected ContactsListAdapter mAdapter;
    protected HashMap<String, String> mSelectedSMSContactsAdapterData = new HashMap<String, String>();
    protected OnContactSelectedListener mListener;
    
    private String mConversationTitle;
	private String mFileDataName;
	private boolean isWelcomeScreen;
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    	menu.add(NEXT)
		.setActionView(R.layout.button_next)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        ;
    	
    	for(int i = 0; i < menu.size(); i++)
    	{
    		if(menu.getItem(i).getTitle().toString().equalsIgnoreCase(NEXT))
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
						
						if(isWelcomeScreen)
						{
							inviteAndsendVideo();
						}
						else
						{
							inviteAndRecordVideo();
						}
					}
				});
    			break;
    		}
    	}
    }
    
	public static ContactsFragment newInstance() {
		ContactsFragment f = new ContactsFragment();
		return f;
	}
	
	public static ContactsFragment newInstance(boolean isWelcomeScreen, String fileDataName) {
		ContactsFragment f = new ContactsFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("isWelcomeScreen", isWelcomeScreen);
		bundle.putString("fileDataName", fileDataName);
		f.setArguments(bundle);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity.getSupportActionBar().setTitle(this.getResources().getString(R.string.preference_new_group));
		mRootView = (ViewGroup)inflater.inflate(R.layout.contact_fragment, null);

		if(this.getArguments() != null && this.getArguments().getBoolean("isWelcomeScreen"))
        {
			isWelcomeScreen = this.getArguments().getBoolean("isWelcomeScreen");
			mFileDataName = this.getArguments().getString("fileDataName");
        }
		initializeView(mRootView);
		
		bindData();
		
		return mRootView;
	}

	@Override
	protected void initializeView(View view) {
		mAdapter = new ContactsListAdapter(mActivity);
        mSMSList = (ExpandableListView) mRootView.findViewById(R.id.smsList);
        mSMSList.setOnGroupClickListener(null);
        mSMSList.setOnGroupCollapseListener(null);
        mSMSList.setOnGroupExpandListener(null);
        mSMSList.setClickable(false);

        //when item is clicked
        mListener = new OnContactSelectedListener(){

			@Override
			public void onItemClicked(int position) {
				String[] names = mSelectedSMSContactsAdapterData.values().toArray(new String[mSelectedSMSContactsAdapterData.size()]);
				if(names.length > 0)
				{
					mConversationTitle = "";
					for(int i = 0; i < names.length; i++)
					{
						mConversationTitle += names[i];
						if(i < names.length - 1)
						{
							mConversationTitle += ",";
						}
					}
					ContactsFragment.this.getSherlockActivity().getSupportActionBar().setTitle(mConversationTitle);
					
				}
				else
				{
					ContactsFragment.this.getSherlockActivity().getSupportActionBar().setTitle(mActivity.getResources().getString(R.string.preference_friends));
				}
			}
        	
        };
        
        TempMemoryStore.users = getSortedUserArray();
        
        if(isWelcomeScreen)
        {
        	TextView txtHeader = (TextView)mRootView.findViewById(R.id.txtHeader);
        	txtHeader.setVisibility(View.VISIBLE);
        }

        mSMSList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Toast.makeText(mActivity, "test", Toast.LENGTH_LONG).show();
				return false;
			}
		});

		mAdapter.setContacts(TempMemoryStore.users.sortedKeys, mSelectedSMSContactsAdapterData, mListener, TempMemoryStore.users.sortedKeys.size(), 0);

    	mSMSList.setAdapter(mAdapter);
	}
	
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
					int hollerback = 0;
					int phone = 0;
					
					for(int i = 0; i < TempMemoryStore.users.sortedKeys.size(); i++)
					{
						UserModel user = TempMemoryStore.users.mUserModelHash.get(TempMemoryStore.users.sortedKeys.get(i));
						if(user.isHollerbackUser)
						{
							hollerback++;
						}
						else phone++;
					}
					
					mAdapter.setContacts(TempMemoryStore.users.sortedKeys, mSelectedSMSContactsAdapterData, mListener, hollerback, phone);
					mAdapter.notifyDataSetChanged();
					
			        for(int i = 0; i < mAdapter.getGroupCount(); i++)
			        {
			        	mSMSList.expandGroup(i);
			        }
				}
		        ContactsFragment.this.stopLoading();

			}

		});

		
	}
	
	protected SortedArray getSortedUserArray() {
		Cursor c = this.getActivity().getContentResolver().query(
				Data.CONTENT_URI,
				new String[] { Data._ID, Data.DISPLAY_NAME, Phone.NUMBER,
						Data.CONTACT_ID, Phone.TYPE, Phone.LABEL, Data.PHOTO_ID },
				Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'", null,
				Data.DISPLAY_NAME);

		int count = c.getCount();
		boolean b = c.moveToFirst();
		String[] columnNames = c.getColumnNames();
		int displayNameColIndex = c.getColumnIndex("display_name");
		int idColIndex = c.getColumnIndex("_id");
		// int contactIdColIndex = c.getColumnIndex("contact_id");
		int col2Index = c.getColumnIndex(columnNames[2]);
		int col3Index = c.getColumnIndex(columnNames[3]);
		int col4Index = c.getColumnIndex(columnNames[4]);
		int col6Index = c.getColumnIndex(columnNames[6]);
		
		ArrayList<UserModel> contactItemList = new ArrayList<UserModel>();

		ActiveAndroid.beginTransaction();
		new Delete().from(ConversationModel.class).execute();

		for (int i = 0; i < count; i++) {

			String displayName = c.getString(displayNameColIndex);
			String phoneNumber = c.getString(col2Index);
			int contactId = c.getInt(col3Index);
			String phoneType = c.getString(col4Index);
			String photourl = c.getString(col6Index) ;
					
			long _id = c.getLong(idColIndex);
			UserModel contactItem = new UserModel();
			// contactItem.userId = _id;
			contactItem.contactId = contactId;
			contactItem.name = displayName;
			contactItem.photourl = photourl;
					
			contactItem.phone = NumberUtil.getE164Number(phoneNumber);
			if (contactItem.phone != null) {
				contactItemList.add(contactItem);
			}

			contactItem.save();
			boolean b2 = c.moveToNext();
		}

		ActiveAndroid.setTransactionSuccessful();
		ActiveAndroid.endTransaction();

		c.close();

		return CollectionOpUtils.sortContacts(contactItemList);

	}
	
	/**
	 * These 2 functions can use some clean up, I did it extremely fast but you can play with logic a little bit.
	 */
	private void inviteAndRecordVideo()
	{
		String[] phones = mSelectedSMSContactsAdapterData.keySet().toArray(new String[mSelectedSMSContactsAdapterData.size()]);
		
		mActivity.getSupportFragmentManager().popBackStack();
		
		RecordVideoFragment recordfragment = RecordVideoFragment.newInstance(phones, mConversationTitle);

		mActivity.getSupportFragmentManager()
		.beginTransaction().replace(R.id.fragment_holder, recordfragment)
		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .addToBackStack(RecordVideoFragment.class.getSimpleName())
        .commitAllowingStateLoss();
	}
	
	/**
	 * just like the function name, this is suppose to be in loader, but his HBManager structure is weird so I can't create 
	 * a loader.  But all the callbacks should be able to interact with the fragment rather than coming in from Broadcast
	 * change all the arg1 crap, makes it unreadable
	 */
	private void inviteAndsendVideo()
	{
		String[] phones = mSelectedSMSContactsAdapterData.keySet().toArray(new String[mSelectedSMSContactsAdapterData.size()]);
		
		ArrayList<String> contacts = new ArrayList<String>();
		contacts.addAll(Arrays.asList(phones));
		
		HBRequestManager.postConversations(contacts, 
			new JsonHttpResponseHandler() {
	
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
				public void onSuccess(int statusId, JSONObject response) {
					// TODO Auto-generated method stub
					super.onSuccess(statusId, response);
					LogUtil.i("ON SUCCESS API CONVO");
					JSONUtil.processPostConversations(response);
					
					//successful, now we upload video
					try {

						JSONObject conversation = response.getJSONObject("data");
						
						if(!conversation.has("id"))
						{
							return;
						}
						
						String conversationId = String.valueOf(conversation.getInt("id"));
						
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);
						
						JSONObject cacheData = new JSONObject();
						try {
							File tmp = new File(FileUtil.getLocalFile(FileUtil.getImageUploadName(mFileDataName)));
							String fileurl = Uri.fromFile(tmp).toString();
							
							cacheData.put("filename", mFileDataName);
							cacheData.put("id", 0);
							cacheData.put("conversation_id", conversationId);
							cacheData.put("isRead", true);
							cacheData.put("url", fileurl);
							cacheData.put("thumb_url", fileurl);
							cacheData.put("created_at", df.format(new Date()));
							cacheData.put("username", "me");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Intent serviceIntent = new Intent(mActivity, VideoUploadService.class);
						serviceIntent.putExtra("ConversationId", conversationId);
						serviceIntent.putExtra("FileDataName", mFileDataName);
						serviceIntent.putExtra("ImageUploadName", FileUtil.getImageUploadName(mFileDataName));
						
						if(cacheData != new JSONObject())
						{
							serviceIntent.putExtra("JSONCache", cacheData.toString());
							UploadCacheUtil.setUploadCacheFlag(mActivity, conversationId, cacheData);
						}
						
						mActivity.startService(serviceIntent);
						
						mActivity.getSupportFragmentManager().popBackStack(WelcomeFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
						mActivity.getSupportFragmentManager().popBackStack(WelcomeFinishFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
						
						WelcomeFinishFragment fragment = WelcomeFinishFragment.newInstance();
						mActivity.getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.fragment_holder, fragment)
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				        .addToBackStack(WelcomeFinishFragment.class.getSimpleName())
				        .commitAllowingStateLoss();	
					} catch (Exception e) {
						e.printStackTrace();
					}
					//just posted, now upload the video
				}
		});
	}
}
