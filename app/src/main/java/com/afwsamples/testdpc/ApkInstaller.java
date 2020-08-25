package com.afwsamples.testdpc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.afwsamples.testdpc.common.PackageInstallationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ApkInstaller extends BroadcastReceiver {

    private static final String TAG = "ApkInstaller";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("Nitin", "Recieved Intent");

        if (intent.getAction().equals("com.afwsamples.testdpc.intent.action.INSTALL_APK")) {
            String path = intent.getStringExtra("path");
            String packageName = intent.getStringExtra("package");
            Log.i("Nitin", "Recieved Intent path " +path);
            Log.i("Nitin", "Recieved Intent packageName " +packageName);

            try {
                //InputStream inputStream = context.openFileInput(new File(path));
                FileInputStream fIS = new FileInputStream (new File(path));
                PackageInstallationUtils.installPackage(context, fIS, packageName);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
