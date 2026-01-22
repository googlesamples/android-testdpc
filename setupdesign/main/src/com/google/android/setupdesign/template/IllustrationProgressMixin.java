/*
 * Copyright (C) 2020 The Android Open Source Project
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

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfig.ResourceType;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.partnerconfig.ResourceEntry;
import com.google.android.setupcompat.template.Mixin;
import com.google.android.setupcompat.util.Logger;
import com.google.android.setupdesign.GlifLayout;
import com.google.android.setupdesign.R;
import com.google.android.setupdesign.view.IllustrationVideoView;

// TODO: remove this mixin after migrate to new GlifLoadingLayout
/**
 * A {@link Mixin} for showing a progress illustration.
 *
 * @deprecated Will be replaced by GlifLoadingLayout.
 */
@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
@Deprecated
public class IllustrationProgressMixin implements Mixin {
  private static final Logger LOG = new Logger(IllustrationProgressMixin.class);

  private final GlifLayout glifLayout;
  private final Context context;

  private ProgressConfig progressConfig = ProgressConfig.CONFIG_DEFAULT;
  private String progressDescription;

  public IllustrationProgressMixin(GlifLayout layout) {
    this.glifLayout = layout;
    context = layout.getContext();
  }

  /**
   * Sets whether the progress layout is shown. If the progress layout has not been inflated from
   * the stub, this method will inflate the progress layout.
   *
   * @param shown True to show the progress layout, false to set the layout visibiltiy to {@code
   *     GONE}
   */
  public void setShown(boolean shown) {
    LOG.atInfo("setShown(" + shown + ")");
    if (!shown) {
      View view = peekProgressIllustrationLayout();
      if (view != null) {
        view.setVisibility(GONE);
      }
    } else {
      View view = getProgressIllustrationLayout();
      if (view != null) {
        view.setVisibility(VISIBLE);

        if (progressDescription != null) {
          TextView descriptionView = view.findViewById(R.id.sud_layout_description);
          if (descriptionView != null) {
            descriptionView.setVisibility(VISIBLE);
            descriptionView.setText(progressDescription);
          }
        }
      }
    }
  }

  /** Returns true if the progress layout is currently shown. */
  public boolean isShown() {
    View view = peekProgressIllustrationLayout();
    return view != null && view.getVisibility() == VISIBLE;
  }

  /**
   * Sets the type of progress illustration.
   *
   * @param config {@link ProgressConfig}
   */
  public void setProgressConfig(ProgressConfig config) {
    this.progressConfig = config;

    // When ViewStub not inflated, do nothing. It will set illustration resource when inflate
    // layout.
    if (peekProgressIllustrationLayout() != null) {
      setIllustrationResource();
    }
  }

  /**
   * Sets the description text of progress
   * @param description the description text.
   */
  public void setProgressIllustrationDescription(String description) {
    progressDescription = description;

    if (isShown()) {
      final View progressLayout = getProgressIllustrationLayout();
      if (progressLayout != null) {
        TextView descriptionView = progressLayout.findViewById(R.id.sud_layout_description);
        if (description != null) {
          descriptionView.setVisibility(VISIBLE);
          descriptionView.setText(description);
        } else {
          descriptionView.setVisibility(INVISIBLE);
          descriptionView.setText(description);
        }
      }
    }
  }

  @Nullable private View getProgressIllustrationLayout() {
    final View progressLayout = peekProgressIllustrationLayout();
    if (progressLayout == null) {
      final ViewStub viewStub =
          glifLayout.findManagedViewById(R.id.sud_layout_illustration_progress_stub);

      if (viewStub != null) {
        viewStub.inflate();
        setIllustrationResource();
      }
    }

    return peekProgressIllustrationLayout();
  }

  private void setIllustrationResource() {
    IllustrationVideoView illustrationVideoView =
        glifLayout.findManagedViewById(R.id.sud_progress_illustration);
    ProgressBar progressBar = glifLayout.findManagedViewById(R.id.sud_progress_bar);

    PartnerConfigHelper partnerConfigHelper = PartnerConfigHelper.get(context);
    ResourceEntry resourceEntry =
        partnerConfigHelper.getIllustrationResourceEntry(
            context, progressConfig.getPartnerConfig());

    if (resourceEntry != null) {
      progressBar.setVisibility(GONE);
      illustrationVideoView.setVisibility(VISIBLE);
      illustrationVideoView.setVideoResourceEntry(resourceEntry);
    } else {
      progressBar.setVisibility(VISIBLE);
      illustrationVideoView.setVisibility(GONE);
    }
  }

  @Nullable private View peekProgressIllustrationLayout() {
    return glifLayout.findViewById(R.id.sud_layout_progress_illustration);
  }

  /** The progress config used to maps to different animation */
  public enum ProgressConfig {
    CONFIG_DEFAULT(PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_DEFAULT),
    CONFIG_ACCOUNT(PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_ACCOUNT),
    CONFIG_CONNECTION(PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_CONNECTION),
    CONFIG_UPDATE(PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_UPDATE);

    private final PartnerConfig config;

    ProgressConfig(PartnerConfig config) {
      if (config.getResourceType() != ResourceType.ILLUSTRATION) {
        throw new IllegalArgumentException(
            "Illustration progress only allow illustration resource");
      }
      this.config = config;
    }

    PartnerConfig getPartnerConfig() {
      return config;
    }
  }
}
