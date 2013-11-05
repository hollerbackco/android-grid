package com.moziy.hollerback.activity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.moziy.hollerback.util.LoadingFragmentUtil;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class AbstractActivity extends SherlockFragmentActivity {
    public LoadingFragmentUtil mLoadview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoadview = new LoadingFragmentUtil(this);

    }
}
