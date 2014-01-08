package com.moziy.hollerback.fragment.workers;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;

public class FragmentTaskWorker extends AbsTaskWorker {
    private static final String TAG = FragmentTaskWorker.class.getSimpleName();

    public interface TaskClient extends Task.Listener {
        public Task getTask();
    }

    public static FragmentTaskWorker newInstance(boolean useSingleThreadPool) {
        Bundle args = new Bundle();
        args.putBoolean(SERIAL_EXECUTER_BUNDLE_ARG_KEY, useSingleThreadPool);

        FragmentTaskWorker worker = new FragmentTaskWorker();
        worker.setArguments(args);
        return worker;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTask = ((TaskClient) getTargetFragment()).getTask(); // start working on the task

        if (getArguments() != null) {
            Bundle args = getArguments();
            if (args.containsKey(SERIAL_EXECUTER_BUNDLE_ARG_KEY)) {
                mRunSerially = args.getBoolean(SERIAL_EXECUTER_BUNDLE_ARG_KEY);
            }
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "myId: " + getTag());

        mTaskClient = (TaskClient) getTargetFragment();
        setTaskListeners(mTask, false);

        if (mExecuter == null) {
            // start executing the task
            mExecuter = new TaskExecuter() {

                @Override
                protected void onPostExecute(Task result) {
                    super.onPostExecute(result);

                    Log.d(TAG, "removing self from fragment manager");
                    if (getFragmentManager() != null) { // if we've been removed completely, no need to remove
                        setTargetFragment(null, 0); // clear out the target fragment as to avoid state loss info
                        getFragmentManager().beginTransaction().remove(FragmentTaskWorker.this).commitAllowingStateLoss();
                    }

                    clearTaskListeners(mTask);

                }

            };

            if (Build.VERSION.SDK_INT >= 11 && !mRunSerially) {
                mExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTask);
            } else {
                mExecuter.execute(mTask, null); // the null is a hack
            }
        }

    }
}
