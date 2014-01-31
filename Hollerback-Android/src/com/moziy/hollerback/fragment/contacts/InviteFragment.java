package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.SearchView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.Fields;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.fragment.AbsContactListFragment;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.SmsUtil;
import com.moziy.hollerback.util.sharedpreference.HBPreferences;
import com.moziy.hollerback.util.sharedpreference.PreferenceManagerUtil;

public class InviteFragment extends AbsContactListFragment {
    private static final String TAG = InviteFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = TAG;
    public static final String SHOW_DIALOG_BUNDLE_ARG_KEY = "SHOW_DIALOG";

    public static InviteFragment newInstance() {
        return newInstance(false);
    }

    public static InviteFragment newInstance(boolean showDialog) {
        InviteFragment f = new InviteFragment();
        Bundle args = new Bundle();
        args.putBoolean(SHOW_DIALOG_BUNDLE_ARG_KEY, showDialog);
        f.setArguments(args);

        return f;
    }

    private SearchView mSearchView;
    private boolean mShowDialog;
    private AlertDialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDialog = getArguments().getBoolean(SHOW_DIALOG_BUNDLE_ARG_KEY);

        if (mShowDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            mDialog = builder.setTitle(getString(R.string.invite_dialog_title)).setMessage(getString(R.string.invite_dialog_message))
                    .setPositiveButton(R.string.alright, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isResumed() && mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                        }
                    }).show();
        }
    }

    @Override
    public void onPause() {

        if (isRemoving() && mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        super.onPause();
    }

    @Override
    protected List<ContactListSegmentData> buildSegmentData(ContactsInterface ci) {

        List<ContactListSegmentData> segmentList = new ArrayList<ContactListSegmentData>();

        ContactListSegmentData segmentData = new ContactListSegmentData();
        segmentData.mContacts = mContactsInterface.getContactsExcludingHBContacts();
        segmentData.mSegmentTitle = getString(R.string.contacts_cc);
        segmentData.mTextPlaceHolderMsg = getString(R.string.contacts_book_empty);

        segmentList.add(segmentData);

        return segmentList;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.invite_menu, menu);
        final MenuItem item = menu.findItem(R.id.mi_search);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        // mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
        //
        // @Override
        // public void onFocusChange(View v, boolean hasFocus) {
        // if (!hasFocus) {
        // item.collapseActionView();
        // mSearchView.setQuery("", false);
        // }
        //
        // }
        // });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (mStickyListView != null) {
                    if (newText.length() > 0) {
                        mStickyListView.disableStickyHeader();
                    } else {
                        mStickyListView.enableStickyHeader();
                    }
                    mAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.mi_invite) {
            if (mSelected.isEmpty()) {
                return true;
            }
            // send an sms and then pop the backstack
            SmsUtil.invite(mActivity, new ArrayList<Contact>(mSelected), HollerbackApplication.getInstance().getString(R.string.sms_invite_friends), null, null);

            String dimensionValue = "Yes";
            AnalyticsUtil.getGaTracker().set(Fields.customDimension(3), dimensionValue);

            String metricValue = String.valueOf(mSelected.size());
            AnalyticsUtil.getGaTracker().set(Fields.customMetric(1), metricValue);

            if (mDialog != null) {
                PreferenceManagerUtil.setPreferenceValue(HBPreferences.VideoInviteInfo.SEEN_INVITE_SCREEN, true);
            }

            getFragmentManager().popBackStack();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getScreenName() {
        return AnalyticsUtil.ScreenNames.SMS_INVITE;
    }

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.invite_contacts);
    }

}
