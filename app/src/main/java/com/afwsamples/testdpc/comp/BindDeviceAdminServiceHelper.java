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

import android.annotation.TargetApi;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import com.afwsamples.testdpc.DeviceAdminReceiver;

/**
 * Helper class for {@link DevicePolicyManager#bindDeviceAdminServiceAsUser(
 * ComponentName, Intent, ServiceConnection, int, UserHandle)}.
 */
@TargetApi(Build.VERSION_CODES.O)
public class BindDeviceAdminServiceHelper<T> {
    private static final String TAG = "BindDeviceAdminService";

    private Context mContext;
    private DevicePolicyManager mDpm;
    private Intent mServiceIntent;
    private UserHandle mTargetUserHandle;
    private ServiceInterfaceConverter<T> mServiceInterfaceConverter;

    /**
     * @param context
     * @param serviceClass Which service we are going to bind with.
     * @param serviceInterfaceConverter Used to convert {@link IBinder} to service interface.
     * @param targetUserHandle Who we are talking to.
     */
    public BindDeviceAdminServiceHelper(
            Context context, Class<? extends Service> serviceClass,
            ServiceInterfaceConverter<T> serviceInterfaceConverter,
            UserHandle targetUserHandle) {
        mContext = context;
        mDpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mServiceInterfaceConverter = serviceInterfaceConverter;
        mServiceIntent = new Intent(context, serviceClass);
        mTargetUserHandle = targetUserHandle;
    }

    /**
     * Provide an easy way to run a one-off cross user call. You should run your service call in
     * {@link OnServiceConnectedListener#onServiceConnected(Object)}. Note that the
     * listener is always called in main thread, so if your service call is time consuming, please
     * make sure you either run it in worker thread or implement a callback mechanism.
     * @param listener Called when service is connected.
     * @return Whether the binding is successful.
     */
    public boolean crossUserCall(OnServiceConnectedListener<T> listener) {
        final ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                T service = mServiceInterfaceConverter.convert(iBinder);
                try {
                    listener.onServiceConnected(service);
                } catch (RemoteException e) {
                    Log.e(TAG, "onServiceConnected: ", e);
                } finally {
                    mContext.unbindService(this);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mContext.unbindService(this);
            }
        };
        return mDpm.bindDeviceAdminServiceAsUser(
                DeviceAdminReceiver.getComponentName(mContext),
                mServiceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE,
                mTargetUserHandle);
    }
}
