Test Device Policy Control (Test DPC) App
=========================================

Test DPC is an app designed to help EMMs, ISVs and OEMs to test their applications and platforms in a Managed Profile effectively. It serves as both a sample Device Policy Client and a testing application to flex the APIs available for Android for Work. It supports devices running Android 5.0 Lollipop or later.

Pre-requisites
--------------

- Android SDK v23
- Android Build Tools v23.0.1
- Android Support Repository

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew assemble” command or use "Import Project" in Android Studio.

Screenshots
-----------

<img src="doc/setup.png" height="400" alt="Setup" title="Setup screen"/>
<img src="doc/policy_management.png" height="400" alt="Policy Management" title="Home screen once the profile is setup" />
<img src="doc/manage_app_restrictions.png" height="400" alt="Manage App Restrictions" title="Manage restrictions for apps in the work profile" />
<img src="doc/work_profile_management.png" height="400" alt="Work Profile Management" title="Manage policies specific to the work profile" />
<img src="doc/network_data_usage_stats.png" height="400" alt="Network Data Usage Stats" title="Analyze data usage for specific work apps or the entire profile" />

Known Issues
------------

1. No support for mime type in cross-profile intent filter creation.
2. No support for PackageInstaller APIs.
3. Values are not saving correctly under Manage app permissions.

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
