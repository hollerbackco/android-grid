package com.moziy.hollerback.view.preference;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.moziy.hollerback.view.FontManager;

public class CustomPreferenceCategory extends PreferenceCategory {

    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CustomPreferenceCategory(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTypeface(FontManager.getFont(FontManager.Font.GOTHAM_MEDIUM));
        titleView.setTextColor(getContext().getResources().getColorStateList(com.moziy.hollerback.R.drawable.grey_font));

    }

}
