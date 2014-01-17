package com.moziy.hollerback.fragment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.SmsUtil;

public class SmsDialogFragment extends android.support.v4.app.DialogFragment {

    public static final String CONTACTS_BUNDLE_ARG_KEY = "CONTACTS";
    public static final String IMAGE_URI_BUNDLE_ARG_KEY = "IMAGE_URI";
    public static final String MSG_BODY_BUNDLE_ARG_KEY = "MSG_BODY";

    private Button mSendBtn;
    private TextView mRecipientTv;
    private EditText mMessageEt;

    private List<Contact> mContacts;
    private Uri mImageUri;
    private String mBody;

    public static SmsDialogFragment newInstance(ArrayList<Contact> contacts, Uri imageUri, String body) {

        SmsDialogFragment f = new SmsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(CONTACTS_BUNDLE_ARG_KEY, contacts);
        args.putParcelable(IMAGE_URI_BUNDLE_ARG_KEY, imageUri);
        args.putString(MSG_BODY_BUNDLE_ARG_KEY, body);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setStyle(0, android.R.style.Theme_Holo_Dialog_NoActionBar);
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mContacts = (List<Contact>) args.getSerializable(CONTACTS_BUNDLE_ARG_KEY);
        mImageUri = args.getParcelable(IMAGE_URI_BUNDLE_ARG_KEY);
        mBody = args.getString(MSG_BODY_BUNDLE_ARG_KEY);
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
                SmsUtil.sendSms(mContacts, mImageUri, mBody);

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
                d = new BitmapDrawable(getResources(), new FileInputStream(mImageUri.getPath()));
                d.setBounds(0, 0, 200, 200);
                mMessageEt.setCompoundDrawables(null, d, null, null);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // new BitmapDrawable(getResources(), new FileInputStream(mImageUri.getPath()))
        }
        mMessageEt.setText(mBody);

    }
}
