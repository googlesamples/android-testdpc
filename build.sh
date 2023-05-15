#!/bin/bash

bazel clean --expunge
bazel build --java_runtime_version=remotejdk_11 //:testdpc

## apk file created in path  bazel-bin
