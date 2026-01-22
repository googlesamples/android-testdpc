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

import androidx.annotation.StringDef;
import androidx.annotation.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Resource names that can be customized by partner overlay APK. */
@Retention(RetentionPolicy.SOURCE)
@StringDef({
  PartnerConfigKey.KEY_STATUS_BAR_BACKGROUND,
  PartnerConfigKey.KEY_LIGHT_STATUS_BAR,
  PartnerConfigKey.KEY_NAVIGATION_BAR_BG_COLOR,
  PartnerConfigKey.KEY_LIGHT_NAVIGATION_BAR,
  PartnerConfigKey.KEY_NAVIGATION_BAR_DIVIDER_COLOR,
  PartnerConfigKey.KEY_FOOTER_BAR_BG_COLOR,
  PartnerConfigKey.KEY_FOOTER_BAR_MIN_HEIGHT,
  PartnerConfigKey.KEY_FOOTER_BAR_PADDING_START,
  PartnerConfigKey.KEY_FOOTER_BAR_PADDING_END,
  PartnerConfigKey.KEY_FOOTER_BUTTON_FONT_FAMILY,
  PartnerConfigKey.KEY_FOOTER_BUTTON_FONT_WEIGHT,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_ADD_ANOTHER,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_CANCEL,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_CLEAR,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_DONE,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_NEXT,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_OPT_IN,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_SKIP,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ICON_STOP,
  PartnerConfigKey.KEY_FOOTER_BUTTON_PADDING_TOP,
  PartnerConfigKey.KEY_FOOTER_BUTTON_PADDING_BOTTOM,
  PartnerConfigKey.KEY_FOOTER_BUTTON_RADIUS,
  PartnerConfigKey.KEY_FOOTER_BUTTON_RIPPLE_ALPHA,
  PartnerConfigKey.KEY_FOOTER_BUTTON_TEXT_SIZE,
  PartnerConfigKey.KEY_FOOTER_BUTTON_TEXT_STYLE,
  PartnerConfigKey.KEY_FOOTER_BUTTON_MIN_HEIGHT,
  PartnerConfigKey.KEY_FOOTER_BUTTON_ALIGNED_END,
  PartnerConfigKey.KEY_FOOTER_BUTTON_DISABLED_ALPHA,
  PartnerConfigKey.KEY_FOOTER_BUTTON_DISABLED_BG_COLOR,
  PartnerConfigKey.KEY_FOOTER_PRIMARY_BUTTON_BG_COLOR,
  PartnerConfigKey.KEY_FOOTER_PRIMARY_BUTTON_TEXT_COLOR,
  PartnerConfigKey.KEY_FOOTER_PRIMARY_BUTTON_MARGIN_START,
  PartnerConfigKey.KEY_PRIMARY_BUTTON_DISABLED_TEXT_COLOR,
  PartnerConfigKey.KEY_FOOTER_SECONDARY_BUTTON_BG_COLOR,
  PartnerConfigKey.KEY_FOOTER_SECONDARY_BUTTON_TEXT_COLOR,
  PartnerConfigKey.KEY_FOOTER_SECONDARY_BUTTON_MARGIN_START,
  PartnerConfigKey.KEY_SECONDARY_BUTTON_DISABLED_TEXT_COLOR,
  PartnerConfigKey.KEY_LAYOUT_BACKGROUND_COLOR,
  PartnerConfigKey.KEY_LAYOUT_MARGIN_START,
  PartnerConfigKey.KEY_LAYOUT_MARGIN_END,
  PartnerConfigKey.KEY_LAND_MIDDLE_HORIZONTAL_SPACING,
  PartnerConfigKey.KEY_HEADER_TEXT_SIZE,
  PartnerConfigKey.KEY_HEADER_TEXT_COLOR,
  PartnerConfigKey.KEY_HEADER_FONT_FAMILY,
  PartnerConfigKey.KEY_HEADER_FONT_WEIGHT,
  PartnerConfigKey.KEY_HEADER_AREA_BACKGROUND_COLOR,
  PartnerConfigKey.KEY_HEADER_TEXT_MARGIN_TOP,
  PartnerConfigKey.KEY_HEADER_TEXT_MARGIN_BOTTOM,
  PartnerConfigKey.KEY_HEADER_CONTAINER_MARGIN_BOTTOM,
  PartnerConfigKey.KEY_HEADER_AUTO_SIZE_ENABLED,
  PartnerConfigKey.KEY_HEADER_AUTO_SIZE_MAX_TEXT_SIZE,
  PartnerConfigKey.KEY_HEADER_AUTO_SIZE_MIN_TEXT_SIZE,
  PartnerConfigKey.KEY_HEADER_AUTO_SIZE_MAX_LINE_OF_MAX_SIZE,
  PartnerConfigKey.KEY_HEADER_AUTO_SIZE_LINE_SPACING_EXTRA,
  PartnerConfigKey.KEY_LAYOUT_GRAVITY,
  PartnerConfigKey.KEY_ICON_MARGIN_TOP,
  PartnerConfigKey.KEY_ICON_SIZE,
  PartnerConfigKey.KEY_ILLUSTRATION_MAX_WIDTH,
  PartnerConfigKey.KEY_ILLUSTRATION_MAX_HEIGHT,
  PartnerConfigKey.KEY_DESCRIPTION_TEXT_SIZE,
  PartnerConfigKey.KEY_DESCRIPTION_TEXT_COLOR,
  PartnerConfigKey.KEY_DESCRIPTION_LINK_TEXT_COLOR,
  PartnerConfigKey.KEY_DESCRIPTION_FONT_FAMILY,
  PartnerConfigKey.KEY_DESCRIPTION_FONT_WEIGHT,
  PartnerConfigKey.KEY_DESCRIPTION_LINK_FONT_FAMILY,
  PartnerConfigKey.KEY_DESCRIPTION_TEXT_MARGIN_TOP,
  PartnerConfigKey.KEY_DESCRIPTION_TEXT_MARGIN_BOTTOM,
  PartnerConfigKey.KEY_ACCOUNT_NAME_TEXT_SIZE,
  PartnerConfigKey.KEY_ACCOUNT_NAME_FONT_FAMILY,
  PartnerConfigKey.KEY_ACCOUNT_AVATAR_MARGIN_END,
  PartnerConfigKey.KEY_ACCOUNT_AVATAR_MAX_SIZE,
  PartnerConfigKey.KEY_CONTENT_TEXT_SIZE,
  PartnerConfigKey.KEY_CONTENT_TEXT_COLOR,
  PartnerConfigKey.KEY_CONTENT_LINK_TEXT_COLOR,
  PartnerConfigKey.KEY_CONTENT_FONT_FAMILY,
  PartnerConfigKey.KEY_CONTENT_LAYOUT_GRAVITY,
  PartnerConfigKey.KEY_CONTENT_PADDING_TOP,
  PartnerConfigKey.KEY_CONTENT_INFO_TEXT_SIZE,
  PartnerConfigKey.KEY_CONTENT_INFO_FONT_FAMILY,
  PartnerConfigKey.KEY_CONTENT_INFO_LINE_SPACING_EXTRA,
  PartnerConfigKey.KEY_CONTENT_INFO_ICON_SIZE,
  PartnerConfigKey.KEY_CONTENT_INFO_ICON_MARGIN_END,
  PartnerConfigKey.KEY_CONTENT_INFO_PADDING_TOP,
  PartnerConfigKey.KEY_CONTENT_INFO_PADDING_BOTTOM,
  PartnerConfigKey.KEY_CARD_VIEW_INTRINSIC_WIDTH,
  PartnerConfigKey.KEY_CARD_VIEW_INTRINSIC_HEIGHT,
  PartnerConfigKey.KEY_ITEMS_TITLE_TEXT_SIZE,
  PartnerConfigKey.KEY_ITEMS_SUMMARY_TEXT_SIZE,
  PartnerConfigKey.KEY_ITEMS_SUMMARY_MARGIN_TOP,
  PartnerConfigKey.KEY_ITEMS_TITLE_FONT_FAMILY,
  PartnerConfigKey.KEY_ITEMS_SUMMARY_FONT_FAMILY,
  PartnerConfigKey.KEY_ITEMS_PADDING_TOP,
  PartnerConfigKey.KEY_ITEMS_PADDING_BOTTOM,
  PartnerConfigKey.KEY_ITEMS_GROUP_CORNER_RADIUS,
  PartnerConfigKey.KEY_ITEMS_MIN_HEIGHT,
  PartnerConfigKey.KEY_ITEMS_DIVIDER_SHOWN,
  PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_DEFAULT,
  PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_ACCOUNT,
  PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_CONNECTION,
  PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_UPDATE,
  PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_FINAL_HOLD,
  PartnerConfigKey.KEY_PROGRESS_ILLUSTRATION_DISPLAY_MINIMUM_MS,
  PartnerConfigKey.KEY_LOADING_LOTTIE_ACCOUNT,
  PartnerConfigKey.KEY_LOADING_LOTTIE_CONNECTION,
  PartnerConfigKey.KEY_LOADING_LOTTIE_DEFAULT,
  PartnerConfigKey.KEY_LOADING_LOTTIE_UPDATE,
  PartnerConfigKey.KEY_LOADING_LOTTIE_FINAL_HOLD,
  PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_DEFAULT,
  PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_ACCOUNT,
  PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_CONNECTION,
  PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_UPDATE,
  PartnerConfigKey.KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_FINAL_HOLD,
  PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_DEFAULT,
  PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_ACCOUNT,
  PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_CONNECTION,
  PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_UPDATE,
  PartnerConfigKey.KEY_LOADING_DARK_THEME_CUSTOMIZATION_FINAL_HOLD,
  PartnerConfigKey.KEY_TRANSITION_TYPE,
  PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_TOP,
  PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_START,
  PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_END,
  PartnerConfigKey.KEY_LOADING_LAYOUT_CONTENT_PADDING_BOTTOM,
  PartnerConfigKey.KEY_LOADING_LAYOUT_HEADER_HEIGHT,
  PartnerConfigKey.KEY_LOADING_LAYOUT_FULL_SCREEN_ILLUSTRATION_ENABLED,
  PartnerConfigKey.KEY_LOADING_LAYOUT_WAIT_FOR_ANIMATION_FINISHED,
  PartnerConfigKey.KEY_PROGRESS_BAR_MARGIN_TOP,
  PartnerConfigKey.KEY_PROGRESS_BAR_MARGIN_BOTTOM,
  PartnerConfigKey.KEY_TWO_PANE_ADAPT_WINDOW_WIDTH,
})
// TODO: can be removed and always reference PartnerConfig.getResourceName()?
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public @interface PartnerConfigKey {
  // Status bar background color or illustration.
  String KEY_STATUS_BAR_BACKGROUND = "setup_compat_status_bar_background";

  // The same as "WindowLightStatusBar". If set true, the status bar icons will be drawn such
  // that it is compatible with a light status bar background
  String KEY_LIGHT_STATUS_BAR = "setup_compat_light_status_bar";

  // Navigation bar background color
  String KEY_NAVIGATION_BAR_BG_COLOR = "setup_compat_navigation_bar_bg_color";

  // The same as "windowLightNavigationBar". If set true, the navigation bar icons will be drawn
  // such that it is compatible with a light navigation bar background.
  String KEY_LIGHT_NAVIGATION_BAR = "setup_compat_light_navigation_bar";

  // Navigation bar divider color
  String KEY_NAVIGATION_BAR_DIVIDER_COLOR = "setup_compat_navigation_bar_divider_color";

  // Background color of the footer bar.
  String KEY_FOOTER_BAR_BG_COLOR = "setup_compat_footer_bar_bg_color";

  // The min height of the footer bar
  String KEY_FOOTER_BAR_MIN_HEIGHT = "setup_compat_footer_bar_min_height";

  // The padding start of the footer bar
  String KEY_FOOTER_BAR_PADDING_START = "setup_compat_footer_bar_padding_start";

  // The padding end of the footer bar
  String KEY_FOOTER_BAR_PADDING_END = "setup_compat_footer_bar_padding_end";

  // The font face used in footer buttons. This must be a string reference to a font that is
  // available in the system. Font references (@font or @xml) are not allowed.
  String KEY_FOOTER_BUTTON_FONT_FAMILY = "setup_compat_footer_button_font_family";

  // The font weight used in footer buttons.
  String KEY_FOOTER_BUTTON_FONT_WEIGHT = "setup_compat_footer_button_font_weight";

  // The icon for "add another" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_ADD_ANOTHER = "setup_compat_footer_button_icon_add_another";

  // The icon for "cancel" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_CANCEL = "setup_compat_footer_button_icon_cancel";

  // The icon for "clear" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_CLEAR = "setup_compat_footer_button_icon_clear";

  // The icon for "done" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_DONE = "setup_compat_footer_button_icon_done";

  // The icon for "next" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_NEXT = "setup_compat_footer_button_icon_next";

  // The icon for "opt-in" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_OPT_IN = "setup_compat_footer_button_icon_opt_in";

  // The icon for "skip" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_SKIP = "setup_compat_footer_button_icon_skip";

  // The icon for "stop" action. Can be "@null" for no icon.
  String KEY_FOOTER_BUTTON_ICON_STOP = "setup_compat_footer_button_icon_stop";

  // Top padding of the footer buttons
  String KEY_FOOTER_BUTTON_PADDING_TOP = "setup_compat_footer_button_padding_top";

  // Bottom padding of the footer buttons
  String KEY_FOOTER_BUTTON_PADDING_BOTTOM = "setup_compat_footer_button_padding_bottom";

  // Corner radius of the footer buttons
  String KEY_FOOTER_BUTTON_RADIUS = "setup_compat_footer_button_radius";

  // Ripple color alpha of the footer buttons
  String KEY_FOOTER_BUTTON_RIPPLE_ALPHA = "setup_compat_footer_button_ripple_alpha";

  // Text size of the footer buttons
  String KEY_FOOTER_BUTTON_TEXT_SIZE = "setup_compat_footer_button_text_size";

  // The font face used in footer buttons {0 = NORMAL}, {1 = BOLD}, {2 = ITALIC}, {3 = BOLD_ITALIC}
  String KEY_FOOTER_BUTTON_TEXT_STYLE = "setup_compat_footer_button_text_style";

  // The min height of the footer buttons
  String KEY_FOOTER_BUTTON_MIN_HEIGHT = "setup_compat_footer_button_min_height";

  // Make the footer buttons all aligned the end
  String KEY_FOOTER_BUTTON_ALIGNED_END = "setup_compat_footer_button_aligned_end";

  // Disabled background alpha of the footer buttons
  String KEY_FOOTER_BUTTON_DISABLED_ALPHA = "setup_compat_footer_button_disabled_alpha";

  // Disabled background color of the footer buttons
  String KEY_FOOTER_BUTTON_DISABLED_BG_COLOR = "setup_compat_footer_button_disabled_bg_color";

  // Background color of the primary footer button
  String KEY_FOOTER_PRIMARY_BUTTON_BG_COLOR = "setup_compat_footer_primary_button_bg_color";

  // Text color of the primary footer button
  String KEY_FOOTER_PRIMARY_BUTTON_TEXT_COLOR = "setup_compat_footer_primary_button_text_color";

  // Margin start of the primary footer button
  String KEY_FOOTER_PRIMARY_BUTTON_MARGIN_START = "setup_compat_footer_primary_button_margin_start";

  // Disabled text color of the primary footer button
  String KEY_PRIMARY_BUTTON_DISABLED_TEXT_COLOR = "setup_compat_primary_button_disabled_text_color";

  // Background color of the secondary footer button
  String KEY_FOOTER_SECONDARY_BUTTON_BG_COLOR = "setup_compat_footer_secondary_button_bg_color";

  // Text color of the secondary footer button
  String KEY_FOOTER_SECONDARY_BUTTON_TEXT_COLOR = "setup_compat_footer_secondary_button_text_color";

  // Margin start of the secondary footer button
  String KEY_FOOTER_SECONDARY_BUTTON_MARGIN_START =
      "setup_compat_footer_secondary_button_margin_start";

  // Disabled text color of the secondary footer button
  String KEY_SECONDARY_BUTTON_DISABLED_TEXT_COLOR =
      "setup_compat_secondary_button_disabled_text_color";

  // Background color of layout
  String KEY_LAYOUT_BACKGROUND_COLOR = "setup_design_layout_bg_color";

  // Margin start of the layout
  String KEY_LAYOUT_MARGIN_START = "setup_design_layout_margin_start";

  // Margin end of the layout
  String KEY_LAYOUT_MARGIN_END = "setup_design_layout_margin_end";

  // Middle horizontal spacing of the landscape layout
  String KEY_LAND_MIDDLE_HORIZONTAL_SPACING = "setup_design_land_middle_horizontal_spacing";

  // Text size of the header
  String KEY_HEADER_TEXT_SIZE = "setup_design_header_text_size";

  // Text color of the header
  String KEY_HEADER_TEXT_COLOR = "setup_design_header_text_color";

  // Font family of the header
  String KEY_HEADER_FONT_FAMILY = "setup_design_header_font_family";

  // Font weight of the header
  String KEY_HEADER_FONT_WEIGHT = "setup_design_header_font_weight";

  // Margin top of the header text
  String KEY_HEADER_TEXT_MARGIN_TOP = "setup_design_header_text_margin_top";

  // Margin bottom of the header text
  String KEY_HEADER_TEXT_MARGIN_BOTTOM = "setup_design_header_text_margin_bottom";

  // Gravity of the header, icon and description
  String KEY_LAYOUT_GRAVITY = "setup_design_layout_gravity";

  // Margin top of the icon
  String KEY_ICON_MARGIN_TOP = "setup_design_icon_margin_top";

  // Size of the icon
  String KEY_ICON_SIZE = "setup_design_icon_size";

  // The max width for illustration
  String KEY_ILLUSTRATION_MAX_WIDTH = "setup_design_illustration_max_width";

  // The max height for illustration
  String KEY_ILLUSTRATION_MAX_HEIGHT = "setup_design_illustration_max_height";

  // Background color of the header area
  String KEY_HEADER_AREA_BACKGROUND_COLOR = "setup_design_header_area_background_color";

  // Margin bottom of the header container
  String KEY_HEADER_CONTAINER_MARGIN_BOTTOM = "setup_design_header_container_margin_bottom";

  // Auto text size enabled status
  String KEY_HEADER_AUTO_SIZE_ENABLED = "setup_design_header_auto_size_enabled";

  // Max text size of header when auto size enabled. Ignored if auto size is false.
  String KEY_HEADER_AUTO_SIZE_MAX_TEXT_SIZE = "setup_design_header_auto_size_max_text_size";

  // Min text size of header when auto size enabled. Ignored if auto size is false.
  String KEY_HEADER_AUTO_SIZE_MIN_TEXT_SIZE = "setup_design_header_auto_size_min_text_size";

  // The max lines of the max text size when auto size enabled. Ignored if auto size is false.
  String KEY_HEADER_AUTO_SIZE_MAX_LINE_OF_MAX_SIZE =
      "setup_design_header_auto_size_max_line_of_max_size";

  // Extra line spacing of header when auto size enabled. Ignored if auto size is false.
  String KEY_HEADER_AUTO_SIZE_LINE_SPACING_EXTRA =
      "setup_design_header_auto_size_line_spacing_extra";

  // Text size of the description
  String KEY_DESCRIPTION_TEXT_SIZE = "setup_design_description_text_size";

  // Text color of the description
  String KEY_DESCRIPTION_TEXT_COLOR = "setup_design_description_text_color";

  // Link text color of the description
  String KEY_DESCRIPTION_LINK_TEXT_COLOR = "setup_design_description_link_text_color";

  // Font family of the description
  String KEY_DESCRIPTION_FONT_FAMILY = "setup_design_description_font_family";

  // Font weight of the description
  String KEY_DESCRIPTION_FONT_WEIGHT = "setup_design_description_font_weight";

  // Font family of the link text
  String KEY_DESCRIPTION_LINK_FONT_FAMILY = "setup_design_description_link_font_family";

  // Margin top of the header text
  String KEY_DESCRIPTION_TEXT_MARGIN_TOP = "setup_design_description_text_margin_top";

  // Margin bottom of the header text
  String KEY_DESCRIPTION_TEXT_MARGIN_BOTTOM = "setup_design_description_text_margin_bottom";

  // Font size of the account name
  String KEY_ACCOUNT_NAME_TEXT_SIZE = "setup_design_account_name_text_size";

  // Font family of the account name
  String KEY_ACCOUNT_NAME_FONT_FAMILY = "setup_design_account_name_font_family";

  // Margin end of the account avatar
  String KEY_ACCOUNT_AVATAR_MARGIN_END = "setup_design_account_avatar_margin_end";

  // Size of the account avatar
  String KEY_ACCOUNT_AVATAR_MAX_SIZE = "setup_design_account_avatar_size";

  // Text size of the body content text
  String KEY_CONTENT_TEXT_SIZE = "setup_design_content_text_size";

  // Text color of the body content text
  String KEY_CONTENT_TEXT_COLOR = "setup_design_content_text_color";

  // Link text color of the body content text
  String KEY_CONTENT_LINK_TEXT_COLOR = "setup_design_content_link_text_color";

  // Font family of the body content text
  String KEY_CONTENT_FONT_FAMILY = "setup_design_content_font_family";

  // Gravity of the body content text
  String KEY_CONTENT_LAYOUT_GRAVITY = "setup_design_content_layout_gravity";

  // The padding top of the content
  String KEY_CONTENT_PADDING_TOP = "setup_design_content_padding_top";

  // The text size of the content info.
  String KEY_CONTENT_INFO_TEXT_SIZE = "setup_design_content_info_text_size";

  // The font family of the content info.
  String KEY_CONTENT_INFO_FONT_FAMILY = "setup_design_content_info_font_family";

  // The text line spacing extra of the content info.
  String KEY_CONTENT_INFO_LINE_SPACING_EXTRA = "setup_design_content_info_line_spacing_extra";

  // The icon size of the content info.
  String KEY_CONTENT_INFO_ICON_SIZE = "setup_design_content_info_icon_size";

  // The icon margin end of the content info.
  String KEY_CONTENT_INFO_ICON_MARGIN_END = "setup_design_content_info_icon_margin_end";

  // The padding top of the content info.
  String KEY_CONTENT_INFO_PADDING_TOP = "setup_design_content_info_padding_top";

  // The padding bottom of the content info.
  String KEY_CONTENT_INFO_PADDING_BOTTOM = "setup_design_content_info_padding_bottom";

  // The title text size of list items.
  String KEY_ITEMS_TITLE_TEXT_SIZE = "setup_design_items_title_text_size";

  // The summary text size of list items.
  String KEY_ITEMS_SUMMARY_TEXT_SIZE = "setup_design_items_summary_text_size";

  // The summary margin top of list items.
  String KEY_ITEMS_SUMMARY_MARGIN_TOP = "setup_design_items_summary_margin_top";

  // The title font family of list items.
  String KEY_ITEMS_TITLE_FONT_FAMILY = "setup_design_items_title_font_family";

  // The summary font family of list items.
  String KEY_ITEMS_SUMMARY_FONT_FAMILY = "setup_design_items_summary_font_family";

  // The padding top of list items.
  String KEY_ITEMS_PADDING_TOP = "setup_design_items_padding_top";

  // The padding bottom of list items.
  String KEY_ITEMS_PADDING_BOTTOM = "setup_design_items_padding_bottom";

  // The corner radius of list items group.
  String KEY_ITEMS_GROUP_CORNER_RADIUS = "setup_design_items_group_corner_radius";

  // The minimum height of list items.
  String KEY_ITEMS_MIN_HEIGHT = "setup_design_items_min_height";

  // The divider of list items are showing.
  String KEY_ITEMS_DIVIDER_SHOWN = "setup_design_items_divider_shown";

  // The intrinsic width of the card view for foldable/tablet.
  String KEY_CARD_VIEW_INTRINSIC_WIDTH = "setup_design_card_view_intrinsic_width";

  // The intrinsic height of the card view for foldable/tablet.
  String KEY_CARD_VIEW_INTRINSIC_HEIGHT = "setup_design_card_view_intrinsic_height";

  // The animation of loading screen used in those activities which is non of below type.
  String KEY_PROGRESS_ILLUSTRATION_DEFAULT = "progress_illustration_custom_default";

  // The animation of loading screen used in those activity which is processing account info or
  // related functions.
  // For example:com.google.android.setupwizard.LOAD_ADD_ACCOUNT_INTENT
  String KEY_PROGRESS_ILLUSTRATION_ACCOUNT = "progress_illustration_custom_account";

  // The animation of loading screen used in those activity which is processing data connection.
  // For example:com.android.setupwizard.CAPTIVE_PORTAL
  String KEY_PROGRESS_ILLUSTRATION_CONNECTION = "progress_illustration_custom_connection";

  // The animation of loading screen used in those activities which is updating device.
  // For example:com.google.android.setupwizard.COMPAT_EARLY_UPDATE
  String KEY_PROGRESS_ILLUSTRATION_UPDATE = "progress_illustration_custom_update";

  // The animation of loading screen used in those activities which is updating device.
  // For example:com.google.android.setupwizard.FINAL_HOLD
  String KEY_PROGRESS_ILLUSTRATION_FINAL_HOLD = "final_hold_custom_illustration";

  // The minimum illustration display time, set to 0 may cause the illustration stuck
  String KEY_PROGRESS_ILLUSTRATION_DISPLAY_MINIMUM_MS = "progress_illustration_display_minimum_ms";

  // The animation for S+ devices used in those screens waiting for non of below type.
  String KEY_LOADING_LOTTIE_DEFAULT = "loading_animation_custom_default";

  // The animation for S+ devices used in those screens which is processing account info or related
  // functions.
  // For example:com.google.android.setupwizard.LOAD_ADD_ACCOUNT_INTENT
  String KEY_LOADING_LOTTIE_ACCOUNT = "loading_animation_custom_account";

  // The animation for S+ devices used in those screens which is processing data connection.
  // For example:com.android.setupwizard.CAPTIVE_PORTAL
  String KEY_LOADING_LOTTIE_CONNECTION = "loading_animation_custom_connection";

  // The animation for S+ devices used in those screens which is updating devices.
  // For example:com.google.android.setupwizard.COMPAT_EARLY_UPDATE
  String KEY_LOADING_LOTTIE_UPDATE = "loading_animation_custom_update";

  // The animation for S+ devices used in those screens which is updating devices.
  // For example:com.google.android.setupwizard.FINAL_HOLD
  String KEY_LOADING_LOTTIE_FINAL_HOLD = "loading_animation_custom_final_hold";

  // A string-array to list all the key path and color map for default animation for light theme.
  // For example: background:#FFFFFF
  String KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_DEFAULT =
      "loading_light_theme_customization_default";

  // A string-array to list all the key path and color map for account animation for light theme.
  // For example: background:#FFFFFF
  String KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_ACCOUNT =
      "loading_light_theme_customization_account";

  // A string-array to list all the key path and color map for connection animation for light theme.
  // For example: background:#FFFFFF
  String KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_CONNECTION =
      "loading_light_theme_customization_connection";

  // A string-array to list all the key path and color map for update animation for light theme.
  // For example: background:#FFFFFF
  String KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_UPDATE = "loading_light_theme_customization_update";

  // A string-array to list all the key path and color map for final hold animation for light theme.
  // For example: background:#FFFFFF
  String KEY_LOADING_LIGHT_THEME_CUSTOMIZATION_FINAL_HOLD =
      "loading_light_theme_customization_final_hold";

  // A string-array to list all the key path and color map for default animation for dark theme.
  // For example: background:#000000
  String KEY_LOADING_DARK_THEME_CUSTOMIZATION_DEFAULT = "loading_dark_theme_customization_default";

  // A string-array to list all the key path and color map for account animation for dark theme.
  // For example: background:#000000
  String KEY_LOADING_DARK_THEME_CUSTOMIZATION_ACCOUNT = "loading_dark_theme_customization_account";

  // A string-array to list all the key path and color map for connection animation for dark theme.
  // For example: background:#000000
  String KEY_LOADING_DARK_THEME_CUSTOMIZATION_CONNECTION =
      "loading_dark_theme_customization_connection";

  // A string-array to list all the key path and color map for update animation for dark theme.
  // For example: background:#000000
  String KEY_LOADING_DARK_THEME_CUSTOMIZATION_UPDATE = "loading_dark_theme_customization_update";

  // A string-array to list all the key path and color map for final hold animation for dark theme.
  // For example: background:#000000
  String KEY_LOADING_DARK_THEME_CUSTOMIZATION_FINAL_HOLD =
      "loading_dark_theme_customization_final_hold";

  // The transition type between activities
  String KEY_TRANSITION_TYPE = "setup_design_transition_type";

  // A padding top of the content frame of loading layout.
  String KEY_LOADING_LAYOUT_CONTENT_PADDING_TOP = "loading_layout_content_padding_top";

  // A padding start of the content frame of loading layout.
  String KEY_LOADING_LAYOUT_CONTENT_PADDING_START = "loading_layout_content_padding_start";

  // A padding end of the content frame of loading layout.
  String KEY_LOADING_LAYOUT_CONTENT_PADDING_END = "loading_layout_content_padding_end";

  // A padding bottom of the content frame of loading layout.
  String KEY_LOADING_LAYOUT_CONTENT_PADDING_BOTTOM = "loading_layout_content_padding_bottom";

  // A height of the header of loading layout.
  String KEY_LOADING_LAYOUT_HEADER_HEIGHT = "loading_layout_header_height";

  // Use the fullscreen style lottie animation.
  String KEY_LOADING_LAYOUT_FULL_SCREEN_ILLUSTRATION_ENABLED =
      "loading_layout_full_screen_illustration_enabled";

  // Waiting the animation finished before process to the next page/action.
  String KEY_LOADING_LAYOUT_WAIT_FOR_ANIMATION_FINISHED =
      "loading_layout_wait_for_animation_finished";

  // A margin top of the content frame of progress bar.
  String KEY_PROGRESS_BAR_MARGIN_TOP = "setup_design_progress_bar_margin_top";

  // A margin bottom of the content frame of progress bar.
  String KEY_PROGRESS_BAR_MARGIN_BOTTOM = "setup_design_progress_bar_margin_bottom";

  // A adapt window width to determine how large to show two panel.
  String KEY_TWO_PANE_ADAPT_WINDOW_WIDTH = "setup_compat_two_pane_adapt_window_width";
}
