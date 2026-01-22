/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.setupdesign.template;

import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.template.Mixin;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.util.HeaderAreaStyler;
import com.google.android.setupdesign.util.LayoutStyler;
import com.google.android.setupdesign.util.PartnerStyleHelper;
import com.google.errorprone.annotations.CheckReturnValue;

/** A {@link Mixin} for setting an account name and account avatar on the template layout. */
@CheckReturnValue // see go/why-crv
public class ProfileMixin implements Mixin {

  private final TemplateLayout templateLayout;
  private static final String TAG = "ProfileMixin";
  public static final int RIGHT = 5;
  public static final int CENTER = 17;
  public static final int LEFT = 3;

  /**
   * A {@link Mixin} for setting and getting the Account.
   *
   * @param layout The template layout that this Mixin is a part of
   * @param attrs XML attributes given to the layout
   * @param defStyleAttr The default style attribute as given to the constructor of the layout
   */
  public ProfileMixin(TemplateLayout layout, AttributeSet attrs, int defStyleAttr) {
    templateLayout = layout;
  }

  /**
   * Sets the Account name.
   *
   * @param accountName The text to be set as account name
   */
  public void setAccountName(CharSequence accountName) {
    final TextView accountView = getAccountNameView();
    final ImageView iconView = getAccountAvatarView();
    final LinearLayout container = getContainerView();
    if (accountView != null && accountName != null) {
      accountView.setText(accountName);
      container.setVisibility(View.VISIBLE);
      if (iconView != null && getAccountAvatar() == null) {
        iconView.setVisibility(View.GONE);
      }
    } else {
      Log.w(TAG, "Didn't get the account name");
    }
  }

  /**
   * Sets the icon on this layout.
   *
   * @param icon A drawable icon to set, or {@code null} to hide the icon
   */
  public void setAccountAvatar(Drawable icon) {
    final ImageView iconView = getAccountAvatarView();
    final LinearLayout container = getContainerView();
    if (iconView != null && icon != null) {
      iconView.setImageDrawable(icon);
      container.setVisibility(View.VISIBLE);
      iconView.setVisibility(View.VISIBLE);
    } else if (iconView != null) {
      iconView.setVisibility(View.GONE);
      Log.w(TAG, "Didn't get the account avatar");
    }
  }

  /**
   * Sets the icon on this layout.
   *
   * @param icon A drawable icon resource to set, or {@code null} to hide the icon
   */
  public void setAccountAvatar(@DrawableRes int icon) {
    final ImageView iconView = getAccountAvatarView();
    final LinearLayout container = getContainerView();
    if (iconView != null && icon != 0) {
      // Note: setImageResource on the ImageView is overridden in AppCompatImageView for
      // support lib users, which enables vector drawable compat to work on versions pre-L.
      iconView.setImageResource(icon);
      container.setVisibility(View.VISIBLE);
      iconView.setVisibility(View.VISIBLE);
    } else if (iconView != null) {
      iconView.setVisibility(View.GONE);
      Log.w(TAG, "Didn't get the account avatar");
    }
  }

  /**
   * Sets the account name icon on this layout.
   *
   * @param accountName The text to be set as account name
   * @param icon A drawable icon
   */
  public void setAccount(CharSequence accountName, Drawable icon) {
    setAccountName(accountName);
    setAccountAvatar(icon);
  }

  /**
   * Sets the account name icon on this layout.
   *
   * @param accountName The text to be set as account name
   * @param icon A drawable icon resource
   */
  public void setAccount(CharSequence accountName, @DrawableRes int icon) {
    setAccountName(accountName);
    setAccountAvatar(icon);
  }

  /**
   * Sets the visibility of the account. gone map to 8 invisible map to 4 visible map to 0
   *
   * @param visibility Set it visible or not
   */
  public void setVisibility(int visibility) {
    getContainerView().setVisibility(visibility);
  }

  /**
   * Makes account align to left, center or right.
   *
   * @param gravity the number or the gravity
   */
  public void setAccountAlignment(int gravity) {
    final LinearLayout container = getContainerView();
    if (gravity == RIGHT || gravity == CENTER || gravity == LEFT) {
      container.setGravity(gravity);
    } else {
      Log.w(TAG, "Unsupported alignment");
    }
  }

  /** Tries to apply the partner customization to the account photo. */
  public void tryApplyPartnerCustomizationStyle() {
    if (PartnerStyleHelper.shouldApplyPartnerResource(templateLayout)) {
      final ImageView iconView = getAccountAvatarView();
      final TextView accountView = getAccountNameView();
      final LinearLayout container = getContainerView();
      View iconAreaView = templateLayout.findManagedViewById(R.id.sud_layout_header);
      LayoutStyler.applyPartnerCustomizationExtraPaddingStyle(iconAreaView);
      HeaderAreaStyler.applyPartnerCustomizationAccountStyle(iconView, accountView, container);
    }
  }

  /** Returns the current account name. */
  public CharSequence getAccountName() {
    final TextView accountView = getAccountNameView();
    return accountView.getText();
  }

  /** Returns the current account avatar. */
  public Drawable getAccountAvatar() {
    final ImageView iconView = getAccountAvatarView();
    return iconView.getDrawable();
  }

  /** Returns the current account name text. */
  private TextView getAccountNameView() {
    return (TextView) templateLayout.findManagedViewById(R.id.sud_account_name);
  }

  /** Returns the current account icon image. */
  private ImageView getAccountAvatarView() {
    return (ImageView) templateLayout.findManagedViewById(R.id.sud_account_avatar);
  }

  private LinearLayout getContainerView() {
    return (LinearLayout) templateLayout.findManagedViewById(R.id.sud_layout_profile);
  }

  /** Returns the current account visibility. */
  public int getVisibility() {
    return getContainerView().getVisibility();
  }
}
