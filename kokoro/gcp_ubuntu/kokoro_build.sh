#!/bin/bash

# Fail on any error.
set -e

sudo npm install -g @bazel/bazelisk

cd "${KOKORO_ARTIFACTS_DIR}/github/android-testdpc"
./build.sh
