package com.moziy.hollerback.fragment.workers;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;

public class ActivityTaskWorker extends AbsTaskWorker {
    private static final String TAG = ActivityTaskWorker.class.getSimpleName();
    private Task.Listener mListener;
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
        mListener = (TaskClient) activity;
        if (mTask != null && mTask.isFinished()) {
            if (mTask.isSuccess())
                mListener.onTaskComplete(mTask);
            else
                mListener.onTaskError(mTask);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // lets execute the task
        // start executing the task
        mExecuter = new TaskExecuter();

        if (Build.VERSION.SDK_INT >= 11 && !mRunSerially) {
            mExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTask);
        } else {
            mExecuter.execute(mTask, null); // the null is a hack
        }

    }

}
