package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.MapBuilder;
import com.moziy.hollerback.R;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.widget.CustomButton;

public class WelcomeFragment extends BaseFragment {
    private static final String TAG = WelcomeFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    private SherlockFragmentActivity mActivity;

    private CustomButton mSignInBtn;
    private CustomButton mSignUpBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check to see whether the user is registered or not

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (SherlockFragmentActivity) this.getActivity();
        mActivity.getSupportActionBar().hide();
        View fragmentView = inflater.inflate(R.layout.welcome_layout, null);

        initializeView(fragmentView);
        return fragmentView;
    }

    @Override
    protected void initializeView(View view) {
        // TODO Auto-generated method stub
        mSignInBtn = (CustomButton) view.findViewById(R.id.bt_signin);
        mSignInBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startSignInFragment();
            }
        });

        mSignUpBtn = (CustomButton) view.findViewById(R.id.bt_signup);
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // if (PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, null) != null && !PreferenceManagerUtil.getPreferenceValue(HBPreferences.IS_VERIFIED, false)) {
                // // load the verification step
                // Log.d(TAG, "user isn't verified");
                // SignUpConfirmFragment f = SignUpConfirmFragment.newInstance();
                // getFragmentManager().beginTransaction().replace(R.id.fragment_holder, f).addToBackStack(FRAGMENT_TAG).commit();
                //
                // return;
                // } else {

                AnalyticsUtil.getGaTracker().send(MapBuilder.createEvent(AnalyticsUtil.Category.Registration, AnalyticsUtil.Action.EnteredSignUp, null, null).build());

                SignupUserFragment f = new SignupUserFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_holder, f).addToBackStack(FRAGMENT_TAG).commit();
                // }
            }
        });

    }

    public static WelcomeFragment newInstance() {

        WelcomeFragment f = new WelcomeFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    public void startSignInFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignInFragment fragment = SignInFragment.newInstance();
        fragmentTransaction.replace(R.id.fragment_holder, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(SignInFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    protected String getScreenName() {
        return AnalyticsUtil.ScreenNames.WELCOME;
    }
}
