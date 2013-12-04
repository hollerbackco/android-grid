package com.moziy.hollerback.fragment;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.gcm.GCMUtils;
import com.moziy.hollerback.model.Country;
import com.moziy.hollerback.util.ISOUtil;
import com.moziy.hollerback.util.JSONUtil;
import com.moziy.hollerback.util.NumberUtil;
import com.moziy.hollerback.validator.TextValidator;
import com.moziy.hollerbacky.connection.HBRequestManager;

public class SignUpFragment extends BaseFragment implements OnClickListener {

    public static final String EMAIL_BUNDLE_ARG_KEY = "EMAIL";
    public static final String PASSWORD_BUNDLE_ARG_KEY = "PASSWORD";
    private SherlockFragmentActivity mActivity;
    private EditText mNameField, mPhoneNumberField;

    private Button mSubmitButton;

    private View mRLCountrySelector;

    private TextView mCountryText, mPhoneNumberCode;

    private AlertDialog countriesDialog;

    private List<Country> mCountries;

    private CharSequence[] mCharCountries;

    private String mRegistrationName;
    private String mRegistrationPhone;

    private String mEmail;
    private String mPassword;

    // passing on
    private String mFileDataName;

    public static SignUpFragment newInstance(String fileDataName) {

        SignUpFragment f = new SignUpFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("fileDataName", fileDataName);
        f.setArguments(args);
        return f;
    }

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
    }

    @Override
    protected void initializeView(View view) {
        mNameField = (EditText) view.findViewById(R.id.textfield_name);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = this.getSherlockActivity();
        this.getSherlockActivity().getSupportActionBar().show();
        this.getSherlockActivity().getSupportActionBar().setTitle(R.string.create_account);
        this.getSherlockActivity().getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));

        // TODO Auto-generated method stub
        View fragmentView = inflater.inflate(R.layout.signup_fragment, null);
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

        // if it doesn't have this it will crash
        mFileDataName = this.getArguments().getString("fileDataName");

        return fragmentView;
    }

    public void processSubmit() {

        if (verifyFields()) {
            String regId = GCMUtils.getRegistrationId(HollerbackApplication.getInstance());
            if (!"".equals(regId)) {

                HBRequestManager.postRegistration(mRegistrationName, mRegistrationPhone, regId, new JsonHttpResponseHandler() {
                    @Override
                    protected Object parseResponse(String arg0) throws JSONException {
                        LogUtil.i(arg0);
                        return super.parseResponse(arg0);

                    }

                    @Override
                    public void onFailure(Throwable arg0, JSONObject response) {
                        // TODO Auto-generated method stub
                        super.onFailure(arg0, response);
                        LogUtil.i("LOGIN FAILURE");
                        if (response.has("meta")) {
                            // doesnt have user
                            try {
                                JSONObject metadata = response.getJSONObject("meta");
                                if (metadata.has("code") && metadata.getInt("code") == 400) {
                                    processLogin();
                                } else {
                                    Toast.makeText(mActivity, metadata.getString("msg"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onSuccess(int statusId, JSONObject response) {
                        // TODO Auto-generated method stub
                        super.onSuccess(statusId, response);
                        JSONUtil.processSignUp(response);
                        // has user
                        if (response.has("user")) {
                            SignUpConfirmFragment fragment = SignUpConfirmFragment.newInstance(true, mFileDataName);
                            mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(SignUpConfirmFragment.class.getSimpleName()).commitAllowingStateLoss();
                        }
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Try again in a few seconds", Toast.LENGTH_LONG).show();
            }
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

    private void processLogin() {
        if ("".equals(GCMUtils.getRegistrationId(HollerbackApplication.getInstance()))) {
            Toast.makeText(getActivity(), "Try again in a few seconds", Toast.LENGTH_LONG).show();
            return;
        }

        HBRequestManager.postLogin(mRegistrationPhone, new JsonHttpResponseHandler() {

            @Override
            protected Object parseResponse(String arg0) throws JSONException {
                LogUtil.i(arg0);
                return super.parseResponse(arg0);

            }

            @Override
            public void onFailure(Throwable arg0, JSONObject arg1) {
                // TODO Auto-generated method stub
                super.onFailure(arg0, arg1);
                LogUtil.i("LOGIN FAILURE");
            }

            @Override
            public void onSuccess(int statusId, JSONObject response) {
                // TODO Auto-generated method stub
                super.onSuccess(statusId, response);
                JSONUtil.processSignIn(response);
                LogUtil.i("HB", response.toString());

                SignUpConfirmFragment fragment = SignUpConfirmFragment.newInstance(true, mFileDataName);
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(SignUpConfirmFragment.class.getSimpleName()).commitAllowingStateLoss();
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

    private PhoneNumber getPhoneNumber() {
        if (mPhoneNumberField.getText().toString() == null || mPhoneNumberField.getText().toString().trim().isEmpty() || mPhoneNumberField.getText().toString().trim().length() < 3) {
            return null;
        }
        return NumberUtil.getPhoneNumber("+" + util.getCountryCodeForRegion(mSelectedCountry.code) + mPhoneNumberField.getText().toString());
    }

    public boolean verifyFields() {
        String validPhone = TextValidator.isValidPhone(getPhoneNumber());

        String validName = TextValidator.isValidName(mNameField.getText().toString());

        boolean valid = (validPhone == null && validName == null);

        if (!valid) {
            String message = (validName != null ? validName + "\n" : "") + (validPhone != null ? "\n" + validPhone : "");

            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        } else {
            mRegistrationName = mNameField.getText().toString();
            mRegistrationPhone = "+" + util.getCountryCodeForRegion(mSelectedCountry.code) + mPhoneNumberField.getText().toString();
            LogUtil.i("Signing up with: " + mRegistrationName + " " + mRegistrationPhone);
        }

        return valid;

    }
}
