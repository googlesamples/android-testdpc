package com.afwsamples.testdpc;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;

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
}
