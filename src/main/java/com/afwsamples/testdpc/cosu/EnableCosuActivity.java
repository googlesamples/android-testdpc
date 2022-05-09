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

import android.app.Activity;
import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.PackageInstallationUtils;
import com.afwsamples.testdpc.policy.locktask.KioskModeActivity;
import com.google.android.setupdesign.SetupWizardLayout;
import com.google.android.setupdesign.view.NavigationBar;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This activity is started after provisioning is complete in {@link DeviceAdminReceiver} for COSU
 * devices. It loads a config file and downloads, install, hides and enables apps according to the
 * data in the config file.
 */
public class EnableCosuActivity extends Activity {
  public static final String BUNDLE_KEY_COSU_CONFIG = "cosu-demo-config-location";

  private static final String MODE_CUSTOM = "custom";
  private static final String MODE_DEFAULT = "default";
  private static final String MODE_SINGLE = "single";

  private DownloadManager mDownloadManager;

  private TextView mStatusText;

  private Long mConfigDownloadId;
  private CosuConfig mConfig;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // verify device owner status
    DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    if (!dpm.isDeviceOwnerApp(getPackageName())) {
      Log.e(CosuUtils.TAG, "TestDPC is not the device owner, cannot set up COSU device.");
      finish();
      return;
    }

    // read the admin bundle
    PersistableBundle persistableBundle =
        getIntent().getParcelableExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
    if (persistableBundle == null) {
      Log.e(CosuUtils.TAG, "No admin extra bundle");
      finish();
      return;
    }

    setContentView(R.layout.enable_cosu_activity);
    SetupWizardLayout layout = (SetupWizardLayout) findViewById(R.id.setup_wizard_layout);
    NavigationBar navigationBar = layout.getNavigationBar();
    navigationBar.getNextButton().setVisibility(View.GONE);
    navigationBar.getBackButton().setVisibility(View.GONE);
    layout.showProgressBar();
    mStatusText = (TextView) findViewById(R.id.status_text);

    mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

    // register the download and install receiver
    registerReceiver(mDownloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    registerReceiver(
        mInstallReceiver, new IntentFilter(PackageInstallationUtils.ACTION_INSTALL_COMPLETE));

    // download the config file
    String configDownloadLocation = (String) persistableBundle.get(BUNDLE_KEY_COSU_CONFIG);
    if (configDownloadLocation == null) {
      Log.e(CosuUtils.TAG, "No download-location specified");
      finishWithFailure();
      return;
    }
    if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Downloading config file");
    mConfigDownloadId = CosuUtils.startDownload(mDownloadManager, mHandler, configDownloadLocation);
    mStatusText.setText(getString(R.string.setup_cosu_status_download));
  }

  private void onConfigFileDownloaded() {
    mStatusText.setText(getString(R.string.setup_cosu_status_parse));
    if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Config file downloaded");

    ParcelFileDescriptor pfd;
    try {
      pfd = mDownloadManager.openDownloadedFile(mConfigDownloadId);
    } catch (FileNotFoundException e) {
      Log.e(CosuUtils.TAG, "Download file not found.", e);
      finishWithFailure();
      return;
    }

    InputStream in = new FileInputStream(pfd.getFileDescriptor());
    mConfig = CosuConfig.createConfig(this, in);
    if (mConfig == null) {
      finishWithFailure();
      return;
    }
    Log.d(CosuUtils.TAG, "CosuConfig:");
    Log.d(CosuUtils.TAG, mConfig.toString());

    if (!mConfig.applyPolicies(DeviceAdminReceiver.getComponentName(this))) {
      finishWithFailure();
      return;
    }

    mConfig.initiateDownloadAndInstall(mHandler);
    mStatusText.setText(getString(R.string.setup_cosu_status_apps));
    if (mConfig.areAllInstallsFinished()) {
      startCosuMode();
    }
  }

  /**
   * Start the actual COSU mode and drop the user into different screens depending on the value of
   * mode. default: Launch the home screen with a default launcher custom: Launch KioskModeActivity
   * (custom launcher) with only the kiosk apps present single: Launch the first of the kiosk apps
   */
  private void startCosuMode() {
    Intent launchIntent = null;
    String mode = mConfig.getMode();
    String[] kioskApps = mConfig.getKioskApps();
    if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Starting Cosu mode: " + mode);
    if (mode == null) {
      mode = MODE_DEFAULT;
    }
    if (MODE_CUSTOM.equals(mode)) {
      // Start the KioskModeActivity with all apps in kioskApps
      launchIntent = new Intent(this, KioskModeActivity.class);
      launchIntent.putExtra(KioskModeActivity.LOCKED_APP_PACKAGE_LIST, kioskApps);
      getPackageManager()
          .setComponentEnabledSetting(
              new ComponentName(getPackageName(), KioskModeActivity.class.getName()),
              PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
              PackageManager.DONT_KILL_APP);
    } else if (MODE_SINGLE.equals(mode)) {
      // Start the first app in kioskApps
      if (kioskApps.length != 0) {
        if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "  Launching app " + kioskApps[0]);
        launchIntent = getPackageManager().getLaunchIntentForPackage(kioskApps[0]);
      }
    } else { // MODE_DEFAULT
      // Start the default launcher with a home intent
      launchIntent = new Intent(Intent.ACTION_MAIN);
      launchIntent.addCategory(Intent.CATEGORY_HOME);
    }
    if (launchIntent == null) {
      Log.e(CosuUtils.TAG, "No launch intent specified. Mode=" + mode);
      finishWithFailure();
      return;
    }
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(launchIntent);
    Toast.makeText(this, R.string.setup_cosu_success, Toast.LENGTH_LONG).show();
    unregisterReceiver(mInstallReceiver);
    unregisterReceiver(mDownloadReceiver);

    // check that no timeout messages remain on the handler and remove them
    if (mHandler.hasMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT)) {
      Log.w(CosuUtils.TAG, "Download timeout messages remaining on handler thread.");
      mHandler.removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT);
    }
    finish();
  }

  private void finishWithFailure() {
    Toast.makeText(this, R.string.setup_cosu_failure, Toast.LENGTH_LONG).show();
    unregisterReceiver(mInstallReceiver);
    unregisterReceiver(mDownloadReceiver);
    mHandler.removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT);
    finish();
  }

  private Handler mHandler =
      new Handler() {
        @Override
        public void handleMessage(Message msg) {
          switch (msg.what) {
            case CosuUtils.MSG_DOWNLOAD_COMPLETE:
              {
                if (mConfigDownloadId.equals(msg.obj)) {
                  onConfigFileDownloaded();
                  removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT, mConfigDownloadId);
                } else {
                  Long id = mConfig.onDownloadComplete((Long) msg.obj);
                  if (id != null) {
                    removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT, id);
                  }
                  if (mConfig.areAllInstallsFinished()) {
                    startCosuMode();
                  }
                }
              }
              break;

            case CosuUtils.MSG_DOWNLOAD_TIMEOUT:
              {
                long id = ((Long) msg.obj).longValue();
                if (id == mConfigDownloadId) {
                  Log.e(CosuUtils.TAG, "Time out during download of config file");
                  mDownloadManager.remove(mConfigDownloadId);
                } else {
                  mDownloadManager.remove(id);
                  Log.e(CosuUtils.TAG, "Time out during app download with id: " + id);
                }
                finishWithFailure();
              }
              break;

            case CosuUtils.MSG_INSTALL_COMPLETE:
              {
                mConfig.onInstallComplete((String) msg.obj);
                if (mConfig.areAllInstallsFinished()) {
                  startCosuMode();
                }
              }
              break;
          }
        }
      };

  private BroadcastReceiver mDownloadReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            return;
          }

          final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
          if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Download complete with id: " + id);
          mHandler.sendMessage(
              mHandler.obtainMessage(CosuUtils.MSG_DOWNLOAD_COMPLETE, Long.valueOf(id)));
        }
      };

  private BroadcastReceiver mInstallReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (!PackageInstallationUtils.ACTION_INSTALL_COMPLETE.equals(intent.getAction())) {
            return;
          }

          int result =
              intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
          String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);
          if (CosuUtils.DEBUG)
            Log.d(
                CosuUtils.TAG,
                "PackageInstallerCallback: result=" + result + " packageName=" + packageName);
          switch (result) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
              {
                Intent extraIntent = (Intent) intent.getParcelableExtra(Intent.EXTRA_INTENT);
                // TODO: We should start the intent, after confirming it's the intent we expect
                Log.e(CosuUtils.TAG, "Install requires user action with intent " + extraIntent);
                finishWithFailure();
                return;
              }
            case PackageInstaller.STATUS_SUCCESS:
              {
                mHandler.sendMessage(
                    mHandler.obtainMessage(CosuUtils.MSG_INSTALL_COMPLETE, packageName));
              }
              break;
            default:
              {
                Log.e(CosuUtils.TAG, "Install failed.");
                finishWithFailure();
                return;
              }
          }
        }
      };
}
