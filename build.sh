#!/bin/bash

# Fail on any error.
set -e

if [[ $1 = "clean" ]]; then
  bazel clean --expunge
fi
#bazel build --noincremental_dexing testdpc
bazel build testdpc
bazel mobile-install //:testdpc --verbose_failures
