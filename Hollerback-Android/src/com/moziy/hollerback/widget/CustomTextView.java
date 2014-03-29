package com.moziy.hollerback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.moziy.hollerback.R;
import com.moziy.hollerback.view.FontManager;

public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        this(context, null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            // retrieve the attributes pertaining to the custom textview
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomFont, defStyle, R.style.DefaultTextView);
            setTypefaceFromAttrs(array);
            array.recycle(); // recycle the array
        }
    }

    private void setTypefaceFromAttrs(TypedArray array) {
        String fontName = array.getString(R.styleable.CustomFont_typeface);
        if (fontName != null) {
            setTypeface(FontManager.getFont(fontName));
        }
    }

}
