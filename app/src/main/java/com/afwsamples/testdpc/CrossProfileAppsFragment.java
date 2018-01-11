package com.afwsamples.testdpc;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.pm.crossprofile.CrossProfileApps;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afwsamples.testdpc.common.ReflectionUtil;

import java.util.List;

// TODO: Replace it with P.
@TargetApi(28)
public class CrossProfileAppsFragment extends Fragment {
    private static final String TAG = "CrossProfileAppsFragmen";

    private View mInflatedView;
    private TextView mSwitchProfileTextView;
    private TextView mDescriptionTextView;
    private ImageView mSwitchProfileImageView;
    private CrossProfileApps mCrossProfileApps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mInflatedView = inflater.inflate(R.layout.cross_profile_apps, container, false);
        mSwitchProfileTextView = mInflatedView.findViewById(R.id.cross_profile_app_label);
        mSwitchProfileImageView = mInflatedView.findViewById(R.id.cross_profile_app_icon);
        mDescriptionTextView = mInflatedView.findViewById(R.id.cross_profile_app_description);
        return mInflatedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCrossProfileApps = getActivity().getSystemService(CrossProfileApps.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUi();
    }

    private void refreshUi() {
        List<UserHandle> targetUserProfiles = mCrossProfileApps.getTargetUserProfiles();
        if (targetUserProfiles.isEmpty()) {
            showNoTargetUserUi();
        } else {
            showHasTargetUserUi(targetUserProfiles.get(0));
        }
    }

    private void showNoTargetUserUi() {
        mDescriptionTextView.setText(R.string.cross_profile_apps_not_available);
        mSwitchProfileTextView.setText("");
        mSwitchProfileImageView.setImageDrawable(null);
        mSwitchProfileImageView.setOnClickListener(null);
    }

    private void showHasTargetUserUi(UserHandle userHandle) {
        mSwitchProfileTextView.setText(mCrossProfileApps.getProfileSwitchingLabel(userHandle));
        mSwitchProfileImageView.setImageDrawable(
                mCrossProfileApps.getProfileSwitchingIcon(userHandle));
        mDescriptionTextView.setText(R.string.cross_profile_apps_available);
        mSwitchProfileImageView.setOnClickListener(
                view -> startMainActivity(
                        new ComponentName(getActivity(),
                            PolicyManagementActivity.class),
                            userHandle));
    }

    private void startMainActivity(ComponentName componentName, UserHandle userHandle) {
        try {
            ReflectionUtil.invoke(mCrossProfileApps,
                    "startMainActivity",
                    componentName,
                    userHandle);
        } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
            Log.e(TAG, "startMainActivity: ", e);
        }
    }
}
