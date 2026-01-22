/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.setupdesign.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.google.android.setupdesign.R;
import java.util.ArrayList;

/** An abstract item hierarchy; provides default implementation for ID and observers. */
public abstract class AbstractItemHierarchy implements ItemHierarchy {

  /* static section */

  private static final String TAG = "AbstractItemHierarchy";

  /* non-static section */

  private final ArrayList<Observer> observers = new ArrayList<>();
  private int id = View.NO_ID;

  public AbstractItemHierarchy() {}

  public AbstractItemHierarchy(Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudAbstractItem);
    id = a.getResourceId(R.styleable.SudAbstractItem_android_id, View.NO_ID);
    a.recycle();
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public int getViewId() {
    return getId();
  }

  @Override
  public void registerObserver(Observer observer) {
    observers.add(observer);
  }

  @Override
  public void unregisterObserver(Observer observer) {
    observers.remove(observer);
  }

  /** @see Observer#onChanged(ItemHierarchy) */
  public void notifyChanged() {
    for (Observer observer : observers) {
      observer.onChanged(this);
    }
  }

  /** @see Observer#onItemRangeChanged(ItemHierarchy, int, int) */
  public void notifyItemRangeChanged(int position, int itemCount) {
    if (position < 0) {
      Log.w(TAG, "notifyItemRangeChanged: Invalid position=" + position);
      return;
    }
    if (itemCount < 0) {
      Log.w(TAG, "notifyItemRangeChanged: Invalid itemCount=" + itemCount);
      return;
    }

    for (Observer observer : observers) {
      observer.onItemRangeChanged(this, position, itemCount);
    }
  }

  /** @see Observer#onItemRangeInserted(ItemHierarchy, int, int) */
  public void notifyItemRangeInserted(int position, int itemCount) {
    if (position < 0) {
      Log.w(TAG, "notifyItemRangeInserted: Invalid position=" + position);
      return;
    }
    if (itemCount < 0) {
      Log.w(TAG, "notifyItemRangeInserted: Invalid itemCount=" + itemCount);
      return;
    }

    for (Observer observer : observers) {
      observer.onItemRangeInserted(this, position, itemCount);
    }
  }

  /** @see Observer#onItemRangeMoved(ItemHierarchy, int, int, int) */
  public void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    if (fromPosition < 0) {
      Log.w(TAG, "notifyItemRangeMoved: Invalid fromPosition=" + fromPosition);
      return;
    }
    if (toPosition < 0) {
      Log.w(TAG, "notifyItemRangeMoved: Invalid toPosition=" + toPosition);
      return;
    }
    if (itemCount < 0) {
      Log.w(TAG, "notifyItemRangeMoved: Invalid itemCount=" + itemCount);
      return;
    }

    for (Observer observer : observers) {
      observer.onItemRangeMoved(this, fromPosition, toPosition, itemCount);
    }
  }

  /** @see Observer#onItemRangeRemoved(ItemHierarchy, int, int) */
  public void notifyItemRangeRemoved(int position, int itemCount) {
    if (position < 0) {
      Log.w(TAG, "notifyItemRangeInserted: Invalid position=" + position);
      return;
    }
    if (itemCount < 0) {
      Log.w(TAG, "notifyItemRangeInserted: Invalid itemCount=" + itemCount);
      return;
    }

    for (Observer observer : observers) {
      observer.onItemRangeRemoved(this, position, itemCount);
    }
  }
}
