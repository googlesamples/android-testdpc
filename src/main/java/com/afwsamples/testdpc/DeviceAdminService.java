/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * To allow DPC process to be persistent and foreground.
 *
 * @see {@link android.app.admin.DeviceAdminService}
 */
@RequiresApi(api = VERSION_CODES.O)
public class DeviceAdminService extends android.app.admin.DeviceAdminService {

  private BroadcastReceiver mPackageChangedReceiver;

  @Override
  public void onCreate() {
    super.onCreate();
    registerPackageChangesReceiver();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterPackageChangesReceiver();
  }

  private void registerPackageChangesReceiver() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
    intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
    intentFilter.addDataScheme("package");
    mPackageChangedReceiver = new PackageMonitorReceiver();
    getApplicationContext().registerReceiver(mPackageChangedReceiver, intentFilter);
  }

  private void unregisterPackageChangesReceiver() {
    if (mPackageChangedReceiver != null) {
      getApplicationContext().unregisterReceiver(mPackageChangedReceiver);
      mPackageChangedReceiver = null;
    }
  }

  @Override
  protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    new ShellCommand(getApplicationContext(), writer, args).run();
  }
}
