package com.moziy.hollerback.fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.moziy.hollerback.R;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class WelcomeFragment extends BaseFragment {
    private SherlockFragmentActivity mActivity;

    private TextView mSignInBtn;
    private ImageButton mBtnRecord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (SherlockFragmentActivity) this.getActivity();
        mActivity.getSupportActionBar().hide();
        View fragmentView = inflater.inflate(R.layout.welcome_fragment, null);

        initializeView(fragmentView);
        return fragmentView;
    }

    @Override
    protected void initializeView(View view) {
        // TODO Auto-generated method stub
        mSignInBtn = (TextView) view.findViewById(R.id.btn_signin);
        mSignInBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startSignInFragment();
            }
        });

        mBtnRecord = (ImageButton) view.findViewById(R.id.btnRecord);
        mBtnRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WelcomeRecordVideoFragment fragment = WelcomeRecordVideoFragment.newInstance();
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(WelcomeRecordVideoFragment.class.getSimpleName()).commitAllowingStateLoss();
            }
        });
    }

    public static WelcomeFragment newInstance(int num) {

        WelcomeFragment f = new WelcomeFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
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
}
