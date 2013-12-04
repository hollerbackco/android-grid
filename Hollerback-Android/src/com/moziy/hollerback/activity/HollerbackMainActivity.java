package com.moziy.hollerback.activity;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.crittercism.app.Crittercism;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.fragment.ConversationListFragment;
import com.moziy.hollerback.fragment.workers.ConversationWorkerFragment;
import com.moziy.hollerback.fragment.workers.ConversationWorkerFragment.OnConversationsUpdated;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.HollerbackAppState;

public class HollerbackMainActivity extends BaseActivity implements OnConversationsUpdated {

    private static final String TAG = HollerbackMainActivity.class.getSimpleName();
    private List<ConversationModel> mConversations; // list of conversations
    boolean initFrag = false;
    String convId = null;
    private InternalReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Hollerback);
        super.onCreate(savedInstanceState);

        registerBroadcasts();

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
            return;
        }
        this.getSupportActionBar().show();

        setContentView(R.layout.hollerback_main);

        if (savedInstanceState == null)
            initFragment();
        LogUtil.i("Completed BaseActivity");

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
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IABroadcastManager.unregisterLocalReceiver(mReceiver);
    }

    public void initFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        int count = fragmentManager.getBackStackEntryCount();

        for (int i = 0; i < count; i++) {
            fragmentManager.popBackStackImmediate();
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ConversationListFragment fragment = new ConversationListFragment();
        // fragmentTransaction.add(R.id.fragment_holder, fragment).addToBackStack(ConversationListFragment.FRAGMENT_TAG).commit();
        fragmentTransaction.add(R.id.fragment_holder, fragment).commit();

    }

    private void registerBroadcasts() {
        mReceiver = new InternalReceiver();
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.AUTH_EXCEPTION);
    }

    public List<ConversationModel> getConversations() {
        return mConversations;
    }

    @Override
    public void onUpdate(List<ConversationModel> conversations) {
        mConversations = conversations;

    }

    private class InternalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IABIntent.AUTH_EXCEPTION)) {
                Log.w(TAG, "auth failure! ask user to re-login");
                Intent startActivityIntent = new Intent();
                startActivityIntent.setClass(HollerbackMainActivity.this, WelcomeFragmentActivity.class);
                startActivity(startActivityIntent);
                finish();

            }

        }

    }
}
