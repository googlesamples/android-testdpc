/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.afwsamples.testdpc.common.preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import androidx.annotation.Nullable;

/** Base class for displaying a dialog when clicked upon. */
public abstract class DpcDialogPreference extends DpcPreference {

  private DpcDialogFragment mFragment;

  public DpcDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onClick() {
    super.onClick();
    showDialog();
  }

  protected void showDialog() {
    Activity activity = getActivity(getContext());
    if (activity == null || activity.isFinishing()) {
      return;
    }

    mFragment = new DpcDialogFragment();
    mFragment.setPreference(this);
    mFragment.show(activity.getFragmentManager(), getKey());
  }

  protected void onDialogShown(AlertDialog dialog) {}

  protected abstract void configureDialog(AlertDialog.Builder builder, Activity activity);

  @Nullable
  protected Activity getActivity(Context context) {
    while (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        return (Activity) context;
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    return null;
  }

  /** A static nested class that extends {@link DialogFragment} to display a dialog. */
  public static class DpcDialogFragment extends DialogFragment {
    private DpcDialogPreference mPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    public void setPreference(DpcDialogPreference preference) {
      mPreference = preference;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      if (mPreference != null) {
        mPreference.configureDialog(builder, getActivity());
      }
      return builder.create();
    }

    @Override
    public void onStart() {
      super.onStart();
      if (mPreference != null && getDialog() != null) {
        mPreference.onDialogShown((AlertDialog) getDialog());
      }
    }

    @Override
    public void onDestroyView() {
      if (getDialog() != null && getRetainInstance()) {
        getDialog().setDismissMessage(null);
      }
      super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
      super.onDismiss(dialog);
      if (mPreference != null) {
        mPreference.mFragment = null;
      }
    }
  }
}
