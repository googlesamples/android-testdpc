android_library(
    name = "setupcompat",
    srcs = glob([
        "main/java/**/*.java",
        "main/java/**/*.kt",
    ], exclude = ["partnerconfig/**"]),
    custom_package = "com.google.android.setupcompat",
    idl_import_root = "external/setupcompat/main/aidl",
    idl_parcelables = [
        "main/aidl/com/google/android/setupcompat/portal/NotificationComponent.aidl",
        "main/aidl/com/google/android/setupcompat/portal/ProgressServiceComponent.aidl",
    ],
    idl_srcs = [
        "main/aidl/com/google/android/setupcompat/ISetupCompatService.aidl",
        "main/aidl/com/google/android/setupcompat/portal/IPortalProgressCallback.aidl",
        "main/aidl/com/google/android/setupcompat/portal/IPortalProgressService.aidl",
        "main/aidl/com/google/android/setupcompat/portal/IPortalRegisterResultListener.aidl",
        "main/aidl/com/google/android/setupcompat/portal/ISetupNotificationService.aidl",
        "main/aidl/com/google/android/setupcompat/portal/v1_1/IPortalProgressCallback.aidl",
    ],
    manifest = "AndroidManifest.xml",
    proguard_specs = ["proguard.flags"],
    resource_files = glob([
        "main/res/**",
    ]),
    deps = [
        ":partnerconfig",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
    visibility = ["//visibility:public"],
)

android_library(
    name = "partnerconfig",
    srcs = glob([
        "partnerconfig/java/**/*.java",
    ]),
    custom_package = "com.google.android.setupcompat.partnerconfig",
    exports_manifest = 1,
    manifest = "partnerconfig/AndroidManifest.xml",
    deps = [
        ":setupcompat_util",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_window_window",
    ],
    visibility = ["//visibility:public"],
)

android_library(
    name = "setupcompat_util",
    srcs = ["main/java/com/google/android/setupcompat/util/BuildCompatUtils.java"],
    custom_package = "com.google.android.setupcompat",
    manifest = "AndroidManifest.xml",
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)
