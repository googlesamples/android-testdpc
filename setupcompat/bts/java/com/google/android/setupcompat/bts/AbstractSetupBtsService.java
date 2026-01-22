/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.setupcompat.bts;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.internal.Preconditions;
import com.google.android.setupcompat.util.Logger;
import java.util.concurrent.Executor;

/** Class to handle service binding from SUW, and execute the client's task in the executor. */
public abstract class AbstractSetupBtsService extends Service {
  private static final Logger LOG = new Logger(AbstractSetupBtsService.class);

  private static final String SETUP_WIZARD_PACKAGE_NAME = "com.google.android.setupwizard";

  private static final String BTS_STARTER_FOR_TEST =
      "com.google.android.apps.setupwizard.sample.bts.starter";

  private static final String SETUP_BTS_PERMISSION = "com.google.android.setupwizard.SETUP_BTS";

  @VisibleForTesting
  static final String SETUP_WIZARD_RELEASE_CERTIFICATE_STRING =
      "308204433082032ba003020102020900c2e08746644a308d300d06092a864886f70d01010405003074310b300"
          + "9060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d"
          + "4d6f756e7461696e205669657731143012060355040a130b476f6f676c6520496e632e3110300e06035"
          + "5040b1307416e64726f69643110300e06035504031307416e64726f6964301e170d3038303832313233"
          + "313333345a170d3336303130373233313333345a3074310b30090603550406130255533113301106035"
          + "50408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e2056696577311430"
          + "12060355040a130b476f6f676c6520496e632e3110300e060355040b1307416e64726f69643110300e0"
          + "6035504031307416e64726f696430820120300d06092a864886f70d01010105000382010d0030820108"
          + "0282010100ab562e00d83ba208ae0a966f124e29da11f2ab56d08f58e2cca91303e9b754d372f640a71"
          + "b1dcb130967624e4656a7776a92193db2e5bfb724a91e77188b0e6a47a43b33d9609b77183145ccdf7b"
          + "2e586674c9e1565b1f4c6a5955bff251a63dabf9c55c27222252e875e4f8154a645f897168c0b1bfc61"
          + "2eabf785769bb34aa7984dc7e2ea2764cae8307d8c17154d7ee5f64a51a44a602c249054157dc02cd5f"
          + "5c0e55fbef8519fbe327f0b1511692c5a06f19d18385f5c4dbc2d6b93f68cc2979c70e18ab93866b3bd"
          + "5db8999552a0e3b4c99df58fb918bedc182ba35e003c1b4b10dd244a8ee24fffd333872ab5221985eda"
          + "b0fc0d0b145b6aa192858e79020103a381d93081d6301d0603551d0e04160414c77d8cc2211756259a7"
          + "fd382df6be398e4d786a53081a60603551d2304819e30819b8014c77d8cc2211756259a7fd382df6be3"
          + "98e4d786a5a178a4763074310b3009060355040613025553311330110603550408130a43616c69666f7"
          + "26e6961311630140603550407130d4d6f756e7461696e205669657731143012060355040a130b476f6f"
          + "676c6520496e632e3110300e060355040b1307416e64726f69643110300e06035504031307416e64726"
          + "f6964820900c2e08746644a308d300c0603551d13040530030101ff300d06092a864886f70d01010405"
          + "0003820101006dd252ceef85302c360aaace939bcff2cca904bb5d7a1661f8ae46b2994204d0ff4a68c"
          + "7ed1a531ec4595a623ce60763b167297a7ae35712c407f208f0cb109429124d7b106219c084ca3eb3f9"
          + "ad5fb871ef92269a8be28bf16d44c8d9a08e6cb2f005bb3fe2cb96447e868e731076ad45b33f6009ea1"
          + "9c161e62641aa99271dfd5228c5c587875ddb7f452758d661f6cc0cccb7352e424cc4365c523532f732"
          + "5137593c4ae341f4db41edda0d0b1071a7c440f0fe9ea01cb627ca674369d084bd2fd911ff06cdbf2cf"
          + "a10dc0f893ae35762919048c7efc64c7144178342f70581c9de573af55b390dd7fdb9418631895d5f75"
          + "9f30112687ff621410c069308a";

  @VisibleForTesting
  static final String SETUP_WIZARD_DEBUG_CERTIFICATE_STRING =
      "308204a830820390a003020102020900d585b86c7dd34ef5300d06092a864886f70d0101040500308194310b3"
          + "009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130"
          + "d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b13"
          + "07416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011"
          + "613616e64726f696440616e64726f69642e636f6d301e170d3038303431353233333635365a170d3335"
          + "303930313233333635365a308194310b3009060355040613025553311330110603550408130a43616c6"
          + "9666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307"
          + "416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f6"
          + "9643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d30820120"
          + "300d06092a864886f70d01010105000382010d00308201080282010100d6ce2e080abfe2314dd18db3c"
          + "fd3185cb43d33fa0c74e1bdb6d1db8913f62c5c39df56f846813d65bec0f3ca426b07c5a8ed5a3990c1"
          + "67e76bc999b927894b8f0b22001994a92915e572c56d2a301ba36fc5fc113ad6cb9e7435a16d23ab7df"
          + "aeee165e4df1f0a8dbda70a869d516c4e9d051196ca7c0c557f175bc375f948c56aae86089ba44f8aa6"
          + "a4dd9a7dbf2c0a352282ad06b8cc185eb15579eef86d080b1d6189c0f9af98b1c2ebd107ea45abdb68a"
          + "3c7838a5e5488c76c53d40b121de7bbd30e620c188ae1aa61dbbc87dd3c645f2f55f3d4c375ec4070a9"
          + "3f7151d83670c16a971abe5ef2d11890e1b8aef3298cf066bf9e6ce144ac9ae86d1c1b0f020103a381f"
          + "c3081f9301d0603551d0e041604148d1cc5be954c433c61863a15b04cbc03f24fe0b23081c90603551d"
          + "230481c13081be80148d1cc5be954c433c61863a15b04cbc03f24fe0b2a1819aa48197308194310b300"
          + "9060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d"
          + "4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b130"
          + "7416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d01090116"
          + "13616e64726f696440616e64726f69642e636f6d820900d585b86c7dd34ef5300c0603551d130405300"
          + "30101ff300d06092a864886f70d0101040500038201010019d30cf105fb78923f4c0d7dd223233d4096"
          + "7acfce00081d5bd7c6e9d6ed206b0e11209506416ca244939913d26b4aa0e0f524cad2bb5c6e4ca1016"
          + "a15916ea1ec5dc95a5e3a010036f49248d5109bbf2e1e618186673a3be56daf0b77b1c229e3c255e3e8"
          + "4c905d2387efba09cbf13b202b4e5a22c93263484a23d2fc29fa9f1939759733afd8aa160f4296c2d01"
          + "63e8182859c6643e9c1962fa0c18333335bc090ff9a6b22ded1ad444229a539a94eefadabd065ced24b"
          + "3e51e5dd7b66787bef12fe97fba484c423fb4ff8cc494c02f0f5051612ff6529393e8e46eac5bb21f27"
          + "7c151aa5f2aa627d1e89da70ab6033569de3b9897bfff7ca9da3e1243f60b";

  @VisibleForTesting boolean allowDebugKeys = false;

  @VisibleForTesting IBtsTaskServiceCallback callback;

  /** Allow debug signature calling app when developing stage. */
  protected void setAllowDebugKeys(boolean allowed) {
    allowDebugKeys = allowed;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    if (verifyIntentAction(intent)) {
      return binder;
    } else {
      LOG.w(
          "["
              + this.getClass().getSimpleName()
              + "] Unauthorized binder uid="
              + Binder.getCallingUid()
              + ", intentAction="
              + (intent == null ? "(null)" : intent.getAction()));
      return null;
    }
  }

  @Override
  public boolean onUnbind(Intent intent) {
    if (verifyIntentAction(intent)) {
      callback = null;
    }
    return super.onUnbind(intent);
  }

  private boolean verifyIntentAction(Intent intent) {
    if (intent != null
        && intent.getAction() != null
        && intent.getAction().equals(getIntentAction())) {
      return true;
    }

    return false;
  }

  /**
   * Called when the task is finished.
   *
   * @param succeed whether the task success or not.
   * @param failedReason A simple phrase to explain the failed reason. Like "No network". Null if
   *     task is success.
   */
  protected void onTaskFinished(boolean succeed, @Nullable String failedReason) {
    LOG.atDebug("onTaskFinished callback " + ((callback == null) ? "is null." : "is not null."));
    if (callback != null) {
      try {
        Bundle metricBundle = new Bundle();
        metricBundle.putBoolean(Constants.EXTRA_KEY_TASK_SUCCEED, succeed);
        metricBundle.putString(Constants.EXTRA_KEY_TASK_FAILED_REASON, failedReason);
        callback.onTaskFinished(metricBundle);
      } catch (RemoteException e) {
        LOG.e(
            "[" + this.getClass().getSimpleName() + "] Fail to invoke remove method onJobFinished");
      }
    }
  }

  /**
   * Gets the intent action that expected to execute the task. Use to avoid the receiver launch
   * unexpectedly.
   */
  @NonNull
  protected abstract String getIntentAction();

  /** Returns the executor used to execute the task. */
  @NonNull
  protected abstract Executor getExecutor();

  /** Tasks can be done before activity launched, in order to remove the loading before activity. */
  protected abstract void onStartTask();

  @VisibleForTesting
  final IBtsTaskService.Stub binder =
      new IBtsTaskService.Stub() {
        @Override
        public void setCallback(IBtsTaskServiceCallback callback) {
          LOG.atDebug("setCallback called.");
          if (verifyCallingApp()) {
            AbstractSetupBtsService.this.callback = callback;
            Executor executor = getExecutor();

            if (executor != null) {
              executor.execute(
                  () -> {
                    Preconditions.ensureNotOnMainThread(
                        AbstractSetupBtsService.this.getClass().getSimpleName() + "::onStartTask");
                    onStartTask();
                  });
            }
          } else {
            if (callback != null) {
              try {
                callback.onTaskFinished(Bundle.EMPTY);
              } catch (RemoteException e) {
                LOG.e("Error occurred while invoke remote method onTaskFinished");
              }
            }
            LOG.e(
                "BTS service bound with untrusted application, callingUid="
                    + Binder.getCallingUid());
          }
        }
      };

  @VisibleForTesting
  boolean verifyCallingApp() {
    if (verifyCallingPackageName() && verifyCallingSignature() && verifyCallingAppPermission()) {
      LOG.atInfo("Trusted caller=" + getPackageManager().getNameForUid(Binder.getCallingUid()));
      return true;
    } else {
      LOG.e("Untrusted caller=" + getPackageManager().getNameForUid(Binder.getCallingUid()));
      return false;
    }
  }

  @VisibleForTesting
  boolean verifyCallingPackageName() {
    String packageName = getPackageManager().getNameForUid(Binder.getCallingUid());
    if (SETUP_WIZARD_PACKAGE_NAME.equals(packageName)
        || (allowDebugKeys && BTS_STARTER_FOR_TEST.equals(packageName))) {
      LOG.atDebug("Package name match to SetupWizard");
      return true;
    } else {
      LOG.w("Untrusted package:" + packageName);
      return false;
    }
  }

  @VisibleForTesting
  boolean verifyCallingSignature() {
    String packageName = getPackageManager().getNameForUid(Binder.getCallingUid());
    if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
      try {
        PackageInfo info =
            getPackageManager()
                .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);

        for (Signature signature : info.signingInfo.getApkContentsSigners()) {
          if (SETUP_WIZARD_RELEASE_CERTIFICATE_STRING.equals(signature.toCharsString())
              || (isAllowDebugKeysOrBuild()
                  && SETUP_WIZARD_DEBUG_CERTIFICATE_STRING.equals(signature.toCharsString()))) {
            return true;
          }
        }
      } catch (NameNotFoundException | NullPointerException e) {
        LOG.e("Exception occurred while verify signature", e);
      }
    } else {
      LOG.w("Signature verify is not support before Android P.");
      return false;
    }

    LOG.w("Signature not match to SetupWizard");
    return false;
  }

  private boolean isAllowDebugKeysOrBuild() {
    return Build.TYPE.equals("userdebug") || Build.TYPE.equals("eng") || allowDebugKeys;
  }

  @VisibleForTesting
  boolean verifyCallingAppPermission() {
    int checkPermission =
        checkPermission(SETUP_BTS_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid());
    if (PackageManager.PERMISSION_GRANTED == checkPermission) {
      LOG.atDebug(
          "permission:"
              + SETUP_BTS_PERMISSION
              + ", grant pid="
              + Binder.getCallingPid()
              + ", uid="
              + Binder.getCallingUid()
              + ", checkPermission="
              + checkPermission);
      return true;
    } else {
      return false;
    }
  }
}
