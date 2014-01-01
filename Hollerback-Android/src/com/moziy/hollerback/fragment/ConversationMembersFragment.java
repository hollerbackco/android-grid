package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.moziy.hollerback.R;
import com.moziy.hollerback.connection.HBRequestManager;
import com.moziy.hollerback.connection.HollerbackAPI;
import com.moziy.hollerback.debug.LogUtil;

/**
 * this is the one of the only fragment I've written from start to end.
 * @author peterma
 *
 */
public class ConversationMembersFragment extends BaseFragment {
    private SherlockFragmentActivity mActivity;
    private ViewGroup mRootView;
    private TextView mHeaderView;
    private ViewGroup mFooterView;
    private Button mBtnLeaveGroup;

    private TextView mTxtUnwatched;
    private Button mBtnClear;
    private ViewGroup mWrapperUnwatched;
    private ListView mLsvMembers;
    private String mConversationId;
    private List<String> mMembers = new ArrayList<String>();

    private ArrayAdapter<String> mMembersAdapter;

    /**
     * Create a new instance of ConversationMembersFragment, providing "num" as an
     * argument.
     */
    public static ConversationMembersFragment newInstance(long conversationId) {

        ConversationMembersFragment f = new ConversationMembersFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putLong("conversationId", conversationId);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = this.getSherlockActivity();
        mRootView = (ViewGroup) inflater.inflate(R.layout.conversation_member_fragment, null);
        mHeaderView = (TextView) inflater.inflate(R.layout.view_contactheader, null);
        mHeaderView.setText(this.getResources().getString(R.string.conversation_members));
        mFooterView = (ViewGroup) inflater.inflate(R.layout.footer_memberlist, null);

        mConversationId = this.getArguments().getString("conversationId");
        this.startLoading();
        initializeView(mRootView);

        dataBind();

        return mRootView;
    }

    @Override
    protected void initializeView(View view) {
        mTxtUnwatched = (TextView) mRootView.findViewById(R.id.txtUnwatched);
        mWrapperUnwatched = (ViewGroup) mRootView.findViewById(R.id.wrapperUnwatched);

        mLsvMembers = (ListView) mRootView.findViewById(R.id.lsvMembers);
        mLsvMembers.setItemsCanFocus(false);
        mLsvMembers.setClickable(false);
        mLsvMembers.addHeaderView(mHeaderView);
        mLsvMembers.addFooterView(mFooterView);

        // mMembersAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, mMembers);
        mMembersAdapter = new ArrayAdapter<String>(mActivity, R.layout.custom_simple_list_item, mMembers);
        mLsvMembers.setAdapter(mMembersAdapter);

        mBtnLeaveGroup = (Button) mFooterView.findViewById(R.id.btnLeaveGroup);
        mBtnLeaveGroup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                leaveGroup();
            }
        });

        mBtnClear = (Button) mRootView.findViewById(R.id.btnClear);
        mBtnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearWatched();
            }
        });
    }

    private void clearWatched() {
        HBRequestManager.clearNewConversationWatchedStatus(mConversationId, new JsonHttpResponseHandler() {

            @Override
            protected Object parseResponse(String response) throws JSONException {
                LogUtil.i("RESPONSE: " + response);
                return super.parseResponse(response);
            }

            @Override
            public void onFailure(Throwable error, JSONObject response) {
                // TODO Auto-generated method stub
                super.onFailure(error, response);
                LogUtil.e(HollerbackAPI.API_CONVERSATION_DETAILS + "FAILURE");
            }

            @Override
            public void onSuccess(int statusId, JSONObject response) {
                // TODO Auto-generated method stub
                super.onSuccess(statusId, response);
                LogUtil.i("ON SUCCESS API conversation");
                try {

                    if (response.has("meta")) {
                        if (response.getJSONObject("meta").getInt("code") == 200) {
                            mWrapperUnwatched.setVisibility(View.GONE);
                        }
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
    }

    private void dataBind() {
        HBRequestManager.getConversation(mConversationId, new JsonHttpResponseHandler() {

            @Override
            protected Object parseResponse(String response) throws JSONException {
                LogUtil.i("RESPONSE: " + response);
                return super.parseResponse(response);
            }

            @Override
            public void onFailure(Throwable error, JSONObject response) {
                // TODO Auto-generated method stub
                super.onFailure(error, response);
                LogUtil.e(HollerbackAPI.API_CONVERSATION_DETAILS + "FAILURE");
            }

            @Override
            public void onSuccess(int statusId, JSONObject response) {
                // TODO Auto-generated method stub
                super.onSuccess(statusId, response);
                LogUtil.i("ON SUCCESS API conversation");
                try {
                    JSONObject data = new JSONObject();
                    if (response.has("data")) {
                        data = response.getJSONObject("data");
                    } else
                        return;

                    if (data.has("members")) {
                        JSONArray members = data.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            mMembers.add(members.getJSONObject(i).getString("name"));
                        }

                        mMembersAdapter.notifyDataSetChanged();
                        mHeaderView.setText(String.valueOf(mMembers.size()) + " " + mHeaderView.getText().toString());
                    }

                    if (data.has("unread_count")) {
                        if (data.getInt("unread_count") != 0) {
                            mTxtUnwatched.setText(String.valueOf(data.getInt("unread_count")) + " " + mActivity.getResources().getString(R.string.conversation_unwatched_videos));
                            mWrapperUnwatched.setVisibility(View.VISIBLE);
                        } else {
                            mWrapperUnwatched.setVisibility(View.GONE);
                        }

                    }
                    mMembersAdapter.notifyDataSetChanged();
                    ConversationMembersFragment.this.stopLoading();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
    }

    private void leaveGroup() {
        this.startLoading();
        HBRequestManager.leaveConversation(mConversationId, new JsonHttpResponseHandler() {

            @Override
            protected Object parseResponse(String response) throws JSONException {
                LogUtil.i("RESPONSE: " + response);
                return super.parseResponse(response);
            }

            @Override
            public void onFailure(Throwable error, JSONObject response) {
                // TODO Auto-generated method stub
                super.onFailure(error, response);
                LogUtil.e(HollerbackAPI.API_CONVERSATION_DETAILS + "FAILURE");
            }

            @Override
            public void onSuccess(int statusId, JSONObject response) {
                // TODO Auto-generated method stub
                super.onSuccess(statusId, response);
                LogUtil.i("ON SUCCESS API conversation");
                try {

                    if (response.has("meta")) {
                        if (response.getJSONObject("meta").getInt("code") == 200) {
                            Toast.makeText(mActivity, R.string.conversation_leave_success, Toast.LENGTH_LONG).show();
                            ConversationMembersFragment.this.stopLoading();
                            mActivity.getSupportFragmentManager().popBackStack(ConversationHistoryFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
    }
}
