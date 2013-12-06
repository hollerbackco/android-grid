package com.moziy.hollerback.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.moziy.hollerback.R;
import com.moziy.hollerback.fragment.SignUpConfirmFragment;
import com.moziy.hollerback.fragment.WelcomeFragment;
import com.moziy.hollerback.util.HBPreferences;
import com.moziy.hollerback.util.PreferenceManagerUtil;

public class WelcomeFragmentActivity extends BaseActivity {
    private static final String TAG = WelcomeFragmentActivity.class.getSimpleName();

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        setTheme(R.style.Hollerback);
        super.onCreate(bundle);
        setContentView(R.layout.welcome_fragment_activity);

        initFragment();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // TODO Abstract to fragment manager
    public void initFragment() {

        // check to see whether the user is registered or not
        if (PreferenceManagerUtil.getPreferenceValue(HBPreferences.PHONE, null) != null && !PreferenceManagerUtil.getPreferenceValue(HBPreferences.IS_VERIFIED, false)) {
            // load the verification step
            Log.d(TAG, "user isn't verified");
            SignUpConfirmFragment f = SignUpConfirmFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_holder, f).commit();

            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        WelcomeFragment fragment = new WelcomeFragment();
        fragmentTransaction.add(R.id.fragment_holder, fragment);
        // fragmentTransaction.addToBackStack(WelcomeFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

}
