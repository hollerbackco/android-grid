package com.moziy.hollerback.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.connection.HBAsyncHttpResponseHandler;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.RegisterResponse;
import com.moziy.hollerback.model.web.response.VerifyResponse;
import com.moziy.hollerback.util.date.TimeUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

/**
 * This is a fragment that's going to use the new architecture, loader based rather than braodcast based
 * All the other ones were modified version of original piece, this is built from scratch
 *
 * @author peterma
 *
 */
public class SignUpConfirmFragment extends BaseFragment {
    private static final String TAG = SignUpConfirmFragment.class.getSimpleName();
    public static final String JUST_REGISTERED_BUNDLE_ARG_KEY = "JUST_REGISTERED";

    private SherlockFragmentActivity mActivity;
    private ViewGroup mRootView;
    private TextView mTxtPhone;
    private Button mBtnSubmit;
    private EditText mTxtVerify;
    private TextView mResendText;
    private PhoneNumberUtil mPhoneUtil;
    private final Handler mHandler = new Handler();

    public static SignUpConfirmFragment newInstance() {

        SignUpConfirmFragment f = new SignUpConfirmFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getSherlockActivity().getSupportActionBar().setTitle(R.string.action_verify);
        this.getSherlockActivity().getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        mPhoneUtil = PhoneNumberUtil.getInstance();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((HollerbackMainActivity) getActivity()).initWelcomeFragment();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (SherlockFragmentActivity) this.getSherlockActivity();
        mRootView = (ViewGroup) inflater.inflate(R.layout.verify_fragment, null);

        initializeView(mRootView);

        mTxtVerify.requestFocus();
        return mRootView;
    }

    @Override
    protected void initializeView(View view) {
        mTxtPhone = (TextView) mRootView.findViewById(R.id.tv_phone);

        try {
            mTxtPhone.setText(mPhoneUtil.format(
                    mPhoneUtil.parse(PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, ""), PreferenceManagerUtil.getPreferenceValue(HBPreferences.REGION_CODE, "")),
                    PhoneNumberFormat.INTERNATIONAL));
        } catch (Exception e) { // any exception just show the regular number
            mTxtPhone.setText(PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, "")); // just display the regular version
        }

        mTxtVerify = (EditText) mRootView.findViewById(R.id.txtfield_verify);
        mTxtVerify.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) { // code must be 4 digits
                    mBtnSubmit.setVisibility(View.VISIBLE);
                } else {
                    mBtnSubmit.setVisibility(View.GONE);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        mResendText = (TextView) mRootView.findViewById(R.id.tv_click_to_resend);

        mResendText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (reverificationAllowed()) {
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_REGISTRATION_TIME, System.currentTimeMillis());
                    mResendText.setEnabled(false);
                    resendVerificationText();

                }
            }
        });

        mBtnSubmit = (Button) mRootView.findViewById(R.id.btnSubmit);
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTxtVerify.getWindowToken(), 0);

                SignUpConfirmFragment.this.startLoading();

                HBRequestManager.postVerification(mTxtVerify.getText().toString(), PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, ""), new HBAsyncHttpResponseHandler<VerifyResponse>(
                        new TypeReference<VerifyResponse>() {
                        }) {

                    @Override
                    public void onResponseSuccess(int statusCode, VerifyResponse response) {

                        SignUpConfirmFragment.this.stopLoading();

                        if (response.access_token != null) { // lets save the access token

                            String access_token = response.access_token;

                            PreferenceManagerUtil.setPreferenceValue(HBPreferences.ACCESS_TOKEN, access_token);
                            PreferenceManagerUtil.setPreferenceValue(HBPreferences.IS_VERIFIED, true);

                            // user is officially logged in and registered, pop everything
                            getFragmentManager().popBackStackImmediate(WelcomeFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE); // pop everything

                            // TODO: Evaluate whether to add this fragment in onCreate and then swap it out later
                            // OldContactsFragment fragment = OldContactsFragment.newInstance(true, null);
                            // ContactsFragment fragment = ContactsFragment.newInstance();
                            ConversationListFragment fragment = ConversationListFragment.newInstance();
                            mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .commitAllowingStateLoss();
                        } else {
                            Log.e(TAG, "no access token sent!");
                            throw new IllegalStateException("No access token sent on successful registration!");
                        }

                    }

                    @Override
                    public void onApiFailure(Metadata metaData) {

                        SignUpConfirmFragment.this.stopLoading();

                        String message;
                        if (metaData != null) {

                            Log.w(TAG, "error code: " + metaData.code);

                            if (metaData.msg != null && !metaData.msg.isEmpty()) {
                                message = metaData.msg;
                            } else if (metaData.message != null && !metaData.message.isEmpty()) {
                                message = metaData.message;
                            } else {
                                message = getString(R.string.error_general);
                            }

                            Log.w(TAG, "message: " + metaData.message + " " + metaData.msg);

                        } else {
                            message = getString(R.string.error_general);
                        }

                        if (isAdded()) {
                            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!reverificationAllowed()) {
            mResendText.setEnabled(false);
            mHandler.removeCallbacks(mEnableReVerifyBtn);
            mHandler.postDelayed(mEnableReVerifyBtn, TimeUtil.ONE_MINUTE_MILLIS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mEnableReVerifyBtn);
    }

    private Runnable mEnableReVerifyBtn = new Runnable() {

        @Override
        public void run() {
            mResendText.setEnabled(true);
        }
    };

    private boolean reverificationAllowed() {
        return (System.currentTimeMillis() - PreferenceManagerUtil.getPreferenceValue(HBPreferences.LAST_REGISTRATION_TIME, 0L)) > TimeUtil.ONE_MINUTE_MILLIS;
    }

    private void resendVerificationText() {
        String phone = PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, null);
        String email = PreferenceManagerUtil.getPreferenceValue(HBPreferences.EMAIL, null);
        String password = PreferenceManagerUtil.getPreferenceValue(HBPreferences.PASSWORD, null);
        String username = PreferenceManagerUtil.getPreferenceValue(HBPreferences.USERNAME, null);

        if (phone == null || email == null || password == null || username == null) {
            PreferenceManagerUtil.removeSelectedPreference(HBPreferences.PHONE);
            PreferenceManagerUtil.removeSelectedPreference(HBPreferences.EMAIL);
            PreferenceManagerUtil.removeSelectedPreference(HBPreferences.PASSWORD);
            PreferenceManagerUtil.removeSelectedPreference(HBPreferences.USERNAME);
            // return the user back to the sign up page

            getFragmentManager().popBackStackImmediate(WelcomeFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            WelcomeFragment f = WelcomeFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.fragment_holder, f).commit();

            return;
        }

        HBRequestManager.postRegistration(email, password, username, phone, new HBAsyncHttpResponseHandler<RegisterResponse>(new TypeReference<RegisterResponse>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, RegisterResponse response) {
                Toast.makeText(getActivity(), getString(R.string.toast_reverify_sent), Toast.LENGTH_LONG).show();
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_REGISTRATION_TIME, System.currentTimeMillis());

                mResendText.setEnabled(false);
                mHandler.removeCallbacks(mEnableReVerifyBtn);
                mHandler.postDelayed(mEnableReVerifyBtn, TimeUtil.ONE_MINUTE_MILLIS);
            }

            @Override
            public void onApiFailure(Metadata metaData) {
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_REGISTRATION_TIME, 0L);
            }
        });

    }
}
