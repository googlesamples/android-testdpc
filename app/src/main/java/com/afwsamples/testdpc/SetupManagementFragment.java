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

package com.afwsamples.testdpc;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.os.BuildCompat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.common.ColorPicker;
import com.afwsamples.testdpc.common.LaunchIntentUtil;
import com.afwsamples.testdpc.common.ProvisioningStateUtil;
import com.afwsamples.testdpc.common.Util;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_LOGO_URI;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_MAIN_COLOR;

/**
 * This {@link Fragment} shows the UI that allows the user to start the setup of a managed profile
 * or configuration of a device-owner if the device is in an appropriate state.
 */
public class SetupManagementFragment extends Fragment implements
        NavigationBar.NavigationBarListener, View.OnClickListener,
        ColorPicker.OnColorSelectListener, RadioGroup.OnCheckedChangeListener {
    // Tag for creating this fragment. This tag can be used to retrieve this fragment.
    public static final String FRAGMENT_TAG = "SetupManagementFragment";

    private static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;
    private static final int REQUEST_PROVISION_DEVICE_OWNER = 2;
    private static final int REQUEST_GET_LOGO = 3;

    private RadioGroup mSetupOptions;
    private Button mNavigationNextButton;
    private CheckBox mSkipUserConsent;
    private CheckBox mSkipEncryption;
    private CheckBox mKeepAccountMigrated;
    private ImageButton mParamsIndicator;
    private View mParamsView;
    private static final int[] STATE_EXPANDED = new int[] {R.attr.state_expanded};
    private static final int[] STATE_COLLAPSED = new int[] {-R.attr.state_expanded};

    private ImageView mColorPreviewView;
    private TextView mColorValue;
    private ImageView mLogoPreviewView;
    private TextView mLogoValue;

    private int mCurrentColor;
    private Uri mLogoUri = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLogoUri = (Uri) savedInstanceState.getParcelable(EXTRA_PROVISIONING_LOGO_URI);
            mCurrentColor = savedInstanceState.getInt(EXTRA_PROVISIONING_MAIN_COLOR);
        } else {
            mLogoUri = resourceToUri(getActivity(), R.drawable.ic_launcher);
            mCurrentColor = getResources().getColor(R.color.teal);
        }

        // Use setupwizard theme
        final Context contextThemeWrapper =
                new ContextThemeWrapper(getActivity(), R.style.SetupTheme);
        LayoutInflater themeInflater = inflater.cloneInContext(contextThemeWrapper);
        View view = themeInflater.inflate(R.layout.setup_management_fragment, container, false);
        SetupWizardLayout layout = (SetupWizardLayout) view.findViewById(R.id.setup_wizard_layout);
        NavigationBar navigationBar = layout.getNavigationBar();
        navigationBar.setNavigationBarListener(this);
        navigationBar.getBackButton().setText(R.string.exit);
        mNavigationNextButton = navigationBar.getNextButton();
        mNavigationNextButton.setText(R.string.setup_label);

        mSetupOptions = (RadioGroup) view.findViewById(R.id.setup_options);
        mSetupOptions.setOnCheckedChangeListener(this);
        mSkipUserConsent = (CheckBox) view.findViewById(R.id.skip_user_consent);
        mKeepAccountMigrated = (CheckBox) view.findViewById(R.id.keep_account_migrated);
        mSkipEncryption = (CheckBox) view.findViewById(R.id.skip_encryption);

        mParamsView = view.findViewById(R.id.params);
        mParamsIndicator = (ImageButton) view.findViewById(R.id.params_indicator);
        mParamsIndicator.setOnClickListener(this);

        view.findViewById(R.id.color_select_button).setOnClickListener(this);
        mColorValue = (TextView) view.findViewById(R.id.selected_color_value);
        mColorValue.setText(String.format(ColorPicker.COLOR_STRING_FORMATTER, mCurrentColor));
        mColorPreviewView = (ImageView) view.findViewById(R.id.preview_color);
        mColorPreviewView.setImageTintList(ColorStateList.valueOf(mCurrentColor));

        Intent launchIntent = getActivity().getIntent();
        if (LaunchIntentUtil.isSynchronousAuthLaunch(launchIntent)) {
            Account addedAccount = LaunchIntentUtil.getAddedAccount(launchIntent);
            if (addedAccount != null) {
                view.findViewById(R.id.managed_account_desc).setVisibility(View.VISIBLE);
                // Show the user which account needs management.
                TextView managedAccountName = (TextView) view.findViewById(
                        R.id.managed_account_name);
                managedAccountName.setVisibility(View.VISIBLE);
                managedAccountName.setText(addedAccount.name);
            } else {
                // This is not an expected case, sync-auth is triggered by an account being added so
                // we expect to be told which account to migrate in the launch intent. We don't
                // finish() here as it's still technically feasible to continue.
                Toast.makeText(getActivity(), R.string.invalid_launch_intent_no_account,
                        Toast.LENGTH_LONG).show();
            }
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_PROVISIONING_LOGO_URI, mLogoUri);
        outState.putInt(EXTRA_PROVISIONING_MAIN_COLOR, mCurrentColor);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getActionBar().hide();
        if (setProvisioningMethodsVisibility()) {
            // The extra logo uri and color are supported only from N
            if (BuildCompat.isAtLeastN()) {
                getView().findViewById(R.id.params_title).setVisibility(View.VISIBLE);
                if (canAnAppHandleGetContent()) {
                    getView().findViewById(
                            R.id.choose_logo_item_layout).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.logo_select_button).setOnClickListener(this);
                    mLogoValue = (TextView) getView().findViewById(R.id.selected_logo_value);
                    mLogoPreviewView = (ImageView) getView().findViewById(R.id.preview_logo);
                }
                setProvisioningModeSpecificUI();
            }
        } else {
            showNoProvisioningPossibleUI();
        }
    }

    /**
     * On R.id.setup_options are changed
     */
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        setProvisioningModeSpecificUI();
    }

    private void setProvisioningModeSpecificUI() {
        final int setUpOptionId = mSetupOptions.getCheckedRadioButtonId();
        final boolean isManagedProfileAction = setUpOptionId == R.id.setup_managed_profile;
        final boolean isManagedDeviceAction = setUpOptionId == R.id.setup_device_owner;
        mSkipUserConsent.setVisibility(Util.isAtLeastO() && isManagedProfileAction &&
                Util.isDeviceOwner(getActivity())
                ? View.VISIBLE
                : View.GONE);
        mKeepAccountMigrated.setVisibility(Util.isAtLeastO() && isManagedProfileAction
                ? View.VISIBLE
                : View.GONE);
        mSkipEncryption.setVisibility((isManagedProfileAction && BuildCompat.isAtLeastN())
                || (isManagedDeviceAction && Util.isAtLeastM())
                ? View.VISIBLE
                : View.GONE);
    }

    private void maybeLaunchProvisioning(String intentAction, int requestCode) {
        Activity activity = getActivity();

        Intent intent = new Intent(intentAction);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                    DeviceAdminReceiver.getComponentName(getActivity()));
        } else {
            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                    getActivity().getPackageName());
        }

        if (!maybeSpecifyNExtras(intent)) {
            // Unable to handle user-input - can't continue.
            return;
        }
        maybeSpecifySyncAuthExtras(intent);
        specifySkipUserConsent(intent);
        specifyKeepAccountMigrated(intent);
        specifySkipEncryption(intent);
        specifyDefaultDisclaimers(intent);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(activity, R.string.provisioning_not_supported, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void maybeSpecifySyncAuthExtras(Intent intent) {
        Activity activity = getActivity();
        Intent launchIntent = activity.getIntent();

        if (!LaunchIntentUtil.isSynchronousAuthLaunch(launchIntent)) {
            // Don't do anything if this isn't a sync-auth flow.
            return;
        }

        Account accountToMigrate = LaunchIntentUtil.getAddedAccount(launchIntent);
        if (accountToMigrate != null) {
            // EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE only supported in API 22+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                // Configure the account to migrate into the managed profile if setup
                // completes.
                intent.putExtra(EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE, accountToMigrate);
            } else {
                Toast.makeText(activity, R.string.migration_not_supported, Toast.LENGTH_SHORT)
                        .show();
            }
        }

        // Perculate launch intent extras through to DeviceAdminReceiver so they can be used there.
        PersistableBundle adminExtras = new PersistableBundle();
        LaunchIntentUtil.prepareDeviceAdminExtras(launchIntent, adminExtras);
        intent.putExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, adminExtras);
    }

    /**
     * @return true if we can launch the intent
     */
    private boolean maybeSpecifyNExtras(Intent intent) {
        if (BuildCompat.isAtLeastN()) {
            specifyLogoUri(intent);
            specifyColor(intent);
        }
        return true;
    }

    // TODO: replace with O SDK API
    private static final String EXTRA_PROVISIONING_DISCLAIMERS =
            "android.app.extra.PROVISIONING_DISCLAIMERS";
    private static final String EXTRA_PROVISIONING_DISCLAIMER_HEADER =
            "android.app.extra.PROVISIONING_DISCLAIMER_HEADER";
    private static final String EXTRA_PROVISIONING_DISCLAIMER_CONTENT =
            "android.app.extra.PROVISIONING_DISCLAIMER_CONTENT";

    private void specifyDefaultDisclaimers(Intent intent) {
        if (Util.isAtLeastO()) {
            Bundle emmBundle = new Bundle();
            emmBundle.putString(EXTRA_PROVISIONING_DISCLAIMER_HEADER,
                    getString(R.string.default_disclaimer_emm_name));
            emmBundle.putParcelable(EXTRA_PROVISIONING_DISCLAIMER_CONTENT,
                    resourceToUri(getActivity(), R.raw.emm_disclaimer));
            Bundle companyBundle = new Bundle();
            companyBundle.putString(EXTRA_PROVISIONING_DISCLAIMER_HEADER,
                    getString(R.string.default_disclaimer_company_name));
            companyBundle.putParcelable(EXTRA_PROVISIONING_DISCLAIMER_CONTENT,
                    resourceToUri(getActivity(), R.raw.company_disclaimer));
            intent.putExtra(EXTRA_PROVISIONING_DISCLAIMERS,
                    new Bundle[] { emmBundle, companyBundle });
        }
    }

    private static Uri resourceToUri(Context context, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID) );
    }

    private void specifySkipUserConsent(Intent intent) {
        if (Util.isAtLeastO() && ACTION_PROVISION_MANAGED_PROFILE.equals(intent.getAction())
                && mSkipUserConsent.getVisibility() == View.VISIBLE) {
            // TODO: use action string in Android SDK
            intent.putExtra("android.app.extra.PROVISIONING_SKIP_USER_CONSENT",
                    mSkipUserConsent.isChecked());
        }
    }

    private void specifyKeepAccountMigrated(Intent intent) {
        if (Util.isAtLeastO() && ACTION_PROVISION_MANAGED_PROFILE.equals(intent.getAction())
                && mKeepAccountMigrated.getVisibility() == View.VISIBLE) {
            // TODO: use action string in Android SDK
            intent.putExtra("android.app.extra.PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION",
                    mKeepAccountMigrated.isChecked());
        }
    }

    private void specifySkipEncryption(Intent intent) {
        if (mSkipEncryption.getVisibility() == View.VISIBLE) {
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION,
                    mSkipEncryption.isChecked());
        }
    }


    private void specifyLogoUri(Intent intent) {
        intent.putExtra(EXTRA_PROVISIONING_LOGO_URI, mLogoUri);
        if (mLogoUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newUri(getActivity().getContentResolver(), "", mLogoUri));
        }
    }

    private void specifyColor(Intent intent) {
        intent.putExtra(EXTRA_PROVISIONING_MAIN_COLOR, mCurrentColor);
    }

    private void showNoProvisioningPossibleUI() {
        mNavigationNextButton.setVisibility(View.GONE);
        TextView textView = (TextView) getView().findViewById(R.id.setup_management_message_id);
        textView.setText(R.string.provisioning_not_possible);
    }

    /**
     * Set visibility of all provisioning methods
     *
     * @return false if none of the provisioning method is visible
     */
    private boolean setProvisioningMethodsVisibility() {
        boolean hasProvisioningOption = false;
        hasProvisioningOption |= setVisibility(ACTION_PROVISION_MANAGED_PROFILE,
                R.id.setup_managed_profile);
        hasProvisioningOption |= setVisibility(ACTION_PROVISION_MANAGED_DEVICE,
                R.id.setup_device_owner);
        return hasProvisioningOption;
    }

    private boolean setVisibility(String action, int radioButtonId) {
        final int visibility = ProvisioningStateUtil.isProvisioningAllowed(getActivity(), action)
                ? View.VISIBLE : View.GONE;
        getView().findViewById(radioButtonId).setVisibility(visibility);
        return visibility == View.VISIBLE;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Activity activity = getActivity();

        switch (requestCode) {
            case REQUEST_PROVISION_MANAGED_PROFILE:
            case REQUEST_PROVISION_DEVICE_OWNER:
                if (resultCode == Activity.RESULT_OK) {
                    // Success, finish the enclosing activity. NOTE: Only finish once we're done
                    // here, as in synchronous auth cases we don't want the user to return to the
                    // Android setup wizard or add-account flow prematurely.
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                } else {
                    // Something went wrong (either provisioning failed, or the user backed out).
                    // Let the user decide how to proceed.
                    Toast.makeText(activity, R.string.provisioning_failed_or_cancelled,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_GET_LOGO:
                if (data != null && data.getData() != null) {
                    mLogoUri = data.getData();
                    mLogoValue.setText(mLogoUri.getLastPathSegment());
                    Util.updateImageView(getActivity(), mLogoPreviewView, mLogoUri);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.params_indicator :
                if (mParamsView.getVisibility() == View.VISIBLE) {
                    mParamsView.setVisibility(View.GONE);
                    mParamsIndicator.setImageState(STATE_COLLAPSED, false);
                } else {
                    mParamsIndicator.setImageState(STATE_EXPANDED, false);
                    mParamsView.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.color_select_button:
                ColorPicker.newInstance(mCurrentColor, FRAGMENT_TAG, "provisioningColor")
                        .show(getFragmentManager(), "colorPicker");
                break;
            case R.id.logo_select_button:
                startActivityForResult(getGetContentIntent(), REQUEST_GET_LOGO);
                break;
        }
    }

    @Override
    public void onColorSelected(int colorValue, String id) {
        mCurrentColor = colorValue;
        mColorValue.setText(String.format(ColorPicker.COLOR_STRING_FORMATTER, colorValue));
        mColorPreviewView.setImageTintList(ColorStateList.valueOf(colorValue));
    }

    @Override
    public void onNavigateBack() {
        getActivity().onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        if (mSetupOptions.getCheckedRadioButtonId() == R.id.setup_managed_profile) {
            maybeLaunchProvisioning(ACTION_PROVISION_MANAGED_PROFILE,
                    REQUEST_PROVISION_MANAGED_PROFILE);
        } else {
            maybeLaunchProvisioning(ACTION_PROVISION_MANAGED_DEVICE,
                    REQUEST_PROVISION_DEVICE_OWNER);
        }
    }

    private Intent getGetContentIntent() {
        Intent getContent = new Intent(Intent.ACTION_GET_CONTENT);
        getContent.setType("image/*");
        return getContent;
    }

    private boolean canAnAppHandleGetContent() {
        return getGetContentIntent().resolveActivity(getActivity().getPackageManager()) != null;
    }
}
