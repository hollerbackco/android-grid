package com.moziy.hollerback.fragment;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.Country;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.LoginResponse;
import com.moziy.hollerback.util.HollerbackPreferences;
import com.moziy.hollerback.util.ISOUtil;
import com.moziy.hollerback.util.NumberUtil;
import com.moziy.hollerback.util.PreferenceManagerUtil;
import com.moziy.hollerback.validator.TextValidator;
import com.moziy.hollerbacky.connection.HBAsyncHttpResponseHandler;
import com.moziy.hollerbacky.connection.HBRequestManager;

public class SignInFragment extends BaseFragment {

    private static final String TAG = SignInFragment.class.getSimpleName();

    private SherlockFragmentActivity mActivity;

    private Button mLoginBtn;

    private CharSequence[] mCharCountries;
    private Country mSelectedCountry;
    private View mRLCountrySelector;
    private TextView mCountryText, mPhoneNumberCode;
    private EditText mPhoneNumberField;
    private List<Country> mCountries;
    private AlertDialog countriesDialog;
    private PhoneNumberUtil util;
    private String mRegistrationPhone;

    private String mFileDataName;
    private boolean mHasFile;

    public static SignInFragment newInstance() {
        SignInFragment f = new SignInFragment();
        return f;
    }

    public static SignInFragment newInstance(boolean hasFile, String fileDataName) {

        SignInFragment f = new SignInFragment();

        // Supply num input as an argument.
        Bundle bundle = new Bundle();
        bundle.putString("fileDataName", fileDataName);
        bundle.putBoolean("hasFile", hasFile);
        f.setArguments(bundle);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = this.getSherlockActivity();
        this.getSherlockActivity().getSupportActionBar().show();
        this.getSherlockActivity().getSupportActionBar().setTitle(R.string.signin);
        this.getSherlockActivity().getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        View fragmentView = inflater.inflate(R.layout.signin_fragment, null);

        initializeView(fragmentView);

        util = PhoneNumberUtil.getInstance();
        Set<String> set = util.getSupportedRegions();

        mCountries = ISOUtil.getCountries(set.toArray(new String[set.size()]));

        mCharCountries = new CharSequence[mCountries.size()];

        Locale locale = Locale.getDefault();

        mSelectedCountry = new Country(locale.getISO3Country(), locale.getCountry(), locale.getDisplayCountry());

        mCountryText.setText(mSelectedCountry.name);

        mPhoneNumberCode.setText("+" + Integer.toString(util.getCountryCodeForRegion(mSelectedCountry.code)));

        for (int i = 0; i < mCountries.size(); i++) {
            mCharCountries[i] = mCountries.get(i).name;
        }

        if (this.getArguments() != null && this.getArguments().containsKey("hasFile")) {
            mHasFile = this.getArguments().getBoolean("hasFile");
            if (mHasFile)
                mFileDataName = this.getArguments().getString("fileDataName");
        }

        return fragmentView;
    }

    @Override
    protected void initializeView(View view) {
        mPhoneNumberField = (EditText) view.findViewById(R.id.textfield_phonenumber);
        mCountryText = (TextView) view.findViewById(R.id.tv_country_selector);
        mPhoneNumberCode = (TextView) view.findViewById(R.id.tv_phone_number_code);
        mRLCountrySelector = view.findViewById(R.id.rl_country_selector);

        mRLCountrySelector.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        mLoginBtn = (Button) view.findViewById(R.id.submit_login);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                processLogin();
            }
        });
    }

    public void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Your Country");
        builder.setSingleChoiceItems(mCharCountries, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mSelectedCountry = mCountries.get(item);
                mCountryText.setText(mCountries.get(item).name);
                mPhoneNumberCode.setText("+" + Integer.toString(util.getCountryCodeForRegion(mSelectedCountry.code)));
                countriesDialog.dismiss();
            }
        });
        countriesDialog = builder.create();
        countriesDialog.show();

    }

    private void processLogin() {
        if (HollerbackApplication.getInstance().regId == null) {
            Toast.makeText(getActivity(), "Try again in a few seconds", Toast.LENGTH_LONG).show();
            return;
        }

        if (!verifyFields()) {
            return;
        }

        LogUtil.i("Logging in with: " + mPhoneNumberField.getText().toString());
        this.startLoading();
        HBRequestManager.postLogin(mRegistrationPhone, new HBAsyncHttpResponseHandler<LoginResponse>(new TypeReference<LoginResponse>() {
        }) {

            @Override
            public void onResponseSuccess(int statusCode, LoginResponse response) {
                onSignInSucceeded(response);

            }

            @Override
            public void onApiFailure(Metadata metaData) {
                if (metaData != null)
                    LogUtil.w(TAG, "error code: " + metaData.code);

            }
        });

    }

    private void onSignInSucceeded(LoginResponse response) {
        // process the sign in here!

        String userName = response.user.username;
        String phone = response.user.phone;
        long id = response.user.id;

        /**
         * Reason why I am doing this is because gingerbread does not have user.getstring("value", default)
         */
        PreferenceManagerUtil.setPreferenceValue(HollerbackPreferences.USERNAME, userName);

        PreferenceManagerUtil.setPreferenceValue(HollerbackPreferences.PHONE, phone);

        PreferenceManagerUtil.setPreferenceValue(HollerbackPreferences.ID, id);

        LogUtil.i("HB", response.toString()); // TODO - Sajjad determine whether to use LogUtil or replace it.
        SignInFragment.this.startLoading(); // TODO - Sajjad, what's going on here with startLoading?
        SignUpConfirmFragment fragment = SignUpConfirmFragment.newInstance(false, mFileDataName);
        mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(SignUpConfirmFragment.class.getSimpleName()).commitAllowingStateLoss();
    }

    private PhoneNumber getPhoneNumber() {
        if (mPhoneNumberField.getText().toString() == null || mPhoneNumberField.getText().toString().trim().isEmpty() || mPhoneNumberField.getText().toString().trim().length() < 3) {
            return null;
        }
        return NumberUtil.getPhoneNumber("+" + util.getCountryCodeForRegion(mSelectedCountry.code) + mPhoneNumberField.getText().toString());
    }

    public boolean verifyFields() {
        String validPhone = TextValidator.isValidPhone(getPhoneNumber());

        boolean valid = (validPhone == null);

        if (!valid) {
            String message = (validPhone != null ? "\n" + validPhone : "");

            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        } else {
            mRegistrationPhone = "+" + util.getCountryCodeForRegion(mSelectedCountry.code) + mPhoneNumberField.getText().toString();
            LogUtil.i("Signing up with: " + mRegistrationPhone);
        }

        return valid;

    }
}
