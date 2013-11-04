package com.moziy.hollerback.adapter;

import java.io.InputStream;
import java.util.HashMap;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.moziy.hollerback.R;
import com.moziy.hollerback.model.SMSContact;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SMSAdapter extends SimpleCursorAdapter implements Filterable {
    private int mResId;
    LayoutInflater mInflater;
    private HashMap<String, String> mSelected;

    public SMSAdapter(Context context, int layout, Cursor c, String[] from, int[] to, HashMap<String, String> selected) {
        super(context, layout, c, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mInflater = LayoutInflater.from(context);
        this.mResId = layout;
        mSelected = selected;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mInflater.inflate(this.mResId, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        PhoneNumberUtil phoneutil = PhoneNumberUtil.getInstance();
        // String phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
        // PhoneNumber number = phoneutil.parse(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)), phoneutil.getRegionCodeForNumber(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));

        String subtitle = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));

        ImageView iv = (ImageView) view.findViewById(R.id.lazyIcon);

        Bitmap photo = loadContactPhoto(context.getContentResolver(), Long.valueOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.Photo.CONTACT_ID))));
        if (photo != null) {
            iv.setImageBitmap(loadContactPhoto(context.getContentResolver(), Long.valueOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.Photo.CONTACT_ID)))));
        } else {
            // Change this part later to default android icon
            iv.setImageResource(R.drawable.icon_male);
        }

        TextView bTitle = (TextView) view.findViewById(R.id.lazyTitle);
        bTitle.setText(title);

        TextView bSubTitle = (TextView) view.findViewById(R.id.lazySubTitle);
        bSubTitle.setText(subtitle);

        CheckBox chkSelected = (CheckBox) view.findViewById(R.id.chkSelected);
        chkSelected.setChecked(false);

        if (mSelected.containsKey(title)) {
            chkSelected.setChecked(true);
        }

    }

    public static Bitmap loadContactPhoto(ContentResolver cr, long id) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null) {
            return null;
        }
        return BitmapFactory.decodeStream(input);
    }

    @Override
    public SMSContact getItem(int position) {
        Cursor cursor = this.getCursor();
        cursor.moveToPosition(position);
        SMSContact ret = new SMSContact(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)), cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
        return ret;
    }
}
