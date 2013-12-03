package com.moziy.hollerback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import com.moziy.hollerback.R;
import com.moziy.hollerback.view.FontManager;

public class CustomButton extends Button {

    public CustomButton(Context context) {
        this(context, null);

    }

    public CustomButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            // retrieve the attributes pertaining to the custom textview
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomFont, defStyle, R.style.DefaultButton);
            setTypefaceFromAttrs(array);
            array.recycle();
        }
    }

    private void setTypefaceFromAttrs(TypedArray array) {
        String fontName = array.getString(R.styleable.CustomFont_typeface);
        Log.d("ct", "font name: " + fontName);
        if (fontName != null) {
            setTypeface(FontManager.getFont(fontName));
        }
    }
}
