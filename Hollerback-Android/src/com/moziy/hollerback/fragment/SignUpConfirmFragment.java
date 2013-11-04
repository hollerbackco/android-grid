package com.moziy.hollerback.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.VerifyResponse;
import com.moziy.hollerback.util.HollerbackPreferences;
import com.moziy.hollerback.util.JSONUtil;
import com.moziy.hollerback.util.PreferenceManagerUtil;
import com.moziy.hollerbacky.connection.HBRequestManager;
import com.moziy.hollerbacky.connection.HBAsyncHttpResponseHandler;

/**
 * This is a fragment that's going to use the new architecture, loader based rather than braodcast based
 * All the other ones were modified version of original piece, this is built from scratch
 *
 * @author peterma
 *
 */
public class SignUpConfirmFragment extends BaseFragment{
	private static final String TAG = SignUpConfirmFragment.class.getSimpleName();
	
	private SherlockFragmentActivity mActivity;
	private ViewGroup mRootView; 
	private TextView mTxtPhone;
	private Button mBtnSubmit;
	private EditText mTxtVerify;
	
	private String mFileDataName;
	private boolean mHasFile;

	public static SignUpConfirmFragment newInstance(boolean hasFile, String fileDataName) {

		SignUpConfirmFragment f = new SignUpConfirmFragment();
		Bundle args = new Bundle();
		args.putString("fileDataName", fileDataName);
		args.putBoolean("hasFile", hasFile);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (SherlockFragmentActivity)this.getSherlockActivity();
		mRootView = (ViewGroup)inflater.inflate(R.layout.verify_fragment, null);
    	this.getSherlockActivity().getSupportActionBar().setTitle(R.string.action_verify);
    	this.getSherlockActivity().getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));
    
    	initializeView(mRootView);
    	
    	//if it doesn't have this it will crash
    	if(this.getArguments() != null && this.getArguments().containsKey("hasFile"))
    	{
    		mHasFile = this.getArguments().getBoolean("hasFile");
    		if(mHasFile)mFileDataName = this.getArguments().getString("fileDataName");
    	}
    	mTxtVerify.requestFocus();
    	return mRootView;
	}
	
	@Override
	protected void initializeView(View view) {
		mTxtPhone = (TextView)mRootView.findViewById(R.id.tv_phone);
		mTxtPhone.setText(PreferenceManagerUtil.getPreferenceValue(HollerbackPreferences.PHONE, ""));
		
		mTxtVerify = (EditText)mRootView.findViewById(R.id.txtfield_verify);
		
		mBtnSubmit = (Button)mRootView.findViewById(R.id.btnSubmit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SignUpConfirmFragment.this.startLoading();
				HBRequestManager.postVerification(mTxtVerify.getText().toString(), 
						PreferenceManagerUtil.getPreferenceValue(HollerbackPreferences.PHONE, ""), 
						new HBAsyncHttpResponseHandler<VerifyResponse>(new TypeReference<VerifyResponse>() {
						}) {

							@Override
							public void onResponseSuccess(int statusCode,
									VerifyResponse response) {
								
								LogUtil.i(response.toString());
								SignUpConfirmFragment.this.stopLoading();
								if(response.access_token != null){
									JSONUtil.processVerify(response);
									if(mHasFile)
									{
										ContactsFragment fragment = ContactsFragment.newInstance(true, mFileDataName);
										mActivity.getSupportFragmentManager()
										.beginTransaction()
										.replace(R.id.fragment_holder, fragment)
										.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
								        .addToBackStack(ContactsFragment.class.getSimpleName())
								        .commitAllowingStateLoss();	
									}
									else
									{
										Intent intent = new Intent(mActivity, HollerbackMainActivity.class);
										mActivity.startActivity(intent);
										mActivity.finish();
									}
								}
								
							}
							
							@Override
							public void onApiFailure(Metadata metaData) {
								Log.w(TAG, "error code: " + metaData.code);
								
							}
						});
//				HBRequestManager.postVerification(
//						mTxtVerify.getText().toString(), 
//						PreferenceManagerUtil.getPreferenceValue(HollerbackPreferences.PHONE, ""), 
//						new JsonHttpResponseHandler() {
//							@Override
//							protected Object parseResponse(String arg0)
//									throws JSONException {
//								LogUtil.i(arg0);
//								return super.parseResponse(arg0);
//		
//							}
//		
//							@Override
//							public void onFailure(Throwable arg0, JSONObject response) {
//								// TODO Auto-generated method stub
//								super.onFailure(arg0, response);
//								LogUtil.i("LOGIN FAILURE");
//								if(response.has("meta"))
//								{
//									try {
//										JSONObject metadata = response.getJSONObject("meta");
//										Toast.makeText(mActivity, metadata.getString("msg"), Toast.LENGTH_LONG).show();
//									} catch (JSONException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//									
//								}
//							}
//		
//							@Override
//							public void onSuccess(int statusCode, JSONObject response) {
//								// TODO Auto-generated method stub
//								super.onSuccess(statusCode, response);
//								LogUtil.i(response.toString());
//								SignUpConfirmFragment.this.stopLoading();
//								if(response.has("access_token"))
//								{
//									JSONUtil.processVerify(response);
//									if(mHasFile)
//									{
//										ContactsFragment fragment = ContactsFragment.newInstance(true, mFileDataName);
//										mActivity.getSupportFragmentManager()
//										.beginTransaction()
//										.replace(R.id.fragment_holder, fragment)
//										.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//								        .addToBackStack(ContactsFragment.class.getSimpleName())
//								        .commitAllowingStateLoss();	
//									}
//									else
//									{
//										Intent intent = new Intent(mActivity, HollerbackMainActivity.class);
//										mActivity.startActivity(intent);
//										mActivity.finish();
//									}
//								}
//							}
//						});

			}
		});
	}
}
