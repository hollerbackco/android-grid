package com.moziy.hollerback.fragment.workers;

import java.util.LinkedList;
import java.util.Queue;

import android.support.v4.app.Fragment;

import com.moziy.hollerback.fragment.workers.FragmentTaskWorker.TaskClient;
import com.moziy.hollerback.service.task.Task;
import com.moziy.hollerback.service.task.TaskExecuter;
import com.moziy.hollerback.service.task.TaskGroup;

public class AbsTaskWorker extends Fragment {

    public static final String SERIAL_EXECUTER_BUNDLE_ARG_KEY = "SERIAL_EXECUTER";
    protected TaskExecuter mExecuter;
    protected Task mTask;
    protected TaskClient mTaskClient;
    protected boolean mRunSerially = false;

    protected void clearTaskListeners(Task t) {

        if (t == null)
            return;

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

    protected void setTaskListeners(Task t, boolean notify) {
        if (t == null) {
            return;
        }

        // set a task listener for all tasks,
        Queue<Task> queue = new LinkedList<Task>();
        queue.add(t);

        while (!queue.isEmpty()) { // iterate through all tasks
            Task queuedTask = queue.poll();
            queuedTask.setTaskListener(mTaskClient);

            if (notify && queuedTask.isFinished()) { // if the task is finished, then notify the listener

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

}
