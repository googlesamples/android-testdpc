/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.setupcompat.partnerconfig;

// TODO: optimize the enum
/** Resources that can be customized by partner overlay APK. */
public enum PartnerConfig {

  // Status bar background color or illustration.
  CONFIG_STATUS_BAR_BACKGROUND(PartnerConfigKey.KEY_STATUS_BAR_BACKGROUND, ResourceType.DRAWABLE),

  // The same as "WindowLightStatusBar". If set true, the status bar icons will be drawn such
  // that it is compatible with a light status bar background
  CONFIG_LIGHT_STATUS_BAR(PartnerConfigKey.KEY_LIGHT_STATUS_BAR, ResourceType.BOOL),

  // Navigation bar background color
  CONFIG_NAVIGATION_BAR_BG_COLOR(PartnerConfigKey.KEY_NAVIGATION_BAR_BG_COLOR, ResourceType.COLOR),

  // Navigation bar divider color
  CONFIG_NAVIGATION_BAR_DIVIDER_COLOR(
      PartnerConfigKey.KEY_NAVIGATION_BAR_DIVIDER_COLOR, ResourceType.COLOR),

  // Background color of the footer bar.
  CONFIG_FOOTER_BAR_BG_COLOR(PartnerConfigKey.KEY_FOOTER_BAR_BG_COLOR, ResourceType.COLOR),

  // The min height of the footer buttons
  CONFIG_FOOTER_BAR_MIN_HEIGHT(PartnerConfigKey.KEY_FOOTER_BAR_MIN_HEIGHT, ResourceType.DIMENSION),

  // The padding start of the footer bar
  CONFIG_FOOTER_BAR_PADDING_START(
      PartnerConfigKey.KEY_FOOTER_BAR_PADDING_START, ResourceType.DIMENSION),

  // The padding end of the footer bar
  CONFIG_FOOTER_BAR_PADDING_END(
      PartnerConfigKey.KEY_FOOTER_BAR_PADDING_END, ResourceType.DIMENSION),

  // The same as "windowLightNavigationBar". If set true, the navigation bar icons will be drawn
  // such that it is compatible with a light navigation bar background.
  CONFIG_LIGHT_NAVIGATION_BAR(PartnerConfigKey.KEY_LIGHT_NAVIGATION_BAR, ResourceType.BOOL),

  // The font face used in footer buttons. This must be a string reference to a font that is
  // available in the system. Font references (@font or @xml) are not allowed.
  CONFIG_FOOTER_BUTTON_FONT_FAMILY(
      PartnerConfigKey.KEY_FOOTER_BUTTON_FONT_FAMILY, ResourceType.STRING),

  // The font weight used in footer buttons.
  CONFIG_FOOTER_BUTTON_FONT_WEIGHT(
      PartnerConfigKey.KEY_FOOTER_BUTTON_FONT_WEIGHT, ResourceType.INTEGER),

  // The icon for "add another" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_ADD_ANOTHER(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_ADD_ANOTHER, ResourceType.DRAWABLE),

  // The icon for "cancel" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_CANCEL(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_CANCEL, ResourceType.DRAWABLE),

  // The icon for "clear" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_CLEAR(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_CLEAR, ResourceType.DRAWABLE),

  // The icon for "done" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_DONE(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_DONE, ResourceType.DRAWABLE),

  // The icon for "next" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_NEXT(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_NEXT, ResourceType.DRAWABLE),

  // The icon for "opt-in" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_OPT_IN(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_OPT_IN, ResourceType.DRAWABLE),

  // The icon for "skip" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_SKIP(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_SKIP, ResourceType.DRAWABLE),

  // The icon for "stop" action. Can be "@null" for no icon.
  CONFIG_FOOTER_BUTTON_ICON_STOP(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_STOP, ResourceType.DRAWABLE),

  // Top padding of the footer buttons
  CONFIG_FOOTER_BUTTON_PADDING_TOP(
      PartnerConfigKey.KEY_FOOTER_BUTTON_PADDING_TOP, ResourceType.DIMENSION),

  // Bottom padding of the footer buttons
  CONFIG_FOOTER_BUTTON_PADDING_BOTTOM(
      PartnerConfigKey.KEY_FOOTER_BUTTON_PADDING_BOTTOM, ResourceType.DIMENSION),

  // Corner radius of the footer buttons
  CONFIG_FOOTER_BUTTON_RADIUS(PartnerConfigKey.KEY_FOOTER_BUTTON_RADIUS, ResourceType.DIMENSION),

  // Ripple color alpha the footer buttons
  CONFIG_FOOTER_BUTTON_RIPPLE_COLOR_ALPHA(
      PartnerConfigKey.KEY_FOOTER_BUTTON_RIPPLE_ALPHA, ResourceType.FRACTION),

  // Text size of the footer buttons
  CONFIG_FOOTER_BUTTON_TEXT_SIZE(
      PartnerConfigKey.KEY_FOOTER_BUTTON_TEXT_SIZE, ResourceType.DIMENSION),

  // The text style of footer buttons {0 = NORMAL}, {1 = BOLD}, {2 = ITALIC}, {3 = BOLD_ITALIC}
  CONFIG_FOOTER_BUTTON_TEXT_STYLE(
      PartnerConfigKey.KEY_FOOTER_BUTTON_TEXT_STYLE, ResourceType.INTEGER),

  // The min height of the footer buttons
  CONFIG_FOOTER_BUTTON_MIN_HEIGHT(
      PartnerConfigKey.KEY_FOOTER_BUTTON_MIN_HEIGHT, ResourceType.DIMENSION),

  // Make the footer buttons all aligned the end
  CONFIG_FOOTER_BUTTON_ALIGNED_END(
      PartnerConfigKey.KEY_FOOTER_BUTTON_ALIGNED_END, ResourceType.BOOL),

  // Disabled background alpha of the footer buttons
  CONFIG_FOOTER_BUTTON_DISABLED_ALPHA(
      PartnerConfigKey.KEY_FOOTER_BUTTON_DISABLED_ALPHA, ResourceType.FRACTION),

  // Disabled background color of the footer buttons
  CONFIG_FOOTER_BUTTON_DISABLED_BG_COLOR(
      PartnerConfigKey.KEY_FOOTER_BUTTON_DISABLED_BG_COLOR, ResourceType.COLOR),

  // Disabled text color of the primary footer button
  CONFIG_FOOTER_PRIMARY_BUTTON_DISABLED_TEXT_COLOR(
      PartnerConfigKey.KEY_PRIMARY_BUTTON_DISABLED_TEXT_COLOR, ResourceType.COLOR),

  // Disabled text color of the secondary footer button
  CONFIG_FOOTER_SECONDARY_BUTTON_DISABLED_TEXT_COLOR(
      PartnerConfigKey.KEY_SECONDARY_BUTTON_DISABLED_TEXT_COLOR, ResourceType.COLOR),

  // Background color of the primary footer button
  CONFIG_FOOTER_PRIMARY_BUTTON_BG_COLOR(
      PartnerConfigKey.KEY_FOOTER_PRIMARY_BUTTON_BG_COLOR, ResourceType.COLOR),

  // Text color of the primary footer button
  CONFIG_FOOTER_PRIMARY_BUTTON_TEXT_COLOR(
      PartnerConfigKey.KEY_FOOTER_PRIMARY_BUTTON_TEXT_COLOR, ResourceType.COLOR),

  // Margin start of the primary footer button
  CONFIG_FOOTER_PRIMARY_BUTTON_MARGIN_START(
      PartnerConfigKey.KEY_FOOTER_PRIMARY_BUTTON_MARGIN_START, ResourceType.DIMENSION),

  // Background color of the secondary footer button
  CONFIG_FOOTER_SECONDARY_BUTTON_BG_COLOR(
      PartnerConfigKey.KEY_FOOTER_SECONDARY_BUTTON_BG_COLOR, ResourceType.COLOR),

  // Text color of the secondary footer button
  CONFIG_FOOTER_SECONDARY_BUTTON_TEXT_COLOR(
      PartnerConfigKey.KEY_FOOTER_SECONDARY_BUTTON_TEXT_COLOR, ResourceType.COLOR),

  // Margin start of the secondary footer button
  CONFIG_FOOTER_SECONDARY_BUTTON_MARGIN_START(
      PartnerConfigKey.KEY_FOOTER_SECONDARY_BUTTON_MARGIN_START, ResourceType.DIMENSION),

  // Background color of layout
  CONFIG_LAYOUT_BACKGROUND_COLOR(PartnerConfigKey.KEY_LAYOUT_BACKGROUND_COLOR, ResourceType.COLOR),

  // Margin start of the layout
  CONFIG_LAYOUT_MARGIN_START(PartnerConfigKey.KEY_LAYOUT_MARGIN_START, ResourceType.DIMENSION),

  // Margin end of the layout
  CONFIG_LAYOUT_MARGIN_END(PartnerConfigKey.KEY_LAYOUT_MARGIN_END, ResourceType.DIMENSION),

  // Middle horizontal spacing of the landscape layout
  CONFIG_LAND_MIDDLE_HORIZONTAL_SPACING(
      PartnerConfigKey.KEY_LAND_MIDDLE_HORIZONTAL_SPACING, ResourceType.DIMENSION),

  // Text color of the header
  CONFIG_HEADER_TEXT_COLOR(PartnerConfigKey.KEY_HEADER_TEXT_COLOR, ResourceType.COLOR),

  // Text size of the header
  CONFIG_HEADER_TEXT_SIZE(PartnerConfigKey.KEY_HEADER_TEXT_SIZE, ResourceType.DIMENSION),

  // Font family of the header
  CONFIG_HEADER_FONT_FAMILY(PartnerConfigKey.KEY_HEADER_FONT_FAMILY, ResourceType.STRING),

  // Font weight of the header
  CONFIG_HEADER_FONT_WEIGHT(PartnerConfigKey.KEY_HEADER_FONT_WEIGHT, ResourceType.INTEGER),

  // Margin top of the header text
  CONFIG_HEADER_TEXT_MARGIN_TOP(
      PartnerConfigKey.KEY_HEADER_TEXT_MARGIN_TOP, ResourceType.DIMENSION),

  // Margin bottom of the header text
  CONFIG_HEADER_TEXT_MARGIN_BOTTOM(
      PartnerConfigKey.KEY_HEADER_TEXT_MARGIN_BOTTOM, ResourceType.DIMENSION),

  // Gravity of the header, icon and description
  CONFIG_LAYOUT_GRAVITY(PartnerConfigKey.KEY_LAYOUT_GRAVITY, ResourceType.STRING),

  // Margin top of the icon
  CONFIG_ICON_MARGIN_TOP(PartnerConfigKey.KEY_ICON_MARGIN_TOP, ResourceType.DIMENSION),

  // Size of the icon
  CONFIG_ICON_SIZE(PartnerConfigKey.KEY_ICON_SIZE, ResourceType.DIMENSION),

  // The max width of the illustration
  CONFIG_ILLUSTRATION_MAX_WIDTH(
      PartnerConfigKey.KEY_ILLUSTRATION_MAX_WIDTH, ResourceType.DIMENSION),

  // The max height of the illustration
  CONFIG_ILLUSTRATION_MAX_HEIGHT(
      PartnerConfigKey.KEY_ILLUSTRATION_MAX_HEIGHT, ResourceType.DIMENSION),

  // Background color of the header area
  CONFIG_HEADER_AREA_BACKGROUND_COLOR(
      PartnerConfigKey.KEY_HEADER_AREA_BACKGROUND_COLOR, ResourceType.COLOR),

  // Margin bottom of the header container
  CONFIG_HEADER_CONTAINER_MARGIN_BOTTOM(
      PartnerConfigKey.KEY_HEADER_CONTAINER_MARGIN_BOTTOM, ResourceType.DIMENSION),

  // Auto text size enabled status
  CONFIG_HEADER_AUTO_SIZE_ENABLED(PartnerConfigKey.KEY_HEADER_AUTO_SIZE_ENABLED, ResourceType.BOOL),

  // Max text size of header when auto size enabled. Ignored if auto size is false.
  CONFIG_HEADER_AUTO_SIZE_MAX_TEXT_SIZE(
      PartnerConfigKey.KEY_HEADER_AUTO_SIZE_MAX_TEXT_SIZE, ResourceType.DIMENSION),

  // Min text size of header when auto size enabled. Ignored if auto size is false.
  CONFIG_HEADER_AUTO_SIZE_MIN_TEXT_SIZE(
      PartnerConfigKey.KEY_HEADER_AUTO_SIZE_MIN_TEXT_SIZE, ResourceType.DIMENSION),

  // The max lines of the max text size when auto size enabled. Ignored if auto size is false.
  CONFIG_HEADER_AUTO_SIZE_MAX_LINE_OF_MAX_SIZE(
      PartnerConfigKey.KEY_HEADER_AUTO_SIZE_MAX_LINE_OF_MAX_SIZE, ResourceType.INTEGER),

  // Extra line spacing of header when auto size enabled. Ignored if auto size is false.
  CONFIG_HEADER_AUTO_SIZE_LINE_SPACING_EXTRA(
      PartnerConfigKey.KEY_HEADER_AUTO_SIZE_LINE_SPACING_EXTRA, ResourceType.DIMENSION),

  // Text size of the description
  CONFIG_DESCRIPTION_TEXT_SIZE(PartnerConfigKey.KEY_DESCRIPTION_TEXT_SIZE, ResourceType.DIMENSION),

  // Text color of the description
  CONFIG_DESCRIPTION_TEXT_COLOR(PartnerConfigKey.KEY_DESCRIPTION_TEXT_COLOR, ResourceType.COLOR),

  // Link text color of the description
  CONFIG_DESCRIPTION_LINK_TEXT_COLOR(
      PartnerConfigKey.KEY_DESCRIPTION_LINK_TEXT_COLOR, ResourceType.COLOR),

  // Font family of the description
  CONFIG_DESCRIPTION_FONT_FAMILY(PartnerConfigKey.KEY_DESCRIPTION_FONT_FAMILY, ResourceType.STRING),

  // Font weight of the description
  CONFIG_DESCRIPTION_FONT_WEIGHT(
      PartnerConfigKey.KEY_DESCRIPTION_FONT_WEIGHT, ResourceType.INTEGER),

  // Font family of the link text
  CONFIG_DESCRIPTION_LINK_FONT_FAMILY(
      PartnerConfigKey.KEY_DESCRIPTION_LINK_FONT_FAMILY, ResourceType.STRING),

  // Margin top of the description text
  CONFIG_DESCRIPTION_TEXT_MARGIN_TOP(
      PartnerConfigKey.KEY_DESCRIPTION_TEXT_MARGIN_TOP, ResourceType.DIMENSION),

  // Margin bottom of the description text
  CONFIG_DESCRIPTION_TEXT_MARGIN_BOTTOM(
      PartnerConfigKey.KEY_DESCRIPTION_TEXT_MARGIN_BOTTOM, ResourceType.DIMENSION),

  // Font size of the account name
  CONFIG_ACCOUNT_NAME_TEXT_SIZE(
      PartnerConfigKey.KEY_ACCOUNT_NAME_TEXT_SIZE, ResourceType.DIMENSION),

  // Font family of the account name
  CONFIG_ACCOUNT_NAME_FONT_FAMILY(
      PartnerConfigKey.KEY_ACCOUNT_NAME_FONT_FAMILY, ResourceType.STRING),

  // Margin end of the account avatar
  CONFIG_ACCOUNT_AVATAR_MARGIN_END(
      PartnerConfigKey.KEY_ACCOUNT_AVATAR_MARGIN_END, ResourceType.DIMENSION),

  // Size of account avatar
  CONFIG_ACCOUNT_AVATAR_SIZE(PartnerConfigKey.KEY_ACCOUNT_AVATAR_MAX_SIZE, ResourceType.DIMENSION),

  // Text size of the body content text
  CONFIG_CONTENT_TEXT_SIZE(PartnerConfigKey.KEY_CONTENT_TEXT_SIZE, ResourceType.DIMENSION),

  // Text color of the body content text
  CONFIG_CONTENT_TEXT_COLOR(PartnerConfigKey.KEY_CONTENT_TEXT_COLOR, ResourceType.COLOR),

  // Link text color of the body content text
  CONFIG_CONTENT_LINK_TEXT_COLOR(PartnerConfigKey.KEY_CONTENT_LINK_TEXT_COLOR, ResourceType.COLOR),

  // Font family of the body content text
  CONFIG_CONTENT_FONT_FAMILY(PartnerConfigKey.KEY_CONTENT_FONT_FAMILY, ResourceType.STRING),

  // Gravity of the body content text
  CONFIG_CONTENT_LAYOUT_GRAVITY(PartnerConfigKey.KEY_CONTENT_LAYOUT_GRAVITY, ResourceType.STRING),

  // The padding top of the content
  CONFIG_CONTENT_PADDING_TOP(PartnerConfigKey.KEY_CONTENT_PADDING_TOP, ResourceType.DIMENSION),

  // The text size of the content info.
  CONFIG_CONTENT_INFO_TEXT_SIZE(
      PartnerConfigKey.KEY_CONTENT_INFO_TEXT_SIZE, ResourceType.DIMENSION),

  // The font family of the content info.
  CONFIG_CONTENT_INFO_FONT_FAMILY(
      PartnerConfigKey.KEY_CONTENT_INFO_FONT_FAMILY, ResourceType.STRING),

  // The text line spacing extra of the content info.
  CONFIG_CONTENT_INFO_LINE_SPACING_EXTRA(
      PartnerConfigKey.KEY_CONTENT_INFO_LINE_SPACING_EXTRA, ResourceType.DIMENSION),

  // The icon size of the content info.
  CONFIG_CONTENT_INFO_ICON_SIZE(
      PartnerConfigKey.KEY_CONTENT_INFO_ICON_SIZE, ResourceType.DIMENSION),

  // The icon margin end of the content info.
  CONFIG_CONTENT_INFO_ICON_MARGIN_END(
      PartnerConfigKey.KEY_CONTENT_INFO_ICON_MARGIN_END, ResourceType.DIMENSION),

  // The padding top of the content info.
  CONFIG_CONTENT_INFO_PADDING_TOP(
      PartnerConfigKey.KEY_CONTENT_INFO_PADDING_TOP, ResourceType.DIMENSION),

  // The padding bottom of the content info.
  CONFIG_CONTENT_INFO_PADDING_BOTTOM(
      PartnerConfigKey.KEY_CONTENT_INFO_PADDING_BOTTOM, ResourceType.DIMENSION),

  // The title text size of list items.
  CONFIG_ITEMS_TITLE_TEXT_SIZE(PartnerConfigKey.KEY_ITEMS_TITLE_TEXT_SIZE, ResourceType.DIMENSION),

  // The summary text size of list items.
  CONFIG_ITEMS_SUMMARY_TEXT_SIZE(
      PartnerConfigKey.KEY_ITEMS_SUMMARY_TEXT_SIZE, ResourceType.DIMENSION),

  // The summary margin top of list items.
  CONFIG_ITEMS_SUMMARY_MARGIN_TOP(
      PartnerConfigKey.KEY_ITEMS_SUMMARY_MARGIN_TOP, ResourceType.DIMENSION),

  // The title font family of list items.
  CONFIG_ITEMS_TITLE_FONT_FAMILY(PartnerConfigKey.KEY_ITEMS_TITLE_FONT_FAMILY, ResourceType.STRING),

  // The summary font family of list items.
  CONFIG_ITEMS_SUMMARY_FONT_FAMILY(
      PartnerConfigKey.KEY_ITEMS_SUMMARY_FONT_FAMILY, ResourceType.STRING),

  // The padding top of list items.
  CONFIG_ITEMS_PADDING_TOP(PartnerConfigKey.KEY_ITEMS_PADDING_TOP, ResourceType.DIMENSION),

  // The padding bottom of list items.
  CONFIG_ITEMS_PADDING_BOTTOM(PartnerConfigKey.KEY_ITEMS_PADDING_BOTTOM, ResourceType.DIMENSION),

  // The corner radius of list items group.
  CONFIG_ITEMS_GROUP_CORNER_RADIUS(
      PartnerConfigKey.KEY_ITEMS_GROUP_CORNER_RADIUS, ResourceType.DIMENSION),

  // The minimum height of list items.
  CONFIG_ITEMS_MIN_HEIGHT(PartnerConfigKey.KEY_ITEMS_MIN_HEIGHT, ResourceType.DIMENSION),

  // The divider of list items are showing on the pages.
  CONFIG_ITEMS_DIVIDER_SHOWN(PartnerConfigKey.KEY_ITEMS_DIVIDER_SHOWN, ResourceType.BOOL),

  // The intrinsic width of the card view for foldable/tablet.
  CONFIG_CARD_VIEW_INTRINSIC_WIDTH(
      PartnerConfigKey.KEY_CARD_VIEW_INTRINSIC_WIDTH, ResourceType.DIMENSION),

  // The intrinsic height of the card view for foldable/tablet.
  CONFIG_CARD_VIEW_INTRINSIC_HEIGHT(
      PartnerConfigKey.KEY_CARD_VIEW_INTRINSIC_HEIGHT, ResourceType.DIMENSION),

  // The animation of loading screen used in those activities which is non of below type.
  CONFIG_PROGRESS_ILLUSTRATION_DEFAULT(
      PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_DEFAULT, ResourceType.ILLUSTRATION),

  // The animation of loading screen used in those activity which is processing account info or
  // related functions.
  // For example:com.google.android.setupwizard.LOAD_ADD_ACCOUNT_INTENT
  CONFIG_PROGRESS_ILLUSTRATION_ACCOUNT(
      PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_ACCOUNT, ResourceType.ILLUSTRATION),

  // The animation of loading screen used in those activity which is processing data connection.
  // For example:com.android.setupwizard.CAPTIVE_PORTAL
  CONFIG_PROGRESS_ILLUSTRATION_CONNECTION(
      PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_CONNECTION, ResourceType.ILLUSTRATION),

  // The animation of loading screen used in those activities which is updating device.
  // For example:com.google.android.setupwizard.COMPAT_EARLY_UPDATE
  CONFIG_PROGRESS_ILLUSTRATION_UPDATE(
      PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_UPDATE, ResourceType.ILLUSTRATION),

  // The animation of loading screen used in those activities which is finishing setup.
  // For example:com.google.android.setupwizard.FINAL_HOLD
  CONFIG_PROGRESS_ILLUSTRATION_FINAL_HOLD(
      PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_FINAL_HOLD, ResourceType.ILLUSTRATION),

  // The animation of loading screen to define how long showing on the pages.
  CONFIG_PROGRESS_ILLUSTRATION_DISPLAY_MINIMUM_MS(
      PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_DISPLAY_MINIMUM_MS, ResourceType.INTEGER),

  // The animation for S+ devices used in those screens waiting for non of below type.
  CONFIG_LOADING_LOTTIE_DEFAULT(
      PartnerConfigKey.KEY_LOADING_LOTTIE_DEFAULT, ResourceType.ILLUSTRATION),

  // The animation for S+ devices used in those screens which is processing account info or related
  // functions.
  // For example:com.google.android.setupwizard.LOAD_ADD_ACCOUNT_INTENT
  CONFIG_LOADING_LOTTIE_ACCOUNT(
      PartnerConfigKey.KEY_LOADING_LOTTIE_ACCOUNT, ResourceType.ILLUSTRATION),

  // The animation for S+ devices used in those screens which is processing data connection.
  // For example:com.android.setupwizard.CAPTIVE_PORTAL
  CONFIG_LOADING_LOTTIE_CONNECTION(
      PartnerConfigKey.KEY_LOADING_LOTTIE_CONNECTION, ResourceType.ILLUSTRATION),

  // The animation for S+ devices used in those screens which is updating devices.
  // For example:com.google.android.setupwizard.COMPAT_EARLY_UPDATE
  CONFIG_LOADING_LOTTIE_UPDATE(
      PartnerConfigKey.KEY_LOADING_LOTTIE_UPDATE, ResourceType.ILLUSTRATION),

  // The animation for S+ devices used in those screens which is updating devices.
  // For example:com.google.android.setupwizard.COMPAT_EARLY_UPDATE
  CONFIG_LOADING_LOTTIE_FINAL_HOLD(
      PartnerConfigKey.KEY_LOADING_LOTTIE_FINAL_HOLD, ResourceType.ILLUSTRATION),

  // The transition type to decide the transition between activities or fragments.
  CONFIG_TRANSITION_TYPE(PartnerConfigKey.KEY_TRANSITION_TYPE, ResourceType.INTEGER),

  // The list of keypath and color map, applied to default animation when light theme.
  CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_DEFAULT(
      PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_DEFAULT, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to account animation when light theme.
  CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_ACCOUNT(
      PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_ACCOUNT, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to connection animation when light theme.
  CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_CONNECTION(
      PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_CONNECTION, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to update animation when light theme.
  CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_UPDATE(
      PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_UPDATE, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to update animation when light theme.
  CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_FINAL_HOLD(
      PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_FINAL_HOLD, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to default animation when dark theme.
  CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_DEFAULT(
      PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_DEFAULT, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to account animation when dark theme.
  CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_ACCOUNT(
      PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_ACCOUNT, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to connection animation when dark theme.
  CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_CONNECTION(
      PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_CONNECTION, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to update animation when dark theme.
  CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_UPDATE(
      PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_UPDATE, ResourceType.STRING_ARRAY),

  // The list of keypath and color map, applied to final hold animation when dark theme.
  CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_FINAL_HOLD(
      PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_FINAL_HOLD, ResourceType.STRING_ARRAY),

  // The padding top of the content frame of loading layout.
  CONFIG_LOADING_LAYOUT_PADDING_TOP(
      PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_TOP, ResourceType.DIMENSION),

  // The padding start of the content frame of loading layout.
  CONFIG_LOADING_LAYOUT_PADDING_START(
      PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_START, ResourceType.DIMENSION),

  // The padding end of the content frame of loading layout.
  CONFIG_LOADING_LAYOUT_PADDING_END(
      PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_END, ResourceType.DIMENSION),

  // The padding bottom of the content frame of loading layout.
  CONFIG_LOADING_LAYOUT_PADDING_BOTTOM(
      PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_BOTTOM, ResourceType.DIMENSION),

  // The height of the header of the loading layout.
  CONFIG_LOADING_LAYOUT_HEADER_HEIGHT(
      PartnerConfigKey.KEY_LOADING_LAYOUT_HEADER_HEIGHT, ResourceType.DIMENSION),

  // Use the fullscreen style lottie animation.
  CONFIG_LOADING_LAYOUT_FULL_SCREEN_ILLUSTRATION_ENABLED(
      PartnerConfigKey.KEY_LOADING_LAYOUT_FULL_SCREEN_ILLUSTRATION_ENABLED, ResourceType.BOOL),

  // Waiting for the animation finished before process to the next page/action.
  CONFIG_LOADING_LAYOUT_WAIT_FOR_ANIMATION_FINISHED(
      PartnerConfigKey.KEY_LOADING_LAYOUT_WAIT_FOR_ANIMATION_FINISHED, ResourceType.BOOL),

  // The margin top of progress bar.
  CONFIG_PROGRESS_BAR_MARGIN_TOP(
      PartnerConfigKey.KEY_PROGRESS_BAR_MARGIN_TOP, ResourceType.DIMENSION),

  // The margin bottom of progress bar.
  CONFIG_PROGRESS_BAR_MARGIN_BOTTOM(
      PartnerConfigKey.KEY_PROGRESS_BAR_MARGIN_BOTTOM, ResourceType.DIMENSION),

  // The adapt window width to be part of determining two pane style condition
  CONFIG_TWO_PANE_ADAPT_WINDOW_WIDTH(
      PartnerConfigKey.KEY_TWO_PANE_ADAPT_WINDOW_WIDTH, ResourceType.INTEGER);

  /** Resource type of the partner resources type. */
  public enum ResourceType {
    INTEGER,
    BOOL,
    COLOR,
    DRAWABLE,
    STRING,
    DIMENSION,
    FRACTION,
    ILLUSTRATION,
    STRING_ARRAY
  }

  private final String resourceName;
  private final ResourceType resourceType;

  public ResourceType getResourceType() {
    return resourceType;
  }

  public String getResourceName() {
    return resourceName;
  }

  PartnerConfig(@PartnerConfigKey String resourceName, ResourceType type) {
    this.resourceName = resourceName;
    this.resourceType = type;
  }
}
