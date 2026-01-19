workspace(name = "testdpc")

# Load Bazel toolchains for external repos FIRST
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

# Android SDK repository - using local Android SDK installation
load(":android_sdk.bzl", "android_sdk_repository")
android_sdk_repository(name = "androidsdk")

# Rules JVM External for Maven dependencies
RULES_JVM_EXTERNAL_TAG = "5.3"
RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"

http_archive(
    name = "rules_jvm_external",
    sha256 = "d31e369b854322ca5098ea12c69d7175ded971435e55c18dd9dd5f29cc5249ac",
    strip_prefix = "rules_jvm_external-5.3",
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/5.3/rules_jvm_external-5.3.tar.gz",
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")
rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")
rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

# Single consolidated maven_install with minimal dependencies to avoid conflicts
maven_install(
    name = "maven",
    artifacts = [
        # Core AndroidX dependencies only - minimal set
        "androidx.annotation:annotation:1.5.0",
        "androidx.appcompat:appcompat:1.6.1",
        "androidx.core:core:1.9.0",
        # Google Guava
        "com.google.guava:guava:31.1-android",
        # Testing
        "junit:junit:4.13.2",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
    # Use resolve mode to handle conflicts better
    # pin_versions = False allows some flexibility in version resolution
    version_conflict_policy = "pinned",
    # Add exclusions to avoid transitive conflicts
    excluded_artifacts = [
        "androidx.activity:activity",
        "androidx.activity:activity-ktx",
        "androidx.fragment:fragment",
        "androidx.fragment:fragment-ktx",
        "androidx.lifecycle:lifecycle-common",
        "androidx.lifecycle:lifecycle-runtime",
        "androidx.lifecycle:lifecycle-viewmodel",
        "androidx.constraintlayout:constraintlayout",
        "androidx.preference:preference",
        "androidx.recyclerview:recyclerview",
        "androidx.window:window",
        "androidx.room:room-runtime",
        "androidx.sqlite:sqlite",
        "com.google.android.material:material",
    ],
)

# NOTE: rules_android and setupcompat/setupdesign are commented out
# due to compatibility issues with Bazel 7.4.1 and rules_android 0.1.1
# We'll focus on building the core Java sources first

# Robolectric for testing
http_archive(
    name = "robolectric",
    urls = ["https://github.com/robolectric/robolectric-bazel/archive/4.7.3.tar.gz"],
    strip_prefix = "robolectric-bazel-4.7.3",
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")
robolectric_repositories()
