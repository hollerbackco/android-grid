package com.moziy.hollerback.fragment.workers;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;

public abstract class AbsTaskWorker extends Fragment {
    private static final String TAG = AbsTaskWorker.class.getSimpleName();

    public interface TaskClient extends Task.Listener {
        public Task getTask();
    }

    private TaskExecuter mExecuter;
    private TaskClient mTaskClient;
    private Task mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTaskClient = ((TaskClient) getTargetFragment());
        mTask = mTaskClient.getTask();
        mTask.setTaskListener(mTaskClient);

        // start executing the task
        mExecuter = new TaskExecuter() {

            @Override
            protected void onPostExecute(Task result) {
                super.onPostExecute(result);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTaskClient = ((TaskClient) getTargetFragment());
        mTask.setTaskListener(mTaskClient);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTaskClient = null;
        mTask.setTaskListener(null);
    }
}
