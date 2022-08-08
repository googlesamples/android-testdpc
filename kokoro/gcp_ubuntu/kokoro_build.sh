#!/bin/bash

# Fail on any error.
set -e

sudo npm install -g @bazel/bazelisk
/usr/lib/android-sdk/cmdline-tools/bin/sdkmanager --sdk_root=/usr/lib/android-sdk "platforms;android-33"

cd "${KOKORO_ARTIFACTS_DIR}/github/android-testdpc"
./build.sh
