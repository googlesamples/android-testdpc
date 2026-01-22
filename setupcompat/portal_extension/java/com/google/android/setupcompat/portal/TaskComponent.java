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
 * A class that represents how a persistent notification is to be presented to the user using the
 * {@link com.google.android.setupcompat.portal.ISetupNotificationServicePortalExtension }.
 *
 * @deprecated use {@link com.google.android.setupcompat.portal.v1_1.TaskComponent}.
 */
@Deprecated
public class TaskComponent implements Parcelable {
  private final String packageName;
  private final String taskName;
  @StringRes private final int displayNameResId;
  @DrawableRes private final int displayIconResId;
  private final Intent itemClickIntent;

  private TaskComponent(
      String packageName,
      String taskName,
      @StringRes int displayNameResId,
      @DrawableRes int displayIconResId,
      Intent itemClickIntent) {
    this.packageName = packageName;
    this.taskName = taskName;
    this.displayNameResId = displayNameResId;
    this.displayIconResId = displayIconResId;
    this.itemClickIntent = itemClickIntent;
  }

  /** Returns a new instance of {@link Builder}. */
  public static Builder newBuilder() {
    return new Builder();
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

  /** Returns the Intent to start the user interface while progress item click. */
  public Intent getItemClickIntent() {
    return itemClickIntent;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getPackageName());
    dest.writeString(getTaskName());
    dest.writeInt(getDisplayName());
    dest.writeInt(getDisplayIcon());
    dest.writeParcelable(getItemClickIntent(), 0);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<TaskComponent> CREATOR =
      new Creator<TaskComponent>() {
        @Override
        public TaskComponent createFromParcel(Parcel in) {
          return TaskComponent.newBuilder()
              .setPackageName(in.readString())
              .setTaskName(in.readString())
              .setDisplayName(in.readInt())
              .setDisplayIcon(in.readInt())
              .setItemClickIntent(in.readParcelable(Intent.class.getClassLoader()))
              .build();
        }

        @Override
        public TaskComponent[] newArray(int size) {
          return new TaskComponent[size];
        }
      };

  /** Builder class for {@link com.google.android.setupcompat.portal.TaskComponent} objects. */
  public static class Builder {
    private String packageName;
    private String taskName;
    @StringRes private int displayNameResId;
    @DrawableRes private int displayIconResId;
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

    public Builder setItemClickIntent(Intent itemClickIntent) {
      this.itemClickIntent = itemClickIntent;
      return this;
    }

    public TaskComponent build() {
      Preconditions.checkNotNull(packageName, "packageName cannot be null.");
      Preconditions.checkNotNull(taskName, "serviceClass cannot be null.");
      Preconditions.checkNotNull(itemClickIntent, "Item click intent cannot be null");
      Preconditions.checkArgument(displayNameResId != 0, "Invalidate resource id of display name");
      Preconditions.checkArgument(displayIconResId != 0, "Invalidate resource id of display icon");

      return new TaskComponent(
          packageName, taskName, displayNameResId, displayIconResId, itemClickIntent);
    }
  }
}
