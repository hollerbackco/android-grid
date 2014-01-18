package com.moziy.hollerback.fragment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.SmsUtil;

public class SmsFragment extends BaseFragment {
    private static final String TAG = SmsFragment.class.getSimpleName();
    public static final String CONTACTS_BUNDLE_ARG_KEY = "CONTACTS";
    public static final String IMAGE_URI_BUNDLE_ARG_KEY = "IMAGE_URI";
    public static final String MSG_BODY_BUNDLE_ARG_KEY = "MSG_BODY";

    private Button mSendBtn;
    private TextView mRecipientTv;
    private EditText mMessageEt;
    private ProgressDialog mProgressDialog;

    private List<Contact> mContacts;
    private Uri mImageUri;
    private String mBody;
    private InputMethodManager mInputManager;

    public static SmsFragment newInstance(ArrayList<Contact> contacts, Uri imageUri, String body) {

        SmsFragment f = new SmsFragment();
        Bundle args = new Bundle();
        args.putSerializable(CONTACTS_BUNDLE_ARG_KEY, contacts);
        args.putParcelable(IMAGE_URI_BUNDLE_ARG_KEY, imageUri);
        args.putString(MSG_BODY_BUNDLE_ARG_KEY, body);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sms_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            returnToConvoList();
            return true;
        } else if (item.getItemId() == R.id.mi_send_sms) {
            if (isAdded()) {

                StringBuilder sb = new StringBuilder();
                for (Contact c : mContacts) {
                    for (String phone : c.mPhones) {
                        sb.append(phone).append(";");
                    }
                }

                Log.d(TAG, "destination: " + sb.toString());

                SmsUtil.sendSms(getActivity(), sb.toString(), mImageUri, mBody);
                returnToConvoList();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        mContacts = (List<Contact>) args.getSerializable(CONTACTS_BUNDLE_ARG_KEY);
        mImageUri = args.getParcelable(IMAGE_URI_BUNDLE_ARG_KEY);
        mBody = args.getString(MSG_BODY_BUNDLE_ARG_KEY);
        mProgressDialog = new ProgressDialog(getActivity());
        mInputManager = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sms_dialog, container, false);
        mSendBtn = (Button) v.findViewById(R.id.bt_send);
        mRecipientTv = (TextView) v.findViewById(R.id.tv_recipients);
        mMessageEt = (EditText) v.findViewById(R.id.et_sms_message);

        mSendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMessageEt.clearFocus();
                mInputManager.hideSoftInputFromWindow(mMessageEt.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                if (isAdded()) {

                    StringBuilder sb = new StringBuilder();
                    for (Contact c : mContacts) {
                        for (String phone : c.mPhones) {
                            sb.append(phone).append(";");
                        }
                    }

                    Log.d(TAG, "destination: " + sb.toString());

                    SmsUtil.sendSms(getActivity(), sb.toString(), mImageUri, mBody);
                    returnToConvoList();

                }

            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        StringBuilder sb = new StringBuilder();
        for (Contact c : mContacts) {
            sb.append(c.mName).append(", ");
        }

        sb.deleteCharAt(sb.length() - 2);
        mRecipientTv.setText(sb.toString());

        if (mImageUri != null) {
            Log.d("smsdialog", "path: " + mImageUri.getPath());
            BitmapDrawable d;
            try {
                int imageSize = getResources().getDimensionPixelSize(R.dimen.dim_100dp);
                d = new BitmapDrawable(getResources(), new FileInputStream(mImageUri.getPath()));
                d.setBounds(0, 0, imageSize, imageSize);
                mMessageEt.setCompoundDrawables(null, d, null, null);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // new BitmapDrawable(getResources(), new FileInputStream(mImageUri.getPath()))
        }
        mMessageEt.setText(mBody);
        mMessageEt.requestFocus();
        mInputManager.showSoftInput(mMessageEt, InputMethodManager.SHOW_IMPLICIT);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void returnToConvoList() {

        Fragment f = getFragmentManager().findFragmentByTag(ConversationListFragment.FRAGMENT_TAG);

        if (f == null) {
            Log.d(TAG, "ConversationListFragment not found");
            // TODO - Sajjad: Delay the popping until after we've shown the sent icon
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // go back to the conversation fragment, popping everything

            f = ConversationListFragment.newInstance();
            getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom).replace(R.id.fragment_holder, f).commit();
        } else {
            Log.d(TAG, "ConversationListFragment found!");
            getFragmentManager().popBackStack(ConversationListFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    protected String getActionBarTitle() {
        return "Send SMS";
    }

    @Override
    protected String getScreenName() {

        return null;
    }
}
