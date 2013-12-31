package com.moziy.hollerback.view.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.moziy.hollerback.view.FontManager;

public class CustomPreference extends Preference {

    public CustomPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTypeface(FontManager.getFont(FontManager.Font.GOTHAM_ROUND_MEDIUM));
        titleView.setTextColor(getContext().getResources().getColorStateList(com.moziy.hollerback.R.drawable.grey_font));

        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        summaryView.setTypeface(FontManager.getFont(FontManager.Font.GOTHAM_ROUND_MEDIUM));
        summaryView.setTextColor(getContext().getResources().getColorStateList(com.moziy.hollerback.R.drawable.grey_font));

    }
}
