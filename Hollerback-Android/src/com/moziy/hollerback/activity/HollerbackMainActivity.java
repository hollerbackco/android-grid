package com.moziy.hollerback.activity;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.R;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.contacts.ContactsDelegate;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.fragment.ContactsFragment;
import com.moziy.hollerback.fragment.ContactsFragment.NextAction;
import com.moziy.hollerback.fragment.ConversationListFragment;
import com.moziy.hollerback.fragment.WelcomeFragment;
import com.moziy.hollerback.fragment.workers.ConversationWorkerFragment.OnConversationsUpdated;
import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.service.task.Task;

public class HollerbackMainActivity extends BaseActivity implements OnConversationsUpdated, TaskClient {

    private static final String TAG = HollerbackMainActivity.class.getSimpleName();
    private List<ConversationModel> mConversations; // list of conversations
    boolean initFrag = false;
    String convId = null;
    private InternalReceiver mReceiver;
    private ContactsDelegate mContactsDelegate; // handles all operations for retrieving and storing contacts
    private TextView mActionBarTitle;
    private TextView mActionBarSubTitle;

    private boolean mLaunchWelcome = false;
    private boolean mLaunchInviteFriends = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContactsDelegate = new ContactsDelegate(this);
        super.onCreate(savedInstanceState);

        setupActionBar();

        mContactsDelegate.initWorkers();

        setContentView(R.layout.hollerback_main);

        registerBroadcasts();

        // Fragment worker = getSupportFragmentManager().findFragmentByTag(ConversationWorkerFragment.FRAGMENT_TAG);
        // if (worker == null) {
        // worker = new ConversationWorkerFragment();
        // getSupportFragmentManager().beginTransaction().add(worker, ConversationWorkerFragment.FRAGMENT_TAG).commit();
        // }

        // if (AppEnvironment.getInstance().LOG_CRASHES) {
        // Crittercism.init(getApplicationContext(), AppEnvironment.getInstance().CRITTERCISM_ID);
        // }

        if (savedInstanceState == null) {
            if (!HollerbackAppState.isValidSession()) {
                initWelcomeFragment();
            } else {

                initFragment();
                this.getSupportActionBar().show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode);
        switch (requestCode) {
            case SettingPreferenceActivity.PREFERENCE_PAGE_REQUEST_CODE:
                Log.d(TAG, "checking result code");

                if (resultCode == Activity.RESULT_OK) {
                    Bundle args = data.getExtras();
                    boolean logout = args.getBoolean(SettingPreferenceActivity.Action.LOGOUT, false);
                    Log.d(TAG, "checking logout");
                    if (logout) {
                        Log.d(TAG, "logging out");
                        mLaunchWelcome = true;
                    }

                    boolean findFriends = args.getBoolean(SettingPreferenceActivity.Action.FIND_FRIENDS, false);
                    if (findFriends) {
                        Log.d(TAG, "settings - add friends");
                        mLaunchInviteFriends = true;

                    }

                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLaunchWelcome) {
            mLaunchWelcome = false;
            initWelcomeFragment();
        }

        if (mLaunchInviteFriends) {
            mLaunchInviteFriends = false;
            inviteFriends();

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

    private void setupActionBar() {
        ActionBar supportActionBar = getSupportActionBar();

        supportActionBar.setIcon(R.drawable.banana_medium);
        supportActionBar.setHomeButtonEnabled(true);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowTitleEnabled(false);

        // set custom view for the title
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.header_title, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.START);

        mActionBarTitle = (TextView) customView.findViewById(R.id.title);
        mActionBarSubTitle = (TextView) customView.findViewById(R.id.sub_title);
        supportActionBar.setCustomView(customView, params);
        supportActionBar.setDisplayShowCustomEnabled(true);
        supportActionBar.show();

    }

    public TextView getCustomActionBarTitle() {
        return mActionBarTitle;
    }

    public void setCustomActionBarSubTitle(String subtitle) {
        if (subtitle == null || "".equals(subtitle)) {
            mActionBarSubTitle.setVisibility(View.GONE);
            mActionBarSubTitle.setText("");
        } else {
            mActionBarSubTitle.setVisibility(View.VISIBLE);
            mActionBarSubTitle.setText(subtitle);
        }
    }

    public TextView getCustomActionBarSubTitle() {
        return mActionBarSubTitle;
    }

    public void initFragment() {
        popBackStack();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        ConversationListFragment fragment = ConversationListFragment.newInstance();
        // fragmentTransaction.add(R.id.fragment_holder, fragment).addToBackStack(ConversationListFragment.FRAGMENT_TAG).commit();
        fragmentTransaction.replace(R.id.fragment_holder, fragment, ConversationListFragment.FRAGMENT_TAG).commit();

    }

    public void initWelcomeFragment() {

        popBackStack();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        WelcomeFragment fragment = new WelcomeFragment();
        fragmentTransaction.replace(R.id.fragment_holder, fragment);
        // fragmentTransaction.addToBackStack(WelcomeFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    private void inviteFriends() {
        ContactsFragment f = ContactsFragment.newInstance(NextAction.INVITE_FRIENDS);
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                .replace(R.id.fragment_holder, f).addToBackStack(ConversationListFragment.FRAGMENT_TAG).commit();
    }

    private void popBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();

        for (int i = 0; i < count; i++) {
            fragmentManager.popBackStackImmediate();
        }
    }

    private void registerBroadcasts() {
        mReceiver = new InternalReceiver();
        IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.AUTH_EXCEPTION);
    }

    public List<ConversationModel> getConversations() {
        return mConversations;
    }

    public ContactsInterface getContactsInterface() {
        return mContactsDelegate;
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

                // clear preferences/logout
                HollerbackAppState.logOut(HollerbackMainActivity.this);

                // pop the backstack and launch the initwelcomefragment
                getSupportFragmentManager().popBackStack(WelcomeFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                initWelcomeFragment();

            }

        }

    }

    @Override
    public void onTaskComplete(Task t) {
        mContactsDelegate.onTaskComplete(t);

    }

    @Override
    public void onTaskError(Task t) {
        mContactsDelegate.onTaskError(t);

    }

    @Override
    public Task getTask() { // if we need to add different tasks other than contacts, we'll have to modify things

        return mContactsDelegate.getTask();
    }
}
