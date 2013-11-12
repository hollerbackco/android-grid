package com.moziy.hollerback.fragment.workers;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.moziy.hollerback.service.task.Task;

public abstract class AbsTaskWorker extends Fragment {
    private static final String TAG = AbsTaskWorker.class.getSimpleName();

    public interface TaskClient extends Task.Listener {
        public Task getTask();
    }

    private AsyncTask<Task, Task, Task> mExecuter;
    private Task mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mTask = ((TaskClient) getTargetFragment()).getTask(); // start working on the task

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

            if (Build.VERSION.SDK_INT > 11) {
                mExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTask);
            } else {
                mExecuter.execute(mTask);
            }
        }

    }

}
