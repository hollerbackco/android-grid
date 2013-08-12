package com.moziy.hollerback.adapter;

import java.io.InputStream;
import java.util.List;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SMSAdapter extends SimpleCursorAdapter implements Filterable{
    private int mResId;
    LayoutInflater mInflater;
    private List<SMSContact> mSelected;
    
	public SMSAdapter(Context context, int layout, Cursor c, String[] from, int[] to, List<SMSContact> selected)  {
		super(context, layout, c, from, to);
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
		String title = cursor.getString( cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		String subtitle = "";
	       
		subtitle = cursor.getString( cursor.getColumnIndex(Phone.NUMBER));
 	   	
		ImageView iv = (ImageView)view.findViewById(R.id.lazyIcon);

 	   	Bitmap photo = loadContactPhoto(context.getContentResolver(), Long.valueOf( cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.Photo.CONTACT_ID))));
 	   	if(photo != null)
 	   	{
     	   iv.setImageBitmap(loadContactPhoto(context.getContentResolver(), Long.valueOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.Photo.CONTACT_ID)))));    		   
 	   	}
 	   	else
 	   	{
 		   //Change this part later to default android icon
 		   iv.setImageResource(R.drawable.icon_male);
 	   	}

        TextView bTitle = (TextView) view.findViewById(R.id.lazyTitle);
        bTitle.setText(title);
        
        TextView bSubTitle = (TextView) view.findViewById(R.id.lazySubTitle);
        bSubTitle.setText(subtitle);
        
        ImageView imgOn = (ImageView) view.findViewById(R.id.imgOn);
        ImageView imgOff = (ImageView) view.findViewById(R.id.imgOff);
        
    	imgOn.setVisibility(View.GONE);
    	imgOff.setVisibility(View.VISIBLE);
    	
        for(int i = 0; i < mSelected.size(); i++)
        {
        	if(mSelected.get(i).getSMS().equalsIgnoreCase(subtitle))
        	{
            	imgOn.setVisibility(View.VISIBLE);
            	imgOff.setVisibility(View.GONE);
            	break;
        	}
        }
	} 
	
	public static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
	    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
	    if (input == null) {
	        return null;
	    }
	    return BitmapFactory.decodeStream(input);
	}
}