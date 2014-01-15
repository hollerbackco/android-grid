package com.moziy.hollerback.contacts.task;

import android.app.Activity;

import com.moziy.hollerback.service.task.TaskGroup;

/**
 * This class chains the tasks together
 * @author sajjad
 *
 */
public class ContactsTaskGroup extends TaskGroup {

    public ContactsTaskGroup(Activity activity) {
        GetUserContactsTask contactsTask = new GetUserContactsTask(activity);

        addTask(contactsTask);

        // this task relies on the first one to successfully execute
        addTask(new GetHBContactsTask(contactsTask));

    }
}
