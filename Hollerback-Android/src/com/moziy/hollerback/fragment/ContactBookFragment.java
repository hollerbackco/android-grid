package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.moziy.hollerback.R;

public class ContactBookFragment extends BaseFragment {
    private static final String TAG = ContactBookFragment.class.getSimpleName();
    private static final int NUM_TABS = 3;

    private ContactsFragment mContactsFragment;
    private Fragment mHollerbackContactsFragment;
    private Fragment mSearchFragment;
    private ViewPager mPager;
    private TabPagerAdapter mPagerAdapter;
    private ActionBar mActionbar;

    @Override
    protected String getFragmentName() {
        // TODO Auto-generated method stub
        return TAG;
    }

    public static ContactBookFragment newInstance() {
        return new ContactBookFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        super.onCreate(savedInstanceState);

        mActionbar = getSherlockActivity().getSupportActionBar();
        mActionbar.setTitle(getString(R.string.contactbook_title));

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
        // create three tabs
        Tab tab = mActionbar.newTab();
        tab.setText(R.string.contacts_lc);
        tab.setCustomView(R.layout.contact_tab_view);
        tab.setTabListener(mTabListener);
        tab.setContentDescription(R.string.contacts_lc);
        mActionbar.addTab(tab, 0);

        tab = mActionbar.newTab();
        tab.setText(R.string.hollerback_users_lc);
        tab.setCustomView(R.layout.contact_tab_view);
        tab.setTabListener(mTabListener);
        tab.setContentDescription(R.string.hollerback_users_lc);
        mActionbar.addTab(tab, 1, true); // tab, position, selected

        tab = mActionbar.newTab();
        tab.setText(R.string.search_lc);
        tab.setCustomView(R.layout.contact_tab_view);
        tab.setTabListener(mTabListener);
        tab.setContentDescription(R.string.search_lc);
        mActionbar.addTab(tab, 2);
    }

    @Override
    public void onPause() {

        if (isRemoving()) {
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

    public static class TabPagerAdapter extends FragmentPagerAdapter {

        private ContactsFragment mContactsFragment;
        private Fragment mHollerbackContactsFragment;
        private Fragment mSearchFragment;

        public TabPagerAdapter(FragmentManager fm, ActionBar actionBar) {
            super(fm);
            mContactsFragment = ContactsFragment.newInstance();
            mHollerbackContactsFragment = ContactsFragment.newInstance();
            mSearchFragment = ContactsFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mContactsFragment;
                case 1:
                    return mHollerbackContactsFragment;
                case 2:
                    return mSearchFragment;
            }

            return mHollerbackContactsFragment;
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

    }
}
