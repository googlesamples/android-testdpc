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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A class that represents how a persistent notification is to be presented to the user using the
 * {@link com.google.android.setupcompat.portal.ISetupNotificationService}.
 */
public class NotificationComponent implements Parcelable {

  @NotificationType private final int notificationType;
  private Bundle extraBundle = new Bundle();

  private NotificationComponent(@NotificationType int notificationType) {
    this.notificationType = notificationType;
  }

  protected NotificationComponent(Parcel in) {
    this(in.readInt());
    extraBundle = in.readBundle(Bundle.class.getClassLoader());
  }

  public int getIntExtra(String key, int defValue) {
    return extraBundle.getInt(key, defValue);
  }

  @NotificationType
  public int getNotificationType() {
    return notificationType;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(notificationType);
    dest.writeBundle(extraBundle);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<NotificationComponent> CREATOR =
      new Creator<NotificationComponent>() {
        @Override
        public NotificationComponent createFromParcel(Parcel in) {
          return new NotificationComponent(in);
        }

        @Override
        public NotificationComponent[] newArray(int size) {
          return new NotificationComponent[size];
        }
      };

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    NotificationType.UNKNOWN,
    NotificationType.INITIAL_ONGOING,
    NotificationType.PREDEFERRED,
    NotificationType.PREDEFERRED_PREPARING,
    NotificationType.DEFERRED,
    NotificationType.DEFERRED_ONGOING,
    NotificationType.PORTAL
  })
  public @interface NotificationType {
    int UNKNOWN = 0;
    int INITIAL_ONGOING = 1;
    int PREDEFERRED = 2;
    int PREDEFERRED_PREPARING = 3;
    int DEFERRED = 4;
    int DEFERRED_ONGOING = 5;
    int PORTAL = 6;
    int MAX = 7;
  }

  public static class Builder {

    private final NotificationComponent component;

    public Builder(@NotificationType int notificationType) {
      component = new NotificationComponent(notificationType);
    }

    public Builder putIntExtra(String key, int value) {
      component.extraBundle.putInt(key, value);
      return this;
    }

    public NotificationComponent build() {
      return component;
    }
  }
}
