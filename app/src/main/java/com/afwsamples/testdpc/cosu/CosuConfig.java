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

import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the specific cosu set up we want to achieve. The set up is read from an
 * XML config file. Additional apps are downloaded and the specified policies are applied.
 */
/* package */ class CosuConfig {
    private static final String TAG_APP = "app";
    private static final String TAG_COSU_CONFIG = "cosu-config";
    private static final String TAG_DOWNLOAD_APPS = "download-apps";
    private static final String TAG_ENABLE_APPS = "enable-apps";
    private static final String TAG_HIDE_APPS = "hide-apps";
    private static final String TAG_KIOSK_APPS = "kiosk-apps";
    private static final String TAG_POLICIES = "policies";
    private static final String TAG_USER_RESTRICTION = "user-restriction";
    private static final String TAG_GLOBAL_SETTING = "global-setting";
    private static final String TAG_DISABLE_STATUS_BAR = "disable-status-bar";
    private static final String TAG_DISABLE_KEYGUARD = "disable-keyguard";
    private static final String TAG_DISABLE_CAMERA = "disable-camera";
    private static final String TAG_DISABLE_SCREEN_CAPTURE = "disable-screen-capture";

    private static final String ATTRIBUTE_DOWNLOAD_LOCATION = "download-location";
    private static final String ATTRIBUTE_MODE = "mode";
    private static final String ATTRIBUTE_PACKAGE_NAME = "package-name";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_NAME = "name";

    private static final String NEW_LINE = System.getProperty("line.separator");

    private Context mContext;
    private DownloadManager mDownloadManager;

    private Set<String> mHideApps = new HashSet<>();
    private Set<String> mEnableSystemApps = new HashSet<>();
    private Set<String> mKioskApps = new HashSet<>();
    private Set<DownloadAppInfo> mDownloadApps = new HashSet<>();
    private String mMode;
    private Set<String> mUserRestrictions = new HashSet<>();
    private Set<GlobalSetting> mGlobalSettings = new HashSet<>();
    private boolean mDisableStatusBar = false;
    private boolean mDisableKeyguard = false;
    private boolean mDisableScreenCapture = false;
    private boolean mDisableCamera = false;

    /**
     * Parses the config xml file given in the form of an InputStream.
     */
    private CosuConfig(Context context, InputStream in) throws XmlPullParserException, IOException {
        mContext = context;
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String name = parser.getName();
                if (TAG_COSU_CONFIG.equals(name)) {
                    mMode = parser.getAttributeValue(null, ATTRIBUTE_MODE);
                } else if (TAG_POLICIES.equals(name)) {
                    readPolicies(parser);
                } else if (TAG_ENABLE_APPS.equals(name)) {
                    readApps(parser, mEnableSystemApps);
                } else if (TAG_HIDE_APPS.equals(name)) {
                    readApps(parser, mHideApps);
                } else if (TAG_KIOSK_APPS.equals(name)) {
                    readApps(parser, mKioskApps);
                } else if (TAG_DOWNLOAD_APPS.equals(name)) {
                    readDownloadApps(parser, mDownloadApps);
                }
            }
        } finally {
            in.close();
        }
    }

    public static CosuConfig createConfig(Context context, InputStream in) {
        try {
            return new CosuConfig(context, in);
        } catch (XmlPullParserException | IOException e) {
            Log.e(CosuUtils.TAG, "Exception during config creation.", e);
            return null;
        }
    }

    public void applyPolicies(ComponentName admin) {
        DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        // set the lock task packages
        dpm.setLockTaskPackages(admin, getKioskApps());

        // hide apps
        for (String pkg : mHideApps) {
            dpm.setApplicationHidden(admin, pkg, true);
        }

        // enable system apps
        for (String pkg : mEnableSystemApps) {
            try {
                dpm.enableSystemApp(admin, pkg);
            } catch (IllegalArgumentException e) {
                Log.w(CosuUtils.TAG, "Failed to enable " + pkg
                        + ". Operation is only allowed for system apps.");
            }
        }

        // set user restrictions
        for (String userRestriction : mUserRestrictions) {
            dpm.addUserRestriction(admin, userRestriction);
        }

        for (GlobalSetting globalSetting : mGlobalSettings) {
            dpm.setGlobalSetting(admin, globalSetting.key, globalSetting.value);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dpm.setStatusBarDisabled(admin, mDisableStatusBar);
            dpm.setKeyguardDisabled(admin, mDisableKeyguard);
        }
        dpm.setScreenCaptureDisabled(admin, mDisableScreenCapture);
        dpm.setCameraDisabled(admin, mDisableCamera);
    }

    public void initiateDownloadAndInstall(Handler handler) {
        for (DownloadAppInfo ai : mDownloadApps) {
            ai.downloadId = CosuUtils.startDownload(mDownloadManager, handler, ai.downloadLocation);
        }
    }

    public String getMode() {
        return mMode;
    }

    public String[] getKioskApps() {
        return mKioskApps.toArray(new String[mKioskApps.size()]);
    }

    public boolean areAllInstallsFinished() {
        for (DownloadAppInfo ai : mDownloadApps) {
            if (!ai.installCompleted) {
                return false;
            }
        }
        return true;
    }

    public Long onDownloadComplete(Long id) {
        for (DownloadAppInfo ai : mDownloadApps) {
            if (id.equals(ai.downloadId)) {
                if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Package download complete: "
                        + ai.packageName);
                ai.downloadCompleted = true;
                try {
                    ParcelFileDescriptor pfd = mDownloadManager.openDownloadedFile(id);
                    InputStream in = new FileInputStream(pfd.getFileDescriptor());
                    CosuUtils.installPackage(mContext, in, ai.packageName);
                } catch (IOException e) {
                    Log.e(CosuUtils.TAG, "Error installing package: " + ai.packageName, e);
                    // We are still marking the package as "installed", just so we don't block the
                    // entire flow.
                    ai.installCompleted = true;
                }
                return ai.downloadId;
            }
        }
        Log.w(CosuUtils.TAG, "Unknown download id: " + id);
        return null;
    }

    public void onInstallComplete(String packageName) {
        if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Package install complete: " + packageName);
        for (DownloadAppInfo ai : mDownloadApps) {
            if (packageName.equals(ai.packageName)) {
                ai.installCompleted = true;
                return;
            }
        }
    }

    /**
     * Read a number of apps from the xml parser
     */
    private void readApps(XmlPullParser parser, Set<String> apps)
            throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (TAG_APP.equals(name)) {
                String packageName = parser.getAttributeValue(null, ATTRIBUTE_PACKAGE_NAME);
                if (packageName != null) {
                    apps.add(packageName);
                }
                skipCurrentTag(parser);
            }
        }
    }

    /**
     * Read a number of apps with download information from the xml parser
     */
    private void readDownloadApps(XmlPullParser parser, Set<DownloadAppInfo> apps)
            throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (TAG_APP.equals(name)) {
                String packageName = parser.getAttributeValue(null, ATTRIBUTE_PACKAGE_NAME);
                String downloadLocation = parser.getAttributeValue(null,
                        ATTRIBUTE_DOWNLOAD_LOCATION);
                if (packageName != null && downloadLocation != null) {
                    apps.add(new DownloadAppInfo(packageName, downloadLocation));
                }
                skipCurrentTag(parser);
            }
        }
    }

    /**
     * Read the policies to be set
     */
    private void readPolicies(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (TAG_USER_RESTRICTION.equals(name)) {
                String userRestriction = parser.getAttributeValue(null, ATTRIBUTE_NAME);
                if (userRestriction != null) {
                    mUserRestrictions.add(userRestriction);
                }
            } else if (TAG_GLOBAL_SETTING.equals(name)) {
                String setting = parser.getAttributeValue(null, ATTRIBUTE_NAME);
                String value = parser.getAttributeValue(null, ATTRIBUTE_VALUE);
                if (setting != null && value != null) {
                    mGlobalSettings.add(new GlobalSetting(setting, value));
                }
            } else if (TAG_DISABLE_STATUS_BAR.equals(name)) {
                mDisableStatusBar = Boolean.parseBoolean(parser.getAttributeValue(null,
                        ATTRIBUTE_VALUE));
            } else if (TAG_DISABLE_KEYGUARD.equals(name)) {
                mDisableKeyguard = Boolean.parseBoolean(parser.getAttributeValue(null,
                        ATTRIBUTE_VALUE));
            } else if (TAG_DISABLE_CAMERA.equals(name)) {
                mDisableCamera = Boolean.parseBoolean(parser.getAttributeValue(null,
                        ATTRIBUTE_VALUE));
            } else if (TAG_DISABLE_SCREEN_CAPTURE.equals(name)) {
                mDisableScreenCapture = Boolean.parseBoolean(parser.getAttributeValue(null,
                        ATTRIBUTE_VALUE));
            }
            skipCurrentTag(parser);
        }
    }

    /**
     * Continue to the end of the current xml tag
     */
    private void skipCurrentTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG
                || parser.getDepth() > outerDepth)) {
        }
    }

    private class DownloadAppInfo {
        public final String packageName;
        public final String downloadLocation;
        public Long downloadId;
        public boolean downloadCompleted;
        public boolean installCompleted;

        public DownloadAppInfo(String packageName, String downloadLocation) {
            this.packageName = packageName;
            this.downloadLocation = downloadLocation;
            this.downloadCompleted = false;
            this.installCompleted = false;
        }

        @Override
        public String toString() {
            return "packageName: " + packageName
                    + " downloadLocation: " + downloadLocation;
        }
    }

    private class GlobalSetting {
        public final String key;
        public final String value;

        public GlobalSetting(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "setting: " + key + " value: " + value;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Mode: ").append(mMode).append(NEW_LINE);

        builder.append("Disable status bar: ").append(mDisableStatusBar).append(NEW_LINE);

        builder.append("Disable keyguard: ").append(mDisableKeyguard).append(NEW_LINE);

        builder.append("Disable screen capture: ").append(mDisableScreenCapture).append(NEW_LINE);

        builder.append("Disable camera: ").append(mDisableCamera).append(NEW_LINE);

        builder.append("User restrictions:").append(NEW_LINE);
        dumpSet(builder, mUserRestrictions);

        builder.append("Global settings:").append(NEW_LINE);
        dumpSet(builder, mGlobalSettings);

        builder.append("Hide apps:").append(NEW_LINE);
        dumpSet(builder, mHideApps);

        builder.append("Enable system apps:").append(NEW_LINE);
        dumpSet(builder, mEnableSystemApps);

        builder.append("Kiosk apps:").append(NEW_LINE);
        dumpSet(builder, mKioskApps);

        builder.append("Download apps:").append(NEW_LINE);
        dumpSet(builder, mDownloadApps);

        return builder.toString();
    }

    private void dumpSet(StringBuilder builder, Set<?> set) {
        for (Object obj : set) {
            builder.append("  ").append(obj.toString()).append(NEW_LINE);
        }
    }
}
