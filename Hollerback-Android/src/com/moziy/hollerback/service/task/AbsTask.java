package com.moziy.hollerback.service.task;

public abstract class AbsTask implements Task {
    protected Task.Listener mTaskListener;

    @Override
    public void setTaskListener(Listener listener) {
        mTaskListener = listener;
    }

    @Override
    public Listener getTaskListener() {
        return mTaskListener;
    }

}
