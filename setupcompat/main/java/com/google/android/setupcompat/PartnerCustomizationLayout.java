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

package com.google.android.setupcompat;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.internal.FocusChangedMetricHelper;
import com.google.android.setupcompat.internal.LifecycleFragment;
import com.google.android.setupcompat.internal.PersistableBundles;
import com.google.android.setupcompat.internal.SetupCompatServiceInvoker;
import com.google.android.setupcompat.internal.TemplateLayout;
import com.google.android.setupcompat.logging.CustomEvent;
import com.google.android.setupcompat.logging.LoggingObserver;
import com.google.android.setupcompat.logging.LoggingObserver.SetupCompatUiEvent.LayoutInflatedEvent;
import com.google.android.setupcompat.logging.MetricKey;
import com.google.android.setupcompat.logging.SetupMetricsLogger;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.FooterButton;
import com.google.android.setupcompat.template.StatusBarMixin;
import com.google.android.setupcompat.template.SystemNavBarMixin;
import com.google.android.setupcompat.util.BuildCompatUtils;
import com.google.android.setupcompat.util.Logger;
import com.google.android.setupcompat.util.WizardManagerHelper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** A templatization layout with consistent style used in Setup Wizard or app itself. */
public class PartnerCustomizationLayout extends TemplateLayout {

  private static final Logger LOG = new Logger("PartnerCustomizationLayout");

  /**
   * Attribute indicating whether usage of partner theme resources is allowed. This corresponds to
   * the {@code app:sucUsePartnerResource} XML attribute. Note that when running in setup wizard,
   * this is always overridden to true.
   */
  private boolean usePartnerResourceAttr;

  /**
   * Attribute indicating whether using full dynamic colors or not. This corresponds to the {@code
   * app:sucFullDynamicColor} XML attribute.
   */
  private boolean useFullDynamicColorAttr;

  /**
   * Attribute indicating whether usage of dynamic is allowed. This corresponds to the existence of
   * {@code app:sucFullDynamicColor} XML attribute.
   */
  private boolean useDynamicColor;

  private Activity activity;

  private PersistableBundle layoutTypeBundle;

  @CanIgnoreReturnValue
  public PartnerCustomizationLayout(Context context) {
    this(context, 0, 0);
  }

  @CanIgnoreReturnValue
  public PartnerCustomizationLayout(Context context, int template) {
    this(context, template, 0);
  }

  @CanIgnoreReturnValue
  public PartnerCustomizationLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
    init(null, R.attr.sucLayoutTheme);
  }

  @CanIgnoreReturnValue
  public PartnerCustomizationLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, R.attr.sucLayoutTheme);
  }

  @CanIgnoreReturnValue
  public PartnerCustomizationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  @VisibleForTesting
  final ViewTreeObserver.OnWindowFocusChangeListener windowFocusChangeListener =
      this::onFocusChanged;

  private void init(AttributeSet attrs, int defStyleAttr) {
    if (isInEditMode()) {
      return;
    }

    TypedArray a =
        getContext()
            .obtainStyledAttributes(
                attrs, R.styleable.SucPartnerCustomizationLayout, defStyleAttr, 0);

    boolean layoutFullscreen =
        a.getBoolean(R.styleable.SucPartnerCustomizationLayout_sucLayoutFullscreen, true);

    a.recycle();

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && layoutFullscreen) {
      setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    registerMixin(
        StatusBarMixin.class, new StatusBarMixin(this, activity.getWindow(), attrs, defStyleAttr));
    registerMixin(SystemNavBarMixin.class, new SystemNavBarMixin(this, activity.getWindow()));
    registerMixin(FooterBarMixin.class, new FooterBarMixin(this, attrs, defStyleAttr));

    getMixin(SystemNavBarMixin.class).applyPartnerCustomizations(attrs, defStyleAttr);

    // Override the FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, FLAG_TRANSLUCENT_STATUS,
    // FLAG_TRANSLUCENT_NAVIGATION and SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN attributes of window forces
    // showing status bar and navigation bar.
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, int template) {
    if (template == 0) {
      template = R.layout.partner_customization_layout;
    }
    return inflateTemplate(inflater, 0, template);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method sets all these flags before onTemplateInflated since it will be too late and get
   * incorrect flag value on PartnerCustomizationLayout if sets them after onTemplateInflated.
   */
  @Override
  protected void onBeforeTemplateInflated(AttributeSet attrs, int defStyleAttr) {

    // Sets default value to true since this timing
    // before PartnerCustomization members initialization
    usePartnerResourceAttr = true;

    activity = lookupActivityFromContext(getContext());

    boolean isSetupFlow = WizardManagerHelper.isAnySetupWizard(activity.getIntent());

    TypedArray a =
        getContext()
            .obtainStyledAttributes(
                attrs, R.styleable.SucPartnerCustomizationLayout, defStyleAttr, 0);

    if (!a.hasValue(R.styleable.SucPartnerCustomizationLayout_sucUsePartnerResource)) {
      // TODO: Enable Log.WTF after other client already set sucUsePartnerResource.
      LOG.e("Attribute sucUsePartnerResource not found in " + activity.getComponentName());
    }

    usePartnerResourceAttr =
        isSetupFlow
            || a.getBoolean(R.styleable.SucPartnerCustomizationLayout_sucUsePartnerResource, true);

    useDynamicColor = a.hasValue(R.styleable.SucPartnerCustomizationLayout_sucFullDynamicColor);
    useFullDynamicColorAttr =
        a.getBoolean(R.styleable.SucPartnerCustomizationLayout_sucFullDynamicColor, false);

    a.recycle();

    LOG.atDebug(
        "activity="
            + activity.getClass().getSimpleName()
            + " isSetupFlow="
            + isSetupFlow
            + " enablePartnerResourceLoading="
            + enablePartnerResourceLoading()
            + " usePartnerResourceAttr="
            + usePartnerResourceAttr
            + " useDynamicColor="
            + useDynamicColor
            + " useFullDynamicColorAttr="
            + useFullDynamicColorAttr);
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = R.id.suc_layout_content;
    }
    return super.findContainer(containerId);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    LifecycleFragment.attachNow(activity);
    if (WizardManagerHelper.isAnySetupWizard(activity.getIntent())) {
      getViewTreeObserver().addOnWindowFocusChangeListener(windowFocusChangeListener);
    }
    getMixin(FooterBarMixin.class).onAttachedToWindow();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (VERSION.SDK_INT >= Build.VERSION_CODES.Q
        && WizardManagerHelper.isAnySetupWizard(activity.getIntent())) {
      FooterBarMixin footerBarMixin = getMixin(FooterBarMixin.class);
      footerBarMixin.onDetachedFromWindow();
      FooterButton primaryButton = footerBarMixin.getPrimaryButton();
      FooterButton secondaryButton = footerBarMixin.getSecondaryButton();
      PersistableBundle primaryButtonMetrics =
          primaryButton != null
              ? primaryButton.getMetrics("PrimaryFooterButton")
              : PersistableBundle.EMPTY;
      PersistableBundle secondaryButtonMetrics =
          secondaryButton != null
              ? secondaryButton.getMetrics("SecondaryFooterButton")
              : PersistableBundle.EMPTY;

      PersistableBundle layoutTypeMetrics =
          (layoutTypeBundle != null) ? layoutTypeBundle : PersistableBundle.EMPTY;

      PersistableBundle persistableBundle =
          PersistableBundles.mergeBundles(
              footerBarMixin.getLoggingMetrics(),
              primaryButtonMetrics,
              secondaryButtonMetrics,
              layoutTypeMetrics);

      SetupMetricsLogger.logCustomEvent(
          getContext(),
          CustomEvent.create(MetricKey.get("SetupCompatMetrics", activity), persistableBundle));
    }
    getViewTreeObserver().removeOnWindowFocusChangeListener(windowFocusChangeListener);
  }

  /**
   * PartnerCustomizationLayout is a template layout for different type of GlifLayout. This method
   * allows each type of layout to report its "GlifLayoutType".
   */
  public void setLayoutTypeMetrics(PersistableBundle bundle) {
    this.layoutTypeBundle = bundle;
  }

  /** Returns a {@link PersistableBundle} contains key "GlifLayoutType". */
  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  public PersistableBundle getLayoutTypeMetrics() {
    return this.layoutTypeBundle;
  }

  public static Activity lookupActivityFromContext(Context context) {
    return PartnerConfigHelper.lookupActivityFromContext(context);
  }

  /**
   * Returns true if partner resource loading is enabled. If true, and other necessary conditions
   * for loading theme attributes are met, this layout will use customized theme attributes from OEM
   * overlays. This is intended to be used with flag-based development, to allow a flag to control
   * the rollout of partner resource loading.
   */
  protected boolean enablePartnerResourceLoading() {
    return true;
  }

  /** Returns if the current layout/activity applies partner customized configurations or not. */
  public boolean shouldApplyPartnerResource() {
    if (!enablePartnerResourceLoading()) {
      return false;
    }
    if (!usePartnerResourceAttr) {
      return false;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      return false;
    }
    if (!PartnerConfigHelper.get(getContext()).isAvailable()) {
      return false;
    }
    return true;
  }

  /**
   * Returns {@code true} if the current layout/activity applies dynamic color. Otherwise, returns
   * {@code false}.
   */
  public boolean shouldApplyDynamicColor() {
    if (!BuildCompatUtils.isAtLeastS()) {
      return false;
    }

    if (!PartnerConfigHelper.get(getContext()).isAvailable()) {
      return false;
    }

    // If the dynamic theme is applied, useDynamicColor would be true and shouldApplyDynamicColor
    // would return true.
    if (useDynamicColor) {
      return true;
    }
    if (!PartnerConfigHelper.isSetupWizardDynamicColorEnabled(getContext())) {
      return false;
    }
    return true;
  }

  /**
   * Returns {@code true} if the current layout/activity applies full dynamic color. Otherwise,
   * returns {@code false}. This method combines the result of {@link #shouldApplyDynamicColor()},
   * the value of the {@code app:sucFullDynamicColor}, and the result of {@link
   * PartnerConfigHelper#isSetupWizardFullDynamicColorEnabled(Context)}.
   */
  public boolean useFullDynamicColor() {
    return shouldApplyDynamicColor()
        && (useFullDynamicColorAttr
            || PartnerConfigHelper.isSetupWizardFullDynamicColorEnabled(getContext()));
  }

  /**
   * Sets a logging observer for {@link FooterBarMixin}. The logging observer is used to log UI
   * events (e.g. page impressions and button clicks) on the layout and footer bar buttons.
   */
  public void setLoggingObserver(LoggingObserver loggingObserver) {
    loggingObserver.log(new LayoutInflatedEvent(this));
    getMixin(FooterBarMixin.class).setLoggingObserver(loggingObserver);
  }

  /**
   * Invoke the method onFocusStatusChanged when onWindowFocusChangeListener receive onFocusChanged.
   */
  private void onFocusChanged(boolean hasFocus) {
    SetupCompatServiceInvoker.get(getContext())
        .onFocusStatusChanged(
            FocusChangedMetricHelper.getScreenName(activity),
            FocusChangedMetricHelper.getExtraBundle(
                activity, PartnerCustomizationLayout.this, hasFocus));
  }
}
