package com.afwsamples.testdpc.policy;

import static android.Manifest.permission.MANAGE_DEVICE_POLICY_ACCOUNT_MANAGEMENT;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_APPS_CONTROL;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_AUTOFILL;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_DISPLAY;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_LOCALE;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_LOCATION;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_LOCK_CREDENTIALS;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_MOBILE_NETWORK;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_MODIFY_USERS;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_PROFILES;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_RESTRICT_PRIVATE_DNS;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_SAFE_BOOT;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_SMS;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_TIME;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_VPN;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_WALLPAPER;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_WIFI;
import static android.os.UserManager.ALLOW_PARENT_PROFILE_APP_LINKING;
import static android.os.UserManager.DISALLOW_ADD_MANAGED_PROFILE;
import static android.os.UserManager.DISALLOW_ADD_USER;
import static android.os.UserManager.DISALLOW_ADD_WIFI_CONFIG;
import static android.os.UserManager.DISALLOW_ADJUST_VOLUME;
import static android.os.UserManager.DISALLOW_AIRPLANE_MODE;
import static android.os.UserManager.DISALLOW_AMBIENT_DISPLAY;
import static android.os.UserManager.DISALLOW_APPS_CONTROL;
import static android.os.UserManager.DISALLOW_AUTOFILL;
import static android.os.UserManager.DISALLOW_BLUETOOTH;
import static android.os.UserManager.DISALLOW_BLUETOOTH_SHARING;
import static android.os.UserManager.DISALLOW_CAMERA_TOGGLE;
import static android.os.UserManager.DISALLOW_CELLULAR_2G;
import static android.os.UserManager.DISALLOW_CHANGE_WIFI_STATE;
import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;
import static android.os.UserManager.DISALLOW_CONFIG_BRIGHTNESS;
import static android.os.UserManager.DISALLOW_CONFIG_CELL_BROADCASTS;
import static android.os.UserManager.DISALLOW_CONFIG_CREDENTIALS;
import static android.os.UserManager.DISALLOW_CONFIG_DATE_TIME;
import static android.os.UserManager.DISALLOW_CONFIG_DEFAULT_APPS;
import static android.os.UserManager.DISALLOW_CONFIG_LOCALE;
import static android.os.UserManager.DISALLOW_CONFIG_LOCATION;
import static android.os.UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS;
import static android.os.UserManager.DISALLOW_CONFIG_PRIVATE_DNS;
import static android.os.UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT;
import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_CONFIG_VPN;
import static android.os.UserManager.DISALLOW_CONFIG_WIFI;
import static android.os.UserManager.DISALLOW_CONTENT_CAPTURE;
import static android.os.UserManager.DISALLOW_CONTENT_SUGGESTIONS;
import static android.os.UserManager.DISALLOW_CREATE_WINDOWS;
import static android.os.UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE;
import static android.os.UserManager.DISALLOW_DATA_ROAMING;
import static android.os.UserManager.DISALLOW_DEBUGGING_FEATURES;
import static android.os.UserManager.DISALLOW_FACTORY_RESET;
import static android.os.UserManager.DISALLOW_FUN;
import static android.os.UserManager.DISALLOW_INSTALL_APPS;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY;
import static android.os.UserManager.DISALLOW_MICROPHONE_TOGGLE;
import static android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS;
import static android.os.UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA;
import static android.os.UserManager.DISALLOW_NETWORK_RESET;
import static android.os.UserManager.DISALLOW_OUTGOING_BEAM;
import static android.os.UserManager.DISALLOW_OUTGOING_CALLS;
import static android.os.UserManager.DISALLOW_PRINTING;
import static android.os.UserManager.DISALLOW_REMOVE_MANAGED_PROFILE;
import static android.os.UserManager.DISALLOW_REMOVE_USER;
import static android.os.UserManager.DISALLOW_SAFE_BOOT;
import static android.os.UserManager.DISALLOW_SET_USER_ICON;
import static android.os.UserManager.DISALLOW_SET_WALLPAPER;
import static android.os.UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE;
import static android.os.UserManager.DISALLOW_SHARE_LOCATION;
import static android.os.UserManager.DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI;
import static android.os.UserManager.DISALLOW_SMS;
import static android.os.UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS;
import static android.os.UserManager.DISALLOW_ULTRA_WIDEBAND_RADIO;
import static android.os.UserManager.DISALLOW_UNIFIED_PASSWORD;
import static android.os.UserManager.DISALLOW_UNINSTALL_APPS;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static android.os.UserManager.DISALLOW_USB_FILE_TRANSFER;
import static android.os.UserManager.DISALLOW_USER_SWITCH;
import static android.os.UserManager.DISALLOW_WIFI_DIRECT;
import static android.os.UserManager.DISALLOW_WIFI_TETHERING;
import static android.os.UserManager.ENSURE_VERIFY_APPS;

import com.afwsamples.testdpc.R;
import java.util.Arrays;

public class UserRestriction {
  private static final String DISALLOW_ADD_PRIVATE_PROFILE = "no_add_private_profile";
  private static final String DISALLOW_ASSIST_CONTENT = "no_assist_content";
  private static final String DISALLOW_SIM_GLOBALLY = "no_sim_globally";
  public String key;
  public int titleResId;
  public int minSdkVersion;
  public String permission;

  public UserRestriction(String key, int titleResId) {
    this.key = key;
    this.titleResId = titleResId;
  }

  public UserRestriction(String key, int titleResId, String permission) {
    this.key = key;
    this.titleResId = titleResId;
    this.permission = permission;
  }

  public static final UserRestriction[] ALL_USER_RESTRICTIONS = {
    new UserRestriction(
        ALLOW_PARENT_PROFILE_APP_LINKING, R.string.allow_parent_profile_app_linking),
    new UserRestriction(DISALLOW_ADD_MANAGED_PROFILE, R.string.disallow_add_managed_profile),
    new UserRestriction(
        DISALLOW_ADD_USER, R.string.disallow_add_user, MANAGE_DEVICE_POLICY_MODIFY_USERS),
    new UserRestriction(DISALLOW_ADJUST_VOLUME, R.string.disallow_adjust_volume),
    new UserRestriction(
        DISALLOW_APPS_CONTROL, R.string.disallow_apps_control, MANAGE_DEVICE_POLICY_APPS_CONTROL),
    new UserRestriction(DISALLOW_BLUETOOTH, R.string.disallow_bluetooth),
    new UserRestriction(DISALLOW_CHANGE_WIFI_STATE, R.string.disallow_change_wifi_state),
    new UserRestriction(DISALLOW_CONFIG_BLUETOOTH, R.string.disallow_config_bluetooth),
    new UserRestriction(DISALLOW_CONFIG_CELL_BROADCASTS, R.string.disallow_config_cell_broadcasts),
    new UserRestriction(
        DISALLOW_CONFIG_CREDENTIALS,
        R.string.disallow_config_credentials,
        MANAGE_DEVICE_POLICY_LOCK_CREDENTIALS),
    new UserRestriction(
        DISALLOW_CONFIG_MOBILE_NETWORKS,
        R.string.disallow_config_mobile_networks,
        MANAGE_DEVICE_POLICY_MOBILE_NETWORK),
    new UserRestriction(DISALLOW_CONFIG_TETHERING, R.string.disallow_config_tethering),
    new UserRestriction(
        DISALLOW_CONFIG_VPN, R.string.disallow_config_vpn, MANAGE_DEVICE_POLICY_VPN),
    new UserRestriction(
        DISALLOW_CONFIG_WIFI, R.string.disallow_config_wifi, MANAGE_DEVICE_POLICY_WIFI),
    new UserRestriction(DISALLOW_CONTENT_CAPTURE, R.string.disallow_content_capture),
    new UserRestriction(DISALLOW_CONTENT_SUGGESTIONS, R.string.disallow_content_suggestions),
    new UserRestriction(DISALLOW_CREATE_WINDOWS, R.string.disallow_create_windows),
    new UserRestriction(DISALLOW_SYSTEM_ERROR_DIALOGS, R.string.disallow_system_error_dialogs),
    new UserRestriction(
        DISALLOW_CROSS_PROFILE_COPY_PASTE, R.string.disallow_cross_profile_copy_paste),
    new UserRestriction(DISALLOW_DATA_ROAMING, R.string.disallow_data_roaming),
    new UserRestriction(DISALLOW_DEBUGGING_FEATURES, R.string.disallow_debugging_features),
    new UserRestriction(DISALLOW_FACTORY_RESET, R.string.disallow_factory_reset),
    new UserRestriction(DISALLOW_FUN, R.string.disallow_fun),
    new UserRestriction(
        DISALLOW_INSTALL_APPS, R.string.disallow_install_apps, MANAGE_DEVICE_POLICY_APPS_CONTROL),
    new UserRestriction(
        DISALLOW_INSTALL_UNKNOWN_SOURCES,
        R.string.disallow_install_unknown_sources,
        MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES),
    new UserRestriction(
        DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,
        R.string.disallow_install_unknown_sources_globally,
        MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES),
    new UserRestriction(
        DISALLOW_MODIFY_ACCOUNTS,
        R.string.disallow_modify_accounts,
        MANAGE_DEVICE_POLICY_ACCOUNT_MANAGEMENT),
    new UserRestriction(DISALLOW_MOUNT_PHYSICAL_MEDIA, R.string.disallow_mount_physical_media),
    new UserRestriction(DISALLOW_NETWORK_RESET, R.string.disallow_network_reset),
    new UserRestriction(DISALLOW_OUTGOING_BEAM, R.string.disallow_outgoing_beam),
    new UserRestriction(DISALLOW_OUTGOING_CALLS, R.string.disallow_outgoing_calls),
    new UserRestriction(DISALLOW_REMOVE_MANAGED_PROFILE, R.string.disallow_remove_managed_profile),
    new UserRestriction(
        DISALLOW_REMOVE_USER, R.string.disallow_remove_user, MANAGE_DEVICE_POLICY_MODIFY_USERS),
    new UserRestriction(
        DISALLOW_SAFE_BOOT, R.string.disallow_safe_boot, MANAGE_DEVICE_POLICY_SAFE_BOOT),
    new UserRestriction(
        DISALLOW_SET_USER_ICON, R.string.disallow_set_user_icon, MANAGE_DEVICE_POLICY_MODIFY_USERS),
    new UserRestriction(
        DISALLOW_SET_WALLPAPER, R.string.disallow_set_wallpaper, MANAGE_DEVICE_POLICY_WALLPAPER),
    new UserRestriction(DISALLOW_SHARE_LOCATION, R.string.disallow_share_location),
    new UserRestriction(DISALLOW_SMS, R.string.disallow_sms, MANAGE_DEVICE_POLICY_SMS),
    new UserRestriction(
        DISALLOW_UNINSTALL_APPS,
        R.string.disallow_uninstall_apps,
        MANAGE_DEVICE_POLICY_APPS_CONTROL),
    new UserRestriction(DISALLOW_UNMUTE_MICROPHONE, R.string.disallow_unmute_microphone),
    new UserRestriction(DISALLOW_USB_FILE_TRANSFER, R.string.disallow_usb_file_transfer),
    new UserRestriction(ENSURE_VERIFY_APPS, R.string.ensure_verify_apps),
    new UserRestriction(
        DISALLOW_AUTOFILL, R.string.disallow_autofill, MANAGE_DEVICE_POLICY_AUTOFILL),
    new UserRestriction(DISALLOW_BLUETOOTH_SHARING, R.string.disallow_bluetooth_sharing),
    new UserRestriction(DISALLOW_UNIFIED_PASSWORD, R.string.disallow_unified_password),
    new UserRestriction(DISALLOW_USER_SWITCH, R.string.disallow_user_switch),
    new UserRestriction(
        DISALLOW_CONFIG_LOCATION, R.string.disallow_config_location, MANAGE_DEVICE_POLICY_LOCATION),
    new UserRestriction(DISALLOW_AIRPLANE_MODE, R.string.disallow_airplane_mode),
    new UserRestriction(DISALLOW_CONFIG_BRIGHTNESS, R.string.disallow_config_brightness),
    new UserRestriction(
        DISALLOW_CONFIG_DATE_TIME, R.string.disallow_config_date_time, MANAGE_DEVICE_POLICY_TIME),
    new UserRestriction(
        DISALLOW_CONFIG_SCREEN_TIMEOUT,
        R.string.disallow_config_screen_timeout,
        MANAGE_DEVICE_POLICY_DISPLAY),
    new UserRestriction(
        DISALLOW_AMBIENT_DISPLAY, R.string.disallow_ambient_display, MANAGE_DEVICE_POLICY_DISPLAY),
    new UserRestriction(
        DISALLOW_SHARE_INTO_MANAGED_PROFILE, R.string.disallow_share_into_work_profile),
    new UserRestriction(DISALLOW_PRINTING, R.string.disallow_printing),
    new UserRestriction(
        DISALLOW_CONFIG_PRIVATE_DNS,
        R.string.disallow_config_private_dns,
        MANAGE_DEVICE_POLICY_RESTRICT_PRIVATE_DNS),
    new UserRestriction(DISALLOW_MICROPHONE_TOGGLE, R.string.disallow_microphone_toggle),
    new UserRestriction(
        DISALLOW_CAMERA_TOGGLE,
        R.string.disallow_camera_toggle,
        "android.permission.MANAGE_DEVICE_POLICY_CAMERA_TOGGLE"),
    new UserRestriction(DISALLOW_WIFI_TETHERING, R.string.disallow_wifi_tethering),
    new UserRestriction(
        DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI,
        R.string.disallow_sharing_admin_configured_wifi,
        MANAGE_DEVICE_POLICY_WIFI),
    new UserRestriction(DISALLOW_WIFI_DIRECT, R.string.disallow_wifi_direct),
    new UserRestriction(
        DISALLOW_ADD_WIFI_CONFIG, R.string.disallow_add_wifi_config, MANAGE_DEVICE_POLICY_WIFI),
    new UserRestriction(DISALLOW_CELLULAR_2G, R.string.disallow_cellular_2g),
    new UserRestriction(DISALLOW_CONFIG_DEFAULT_APPS, R.string.disallow_config_default_apps),
    new UserRestriction(
        DISALLOW_CONFIG_LOCALE, R.string.disallow_config_locale, MANAGE_DEVICE_POLICY_LOCALE),
    new UserRestriction(DISALLOW_ULTRA_WIDEBAND_RADIO, R.string.disallow_ultra_wideband_radio),
    new UserRestriction(DISALLOW_ASSIST_CONTENT, R.string.disallow_assist_content),
    new UserRestriction(DISALLOW_SIM_GLOBALLY, R.string.disallow_sim_globally),
    new UserRestriction(
        DISALLOW_ADD_PRIVATE_PROFILE,
        R.string.disallow_add_private_profile,
        MANAGE_DEVICE_POLICY_PROFILES),
  };

  /**
   * These user restrictions are set on the parent DPM and can only be set by profile owners of an
   * organization owned device.
   */
  public static final String[] PROFILE_OWNER_ORG_DEVICE_RESTRICTIONS = {
    DISALLOW_CONFIG_DATE_TIME,
    DISALLOW_CONFIG_TETHERING,
    DISALLOW_DATA_ROAMING,
    DISALLOW_DEBUGGING_FEATURES,
    DISALLOW_BLUETOOTH,
    DISALLOW_BLUETOOTH_SHARING,
    DISALLOW_CHANGE_WIFI_STATE,
    DISALLOW_CONFIG_BLUETOOTH,
    DISALLOW_CONFIG_CELL_BROADCASTS,
    DISALLOW_CONFIG_LOCATION,
    DISALLOW_CONFIG_MOBILE_NETWORKS,
    DISALLOW_CONFIG_PRIVATE_DNS,
    DISALLOW_CONFIG_WIFI,
    DISALLOW_CONTENT_CAPTURE,
    DISALLOW_CONTENT_SUGGESTIONS,
    DISALLOW_SAFE_BOOT,
    DISALLOW_SHARE_LOCATION,
    DISALLOW_SMS,
    DISALLOW_USB_FILE_TRANSFER,
    DISALLOW_AIRPLANE_MODE,
    DISALLOW_MOUNT_PHYSICAL_MEDIA,
    DISALLOW_OUTGOING_CALLS,
    DISALLOW_UNMUTE_MICROPHONE,
    DISALLOW_WIFI_TETHERING,
    DISALLOW_WIFI_DIRECT,
    DISALLOW_ADD_WIFI_CONFIG,
    DISALLOW_CELLULAR_2G,
    DISALLOW_CONFIG_DEFAULT_APPS,
    DISALLOW_ULTRA_WIDEBAND_RADIO,
    DISALLOW_CONFIG_BRIGHTNESS,
    DISALLOW_CONFIG_SCREEN_TIMEOUT,
    DISALLOW_ADD_PRIVATE_PROFILE,
  };

  /** Setting these user restrictions only have effect on primary users. */
  public static final String[] PRIMARY_USER_ONLY_RESTRICTIONS = {
    DISALLOW_ADD_MANAGED_PROFILE,
    DISALLOW_ADD_USER,
    DISALLOW_ADJUST_VOLUME,
    DISALLOW_BLUETOOTH,
    DISALLOW_CONFIG_BLUETOOTH,
    DISALLOW_CONFIG_CELL_BROADCASTS,
    DISALLOW_CONFIG_MOBILE_NETWORKS,
    DISALLOW_CONFIG_TETHERING,
    DISALLOW_CONFIG_WIFI,
    DISALLOW_CREATE_WINDOWS,
    DISALLOW_SYSTEM_ERROR_DIALOGS,
    DISALLOW_DATA_ROAMING,
    DISALLOW_FACTORY_RESET,
    DISALLOW_FUN,
    DISALLOW_MOUNT_PHYSICAL_MEDIA,
    DISALLOW_NETWORK_RESET,
    DISALLOW_OUTGOING_CALLS,
    DISALLOW_REMOVE_MANAGED_PROFILE,
    DISALLOW_SAFE_BOOT,
    DISALLOW_SMS,
    DISALLOW_UNMUTE_MICROPHONE,
    DISALLOW_USB_FILE_TRANSFER,
    DISALLOW_AIRPLANE_MODE,
    DISALLOW_CONFIG_PRIVATE_DNS,
    DISALLOW_ULTRA_WIDEBAND_RADIO,
  };

  /** User restrictions that cannot be set by profile owners. Applied to all users. */
  private static final String[] DEVICE_OWNER_ONLY_RESTRICTIONS = {
    DISALLOW_USER_SWITCH,
    DISALLOW_MICROPHONE_TOGGLE,
    DISALLOW_CAMERA_TOGGLE,
    DISALLOW_CHANGE_WIFI_STATE,
    DISALLOW_WIFI_TETHERING,
    DISALLOW_WIFI_DIRECT,
    DISALLOW_ADD_WIFI_CONFIG,
    DISALLOW_CELLULAR_2G,
  };

  /** Setting these user restrictions only have effect on managed profiles. */
  public static final String[] MANAGED_PROFILE_ONLY_RESTRICTIONS = {
    ALLOW_PARENT_PROFILE_APP_LINKING,
    DISALLOW_CROSS_PROFILE_COPY_PASTE,
    DISALLOW_UNIFIED_PASSWORD,
    DISALLOW_SHARE_INTO_MANAGED_PROFILE,
  };

  /** These restrictions are not meant to be used with managed profiles. */
  public static String[] NON_MANAGED_PROFILE_RESTRICTIONS = {
    DISALLOW_REMOVE_USER,
    DISALLOW_SET_WALLPAPER,
    DISALLOW_CONFIG_DATE_TIME,
    DISALLOW_AIRPLANE_MODE,
    DISALLOW_CONFIG_SCREEN_TIMEOUT,
    DISALLOW_CONFIG_BRIGHTNESS,
    DISALLOW_AMBIENT_DISPLAY,
    DISALLOW_ADD_PRIVATE_PROFILE,
  };

  /** These user restrictions are added in MNC. */
  public static String[] MNC_PLUS_RESTRICTIONS = {
    ALLOW_PARENT_PROFILE_APP_LINKING, DISALLOW_SAFE_BOOT
  };

  public static String[] NYC_PLUS_RESTRICTIONS = {
    DISALLOW_DATA_ROAMING, DISALLOW_SET_USER_ICON, DISALLOW_SET_WALLPAPER
  };

  public static String[] OC_PLUS_RESTRICTIONS = {
    DISALLOW_ADD_MANAGED_PROFILE,
    DISALLOW_BLUETOOTH,
    DISALLOW_REMOVE_MANAGED_PROFILE,
    DISALLOW_AUTOFILL,
    DISALLOW_BLUETOOTH_SHARING
  };

  public static String[] PIC_PLUS_RESTRICTIONS = {
    DISALLOW_UNIFIED_PASSWORD,
    DISALLOW_SYSTEM_ERROR_DIALOGS,
    DISALLOW_USER_SWITCH,
    DISALLOW_CONFIG_LOCATION,
    DISALLOW_AIRPLANE_MODE,
    DISALLOW_CONFIG_DATE_TIME,
    DISALLOW_CONFIG_BRIGHTNESS,
    DISALLOW_CONFIG_SCREEN_TIMEOUT,
    DISALLOW_AMBIENT_DISPLAY,
    DISALLOW_SHARE_INTO_MANAGED_PROFILE,
    DISALLOW_PRINTING,
    DISALLOW_CONFIG_LOCALE,
  };

  public static String[] QT_PLUS_RESTRICTIONS = {
    DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, DISALLOW_CONFIG_PRIVATE_DNS,
  };

  public static String[] SC_PLUS_RESTRICTIONS = {
    DISALLOW_MICROPHONE_TOGGLE, DISALLOW_CAMERA_TOGGLE,
  };

  public static String[] TM_PLUS_RESTRICTIONS = {
    DISALLOW_CHANGE_WIFI_STATE,
    DISALLOW_WIFI_TETHERING,
    DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI,
    DISALLOW_WIFI_DIRECT,
    DISALLOW_ADD_WIFI_CONFIG,
  };

  public static String[] UDC_PLUS_RESTRICTIONS = {
    DISALLOW_CELLULAR_2G, DISALLOW_CONFIG_DEFAULT_APPS, DISALLOW_ULTRA_WIDEBAND_RADIO,
  };

  public static String[] VIC_PLUS_RESTRICTIONS = {DISALLOW_SIM_GLOBALLY};

  public static String[] VIC_PLUS_PARENT_RESTRICTIONS = {
    DISALLOW_CONFIG_BRIGHTNESS,
    DISALLOW_CONFIG_SCREEN_TIMEOUT,
    DISALLOW_ASSIST_CONTENT,
    DISALLOW_ADD_PRIVATE_PROFILE
  };

  public static UserRestriction getRestriction(String restrictionKey) {
    return Arrays.stream(ALL_USER_RESTRICTIONS)
        .filter(r -> r.key.equals(restrictionKey))
        .findFirst()
        .orElse(null);
  }

  public static boolean isDeviceOwnerOnlyRestriction(UserRestriction restriction) {
    return Arrays.stream(DEVICE_OWNER_ONLY_RESTRICTIONS).anyMatch(restriction.key::equals);
  }
}
