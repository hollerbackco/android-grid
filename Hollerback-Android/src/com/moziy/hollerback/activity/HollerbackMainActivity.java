package com.moziy.hollerback.activity;

import java.util.List;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;
import com.moziy.hollerback.R;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.fragment.ConversationListFragment;
import com.moziy.hollerback.fragment.workers.ConversationWorkerFragment;
import com.moziy.hollerback.fragment.workers.ConversationWorkerFragment.OnConversationsUpdated;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.FlurryC;
import com.moziy.hollerback.util.HollerbackAppState;

public class HollerbackMainActivity extends SherlockFragmentActivity implements OnConversationsUpdated {

    private static final String TAG = HollerbackMainActivity.class.getSimpleName();
    private List<ConversationModel> mConversations; // list of conversations
    boolean initFrag = false;
    String convId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Example);
        super.onCreate(savedInstanceState);

        Fragment worker = getSupportFragmentManager().findFragmentByTag(ConversationWorkerFragment.FRAGMENT_TAG);
        if (worker == null) {
            worker = new ConversationWorkerFragment();
            getSupportFragmentManager().beginTransaction().add(worker, ConversationWorkerFragment.FRAGMENT_TAG).commit();
        }

        if (AppEnvironment.getInstance().LOG_CRASHES) {
            Crittercism.init(getApplicationContext(), AppEnvironment.getInstance().CRITTERCISM_ID);
        }

        LogUtil.i("Starting MainActivity");

        if (!HollerbackAppState.isValidSession()) {
            Intent i = new Intent(this, WelcomeFragmentActivity.class);
            startActivity(i);
            this.finish();
        }
        this.getSupportActionBar().show();

        setContentView(R.layout.hollerback_main);

        if (savedInstanceState == null)
            initFragment();
        LogUtil.i("Completed BaseActivity");

        FlurryAgent.onStartSession(this, AppEnvironment.getInstance().FLURRY_ID);

        FlurryAgent.logEvent(FlurryC.EVENT_STARTSESSION,
                AnalyticsUtil.getMap(FlurryC.PARAM_MODELNAME, AnalyticsUtil.getDeviceName(), FlurryC.PARAM_OS_VERISON, Integer.toString(Build.VERSION.SDK_INT)));
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (!HollerbackAppState.isValidSession()) {
            Intent i = new Intent(this, WelcomeFragmentActivity.class);
            startActivity(i);
            this.finish();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        FlurryAgent.onEndSession(this);
        super.onStop();
    }

    public void initFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        int count = fragmentManager.getBackStackEntryCount();

        for (int i = 0; i < count; i++) {
            fragmentManager.popBackStackImmediate();
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ConversationListFragment fragment = new ConversationListFragment();
        fragmentTransaction.add(R.id.fragment_holder, fragment).addToBackStack(ConversationListFragment.FRAGMENT_TAG);
        fragmentTransaction.commit();

        fragmentManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {

                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    // TODO: evalute this or simply not adding the conversation list fragment to the backstack
                    Log.d(TAG, "finishing activity sine all fragments have been removed");
                    finish();
                }

            }
        });
    }

    public List<ConversationModel> getConversations() {
        return mConversations;
    }

    @Override
    public void onUpdate(List<ConversationModel> conversations) {
        mConversations = conversations;

    }
}
