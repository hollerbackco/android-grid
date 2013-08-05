/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moziy.hollerback.service;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;


/**
 * Controller to start and stop a service. The serivce will update a status bar
 * notification every 5 seconds for a minute.
 */
public class NotifyingController extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationManager mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNM.cancelAll();
        finish();
    }
}

