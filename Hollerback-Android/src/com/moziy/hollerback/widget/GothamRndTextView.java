package com.moziy.hollerback.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class GothamRndTextView extends TextView {

    Context context;
    String ttfName;

    String TAG = getClass().getName();

    public GothamRndTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        if (!this.isInEditMode()) {
            Typeface normalTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Gotham-Medium.otf");
            Typeface boldTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Gotham-Medium.otf");

            if (style == Typeface.BOLD) {
                super.setTypeface(boldTypeface);
            } else {
                super.setTypeface(normalTypeface);
            }
        }
    }
}
