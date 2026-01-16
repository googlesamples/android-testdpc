workspace(name = "testdpc")

android_sdk_repository(
    name = "androidsdk",
    api_level = 35,
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

RULES_JVM_EXTERNAL_TAG = "4.5"

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

http_archive(
    name = "robolectric",
    urls = ["https://github.com/robolectric/robolectric-bazel/archive/4.7.3.tar.gz"],
    strip_prefix = "robolectric-bazel-4.7.3",
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

maven_install(
    name = "maven",
    artifacts = [
        "androidx.annotation:annotation:1.5.0",
        "androidx.appcompat:appcompat:1.6.1",
        "androidx.appcompat:appcompat-resources:1.6.1",
        "androidx.collection:collection:1.2.0",
        "androidx.constraintlayout:constraintlayout:2.1.3",
        "androidx.core:core:1.9.0",
        "androidx.enterprise:enterprise-feedback:1.1.0",
        "androidx.legacy:legacy-support-core-ui:1.0.0",
        "androidx.legacy:legacy-support-v13:1.0.0",
        "androidx.lifecycle:lifecycle-common:2.5.1",
        "androidx.lifecycle:lifecycle-process:2.5.1",
        "androidx.lifecycle:lifecycle-runtime:2.5.1",
        "androidx.localbroadcastmanager:localbroadcastmanager:1.1.0",
        "androidx.preference:preference:1.1.0",
        "androidx.recyclerview:recyclerview:1.2.0",
        "androidx.test:core:1.5.0",
        "androidx.test:monitor:1.6.0",
        "androidx.test:runner:1.5.0",
        "androidx.window:window:1.2.0",
        "com.google.android.material:material:1.6.1",
        "com.google.guava:guava:31.1-android",
        "com.google.testparameterinjector:test-parameter-injector:1.15",
        "com.google.errorprone:error_prone_annotations:2.26.1",
        "junit:junit:4.13.2",
        "javax.inject:javax.inject:1",
        "org.hamcrest:java-hamcrest:2.0.0.0",
        "org.robolectric:robolectric-annotations:3.3.2",
        "org.robolectric:shadows-core:3.3.2",
        "org.bouncycastle:bcpkix-jdk15on:1.70",
        "org.bouncycastle:bcprov-jdk15on:1.70",
        "org.robolectric:robolectric:4.2",
        "com.google.truth:truth:1.4.2",
        "androidx.room:room-runtime:2.5.0",
        "androidx.room:room-common:2.5.0",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
    # for androidx.annotation 1.5.0. 1.6.0+ uses gradle module metadata
    # which rules_jvm_external cannot resolve yet, see
    # https://github.com/bazelbuild/rules_jvm_external/issues/909
    version_conflict_policy = "pinned",
)

http_archive(
    name = "setupdesign",
    build_file = "@//:setupdesign.BUILD",
    url = "https://android.googlesource.com/platform/external/setupdesign/+archive/4634dac90e3c09a78c2fcdfcb16ab9cb16265527.tar.gz",
)

http_archive(
    name = "setupcompat",
    build_file = "@//:setupcompat.BUILD",
    # Patch source code to avoid "cannot infer type arguments for Creator<T>" in 2 files
    patch_cmds = [
        "ed -s main/java/com/google/android/setupcompat/logging/ScreenKey.java <<<$',s/Creator<>/Creator<ScreenKey>/g\nw'",
        "ed -s main/java/com/google/android/setupcompat/logging/SetupMetric.java <<<$',s/Creator<>/Creator<SetupMetric>/g\nw'",
    ],
    url = "https://android.googlesource.com/platform/external/setupcompat/+archive/2ce41c8f4de550b5186233cec0a722dd0ffd9a84.tar.gz",
)
