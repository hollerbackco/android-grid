package com.moziy.hollerback.service.task;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.query.From;

public class ActiveAndroidTask<T extends Model> extends AbsTask {

    private List<T> mResult;
    private boolean mIsFinished;
    private From mFrom;

    public ActiveAndroidTask(From from) {
        mFrom = from;
    }

    @Override
    public void run() {

        mResult = mFrom.execute();
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

    public List<T> getResults() {
        return mResult;
    }

}
