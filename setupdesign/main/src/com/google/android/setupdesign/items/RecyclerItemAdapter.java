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

package com.google.android.setupdesign.items;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.VisibleForTesting;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;

/**
 * An adapter used with RecyclerView to display an {@link ItemHierarchy}. The item hierarchy used to
 * create this adapter can be inflated by {@link com.google.android.setupdesign.items.ItemInflater}
 * from XML.
 */
public class RecyclerItemAdapter extends RecyclerView.Adapter<ItemViewHolder>
    implements ItemHierarchy.Observer {

  private static final String TAG = "RecyclerItemAdapter";
  private static final int VANILLA_ICE_CREAM = 35;

  /**
   * A view tag set by {@link View#setTag(Object)}. If set on the root view of a layout, it will not
   * create the default background for the list item. This means the item will not have ripple touch
   * feedback by default.
   */
  public static final String TAG_NO_BACKGROUND = "noBackground";

  /** Listener for item selection in this adapter. */
  public interface OnItemSelectedListener {

    /**
     * Called when an item in this adapter is clicked.
     *
     * @param item The Item corresponding to the position being clicked.
     */
    void onItemSelected(IItem item);
  }

  private final ItemHierarchy itemHierarchy;
  @VisibleForTesting public final boolean applyPartnerHeavyThemeResource;
  @VisibleForTesting public final boolean useFullDynamicColor;
  private OnItemSelectedListener listener;

  public RecyclerItemAdapter(ItemHierarchy hierarchy) {
    this(hierarchy, false);
  }

  public RecyclerItemAdapter(ItemHierarchy hierarchy, boolean applyPartnerHeavyThemeResource) {
    this(hierarchy, applyPartnerHeavyThemeResource, /* useFullDynamicColor= */ false);
  }

  public RecyclerItemAdapter(
      ItemHierarchy hierarchy,
      boolean applyPartnerHeavyThemeResource,
      boolean useFullDynamicColor) {
    this.applyPartnerHeavyThemeResource = applyPartnerHeavyThemeResource;
    this.useFullDynamicColor = useFullDynamicColor;
    itemHierarchy = hierarchy;
    itemHierarchy.registerObserver(this);
  }

  /**
   * Gets the item at the given position.
   *
   * @see ItemHierarchy#getItemAt(int)
   */
  public IItem getItem(int position) {
    return itemHierarchy.getItemAt(position);
  }

  @Override
  public long getItemId(int position) {
    IItem mItem = getItem(position);
    if (mItem instanceof AbstractItem) {
      final int id = ((AbstractItem) mItem).getId();
      return id > 0 ? id : RecyclerView.NO_ID;
    } else {
      return RecyclerView.NO_ID;
    }
  }

  @Override
  public int getItemCount() {
    return itemHierarchy.getCount();
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    final View view = inflater.inflate(viewType, parent, false);
    final ItemViewHolder viewHolder = new ItemViewHolder(view);
    Drawable background = null;
    final Object viewTag = view.getTag();
    if (!TAG_NO_BACKGROUND.equals(viewTag)) {
      final TypedArray typedArray =
          parent.getContext().obtainStyledAttributes(R.styleable.SudRecyclerItemAdapter);
      Drawable selectableItemBackground =
          typedArray.getDrawable(
              R.styleable.SudRecyclerItemAdapter_android_selectableItemBackground);
      if (selectableItemBackground == null) {
        selectableItemBackground =
            typedArray.getDrawable(R.styleable.SudRecyclerItemAdapter_selectableItemBackground);
      } else {
        background = view.getBackground();
        if (background == null) {
          // If full dynamic color enabled which means this activity is running outside of setup
          // flow, the colors should refer to R.style.SudFullDynamicColorThemeGlifV3.
          if (applyPartnerHeavyThemeResource && !useFullDynamicColor) {
            int color =
                PartnerConfigHelper.get(view.getContext())
                    .getColor(view.getContext(), PartnerConfig.CONFIG_LAYOUT_BACKGROUND_COLOR);
            background = new ColorDrawable(color);
          } else {
            background =
                typedArray.getDrawable(R.styleable.SudRecyclerItemAdapter_android_colorBackground);
          }
        }
      }
      if (selectableItemBackground == null || background == null) {
        Log.e(
            TAG,
            "Cannot resolve required attributes."
                + " selectableItemBackground="
                + selectableItemBackground
                + " background="
                + background);
      } else {
        final Drawable[] layers = {background, selectableItemBackground};
        view.setBackgroundDrawable(new PatchedLayerDrawable(layers));
      }

      typedArray.recycle();
    }

    view.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            final IItem item = viewHolder.getItem();
            if (listener != null && item != null && item.isEnabled()) {
              listener.onItemSelected(item);
            }
          }
        });

    return viewHolder;
  }

  @TargetApi(VANILLA_ICE_CREAM)
  private Drawable getFirstBackground(Context context) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackgroundFirst});
    Drawable firstBackground = a.getDrawable(0);
    a.recycle();
    return firstBackground;
  }

  @TargetApi(VANILLA_ICE_CREAM)
  private Drawable getLastBackground(Context context) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackgroundLast});
    Drawable lastBackground = a.getDrawable(0);
    a.recycle();
    return lastBackground;
  }

  @TargetApi(VANILLA_ICE_CREAM)
  private Drawable getMiddleBackground(Context context) {
    TypedArray a = context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackground});
    Drawable middleBackground = a.getDrawable(0);
    a.recycle();
    return middleBackground;
  }

  @TargetApi(VANILLA_ICE_CREAM)
  private Drawable getSingleBackground(Context context) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemBackgroundSingle});
    Drawable singleBackground = a.getDrawable(0);
    a.recycle();
    return singleBackground;
  }

  private float getCornerRadius(Context context) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(new int[] {R.attr.sudItemCornerRadius});
    float conerRadius = a.getDimension(0, 0);
    a.recycle();
    return conerRadius;
  }

  public void updateBackground(View view, int position) {
    if (TAG_NO_BACKGROUND.equals(view.getTag())) {
      return;
    }
    float groupCornerRadius =
        PartnerConfigHelper.get(view.getContext())
            .getDimension(view.getContext(), PartnerConfig.CONFIG_ITEMS_GROUP_CORNER_RADIUS);
    float cornerRadius = getCornerRadius(view.getContext());
    Drawable drawable = view.getBackground();
    // TODO add test case for list item group corner partner config
    if (drawable instanceof LayerDrawable && ((LayerDrawable) drawable).getNumberOfLayers() >= 2) {
      Drawable clickDrawable = ((LayerDrawable) drawable).getDrawable(1);
      Drawable backgroundDrawable = null;
      GradientDrawable background = null;

      if (position == 0 && getItemCount() == 1) {
        backgroundDrawable = getSingleBackground(view.getContext());
      } else if (position == 0) {
        backgroundDrawable = getFirstBackground(view.getContext());
      } else if (position == getItemCount() - 1) {
        backgroundDrawable = getLastBackground(view.getContext());
      } else {
        backgroundDrawable = getMiddleBackground(view.getContext());
      }

      if (backgroundDrawable instanceof GradientDrawable) {
        float topCornerRadius = cornerRadius;
        float bottomCornerRadius = cornerRadius;
        if (position == 0) {
          topCornerRadius = groupCornerRadius;
        }
        if (position == getItemCount() - 1) {
          bottomCornerRadius = groupCornerRadius;
        }
        background = (GradientDrawable) backgroundDrawable;
        background.setCornerRadii(
            new float[] {
              topCornerRadius,
              topCornerRadius,
              topCornerRadius,
              topCornerRadius,
              bottomCornerRadius,
              bottomCornerRadius,
              bottomCornerRadius,
              bottomCornerRadius
            });
        final Drawable[] layers = {background, clickDrawable};
        view.setBackgroundDrawable(new PatchedLayerDrawable(layers));
      }
    }
  }

  @Override
  public void onBindViewHolder(ItemViewHolder holder, int position) {
    final IItem item = getItem(position);
    holder.setEnabled(item.isEnabled());
    holder.setItem(item);
    // TODO  when getContext is not activity context then fallback to out suw behavior
    if (PartnerConfigHelper.isGlifExpressiveEnabled(holder.itemView.getContext())
        && Build.VERSION.SDK_INT >= VANILLA_ICE_CREAM) {
      updateBackground(holder.itemView, position);
    }
    item.onBindView(holder.itemView);
  }

  @Override
  public int getItemViewType(int position) {
    // Use layout resource as item view type. RecyclerView item type does not have to be
    // contiguous.
    IItem item = getItem(position);
    return item.getLayoutResource();
  }

  @Override
  public void onChanged(ItemHierarchy hierarchy) {
    notifyDataSetChanged();
  }

  @Override
  public void onItemRangeChanged(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    notifyItemRangeChanged(positionStart, itemCount);
  }

  @Override
  public void onItemRangeInserted(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    notifyItemRangeInserted(positionStart, itemCount);
  }

  @Override
  public void onItemRangeMoved(
      ItemHierarchy itemHierarchy, int fromPosition, int toPosition, int itemCount) {
    // There is no notifyItemRangeMoved
    // https://code.google.com/p/android/issues/detail?id=125984
    if (itemCount == 1) {
      notifyItemMoved(fromPosition, toPosition);
    } else {
      // If more than one, degenerate into the catch-all data set changed callback, since I'm
      // not sure how recycler view handles multiple calls to notifyItemMoved (if the result
      // is committed after every notification then naively calling
      // notifyItemMoved(from + i, to + i) is wrong).
      // Logging this in case this is a more common occurrence than expected.
      Log.i(TAG, "onItemRangeMoved with more than one item");
      notifyDataSetChanged();
    }
  }

  @Override
  public void onItemRangeRemoved(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    notifyItemRangeRemoved(positionStart, itemCount);
  }

  /**
   * Find an item hierarchy within the root hierarchy.
   *
   * @see ItemHierarchy#findItemById(int)
   */
  public ItemHierarchy findItemById(int id) {
    return itemHierarchy.findItemById(id);
  }

  /** Gets the root item hierarchy in this adapter. */
  public ItemHierarchy getRootItemHierarchy() {
    return itemHierarchy;
  }

  /**
   * Sets the listener to listen for when user clicks on a item.
   *
   * @see OnItemSelectedListener
   */
  public void setOnItemSelectedListener(OnItemSelectedListener listener) {
    this.listener = listener;
  }

  /**
   * Before Lollipop, LayerDrawable always return true in getPadding, even if the children layers do
   * not have any padding. Patch the implementation so that getPadding returns false if the padding
   * is empty.
   *
   * <p>When getPadding is true, the padding of the view will be replaced by the padding of the
   * drawable when {@link View#setBackgroundDrawable(Drawable)} is called. This patched class makes
   * sure layer drawables without padding does not clear out original padding on the view.
   */
  @VisibleForTesting
  static class PatchedLayerDrawable extends LayerDrawable {

    /** {@inheritDoc} */
    PatchedLayerDrawable(Drawable[] layers) {
      super(layers);
    }

    @Override
    public boolean getPadding(Rect padding) {
      final boolean superHasPadding = super.getPadding(padding);
      return superHasPadding
          && !(padding.left == 0 && padding.top == 0 && padding.right == 0 && padding.bottom == 0);
    }
  }
}
