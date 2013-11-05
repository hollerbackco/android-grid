package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moziy.hollerback.R;

public class StartConversationFragment extends BaseFragment {
    public static final String FRAGMENT_TAG = StartConversationFragment.class.getSimpleName();
    private static final String PHONES_BUNDLE_ARG_KEY = "phones";
    private static final String TITLE_BUNDLE_ARG_KEY = "title";

    public static StartConversationFragment newInstance(String[] phones, String title) {
        StartConversationFragment f = new StartConversationFragment();
        Bundle params = new Bundle();
        params.putStringArray(PHONES_BUNDLE_ARG_KEY, phones);
        params.putString(TITLE_BUNDLE_ARG_KEY, title);
        f.setArguments(params);
        return f;
    }

    private String[] mPhones;
    private String mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTitle = args.getString(TITLE_BUNDLE_ARG_KEY);
        mPhones = args.getStringArray(PHONES_BUNDLE_ARG_KEY);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.start_conversation_layout, container, false);

        ((TextView) v.findViewById(R.id.tv_title)).setText(mTitle);

        v.findViewById(R.id.ib_start_video).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                RecordVideoFragment f = RecordVideoFragment.newInstance(mPhones, mTitle);
                // go to the video fragment
                getFragmentManager().beginTransaction().replace(R.id.fragment_holder, f).commitAllowingStateLoss();
            }
        });

        return v;
    }

    @Override
    protected void initializeView(View view) {

    }

}
