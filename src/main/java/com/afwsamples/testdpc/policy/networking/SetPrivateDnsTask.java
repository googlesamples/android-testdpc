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
import android.os.Build.VERSION_CODES;
import android.util.Log;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.policy.keymanagement.ShowToastCallback;

@TargetApi(VERSION_CODES.Q)
final class SetPrivateDnsTask extends AsyncTask<Void, Void, String> {
  public static final String TAG = "Networking";
  private final ShowToastCallback mCallback;
  private final DevicePolicyManager mDpm;
  private final ComponentName mComponent;
  private final int mMode;
  private final String mResolver;

  public SetPrivateDnsTask(
      DevicePolicyManager dpm,
      ComponentName component,
      int mode,
      String resolver,
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
      final int result;
      switch (mMode) {
        case DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME:
          result = mDpm.setGlobalPrivateDnsModeSpecifiedHost(mComponent, mResolver);
          break;
        case DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC:
          result = mDpm.setGlobalPrivateDnsModeOpportunistic(mComponent);
          break;
        default:
          throw new IllegalArgumentException("Invalid private dns mode: " + mMode);
      }
      switch (result) {
        case DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR:
          return null;
        case DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING:
          return "General failure to set the Private DNS mode";
        case DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING:
          return "Provided host doesn't serve DNS-over-TLS";
        default:
          return "Unexpected error setting private dns: " + result;
      }
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
