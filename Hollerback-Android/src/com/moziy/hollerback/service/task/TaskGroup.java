package com.moziy.hollerback.service.task;

import java.util.ArrayList;

public abstract class TaskGroup implements Task {

    private Task.Listener mListener;
    private final ArrayList<Task> mTasks = new ArrayList<Task>();
    private final ArrayList<Task> mFailedTasks = new ArrayList<Task>();
    private boolean mOverallStatus;
    private boolean mHasRun;
    private boolean mIsRunning;

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
    public void run() {
        mIsRunning = true;
        mOverallStatus = true;

        // run the tasks
        for (Task t : mTasks) {

            t.run();

            if (!t.isSuccess()) {
                mFailedTasks.add(t);
                mOverallStatus = false;
            }

        }

        mIsRunning = false;
        mHasRun = true;

    }

    public void reset() {
        mOverallStatus = true;
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

    @Override
    public Listener getTaskListener() {

        return mListener;
    }
}
