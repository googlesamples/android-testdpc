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
import androidx.appcompat.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import com.google.android.setupdesign.R;

/**
 * An item that is displayed with a switch, with methods to manipulate and listen to the checked
 * state of the switch. Note that by default, only click on the switch will change the on-off state.
 * To change the switch state when tapping on the text, use the click handlers of list view or
 * RecyclerItemAdapter with {@link #toggle(View)}.
 */
public class SwitchItem extends Item implements CompoundButton.OnCheckedChangeListener {

  /** Listener for check state changes of this switch item. */
  public interface OnCheckedChangeListener {

    /**
     * Callback when checked state of a {@link SwitchItem} is changed.
     *
     * @see #setOnCheckedChangeListener(OnCheckedChangeListener)
     */
    void onCheckedChange(SwitchItem item, boolean isChecked);
  }

  private boolean checked = false;
  private OnCheckedChangeListener listener;

  /** Creates a default switch item. */
  public SwitchItem() {
    super();
  }

  /**
   * Creates a switch item. This constructor is used for inflation from XML.
   *
   * @param context The context which this item is inflated in.
   * @param attrs The XML attributes defined on the item.
   */
  public SwitchItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudSwitchItem);
    checked = a.getBoolean(R.styleable.SudSwitchItem_android_checked, false);
    a.recycle();
  }

  /** Sets whether this item should be checked. */
  public void setChecked(boolean checked) {
    if (this.checked != checked) {
      this.checked = checked;
      notifyItemChanged();
      if (listener != null) {
        listener.onCheckedChange(this, checked);
      }
    }
  }

  /** @return True if this switch item is currently checked. */
  public boolean isChecked() {
    return checked;
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.sud_items_switch;
  }

  /**
   * Toggle the checked state of the switch, without invalidating the entire item.
   *
   * @param view The root view of this item, typically from the argument of onItemClick.
   */
  public void toggle(View view) {
    checked = !checked;
    final SwitchCompat switchView = (SwitchCompat) view.findViewById(R.id.sud_items_switch);
    switchView.setChecked(checked);
  }

  @Override
  public void onBindView(View view) {
    super.onBindView(view);
    final SwitchCompat switchView = (SwitchCompat) view.findViewById(R.id.sud_items_switch);
    switchView.setOnCheckedChangeListener(null);
    switchView.setChecked(checked);
    switchView.setOnCheckedChangeListener(this);
    switchView.setEnabled(isEnabled());
  }

  /**
   * Sets a listener to listen for changes in checked state. This listener is invoked in both user
   * toggling the switch and calls to {@link #setChecked(boolean)}.
   */
  public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    this.listener = listener;
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    checked = isChecked;
    if (listener != null) {
      listener.onCheckedChange(this, isChecked);
    }
  }
}
