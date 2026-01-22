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

package com.google.android.setupdesign;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.window.embedding.ActivityEmbeddingController;
import com.google.android.setupcompat.PartnerCustomizationLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.StatusBarMixin;
import com.google.android.setupcompat.util.ForceTwoPaneHelper;
import com.google.android.setupcompat.util.KeyboardHelper;
import com.google.android.setupcompat.util.Logger;
import com.google.android.setupdesign.template.DescriptionMixin;
import com.google.android.setupdesign.template.FloatingBackButtonMixin;
import com.google.android.setupdesign.template.HeaderMixin;
import com.google.android.setupdesign.template.IconMixin;
import com.google.android.setupdesign.template.IllustrationProgressMixin;
import com.google.android.setupdesign.template.ProfileMixin;
import com.google.android.setupdesign.template.ProgressBarMixin;
import com.google.android.setupdesign.template.RequireScrollMixin;
import com.google.android.setupdesign.template.ScrollViewScrollHandlingDelegate;
import com.google.android.setupdesign.util.DescriptionStyler;
import com.google.android.setupdesign.util.LayoutStyler;
import com.google.android.setupdesign.view.BottomScrollView;
import com.google.android.setupdesign.view.BottomScrollView.BottomScrollListener;

/**
 * Layout for the GLIF theme used in Setup Wizard for N.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * &lt;com.google.android.setupdesign.GlifLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:icon="@drawable/my_icon"
 *     app:sucHeaderText="@string/my_title">
 *
 *     &lt;!-- Content here -->
 *
 * &lt;/com.google.android.setupdesign.GlifLayout>
 * }</pre>
 */
public class GlifLayout extends PartnerCustomizationLayout {

  private static final Logger LOG = new Logger(GlifLayout.class);
  private static final int VANILLA_ICE_CREAM = 35;

  private ColorStateList primaryColor;

  private boolean backgroundPatterned = true;

  private boolean applyPartnerHeavyThemeResource = false;

  /** The color of the background. If null, the color will inherit from primaryColor. */
  @Nullable private ColorStateList backgroundBaseColor;

  public GlifLayout(Context context) {
    this(context, 0, 0);
  }

  public GlifLayout(Context context, int template) {
    this(context, template, 0);
  }

  public GlifLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
    init(null, R.attr.sudLayoutTheme);
  }

  public GlifLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, R.attr.sudLayoutTheme);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public GlifLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  // All the constructors delegate to this init method. The 3-argument constructor is not
  // available in LinearLayout before v11, so call super with the exact same arguments.
  private void init(AttributeSet attrs, int defStyleAttr) {
    if (isInEditMode()) {
      return;
    }

    TypedArray a =
        getContext().obtainStyledAttributes(attrs, R.styleable.SudGlifLayout, defStyleAttr, 0);
    boolean usePartnerHeavyTheme =
        a.getBoolean(R.styleable.SudGlifLayout_sudUsePartnerHeavyTheme, false);
    applyPartnerHeavyThemeResource = shouldApplyPartnerResource() && usePartnerHeavyTheme;

    registerMixin(HeaderMixin.class, new HeaderMixin(this, attrs, defStyleAttr));
    registerMixin(DescriptionMixin.class, new DescriptionMixin(this, attrs, defStyleAttr));
    registerMixin(IconMixin.class, new IconMixin(this, attrs, defStyleAttr));
    registerMixin(ProfileMixin.class, new ProfileMixin(this, attrs, defStyleAttr));
    registerMixin(ProgressBarMixin.class, new ProgressBarMixin(this, attrs, defStyleAttr));
    registerMixin(IllustrationProgressMixin.class, new IllustrationProgressMixin(this));
    registerMixin(
        FloatingBackButtonMixin.class, new FloatingBackButtonMixin(this, attrs, defStyleAttr));
    final RequireScrollMixin requireScrollMixin = new RequireScrollMixin(this);
    registerMixin(RequireScrollMixin.class, requireScrollMixin);

    final ScrollView scrollView = getScrollView();
    if (scrollView != null) {
      requireScrollMixin.setScrollHandlingDelegate(
          new ScrollViewScrollHandlingDelegate(requireScrollMixin, scrollView));
    }

    ColorStateList primaryColor = a.getColorStateList(R.styleable.SudGlifLayout_sudColorPrimary);
    if (primaryColor != null) {
      setPrimaryColor(primaryColor);
    }
    if (shouldApplyPartnerHeavyThemeResource()) {
      updateContentBackgroundColorWithPartnerConfig();
    }

    View view = findManagedViewById(R.id.sud_layout_content);
    if (view != null) {
      if (shouldApplyPartnerResource()) {
        // The margin of content is defined by @style/SudContentFrame. The Setupdesign library
        // cannot obtain the content resource ID of the client, so the value of the content margin
        // cannot be adjusted through GlifLayout. If the margin sides are changed through the
        // partner config, it can only be based on the increased or decreased value to adjust the
        // value of padding. In this way, the value of content margin plus padding will be equal to
        // the value of partner config.
        LayoutStyler.applyPartnerCustomizationExtraPaddingStyle(view);
      }

      // {@class GlifPreferenceLayout} Inherited from {@class GlifRecyclerLayout}. The API would
      // be called twice from GlifRecyclerLayout and GlifLayout, so it should skip the API here
      // when the instance is GlifPreferenceLayout.
      if (!(this instanceof GlifPreferenceLayout)) {
        tryApplyPartnerCustomizationContentPaddingTopStyle(view);
      }
    }

    updateLandscapeMiddleHorizontalSpacing();

    updateViewFocusable();

    ColorStateList backgroundColor =
        a.getColorStateList(R.styleable.SudGlifLayout_sudBackgroundBaseColor);
    setBackgroundBaseColor(backgroundColor);

    boolean backgroundPatterned =
        a.getBoolean(R.styleable.SudGlifLayout_sudBackgroundPatterned, true);
    setBackgroundPatterned(backgroundPatterned);

    final int stickyHeader = a.getResourceId(R.styleable.SudGlifLayout_sudStickyHeader, 0);
    if (stickyHeader != 0) {
      inflateStickyHeader(stickyHeader);
    }

    if (PartnerConfigHelper.isGlifExpressiveEnabled(getContext())) {
      initScrollingListener();
    }

    initBackButton();

    a.recycle();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    getMixin(IconMixin.class).tryApplyPartnerCustomizationStyle();
    getMixin(HeaderMixin.class).tryApplyPartnerCustomizationStyle();
    getMixin(DescriptionMixin.class).tryApplyPartnerCustomizationStyle();
    getMixin(ProgressBarMixin.class).tryApplyPartnerCustomizationStyle();
    getMixin(ProfileMixin.class).tryApplyPartnerCustomizationStyle();
    getMixin(FloatingBackButtonMixin.class).tryApplyPartnerCustomizationStyle();
    tryApplyPartnerCustomizationStyleToShortDescription();
  }

  private void updateViewFocusable() {
    if (KeyboardHelper.isKeyboardFocusEnhancementEnabled(getContext())) {
      View headerView = this.findManagedViewById(R.id.sud_header_scroll_view);
      if (headerView != null) {
        headerView.setFocusable(false);
      }
      View view = this.findManagedViewById(R.id.sud_scroll_view);
      if (view != null) {
        view.setFocusable(false);
      }
    }
  }

  // TODO: remove when all sud_layout_description has migrated to
  // DescriptionMixin(sud_layout_subtitle)
  private void tryApplyPartnerCustomizationStyleToShortDescription() {
    TextView description = this.findManagedViewById(R.id.sud_layout_description);
    if (description != null) {
      if (applyPartnerHeavyThemeResource) {
        DescriptionStyler.applyPartnerCustomizationHeavyStyle(description);
      } else if (shouldApplyPartnerResource()) {
        DescriptionStyler.applyPartnerCustomizationLightStyle(description);
      }
    }
  }

  protected void updateLandscapeMiddleHorizontalSpacing() {
    int horizontalSpacing =
        getResources().getDimensionPixelSize(R.dimen.sud_glif_land_middle_horizontal_spacing);
    if (shouldApplyPartnerResource()
        && PartnerConfigHelper.get(getContext())
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAND_MIDDLE_HORIZONTAL_SPACING)) {
      horizontalSpacing =
          (int)
              PartnerConfigHelper.get(getContext())
                  .getDimension(getContext(), PartnerConfig.CONFIG_LAND_MIDDLE_HORIZONTAL_SPACING);
    }

    View headerView = this.findManagedViewById(R.id.sud_landscape_header_area);
    if (headerView != null) {
      int layoutMarginEnd;
      if (shouldApplyPartnerResource()
          && PartnerConfigHelper.get(getContext())
              .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_END)) {
        layoutMarginEnd =
            (int)
                PartnerConfigHelper.get(getContext())
                    .getDimension(getContext(), PartnerConfig.CONFIG_LAYOUT_MARGIN_END);
      } else {
        TypedArray a = getContext().obtainStyledAttributes(new int[] {R.attr.sudMarginEnd});
        layoutMarginEnd = a.getDimensionPixelSize(0, 0);
        a.recycle();
      }
      int paddingEnd = (horizontalSpacing / 2) - layoutMarginEnd;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        headerView.setPadding(
            headerView.getPaddingStart(),
            headerView.getPaddingTop(),
            paddingEnd,
            headerView.getPaddingBottom());
      } else {
        headerView.setPadding(
            headerView.getPaddingLeft(),
            headerView.getPaddingTop(),
            paddingEnd,
            headerView.getPaddingBottom());
      }
    }

    View contentView = this.findManagedViewById(R.id.sud_landscape_content_area);
    if (contentView != null) {
      int layoutMarginStart;
      if (shouldApplyPartnerResource()
          && PartnerConfigHelper.get(getContext())
              .isPartnerConfigAvailable(PartnerConfig.CONFIG_LAYOUT_MARGIN_START)) {
        layoutMarginStart =
            (int)
                PartnerConfigHelper.get(getContext())
                    .getDimension(getContext(), PartnerConfig.CONFIG_LAYOUT_MARGIN_START);
      } else {
        TypedArray a = getContext().obtainStyledAttributes(new int[] {R.attr.sudMarginStart});
        layoutMarginStart = a.getDimensionPixelSize(0, 0);
        a.recycle();
      }
      int paddingStart = 0;
      if (headerView != null) {
        paddingStart = (horizontalSpacing / 2) - layoutMarginStart;
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        contentView.setPadding(
            paddingStart,
            contentView.getPaddingTop(),
            contentView.getPaddingEnd(),
            contentView.getPaddingBottom());
      } else {
        contentView.setPadding(
            paddingStart,
            contentView.getPaddingTop(),
            contentView.getPaddingRight(),
            contentView.getPaddingBottom());
      }
    }
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, @LayoutRes int template) {
    if (template == 0) {
      template = R.layout.sud_glif_template;

      // if the activity is embedded should apply an embedded layout.
      if (isEmbeddedActivityOnePaneEnabled(getContext())) {
        if (isGlifExpressiveEnabled()) {
          template = R.layout.sud_glif_expressive_embedded_template;
        } else {
          template = R.layout.sud_glif_embedded_template;
        }
        // TODO add unit test for this case.
      } else if (isGlifExpressiveEnabled()) {
        template = R.layout.sud_glif_expressive_template;
      } else if (ForceTwoPaneHelper.isForceTwoPaneEnable(getContext())) {
        template = R.layout.sud_glif_template_two_pane;
      }
    }

    return inflateTemplate(inflater, R.style.SudThemeGlif_Light, template);
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = R.id.sud_layout_content;
    }
    return super.findContainer(containerId);
  }

  /**
   * Sets the sticky header (i.e. header that doesn't scroll) of the layout, which is at the top of
   * the content area outside of the scrolling container. The header can only be inflated once per
   * instance of this layout.
   *
   * @param header The layout to be inflated as the header
   * @return The root of the inflated header view
   */
  public View inflateStickyHeader(@LayoutRes int header) {
    ViewStub stickyHeaderStub = findManagedViewById(R.id.sud_layout_sticky_header);
    stickyHeaderStub.setLayoutResource(header);
    return stickyHeaderStub.inflate();
  }

  public ScrollView getScrollView() {
    final View view = findManagedViewById(R.id.sud_scroll_view);
    return view instanceof ScrollView ? (ScrollView) view : null;
  }

  public TextView getHeaderTextView() {
    return getMixin(HeaderMixin.class).getTextView();
  }

  public void setHeaderText(int title) {
    getMixin(HeaderMixin.class).setText(title);
  }

  public void setHeaderText(CharSequence title) {
    getMixin(HeaderMixin.class).setText(title);
  }

  public CharSequence getHeaderText() {
    return getMixin(HeaderMixin.class).getText();
  }

  public TextView getDescriptionTextView() {
    return getMixin(DescriptionMixin.class).getTextView();
  }

  /**
   * Sets the description text and also sets the text visibility to visible. This can also be set
   * via the XML attribute {@code app:sudDescriptionText}.
   *
   * @param title The resource ID of the text to be set as description
   */
  public void setDescriptionText(@StringRes int title) {
    getMixin(DescriptionMixin.class).setText(title);
  }

  /**
   * Sets the description text and also sets the text visibility to visible. This can also be set
   * via the XML attribute {@code app:sudDescriptionText}.
   *
   * @param title The text to be set as description
   */
  public void setDescriptionText(CharSequence title) {
    getMixin(DescriptionMixin.class).setText(title);
  }

  /** Returns the current description text. */
  public CharSequence getDescriptionText() {
    return getMixin(DescriptionMixin.class).getText();
  }

  public void setHeaderColor(ColorStateList color) {
    getMixin(HeaderMixin.class).setTextColor(color);
  }

  public ColorStateList getHeaderColor() {
    return getMixin(HeaderMixin.class).getTextColor();
  }

  public void setIcon(Drawable icon) {
    getMixin(IconMixin.class).setIcon(icon);
  }

  public Drawable getIcon() {
    return getMixin(IconMixin.class).getIcon();
  }

  /**
   * Sets the visibility of header area in landscape mode. These views includes icon, header title
   * and subtitle. It can make the content view become full screen when set false.
   */
  @TargetApi(Build.VERSION_CODES.S)
  public void setLandscapeHeaderAreaVisible(boolean visible) {
    View view = this.findManagedViewById(R.id.sud_landscape_header_area);
    if (view == null) {
      return;
    }
    if (visible) {
      view.setVisibility(View.VISIBLE);
    } else {
      view.setVisibility(View.GONE);
    }
    updateLandscapeMiddleHorizontalSpacing();
  }

  /**
   * Sets the primary color of this layout, which will be used to determine the color of the
   * progress bar and the background pattern.
   */
  public void setPrimaryColor(@NonNull ColorStateList color) {
    primaryColor = color;
    updateBackground();
    getMixin(ProgressBarMixin.class).setColor(color);
  }

  public ColorStateList getPrimaryColor() {
    return primaryColor;
  }

  /**
   * Sets the base color of the background view, which is the status bar for phones and the full-
   * screen background for tablets. If {@link #isBackgroundPatterned()} is true, the pattern will be
   * drawn with this color.
   *
   * @param color The color to use as the base color of the background. If {@code null}, {@link
   *     #getPrimaryColor()} will be used
   */
  public void setBackgroundBaseColor(@Nullable ColorStateList color) {
    backgroundBaseColor = color;
    updateBackground();
  }

  /**
   * @return The base color of the background. {@code null} indicates the background will be drawn
   *     with {@link #getPrimaryColor()}.
   */
  @Nullable
  public ColorStateList getBackgroundBaseColor() {
    return backgroundBaseColor;
  }

  /**
   * Sets whether the background should be {@link GlifPatternDrawable}. If {@code false}, the
   * background will be a solid color.
   */
  public void setBackgroundPatterned(boolean patterned) {
    backgroundPatterned = patterned;
    updateBackground();
  }

  /** Returns true if this view uses {@link GlifPatternDrawable} as background. */
  public boolean isBackgroundPatterned() {
    return backgroundPatterned;
  }

  private void updateBackground() {
    final View patternBg = findManagedViewById(R.id.suc_layout_status);
    if (patternBg != null) {
      int backgroundColor = 0;
      if (backgroundBaseColor != null) {
        backgroundColor = backgroundBaseColor.getDefaultColor();
      } else if (primaryColor != null) {
        backgroundColor = primaryColor.getDefaultColor();
      }
      Drawable background =
          backgroundPatterned
              ? new GlifPatternDrawable(backgroundColor)
              : new ColorDrawable(backgroundColor);
      getMixin(StatusBarMixin.class).setStatusBarBackground(background);
    }
  }

  public boolean isProgressBarShown() {
    return getMixin(ProgressBarMixin.class).isShown();
  }

  public void setProgressBarShown(boolean shown) {
    getMixin(ProgressBarMixin.class).setShown(shown);
  }

  public ProgressBar peekProgressBar() {
    return getMixin(ProgressBarMixin.class).peekProgressBar();
  }

  /**
   * Returns if the current layout/activity applies heavy partner customized configurations or not.
   */
  public boolean shouldApplyPartnerHeavyThemeResource() {

    return applyPartnerHeavyThemeResource
        || (shouldApplyPartnerResource()
            && PartnerConfigHelper.shouldApplyExtendedPartnerConfig(getContext()));
  }

  /** Check if the one pane layout is enabled in embedded activity */
  protected boolean isEmbeddedActivityOnePaneEnabled(Context context) {
    boolean embeddedActivityOnePaneEnabled =
        PartnerConfigHelper.isEmbeddedActivityOnePaneEnabled(context);
    boolean activityEmbedded =
        ActivityEmbeddingController.getInstance(context)
            .isActivityEmbedded(PartnerCustomizationLayout.lookupActivityFromContext(context));
    LOG.atVerbose(
        "isEmbeddedActivityOnePaneEnabled = "
            + embeddedActivityOnePaneEnabled
            + "; isActivityEmbedded = "
            + activityEmbedded);
    return embeddedActivityOnePaneEnabled && activityEmbedded;
  }

  /** Updates the background color of this layout with the partner-customizable background color. */
  private void updateContentBackgroundColorWithPartnerConfig() {
    // If full dynamic color enabled which means this activity is running outside of setup
    // flow, the colors should refer to R.style.SudFullDynamicColorThemeGlifV3.
    if (useFullDynamicColor()) {
      return;
    }

    @ColorInt
    int color =
        PartnerConfigHelper.get(getContext())
            .getColor(getContext(), PartnerConfig.CONFIG_LAYOUT_BACKGROUND_COLOR);
    this.getRootView().setBackgroundColor(color);
  }

  @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
  protected void tryApplyPartnerCustomizationContentPaddingTopStyle(View view) {
    Context context = view.getContext();
    boolean partnerPaddingTopAvailable =
        PartnerConfigHelper.get(context)
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_CONTENT_PADDING_TOP);

    if (shouldApplyPartnerResource() && partnerPaddingTopAvailable) {
      int paddingTop =
          (int)
              PartnerConfigHelper.get(context)
                  .getDimension(context, PartnerConfig.CONFIG_CONTENT_PADDING_TOP);

      if (paddingTop != view.getPaddingTop()) {
        view.setPadding(
            view.getPaddingStart(), paddingTop, view.getPaddingEnd(), view.getPaddingBottom());
      }
    }
  }

  protected void initScrollingListener() {
    ScrollView scrollView = getScrollView();

    if (scrollView instanceof BottomScrollView) {
      ((BottomScrollView) scrollView)
          .setBottomScrollListener(
              new BottomScrollListener() {
                @Override
                public void onScrolledToBottom() {
                  onScrolling(true);
                }

                @Override
                public void onRequiresScroll() {
                  onScrolling(false);
                }
              });
    }
  }

  protected void onScrolling(boolean isBottom) {
    FooterBarMixin footerBarMixin = getMixin(FooterBarMixin.class);
    if (footerBarMixin != null) {
      LinearLayout footerContainer = footerBarMixin.getButtonContainer();
      if (footerContainer != null) {
        if (isBottom) {
          footerContainer.setBackgroundColor(Color.TRANSPARENT);
        } else {
          footerContainer.setBackgroundColor(getFooterBackgroundColorFromStyle());
        }
      }
    }
  }

  /**
   * Make button visible and register the {@link Activity#onBackPressed()} to the on click event of
   * the floating back button. It works when {@link
   * PartnerConfigHelper#isGlifExpressiveEnabled(Context)} return true.
   */
  protected void initBackButton() {
    if (PartnerConfigHelper.isGlifExpressiveEnabled(getContext())) {
      Activity activity = PartnerCustomizationLayout.lookupActivityFromContext(getContext());

      FloatingBackButtonMixin floatingBackButtonMixin = getMixin(FloatingBackButtonMixin.class);
      if (floatingBackButtonMixin != null) {
        floatingBackButtonMixin.setVisibility(VISIBLE);
        floatingBackButtonMixin.setOnClickListener(v -> activity.onBackPressed());
      } else {
        LOG.w("FloatingBackButtonMixin button is null");
      }
    } else {
      LOG.atDebug("isGlifExpressiveEnabled is false");
    }
  }

  /** Gets footer bar background color from theme style. */
  public int getFooterBackgroundColorFromStyle() {
    TypedValue typedValue = new TypedValue();
    Theme theme = getContext().getTheme();
    theme.resolveAttribute(R.attr.sudFooterBackgroundColor, typedValue, true);
    return typedValue.data;
  }

  protected boolean isGlifExpressiveEnabled() {
    return PartnerConfigHelper.isGlifExpressiveEnabled(getContext())
        && Build.VERSION.SDK_INT >= VANILLA_ICE_CREAM;
  }
}
