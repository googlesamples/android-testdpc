Test Device Policy Control (Test DPC) App
=========================================

Test DPC is an app designed to help EMMs, ISVs, and OEMs to test their applications and platforms in a Android for Work managed profile (i.e. work profile). It serves as both a sample Device Policy Controller and a testing application to flex the APIs available for Android for Work. It supports devices running Android 5.0 Lollipop or later. As of Mar 24, 2017, it also includes support for devices running API level 26.

See the [documentation](https://developer.android.com/work/index.html) to learn more about Android for Work.

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the "gradlew assemble" command or use "Import Project" in Android Studio.

Provisioning
------------

You can find various kinds of provisioning methods [here](https://developers.google.com/android/work/prov-devices#Key_provisioning_differences_across_android_releases). Let's take a few of them as an example.

#### QR code provisioing (Device Owner N+ only) ####
1. Factory reset your device and tap the welcome screen in setup wizard 6 times.
2. The setup wizard prompts the user to connect to the Internet so the setup wizard can download a QR code reader.
3. Modify (if needed) and scan [this QR code] (http://down-box.appspot.com/qr/nQB0tw7b).
4. Follow onscreen instructions

#### NFC provisioning (Device Owner) ####
The [NFC Provisioning app](https://github.com/googlesamples/android-NfcProvisioning) is used for *device owner* provisioning.
1. Push the nfcprovisioning.txt file to your device:
`adb push nfcprovisioning.txt /sdcard/`
2. Open the NFC Provisioning app and ensure that com.afwsamples.testdpc is auto-populated.
3. Bump the devices and touch to beam.
4. Follow onscreen instructions on the target device.

#### adb command (Device Owner) ####
adb shell dpm set-device-owner com.afwsamples.testdpc/.DeviceAdminReceiver

#### Work profile ####
The easiest way is to launch the Test DPC app in launcher and follow the onscreen instructions.

Screenshots
------------

<img src="doc/setup.png" height="400" alt="Setup" title="Setup screen"/>
<img src="doc/policy_management.png" height="400" alt="Policy Management" title="Home screen once the profile is setup" />
<img src="doc/manage_app_restrictions.png" height="400" alt="Manage App Restrictions" title="Manage restrictions for apps in the work profile" />
<img src="doc/work_profile_management.png" height="400" alt="Work Profile Management" title="Manage policies specific to the work profile" />
<img src="doc/network_data_usage_stats.png" height="400" alt="Network Data Usage Stats" title="Analyze data usage for specific work apps or the entire profile" />

Support
-------

If you've found an error in this sample, please file an issue:
https://github.com/googlesamples/android-testdpc/issues

Patches are encouraged, and may be submitted by forking this project and submitting a pull request through GitHub.

License
-------

Licensed under the Apache 2.0 license. See the LICENSE file for details.

How to make contributions?
--------------------------

Please read and follow the steps in the CONTRIB file.
