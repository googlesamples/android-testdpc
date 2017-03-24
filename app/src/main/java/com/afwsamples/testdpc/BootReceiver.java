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

package com.afwsamples.testdpc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.UserHandle;

import com.afwsamples.testdpc.common.Util;
import com.afwsamples.testdpc.comp.BindDeviceAdminServiceHelper;
import com.afwsamples.testdpc.comp.DeviceOwnerService;
import com.afwsamples.testdpc.comp.IDeviceOwnerService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (!Util.isProfileOwner(context)
                    || Util.getBindDeviceAdminTargetUsers(context).size() == 0) {
                return;
            }
            // We are a profile owner and can bind to the device owner - let's notify the device
            // owner that we are up and running (i.e. our user was just started and/or unlocked)
            UserHandle targetUser = Util.getBindDeviceAdminTargetUsers(context).get(0);
            BindDeviceAdminServiceHelper<IDeviceOwnerService> helper =
                    createBindDeviceOwnerServiceHelper(context, targetUser);
            helper.crossUserCall(service -> service.notifyUserIsUnlocked(Process.myUserHandle()));
        }
    }

    private BindDeviceAdminServiceHelper<IDeviceOwnerService> createBindDeviceOwnerServiceHelper(
            Context context, UserHandle targetUserHandle) {
        return new BindDeviceAdminServiceHelper<>(
                context,
                DeviceOwnerService.class,
                IDeviceOwnerService.Stub::asInterface,
                targetUserHandle);
    }
}
