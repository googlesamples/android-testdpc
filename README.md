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

#### AFW# code provisioning (Device Owner M+)
1. Factory reset your device and tap the welcome screen in setup wizard 6 times.
2. When prompted to sign in, enter **afw#testdpc**
3. Follow onscreen instructions

#### QR code provisioning (Device Owner N+ only) ####
1. Factory reset your device and tap the welcome screen in setup wizard 6 times.
2. On Android O or older, the setup wizard prompts the user to connect to the Internet so the setup wizard can download a QR code reader.
3. Generate a QR code with the content:
   ```
    {
    	"android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.afwsamples.testdpc/com.afwsamples.testdpc.DeviceAdminReceiver",
    	"android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM": "gJD2YwtOiWJHkSMkkIfLRlj-quNqG1fb6v100QmzM9w=",
    	"android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "https://testdpc-latest-apk.appspot.com"
    }
   ```
6. Scan the QR code and follow onscreen instructions

#### ADB command ####

**Device Owner**

*   Run the `adb` command:

    ```console
    adb shell dpm set-device-owner com.afwsamples.testdpc/.DeviceAdminReceiver
    ```

**Profile Owner**

*   Create a managed profile by launching the “Set up TestDPC” app (if this app
    seems broken and you are in dark mode, switch to light mode)
*   Skip adding an account at the end of the flow

**COPE Profile Owner**

*   Create a managed profile by launching the “Set up TestDPC” app (if this app
    seems broken and you are in dark mode, switch to light mode)
*   Skip adding an account at the end of the flow
*   Run the `adb` command:

    ```console
    adb shell dpm mark-profile-owner-on-organization-owned-device --user 10 com.afwsamples.testdpc/.DeviceAdminReceiver`
    ```

## TestDPC as DM role holder

TestDPC v9.0.5+ can be setup as Device Management Role Holder.

*   Running the following `adb` commands:

    ```console
    adb shell cmd role set-bypassing-role-qualification true
    adb shell cmd role add-role-holder android.app.role.DEVICE_POLICY_MANAGEMENT com.afwsamples.testdpc
    ```

    Note: unlike DO/PO, this change is not persisted so TestDPC needs to be
    marked as role holder again if the device reboots.

Android Studio import
---------------------

To import this repository in Android Studio, you need to use the 
[Bazel for Android Studio](https://plugins.jetbrains.com/plugin/9185-bazel-for-android-studio)
Plugin.

When importing the project you have to select the folder containing the Bazel's
`BUILD` file. When prompted to select a "project view", you can choose the
option "Copy external" and choose the `scripts/ij.bazelproject` available in
this repository.

Once Bazel has complete the import operation and the first sync of the
project, you can create a "Run Configuration".
Select "Bazel Command" as Configuration type and add `//:testdpc` as
"target expression".

You can now run the project from inside Android Studio.


Building with Bazel
-------------------

The repository includes a `build.sh` script to build the application. The required
[setupdesign library](https://android.googlesource.com/platform/external/setupdesign/+/refs/heads/main)
is now imported and patched dynamically using the command line utility `ed`. This needs to be
available on the path to successfully build the project.

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
