package com.moziy.hollerback.service.task;

public interface Task extends Runnable {

    public static interface Listener {
        public void onTaskComplete(Task t);

        public void onTaskError(Task t);
    }

    public boolean isSuccess();

    public boolean isFinished();

    public void setTaskListener(Task.Listener listener);

    public Task.Listener getTaskListener();

}
