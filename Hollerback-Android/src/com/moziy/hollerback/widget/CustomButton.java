package com.moziy.hollerback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import com.moziy.hollerback.R;
import com.moziy.hollerback.view.FontManager;

// TODO - Sajjad: Figure out if we can create a tint on a background resource that's a drawable
// I know that ColorDrawable's don't take a color filter
public class CustomButton extends Button {

    private Drawable mTint;
    private Drawable mBg;

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

        mTint = new ColorDrawable(Color.parseColor("#80000000"));
    }

    private void setTypefaceFromAttrs(TypedArray array) {
        String fontName = array.getString(R.styleable.CustomFont_typeface);
        if (fontName != null) {
            setTypeface(FontManager.getFont(fontName));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mBg = getBackground();
            setBackgroundDrawable(mTint);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d("cb", "clear color filter");
            setBackgroundDrawable(mBg);
        }

        return super.onTouchEvent(event);
    }
}
