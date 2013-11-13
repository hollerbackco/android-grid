package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.List;

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
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.moziy.hollerback.R;
import com.moziy.hollerback.activity.SettingPreferenceActivity;
import com.moziy.hollerback.adapter.ConversationListAdapter;
import com.moziy.hollerback.communication.IABIntent;
import com.moziy.hollerback.communication.IABroadcastManager;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.fragment.workers.ConversationWorkerFragment.OnConversationsUpdated;
import com.moziy.hollerback.model.ConversationModel;
import com.moziy.hollerback.service.SyncService;
import com.moziy.hollerback.util.AppEnvironment;
import com.moziy.hollerback.util.QU;

public class ConversationListFragment extends BaseFragment implements OnConversationsUpdated, LoaderCallbacks<List<ConversationModel>> {

    public static final String FRAGMENT_TAG = ConversationListFragment.class.getSimpleName();

    private int PREFERENCE_PAGE;
    private ViewGroup mHeader;
    private EditText mTxtSearch;

    PullToRefreshListView mConversationList;
    ListView lsvBaseListView;

    ConversationListAdapter mConversationListAdapter;

    AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(AppEnvironment.getInstance().ACCESS_KEY_ID, AppEnvironment.getInstance().SECRET_KEY));

    private List<ConversationModel> mConversations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mActivity.getSupportActionBar().setHomeButtonEnabled(false);
        mActivity.getSupportActionBar().setIcon(R.drawable.logo);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        View fragmentView = inflater.inflate(R.layout.message_list_fragment, null);

        this.startLoading();

        mHeader = (ViewGroup) inflater.inflate(R.layout.message_list_item_header, null);
        initializeView(fragmentView);

        getLoaderManager().initLoader(0, null, this); // the loader will be autostarted

        return fragmentView;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
        IABroadcastManager.registerForLocalBroadcast(receiver, IABIntent.GET_CONVERSATIONS);

    }

    @Override
    public void onPause() {
        super.onPause();
        IABroadcastManager.unregisterLocalReceiver(receiver);
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
        IABroadcastManager.unregisterLocalReceiver(receiver);
        Log.d(FRAGMENT_TAG, "onDestroyView");

    }

    @Override
    protected void initializeView(View view) {
        mConversationList = (PullToRefreshListView) view.findViewById(R.id.message_listview);

        mTxtSearch = (EditText) mHeader.findViewById(R.id.txtSearch);
        mTxtSearch.addTextChangedListener(filterTextWatcher);

        lsvBaseListView = mConversationList.getRefreshableView();
        lsvBaseListView.addHeaderView(mHeader);
        mConversationList.setShowIndicator(false);
        mConversationList.setPullToRefreshEnabled(false);
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
                ContactsInviteFragment contactfragment = ContactsInviteFragment.newInstance();
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, contactfragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(ContactsInviteFragment.class.getSimpleName()).commitAllowingStateLoss();
                break;
            case R.id.action_add:
                ContactsFragment fragment = ContactsFragment.newInstance();
                mActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(ContactsFragment.class.getSimpleName()).commitAllowingStateLoss();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    OnItemClickListener mOnListItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.i("Starting Conversation: " + position + " id: " + id);
            ConversationModel item = mConversationListAdapter.getItem(position);
            Log.d(FRAGMENT_TAG, "watching conversation with id: " + item.getConversationId());
            startConversationFragment(mConversationListAdapter.getItem(position));

        }

    };

    public void startConversationFragment(ConversationModel conversation) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // TODO: Fetch data from API call
        // ConversationHistoryFragment fragment = ConversationHistoryFragment.newInstance(conversation);

        ConversationFragment fragment = ConversationFragment.newInstance(conversation.getConversationId());
        fragmentTransaction.replace(R.id.fragment_holder, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        fragmentTransaction.addToBackStack(ConversationHistoryFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    private void startSettingsFragment() {
        Intent intent = new Intent(mActivity, SettingPreferenceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivityForResult(intent, PREFERENCE_PAGE);
    }

    /**
     * Create a new instance of CountingFragment, providing "num" as an
     * argument.
     */
    public static ConversationListFragment newInstance(int num) {

        ConversationListFragment f = new ConversationListFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IABIntent.isIntent(intent, IABIntent.GET_CONVERSATIONS)) {
                // mConversationListAdapter
                // .setConversations(TempMemoryStore.conversations);
                String hash = intent.getStringExtra(IABIntent.PARAM_INTENT_DATA);

                ArrayList<ConversationModel> conversations = (ArrayList<ConversationModel>) QU.getDM().getObjectForToken(hash);

                mConversationListAdapter.setConversations(conversations);
                mConversationListAdapter.notifyDataSetChanged();

                mConversationList.onRefreshComplete();
                ConversationListFragment.this.stopLoading();
            }

        }
    };

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mConversationListAdapter.getFilter().filter(s);
        }

    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.i("Receiving onActivityResult: " + requestCode);
        if (requestCode == PREFERENCE_PAGE && resultCode == mActivity.RESULT_OK) {
            // It wants contact list
            ContactsInviteFragment fragment = ContactsInviteFragment.newInstance();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(ContactsFragment.class.getSimpleName()).commitAllowingStateLoss();
        }
    }

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
        return new AsyncTaskLoader<List<ConversationModel>>(getActivity()) {

            private List<ConversationModel> mConvos;
            private SyncReceiver mReceiver;

            // initialization block
            {
                Log.d(FRAGMENT_TAG, "launch sync");
                mReceiver = new SyncReceiver(this);
                IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.NOTIFY_SYNC);
                IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.SYNC_FAILED);
                IABroadcastManager.registerForLocalBroadcast(mReceiver, IABIntent.CONVERSATION_CREATED); // if a conversation was crreated, then lets update the content

                // start the sync intent service
                Intent intent = new Intent();
                intent.setClass(getActivity(), SyncService.class);
                getActivity().startService(intent);
            }

            @Override
            public void onContentChanged() {
                mConvos = null; // delete the contents
                Log.d(FRAGMENT_TAG, "removing convos");
                super.onContentChanged();
            }

            @Override
            protected void onStartLoading() {
                if (mConvos != null) {
                    Log.d(FRAGMENT_TAG, "delivering results");
                    deliverResult(mConvos);
                    return;
                }

                Log.d(FRAGMENT_TAG, "onstartloading");
                forceLoad();

            }

            @Override
            public List<ConversationModel> loadInBackground() {
                Log.d(FRAGMENT_TAG, "thread id: " + Thread.currentThread().getId());
                mConvos = new Select().all().from(ConversationModel.class).execute();
                Log.d(FRAGMENT_TAG, "retrieved " + mConvos.size() + " convos from the database");
                return mConvos;
            }

            @Override
            protected void onReset() {
                IABroadcastManager.unregisterLocalReceiver(mReceiver);
                super.onReset();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<ConversationModel>> loader, List<ConversationModel> data) {
        Log.d(FRAGMENT_TAG, "loader finished");
        mConversationListAdapter = new ConversationListAdapter(getSherlockActivity());
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
        mConversationListAdapter = new ConversationListAdapter(getSherlockActivity());
        mConversationList.setAdapter(mConversationListAdapter);
        mConversationListAdapter.notifyDataSetChanged();

    }

    public static class SyncReceiver extends BroadcastReceiver {

        private Loader<List<ConversationModel>> mLoader;

        public SyncReceiver(AsyncTaskLoader<List<ConversationModel>> loader) {
            mLoader = loader;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SyncReceiver", "onReceive - " + "isStarted: " + mLoader.isStarted());

            mLoader.onContentChanged();
        }
    }
}
