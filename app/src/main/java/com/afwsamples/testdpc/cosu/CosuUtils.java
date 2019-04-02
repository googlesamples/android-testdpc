/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.afwsamples.testdpc.cosu;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for various operations necessary during COSU set up.
 */
/* package */ class CosuUtils {
    public static final String TAG = "CosuSetup";
    public static final boolean DEBUG = false;

    public static final int MSG_DOWNLOAD_COMPLETE = 1;
    public static final int MSG_DOWNLOAD_TIMEOUT = 2;
    public static final int MSG_INSTALL_COMPLETE = 3;

    private static final int DOWNLOAD_TIMEOUT_MILLIS = 120_000;

    public static Long startDownload(DownloadManager dm, Handler handler, String location) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(location));
        Long id = dm.enqueue(request);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_DOWNLOAD_TIMEOUT, id),
                DOWNLOAD_TIMEOUT_MILLIS);
        if (DEBUG) Log.d(TAG, "Starting download: DownloadId=" + id);
        return id;
    }
}


