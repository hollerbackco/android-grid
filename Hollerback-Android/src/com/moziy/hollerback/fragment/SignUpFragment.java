package com.moziy.hollerback.fragment;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.Country;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.RegisterResponse;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.ISOUtil;
import com.moziy.hollerback.util.LoadingFragmentUtil;
import com.moziy.hollerback.util.PhoneTextWatcher;
import com.moziy.hollerback.util.PreferenceManagerUtil;
import com.moziy.hollerback.util.validators.ValidatorUtil;
import com.moziy.hollerbacky.connection.HBAsyncHttpResponseHandler;
import com.moziy.hollerbacky.connection.HBRequestManager;

public class SignUpFragment extends BaseFragment implements OnClickListener {
    private static final String TAG = SignUpFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG = TAG;

    public static final String EMAIL_BUNDLE_ARG_KEY = "EMAIL";
    public static final String PASSWORD_BUNDLE_ARG_KEY = "PASSWORD";
    public static final String REGION_BUNDLE_ARG_KEY = "REGION";

    public static final String SUBMITTED_BUNDLE_ARG_KEY = "SUBMITTED";

    private SherlockFragmentActivity mActivity;
    private EditText mNameField, mPhoneNumberField;

    private Button mSubmitButton;

    private View mRLCountrySelector;

    private TextView mCountryText, mPhoneNumberCode;

    private AlertDialog countriesDialog;

    private List<Country> mCountries;

    private CharSequence[] mCharCountries;

    private View mSubmitLayout;

    private String mRegistrationName;
    private String mRegistrationPhone;

    private String mEmail;
    private String mPassword;

    private boolean mIsSubmitted = false;

    private LoadingFragmentUtil mLoadingBar;

    private TextWatcher mPhoneTextWatcher;

    public static SignUpFragment newInstance(String email, String password) {

        SignUpFragment f = new SignUpFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(EMAIL_BUNDLE_ARG_KEY, email);
        args.putString(PASSWORD_BUNDLE_ARG_KEY, password);
        f.setArguments(args);
        return f;
    }

    private Country mSelectedCountry;

    private PhoneNumberUtil util;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mEmail = args.getString(EMAIL_BUNDLE_ARG_KEY);
        mPassword = args.getString(PASSWORD_BUNDLE_ARG_KEY);

        util = PhoneNumberUtil.getInstance();
        Set<String> set = util.getSupportedRegions();

        mCountries = ISOUtil.getCountries(set.toArray(new String[set.size()]));

        mCharCountries = new CharSequence[mCountries.size()];

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SUBMITTED_BUNDLE_ARG_KEY)) {

                mIsSubmitted = savedInstanceState.getBoolean(SUBMITTED_BUNDLE_ARG_KEY);
            }

        } else {
            mIsSubmitted = false;
        }

        // check to see if the user has actually gotten a registration response back
        mLoadingBar = new LoadingFragmentUtil(getSherlockActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = this.getSherlockActivity();
        this.getSherlockActivity().getSupportActionBar().show();
        this.getSherlockActivity().getSupportActionBar().setTitle(R.string.create_account);
        this.getSherlockActivity().getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        // TODO Auto-generated method stub
        View fragmentView = inflater.inflate(R.layout.signup_fragment, container, false);
        initializeView(fragmentView);

        Locale locale = Locale.getDefault();

        mSelectedCountry = new Country(locale.getISO3Country(), locale.getCountry(), locale.getDisplayCountry());

        mSubmitLayout = fragmentView.findViewById(R.id.submit_layout);

        mPhoneTextWatcher = new CustomPhoneTextWatcher(mSelectedCountry.code);
        mPhoneNumberField.addTextChangedListener(mPhoneTextWatcher);
        setPhoneNumber();

        mCountryText.setText(mSelectedCountry.name);

        mPhoneNumberCode.setText("+" + Integer.toString(util.getCountryCodeForRegion(mSelectedCountry.code)));

        for (int i = 0; i < mCountries.size(); i++) {
            mCharCountries[i] = mCountries.get(i).name;
        }

        return fragmentView;
    }

    @Override
    protected void initializeView(View view) {
        mNameField = (EditText) view.findViewById(R.id.et_username);
        mPhoneNumberField = (EditText) view.findViewById(R.id.textfield_phonenumber);

        mSubmitButton = (Button) view.findViewById(R.id.register_submit);

        mSubmitButton.setOnClickListener(this);

        mRLCountrySelector = view.findViewById(R.id.rl_country_selector);

        mCountryText = (TextView) view.findViewById(R.id.tv_country_selector);
        mRLCountrySelector.setOnClickListener(this);
        mPhoneNumberCode = (TextView) view.findViewById(R.id.tv_phone_number_code);

        TextView headerAccount = (TextView) view.findViewById(R.id.tv_header_account);
        TextView headerCPhone = (TextView) view.findViewById(R.id.tv_header_counter_phone);
        TextView signupAgreement = (TextView) view.findViewById(R.id.tv_signup_agreement);

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mIsSubmitted && PreferenceManagerUtil.getPreferenceValue(HBPreferences.USERNAME, null) != null) {
            // go directly to the signup fragment
            SignUpConfirmFragment fragment = SignUpConfirmFragment.newInstance();
            mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(SUBMITTED_BUNDLE_ARG_KEY, mIsSubmitted);

        super.onSaveInstanceState(outState);
    }

    private void setPhoneNumber() {
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getLine1Number();

        if (number != null) {
            mPhoneNumberField.setText(number);
        }
    }

    public void processSubmit() {

        if (verifyFields()) {

            if (mIsSubmitted) {
                return;
            }

            final String regionCode = mSelectedCountry.code;

            mIsSubmitted = true;
            mLoadingBar.startLoading();
            HBRequestManager.postRegistration(mEmail, mPassword, mRegistrationName, mRegistrationPhone, new HBAsyncHttpResponseHandler<RegisterResponse>(new TypeReference<RegisterResponse>() {
            }) {

                @Override
                public void onResponseSuccess(int statusCode, RegisterResponse response) {

                    mIsSubmitted = false;

                    mLoadingBar.stopLoading();
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.USERNAME, response.user.username);
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.EMAIL, mEmail);
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.PASSWORD, mPassword);
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.PHONE, response.user.phone);
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.ID, response.user.id);
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.REGION_CODE, regionCode);
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.IS_VERIFIED, response.user.is_verified);
                    PreferenceManagerUtil.setPreferenceValue(HBPreferences.LAST_REGISTRATION_TIME, System.currentTimeMillis());

                    SignUpConfirmFragment fragment = SignUpConfirmFragment.newInstance();
                    mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(FRAGMENT_TAG).commitAllowingStateLoss();

                }

                @Override
                public void onApiFailure(Metadata metaData) {

                    mLoadingBar.stopLoading();
                    mIsSubmitted = false;

                    Log.w(TAG, "Registration Failed");
                    if (metaData != null) {
                        Log.w(TAG, "message: " + metaData.message);
                    }

                    if (isAdded())
                        Toast.makeText(mActivity, getString(R.string.error_registration), Toast.LENGTH_LONG).show();

                }
            });

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_submit:
                processSubmit();
                break;
            case R.id.rl_country_selector:
                showDialog();
                break;
        }
    }

    public void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Your Country");
        builder.setSingleChoiceItems(mCharCountries, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mSelectedCountry = mCountries.get(item);
                mCountryText.setText(mCountries.get(item).name);
                mPhoneNumberCode.setText("+" + Integer.toString(util.getCountryCodeForRegion(mSelectedCountry.code)));
                mPhoneNumberField.removeTextChangedListener(mPhoneTextWatcher);
                mPhoneNumberField.getText().clear();
                mPhoneNumberField.addTextChangedListener(new CustomPhoneTextWatcher(mSelectedCountry.code));
                countriesDialog.dismiss();
            }
        });
        countriesDialog = builder.create();
        countriesDialog.show();

    }

    private PhoneNumber getPhoneNumber() {
        if (mPhoneNumberField.getText().toString() == null || mPhoneNumberField.getText().toString().trim().isEmpty() || mPhoneNumberField.getText().toString().trim().length() < 3) {
            return null;
        }

        PhoneNumber number = null;
        try {
            number = util.parse(mPhoneNumberField.getText().toString(), mSelectedCountry.code);
            Log.d(TAG, "getPhoneNumber() - " + number.toString());
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return number;
    }

    public boolean verifyFields() {
        boolean valid = true;
        String[] messageOut = new String[1];

        // check fields in opposite order

        valid &= ValidatorUtil.isValidPhone(getPhoneNumber(), mSelectedCountry.code, messageOut);

        valid &= ValidatorUtil.isValidName(mNameField.getText().toString(), messageOut);

        if (!valid) {
            Toast.makeText(getActivity(), messageOut[0], Toast.LENGTH_LONG).show();
        } else {
            mRegistrationName = mNameField.getText().toString();
            mRegistrationPhone = util.format(getPhoneNumber(), PhoneNumberFormat.E164);
            LogUtil.i("Signing up with: " + mRegistrationName + " " + mRegistrationPhone);
        }

        return valid;

    }

    private class CustomPhoneTextWatcher extends PhoneTextWatcher {

        public CustomPhoneTextWatcher(String countryCode) {
            super(countryCode);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);

            if (s.length() > 3) {
                mSubmitLayout.setVisibility(View.VISIBLE);
            } else {
                mSubmitLayout.setVisibility(View.INVISIBLE);
            }

        }
    }
}
