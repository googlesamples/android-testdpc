def _android_sdk_impl(repository_ctx):
    """Creates a proper Android SDK repository for Bazel."""
    sdk_path = "C:/Users/AhsanHussain/AppData/Local/Android/Sdk"

    # Symlink the entire SDK
    repository_ctx.symlink(sdk_path, "sdk")

    # Create BUILD file with proper filegroup definitions
    repository_ctx.file("BUILD.bazel", """
package(default_visibility = ["//visibility:public"])

# Root SDK filegroup
filegroup(
    name = "sdk",
    srcs = glob(["sdk/**"]),
)

# Android platform JAR (Android 36)
filegroup(
    name = "android_jar",
    srcs = ["sdk/platforms/android-36/android.jar"],
)

# AIDL tool (Windows executable)
filegroup(
    name = "aidl",
    srcs = ["sdk/build-tools/36.1.0/aidl.exe"],
)

# AAPT2 tool (Windows executable)
filegroup(
    name = "aapt2",
    srcs = ["sdk/build-tools/36.1.0/aapt2.exe"],
)

# DX tool
filegroup(
    name = "dx",
    srcs = ["sdk/build-tools/36.1.0/dx.bat"],
)

# Android SDK tools
filegroup(
    name = "android_sdk_tools",
    srcs = glob(["sdk/tools/**"]),
)

# Platform tools
filegroup(
    name = "platform_tools",
    srcs = glob(["sdk/platform-tools/**"]),
)

# For compatibility with rules_android
filegroup(
    name = "all_files",
    srcs = glob(["sdk/**"]),
)
""")

android_sdk_repository = repository_rule(
    implementation = _android_sdk_impl,
    attrs = {},
    doc = """Create Android SDK repository for Bazel builds.

This rule creates a local Android SDK repository by symlinking the
local Android SDK installation. It provides filegroup targets for:
- android_jar: The platform JAR (Android 36)
- aidl: The AIDL tool
- aapt2: The AAPT2 packaging tool
- sdk: The entire SDK tree

Usage in WORKSPACE:
    load(":android_sdk.bzl", "android_sdk_repository")
    android_sdk_repository(name = "androidsdk")
""",
)
