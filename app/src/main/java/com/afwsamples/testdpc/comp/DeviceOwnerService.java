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

package com.afwsamples.testdpc.comp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.Util;

public class DeviceOwnerService extends Service {
    private static final String TAG = "DeviceOwnerService";

    private Binder mBinder;

    @Override
    public void onCreate() {
        mBinder = new DeviceOwnerServiceImpl(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    static class DeviceOwnerServiceImpl extends IDeviceOwnerService.Stub {
        private Context mContext;

        private DeviceOwnerServiceImpl(Context context) {
            mContext = context;
        }

        @Override
        public void notifyUserIsUnlocked() throws RemoteException {
            Util.showNotification(mContext, R.string.profile_status,
                    mContext.getString(R.string.profile_is_unlocked), 0);
            Log.d(TAG, "notifyUserIsUnlocked() called");
        }
    }
}
