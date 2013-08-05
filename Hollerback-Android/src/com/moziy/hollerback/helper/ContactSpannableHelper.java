package com.moziy.hollerback.helper;

import com.moziy.hollerback.R;
import com.moziy.hollerback.util.FontUtil;
import com.moziy.hollerback.util.HollerbackConstants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.EditText;
import android.widget.TextView;

public class ContactSpannableHelper {

	public void addContactName(EditText editText, Context context, String name,
			String phoneNumber) {
		View parent = LayoutInflater.from(context).inflate(
				R.layout.contact_item_chip, null);
		TextView textView = (TextView) parent.findViewById(R.id.tv_chips_name);
		textView.setTypeface(FontUtil.MuseoSans_500);
		textView.setText(name);
		textView.setBackgroundResource(R.drawable.chips_edittext_gb);
		// textView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
		// R.drawable.icon_cross, 0);

		BitmapDrawable dd = (BitmapDrawable) getDrawableFromTextView(textView);
		// editText.setText(addSmily(dd));
		// editText.append(addNameDrawable(dd));
		// editText.append(addNameDrawable(dd));

		editText.getEditableText().insert(editText.getSelectionStart(),
				addNameDrawable(dd, phoneNumber));
	}

	// convert image to spannableString
	public SpannableStringBuilder addNameDrawable(Drawable dd, String phone) {
		dd.setBounds(0, 0, dd.getIntrinsicWidth(), dd.getIntrinsicHeight());
		SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append(HollerbackConstants.PHONE_PRE + phone
				+ HollerbackConstants.PHONE_SUF);
		builder.setSpan(
				new ImageSpan(dd),
				builder.length()
						- (HollerbackConstants.PHONE_PRE + phone + HollerbackConstants.PHONE_SUF)
								.length(), builder.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return builder;
	}

	// convert view to drawable
	public static Object getDrawableFromTextView(View view) {

		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(spec, spec);
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		view.setDrawingCacheEnabled(true);
		Bitmap cacheBmp = view.getDrawingCache();
		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
		view.destroyDrawingCache();
		return new BitmapDrawable(viewBmp);

	}
}
