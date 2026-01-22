/*
 * Copyright (C) 2021 The Android Open Source Project
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

import static com.google.android.setupcompat.partnerconfig.Util.isNightMode;
import static java.lang.Math.min;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StringDef;
import androidx.annotation.VisibleForTesting;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfig.ResourceType;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupcompat.partnerconfig.ResourceEntry;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.util.BuildCompatUtils;
import com.google.android.setupcompat.util.ForceTwoPaneHelper;
import com.google.android.setupcompat.util.Logger;
import com.google.android.setupdesign.lottieloadinglayout.R;
import com.google.android.setupdesign.util.LayoutStyler;
import com.google.android.setupdesign.util.LottieAnimationHelper;
import com.google.android.setupdesign.view.IllustrationVideoView;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * A GLIF themed layout with a {@link com.airbnb.lottie.LottieAnimationView} to showing lottie
 * illustration and a substitute {@link com.google.android.setupdesign.view.IllustrationVideoView}
 * to showing mp4 illustration. {@code app:sudIllustrationType} can also be used to specify one of
 * the set including "default", "account", "connection", "update", and "final_hold". {@code
 * app:sudLottieRes} can assign the json file of Lottie resource.
 */
public class GlifLoadingLayout extends GlifLayout {
  private static final Logger LOG = new Logger(GlifLoadingLayout.class);
  View inflatedView;

  @VisibleForTesting @IllustrationType String illustrationType = IllustrationType.DEFAULT;
  @VisibleForTesting LottieAnimationConfig animationConfig = LottieAnimationConfig.CONFIG_DEFAULT;

  @VisibleForTesting @RawRes int customLottieResource = 0;

  private AnimatorListener animatorListener;
  private Runnable nextActionRunnable;
  private boolean workFinished;
  protected static final String GLIF_LAYOUT_TYPE = "GlifLayoutType";
  protected static final String LOADING_LAYOUT = "LoadingLayout";
  @VisibleForTesting public boolean runRunnable;
  private boolean isHeaderFullTextEnabled = false;
  // This value is decided by local test expereince.
  @VisibleForTesting static final float MIN_ALLOWED_ILLUSTRATION_HEIGHT_RATIO = 0.25f;

  @VisibleForTesting
  public List<LottieAnimationFinishListener> animationFinishListeners = new ArrayList<>();

  public GlifLoadingLayout(Context context) {
    this(context, 0, 0);
  }

  public GlifLoadingLayout(Context context, int template) {
    this(context, template, 0);
  }

  public GlifLoadingLayout(Context context, int template, int containerId) {
    super(context, template, containerId);
    init(null, com.google.android.setupdesign.R.attr.sudLayoutTheme);
  }

  public GlifLoadingLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, com.google.android.setupdesign.R.attr.sudLayoutTheme);
  }

  public GlifLoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(AttributeSet attrs, int defStyleAttr) {
    registerMixin(FooterBarMixin.class, new LoadingFooterBarMixin(this, attrs, defStyleAttr));

    TypedArray a =
        getContext()
            .obtainStyledAttributes(attrs, R.styleable.SudGlifLoadingLayout, defStyleAttr, 0);
    customLottieResource = a.getResourceId(R.styleable.SudGlifLoadingLayout_sudLottieRes, 0);
    String illustrationType = a.getString(R.styleable.SudGlifLoadingLayout_sudIllustrationType);
    a.recycle();

    if (customLottieResource != 0) {
      inflateLottieView();
      ViewGroup container = findContainer(0);
      container.setVisibility(View.VISIBLE);
    } else {
      if (illustrationType != null) {
        setIllustrationType(illustrationType);
      }

      if (BuildCompatUtils.isAtLeastS()) {
        inflateLottieView();
      } else {
        inflateIllustrationStub();
      }
    }

    View view = findManagedViewById(R.id.sud_layout_loading_content);
    if (view != null) {
      if (shouldApplyPartnerResource()) {
        LayoutStyler.applyPartnerCustomizationExtraPaddingStyle(view);
      }
      tryApplyPartnerCustomizationContentPaddingTopStyle(view);
    }

    updateHeaderHeight();
    updateLandscapeMiddleHorizontalSpacing();

    workFinished = false;
    runRunnable = true;

    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      /*
       * add the listener used to log animation end and check whether the
       * work in background finish when repeated.
       */
      animatorListener =
          new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
              // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
              LOG.atInfo("Animate enable:" + isAnimateEnable() + ". Animation end.");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
              // Do nothing.
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
              if (workFinished) {
                LOG.atInfo("Animation repeat but work finished, run the register runnable.");
                finishRunnable(nextActionRunnable);
                workFinished = false;
              }
            }
          };
      lottieAnimationView.addAnimatorListener(animatorListener);
    }

    initBackButton();
  }

  public void setHeaderFullTextEnabled(boolean enabled) {
    isHeaderFullTextEnabled = enabled;
    // Update header height again.
    updateHeaderHeight();
  }

  public boolean isHeaderFullTextEnabled() {
    return isHeaderFullTextEnabled;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (inflatedView instanceof LinearLayout) {
      updateContentPadding((LinearLayout) inflatedView);
    }
  }

  private boolean isAnimateEnable() {
    try {
      if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
        return Settings.Global.getFloat(
                getContext().getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE)
            != 0f;
      } else {
        return true;
      }

    } catch (SettingNotFoundException e) {
      return true;
    }
  }

  public void setIllustrationType(@IllustrationType String type) {
    if (customLottieResource != 0) {
      throw new IllegalStateException(
          "custom illustration already applied, should not set illustration.");
    }

    if (!illustrationType.equals(type)) {
      illustrationType = type;
    }

    switch (type) {
      case IllustrationType.ACCOUNT:
        animationConfig = LottieAnimationConfig.CONFIG_ACCOUNT;
        break;

      case IllustrationType.CONNECTION:
        animationConfig = LottieAnimationConfig.CONFIG_CONNECTION;
        break;

      case IllustrationType.UPDATE:
        animationConfig = LottieAnimationConfig.CONFIG_UPDATE;
        break;

      case IllustrationType.FINAL_HOLD:
        animationConfig = LottieAnimationConfig.CONFIG_FINAL_HOLD;
        break;

      default:
        animationConfig = LottieAnimationConfig.CONFIG_DEFAULT;
        break;
    }

    updateAnimationView();
  }

  // TODO: [GlifLoadingLayout] Should add testcase. LottieAnimationView was auto
  // generated not able to mock. So we have no idea how to detected is the api pass to
  // LottiAnimationView correctly.
  public boolean setAnimation(InputStream inputStream, String keyCache) {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      lottieAnimationView.setAnimation(inputStream, keyCache);
      return true;
    } else {
      return false;
    }
  }

  public boolean setAnimation(String assetName) {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      lottieAnimationView.setAnimation(assetName);
      return true;
    } else {
      return false;
    }
  }

  public boolean setAnimation(@RawRes int rawRes) {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      lottieAnimationView.setAnimation(rawRes);
      return true;
    } else {
      return false;
    }
  }

  private void updateAnimationView() {
    if (BuildCompatUtils.isAtLeastS()) {
      setLottieResource();
    } else {
      setIllustrationResource();
    }
  }

  /**
   * Call this when your activity is done and should be closed. The activity will be finished while
   * animation finished.
   */
  public void finish(@NonNull Activity activity) {
    if (activity == null) {
      throw new NullPointerException("activity should not be null");
    }
    registerAnimationFinishRunnable(activity::finish);
  }

  private void updateHeaderHeight() {
    View headerView = findManagedViewById(R.id.sud_header_scroll_view);
    Configuration currentConfig = getResources().getConfiguration();
    if (headerView != null
        && PartnerConfigHelper.get(getContext())
            .isPartnerConfigAvailable(PartnerConfig.CONFIG_LOADING_LAYOUT_HEADER_HEIGHT)
        && currentConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
      if (isHeaderFullTextEnabled) {
        // Set header height to wrap content for rendering full text view as much as possible.
        headerView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        // Optimize loading style if there's no extra space for showing illustration view.
        optimizeLoadingStyle(headerView);
      } else {
        float configHeaderHeight =
            PartnerConfigHelper.get(getContext())
                .getDimension(getContext(), PartnerConfig.CONFIG_LOADING_LAYOUT_HEADER_HEIGHT);
        headerView.getLayoutParams().height = (int) configHeaderHeight;
      }
    }
  }

  /**
   * If there's no space in the loading screen page, layout will hide the original illustration view
   * and show a linear progress bar for saving space.
   */
  private void optimizeLoadingStyle(View headerView) {
    if (headerView == null) {
      return;
    }
    // Listen for layout changes to header view
    headerView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Check if linear progress should be shown
                if (shouldShowTopLinearProgress(headerView)) {
                  showTopLinearProgress();
                  hideLoadingIllustration();
                }
              }
            });
  }

  private boolean shouldShowTopLinearProgress(View headerView) {
    Context context = getContext();
    // Get device height in dp
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float deviceHeightDp = displayPixelsToDp(displayMetrics.heightPixels, context);

    // Get height of the view in dp
    float headerViewHeightDp = displayPixelsToDp(headerView.getHeight(), context);

    // Calculate remaining height after subtracting view's height in dp
    float remainingHeightDp = deviceHeightDp - headerViewHeightDp;
    if (deviceHeightDp <= 0) {
      return false;
    }

    LOG.atInfo(
        "deviceHeightDp : "
            + deviceHeightDp
            + " viewHeightDp : "
            + headerViewHeightDp
            + " remainingHeightDp : "
            + remainingHeightDp
            + " remainingHeightRatio : "
            + remainingHeightDp / deviceHeightDp);

    // Check if remaining height ratio is less than the minimum allowed ratio
    return (remainingHeightDp / deviceHeightDp) < MIN_ALLOWED_ILLUSTRATION_HEIGHT_RATIO;
  }

  private void showTopLinearProgress() {
    View view;
    if (isGlifExpressiveEnabled()) {
      view = peekProgressBar();
      if (view == null) {
        final ViewStub progressIndicatorStub =
            findViewById(com.google.android.setupdesign.R.id.sud_glif_top_progress_indicator_stub);
        if (progressIndicatorStub != null) {
          progressIndicatorStub.inflate();
        }
        view = peekProgressBar();
      }
    } else {
      view = findViewById(com.google.android.setupdesign.R.id.sud_glif_top_progress_bar);
    }
    if (view == null) {
      return;
    }
    view.setVisibility(View.VISIBLE);
  }

  private void hideLoadingIllustration() {
    View lottieAnimationView = findViewById(R.id.sud_lottie_view);
    if (lottieAnimationView == null) {
      return;
    }
    lottieAnimationView.setVisibility(View.GONE);
  }

  private float displayPixelsToDp(int pixels, Context context) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return pixels / displayMetrics.density;
  }

  private void updateContentPadding(LinearLayout linearLayout) {
    int paddingTop = linearLayout.getPaddingTop();
    int paddingLeft = linearLayout.getPaddingLeft();
    int paddingRight = linearLayout.getPaddingRight();
    int paddingBottom = linearLayout.getPaddingBottom();

    if (PartnerConfigHelper.get(getContext())
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_TOP)) {
      float configPaddingTop =
          PartnerConfigHelper.get(getContext())
              .getDimension(getContext(), PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_TOP);
      if (configPaddingTop >= 0) {
        paddingTop = (int) configPaddingTop;
      }
    }

    if (PartnerConfigHelper.get(getContext())
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_START)) {
      float configPaddingLeft =
          PartnerConfigHelper.get(getContext())
              .getDimension(getContext(), PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_START);
      if (configPaddingLeft >= 0) {
        paddingLeft = (int) configPaddingLeft;
      }
    }

    if (PartnerConfigHelper.get(getContext())
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_END)) {
      float configPaddingRight =
          PartnerConfigHelper.get(getContext())
              .getDimension(getContext(), PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_END);
      if (configPaddingRight >= 0) {
        paddingRight = (int) configPaddingRight;
      }
    }

    if (PartnerConfigHelper.get(getContext())
        .isPartnerConfigAvailable(PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_BOTTOM)) {
      float configPaddingBottom =
          PartnerConfigHelper.get(getContext())
              .getDimension(getContext(), PartnerConfig.CONFIG_LOADING_LAYOUT_PADDING_BOTTOM);
      if (configPaddingBottom >= 0) {
        FooterBarMixin footerBarMixin = getMixin(FooterBarMixin.class);
        if (footerBarMixin == null || footerBarMixin.getButtonContainer() == null) {
          paddingBottom = (int) configPaddingBottom;
        } else {
          paddingBottom =
              (int) configPaddingBottom
                  - (int)
                      min(
                          configPaddingBottom,
                          getButtonContainerHeight(footerBarMixin.getButtonContainer()));
        }
      }
    }

    linearLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
  }

  private static int getButtonContainerHeight(View view) {
    view.measure(
        MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), MeasureSpec.EXACTLY));
    return view.getMeasuredHeight();
  }

  private void inflateLottieView() {
    final View lottieLayout = peekLottieLayout();
    if (lottieLayout == null) {
      ViewStub viewStub = findManagedViewById(R.id.sud_loading_layout_lottie_stub);
      if (viewStub != null) {
        inflatedView = viewStub.inflate();
        if (inflatedView instanceof LinearLayout) {
          updateContentPadding((LinearLayout) inflatedView);
        }
        setLottieResource();
      }
    }
  }

  private void inflateIllustrationStub() {
    final View progressLayout = peekProgressIllustrationLayout();
    if (progressLayout == null) {
      ViewStub viewStub = findManagedViewById(R.id.sud_loading_layout_illustration_stub);
      if (viewStub != null) {
        inflatedView = viewStub.inflate();
        if (inflatedView instanceof LinearLayout) {
          updateContentPadding((LinearLayout) inflatedView);
        }
        setIllustrationResource();
      }
    }
  }

  private void setLottieResource() {
    LottieAnimationView lottieView = findViewById(R.id.sud_lottie_view);
    if (lottieView == null) {
      LOG.w("Lottie view not found, skip set resource. Wait for layout inflated.");
      return;
    }
    if (customLottieResource != 0) {
      try {
        LOG.atInfo(
            "setCustom Lottie resource=" + getResources().getResourceName(customLottieResource));
      } catch (Exception e) {
        // Dump the resource id when it failed to get the resource name.
        LOG.atInfo("setCustom Lottie resource 0x" + Integer.toHexString(customLottieResource));
      }

      InputStream inputRaw = getResources().openRawResource(customLottieResource);
      lottieView.setAnimation(inputRaw, null);
      lottieView.playAnimation();
    } else {
      PartnerConfigHelper partnerConfigHelper = PartnerConfigHelper.get(getContext());
      ResourceEntry resourceEntry =
          partnerConfigHelper.getIllustrationResourceEntry(
              getContext(), animationConfig.getLottieConfig());

      if (resourceEntry != null) {
        InputStream inputRaw =
            resourceEntry.getResources().openRawResource(resourceEntry.getResourceId());
        try {
          LOG.atInfo(
              "setAnimation "
                  + resourceEntry.getResourceName()
                  + " length="
                  + inputRaw.available());
        } catch (IOException e) {
          LOG.w("IOException while length of " + resourceEntry.getResourceName());
        }

        lottieView.setAnimation(inputRaw, null);
        lottieView.playAnimation();
        setLottieLayoutVisibility(View.VISIBLE);
        setIllustrationLayoutVisibility(View.GONE);
        LottieAnimationHelper.get()
            .applyColor(
                getContext(),
                findLottieAnimationView(),
                isNightMode(getResources().getConfiguration())
                    ? animationConfig.getDarkThemeCustomization()
                    : animationConfig.getLightThemeCustomization());
      } else {
        LOG.w(
            "Can not find the resource entry for "
                + animationConfig.getLottieConfig().getResourceName());
        setLottieLayoutVisibility(View.GONE);
        setIllustrationLayoutVisibility(View.VISIBLE);
        inflateIllustrationStub();
      }
    }
  }

  private void setIllustrationLayoutVisibility(int visibility) {
    View illustrationLayout = findViewById(R.id.sud_layout_progress_illustration);
    if (illustrationLayout != null) {
      illustrationLayout.setVisibility(visibility);
    }
  }

  private void setLottieLayoutVisibility(int visibility) {
    View lottieLayout = findViewById(R.id.sud_layout_lottie_illustration);
    if (lottieLayout != null) {
      lottieLayout.setVisibility(visibility);
    }
  }

  @VisibleForTesting
  boolean isLottieLayoutVisible() {
    View lottieLayout = findViewById(R.id.sud_layout_lottie_illustration);
    return lottieLayout != null && lottieLayout.getVisibility() == View.VISIBLE;
  }

  private void setIllustrationResource() {
    View illustrationLayout = findViewById(R.id.sud_layout_progress_illustration);
    if (illustrationLayout == null) {
      LOG.atInfo("Illustration stub not inflated, skip set resource");
      return;
    }

    IllustrationVideoView illustrationVideoView =
        findManagedViewById(R.id.sud_progress_illustration);
    ProgressBar progressBar = findManagedViewById(R.id.sud_progress_bar);

    PartnerConfigHelper partnerConfigHelper = PartnerConfigHelper.get(getContext());
    ResourceEntry resourceEntry =
        partnerConfigHelper.getIllustrationResourceEntry(
            getContext(), animationConfig.getIllustrationConfig());

    if (resourceEntry != null) {
      progressBar.setVisibility(GONE);
      illustrationVideoView.setVisibility(VISIBLE);
      illustrationVideoView.setVideoResourceEntry(resourceEntry);
    } else {
      progressBar.setVisibility(VISIBLE);
      illustrationVideoView.setVisibility(GONE);
    }
  }

  private LottieAnimationView findLottieAnimationView() {
    return findViewById(R.id.sud_lottie_view);
  }

  private IllustrationVideoView findIllustrationVideoView() {
    return findManagedViewById(R.id.sud_progress_illustration);
  }

  public void playAnimation() {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
      lottieAnimationView.playAnimation();
    }
  }

  /** Returns whether the layout is waiting for animation finish or not. */
  public boolean isFinishing() {
    LottieAnimationView lottieAnimationView = findLottieAnimationView();
    if (lottieAnimationView != null) {
      return !animationFinishListeners.isEmpty() && lottieAnimationView.getRepeatCount() == 0;
    } else {
      return false;
    }
  }

  @AnimationType
  public int getAnimationType() {
    if (findLottieAnimationView() != null && isLottieLayoutVisible()) {
      return AnimationType.LOTTIE;
    } else if (findIllustrationVideoView() != null) {
      return AnimationType.ILLUSTRATION;
    } else {
      return AnimationType.PROGRESS_BAR;
    }
  }

  // TODO: Should add testcase with mocked LottieAnimationView.
  /** Add an animator listener to {@link LottieAnimationView}. */
  public void addAnimatorListener(Animator.AnimatorListener listener) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.addAnimatorListener(listener);
    }
  }

  /** Remove the listener from {@link LottieAnimationView}. */
  public void removeAnimatorListener(AnimatorListener listener) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.removeAnimatorListener(listener);
    }
  }

  /** Remove all {@link AnimatorListener} from {@link LottieAnimationView}. */
  public void removeAllAnimatorListener() {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.removeAllAnimatorListeners();
    }
  }

  /** Add a value callback with property {@link LottieProperty.COLOR_FILTER}. */
  public void addColorCallback(KeyPath keyPath, LottieValueCallback<ColorFilter> callback) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);
    }
  }

  /** Add a simple value callback with property {@link LottieProperty.COLOR_FILTER}. */
  public void addColorCallback(KeyPath keyPath, SimpleLottieValueCallback<ColorFilter> callback) {
    LottieAnimationView animationView = findLottieAnimationView();
    if (animationView != null) {
      animationView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);
    }
  }

  @Nullable
  private View peekLottieLayout() {
    return findViewById(R.id.sud_layout_lottie_illustration);
  }

  @Nullable
  private View peekProgressIllustrationLayout() {
    return findViewById(R.id.sud_layout_progress_illustration);
  }

  @Override
  protected View onInflateTemplate(LayoutInflater inflater, int template) {
    Context context = getContext();
    if (template == 0) {
      boolean useFullScreenIllustration =
          PartnerConfigHelper.get(context)
              .getBoolean(
                  context,
                  PartnerConfig.CONFIG_LOADING_LAYOUT_FULL_SCREEN_ILLUSTRATION_ENABLED,
                  false);
      if (useFullScreenIllustration) {
        template = R.layout.sud_glif_fullscreen_loading_template;

        // if the activity is embedded should apply an embedded layout.
        if (isEmbeddedActivityOnePaneEnabled(context)) {
          // TODO add unit test for this case.
          if (isGlifExpressiveEnabled()) {
            template = R.layout.sud_glif_expressive_fullscreen_loading_embedded_template;
          } else {
            template = R.layout.sud_glif_fullscreen_loading_embedded_template;
          }
          // TODO add unit test for this case.
        } else if (isGlifExpressiveEnabled()) {
          template = R.layout.sud_glif_expressive_fullscreen_loading_template;
        } else if (ForceTwoPaneHelper.isForceTwoPaneEnable(getContext())) {
          template = R.layout.sud_glif_fullscreen_loading_template_two_pane;
        }
      } else {
        template = R.layout.sud_glif_loading_template;

        // if the activity is embedded should apply an embedded layout.
        if (isEmbeddedActivityOnePaneEnabled(context)) {
          if (isGlifExpressiveEnabled()) {
            template = R.layout.sud_glif_expressive_loading_embedded_template;
            // TODO add unit test for this case.
          } else {
            template = R.layout.sud_glif_loading_embedded_template;
          }
          // TODO add unit test for this case.
        } else if (isGlifExpressiveEnabled()) {
          template = R.layout.sud_glif_expressive_loading_template;
        } else if (ForceTwoPaneHelper.isForceTwoPaneEnable(getContext())) {
          template = R.layout.sud_glif_loading_template_two_pane;
        }
      }
    }
    return inflateTemplate(
        inflater, com.google.android.setupdesign.R.style.SudThemeGlif_Light, template);
  }

  @Override
  protected ViewGroup findContainer(int containerId) {
    if (containerId == 0) {
      containerId = R.id.sud_layout_content;
    }
    return super.findContainer(containerId);
  }

  @Override
  protected void onDetachedFromWindow() {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      PersistableBundle bundle = new PersistableBundle();
      bundle.putString(GLIF_LAYOUT_TYPE, LOADING_LAYOUT);
      setLayoutTypeMetrics(bundle);
      super.onDetachedFromWindow();
    }
  }

  /** The progress config used to maps to different animation */
  public enum LottieAnimationConfig {
    CONFIG_DEFAULT(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_DEFAULT,
        PartnerConfig.CONFIG_LOADING_LOTTIE_DEFAULT,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_DEFAULT,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_DEFAULT),
    CONFIG_ACCOUNT(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_ACCOUNT,
        PartnerConfig.CONFIG_LOADING_LOTTIE_ACCOUNT,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_ACCOUNT,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_ACCOUNT),
    CONFIG_CONNECTION(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_CONNECTION,
        PartnerConfig.CONFIG_LOADING_LOTTIE_CONNECTION,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_CONNECTION,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_CONNECTION),
    CONFIG_UPDATE(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_UPDATE,
        PartnerConfig.CONFIG_LOADING_LOTTIE_UPDATE,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_UPDATE,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_UPDATE),
    CONFIG_FINAL_HOLD(
        PartnerConfig.CONFIG_PROGRESS_ILLUSTRATION_FINAL_HOLD,
        PartnerConfig.CONFIG_LOADING_LOTTIE_FINAL_HOLD,
        PartnerConfig.CONFIG_LOTTIE_LIGHT_THEME_CUSTOMIZATION_FINAL_HOLD,
        PartnerConfig.CONFIG_LOTTIE_DARK_THEME_CUSTOMIZATION_FINAL_HOLD);

    private final PartnerConfig illustrationConfig;
    private final PartnerConfig lottieConfig;
    private final PartnerConfig lightThemeCustomization;
    private final PartnerConfig darkThemeCustomization;

    LottieAnimationConfig(
        PartnerConfig illustrationConfig,
        PartnerConfig lottieConfig,
        PartnerConfig lightThemeCustomization,
        PartnerConfig darkThemeCustomization) {
      if (illustrationConfig.getResourceType() != ResourceType.ILLUSTRATION
          || lottieConfig.getResourceType() != ResourceType.ILLUSTRATION) {
        throw new IllegalArgumentException(
            "Illustration progress only allow illustration resource");
      }
      this.illustrationConfig = illustrationConfig;
      this.lottieConfig = lottieConfig;
      this.lightThemeCustomization = lightThemeCustomization;
      this.darkThemeCustomization = darkThemeCustomization;
    }

    PartnerConfig getIllustrationConfig() {
      return illustrationConfig;
    }

    PartnerConfig getLottieConfig() {
      return lottieConfig;
    }

    PartnerConfig getLightThemeCustomization() {
      return lightThemeCustomization;
    }

    PartnerConfig getDarkThemeCustomization() {
      return darkThemeCustomization;
    }
  }

  /**
   * Register the {@link Runnable} as a callback that will be performed when the animation finished.
   */
  public void registerAnimationFinishRunnable(Runnable runnable) {
    workFinished = true;
    nextActionRunnable = runnable;
    synchronized (this) {
      runRunnable = true;
      animationFinishListeners.add(
          new LottieAnimationFinishListener(this, () -> finishRunnable(runnable)));
    }
  }

  @VisibleForTesting
  public synchronized void finishRunnable(Runnable runnable) {
    // to avoid run the runnable twice.
    if (runRunnable) {
      runnable.run();
    }
    runRunnable = false;
  }

  /** The listener that to indicate the playing status for lottie animation. */
  @VisibleForTesting
  public static class LottieAnimationFinishListener {

    private final Runnable runnable;
    private final GlifLoadingLayout glifLoadingLayout;
    private final LottieAnimationView lottieAnimationView;

    @VisibleForTesting
    AnimatorListener animatorListener =
        new AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {
            // Do nothing.
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            onAnimationFinished();
          }

          @Override
          public void onAnimationCancel(Animator animation) {
            // Do nothing.
          }

          @Override
          public void onAnimationRepeat(Animator animation) {
            // Do nothing.
          }
        };

    @VisibleForTesting
    LottieAnimationFinishListener(GlifLoadingLayout glifLoadingLayout, Runnable runnable) {
      if (runnable == null) {
        throw new NullPointerException("Runnable can not be null");
      }
      this.glifLoadingLayout = glifLoadingLayout;
      this.runnable = runnable;
      this.lottieAnimationView = glifLoadingLayout.findLottieAnimationView();

      boolean shouldAnimationBeFinished =
          PartnerConfigHelper.get(glifLoadingLayout.getContext())
              .getBoolean(
                  glifLoadingLayout.getContext(),
                  PartnerConfig.CONFIG_LOADING_LAYOUT_WAIT_FOR_ANIMATION_FINISHED,
                  true);
      // TODO: add test case for verify the case which isAnimating returns true.
      if (glifLoadingLayout.isLottieLayoutVisible()
          && lottieAnimationView.isAnimating()
          && !isZeroAnimatorDurationScale()
          && shouldAnimationBeFinished) {
        LOG.atInfo("Register animation finish.");
        lottieAnimationView.addAnimatorListener(animatorListener);
        lottieAnimationView.setRepeatCount(0);
      } else {
        onAnimationFinished();
      }
    }

    @VisibleForTesting
    boolean isZeroAnimatorDurationScale() {
      try {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
          return Settings.Global.getFloat(
                  glifLoadingLayout.getContext().getContentResolver(),
                  Settings.Global.ANIMATOR_DURATION_SCALE)
              == 0f;
        } else {
          return false;
        }

      } catch (SettingNotFoundException e) {
        return false;
      }
    }

    @VisibleForTesting
    public void onAnimationFinished() {
      runnable.run();
      if (lottieAnimationView != null) {
        lottieAnimationView.removeAnimatorListener(animatorListener);
      }
      glifLoadingLayout.animationFinishListeners.remove(this);
    }
  }

  /** Annotates the state for the illustration. */
  @Retention(RetentionPolicy.SOURCE)
  @StringDef({
    IllustrationType.ACCOUNT,
    IllustrationType.CONNECTION,
    IllustrationType.DEFAULT,
    IllustrationType.UPDATE,
    IllustrationType.FINAL_HOLD
  })
  public @interface IllustrationType {
    String DEFAULT = "default";
    String ACCOUNT = "account";
    String CONNECTION = "connection";
    String UPDATE = "update";
    String FINAL_HOLD = "final_hold";
  }

  /** Annotates the type for the illustration. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({AnimationType.LOTTIE, AnimationType.ILLUSTRATION, AnimationType.PROGRESS_BAR})
  public @interface AnimationType {
    int LOTTIE = 1;
    int ILLUSTRATION = 2;
    int PROGRESS_BAR = 3;
  }
}
