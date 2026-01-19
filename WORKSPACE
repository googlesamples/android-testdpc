workspace(name = "testdpc")

# Android SDK repository - using local Android SDK installation
load(":android_sdk.bzl", "android_sdk_repository")
android_sdk_repository(name = "androidsdk")

# Load Bazel toolchains for external repos
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

# Android rules for Bazel 7.4.1 - using rules_android 0.1.1
http_archive(
    name = "rules_android",
    sha256 = "6461c1c5744442b394f46645957d6bd3420eb1b421908fe63caa03091b1b3655",
    strip_prefix = "rules_android-0.1.1",
    urls = ["https://github.com/bazelbuild/rules_android/archive/refs/tags/v0.1.1.tar.gz"],
    patch_cmds = [
        "echo 'package(default_visibility = [\"//visibility:public\"])' > BUILD.bazel",
        "echo 'exports_files([\"defs.bzl\", \"rules.bzl\"])' >> BUILD.bazel",
        "mkdir -p tools/build_defs/repo && echo 'package(default_visibility = [\"//visibility:public\"])' > tools/build_defs/repo/BUILD.bazel",
        "echo 'exports_files([\"android.bzl\"])' >> tools/build_defs/repo/BUILD.bazel",
    ],
    patch_cmds_win = [
        "powershell -Command \"'package(default_visibility = [\\\"//visibility:public\\\"])' | Out-File -FilePath BUILD.bazel -Encoding utf8\"",
        "powershell -Command \"'exports_files([\\\"defs.bzl\\\", \\\"rules.bzl\\\"])' | Out-File -FilePath BUILD.bazel -Encoding utf8 -Append\"",
        "powershell -Command \"New-Item -ItemType Directory -Path tools/build_defs/repo -Force\"",
        "powershell -Command \"'package(default_visibility = [\\\"//visibility:public\\\"])' | Out-File -FilePath tools/build_defs/repo/BUILD.bazel -Encoding utf8\"",
        "powershell -Command \"'exports_files([\\\"android.bzl\\\"])' | Out-File -FilePath tools/build_defs/repo/BUILD.bazel -Encoding utf8 -Append\"",
    ],
)

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

# Single consolidated maven_install with all dependencies
maven_install(
    name = "maven",
    artifacts = [
        # AndroidX Core
        "androidx.annotation:annotation:1.5.0",
        "androidx.appcompat:appcompat:1.6.1",
        "androidx.appcompat:appcompat-resources:1.6.1",
        "androidx.collection:collection:1.2.0",
        "androidx.core:core:1.9.0",
        "androidx.core:core-ktx:1.9.0",
        "androidx.fragment:fragment:1.5.5",
        "androidx.fragment:fragment-ktx:1.5.5",
        "androidx.lifecycle:lifecycle-common:2.5.1",
        "androidx.lifecycle:lifecycle-process:2.5.1",
        "androidx.lifecycle:lifecycle-runtime:2.5.1",
        "androidx.lifecycle:lifecycle-runtime-ktx:2.5.1",
        "androidx.lifecycle:lifecycle-viewmodel:2.5.1",
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1",
        "androidx.localbroadcastmanager:localbroadcastmanager:1.1.0",
        "androidx.preference:preference:1.1.0",
        "androidx.recyclerview:recyclerview:1.2.0",
        "androidx.room:room-runtime:2.5.0",
        "androidx.room:room-common:2.5.0",
        "androidx.room:room-ktx:2.5.0",
        "androidx.sqlite:sqlite:2.3.0",
        "androidx.sqlite:sqlite-framework:2.3.0",
        "androidx.sqlite:sqlite-ktx:2.3.0",
        "androidx.test:core:1.5.0",
        "androidx.test:monitor:1.6.0",
        "androidx.test:runner:1.5.0",
        "androidx.window:window:1.2.0",
        "androidx.window:window-core:1.2.0",

        # ConstraintLayout
        "androidx.constraintlayout:constraintlayout:2.1.3",
        "androidx.constraintlayout:constraintlayout-core:2.1.3",

        # Material Design
        "com.google.android.material:material:1.6.1",

        # Enterprise Feedback
        "androidx.enterprise:enterprise-feedback:1.1.0",

        # Legacy Support
        "androidx.legacy:legacy-support-core-ui:1.0.0",
        "androidx.legacy:legacy-support-v13:1.0.0",

        # Google Guava
        "com.google.guava:guava:31.1-android",

        # Error Prone Annotations
        "com.google.errorprone:error_prone_annotations:2.26.1",

        # Bouncy Castle (for crypto)
        "org.bouncycastle:bcpkix-jdk15on:1.70",
        "org.bouncycastle:bcprov-jdk15on:1.70",

        # Testing
        "junit:junit:4.13.2",
        "org.hamcrest:java-hamcrest:2.0.0.0",
        "org.robolectric:robolectric:4.2",
        "org.robolectric:robolectric-annotations:3.3.2",
        "org.robolectric:shadows-core:3.3.2",
        "com.google.truth:truth:1.4.2",
        "com.google.testparameterinjector:test-parameter-injector:1.15",

        # Dependency Injection
        "javax.inject:javax.inject:1",

        # Kotlin (for potential future use)
        "org.jetbrains.kotlin:kotlin-stdlib:1.8.0",
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

# SetupCompat for Android SetupWizard compatibility
http_archive(
    name = "setupdesign",
    build_file = "@//:setupdesign.BUILD",
    url = "https://android.googlesource.com/platform/external/setupdesign/+archive/4634dac90e3c09a78c2fcdfcb16ab9cb16265527.tar.gz",
)

http_archive(
    name = "setupcompat",
    build_file = "@//:setupcompat.BUILD",
    patch_cmds = [
        "ed -s main/java/com/google/android/setupcompat/logging/ScreenKey.java <<<$',s/Creator<>/Creator<ScreenKey>/g\nw'",
        "ed -s main/java/com/google/android/setupcompat/logging/SetupMetric.java <<<$',s/Creator<>/Creator<SetupMetric>/g\nw'",
    ],
    patch_cmds_win = [
        "powershell -Command \"(Get-Content 'main/java/com/google/android/setupcompat/logging/ScreenKey.java') -replace 'Creator<>', 'Creator<ScreenKey>' | Set-Content 'main/java/com/google/android/setupcompat/logging/ScreenKey.java'\"",
        "powershell -Command \"(Get-Content 'main/java/com/google/android/setupcompat/logging/SetupMetric.java') -replace 'Creator<>', 'Creator<SetupMetric>' | Set-Content 'main/java/com/google/android/setupcompat/logging/SetupMetric.java'\"",
    ],
    url = "https://android.googlesource.com/platform/external/setupcompat/+archive/2ce41c8f4de550b5186233cec0a722dd0ffd9a84.tar.gz",
)

# Robolectric for testing
http_archive(
    name = "robolectric",
    urls = ["https://github.com/robolectric/robolectric-bazel/archive/4.7.3.tar.gz"],
    strip_prefix = "robolectric-bazel-4.7.3",
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")
robolectric_repositories()
