package com.moziy.hollerback.fragment;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.R;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.RegisterResponse;
import com.moziy.hollerback.model.web.response.VerifyResponse;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.PreferenceManagerUtil;
import com.moziy.hollerback.util.TimeUtil;
import com.moziy.hollerbacky.connection.HBAsyncHttpResponseHandler;
import com.moziy.hollerbacky.connection.HBRequestManager;

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
        mTxtPhone.setText(PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, ""));

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
                    Toast.makeText(getActivity(), getString(R.string.toast_reverify_sent), Toast.LENGTH_LONG).show();

                }
            }
        });

        mBtnSubmit = (Button) mRootView.findViewById(R.id.btnSubmit);
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

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

                            // user is officially logged in and registered, pop everything
                            getFragmentManager().popBackStackImmediate(WelcomeFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE); // pop everything

                            ContactsFragment fragment = ContactsFragment.newInstance(true, null);
                            mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .commitAllowingStateLoss();
                        } else {
                            Log.e(TAG, "no access token sent!");
                            throw new IllegalStateException("No access token sent on successful registration!");
                        }

                    }

                    @Override
                    public void onApiFailure(Metadata metaData) {
                        Log.w(TAG, "error code: " + metaData.code);

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
