/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.afwsamples.testdpc.policy.networking;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.AsyncTask;
import android.util.Log;

import com.afwsamples.testdpc.policy.keymanagement.ShowToastCallback;
import com.afwsamples.testdpc.R;

@TargetApi(29)
final class SetPrivateDnsTask extends AsyncTask<Void, Void, String> {
    public static final String TAG = "Networking";
    private final ShowToastCallback mCallback;
    private final DevicePolicyManager mDpm;
    private final ComponentName mComponent;
    private final int mMode;
    private final String mResolver;

    public SetPrivateDnsTask(
            DevicePolicyManager dpm, ComponentName component, int mode, String resolver,
            ShowToastCallback callback) {
        mDpm = dpm;
        mComponent = component;
        mCallback = callback;
        mMode = mode;
        mResolver = resolver;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            mDpm.setGlobalPrivateDns(mComponent, mMode, mResolver);
            return null;
        } catch (SecurityException | IllegalArgumentException e) {
            Log.w(TAG, "Failed to invoke, cause", e);
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String error) {
        if (error == null) {
            mCallback.showToast(R.string.setting_private_dns_succeess);
        } else {
            mCallback.showToast(R.string.setting_private_dns_failure, error);
        }
    }
}
