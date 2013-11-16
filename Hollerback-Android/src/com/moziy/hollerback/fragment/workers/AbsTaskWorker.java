package com.moziy.hollerback.fragment.workers;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.moziy.hollerback.service.task.Task;

public class AbsTaskWorker extends Fragment {
    private static final String TAG = AbsTaskWorker.class.getSimpleName();
    public static final String SERIAL_EXECUTER_BUNDLE_ARG_KEY = "SERIAL_EXECUTER";

    public interface TaskClient extends Task.Listener {
        public Task getTask();
    }

    public static AbsTaskWorker newInstance(boolean useSingleThreadPool) {
        Bundle args = new Bundle();
        args.putBoolean(SERIAL_EXECUTER_BUNDLE_ARG_KEY, useSingleThreadPool);

        AbsTaskWorker worker = new AbsTaskWorker();
        worker.setArguments(args);
        return worker;
    }

    private AsyncTask<Task, Task, Task> mExecuter;
    private Task mTask;
    private boolean mRunSerially = false;

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

        mTask.setTaskListener(((TaskClient) getTargetFragment())); // update the task listener during a config change

        if (mExecuter == null) {
            // start executing the task
            mExecuter = new AsyncTask<Task, Task, Task>() {

                @Override
                protected Task doInBackground(Task... params) {
                    params[0].run();
                    return params[0];
                }

                @Override
                protected void onPostExecute(Task result) {
                    super.onPostExecute(result);

                    if (mTask.getTaskListener() != null) {
                        if (result.isSuccess()) {
                            mTask.getTaskListener().onTaskComplete(result);
                        } else {
                            mTask.getTaskListener().onTaskError(result);
                        }
                    } else {
                        Log.d(TAG, "not delivering results because of null callback");
                    }

                    Log.d(TAG, "removing self from fragment manager");
                    getFragmentManager().beginTransaction().remove(AbsTaskWorker.this).commitAllowingStateLoss();

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
