package com.moziy.hollerback.service.task;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public class TaskExecuter extends AsyncTask<Task, Void, Task> {

    private static final String TAG = TaskExecuter.class.getSimpleName();
    private boolean mUseSingleThreadPool = false;

    public TaskExecuter() {
    }

    public TaskExecuter(boolean useSingleThreadPool) {
        mUseSingleThreadPool = useSingleThreadPool;
    }

    public void executeTask(Task task) {

        if (Build.VERSION.SDK_INT >= 11) {
            if (mUseSingleThreadPool)
                this.executeOnExecutor(SERIAL_EXECUTOR, task);
            else
                this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, task);
        } else {
            this.execute(task, null);
        }
    }

    @Override
    protected Task doInBackground(Task... params) {

        params[0].run();
        return params[0];
    }

    @Override
    protected void onPostExecute(Task result) {
        super.onPostExecute(result);

        if (result.getTaskListener() != null) {
            if (result.isSuccess()) {
                result.getTaskListener().onTaskComplete(result);
            } else {
                result.getTaskListener().onTaskError(result);
            }
        } else {
            Log.d(TAG, "not delivering results because of null callback");
        }
    }

}
