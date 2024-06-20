设备政策控制测试 (Test DPC) 应用程序
=========================================

[English](https://github.com/googlesamples/android-testdpc/blob/master/README.md) | **简体中文**

Test DPC 是一款帮助企业移动管理、独立软件厂商和原始设备制造商在 Android 企业托管配置文件 (即工作配置文件)的一款应用程序. 它既是一个设备策略控制器样本，也是一个测试应用程序，可灵活运用 Android 企业可用的 API。它支持 Android 5.0 及更高版本。
请参阅 [文档（位于google.cn）](https://developer.android.google.cn/work/index.html)，了解有关企业中的 Android 的更多信息。

入门
---------------

本示例使用 Bazel 构建系统。要构建此项目，请使用 `bazel build testdpc `命令。

您还可以在 [Play 商店](https://play.google.com/store/apps/details?id=com.afwsamples.testdpc "您所在的国家和地区可能无法访问此链接。") 中获得此应用程序。

配置
------------

您可以在 [此处（位于google.cn）](https://developers.google.cn/android/work/prov-devices#Key_provisioning_differences_across_android_releases) 找到各种配置方法。让我们以其中几种为例。

#### QR 码配置（仅限 Device Owner Android 7.0+） ####
1. 重置设备并在设置向导中点击欢迎屏幕 6 次。
2. 安装向导会提示用户连接互联网，以便安装向导下载 QR 码阅读器。
3. 修改（如需要）并扫描 [此二维码](http://down-box.appspot.com/qr/nQB0tw7b)。
4. 按照屏幕上的说明继续操作。

#### ADB 命令 ####

**Device Owner （设备所有者）**

*   执行此`ADB`命令:

    ```console
    adb shell dpm set-device-owner com.afwsamples.testdpc/.DeviceAdminReceiver
    ```

**Profile Owner（配置文件所有者）**

*   启动 "Set up TestDPC "应用程序，创建管理配置文件（如果该应用程序
    似乎不正常，而且您处于深色模式，请切换到浅色模式模式）。
*   跳过末尾的添加账户操作。

**COPE Profile Owner （公司所有的配置文件所有者）**

*   启动 "Set up TestDPC "应用程序，创建管理配置文件（如果该应用程序
    似乎不正常，而且您处于深色模式，请切换到浅色模式模式）。
*   跳过末尾的添加账户操作。
*   执行此`ADB`命令:

    ```console
    adb shell dpm mark-profile-owner-on-organization-owned-device --user 10 com.afwsamples.testdpc/.DeviceAdminReceiver`
    ```

## TestDPC 作为设备管理角色持有者

TestDPC v9.0.5+ 可设置为设备管理角色持有者。

*  执行下面的 `adb` 命令:

    ```console
    adb shell cmd role set-bypassing-role-qualification true
    adb shell cmd role add-role-holder android.app.role.DEVICE_POLICY_MANAGEMENT com.afwsamples.testdpc
    ```

    注意：与 设备所有者/配置文件所有者 不同的是，这一更改不会持久，因此如果设备重启，TestDPC 需要再次被标记为角色持有者。
    
导入 Android Studio
---------------------

要在 Android Studio 中导入该版本库，需要使用 [Bazel for Android Studio](https://plugins.jetbrains.com/plugin/9185-bazel-for-android-studio)
插件。

导入项目时，您必须选择包含 Bazel 的
BUILD` 文件的文件夹。当提示选择 "项目视图 "时，可以选择
选项 "复制外部"，并选择该资源库中的 `scripts/ij.bazelproject`。

一旦 Bazel 完成导入操作和项目的首次同步，您就可以创建 "运行配置"。
项目后，即可创建 "运行配置"。
选择 "Bazel 命令 "作为配置类型，并添加 `//:testdpc` 作为 "目标表达式"。
"目标表达式"。

现在，您可以在 Android Studio 中运行该项目。


使用 Bazel 构建
-------------------

仓库包含一个用于构建应用程序的 `build.sh` 脚本。 所需的
[setupdesign 库](https://android.googlesource.com/platform/external/setupdesign/+/refs/heads/main "您所在的国家和地区可能无法访问此链接。")现在可使用命令行实用程序 `ed`动态导入和修补。这需要被添加到path才能成功构建项目。

支持
-------

如果您在此示例中发现错误，请提交问题：
https://github.com/googlesamples/android-testdpc/issues

我们鼓励您提交补丁，您可以通过 fork 本项目并在 GitHub 上提交 pull request 来提交补丁。

许可证
-------

采用 Apache 2.0 许可证授权。详情请参见 LICENSE 文件。

如何贡献？
--------------------------

请阅读并遵循 CONTRIB 文件中的步骤。
