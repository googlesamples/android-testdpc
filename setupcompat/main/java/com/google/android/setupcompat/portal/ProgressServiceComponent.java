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

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.google.android.setupcompat.internal.Preconditions;

/**
 * A class that represents how a progress service to be registered to {@link
 * com.google.android.setupcompat.portal.ISetupNotificationService}.
 */
public class ProgressServiceComponent implements Parcelable {
  private final String packageName;
  private final String taskName;
  private final boolean isSilent;
  private final boolean autoRebind;
  private final long timeoutForReRegister;
  @StringRes private final int displayNameResId;
  @DrawableRes private final int displayIconResId;
  private final Intent serviceIntent;
  private final Intent itemClickIntent;

  private ProgressServiceComponent(
      String packageName,
      String taskName,
      boolean isSilent,
      boolean autoRebind,
      long timeoutForReRegister,
      @StringRes int displayNameResId,
      @DrawableRes int displayIconResId,
      Intent serviceIntent,
      Intent itemClickIntent) {
    this.packageName = packageName;
    this.taskName = taskName;
    this.isSilent = isSilent;
    this.autoRebind = autoRebind;
    this.timeoutForReRegister = timeoutForReRegister;
    this.displayNameResId = displayNameResId;
    this.displayIconResId = displayIconResId;
    this.serviceIntent = serviceIntent;
    this.itemClickIntent = itemClickIntent;
  }

  /** Returns a new instance of {@link Builder}. */
  public static Builder newBuilder() {
    return new ProgressServiceComponent.Builder();
  }

  /** Returns the package name where the service exist. */
  @NonNull
  public String getPackageName() {
    return packageName;
  }

  /** Returns the service class name */
  @NonNull
  public String getTaskName() {
    return taskName;
  }

  /** Returns the whether the service is silent or not */
  public boolean isSilent() {
    return isSilent;
  }

  /** Auto rebind progress service while service connection disconnect. Default: true */
  public boolean isAutoRebind() {
    return autoRebind;
  }

  /** The timeout period waiting for client register progress service again. */
  public long getTimeoutForReRegister() {
    return timeoutForReRegister;
  }

  /** Returns the string resource id of display name. */
  @StringRes
  public int getDisplayName() {
    return displayNameResId;
  }

  /** Returns the drawable resource id of display icon. */
  @DrawableRes
  public int getDisplayIcon() {
    return displayIconResId;
  }

  /** Returns the Intent used to bind progress service. */
  public Intent getServiceIntent() {
    return serviceIntent;
  }

  /** Returns the Intent to start the user interface while progress item click. */
  public Intent getItemClickIntent() {
    return itemClickIntent;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getPackageName());
    dest.writeString(getTaskName());
    dest.writeInt(isSilent() ? 1 : 0);
    dest.writeInt(getDisplayName());
    dest.writeInt(getDisplayIcon());
    dest.writeParcelable(getServiceIntent(), 0);
    dest.writeParcelable(getItemClickIntent(), 0);
    dest.writeInt(isAutoRebind() ? 1 : 0);
    dest.writeLong(getTimeoutForReRegister());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ProgressServiceComponent> CREATOR =
      new Creator<ProgressServiceComponent>() {
        @Override
        public ProgressServiceComponent createFromParcel(Parcel in) {
          return ProgressServiceComponent.newBuilder()
              .setPackageName(in.readString())
              .setTaskName(in.readString())
              .setSilentMode(in.readInt() == 1)
              .setDisplayName(in.readInt())
              .setDisplayIcon(in.readInt())
              .setServiceIntent(in.readParcelable(Intent.class.getClassLoader()))
              .setItemClickIntent(in.readParcelable(Intent.class.getClassLoader()))
              .setAutoRebind(in.readInt() == 1)
              .setTimeoutForReRegister(in.readLong())
              .build();
        }

        @Override
        public ProgressServiceComponent[] newArray(int size) {
          return new ProgressServiceComponent[size];
        }
      };

  /** Builder class for {@link ProgressServiceComponent} objects */
  public static class Builder {
    private String packageName;
    private String taskName;
    private boolean isSilent = false;
    private boolean autoRebind = true;
    private long timeoutForReRegister = 0L;
    @StringRes private int displayNameResId;
    @DrawableRes private int displayIconResId;
    private Intent serviceIntent;
    private Intent itemClickIntent;

    /** Sets the packages name which is the service exists */
    public Builder setPackageName(@NonNull String packageName) {
      this.packageName = packageName;
      return this;
    }

    /** Sets a name to identify what task this progress is. */
    public Builder setTaskName(@NonNull String taskName) {
      this.taskName = taskName;
      return this;
    }

    /** Sets the service as silent mode, it executes without UI on PortalActivity. */
    public Builder setSilentMode(boolean isSilent) {
      this.isSilent = isSilent;
      return this;
    }

    /** Sets the service need auto rebind or not when service connection disconnected. */
    public Builder setAutoRebind(boolean autoRebind) {
      this.autoRebind = autoRebind;
      return this;
    }

    /**
     * Sets the timeout period waiting for the client register again, only works when auto-rebind
     * disabled. When 0 is set, will read default configuration from SUW.
     */
    public Builder setTimeoutForReRegister(long timeoutForReRegister) {
      this.timeoutForReRegister = timeoutForReRegister;
      return this;
    }

    /** Sets the name which is displayed on PortalActivity */
    public Builder setDisplayName(@StringRes int displayNameResId) {
      this.displayNameResId = displayNameResId;
      return this;
    }

    /** Sets the icon which is display on PortalActivity */
    public Builder setDisplayIcon(@DrawableRes int displayIconResId) {
      this.displayIconResId = displayIconResId;
      return this;
    }

    public Builder setServiceIntent(Intent serviceIntent) {
      this.serviceIntent = serviceIntent;
      return this;
    }

    public Builder setItemClickIntent(Intent itemClickIntent) {
      this.itemClickIntent = itemClickIntent;
      return this;
    }

    public ProgressServiceComponent build() {
      Preconditions.checkNotNull(packageName, "packageName cannot be null.");
      Preconditions.checkNotNull(taskName, "serviceClass cannot be null.");
      Preconditions.checkNotNull(serviceIntent, "Service intent cannot be null.");
      Preconditions.checkNotNull(itemClickIntent, "Item click intent cannot be null");
      if (!isSilent) {
        Preconditions.checkArgument(
            displayNameResId != 0, "Invalidate resource id of display name");
        Preconditions.checkArgument(
            displayIconResId != 0, "Invalidate resource id of display icon");
      }
      return new ProgressServiceComponent(
          packageName,
          taskName,
          isSilent,
          autoRebind,
          timeoutForReRegister,
          displayNameResId,
          displayIconResId,
          serviceIntent,
          itemClickIntent);
    }

    private Builder() {}
  }
}
