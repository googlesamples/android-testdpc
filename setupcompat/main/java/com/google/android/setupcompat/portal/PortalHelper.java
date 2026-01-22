/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.setupcompat.portal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import androidx.annotation.NonNull;
import com.google.android.setupcompat.internal.Preconditions;
import com.google.android.setupcompat.portal.PortalConstants.RemainingValues;
import com.google.android.setupcompat.util.Logger;

/** This class is responsible for safely executing methods on SetupNotificationService. */
public class PortalHelper {

  private static final Logger LOG = new Logger("PortalHelper");

  public static final String EXTRA_KEY_IS_SETUP_WIZARD = "isSetupWizard";

  public static final String ACTION_BIND_SETUP_NOTIFICATION_SERVICE =
      "com.google.android.setupcompat.portal.SetupNotificationService.BIND";

  public static final String RESULT_BUNDLE_KEY_RESULT = "Result";
  public static final String RESULT_BUNDLE_KEY_ERROR = "Error";
  public static final String RESULT_BUNDLE_KEY_PORTAL_NOTIFICATION_AVAILABLE =
      "PortalNotificationAvailable";

  public static final Intent NOTIFICATION_SERVICE_INTENT =
      new Intent(ACTION_BIND_SETUP_NOTIFICATION_SERVICE)
          .setPackage("com.google.android.setupwizard");

  /**
   * Binds SetupNotificationService. For more detail see {@link Context#bindService(Intent,
   * ServiceConnection, int)}
   */
  public static boolean bindSetupNotificationService(
      @NonNull Context context, @NonNull ServiceConnection connection) {
    Preconditions.checkNotNull(context, "Context cannot be null");
    Preconditions.checkNotNull(connection, "ServiceConnection cannot be null");
    try {
      return context.bindService(NOTIFICATION_SERVICE_INTENT, connection, Context.BIND_AUTO_CREATE);
    } catch (SecurityException e) {
      LOG.e("Exception occurred while binding SetupNotificationService", e);
      return false;
    }
  }

  /**
   * Registers a progress service to SUW service. The function response for bind service and invoke
   * function safely, and returns the result using {@link RegisterCallback}.
   *
   * @param context The application context.
   * @param component Identifies the progress service to execute.
   * @param callback Receives register result. {@link RegisterCallback#onSuccess} called while
   *     register succeed. {@link RegisterCallback#onFailure} called while register failed.
   */
  public static void registerProgressService(
      @NonNull Context context,
      @NonNull ProgressServiceComponent component,
      @NonNull RegisterCallback callback) {
    Preconditions.checkNotNull(context, "Context cannot be null");
    Preconditions.checkNotNull(component, "ProgressServiceComponent cannot be null");
    Preconditions.checkNotNull(callback, "RegisterCallback cannot be null");

    ServiceConnection connection =
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder != null) {
              ISetupNotificationService service =
                  ISetupNotificationService.Stub.asInterface(binder);
              try {
                if (VERSION.SDK_INT >= VERSION_CODES.N) {
                  final ServiceConnection serviceConnection = this;
                  service.registerProgressService(
                      component,
                      getCurrentUserHandle(),
                      new IPortalRegisterResultListener.Stub() {
                        @Override
                        public void onResult(Bundle result) {
                          if (result.getBoolean(RESULT_BUNDLE_KEY_RESULT, false)) {
                            callback.onSuccess(
                                result.getBoolean(
                                    RESULT_BUNDLE_KEY_PORTAL_NOTIFICATION_AVAILABLE, false));
                          } else {
                            callback.onFailure(
                                new IllegalStateException(
                                    result.getString(RESULT_BUNDLE_KEY_ERROR, "Unknown error")));
                          }
                          context.unbindService(serviceConnection);
                        }
                      });
                } else {
                  callback.onFailure(
                      new IllegalStateException(
                          "SetupNotificationService is not supported before Android N"));
                  context.unbindService(this);
                }
              } catch (RemoteException | NullPointerException e) {
                callback.onFailure(e);
                context.unbindService(this);
              }
            } else {
              callback.onFailure(
                  new IllegalStateException("SetupNotification should not return null binder"));
              context.unbindService(this);
            }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
            // Do nothing when service disconnected
          }
        };

    if (!bindSetupNotificationService(context, connection)) {
      LOG.e("Failed to bind SetupNotificationService.");
      callback.onFailure(new SecurityException("Failed to bind SetupNotificationService."));
    }
  }

  public static void isPortalAvailable(
      @NonNull Context context, @NonNull final PortalAvailableResultListener listener) {
    ServiceConnection connection =
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder != null) {
              ISetupNotificationService service =
                  ISetupNotificationService.Stub.asInterface(binder);

              try {
                listener.onResult(service.isPortalAvailable());
              } catch (RemoteException e) {
                LOG.e("Failed to invoke SetupNotificationService#isPortalAvailable");
                listener.onResult(false);
              }
            }
            context.unbindService(this);
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {}
        };

    if (!bindSetupNotificationService(context, connection)) {
      LOG.e(
          "Failed to bind SetupNotificationService. Do you have permission"
              + " \"com.google.android.setupwizard.SETUP_PROGRESS_SERVICE\"");
      listener.onResult(false);
    }
  }

  public static void isProgressServiceAlive(
      @NonNull final Context context,
      @NonNull final ProgressServiceComponent component,
      @NonNull final ProgressServiceAliveResultListener listener) {
    Preconditions.checkNotNull(context, "Context cannot be null");
    Preconditions.checkNotNull(component, "ProgressServiceComponent cannot be null");
    Preconditions.checkNotNull(listener, "ProgressServiceAliveResultCallback cannot be null");

    ServiceConnection connection =
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder != null) {
              ISetupNotificationService service =
                  ISetupNotificationService.Stub.asInterface(binder);

              try {
                if (VERSION.SDK_INT >= VERSION_CODES.N) {
                  listener.onResult(
                      service.isProgressServiceAlive(component, getCurrentUserHandle()));
                } else {
                  listener.onResult(false);
                }

              } catch (RemoteException e) {
                LOG.w("Failed to invoke SetupNotificationService#isProgressServiceAlive");
                listener.onResult(false);
              }
            }
            context.unbindService(this);
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {}
        };

    if (!bindSetupNotificationService(context, connection)) {
      LOG.e(
          "Failed to bind SetupNotificationService. Do you have permission"
              + " \"com.google.android.setupwizard.SETUP_PROGRESS_SERVICE\"");
      listener.onResult(false);
    }
  }

  private static UserHandle getCurrentUserHandle() {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      return UserHandle.getUserHandleForUid(Process.myUid());
    } else {
      return null;
    }
  }

  /**
   * Creates the {@code Bundle} including the bind progress service result.
   *
   * @param succeed whether bind service success or not.
   * @param errorMsg describe the reason why bind service failed.
   * @return A bundle include bind result and error message.
   */
  public static Bundle createResultBundle(
      boolean succeed, String errorMsg, boolean isPortalNotificationAvailable) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(RESULT_BUNDLE_KEY_RESULT, succeed);
    if (!succeed) {
      bundle.putString(RESULT_BUNDLE_KEY_ERROR, errorMsg);
    }
    bundle.putBoolean(
        RESULT_BUNDLE_KEY_PORTAL_NOTIFICATION_AVAILABLE, isPortalNotificationAvailable);
    return bundle;
  }

  /**
   * Returns {@code true}, if the intent is bound from SetupWizard, otherwise returns false.
   *
   * @param intent that received when onBind.
   */
  public static boolean isFromSUW(Intent intent) {
    return intent != null && intent.getBooleanExtra(EXTRA_KEY_IS_SETUP_WIZARD, false);
  }

  /** A callback for accepting the results of SetupNotificationService. */
  public interface RegisterCallback {
    void onSuccess(boolean isPortalNow);

    void onFailure(Throwable throwable);
  }

  public interface RegisterNotificationCallback {
    void onSuccess();

    void onFailure(Throwable throwable);
  }

  public interface ProgressServiceAliveResultListener {
    void onResult(boolean isAlive);
  }

  public interface PortalAvailableResultListener {
    void onResult(boolean isAvailable);
  }

  public static class RemainingValueBuilder {
    private final Bundle bundle = new Bundle();

    public static RemainingValueBuilder createBuilder() {
      return new RemainingValueBuilder();
    }

    public RemainingValueBuilder setRemainingSizeInKB(int size) {
      Preconditions.checkArgument(
          size >= 0, "The remainingSize should be positive integer or zero.");
      bundle.putInt(RemainingValues.REMAINING_SIZE_TO_BE_DOWNLOAD_IN_KB, size);
      return this;
    }

    public Bundle build() {
      return bundle;
    }

    private RemainingValueBuilder() {}
  }

  private PortalHelper() {}
}


