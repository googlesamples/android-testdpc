/*
 * Copyright (C) 2019 The Android Open Source Project
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

/**
 * Same as {@link com.google.android.setupdesign.DividerItemDecoration.DividedViewHolder} but not
 * limited for use to {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}
 */
public interface Dividable {
  /**
   * Returns whether divider is allowed above this item. A divider will be shown only if both items
   * immediately above and below it allows this divider.
   */
  boolean isDividerAllowedAbove();

  /**
   * Returns whether divider is allowed below this item. A divider will be shown only if both items
   * immediately above and below it allows this divider.
   */
  boolean isDividerAllowedBelow();
}
