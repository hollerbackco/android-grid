package com.moziy.hollerback.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.connection.HBAsyncHttpResponseHandler;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.gcm.GCMUtils;
import com.moziy.hollerback.model.web.Envelope.Metadata;
import com.moziy.hollerback.model.web.response.LoginResponse;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;
import com.moziy.hollerback.widget.CustomEditText;

public class SignInFragment extends BaseFragment {

    private static final String TAG = SignInFragment.class.getSimpleName();

    private SherlockFragmentActivity mActivity;

    private Button mLoginBtn;
    private CustomEditText mEmailEditText;
    private CustomEditText mPasswordEditText;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSherlockActivity().getSupportActionBar().show();
        this.getSherlockActivity().getSupportActionBar().setTitle(R.string.signin);
        this.getSherlockActivity().getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_solid_example));
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

        mActivity = this.getSherlockActivity();

        View v = inflater.inflate(R.layout.signin_fragment, null);
        initializeView(v);

        return v;
    }

    @Override
    protected void initializeView(View view) {

        mEmailEditText = (CustomEditText) view.findViewById(R.id.et_email);

        mPasswordEditText = (CustomEditText) view.findViewById(R.id.et_password);
        mPasswordEditText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    processLogin();
                }

                return false;
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

    private void processLogin() {
        if ("".equals(GCMUtils.getRegistrationId(HollerbackApplication.getInstance()))) {
            Toast.makeText(HollerbackApplication.getInstance(), "Try again in a few seconds", Toast.LENGTH_LONG).show();
            return;
        }

        if (!verifyFields()) {
            return;
        }

        this.startLoading();

        HBRequestManager.postLogin(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString(), GCMUtils.getRegistrationId(HollerbackApplication.getInstance()),
                new HBAsyncHttpResponseHandler<LoginResponse>(new TypeReference<LoginResponse>() {
                }) {

                    @Override
                    public void onResponseSuccess(int statusCode, LoginResponse response) {
                        SignInFragment.this.stopLoading();
                        onSignInSucceeded(response);

                    }

                    @Override
                    public void onApiFailure(Metadata metaData) {
                        SignInFragment.this.stopLoading();
                        if (metaData != null)
                            LogUtil.w(TAG, "error code: " + metaData.code);

                        if (isAdded()) { // TODO: add custom dialogs
                            // TODO - sajjad: launch a popup saying that login failed
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                            builder.setTitle(getString(R.string.error_oops));
                            builder.setMessage(getString(R.string.error_login));
                            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (isAdded())
                                        dialog.dismiss();

                                }
                            }).show();

                        }
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
        PreferenceManagerUtil.setPreferenceValue(HBPreferences.USERNAME, userName);

        PreferenceManagerUtil.setPreferenceValue(HBPreferences.PHONE, phone);

        PreferenceManagerUtil.setPreferenceValue(HBPreferences.ID, id);

        PreferenceManagerUtil.setPreferenceValue(HBPreferences.ACCESS_TOKEN, response.access_token);

        LogUtil.i("HB", response.toString()); // TODO - Sajjad determine whether to use LogUtil or replace it.
        Intent intent = new Intent(mActivity, HollerbackMainActivity.class);
        mActivity.startActivity(intent);
        mActivity.finish();
    }

    private AlertDialog getErrorDialog(String title, String message, String positiveText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isAdded())
                    dialog.dismiss();

            }
        });
        return builder.create();
    }

    private boolean verifyFields() {

        boolean status = true;
        // status &= ValidatorUtil.isValidEmail(mEmailEditText.getText());

        if (!status) {
            getErrorDialog(getString(R.string.error_oops), getString(R.string.error_email), getString(R.string.ok)).show();
            return false;
        }

        status &= mPasswordEditText.getText() != null && mPasswordEditText.getText().length() > 0;
        if (!status) {
            getErrorDialog(getString(R.string.error_oops), getString(R.string.error_password), getString(R.string.ok)).show();
        }

        return status;// mPasswordEditText.getText() != null && mPasswordEditText.getText().length() > 0;

    }

}
