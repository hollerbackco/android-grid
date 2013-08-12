package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.adapter.SMSAdapter;
import com.moziy.hollerback.model.SMSContact;

public class ContactsFragment extends BaseFragment {

	private ViewGroup mRootView;
	private Activity mActivity;
    private ListView mSMSList;

    private SMSAdapter mContactSMSAdapter;
    private List<SMSContact> mSelectedSMSContactsAdapterData = new ArrayList<SMSContact>();
    
	public static ContactsFragment newInstance() {
		ContactsFragment f = new ContactsFragment();
		return f;
	}
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	switch(item.getItemId())
    	{
	    	case android.R.id.home:
	    		this.getFragmentManager().popBackStack();
	    		break;
	    }
    	
    	return super.onOptionsItemSelected(item);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		this.getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mActivity = this.getActivity();
		mRootView = (ViewGroup)inflater.inflate(R.layout.contact_fragment, null);
        mSMSList = (ListView) mRootView.findViewById(R.id.smsList);
        
        final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        final String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                Phone.NUMBER,
                ContactsContract.Contacts.Photo.CONTACT_ID
        };
        final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
        String[] selectionArgs = null;
        
        String[] fields = new String[] {
        		ContactsContract.Contacts.Photo.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                Phone.NUMBER
        };
        
        Cursor cursor = mActivity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
        mContactSMSAdapter = new SMSAdapter(	mActivity.getApplicationContext(), R.layout.row_invitecontactresults, 
				cursor, fields, new int[] {R.id.lazyIcon, R.id.lazyTitle, R.id.lazySubTitle}, mSelectedSMSContactsAdapterData);
        
    	mContactSMSAdapter.setFilterQueryProvider(new FilterQueryProvider() {
   		 
    		public Cursor runQuery(CharSequence constraint) {
    			String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%"+constraint+"%'";
    			String[] selectionArgs = null;
    			Cursor cur = mActivity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    			return cur;
    		}
     
    	});
		
    	mSMSList.setAdapter(mContactSMSAdapter);

		return mRootView;
	}

	@Override
	protected void initializeView(View view) {
		// TODO Auto-generated method stub
		
	}
}
