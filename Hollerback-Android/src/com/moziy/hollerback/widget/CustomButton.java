package com.moziy.hollerback.widget;

import android.content.Context;
import android.content.res.TypedArray;
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
    private static final int[] STATE_ACTIVE_MODE = {
        R.attr.emphasized
    };
    private boolean mIsEmphasized = false;

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

            array = context.obtainStyledAttributes(attrs, R.styleable.CustomButton, defStyle, R.style.DefaultButton);
            int color = array.getColor(R.styleable.CustomButton_tintColor, -1);
            if (color > -1) {
                mTint = new ColorDrawable(color);
            }
            array.recycle();

        }

    }

    private void setTypefaceFromAttrs(TypedArray array) {
        String fontName = array.getString(R.styleable.CustomFont_typeface);
        if (fontName != null) {
            setTypeface(FontManager.getFont(fontName));
        }
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mIsEmphasized)
            mergeDrawableStates(drawableState, STATE_ACTIVE_MODE);

        return drawableState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mTint != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mBg = getBackground();
                setBackgroundDrawable(mTint);
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("cb", "clear color filter");
                setBackgroundDrawable(mBg);
            }
        }

        return super.onTouchEvent(event);
    }

    public void setEmphasized(boolean emphasized) {

        if (mIsEmphasized != emphasized) {
            mIsEmphasized = emphasized;
            refreshDrawableState();
        }

    }
}
