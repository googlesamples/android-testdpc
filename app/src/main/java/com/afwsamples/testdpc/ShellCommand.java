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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides a CLI (command-line interface) to TestDPC through {@code dumpsys}.
 *
 * <p>Usage: {@code adb shell dumpsys activity service --user USER_ID com.afwsamples.testdpc CMD}.
 *
 */
final class ShellCommand {
    private static final String TAG = "TestDPCShellCommand";

    private static final String CMD_CREATE_USER = "create-user";
    private static final String CMD_SET_USER_ICON = "set-user-icon";
    private static final String CMD_REMOVE_USER = "remove-user";
    private static final String CMD_SWITCH_USER = "switch-user";
    private static final String CMD_START_USER_BG = "start-user-in-background";
    private static final String CMD_STOP_USER = "stop-user";
    private static final String CMD_LIST_USER_RESTRICTIONS = "list-user-restrictions";
    private static final String CMD_SET_USER_RESTRICTION = "set-user-restriction";
    private static final String CMD_IS_USER_AFFILIATED = "is-user-affiliated";
    private static final String CMD_SET_AFFILIATION_IDS = "set-affiliation-ids";
    private static final String CMD_GET_AFFILIATION_IDS = "get-affiliation-ids";
    private static final String CMD_HELP = "help";
    private static final String CMD_LOCK_NOW = "lock-now";
    private static final String CMD_REBOOT = "reboot";
    private static final String CMD_WIPE_DATA = "wipe-data";
    private static final String CMD_REQUEST_BUGREPORT = "request-bugreport";
    private static final String CMD_SET_NETWORK_LOGGING = "set-network-logging";
    private static final String CMD_SET_ORGANIZATION_NAME = "set-organization-name";
    private static final String CMD_GET_ORGANIZATION_NAME = "get-organization-name";
    private static final String CMD_SET_USER_CONTROL_DISABLED_PACKAGES =
            "set-user-control-disabled-packages";
    private static final String CMD_GET_USER_CONTROL_DISABLED_PACKAGES =
            "get-user-control-disabled-packages";
    private static final String CMD_SET_PERMITTED_INPUT_METHODS_PARENT =
        "set-permitted-input-methods-parent";
    private static final String ARG_FLAGS = "--flags";

    private final Context mContext;
    private final PrintWriter mWriter;
    private final String[] mArgs;
    private final DevicePolicyManagerGateway mDevicePolicyManagerGateway;

    public ShellCommand(@NonNull Context context, @NonNull PrintWriter writer,
            @Nullable String[] args) {
        mContext = context;
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
            case CMD_SET_USER_ICON:
                execute(() -> setUserIcon());
                break;
            case CMD_REMOVE_USER:
                execute(() -> removeUser());
                break;
            case CMD_SWITCH_USER:
                execute(() -> switchUser());
                break;
            case CMD_START_USER_BG:
                execute(() -> startUserInBackground());
                break;
            case CMD_STOP_USER:
                execute(() -> stopUser());
                break;
            case CMD_IS_USER_AFFILIATED:
                execute(() -> isUserAffiliated());
                break;
            case CMD_SET_AFFILIATION_IDS:
                execute(() -> setAffiliationIds());
                break;
            case CMD_GET_AFFILIATION_IDS:
                execute(() -> getAffiliationIds());
                break;
            case CMD_LIST_USER_RESTRICTIONS:
                execute(() -> listUserRestrictions());
                break;
            case CMD_SET_USER_RESTRICTION:
                execute(() -> setUserRestriction());
                break;
            case CMD_LOCK_NOW:
                execute(() -> lockNow());
                break;
            case CMD_REBOOT:
                execute(() -> reboot());
                break;
            case CMD_WIPE_DATA:
                execute(() -> wipeData());
                break;
            case CMD_REQUEST_BUGREPORT:
                execute(() -> requestBugreport());
                break;
            case CMD_SET_NETWORK_LOGGING:
                execute(() -> setNetworkLogging());
                break;
            case CMD_SET_ORGANIZATION_NAME:
                execute(() -> setOrganizationName());
                break;
            case CMD_GET_ORGANIZATION_NAME:
                execute(() -> getOrganizationName());
                break;
            case CMD_SET_USER_CONTROL_DISABLED_PACKAGES:
                execute(() -> setUserControlDisabledPackages());
                break;
            case CMD_GET_USER_CONTROL_DISABLED_PACKAGES:
                execute(() -> getUserControlDisabledPackages());
                break;
            case CMD_SET_PERMITTED_INPUT_METHODS_PARENT:
                execute(() -> setPermittedInputMethodsOnParent());
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
        File setIconRootDir = UserIconContentProvider.getStorageDirectory(mContext);
        mWriter.printf("\t%s <FILE> - sets the user icon using the bitmap located at the given "
                + "file,\n"
                + "\t\twhich must be located in the user's `%s` directory.\n"
                + "\t\tFor user 0, you can use `adb push` to push a local file to that directory\n"
                + "\t\t(%s),\n"
                + "\t\tbut for other users you need to switch to that user and use its content "
                + "provider \n"
                + "\t\t(for example, `adb shell content write --user 10 --uri \n"
                + "\t\tcontent://%s/icon.png < /tmp/icon.png`)\n", CMD_SET_USER_ICON,
                setIconRootDir.getName(), setIconRootDir, UserIconContentProvider.AUTHORITY);
        mWriter.printf("\t%s <USER_SERIAL_NUMBER> - remove the given user\n", CMD_REMOVE_USER);
        mWriter.printf("\t%s <USER_SERIAL_NUMBER> - switch the given user to foreground\n",
                CMD_SWITCH_USER);
        mWriter.printf("\t%s <USER_SERIAL_NUMBER> - start the given user in the background\n",
                CMD_START_USER_BG);
        mWriter.printf("\t%s - checks if the user is affiliated with the device\n",
                CMD_IS_USER_AFFILIATED);
        mWriter.printf("\t%s [ID1] [ID2] [IDN] - sets the user affiliation ids (or clear them if "
                + "no ids is passed)\n", CMD_SET_AFFILIATION_IDS);
        mWriter.printf("\t%s - gets the user affiliation ids\n",
                CMD_GET_AFFILIATION_IDS);
        mWriter.printf("\t%s - list the user restrictions\n", CMD_LIST_USER_RESTRICTIONS);
        mWriter.printf("\t%s <RESTRICTION> <true|false>- set the given user restriction\n",
                CMD_SET_USER_RESTRICTION);
        mWriter.printf("\t%s [FLAGS]- lock the device (now! :-)\n", CMD_LOCK_NOW);
        mWriter.printf("\t%s - reboot the device\n", CMD_REBOOT);
        mWriter.printf("\t%s [FLAGS]- factory reset the device\n", CMD_WIPE_DATA);
        mWriter.printf("\t%s - request a bugreport\n", CMD_REQUEST_BUGREPORT);
        mWriter.printf("\t%s <true|false> - enable / disable network logging\n",
                CMD_SET_NETWORK_LOGGING);
        mWriter.printf("\t%s [NAME] - set the organization name; use it without a name to reset\n",
                CMD_SET_ORGANIZATION_NAME);
        mWriter.printf("\t%s - get the organization name\n", CMD_GET_ORGANIZATION_NAME);
        mWriter.printf("\t%s [PKG1] [PKG2] [PKGN] - sets the packages that the user cannot force "
                + "stop or clear data. Use no args to reset it.\n",
                CMD_SET_USER_CONTROL_DISABLED_PACKAGES);
        mWriter.printf("\t%s - gets the packages that the user cannot force stop or "
                + "clear data\n", CMD_GET_USER_CONTROL_DISABLED_PACKAGES);
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

    private void setUserIcon() {
        if (!hasExactlyNumberOfArguments(2)) return;

        String name = mArgs[1];
        Log.i(TAG, "setUserIcon(): name=" + name);

        File file = UserIconContentProvider.getFile(mContext, name);

        if (!file.isFile()) {
            mWriter.printf("Could not open file %s\n", name);
            return;
        }

        String absolutePath = file.getAbsolutePath();
        Log.i(TAG, "setUserIcon(): path=" + absolutePath);
        Bitmap icon = BitmapFactory.decodeFile(absolutePath, /* bmOptions= */ null);
        if (icon == null) {
            mWriter.printf("Could not create bitmap from file %s\n", absolutePath);
            return;
        }
        mDevicePolicyManagerGateway.setUserIcon(icon,
                (v) -> onSuccess("User icon created from file %s", absolutePath),
                (e) -> onError(e, "Error creating user icon from file %s", absolutePath));
    }

    private void removeUser() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.removeUser(userHandle,
                (v) -> onSuccess("User %s removed", userHandle),
                (e) -> onError(e, "Error removing user %s", userHandle));
    }

    private void switchUser() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.switchUser(userHandle,
                (v) -> onSuccess("User %s switched", userHandle),
                (e) -> onError(e, "Error switching user %s", userHandle));
    }

    private void startUserInBackground() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.startUserInBackground(userHandle,
                (v) -> onSuccess("User %s started in background", userHandle),
                (e) -> onError(e, "Error starting user %s in background", userHandle));
    }

    private void stopUser() {
        UserHandle userHandle = getUserHandleArg(1);
        if (userHandle == null) return;

        mDevicePolicyManagerGateway.stopUser(userHandle,
                (v) -> onSuccess("User %s stopped", userHandle),
                (e) -> onError(e, "Error stopping user %s", userHandle));
    }

    private void getAffiliationIds() {
        Set<String> ids = mDevicePolicyManagerGateway.getAffiliationIds();
        if (ids.isEmpty()) {
            mWriter.println("no affiliation ids");
            return;
        }
        mWriter.printf("%d affiliation ids: %s\n", ids.size(), ids);
    }

    private void setAffiliationIds() {
        Set<String> ids = getSetFromAllArgs();
        Log.i(TAG, "setAffiliationIds(): ids=" + ids);
        mDevicePolicyManagerGateway.setAffiliationIds(ids);

        getAffiliationIds();
    }

    private void isUserAffiliated() {
        mWriter.println(mDevicePolicyManagerGateway.isAffiliatedUser());
    }


    private void listUserRestrictions() {
        Log.i(TAG, "listUserRestrictions()");

        print("user restrictions", mDevicePolicyManagerGateway.getUserRestrictions());
    }

    private void setUserRestriction() {
        // TODO(b/171350084): check args
        String userRestriction = mArgs[1];
        boolean enabled = Boolean.parseBoolean(mArgs[2]);
        Log.i(TAG, "setUserRestriction(" + userRestriction + ", " + enabled + ")");

        mDevicePolicyManagerGateway.setUserRestriction(userRestriction, enabled,
                (v) -> onSuccess("User restriction '%s' set to %b", userRestriction, enabled),
                (e) -> onError(e, "Error setting user restriction '%s' to %b", userRestriction,
                        enabled));
    }

    private void lockNow() {
        Integer flags = getIntArg(/* index= */ 1);
        if (flags == null) {
            Log.i(TAG, "lockNow()");
            mDevicePolicyManagerGateway.lockNow(
                    (v) -> onSuccess("Device locked"),
                    (e) -> onError(e, "Error locking device"));
        } else {
            Log.i(TAG, "lockNow(" + flags + ")");
            mDevicePolicyManagerGateway.lockNow(flags,
                    (v) -> onSuccess("Device locked"),
                    (e) -> onError(e, "Error locking device"));
        }
    }

    private void reboot() {
        Log.i(TAG, "reboot()");
        mDevicePolicyManagerGateway.reboot(
                (v) -> onSuccess("Device rebooted"),
                (e) -> onError(e, "Error rebooting device"));
    }

    private void wipeData() {
        Integer flags = getIntArg(/* index= */ 1);
        Log.i(TAG, "wipeData()");
        mDevicePolicyManagerGateway.wipeData(flags == null ? 0 : flags,
                (v) -> onSuccess("Data wiped"),
                (e) -> onError(e, "Error wiping data"));
    }

    private void requestBugreport() {
        Log.i(TAG, "requestBugreport()");
        mDevicePolicyManagerGateway.requestBugreport(
                (v) -> onSuccess("Bugreport requested"),
                (e) -> onError(e, "Error requesting bugreport"));
    }

    private void setNetworkLogging() {
        // TODO(b/171350084): check args
        boolean enabled = Boolean.parseBoolean(mArgs[1]);
        Log.i(TAG, "setNetworkLogging(" + enabled + ")");

        mDevicePolicyManagerGateway.setNetworkLogging(enabled,
                (v) -> onSuccess("Network logging set to %b", enabled),
                (e) -> onError(e, "Error setting network logging to %b", enabled));
    }

    private void setOrganizationName() {
        String title = mArgs.length > 1 ? mArgs[1] : null;
        Log.i(TAG, "setOrganizationName(" + title + ")");

        mDevicePolicyManagerGateway.setOrganizationName(title,
                (v) -> onSuccess("Organization name set to %s", title),
                (e) -> onError(e, "Error setting Organization name to %s", title));
    }

    private void getOrganizationName() {
        CharSequence title = mDevicePolicyManagerGateway.getOrganizationName();
        if (title == null) {
            mWriter.println("Not set");
            return;
        }
        mWriter.println(title);
    }

    private void setUserControlDisabledPackages() {
        List<String> pkgs = getListFromAllArgs();
        Log.i(TAG, "setUserControlDisabledPackages(" + pkgs + ")");

        mDevicePolicyManagerGateway.setUserControlDisabledPackages(pkgs,
                (v) -> onSuccess("User-control disabled packages set to %s", pkgs),
                (e) -> onError(e, "Error setting User-control disabled packages to %s", pkgs));
    }

    private void getUserControlDisabledPackages() {
        List<String> pkgs = mDevicePolicyManagerGateway.getUserControlDisabledPackages();
        pkgs.forEach((p) -> mWriter.println(p));
    }

    private void setPermittedInputMethodsOnParent() {
        List<String> inputMethods = getListFromAllArgs();
        Log.i(TAG, "setPermittedInputMethodsOnParent(" + inputMethods + ")");

        DevicePolicyManagerGateway parentDpmGateway =
            DevicePolicyManagerGatewayImpl.forParentProfile(mContext);
        parentDpmGateway.setPermittedInputMethods(inputMethods);
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

    private void print(String name, Collection<String> collection) {
        if (collection.isEmpty()) {
            mWriter.printf("No %s\n", name);
            return;

        }
        int size = collection.size();
        mWriter.printf("%d %s:\n", size, name);
        collection.forEach((s) -> mWriter.printf("  %s\n", s));
    }

    private UserHandle getUserHandleArg(int index) {
        // TODO(b/171350084): check args
        long serialNumber = Long.parseLong(mArgs[index]);
        UserHandle userHandle = mDevicePolicyManagerGateway.getUserHandle(serialNumber);
        if (userHandle == null) {
            mWriter.printf("No user handle for serial number %d\n", serialNumber);
        }
        return userHandle;
    }

    /** Gets an optional {@code int} argument at index {@code index}. */
    @Nullable
    private Integer getIntArg(int index) {
        return mArgs.length <= index ? null : Integer.parseInt(mArgs[index]);
    }

    private boolean hasExactlyNumberOfArguments(int number) {
        if (mArgs.length != number) {
            mWriter.printf("Must have exactly %d arguments: %s\n", number, Arrays.toString(mArgs));
            return false;
        }
        return true;
    }

    private List<String> getListFromAllArgs() {
        List<String> list = new ArrayList<>(mArgs.length - 1);
        for (int i = 1; i < mArgs.length; i++) {
            list.add(mArgs[i]);
        }
        return list;
    }

    private Set<String> getSetFromAllArgs() {
        return new LinkedHashSet<String>(getListFromAllArgs());
    }
}
