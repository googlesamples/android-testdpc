#!/bin/bash

# Fail on any error.
set -e

sudo npm install -g @bazel/bazelisk
$ANDROID_HOME/tools/bin/sdkmanager "platforms;android-33"

cd "${KOKORO_ARTIFACTS_DIR}/github/android-testdpc"
./build.sh
