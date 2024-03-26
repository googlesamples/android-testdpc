load("@rules_jvm_external//:defs.bzl", "artifact")

exports_files([
    "LICENSE",
    ".blazeproject",
    "WORKSPACE",
])

MANIFEST = "src/main/AndroidManifest.xml"

MANIFEST_DEBUG = "src/main/AndroidManifestDebug.xml"

PACKAGE = "com.afwsamples.testdpc"

android_library(
    name = "androidx_deps",
    exports = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_collection_collection",
        "@maven//:androidx_core_core",
        "@maven//:androidx_enterprise_enterprise_feedback",
        "@maven//:androidx_legacy_legacy_support_v13",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:androidx_lifecycle_lifecycle_process",
        "@maven//:androidx_lifecycle_lifecycle_runtime",
        "@maven//:androidx_localbroadcastmanager_localbroadcastmanager",
        "@maven//:androidx_preference_preference",
        "@maven//:androidx_recyclerview_recyclerview",
    ],
)

android_library(
    name = "bouncycastle_deps",
    exports = [
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)

android_library(
    name = "guava_deps",
    exports = [
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "test_deps",
    exports = [
        artifact("com.google.testparameterinjector:test-parameter-injector"),
        artifact("com.google.truth:truth"),
        artifact("androidx.test:core"),
        artifact("androidx.test:monitor"),
        artifact("androidx.test:runner"),
        artifact("junit:junit"),
        artifact("org.hamcrest:java-hamcrest"),
        artifact("org.robolectric:robolectric"),
        artifact("org.robolectric:robolectric-annotations"),
        artifact("org.robolectric:shadows-core"),
        "@robolectric//bazel:android-all",
    ],
)

android_binary(
    name = "testdpc",
    custom_package = PACKAGE,
    dexopts = [
        "--force-jumbo",
    ],
    manifest = MANIFEST,
    multidex = "native",
    deps = [
        ":testdpc_lib",
    ],
)

android_binary(
    name = "testdpc_debug",
    custom_package = PACKAGE,
    dexopts = [
        "--force-jumbo",
    ],
    manifest = MANIFEST_DEBUG,
    multidex = "native",
    deps = [
        ":testdpc_lib",
    ],
)

android_library(
    name = "testdpc_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    custom_package = PACKAGE,
    javacopts = ["-Xep:AndroidJdkLibsChecker:OFF"],
    manifest = MANIFEST,
    resource_files = glob(["src/main/res/**"]),
    deps = [
        ":aidl",
        ":androidx_deps",
        ":bouncycastle_deps",
        ":guava_deps",
        "@setupdesign//:setupdesign",
        "@setupcompat//:setupcompat",
    ],
)

android_library(
    name = "aidl",
    custom_package = PACKAGE,
    idl_parcelables = [
        "src/main/aidl/android/content/res/AssetFileDescriptor.aidl",
    ],
    idl_srcs = glob(["src/main/aidl/com/afwsamples/testdpc/comp/*.aidl"]),
)

java_library(
    name = "test_utils",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/Utils.java"],
)

#
#android_local_test(
#    name = "PermissionsHelperTest",
#    srcs = ["src/test/java/com/afwsamples/testdpc/common/PermissionsHelperTest.java"],
#    manifest = MANIFEST,
#    deps = [
#        ":test_deps",
#        ":testdpc_lib",
#        "@robolectric//bazel:android-all",
#    ],
#)
#
#android_local_test(
#    name = "AppStatesServiceTest",
#    srcs = ["src/test/java/com/afwsamples/testdpc/feedback/AppStatesServiceTest.java"],
#    manifest = MANIFEST,
#    deps = [
#        ":androidx_deps",
#        ":test_deps",
#        ":testdpc_lib",
#        "@robolectric//bazel:android-all",
#    ],
#)
#
#android_local_test(
#    name = "WifiConfigUtilTest",
#    srcs = ["src/test/java/com/afwsamples/testdpc/policy/wifimanagement/WifiConfigUtilTest.java"],
#    manifest = MANIFEST,
#    deps = [
#        ":test_deps",
#        ":testdpc_lib",
#        "@robolectric//bazel:android-all",
#    ],
#)
#
#android_local_test(
#    name = "GetProvisioningModeActivityTest",
#    srcs = ["src/test/java/com/afwsamples/testdpc/provision/GetProvisioningModeActivityTest.java"],
#    manifest = MANIFEST,
#    test_class = "com.afwsamples.testdpc.provision.GetProvisioningModeActivityTest",
#    deps = [
#        ":test_deps",
#        ":testdpc_lib",
#    ],
#)
#
#android_local_test(
#    name = "GetProvisioningModeActivityTest",
#    srcs = ["src/test/java/com/afwsamples/testdpc/provision/GetProvisioningModeActivityTest.java"],
#    manifest = MANIFEST,
#    test_class = "com.afwsamples.testdpc.provision.GetProvisioningModeActivityTest",
#    deps = [
#        ":test_deps",
#        ":testdpc_lib",
#        "@maven//:org_robolectric_robolectric",
#        "@robolectric//bazel:android-all",
#    ],
#)

java_test(
    name = "BooleanParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/BooleanParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "ByteParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ByteParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "CharParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CharParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "DoubleParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/DoubleParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "FloatParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/FloatParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "IntParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/IntParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "LongParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/LongParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "ShortParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ShortParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "StringParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/StringParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "CustomParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CustomParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "CallbackTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CallbackTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "HelpTextGenerationTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/HelpTextGenerationTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "InvalidCallsTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/InvalidCallsTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "ParamTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ParamTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "RegistrationTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/RegistrationTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)
