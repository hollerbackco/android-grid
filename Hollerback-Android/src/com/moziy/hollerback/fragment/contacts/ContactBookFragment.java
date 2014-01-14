package com.moziy.hollerback.fragment.contacts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.MenuItem;
import com.moziy.hollerback.R;
import com.moziy.hollerback.fragment.BaseFragment;
import com.moziy.hollerback.util.contacts.ContactsDelegate.Transaction;

public class ContactBookFragment extends BaseFragment {
    private static final String TAG = ContactBookFragment.class.getSimpleName();
    private static final int NUM_TABS = 3;

    private ViewPager mPager;
    private TabPagerAdapter mPagerAdapter;
    private ActionBar mActionbar;

    private ContactsChildFragment mContactsFragment;
    private AddedMeChildFragment mAddedMeFragment;
    private ContactsChildFragment mSearchFragment;

    @Override
    protected String getFragmentName() {
        // TODO Auto-generated method stub
        return TAG;
    }

    public static ContactBookFragment newInstance() {
        return new ContactBookFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "title; " + getActionBarTitle());
        mActionbar = getSherlockActivity().getSupportActionBar();
        mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_book_fragment, container, false);
        mPager = (ViewPager) v.findViewById(R.id.view_pager);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mActionbar.selectTab(mActionbar.getTabAt(position));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // TODO Auto-generated method stub

            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPagerAdapter = new TabPagerAdapter(getChildFragmentManager(), getSherlockActivity().getSupportActionBar());
        mPager.setAdapter(mPagerAdapter);

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        // create three tabs
        Tab tab = mActionbar.newTab();
        tab.setText(R.string.contacts_lc);
        View v = inflater.inflate(R.layout.contact_tab_view, null);
        ((TextView) v.findViewById(R.id.tv_tab_text)).setText(getString(R.string.contacts_lc));
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        tab.setCustomView(v);
        tab.setTabListener(mTabListener);
        tab.setContentDescription(R.string.contacts_lc);
        mActionbar.addTab(tab, 0);

        tab = mActionbar.newTab();
        tab.setText(R.string.hollerback_users_lc);
        v = inflater.inflate(R.layout.contact_tab_view, null);
        ((TextView) v.findViewById(R.id.tv_tab_text)).setText(getString(R.string.hollerback_users_lc));
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        tab.setCustomView(v);
        tab.setTabListener(mTabListener);
        tab.setContentDescription(R.string.hollerback_users_lc);
        mActionbar.addTab(tab, 1, true); // tab, position, selected

        tab = mActionbar.newTab();
        tab.setText(R.string.search_lc);
        v = inflater.inflate(R.layout.contact_tab_view, null);
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        ((TextView) v.findViewById(R.id.tv_tab_text)).setText(getString(R.string.search_lc));
        tab.setCustomView(v);
        tab.setTabListener(mTabListener);
        tab.setContentDescription(R.string.search_lc);
        mActionbar.addTab(tab, 2);

    }

    @Override
    public void onPause() {

        if (isRemoving()) {

            for (Fragment f : getChildFragmentManager().getFragments()) {
                Transaction t = ((ContactBookChild) f).getContactTransaction();
                if (t != null) {
                    Log.d(TAG, "commiting contact transaction");
                    t.commit();
                }
            }

            getSherlockActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            getSherlockActivity().getActionBar().removeAllTabs();

        }

        super.onPause();
    }

    private TabListener mTabListener = new TabListener() {

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 0:
                    mPager.setCurrentItem(0);
                    break;
                case 1:
                    mPager.setCurrentItem(1);
                    break;
                case 2:
                    mPager.setCurrentItem(2);
                    break;
            }

        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }
    };

    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.contactbook_title);
    }

    public class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fm, ActionBar actionBar) {
            super(fm);
            mContactsFragment = ContactsChildFragment.newInstance();
            mContactsFragment.setChildFragment();

            mAddedMeFragment = AddedMeChildFragment.newInstance();
            mAddedMeFragment.setChildFragment();

            mSearchFragment = ContactsChildFragment.newInstance();
            mSearchFragment.setChildFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mContactsFragment;
                case 1:
                    return mAddedMeFragment;
                case 2:
                    return mSearchFragment;
            }

            return mAddedMeFragment;
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

    }

    public interface ContactBookChild {
        public Transaction getContactTransaction();
    }
}
