load("@rules_jvm_external//:defs.bzl", "artifact")

exports_files([
    "LICENSE",
    ".blazeproject",
    "WORKSPACE",
])

# Java binary (temporary - avoiding rules_android compatibility issues)
java_binary(
    name = "testdpc",
    srcs = [],
    main_class = "com.afwsamples.testdpc.PolicyManagementActivity",
    deps = [
        "//src/main:src",
        # "//src/main:resources",  # Not ready yet - requires Android resources
        # "//src/main:aidl",  # Not ready yet - requires AIDL tool
    ],
    javacopts = ["--release=11"],
)

# Note: This is a temporary build that doesn't actually build an APK
# It's just a demonstration that the basic Java sources can be built
# Full Android APK building requires rules_android which has compatibility
# issues with Bazel 7.4.1

# Test library (placeholder - actual tests would go here)
java_library(
    name = "demo_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:androidx_annotation_annotation",
    ],
    javacopts = ["--release=11"],
)

java_test(
    name = "demo_test",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/BooleanParserTest.java"],
    deps = [
        "@maven//:junit_junit",
        "//src/main:src",
    ],
    javacopts = ["--release=11"],
)
