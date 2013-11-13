package com.moziy.hollerback.service.task;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public class TaskExecuter extends AsyncTask<Task, Void, Task> {

    private static final String TAG = TaskExecuter.class.getSimpleName();

    public void executeTask(Task task) {
        if (Build.VERSION.SDK_INT > 11) {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, task);
        } else {
            this.execute(task);
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

        if (result.isSuccess()) {
            Log.d(TAG, "task successfull");
            result.getTaskListener().onTaskComplete(result);
        } else {
            Log.d(TAG, "task failed");
            result.getTaskListener().onTaskError(result);
        }
    }

}
