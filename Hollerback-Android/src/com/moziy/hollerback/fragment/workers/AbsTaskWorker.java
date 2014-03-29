package com.moziy.hollerback.fragment.workers;

import android.support.v4.app.Fragment;

import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;

public class AbsTaskWorker extends Fragment {

    public static final String SERIAL_EXECUTER_BUNDLE_ARG_KEY = "SERIAL_EXECUTER";
    protected TaskExecuter mExecuter;
    protected Task mTask;
    protected boolean mRunSerially = false;

}
