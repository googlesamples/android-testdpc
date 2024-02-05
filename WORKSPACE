android_sdk_repository(
    name = "androidsdk",
    api_level = 34,
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.2"
RULES_JVM_EXTERNAL_SHA = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "androidx.annotation:annotation:1.3.0",
        "androidx.appcompat:appcompat:1.6.1",
        "androidx.appcompat:appcompat-resources:1.6.1",
        "androidx.constraintlayout:constraintlayout:2.1.3",
        "androidx.core:core:1.6.0",
        "androidx.enterprise:enterprise-feedback:1.1.0",
        "androidx.legacy:legacy-support-core-ui:1.0.0",
        "androidx.legacy:legacy-support-v13:1.0.0",
        "androidx.lifecycle:lifecycle-common:2.5.1",
        "androidx.lifecycle:lifecycle-process:2.5.1",
        "androidx.lifecycle:lifecycle-runtime:2.5.1",
        "androidx.localbroadcastmanager:localbroadcastmanager:1.1.0",
        "androidx.preference:preference:1.1.0",
        "androidx.recyclerview:recyclerview:1.2.0",
        "androidx.collection:collection:1.2.0",
        "com.google.android.material:material:1.5.0",
        "com.google.guava:guava:31.1-android",
        "org.bouncycastle:bcpkix-jdk15on:1.70",
        "org.bouncycastle:bcprov-jdk15on:1.70"
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)
