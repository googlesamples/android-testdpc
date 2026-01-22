/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.setupdesign;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.setupcompat.util.ForceTwoPaneHelper;
import com.google.android.setupdesign.template.RecyclerMixin;

/**
 * A layout to be used with {@code PreferenceFragment} in v14 support library. This can be specified
 * as the {@code android:layout} in the {@code app:preferenceFragmentStyle} in {@code
 * app:preferenceTheme}.
 *
 * <p>Example:
 *
 * <pre
 * &lt;style android:name="MyActivityTheme">
 *     &lt;item android:name="preferenceTheme">@style/MyPreferenceTheme&lt;/item>
 * &lt;/style>
 *
 * &lt;style android:name="MyPreferenceTheme">
 *     &lt;item android:name="preferenceFragmentStyle">@style/MyPreferenceFragmentStyle&lt;/item>
 * &lt;/style>
 *
 * &lt;style android:name="MyPreferenceFragmentStyle">
 *     &lt;item android:name="android:layout">@layout/my_preference_layout&lt;/item>
 * &lt;/style>
 * </pre>
 *
 * where {@code my_preference_layout} is a layout that contains {@link
 * com.google.android.setupdesign.GlifPreferenceLayout}.
 *
 * <p>Example:
 *
 * <pre>
 * &lt;com.google.android.setupdesign.GlifPreferenceLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:id="@id/list_container"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * </pre>
 *
 * <p>Fragments using this layout <em>must</em> delegate {@code onCreateRecyclerView} to the
 * implementation in this class: {@link #onCreateRecyclerView(android.view.LayoutInflater,
 * android.view.ViewGroup, android.os.Bundle)}
 */
public class GlifPreferenceLayout extends GlifRecyclerLayout {

  public GlifPreferenceLayout(Context context) {
    super(context);
  }

  public GlifPreferenceLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
  }

  public GlifPreferenceLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public GlifPreferenceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = R.id.sud_layout_content;
    }
    return super.findContainer(containerId);
  }

  /** This method must be called in {@code PreferenceFragment#onCreateRecyclerView}. */
  public RecyclerView onCreateRecyclerView(
      LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
    return recyclerMixin.getRecyclerView();
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, int template) {
    if (template == 0) {
      template = R.layout.sud_glif_preference_template;

      // if the activity is embedded should apply an embedded layout.
      if (isEmbeddedActivityOnePaneEnabled(getContext())) {
        if (isGlifExpressiveEnabled()) {
          template = R.layout.sud_glif_expressive_preference_embedded_template;
        } else {
          template = R.layout.sud_glif_preference_embedded_template;
        }
        // TODO add unit test for this case.
      } else if (isGlifExpressiveEnabled()) {
        template = R.layout.sud_glif_expressive_preference_template;
      } else if (ForceTwoPaneHelper.isForceTwoPaneEnable(getContext())) {
        template = R.layout.sud_glif_preference_template_two_pane;
      }
    }
    return super.onInflateTemplate(inflater, template);
  }

  @Override
  protected void onTemplateInflated() {
    // Inflate the recycler view here, so attributes on the decoration views can be applied
    // immediately.
    final LayoutInflater inflater = LayoutInflater.from(getContext());
    int recyclerViewLayoutId = R.layout.sud_glif_preference_recycler_view;
    if (ForceTwoPaneHelper.isForceTwoPaneEnable(getContext())) {
      // Use the compat two pane layout for the recycler view if the two pane is enabled. Since the
      // sud_glif_preference_recycler_view layout is not compatible with original layout.
      recyclerViewLayoutId = R.layout.sud_glif_preference_recycler_view_compat_two_pane;
    }
    RecyclerView recyclerView = (RecyclerView) inflater.inflate(recyclerViewLayoutId, this, false);
    recyclerMixin = new RecyclerMixin(this, recyclerView);
  }
}
