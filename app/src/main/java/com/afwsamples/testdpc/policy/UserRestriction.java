package com.afwsamples.testdpc.policy;

import static android.os.UserManager.ALLOW_PARENT_PROFILE_APP_LINKING;
import static android.os.UserManager.DISALLOW_ADD_MANAGED_PROFILE;
import static android.os.UserManager.DISALLOW_ADD_USER;
import static android.os.UserManager.DISALLOW_ADJUST_VOLUME;
import static android.os.UserManager.DISALLOW_APPS_CONTROL;
import static android.os.UserManager.DISALLOW_AUTOFILL;
import static android.os.UserManager.DISALLOW_BLUETOOTH;
import static android.os.UserManager.DISALLOW_BLUETOOTH_SHARING;
import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;
import static android.os.UserManager.DISALLOW_CONFIG_CELL_BROADCASTS;
import static android.os.UserManager.DISALLOW_CONFIG_CREDENTIALS;
import static android.os.UserManager.DISALLOW_CONFIG_DATE_TIME;
import static android.os.UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS;
import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_CONFIG_VPN;
import static android.os.UserManager.DISALLOW_CONFIG_WIFI;
import static android.os.UserManager.DISALLOW_CREATE_WINDOWS;
import static android.os.UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE;
import static android.os.UserManager.DISALLOW_DATA_ROAMING;
import static android.os.UserManager.DISALLOW_DEBUGGING_FEATURES;
import static android.os.UserManager.DISALLOW_FACTORY_RESET;
import static android.os.UserManager.DISALLOW_FUN;
import static android.os.UserManager.DISALLOW_INSTALL_APPS;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS;
import static android.os.UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA;
import static android.os.UserManager.DISALLOW_NETWORK_RESET;
import static android.os.UserManager.DISALLOW_OUTGOING_BEAM;
import static android.os.UserManager.DISALLOW_OUTGOING_CALLS;
import static android.os.UserManager.DISALLOW_REMOVE_MANAGED_PROFILE;
import static android.os.UserManager.DISALLOW_REMOVE_USER;
import static android.os.UserManager.DISALLOW_SAFE_BOOT;
import static android.os.UserManager.DISALLOW_SET_USER_ICON;
import static android.os.UserManager.DISALLOW_SET_WALLPAPER;
import static android.os.UserManager.DISALLOW_SHARE_LOCATION;
import static android.os.UserManager.DISALLOW_SMS;
import static android.os.UserManager.DISALLOW_UNINSTALL_APPS;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static android.os.UserManager.DISALLOW_USB_FILE_TRANSFER;
import static android.os.UserManager.DISALLOW_USER_SWITCH;
import static android.os.UserManager.ENSURE_VERIFY_APPS;

import com.afwsamples.testdpc.R;

public class UserRestriction {
    public static final String DISALLOW_CONFIG_LOCATION_MODE = "no_config_location_mode";
    public static final String DISALLOW_AIRPLANE_MODE = "no_airplane_mode";
    public static final String DISALLOW_CONFIG_BRIGHTNESS = "no_config_brightness";
    public static final String DISALLOW_CONFIG_SCREEN_TIMEOUT = "no_config_screen_timeout";
    public static final String DISALLOW_CONFIG_AMBIENT_DISPLAY = "no_config_ambient_display";

    // TODO: remove it once available in SDK
    public static final String DISALLOW_UNIFIED_PASSWORD = "no_unified_password";

    public String key;
    public int titleResId;

    public UserRestriction(String key, int titleResId) {
        this.key = key;
        this.titleResId = titleResId;
    }

    // TODO(68687253): Replace this hard-coded string with UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS
    private static final String DISALLOW_SYSTEM_ERROR_DIALOGS = "no_system_error_dialogs";

    public static final UserRestriction[] ALL_USER_RESTRICTIONS = {
            new UserRestriction(
                    ALLOW_PARENT_PROFILE_APP_LINKING,
                    R.string.allow_parent_profile_app_linking),
            new UserRestriction(
                    DISALLOW_ADD_MANAGED_PROFILE,
                    R.string.disallow_add_managed_profile),
            new UserRestriction(DISALLOW_ADD_USER,
                    R.string.disallow_add_user),
            new UserRestriction(
                    DISALLOW_ADJUST_VOLUME,
                    R.string.disallow_adjust_volume),
            new UserRestriction(
                    DISALLOW_APPS_CONTROL,
                    R.string.disallow_apps_control),
            new UserRestriction(
                    DISALLOW_BLUETOOTH,
                    R.string.disallow_bluetooth),
            new UserRestriction(
                    DISALLOW_CONFIG_BLUETOOTH,
                    R.string.disallow_config_bluetooth),
            new UserRestriction(
                    DISALLOW_CONFIG_CELL_BROADCASTS,
                    R.string.disallow_config_cell_broadcasts),
            new UserRestriction(
                    DISALLOW_CONFIG_CREDENTIALS, R.string.disallow_config_credentials),
            new UserRestriction(DISALLOW_CONFIG_MOBILE_NETWORKS,
                    R.string.disallow_config_mobile_networks),
            new UserRestriction(DISALLOW_CONFIG_TETHERING, R
                    .string.disallow_config_tethering),
            new UserRestriction(
                    DISALLOW_CONFIG_VPN,
                    R.string.disallow_config_vpn),
            new UserRestriction(
                    DISALLOW_CONFIG_WIFI,
                    R.string.disallow_config_wifi),
            new UserRestriction(
                    DISALLOW_CREATE_WINDOWS,
                    R.string.disallow_create_windows),
            new UserRestriction(
                    DISALLOW_SYSTEM_ERROR_DIALOGS,
                    R.string.disallow_system_error_dialogs),
            new UserRestriction(DISALLOW_CROSS_PROFILE_COPY_PASTE,
                    R.string.disallow_cross_profile_copy_paste),
            new UserRestriction(DISALLOW_DATA_ROAMING,
                    R.string.disallow_data_roaming),
            new UserRestriction(DISALLOW_DEBUGGING_FEATURES, R
                    .string.disallow_debugging_features),
            new UserRestriction(
                    DISALLOW_FACTORY_RESET,
                    R.string.disallow_factory_reset),
            new UserRestriction(
                    DISALLOW_FUN,
                    R.string.disallow_fun),
            new UserRestriction(
                    DISALLOW_INSTALL_APPS,
                    R.string.disallow_install_apps),
            new UserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES,
                    R.string.disallow_install_unknown_sources),
            new UserRestriction(DISALLOW_MODIFY_ACCOUNTS, R
                    .string.disallow_modify_accounts),
            new UserRestriction(DISALLOW_MOUNT_PHYSICAL_MEDIA,
                    R.string.disallow_mount_physical_media),
            new UserRestriction(
                    DISALLOW_NETWORK_RESET,
                    R.string.disallow_network_reset),
            new UserRestriction(
                    DISALLOW_OUTGOING_BEAM,
                    R.string.disallow_outgoing_beam),
            new UserRestriction(
                    DISALLOW_OUTGOING_CALLS,
                    R.string.disallow_outgoing_calls),
            new UserRestriction(DISALLOW_REMOVE_MANAGED_PROFILE,
                    R.string.disallow_remove_managed_profile),
            new UserRestriction(
                    DISALLOW_REMOVE_USER,
                    R.string.disallow_remove_user),
            new UserRestriction(
                    DISALLOW_SAFE_BOOT,
                    R.string.disallow_safe_boot),
            new UserRestriction(
                    DISALLOW_SET_USER_ICON,
                    R.string.disallow_set_user_icon),
            new UserRestriction(
                    DISALLOW_SET_WALLPAPER,
                    R.string.disallow_set_wallpaper),
            new UserRestriction(
                    DISALLOW_SHARE_LOCATION,
                    R.string.disallow_share_location),
            new UserRestriction(
                    DISALLOW_SMS,
                    R.string.disallow_sms),
            new UserRestriction(
                    DISALLOW_UNINSTALL_APPS,
                    R.string.disallow_uninstall_apps),
            new UserRestriction(DISALLOW_UNMUTE_MICROPHONE, R
                    .string.disallow_unmute_microphone),
            new UserRestriction(DISALLOW_USB_FILE_TRANSFER, R
                    .string.disallow_usb_file_transfer),
            new UserRestriction(
                    ENSURE_VERIFY_APPS,
                    R.string.ensure_verify_apps),
            new UserRestriction(
                    DISALLOW_AUTOFILL,
                    R.string.disallow_autofill),
            new UserRestriction(
                    DISALLOW_BLUETOOTH_SHARING,
                    R.string.disallow_bluetooth_sharing),
            new UserRestriction(DISALLOW_UNIFIED_PASSWORD, R.string.disallow_unified_password),
            new UserRestriction(DISALLOW_USER_SWITCH, R.string.disallow_user_switch),
            new UserRestriction(
                    DISALLOW_CONFIG_LOCATION_MODE,
                    R.string.disallow_config_location_mode),
            new UserRestriction(
                    DISALLOW_AIRPLANE_MODE,
                    R.string.disallow_airplane_mode),
            new UserRestriction(
                    DISALLOW_CONFIG_BRIGHTNESS,
                    R.string.disallow_config_brightness),
            new UserRestriction(
                    DISALLOW_CONFIG_DATE_TIME,
                    R.string.disallow_config_date_time),
            new UserRestriction(
                    DISALLOW_CONFIG_SCREEN_TIMEOUT,
                    R.string.disallow_config_screen_timeout),
            new UserRestriction(
                    DISALLOW_CONFIG_AMBIENT_DISPLAY,
                    R.string.disallow_config_ambient_display),
    };

    /**
     * Setting these user restrictions only have effect on primary users.
     */
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
            DISALLOW_AIRPLANE_MODE
    };

    /**
     * User restrictions that cannot be set by profile owners. Applied to all users.
     */
    public static final String[] DEVICE_OWNER_ONLY_RESTRICTIONS = {
            DISALLOW_USER_SWITCH
    };

    /**
     * Setting these user restrictions only have effect on managed profiles.
     */
    public static final String[] MANAGED_PROFILE_ONLY_RESTRICTIONS = {
            ALLOW_PARENT_PROFILE_APP_LINKING,
            DISALLOW_CROSS_PROFILE_COPY_PASTE,
            DISALLOW_UNIFIED_PASSWORD
    };

    /**
     * These restrictions are not meant to be used with managed profiles.
     */
    public static String[] NON_MANAGED_PROFILE_RESTRICTIONS = {
            DISALLOW_REMOVE_USER,
            DISALLOW_SET_WALLPAPER
    };

    /**
     * These user restrictions are added in MNC.
     */
    public static String[] MNC_PLUS_RESTRICTIONS = {
            ALLOW_PARENT_PROFILE_APP_LINKING,
            DISALLOW_SAFE_BOOT
    };

    public static String[] NYC_PLUS_RESTRICTIONS = {
            DISALLOW_DATA_ROAMING,
            DISALLOW_SET_USER_ICON,
            DISALLOW_SET_WALLPAPER
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
            DISALLOW_CONFIG_LOCATION_MODE,
            DISALLOW_AIRPLANE_MODE,
            DISALLOW_CONFIG_DATE_TIME,
            DISALLOW_CONFIG_BRIGHTNESS,
            DISALLOW_CONFIG_SCREEN_TIMEOUT,
            DISALLOW_CONFIG_AMBIENT_DISPLAY
    };
}
