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

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import com.google.android.setupdesign.DividerItemDecoration;

/**
 * ViewHolder for the RecyclerItemAdapter that describes an item view and metadata about its place
 * within the RecyclerView.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder
    implements DividerItemDecoration.DividedViewHolder {

  private boolean isEnabled;
  private IItem item;

  ItemViewHolder(View itemView) {
    super(itemView);
  }

  @Override
  public boolean isDividerAllowedAbove() {
    return item instanceof Dividable ? ((Dividable) item).isDividerAllowedAbove() : isEnabled;
  }

  @Override
  public boolean isDividerAllowedBelow() {
    return item instanceof Dividable ? ((Dividable) item).isDividerAllowedBelow() : isEnabled;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
    itemView.setClickable(isEnabled);
    itemView.setEnabled(isEnabled);
    itemView.setFocusable(isEnabled);
  }

  public void setItem(IItem item) {
    this.item = item;
  }

  public IItem getItem() {
    return item;
  }
}
