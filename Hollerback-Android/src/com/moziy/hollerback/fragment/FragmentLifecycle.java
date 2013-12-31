package com.moziy.hollerback.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public interface FragmentLifecycle {

    public void onPreSuperAttach(Fragment fragment);

    public void onPostSuperAttach(Fragment fragment);

    public void onPreSuperDetach(Fragment fragment);

    public void onPostSuperDetach(Fragment fragment);

    public void onPreSuperResume(Fragment fragment);

    public void onPostSuperResume(Fragment fragment);

    public void onPreSuperPause(Fragment fragment);

    public void onPostSuperPause(Fragment fragment);

    public void onSaveInstanceState(Bundle outState);

    public void onPreSuperActivityCreated(Bundle savedInstanceState);

    public void onPostSuperActivityCreated(Bundle savedInstanceState);

    public void init(Bundle savedInstanceState);
}
