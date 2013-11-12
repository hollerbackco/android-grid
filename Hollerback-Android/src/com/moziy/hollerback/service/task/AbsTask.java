package com.moziy.hollerback.service.task;

public abstract class AbsTask implements Task {
    protected Task.Listener mTaskListener;
    protected boolean mIsSuccess = true;
    protected boolean mIsFinished = false;

    @Override
    public void setTaskListener(Listener listener) {
        mTaskListener = listener;
    }

    @Override
    public Listener getTaskListener() {
        return mTaskListener;
    }

    @Override
    public boolean isSuccess() {

        return mIsSuccess;
    }

    @Override
    public boolean isFinished() {
        return mIsFinished;
    }

}
