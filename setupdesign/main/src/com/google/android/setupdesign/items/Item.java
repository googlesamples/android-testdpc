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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.ItemStyler;
import com.google.android.setupdesign.util.LayoutStyler;

/**
 * Definition of an item in an {@link ItemHierarchy}. An item is usually defined in XML and inflated
 * using {@link ItemInflater}.
 */
public class Item extends AbstractItem {

  private boolean enabled = true;
  @Nullable private Drawable icon;
  private int layoutRes;
  @Nullable private CharSequence summary;
  @Nullable private CharSequence title;
  @Nullable private CharSequence contentDescription;
  private boolean visible = true;
  @ColorInt private int iconTint = Color.TRANSPARENT;
  private int iconGravity = Gravity.CENTER_VERTICAL;

  public Item() {
    super();
    layoutRes = getDefaultLayoutResource();
  }

  public Item(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudItem);
    enabled = a.getBoolean(R.styleable.SudItem_android_enabled, true);
    icon = a.getDrawable(R.styleable.SudItem_android_icon);
    title = a.getText(R.styleable.SudItem_android_title);
    summary = a.getText(R.styleable.SudItem_android_summary);
    contentDescription = a.getText(R.styleable.SudItem_android_contentDescription);
    layoutRes = a.getResourceId(R.styleable.SudItem_android_layout, getDefaultLayoutResource());
    visible = a.getBoolean(R.styleable.SudItem_android_visible, true);
    iconTint = a.getColor(R.styleable.SudItem_sudIconTint, Color.TRANSPARENT);
    iconGravity = a.getInt(R.styleable.SudItem_sudIconGravity, Gravity.CENTER_VERTICAL);

    a.recycle();
  }

  protected int getDefaultLayoutResource() {
    return R.layout.sud_items_default;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    notifyItemChanged();
  }

  @Override
  public int getCount() {
    return isVisible() ? 1 : 0;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public void setIcon(@Nullable Drawable icon) {
    this.icon = icon;
    notifyItemChanged();
  }

  @Nullable
  public Drawable getIcon() {
    return icon;
  }

  public void setIconTint(@ColorInt int iconTint) {
    this.iconTint = iconTint;
  }

  @ColorInt
  public int getIconTint() {
    return iconTint;
  }

  public void setIconGravity(int iconGravity) {
    this.iconGravity = iconGravity;
  }

  public int getIconGravity() {
    return iconGravity;
  }

  public void setLayoutResource(int layoutResource) {
    layoutRes = layoutResource;
    notifyItemChanged();
  }

  @Override
  public int getLayoutResource() {
    return layoutRes;
  }

  public void setSummary(@Nullable CharSequence summary) {
    this.summary = summary;
    notifyItemChanged();
  }

  @Nullable
  public CharSequence getSummary() {
    return summary;
  }

  public void setTitle(@Nullable CharSequence title) {
    this.title = title;
    notifyItemChanged();
  }

  @Nullable
  public CharSequence getTitle() {
    return title;
  }

  @Nullable
  public CharSequence getContentDescription() {
    return contentDescription;
  }

  public void setContentDescription(@Nullable CharSequence contentDescription) {
    this.contentDescription = contentDescription;
    notifyItemChanged();
  }

  public void setVisible(boolean visible) {
    if (this.visible == visible) {
      return;
    }
    this.visible = visible;
    if (!visible) {
      notifyItemRangeRemoved(0, 1);
    } else {
      notifyItemRangeInserted(0, 1);
    }
  }

  public boolean isVisible() {
    return visible;
  }

  private boolean hasSummary(CharSequence summary) {
    return summary != null && summary.length() > 0;
  }

  @Override
  public int getViewId() {
    return getId();
  }

  @Override
  public void onBindView(View view) {
    TextView label = (TextView) view.findViewById(R.id.sud_items_title);
    label.setText(getTitle());

    TextView summaryView = (TextView) view.findViewById(R.id.sud_items_summary);
    CharSequence summary = getSummary();
    if (hasSummary(summary)) {
      summaryView.setText(summary);
      summaryView.setVisibility(View.VISIBLE);
    } else {
      summaryView.setVisibility(View.GONE);
    }

    view.setContentDescription(getContentDescription());

    final View iconContainer = view.findViewById(R.id.sud_items_icon_container);
    final Drawable icon = getIcon();
    if (icon != null) {
      final ImageView iconView = (ImageView) view.findViewById(R.id.sud_items_icon);
      // Set the image drawable to null before setting the state and level to avoid affecting
      // any recycled drawable in the ImageView
      iconView.setImageDrawable(null);
      onMergeIconStateAndLevels(iconView, icon);
      iconView.setImageDrawable(icon);
      if (iconTint != Color.TRANSPARENT) {
        iconView.setColorFilter(iconTint);
      } else {
        iconView.clearColorFilter();
      }
      LayoutParams layoutParams = iconContainer.getLayoutParams();
      if (layoutParams instanceof LinearLayout.LayoutParams) {
        ((LinearLayout.LayoutParams) layoutParams).gravity = iconGravity;
      }
      iconContainer.setVisibility(View.VISIBLE);
    } else {
      iconContainer.setVisibility(View.GONE);
    }

    view.setId(getViewId());

    // ExpandableSwitchItem uses its child view to apply the style SudItemContainer. It is not
    // possible to directly adjust the padding start/end of the item's layout here. It needs to
    // get its child view to adjust it first, so skip the Layout padding adjustment.
    // If the item view is a header layout, it doesn't need to adjust the layout padding start/end
    // here. It will be adjusted by HeaderMixin.
    // TODO: Add partner resource enable check
    if (!(this instanceof ExpandableSwitchItem)
        && view.getId() != R.id.sud_layout_header
        && !(PartnerConfigHelper.isGlifExpressiveEnabled(view.getContext()))) {
      LayoutStyler.applyPartnerCustomizationLayoutPaddingStyle(view);
    }
    ItemStyler.applyPartnerCustomizationItemStyle(view);
  }

  /**
   * Copies state and level information from {@link #getIcon()} to the currently bound view's
   * ImageView. Subclasses can override this method to change whats being copied from the icon to
   * the ImageView.
   */
  protected void onMergeIconStateAndLevels(ImageView iconView, Drawable icon) {
    iconView.setImageState(icon.getState(), false /* merge */);
    iconView.setImageLevel(icon.getLevel());
  }
}
