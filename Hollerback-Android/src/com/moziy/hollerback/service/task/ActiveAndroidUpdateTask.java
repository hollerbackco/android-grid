package com.moziy.hollerback.service.task;

import com.activeandroid.query.Set;

public class ActiveAndroidUpdateTask extends AbsTask {

    private boolean mIsFinished;
    private Set mSet;

    public ActiveAndroidUpdateTask(Set set) {
        mSet = set;
    }

    @Override
    public void run() {

        mSet.execute();
        mIsFinished = true;
    }

    @Override
    public boolean isSuccess() {

        return true;
    }

    @Override
    public boolean isFinished() {

        return mIsFinished;
    }

}
