package com.afwsamples.testdpc.policy;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.MacAddress;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;
import com.afwsamples.testdpc.common.PackageInstallationUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;
import static com.afwsamples.testdpc.DeviceAdminReceiver.getComponentName;


@TargetApi(Build.VERSION_CODES.P)
public class AdditionalSettings extends BaseSearchablePolicyPreferenceFragment implements
        Preference.OnPreferenceClickListener{

    private static String LOG_TAG = "AdditionalSettingsFragment";
    private static final String GRANT_ALL_PERMISSION_APN_KEY = "grant_all_permission";
    private static final String CHECK_PERMISSION_APN_KEY = "check_permission_state";
    private static final String CREATE_FILE_SDCARD_KEY = "create-file-sdcard";
    private static final String CHANGE_PERMISSION_FILE_SDCARD_KEY = "change-file-permissions";
    private static final String COPY_FILES_FILE_SDCARD_KEY = "copy-files";
    private static final String DELETE_FILES_FILE_SDCARD_KEY = "delete-files";
    private static final String INSTALL_APP_FROM_ASSETS = "install-assets";


    private  Context mContext;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        getActivity().getActionBar().setTitle(R.string.additional_settings_title);
        mContext = getActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.additional_settings_preference);

        findPreference(GRANT_ALL_PERMISSION_APN_KEY).setOnPreferenceClickListener(this);
        findPreference(CHECK_PERMISSION_APN_KEY).setOnPreferenceClickListener(this);
        findPreference(CREATE_FILE_SDCARD_KEY).setOnPreferenceClickListener(this);
        findPreference(CHANGE_PERMISSION_FILE_SDCARD_KEY).setOnPreferenceClickListener(this);
        findPreference(COPY_FILES_FILE_SDCARD_KEY).setOnPreferenceClickListener(this);
        findPreference(DELETE_FILES_FILE_SDCARD_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_APP_FROM_ASSETS).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean isAvailable(Context context) {
        return true;
    }
    private static final int INVALID_NETWORK_ID = -1;

    private static boolean saveAddedWifiConfiguration(WifiManager wifiManager, int networkId) {
        boolean saveConfigurationSuccess = wifiManager.saveConfiguration();
        if (!saveConfigurationSuccess) {
            wifiManager.removeNetwork(networkId);
            return false;
        }
        return true;
    }
    private static int addWifiNetwork(
        WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
        // WifiManager APIs are marked as deprecated but still explicitly supported for DPCs.
        int networkId = wifiManager.addNetwork(wifiConfiguration);
        if (networkId == INVALID_NETWORK_ID) {
            return INVALID_NETWORK_ID;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Saving the configuration is required pre-O.
            if (!saveAddedWifiConfiguration(wifiManager, networkId)) {
                return INVALID_NETWORK_ID;
            }
        }

        return networkId;
    }

    private static int updateWifiNetwork(WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
        // WifiManager APIs are marked as deprecated but still explicitly supported for DPCs.
        int networkId = wifiManager.updateNetwork(wifiConfiguration);
        if (networkId == INVALID_NETWORK_ID) {
            return INVALID_NETWORK_ID;
        }
        return networkId;
    }

    private static void setRandomization(Context context, WifiConfiguration wifiConfiguration){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        MacAddress nweMacAddress = MacAddress.fromString("02:00:00:00:00:00");

        MacAddress macAddress = null;
        try {
            @SuppressLint("SoonBlockedPrivateApi") Method setRandomizedMacAddress_ = wifiConfiguration.getClass().getDeclaredMethod("setRandomizedMacAddress", MacAddress.class);
            setRandomizedMacAddress_.invoke(wifiConfiguration, nweMacAddress);
        } catch (NoSuchMethodException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

private static boolean isNetworkConfigured( Context context, WifiConfiguration wifiConfiguration){
    WifiConfiguration wifiConfigUpdate = null;
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    @SuppressLint("MissingPermission") List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

    for (WifiConfiguration config : list) {
           if (config.SSID != null && config.SSID.equals(wifiConfiguration.SSID))  {
                    //wifiConfigUpdate = config;
               wifiConfiguration.networkId=config.networkId;
                    Log.e("isNetworkConfigured", "> > > >got id " + config.networkId);
                    return true;
                }
    }
    return false;

}

    public static boolean saveWifiConfiguration(
            Context context, WifiConfiguration wifiConfiguration) {
        Log.e("saveWifiConfiguration", "> > > > wifiConfiguration: " + wifiConfiguration);

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int networkId=INVALID_NETWORK_ID;
        //if (wifiConfiguration.networkId == INVALID_NETWORK_ID)
        if (!isNetworkConfigured(context,wifiConfiguration))
        {
            networkId = addWifiNetwork(wifiManager, wifiConfiguration);
            wifiManager.enableNetwork(networkId,  false);
            Log.e("AddNetwork", "> > > > networkId1: " + networkId);

            WifiConfiguration wifiConfigUpdate = null;
            @SuppressLint("MissingPermission") List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration config : list) {
                if (config.networkId == networkId) {
                    wifiConfigUpdate = config;
                    break;
                }
            }

            if (null != wifiConfigUpdate) {
                wifiConfigUpdate.networkId = networkId;
                ProxyInfo pr = ProxyInfo.buildDirectProxy("192.168.0.1", 8888);
                wifiConfigUpdate.setHttpProxy(pr);
                networkId = updateWifiNetwork(wifiManager, wifiConfigUpdate);
                Log.e("Updating Proxy", "> > > > networkId2: " + networkId);
            }
        } else {
            Log.e("UpdateNetwork", "> > > > networkId1: " + wifiConfiguration.networkId);
            WifiConfiguration wifiConfigUpdate = null;
            @SuppressLint("MissingPermission") List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration config : list) {
                if (config.networkId == wifiConfiguration.networkId) {
                    wifiConfigUpdate = config;
                    break;
                }
            }

            if (null != wifiConfigUpdate) {
                wifiConfigUpdate.networkId = networkId;
                ProxyInfo pr = ProxyInfo.buildDirectProxy("192.168.0.1", 8888);
                wifiConfigUpdate.setHttpProxy(pr);
                networkId = updateWifiNetwork(wifiManager, wifiConfigUpdate);
                Log.e("updating proxy", "> > > > networkId2: " + networkId);
            }
        }
        if (networkId == INVALID_NETWORK_ID) {
            return false;
        }
        wifiManager.enableNetwork(networkId, /* disableOthers= */ false);
        return true;
    }
    private String getQuotedString(String string) {
        return "\"" + string + "\"";
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case GRANT_ALL_PERMISSION_APN_KEY:
               // if (Util.SDK_INT >= Build.VERSION_CODES.M) {
                 //   autoGrantRequestedPermissionsToSelf();
                //}
                WifiConfiguration config = new WifiConfiguration();
                config.SSID = getQuotedString("B803_Airtel_Zerotouch_5G");
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.preSharedKey = getQuotedString("9341381313");
                saveWifiConfiguration(mContext,config);

                return true;
            case CHECK_PERMISSION_APN_KEY:
                /*if (Util.SDK_INT >= Build.VERSION_CODES.M) {
                    dumpPermissions();
                }*/
                /*
                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                MacAddress nweMacAddress = MacAddress.fromString("03:ff:ff:ff:ff:ff");
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                MacAddress macAddress = null;
                try {
                        Method setRandomizedMacAddress_ = wifiConfiguration.getClass().getMethod("setRandomizedMacAddress", MacAddress.class);
                       setRandomizedMacAddress_.invoke(wifiConfiguration, nweMacAddress);
                    } catch (NoSuchMethodException e) {
                        Log.e(LOG_TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        Log.e(LOG_TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        Log.e(LOG_TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                */

                return true;
            case CREATE_FILE_SDCARD_KEY:
                fileOperations();
                return true;
            case CHANGE_PERMISSION_FILE_SDCARD_KEY:
                String command = "chmod -R 777 " + "/sdcard/NitinTestDir";
                try {
                    Runtime.getRuntime().exec(command, null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            command = "chmod 777 " + "/sdcard/NitinTestDir/myhello.txt";
            try {
                Runtime.getRuntime().exec(command, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
                return true;
            case COPY_FILES_FILE_SDCARD_KEY:
                try {
                    copyDirectory(new File("/sdcard/NitinTestDir") , new File("/enterprise/usr/NitinTestDir"));
                    copyDirectory(new File("/enterprise/usr/NitinTestDir") , new File("/sdcard/NitinTestDir1"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case DELETE_FILES_FILE_SDCARD_KEY:
                deleteRecursive(new File("/sdcard/NitinTestDir"));
                deleteRecursive(new File("/sdcard/NitinTestDir1"));
                deleteRecursive(new File("/sdcard/Download/NitinTestDirDL"));

                deleteRecursive(new File("/sdcard/ViaJavaIOStreamSDcardRoot.txt"));
                deleteRecursive(new File("/sdcard/ViaJavaCreateFileSDcardRoot.txt"));
                deleteRecursive(new File("/sdcard/storage-emulated.txt"));
                deleteRecursive(new File("/sdcard/mntSdcard.txt"));

                return true;
            case INSTALL_APP_FROM_ASSETS:
                try {
                    InputStream inputStream = getActivity().getAssets().open("app-release.apk");
                    PackageInstallationUtils.installPackage(mContext, inputStream, "com.symbol.filesharingapp");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
        }
        return false;
    }

    void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();

    }

    public void copyDirectory(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }


    private void fileOperations()
    {
        createAndUpdateFileViaFileWriter(mContext, "ViaJavaIOStreamSDcardRoot.txt","Hello enterprise_usr", "/sdcard");
        createAndUpdateFileViaFileApi(mContext, "ViaJavaCreateFileSDcardRoot.txt","Hello enterprise_usr", "/sdcard");
        createAndUpdateFileViaFileApi(mContext, "storage-emulated.txt","Hello enterprise_usr", "/storage/emulated/0");
        createAndUpdateFileViaFileApi(mContext, "mntSdcard.txt","Hello enterprise_usr", "/mnt/sdcard");

        //createAndUpdateFileCreateException(mContext, "Createatroot.txt","Hello enterprise_usr", "/sdcard");

        createDirectory(mContext, "NitinTestDir", "/sdcard");
        createAndUpdateFileViaFileApi(mContext, "myhello.txt","Hello enterprise_usr", "/sdcard/NitinTestDir");
        createAndUpdateFileViaFileApi(mContext, "myhello1.txt","Hello enterprise_usr1", "/sdcard/NitinTestDir");

        createDirectory(mContext, "NitinTestDirDL", "/sdcard/Download");
        createAndUpdateFileViaFileApi(mContext, "myhello.txt","Hello enterprise_usr", "/sdcard/Download/NitinTestDirDL");
        createAndUpdateFileViaFileApi(mContext, "myhello1.txt","Hello enterprise_usr1", "/sdcard/Download/NitinTestDirDL");

        createDirectory(mContext, "NitinTestDir", "/enterprise/usr");
        createAndUpdateFileViaFileApi(mContext, "myhello.txt","Hello enterprise_usr", "/enterprise/usr/NitinTestDir");
    }

    public void createDirectory(Context context, String sFileName, String path){

        File gpxfile = new File(path, sFileName);
        gpxfile.mkdirs();
        //Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();

    }

    public void createAndUpdateFileCreateException(Context context, String sFileName, String sBody, String path) {
        try {
            boolean result;
            File gpxfile = new File(sFileName);
            result = gpxfile.createNewFile();
            BufferedWriter buf = new BufferedWriter(new FileWriter(gpxfile, true));
            buf.append(sBody);
            buf.newLine();
            buf.close();
            // Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createAndUpdateFileViaFileApi(Context context, String sFileName, String sBody, String path) {
        try {
            boolean result;
            File gpxfile = new File(path, sFileName);
            result = gpxfile.createNewFile();
            BufferedWriter buf = new BufferedWriter(new FileWriter(gpxfile, true));
            buf.append(sBody);
            buf.newLine();
            buf.close();
            // Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createAndUpdateFileViaFileWriter(Context context, String sFileName, String sBody, String path) {
        try {
            File gpxfile = new File(path, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            gpxfile.setWritable(true,false);
            gpxfile.setReadable(true,false);
           // Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showToast(int msgId, Object... args) {
        showToast(getString(msgId, args), Toast.LENGTH_SHORT);
    }

    private void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    private void showToast(String msg, int duration) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Toast.makeText(activity, msg, duration).show();
    }


    private void dumpPermissions() {
        String packageName = mContext.getPackageName();
        ComponentName adminComponentName = getComponentName(mContext);

        List<String> permissions = getRuntimePermissions(mContext.getPackageManager(), packageName);
        for (String permission : permissions) {
            int success = mDevicePolicyManager.getPermissionGrantState(adminComponentName,
                    packageName, permission);

            Log.d(LOG_TAG, "Permission state  " + permission + ", success: " + success);

            }
        }


    @TargetApi(Build.VERSION_CODES.M)
    private void autoGrantRequestedPermissionsToSelf() {
        String packageName = mContext.getPackageName();
        ComponentName adminComponentName = getComponentName(mContext);

        List<String> permissions = getRuntimePermissions(mContext.getPackageManager(), packageName);
        for (String permission : permissions) {
            boolean success = mDevicePolicyManager.setPermissionGrantState(adminComponentName,
                    packageName, permission, PERMISSION_GRANT_STATE_GRANTED);
            Log.d(LOG_TAG, "Auto-granting " + permission + ", success: " + success);
            if (!success) {
                Log.e(LOG_TAG, "Failed to auto grant permission to self: " + permission);
            }
        }
    }

    private List<String> getRuntimePermissions(PackageManager packageManager, String packageName) {
        List<String> permissions = new ArrayList<>();
        PackageInfo packageInfo;
        try {
            packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Could not retrieve info about the package: " + packageName, e);
            return permissions;
        }

        if (packageInfo != null && packageInfo.requestedPermissions != null) {
            for (String requestedPerm : packageInfo.requestedPermissions) {
                if (isRuntimePermission(packageManager, requestedPerm)) {
                    permissions.add(requestedPerm);
                }
            }
        }
        return permissions;
    }

    private boolean isRuntimePermission(PackageManager packageManager, String permission) {
        try {
            PermissionInfo pInfo = packageManager.getPermissionInfo(permission, 0);
            if (pInfo != null) {
                if ((pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
                        == PermissionInfo.PROTECTION_DANGEROUS) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(LOG_TAG, "Could not retrieve info about the permission: " + permission);
        }
        return false;
    }
}