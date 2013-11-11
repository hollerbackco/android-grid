package com.moziy.hollerback.fragment;

import java.util.ArrayList;
import java.util.Queue;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.activeandroid.query.Select;
import com.moziy.hollerback.database.ActiveRecordFields;
import com.moziy.hollerback.fragment.workers.AbsTaskWorker;
import com.moziy.hollerback.fragment.workers.AbsTaskWorker.TaskClient;
import com.moziy.hollerback.model.VideoModel;
import com.moziy.hollerback.service.task.ActiveAndroidTask;
import com.moziy.hollerback.service.task.Task;

public class ConversationFragment extends SherlockFragment implements TaskClient {

    public ConversationFragment newInstance(long conversationId) {
        ConversationFragment c = new ConversationFragment();
        return c;
    }

    private long mConvoId;
    private ArrayList<VideoModel> mVideos;

    private Queue<Task> mTaskQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AbsTaskWorker worker = new AbsTaskWorker() {
        };
        worker.setTargetFragment(this, 0);
    }

    @Override
    public void onTaskComplete(Task t) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTaskError(Task t) {
        // TODO Auto-generated method stub

    }

    @Override
    public Task getTask() {
        return new ActiveAndroidTask<VideoModel>(new Select().from(VideoModel.class).where(
                ActiveRecordFields.C_VID_CONV_ID + " = " + mConvoId + " AND " + ActiveRecordFields.C_VID_ISREAD + " = " + "false"));
    }
}
