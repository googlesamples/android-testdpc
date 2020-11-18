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
    private static final String CMD_REMOVE_USER = "remove-user";
    private static final String CMD_HELP = "help";
    private static final String CMD_LOCK_NOW = "lock-now";
    private static final String ARG_FLAGS = "--flags";
    private static final String CMD_WIPE_DATA = "wipe-data";

    private final PrintWriter mWriter;
    private final String[] mArgs;
    private final DevicePolicyManagerGateway mDevicePolicyManagerGateway;

    public ShellCommand(@NonNull Context context, @NonNull PrintWriter writer,
            @Nullable String[] args) {
        mWriter = writer;
        mArgs = args;
        mDevicePolicyManagerGateway = new DevicePolicyManagerGatewayImpl(context);

        Log.d(TAG, "args=" + Arrays.toString(args));
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
            case CMD_REMOVE_USER:
                execute(() -> removeUser());
                break;
            case CMD_LOCK_NOW:
                execute(() -> lockNow());
                break;
            case CMD_WIPE_DATA:
                execute(() -> wipeData());
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
        mWriter.printf("\t%s <USER_SERIAL_NUMBER> - remove the given user\n", CMD_REMOVE_USER);
        mWriter.printf("\t%s - locks the device (now! :-)\n", CMD_LOCK_NOW);
    }

    private void createUser() {
        // TODO(b/171350084): once more commands are added, add a generic argument parsing
        // mechanism like getRequiredArg(), getOptionalArg, etc...
        int nextArgIndex = 1;
        String nextArg = null;

        final String name;
        int flags = 0;

        if (mArgs.length > nextArgIndex) {
            nextArg = mArgs[nextArgIndex++];
            if (ARG_FLAGS.equals(nextArg)) {
                flags = Integer.parseInt(mArgs[nextArgIndex++]);
                if (mArgs.length > nextArgIndex) {
                    name = mArgs[nextArgIndex++];
                }
                else {
                    name = null;
                }
            } else {
                name = nextArg;
            }
        } else {
            name = null;
        }
        Log.i(TAG, "createUser(): name=" + name + ", flags=" + flags);

        mDevicePolicyManagerGateway.createAndManageUser(name, flags,
                (u) -> onSuccess("User created: %s", u),
                (e) -> onError(e, "Error creating user %s", name));
    }

    private void removeUser() {
        long serialNumber = Long.parseLong(mArgs[1]);
        Log.i(TAG, "removeUser(): serialNumber=" + serialNumber);

        mDevicePolicyManagerGateway.removeUser(serialNumber,
                (u) -> onSuccess("User removed"),
                (e) -> onError(e, "Error removing user %d", serialNumber));
    }

    private void lockNow() {
        // TODO(b/171350084): add flags
        Log.i(TAG, "lockNow()");
        mDevicePolicyManagerGateway.lockNow(
                (v) -> onSuccess("Device locked"),
                (e) -> onError(e, "Error locking device"));
    }

    private void wipeData() {
        // TODO(b/171350084): add flags
        Log.i(TAG, "wipeData()");
        mDevicePolicyManagerGateway.wipeData(/* flags= */ 0,
                (v) -> onSuccess("Data wiped"),
                (e) -> onError(e, "Error wiping data"));
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
