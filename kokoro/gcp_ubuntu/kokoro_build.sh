i#!/bin/bash

# Fail on any error.
set -e

sudo npm install -g @bazel/bazelisk
yes | $ANDROID_HOME/tools/bin/sdkmanager "platforms;android-33" "build-tools;30.0.3"

cd "${KOKORO_ARTIFACTS_DIR}/github/android-testdpc"
./build.sh
