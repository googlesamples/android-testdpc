exports_files(["LICENSE"])

MANIFEST = "src/main/AndroidManifest.xml"

MANIFEST_DEBUG = "src/main/AndroidManifestDebug.xml"

PACKAGE = "com.afwsamples.testdpc"

aar_import(
    name = "setupdesign",
    aar = "lib/setupdesign.aar",
    deps = [
        ":setupdesign_strings",
        ":setupcompat",
        "@maven//:com_google_android_material_material",
    ],
)

aar_import(
    name = "setupcompat",
    aar = "lib/setupcompat.aar",
)

aar_import(
    name = "setupdesign_strings",
    aar = "lib/strings.aar",
)

android_library(
    name = "setupdesign_deps",
    exports = [
        ":setupdesign",
        ":setupcompat"
    ],
)

android_library(
    name = "androidx_deps",
    exports = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_core_core",
        "@maven//:androidx_enterprise_enterprise_feedback",
        "@maven//:androidx_legacy_legacy_support_v13",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:androidx_lifecycle_lifecycle_process",
        "@maven//:androidx_lifecycle_lifecycle_runtime",
        "@maven//:androidx_localbroadcastmanager_localbroadcastmanager",
        "@maven//:androidx_preference_preference",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:androidx_collection_collection"
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
        "@maven//:org_robolectric_robolectric",
        "@robolectric//bazel:android-all",
        "@maven//:org_robolectric_annotations",
        "@maven//:org_robolectric_shadows_framework",
        "@maven//:com_google_truth_truth",
        "@maven//:androidx_test_core",
        "@maven//:com_google_testparameterinjector_test_parameter_injector"
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
        ":setupdesign_deps",
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

android_local_test(
    name = "PermissionsHelperTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/common/PermissionsHelperTest.java"],
    manifest = MANIFEST,
    deps = [
        ":testdpc_lib",
        ":test_deps"
    ],
    custom_package = "com.afwsamples.testdpc.common"
)

android_local_test(
    name = "AppStatesServiceTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/feedback/AppStatesServiceTest.java"],
    manifest = MANIFEST,
    deps = [
        ":testdpc_lib",
        ":androidx_deps",
        ":test_deps"
    ],
    custom_package = "com.afwsamples.testdpc.feedback"
)

android_local_test(
    name = "WifiConfigUtilTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/policy/wifimanagement/WifiConfigUtilTest.java"],
    manifest = MANIFEST,
    deps = [
        ":testdpc_lib",
        ":test_deps"
    ],
    custom_package = "com.afwsamples.testdpc.policy.wifimanagement"
)

android_local_test(
    name = "GetProvisioningModeActivityTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/provision/GetProvisioningModeActivityTest.java"],
    manifest = MANIFEST,
    deps = [
        ":testdpc_lib",
        ":test_deps"
    ],
    custom_package = "com.afwsamples.testdpc.provision"
)

java_test(
    name = "BooleanParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/BooleanParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "ByteParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ByteParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "CharParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CharParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "DoubleParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/DoubleParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "FloatParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/FloatParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "IntParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/IntParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "LongParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/LongParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "ShortParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ShortParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "StringParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/StringParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "CustomParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CustomParserTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "CallbackTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CallbackTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "HelpTextGenerationTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/HelpTextGenerationTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "InvalidCallsTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/InvalidCallsTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "ParamTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ParamTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)

java_test(
    name = "RegistrationTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/RegistrationTest.java"],
    deps = [
        ":test_utils",
        ":testdpc_lib",
        ":test_deps"
    ],
)