package com.moziy.hollerback.service.task;

import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

public class TaskGroup extends AbsTask {
    private static final String TAG = TaskGroup.class.getSimpleName();

    private final ArrayList<Task> mTasks = new ArrayList<Task>();
    private final ArrayList<Task> mFailedTasks = new ArrayList<Task>();
    volatile private boolean mHasRun;
    volatile private boolean mIsRunning; // make sure other thread sees the status

    private final Handler mHandler = new Handler();

    public TaskGroup() {
    }

    public TaskGroup(ArrayList<Task> tasks) {
        mTasks.addAll(tasks);
    }

    public void addTask(Task task) {
        mTasks.add(task);
    }

    public boolean removeTask(Task task) {
        return mTasks.remove(task);
    }

    @Override
    public void run() { // typically runs in the background thread
        mIsRunning = true;
        mIsSuccess = true;

        for (int i = 0; i < mTasks.size(); i++) {
            final Task t = mTasks.get(i);

            t.run();

            if (!t.isSuccess()) {
                mFailedTasks.add(t);
                mIsSuccess = false;

            }

            postResult(t);
        }

        mIsRunning = false;
        mHasRun = true;

    }

    protected void postResult(final Task t) {
        if (t.getTaskListener() != null) {
            if (!t.isSuccess()) {
                Log.d(TAG, "posting error");
                t.getTaskListener().onTaskError(t);
            } else {
                Log.d(TAG, "posting success");
                t.getTaskListener().onTaskComplete(t);

            }

        }
    }

    /**
     * Resets this task so that the same download tasks can run
     */
    public void reset() {
        if (isRunning()) { // TODO - Sajjad: Remove from prod
            throw new IllegalStateException("can't reset while task is running!");
        }
        mIsSuccess = true;
        mHasRun = false;
        mIsRunning = false;
        mFailedTasks.clear();

    }

    public ArrayList<Task> getTasks() {
        return mTasks;
    }

    public ArrayList<Task> getFailedTasks() {
        return mFailedTasks;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public boolean hasRun() {
        return mHasRun;
    }

}
