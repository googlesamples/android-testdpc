android_library(
    name = "setupdesign",
    srcs = glob([
        "main/src/**/*.java",
    ]),
    custom_package = "com.google.android.setupdesign",
    exports_manifest = 1,
    manifest = "main/AndroidManifest.xml",
    proguard_specs = [
        "proguard.flags",
    ],
    resource_files = glob(
        [
            "main/res/**",
        ],
        exclude_directories = 1,
    ),

    deps = [
        ":setupdesign_strings",
        "@setupcompat//:setupcompat",
        "@setupcompat//:partnerconfig",
        "@maven//:com_google_android_material_material",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:androidx_window_window",
        "@maven//:androidx_core_core",
        "@maven//:androidx_customview_customview",
        "@maven//:androidx_fragment_fragment",
        "@maven//:androidx_vectordrawable_vectordrawable",
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
    visibility = ["//visibility:public"],
)

android_library(
    name = "setupdesign_strings",
    manifest = "strings/AndroidManifest.xml",
    resource_files = glob(["strings/res/**"]),
)
