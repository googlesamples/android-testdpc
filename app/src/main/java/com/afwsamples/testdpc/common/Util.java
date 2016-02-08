/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.afwsamples.testdpc.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.afwsamples.testdpc.R;

/**
 * Common utility functions.
 */
public class Util {

    public static void showNotification(Context context, int titleId, String msg,
            int notificationId) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(titleId))
                .setContentText(msg)
                .setStyle(new Notification.BigTextStyle().bigText(msg))
                .build();
        mNotificationManager.notify(notificationId, notification);
    }
}
