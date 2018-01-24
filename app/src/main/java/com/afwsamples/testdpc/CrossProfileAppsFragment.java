package com.afwsamples.testdpc;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.CrossProfileApps;
import android.graphics.drawable.Drawable;
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
    private Object mCrossProfileApps;

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
        mCrossProfileApps = getActivity().getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUi();
    }

    private void refreshUi() {
        try {
            List<UserHandle> targetUserProfiles =
                    (List<UserHandle>) ReflectionUtil.invoke(
                            mCrossProfileApps,
                            "getTargetUserProfiles");
            if (targetUserProfiles.isEmpty()) {
                showNoTargetUserUi();
            } else {
                showHasTargetUserUi(targetUserProfiles.get(0));
            }
        } catch (ReflectionUtil.ReflectionIsTemporaryException ex) {
            Log.e(TAG, "Failed to call CrossProfileApps API: ", ex);
        }
    }

    private void showNoTargetUserUi() {
        mDescriptionTextView.setText(R.string.cross_profile_apps_not_available);
        mSwitchProfileTextView.setText("");
        mSwitchProfileImageView.setImageDrawable(null);
        mSwitchProfileImageView.setOnClickListener(null);
    }

    private void showHasTargetUserUi(UserHandle userHandle)
            throws ReflectionUtil.ReflectionIsTemporaryException {
        mSwitchProfileTextView.setText((String)
                ReflectionUtil.invoke(
                        mCrossProfileApps, "getProfileSwitchingLabel", userHandle));
        mSwitchProfileImageView.setImageDrawable((Drawable) ReflectionUtil.invoke(
                mCrossProfileApps, "getProfileSwitchingIconDrawable", userHandle));
        mDescriptionTextView.setText(R.string.cross_profile_apps_available);
        mSwitchProfileImageView.setOnClickListener(
                view -> {
                    try {
                        ReflectionUtil.invoke(
                                mCrossProfileApps, "startMainActivity",
                                new ComponentName(getActivity(), PolicyManagementActivity.class),
                                userHandle);
                    } catch (ReflectionUtil.ReflectionIsTemporaryException e) {
                        e.printStackTrace();
                    }
                });
    }
}
