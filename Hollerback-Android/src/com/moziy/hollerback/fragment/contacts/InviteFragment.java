package com.moziy.hollerback.fragment.contacts;

import java.util.ArrayList;
import java.util.List;

import android.text.InputType;
import android.widget.SearchView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.HollerbackApplication;
import com.moziy.hollerback.R;
import com.moziy.hollerback.contacts.ContactsInterface;
import com.moziy.hollerback.contacts.data.ContactListSegmentData;
import com.moziy.hollerback.fragment.AbsContactListFragment;
import com.moziy.hollerback.model.Contact;
import com.moziy.hollerback.util.AnalyticsUtil;
import com.moziy.hollerback.util.SmsUtil;

public class InviteFragment extends AbsContactListFragment {
    private static final String TAG = InviteFragment.class.getSimpleName();

    public static InviteFragment newInstance() {
        return new InviteFragment();
    }

    private SearchView mSearchView;

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
            // send an sms and then pop the backstack
            SmsUtil.invite(mActivity, new ArrayList<Contact>(mSelected), HollerbackApplication.getInstance().getString(R.string.sms_invite_friends), null, null);
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
