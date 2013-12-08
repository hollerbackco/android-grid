package com.moziy.hollerback.fragment.workers;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.service.task.TaskGroup;

public class ActivityTaskWorker extends AbsTaskWorker {
    private static final String TAG = ActivityTaskWorker.class.getSimpleName();
    private TaskClient mTaskClient;
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
        setTaskListeners(mTask);
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

        setTaskListeners(mTask);

        if (Build.VERSION.SDK_INT >= 11 && !mRunSerially) {
            mExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTask);
        } else {
            mExecuter.execute(mTask, null); // the null is a hack
        }

    }

    private void setTaskListeners(Task t) {

        if (t == null) {
            Log.w(TAG, "null task");
            return;
        }

        // set a task listener for all tasks,
        Queue<Task> queue = new LinkedList<Task>();
        queue.add(t);

        while (!queue.isEmpty()) { // iterate through all tasks
            Task queuedTask = queue.poll();
            queuedTask.setTaskListener(mTaskClient);

            if (queuedTask.isFinished()) { // if the task is finished, then notify the listener

                if (queuedTask.isSuccess())
                    mTaskClient.onTaskComplete(queuedTask);
                else
                    mTaskClient.onTaskError(queuedTask);

            }

            if (queuedTask instanceof TaskGroup) {
                for (Task child : ((TaskGroup) queuedTask).getTasks()) {
                    queue.add(child);
                }

            }

        }

    }

    private void clearTaskListeners(Task t) {

        // clear task listener for all tasks,
        Queue<Task> queue = new LinkedList<Task>();
        queue.add(t);
        while (!queue.isEmpty()) {
            Task queuedTask = queue.poll();
            queuedTask.setTaskListener(null);

            if (queuedTask instanceof TaskGroup) {
                for (Task child : ((TaskGroup) queuedTask).getTasks()) {
                    queue.add(child);
                }

            }

        }
    }
}
