package com.moziy.hollerback.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.util.validators.ValidatorUtil;
import com.moziy.hollerback.widget.CustomButton;
import com.moziy.hollerback.widget.CustomEditText;

public class SignupUserFragment extends BaseFragment {
    private static final String TAG = SignupUserFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG = TAG;

    private CustomEditText mEmailEditText;
    private CustomEditText mPasswordEditText;
    private CustomButton mNextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.signup));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = this.getSherlockActivity();

        View v = inflater.inflate(R.layout.signup_email_layout, null);
        initializeView(v);

        return v;
    }

    @Override
    protected void initializeView(View view) {

        mEmailEditText = (CustomEditText) view.findViewById(R.id.et_email);
        mEmailEditText.setHint(getString(R.string.email));

        mPasswordEditText = (CustomEditText) view.findViewById(R.id.et_password);

        mPasswordEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (mEmailEditText.length() > 0 && s.length() > 0) {
                    mNextButton.setVisibility(View.VISIBLE);
                } else {
                    mNextButton.setVisibility(View.GONE);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mNextButton = (CustomButton) view.findViewById(R.id.bt_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (verifyFields()) {
                    Fragment f = SignUpFragment.newInstance(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
                    getFragmentManager().beginTransaction().replace(R.id.fragment_holder, f).addToBackStack(FRAGMENT_TAG).commit();

                }

            }
        });

        ((TextView) view.findViewById(R.id.tv_eula)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    // XXX: refactor this into it's own utility
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
        status &= ValidatorUtil.isValidEmail(mEmailEditText.getText());

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
