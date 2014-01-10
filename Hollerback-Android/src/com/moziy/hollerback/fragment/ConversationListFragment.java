package com.moziy.hollerback.fragment;

import java.util.List;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.activeandroid.query.Select;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.moziy.hollerback.HollerbackAppState;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.HollerbackMainActivity;
import com.moziy.hollerback.activity.SettingPreferenceActivity;
import com.moziy.hollerback.adapter.ConversationListAdapter;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.fragment.workers.ConversationWorkerFragment.OnConversationsUpdated;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.service.SyncService;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.AppEnvironment;

public class ConversationListFragment extends BaseFragment implements OnConversationsUpdated, LoaderCallbacks<List<ConversationModel>> {
    private static final String TAG = ConversationListFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = ConversationListFragment.class.getSimpleName();

    private ViewGroup mFooter;
    private EditText mTxtSearch;

    // PullToRefreshListView mConversationList;
    // ListView lsvBaseListView;
    ListView mConversationList;

    ConversationListAdapter mConversationListAdapter;

    AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(AppEnvironment.getInstance().ACCESS_KEY_ID, AppEnvironment.getInstance().SECRET_KEY));

    // private List<ConversationModel> mConversations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!HollerbackAppState.isValidSession()) {
            ((HollerbackMainActivity) getActivity()).initWelcomeFragment();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (!HollerbackAppState.isValidSession()) {
            return null;
        }

        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mActivity.getSupportActionBar().setHomeButtonEnabled(false);
        mActivity.getSupportActionBar().setIcon(R.drawable.logo);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        View fragmentView = inflater.inflate(R.layout.conversation_list_fragment, container, false);

        this.startLoading();

        mConversationList = (ListView) fragmentView.findViewById(R.id.message_listview);
        mFooter = (ViewGroup) inflater.inflate(R.layout.conversation_list_footer, null);
        mTxtSearch = (EditText) fragmentView.findViewById(R.id.txtSearch);

        initListViewAnimation();

        mTxtSearch.addTextChangedListener(filterTextWatcher);

        mConversationList.addFooterView(mFooter, null, true); // respond to touch events

        getLoaderManager().initLoader(0, null, this); // the loader will be autostarted

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
        mActivity.getSupportActionBar().show();
        // IABroadcastManager.registerForLocalBroadcast(receiver, IABIntent.GET_CONVERSATIONS);

        // TODO - Sajjad: refresh conversation list
        if (mConversationListAdapter != null) // notify so that we get the latest results on the screen
            mConversationListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onPause() {
        super.onPause();
        // IABroadcastManager.unregisterLocalReceiver(receiver);
        if (mTxtSearch != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mTxtSearch.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        mTxtSearch.removeTextChangedListener(filterTextWatcher);
        // IABroadcastManager.unregisterLocalReceiver(receiver);
        Log.d(TAG, "onDestroyView");

    }

    @Override
    protected void initializeView(View view) {

        // lsvBaseListView = mConversationList.getRefreshableView();
        // lsvBaseListView.addHeaderView(mHeader);
        // mConversationList.setShowIndicator(false);
        // mConversationList.setPullToRefreshEnabled(false);
        // mConversationList.setOnRefreshListener(new OnRefreshListener() {
        //
        // @Override
        // public void onRefresh(PullToRefreshBase refreshView) {
        // LogUtil.i("Refresh the Listview");
        // HBRequestManager.getConversations();
        // }
        // });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                this.startSettingsFragment();
                break;
            case R.id.action_find_friends:

                if (isResumed()) { // no need to launch if we're not in the resumed state
                    // log analytic event
                    AnalyticsUtil.log(AnalyticsUtil.Category.UI, AnalyticsUtil.UiAction.ButtonPress, AnalyticsUtil.Label.ConvoListAddFriends, null);

                    ContactsFragment f = ContactsFragment.newInstance(ContactsFragment.NextAction.INVITE_FRIENDS);

                    mActivity.getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_top).replace(R.id.fragment_holder, f)
                            .addToBackStack(FRAGMENT_TAG).commit();
                }
                // Toast.makeText(mActivity, "We are working hard to get this to you ASAP!", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_add:
                if (isResumed()) { // no need to take action if we're not in the resumed state
                    // log analytic event
                    EasyTracker.getInstance(HollerbackApplication.getInstance()).send(
                            MapBuilder.createEvent(AnalyticsUtil.Category.UI, AnalyticsUtil.UiAction.ButtonPress, AnalyticsUtil.Label.ConvoListPlus, null).build());
                    ContactsFragment fragment = ContactsFragment.newInstance();
                    mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(FRAGMENT_TAG).commit();

                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    OnItemClickListener mOnListItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // return if we're not in the resumsed state since we can't add fragments
            if (!isResumed())
                return;

            LogUtil.i("Starting Conversation: " + position + " id: " + id);
            ConversationModel conversation = (ConversationModel) parent.getItemAtPosition(position);

            if (conversation == null) {

                // log analytic event
                EasyTracker.getInstance(HollerbackApplication.getInstance()).send(
                        MapBuilder.createEvent(AnalyticsUtil.Category.UI, AnalyticsUtil.UiAction.ButtonPress, AnalyticsUtil.Label.ConvoListNewConvo, null).build());

                // this is the footer view
                Log.d(TAG, "footer view tapped");
                AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.convo_item_tap_anim);
                set.setTarget(view);
                set.start();

                ContactsFragment fragment = ContactsFragment.newInstance();
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(FRAGMENT_TAG)
                        .commit();

                return;
            }

            // if (conversation.getUnreadCount() > 0) {
            if (true) {

                Log.d(TAG, "watching conversation with id: " + conversation.getConversationId());

                // dismiss any notifications bound to this convo id
                NotificationManager nm = (NotificationManager) HollerbackApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel((int) conversation.getConversationId());

                startConversationFragment(conversation);
            } else {

                AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.convo_item_tap_anim);
                set.setTarget(((ConversationListAdapter.ViewHolder) view.getTag()).topLayer);
                set.start();
                // TODO: Fetch data from API call
                // ConversationHistoryFragment fragment = ConversationHistoryFragment.newInstance(conversation);
                // getFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment) //
                // .addToBackStack(FRAGMENT_TAG).setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).commit();

            }

        }

    };

    public void startConversationFragment(ConversationModel conversation) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ConversationFragment fragment = ConversationFragment.newInstance(conversation.getConversationId());
        fragmentTransaction.replace(R.id.fragment_holder, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        fragmentTransaction.addToBackStack(FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    private void startSettingsFragment() {
        Intent intent = new Intent(mActivity, SettingPreferenceActivity.class);

        // if startactivity for result is launched from a fragment, the request code gets altered to indicate that this is for a fragment
        // and not an activity. If you want to launch an activity and have the activity handle it, then you should go getActivity().start..
        // startActivityForResult(intent, SettingPreferenceActivity.PREFERENCE_PAGE_REQUEST_CODE);
        getActivity().startActivityForResult(intent, SettingPreferenceActivity.PREFERENCE_PAGE_REQUEST_CODE);
    }

    private void initListViewAnimation() {
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(150);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(200);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);

        mConversationList.setLayoutAnimation(controller);
    }

    /**
     * Create a new instance of CountingFragment, providing "num" as an
     * argument.
     */
    public static ConversationListFragment newInstance() {

        ConversationListFragment f = new ConversationListFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    // BroadcastReceiver receiver = new BroadcastReceiver() {
    //
    // @Override
    // public void onReceive(Context context, Intent intent) {
    // if (IABIntent.isIntent(intent, IABIntent.GET_CONVERSATIONS)) {
    // // mConversationListAdapter
    // // .setConversations(TempMemoryStore.conversations);
    // String hash = intent.getStringExtra(IABIntent.PARAM_INTENT_DATA);
    //
    // ArrayList<ConversationModel> conversations = (ArrayList<ConversationModel>) QU.getDM().getObjectForToken(hash);
    //
    // mConversationListAdapter.setConversations(conversations);
    // mConversationListAdapter.notifyDataSetChanged();
    //
    // // mConversationList.onRefreshComplete();
    // ConversationListFragment.this.stopLoading();
    // }
    //
    // }
    // };

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mConversationListAdapter != null)
                mConversationListAdapter.getFilter().filter(s);
        }

    };

    @Override
    public void onUpdate(List<ConversationModel> conversations) {

        // if (mConversationListAdapter == null) {
        // mConversationListAdapter = new ConversationListAdapter(getSherlockActivity());
        // }
        //
        // if (mActivity == null) {
        // mActivity = getSherlockActivity();
        // }
        mConversationListAdapter.setConversations(conversations);
        mConversationListAdapter.notifyDataSetChanged();

        // mConversationList.onRefreshComplete();
        ConversationListFragment.this.stopLoading();

    }

    @Override
    public Loader<List<ConversationModel>> onCreateLoader(final int id, final Bundle args) {
        return new ConvoLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ConversationModel>> loader, List<ConversationModel> data) {

        Log.d(TAG, "loader finished");
        mConversationListAdapter = new ConversationListAdapter(getSherlockActivity(), this);
        mConversationListAdapter.setConversations(data);
        mConversationList.setAdapter(mConversationListAdapter);
        mConversationList.setOnItemClickListener(mOnListItemClickListener);
        mConversationListAdapter.notifyDataSetChanged();

        // mConversationList.onRefreshComplete();
        ConversationListFragment.this.stopLoading();

    }

    @Override
    public void onLoaderReset(Loader<List<ConversationModel>> loader) {
        // TODO: remove all references to the data
        mConversationListAdapter = new ConversationListAdapter(getSherlockActivity(), this);
        mConversationList.setAdapter(mConversationListAdapter);
        mConversationListAdapter.notifyDataSetChanged();

    }

    public static class ConvoLoader extends AsyncTaskLoader<List<ConversationModel>> {

        private List<ConversationModel> mConvos;
        private SyncReceiver mReceiver;

        // initialization block
        {
            Log.d(TAG, "launch sync");
            mReceiver = new SyncReceiver(this);
            IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.NOTIFY_SYNC);
            IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.SYNC_FAILED);
            IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONVERSATION_CREATED); // if a conversation was crreated, then lets update the content
            IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONVERSATION_UPDATED); // if a conversation got updated, lets repopulate

            // start the sync intent service
            Intent intent = new Intent();
            intent.setClass(getContext(), SyncService.class);
            getContext().startService(intent);
        }

        public ConvoLoader(Context context) {
            super(context);
        }

        @Override
        public void onContentChanged() {
            mConvos = null; // delete the contents
            Log.d(TAG, "removing convos");
            super.onContentChanged();
        }

        @Override
        protected void onStartLoading() {
            if (mConvos != null) {
                Log.d(TAG, "delivering results");
                deliverResult(mConvos);
                return;
            }

            Log.d(TAG, "onstartloading");
            forceLoad();

        }

        @Override
        public List<ConversationModel> loadInBackground() {
            mConvos = new Select().all().from(ConversationModel.class).orderBy(ActiveRecordFields.C_CONV_LAST_MESSAGE_AT + " DESC").execute();
            Log.d(TAG, "retrieved " + mConvos.size() + " convos from the database");
            return mConvos;
        }

        @Override
        protected void onReset() {
            IABroadcastManager.unregisterLocalReceiver(mReceiver);
            super.onReset();
        }

        public boolean hasSynched() {
            return mReceiver.mHasReceived;
        }

    }

    public static class SyncReceiver extends BroadcastReceiver { // receives intents from the syncservice

        private Loader<List<ConversationModel>> mLoader;
        private boolean mHasReceived;

        public SyncReceiver(AsyncTaskLoader<List<ConversationModel>> loader) {
            mLoader = loader;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SyncReceiver", "onReceive - " + "isStarted: " + mLoader.isStarted());
            mHasReceived = true;
            mLoader.onContentChanged();
        }

    }

    @Override
    protected String getFragmentName() {
        return TAG;
    }
}
