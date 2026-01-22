/*
 * Copyright (C) 2016 The Android Open Source Project
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
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.android.setupdesign.R;
import java.util.ArrayList;

/**
 * A list item with one or more buttons, declared as {@link
 * com.google.android.setupdesign.items.ButtonItem}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * &lt;ButtonBarItem&gt;
 *
 *     &lt;ButtonItem
 *         android:id="@+id/skip_button"
 *         android:text="@string/skip_button_label /&gt;
 *
 *     &lt;ButtonItem
 *         android:id="@+id/next_button"
 *         android:text="@string/next_button_label
 *         android:theme="@style/SudButtonItem.Colored" /&gt;
 *
 * &lt;/ButtonBarItem&gt;
 * }</pre>
 */
public class ButtonBarItem extends AbstractItem implements ItemInflater.ItemParent {

  private final ArrayList<ButtonItem> buttons = new ArrayList<>();
  private boolean visible = true;

  public ButtonBarItem() {
    super();
  }

  public ButtonBarItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public int getCount() {
    return isVisible() ? 1 : 0;
  }

  @Override
  public boolean isEnabled() {
    // The children buttons are enabled and clickable, but the item itself is not
    return false;
  }

  @Override
  public int getLayoutResource() {
    return R.layout.sud_items_button_bar;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return visible;
  }

  @Override
  public int getViewId() {
    return getId();
  }

  @Override
  public void onBindView(View view) {
    // Note: The efficiency could be improved by trying to recycle the buttons created by
    // ButtonItem
    final LinearLayout layout = (LinearLayout) view;
    layout.removeAllViews();

    for (ButtonItem buttonItem : buttons) {
      Button button = buttonItem.createButton(layout);
      layout.addView(button);
    }

    view.setId(getViewId());
  }

  @Override
  public void addChild(ItemHierarchy child) {
    if (child instanceof ButtonItem) {
      buttons.add((ButtonItem) child);
    } else {
      throw new UnsupportedOperationException("Cannot add non-button item to Button Bar");
    }
  }

  @Override
  public ItemHierarchy findItemById(int id) {
    if (getId() == id) {
      return this;
    }
    for (ButtonItem button : buttons) {
      final ItemHierarchy item = button.findItemById(id);
      if (item != null) {
        return item;
      }
    }
    return null;
  }
}
