/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.afwsamples.testdpc;

import java.io.PrintWriter;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.util.Log;

/**
 * Provides a CLI (command-line interface) to TestDPC through {@code dumpsys}.
 *
 * <p>Usage: <@code adb shell dumpsys activity service com.afwsamples.testdpc}.
 *
 */
final class ShellCommand {
    // TODO(b/171350084): add unit tests

    private static final String TAG = "TestDPCShellCommand";

    private static final String CMD_CREATE_USER = "create-user";
    private static final String CMD_HELP = "help";
    private static final String ARG_FLAGS = "--flags";

    private final PrintWriter mWriter;
    private final String[] mArgs;
    private final DevicePolicyManager mDevicePolicyManager;
    private final ComponentName mAdminComponentName;

    public ShellCommand(@NonNull Context context, @NonNull PrintWriter writer,
            @Nullable String[] args) {
        mWriter = writer;
        mArgs = args;
        mDevicePolicyManager = context.getSystemService(DevicePolicyManager.class);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(context);
        Log.d(TAG, "context: " + context + ", admin: " + mAdminComponentName
                + ", args: " + Arrays.toString(args));
    }

    public void run() {
        if (mArgs == null || mArgs.length == 0) {
            showUsage();
            return;
        }
        String cmd = mArgs[0];
        switch (cmd) {
            case CMD_HELP:
                showUsage();
                break;
            case CMD_CREATE_USER:
                execute(() -> createUser());
                break;
            default:
                mWriter.printf("Invalid command: %s\n\n", cmd);
                showUsage();
        }
    }

    private void showUsage() {
        mWriter.printf("Usage:\n\n");
        mWriter.printf("\t%s - show this help\n", CMD_HELP);
        mWriter.printf("\t%s [%s FLAGS] [NAME] - create a user with the optional flags and name\n",
                CMD_CREATE_USER, ARG_FLAGS);
    }

    private void createUser() {
        // TODO(b/171350084): once more commands are added, add a generic argument parsing
        // mechanism like getRequiredArg(), getOptionalArg, etc...
        int nextArgIndex = 1;
        String nextArg = null;

        String name = null;
        int flags = 0;

        if (mArgs.length > nextArgIndex) {
            nextArg = mArgs[nextArgIndex++];
            if (ARG_FLAGS.equals(nextArg)) {
                flags = Integer.parseInt(mArgs[nextArgIndex++]);
                if (mArgs.length > nextArgIndex) {
                    name = mArgs[nextArgIndex++];
                }
            } else {
                name = nextArg;
            }
        }
        Log.d(TAG, "createUser(): name=" + name + ", flags=" + flags);

        try {
            UserHandle userHandle = mDevicePolicyManager.createAndManageUser(mAdminComponentName,
                    name, mAdminComponentName, /* adminExtras= */ null, flags);
            onSuccess("User created: %s", userHandle);
        } catch (Exception e) {
            onError(e, "Error creating user %s", name);
        }
    }

    private void execute(@NonNull Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            // Must explicitly catch and show generic exceptions (like NumberFormatException parsing
            // args), otherwise they'dbe logcat'ed on AndroidRuntime and not surfaced to caller
            onError(e, "error executing %s", Arrays.toString(mArgs));
        }
    }

    private void onSuccess(@NonNull String pattern, @Nullable Object...args) {
        String msg = String.format(pattern, args);
        Log.d(TAG, msg);
        mWriter.println(msg);
    }

    private void onError(@NonNull Exception e, @NonNull String pattern, @Nullable Object...args) {
        String msg = String.format(pattern, args);
        Log.e(TAG, msg, e);
        mWriter.printf("%s: %s\n", msg, e);
    }
}
