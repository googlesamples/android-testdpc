Test Device Policy Control (Test DPC) App
=========================================

Test DPC is an app designed to help EMMs, ISVs and OEMs to test their applications and platforms in a Managed Profile effectively.

Pre-requisites
--------------

- Android SDK v23

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew assemble” command or use "Import Project" in Android Studio.

Screenshots
-----------

![Setup](doc/setup.png “Setup screen“)
![Policy Management](doc/policy_management.png “Home screen once the profile is setup“)
![Manage App Restrictions](doc/manage_app_restrictions.png “Manage restrictions for apps in the Work Profile“)
![Work Profile Management](doc/work_profile_management.png “Manage policies specific to the Work Profile“)

Known Issues
------------

1. This app will only work with devices running Android M. We will be adding support for L very shortly.
2. Disable Camera policy does not work.
3. No support for mime type in cross-profile intent filter creation.
4. No support for PackageInstaller APIs.

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

