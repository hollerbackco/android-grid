package com.moziy.hollerback.fragment.workers;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;

public class ActivityTaskWorker extends AbsTaskWorker {
    private static final String TAG = ActivityTaskWorker.class.getSimpleName();
    private Task mTask;
    private TaskExecuter mExecuter;

    public static ActivityTaskWorker newInstance(boolean useSingleThreadPool) {
        Bundle args = new Bundle();
        args.putBoolean(SERIAL_EXECUTER_BUNDLE_ARG_KEY, useSingleThreadPool);

        ActivityTaskWorker worker = new ActivityTaskWorker();
        worker.setArguments(args);
        return worker;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTaskClient = (TaskClient) activity;
        setTaskListeners(mTask, true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTaskClient = null;
        clearTaskListeners(mTask);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { // only launched once
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // lets execute the task
        // start executing the task
        mExecuter = new TaskExecuter();
        mTask = mTaskClient.getTask();

        if (mTask == null) {
            Log.w(TAG, "empty task so not launching");
            return;
        }

        setTaskListeners(mTask, true);

        if (Build.VERSION.SDK_INT >= 11 && !mRunSerially) {
            mExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTask);
        } else {
            mExecuter.execute(mTask, null); // the null is a hack
        }

    }

}
