Test Device Policy Control (Test DPC) App
=========================================

Test DPC is an app designed to help EMMs, ISVs, and OEMs to test their applications and platforms in a Android enterprise managed profile (i.e. work profile). It serves as both a sample Device Policy Controller and a testing application to flex the APIs available for Android enterprise. It supports devices running Android 5.0 Lollipop or later.

See the [documentation](https://developer.android.com/work/index.html) to learn more about Android in the enterprise.

Getting Started
---------------

This sample uses the Bazel build system. To build this project, use the "bazel build testdpc" command.

This app can also be found [on the Play store](https://play.google.com/store/apps/details?id=com.afwsamples.testdpc).

Provisioning
------------

You can find various kinds of provisioning methods [here](https://developers.google.com/android/work/prov-devices#Key_provisioning_differences_across_android_releases). Let's take a few of them as an example.

#### QR code provisioing (Device Owner N+ only) ####
1. Factory reset your device and tap the welcome screen in setup wizard 6 times.
2. The setup wizard prompts the user to connect to the Internet so the setup wizard can download a QR code reader.
3. Modify (if needed) and scan [this QR code](http://down-box.appspot.com/qr/nQB0tw7b).
4. Follow onscreen instructions

#### adb command (Device Owner) ####
adb shell dpm set-device-owner com.afwsamples.testdpc/.DeviceAdminReceiver

#### Work profile ####
The easiest way is to launch the "Set Up TestDPC" app in launcher and follow the onscreen instructions.

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
