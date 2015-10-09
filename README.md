Test Device Policy Control (Test DPC) App
=========================================

Test DPC is an app designed to help EMMs, ISVs and OEMs to test their applications and platforms in a Managed Profile effectively. It serves as both a sample Device Policy Client and a testing application to flex the APIs available for Android for Work.

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

![Setup](doc/setup.png "Setup screen")
![Policy Management](doc/policy_management.png "Home screen once the profile is setup")
![Manage App Restrictions](doc/manage_app_restrictions.png "Manage restrictions for apps in the work profile")
![Work Profile Management](doc/work_profile_management.png "Manage policies specific to the work profile")
![Network Data Usage Stats](doc/network_data_usage_stats.png "Analyze data usage for specific work apps or the entire profile")

Known Issues
------------

1. No support for mime type in cross-profile intent filter creation.
2. No support for PackageInstaller APIs.
3. Values are not saving correctly under Manage app permissions.

Support
-------

- Google+ Community: https://plus.google.com/communities/105153134372062985968
- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/googlesamples/android-testdpc

License
-------

Licensed under the Apache 2.0 license. See the LICENSE file for details.

How to make contributions?
--------------------------

Please read and follow the steps in the CONTRIB file.
