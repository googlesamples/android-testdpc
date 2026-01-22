/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.setupcompat.internal;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.ISetupCompatService;
import com.google.android.setupcompat.util.Logger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * This class provides an instance of {@link ISetupCompatService}. It keeps track of the connection
 * state and reconnects if necessary.
 */
public class SetupCompatServiceProvider {

  private static final Logger LOG = new Logger("SetupCompatServiceProvider");

  /**
   * Returns an instance of {@link ISetupCompatService} if one already exists. If not, attempts to
   * rebind if the current state allows such an operation and waits until {@code waitTime} for
   * receiving the stub reference via {@link ServiceConnection#onServiceConnected(ComponentName,
   * IBinder)}.
   *
   * @throws IllegalStateException if called from the main thread since this is a blocking
   *     operation.
   * @throws TimeoutException if timed out waiting for {@code waitTime}.
   */
  public static ISetupCompatService get(Context context, long waitTime, @NonNull TimeUnit timeUnit)
      throws TimeoutException, InterruptedException {
    return getInstance(context).getService(waitTime, timeUnit);
  }

  @VisibleForTesting
  public ISetupCompatService getService(long timeout, TimeUnit timeUnit)
      throws TimeoutException, InterruptedException {
    Preconditions.checkState(
        disableLooperCheckForTesting || Looper.getMainLooper() != Looper.myLooper(),
        "getService blocks and should not be called from the main thread.");
    ServiceContext serviceContext = getCurrentServiceState();
    switch (serviceContext.state) {
      case CONNECTED:
        return serviceContext.compatService;

      case SERVICE_NOT_USABLE:
      case BIND_FAILED:
        // End states, no valid connection can be obtained ever.
        return null;

      case DISCONNECTED:
      case BINDING:
        return waitForConnection(timeout, timeUnit);

      case REBIND_REQUIRED:
        requestServiceBind();
        return waitForConnection(timeout, timeUnit);

      case NOT_STARTED:
        LOG.w("NOT_STARTED state only possible before instance is created.");
        return null;
    }
    throw new IllegalStateException("Unknown state = " + serviceContext.state);
  }

  private ISetupCompatService waitForConnection(long timeout, TimeUnit timeUnit)
      throws TimeoutException, InterruptedException {
    ServiceContext currentServiceState = getCurrentServiceState();
    if (currentServiceState.state == State.CONNECTED) {
      return currentServiceState.compatService;
    }

    CountDownLatch connectedStateLatch = getConnectedCondition();
    LOG.atInfo("Waiting for service to get connected");
    boolean stateChanged = connectedStateLatch.await(timeout, timeUnit);
    if (!stateChanged) {
      // Even though documentation states that disconnected service should connect again,
      // requesting rebind reduces the wait time to acquire a new connection.
      requestServiceBind();
      throw new TimeoutException(
          String.format("Failed to acquire connection after [%s %s]", timeout, timeUnit));
    }
    currentServiceState = getCurrentServiceState();
    LOG.atInfo(
        String.format(
            "Finished waiting for service to get connected. Current state = %s",
            currentServiceState.state));
    return currentServiceState.compatService;
  }

  /**
   * This method is being overwritten by {@link SetupCompatServiceProviderTest} for injecting an
   * instance of {@link CountDownLatch}.
   */
  @VisibleForTesting
  protected CountDownLatch createCountDownLatch() {
    return new CountDownLatch(1);
  }

  private synchronized void requestServiceBind() {
    ServiceContext currentServiceState = getCurrentServiceState();
    if (currentServiceState.state == State.CONNECTED) {
      LOG.atInfo("Refusing to rebind since current state is already connected");
      return;
    }
    if (currentServiceState.state != State.NOT_STARTED) {
      LOG.atInfo("Unbinding existing service connection.");
      context.unbindService(serviceConnection);
    }

    boolean bindAllowed;
    try {
      bindAllowed =
          context.bindService(COMPAT_SERVICE_INTENT, serviceConnection, Context.BIND_AUTO_CREATE);
    } catch (SecurityException e) {
      LOG.e("Unable to bind to compat service. " + e);
      bindAllowed = false;
    }

    if (bindAllowed) {
      // Robolectric calls ServiceConnection#onServiceConnected inline during Context#bindService.
      // This check prevents us from overriding connected state which usually arrives much later
      // in the normal world
      if (getCurrentState() != State.CONNECTED) {
        swapServiceContextAndNotify(new ServiceContext(State.BINDING));
        LOG.atInfo("Context#bindService went through, now waiting for service connection");
      }
    } else {
      // SetupWizard is not installed/calling app does not have permissions to bind.
      swapServiceContextAndNotify(new ServiceContext(State.BIND_FAILED));
      LOG.e("Context#bindService did not succeed.");
    }
  }

  @VisibleForTesting
  static final Intent COMPAT_SERVICE_INTENT =
      new Intent()
          .setPackage("com.google.android.setupwizard")
          .setAction("com.google.android.setupcompat.SetupCompatService.BIND");

  @VisibleForTesting
  State getCurrentState() {
    return serviceContext.state;
  }

  private synchronized ServiceContext getCurrentServiceState() {
    return serviceContext;
  }

  @VisibleForTesting
  void swapServiceContextAndNotify(ServiceContext latestServiceContext) {
    LOG.atInfo(
        String.format("State changed: %s -> %s", serviceContext.state, latestServiceContext.state));

    serviceContext = latestServiceContext;
    CountDownLatch countDownLatch = getAndClearConnectedCondition();
    if (countDownLatch != null) {
      countDownLatch.countDown();
    }
  }

  private CountDownLatch getAndClearConnectedCondition() {
    return connectedConditionRef.getAndSet(/* newValue= */ null);
  }

  /**
   * Cannot use {@link AtomicReference#updateAndGet(UnaryOperator)} to fix null reference since the
   * library needs to be compatible with legacy android devices.
   */
  private CountDownLatch getConnectedCondition() {
    CountDownLatch countDownLatch;
    // Loop until either count down latch is found or successfully able to update atomic reference.
    do {
      countDownLatch = connectedConditionRef.get();
      if (countDownLatch != null) {
        return countDownLatch;
      }
      countDownLatch = createCountDownLatch();
    } while (!connectedConditionRef.compareAndSet(/* expectedValue= */ null, countDownLatch));
    return countDownLatch;
  }

  @VisibleForTesting
  SetupCompatServiceProvider(Context context) {
    this.context = context.getApplicationContext();
  }

  @VisibleForTesting
  final ServiceConnection serviceConnection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
          State state = State.CONNECTED;
          if (binder == null) {
            state = State.DISCONNECTED;
            LOG.w("Binder is null when onServiceConnected was called!");
          }
          swapServiceContextAndNotify(
              new ServiceContext(state, ISetupCompatService.Stub.asInterface(binder)));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
          swapServiceContextAndNotify(new ServiceContext(State.DISCONNECTED));
        }

        @Override
        public void onBindingDied(ComponentName name) {
          swapServiceContextAndNotify(new ServiceContext(State.REBIND_REQUIRED));
        }

        @Override
        public void onNullBinding(ComponentName name) {
          swapServiceContextAndNotify(new ServiceContext(State.SERVICE_NOT_USABLE));
        }
      };

  private volatile ServiceContext serviceContext = new ServiceContext(State.NOT_STARTED);
  private final Context context;
  private final AtomicReference<CountDownLatch> connectedConditionRef = new AtomicReference<>();

  @VisibleForTesting
  enum State {
    /** Initial state of the service instance is completely created. */
    NOT_STARTED,

    /**
     * Attempt to call {@link Context#bindService(Intent, ServiceConnection, int)} failed because,
     * either Setupwizard is not installed or the app does not have permission to bind. This is an
     * unrecoverable situation.
     */
    BIND_FAILED,

    /**
     * Call to bind with the service went through, now waiting for {@link
     * ServiceConnection#onServiceConnected(ComponentName, IBinder)}.
     */
    BINDING,

    /** Provider is connected to the service and can call the API(s). */
    CONNECTED,

    /**
     * Not connected since provider received the call {@link
     * ServiceConnection#onServiceDisconnected(ComponentName)}, and waiting for {@link
     * ServiceConnection#onServiceConnected(ComponentName, IBinder)}.
     */
    DISCONNECTED,

    /**
     * Similar to {@link #BIND_FAILED}, the bind call went through but we received a "null" binding
     * via {@link ServiceConnection#onNullBinding(ComponentName)}. This is an unrecoverable
     * situation.
     */
    SERVICE_NOT_USABLE,

    /**
     * The provider has requested rebind via {@link Context#bindService(Intent, ServiceConnection,
     * int)} and is waiting for a service connection.
     */
    REBIND_REQUIRED
  }

  @VisibleForTesting
  static final class ServiceContext {
    final State state;
    @Nullable final ISetupCompatService compatService;

    private ServiceContext(State state, @Nullable ISetupCompatService compatService) {
      this.state = state;
      this.compatService = compatService;
      if (state == State.CONNECTED) {
        Preconditions.checkNotNull(
            compatService, "CompatService cannot be null when state is connected");
      }
    }

    @VisibleForTesting
    ServiceContext(State state) {
      this(state, /* compatService= */ null);
    }
  }

  @VisibleForTesting
  static SetupCompatServiceProvider getInstance(@NonNull Context context) {
    Preconditions.checkNotNull(context, "Context object cannot be null.");
    SetupCompatServiceProvider result = instance;
    if (result == null) {
      synchronized (SetupCompatServiceProvider.class) {
        result = instance;
        if (result == null) {
          instance = result = new SetupCompatServiceProvider(context.getApplicationContext());
          instance.requestServiceBind();
        }
      }
    }
    return result;
  }

  @VisibleForTesting
  public static void setInstanceForTesting(SetupCompatServiceProvider testInstance) {
    instance = testInstance;
  }

  @VisibleForTesting static boolean disableLooperCheckForTesting = false;

  // The instance is coming from Application context which alive during the application activate and
  // it's not depend on the activities life cycle, so we can avoid memory leak. However linter
  // cannot distinguish Application context or activity context, so we add @SuppressLint to avoid
  // lint error.
  @SuppressLint("StaticFieldLeak")
  private static volatile SetupCompatServiceProvider instance;
}
