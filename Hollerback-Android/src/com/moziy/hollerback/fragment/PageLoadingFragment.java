package com.moziy.hollerback.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.moziy.hollerback.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PageLoadingFragment extends SherlockFragment{
    private ViewGroup mRootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_loading, container, false);        
        
        return mRootView;
    }
}
