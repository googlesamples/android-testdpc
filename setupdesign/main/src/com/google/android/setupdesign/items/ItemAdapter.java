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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import com.google.android.setupdesign.R;

/**
 * An adapter typically used with ListView to display an {@link
 * com.google.android.setupdesign.items.ItemHierarchy}. The item hierarchy used to create this
 * adapter can be inflated by {@link ItemInflater} from XML.
 */
public class ItemAdapter extends BaseAdapter implements ItemHierarchy.Observer {

  private static final int VANILLA_ICE_CREAM = 35;

  private final ItemHierarchy itemHierarchy;
  private final ViewTypes viewTypes = new ViewTypes();

  public ItemAdapter(ItemHierarchy hierarchy) {
    itemHierarchy = hierarchy;
    itemHierarchy.registerObserver(this);
    refreshViewTypes();
  }

  @Override
  public int getCount() {
    return itemHierarchy.getCount();
  }

  @Override
  public IItem getItem(int position) {
    return itemHierarchy.getItemAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {
    IItem item = getItem(position);
    int layoutRes = item.getLayoutResource();
    return viewTypes.get(layoutRes);
  }

  @Override
  public int getViewTypeCount() {
    return viewTypes.size();
  }

  private void refreshViewTypes() {
    for (int i = 0; i < getCount(); i++) {
      IItem item = getItem(i);
      viewTypes.add(item.getLayoutResource());
    }
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

  public void updateBackground(View convertView, int position) {
    float groupCornerRadius =
        PartnerConfigHelper.get(convertView.getContext())
            .getDimension(convertView.getContext(), PartnerConfig.CONFIG_ITEMS_GROUP_CORNER_RADIUS);
    float cornerRadius = getCornerRadius(convertView.getContext());
    Drawable drawable = convertView.getBackground();
    Drawable clickDrawable = null;
    Drawable backgroundDrawable = null;
    GradientDrawable background = null;

    if (position == 0 && getCount() == 1) {
      backgroundDrawable = getSingleBackground(convertView.getContext());
    } else if (position == 0) {
      backgroundDrawable = getFirstBackground(convertView.getContext());
    } else if (position == getCount() - 1) {
      backgroundDrawable = getLastBackground(convertView.getContext());
    } else {
      backgroundDrawable = getMiddleBackground(convertView.getContext());
    }
    // TODO add test case for list item group corner partner config
    if (drawable instanceof LayerDrawable && ((LayerDrawable) drawable).getNumberOfLayers() >= 2) {
      clickDrawable = ((LayerDrawable) drawable).getDrawable(1);
    } else {
      TypedArray a =
          convertView
              .getContext()
              .getTheme()
              .obtainStyledAttributes(new int[] {R.attr.selectableItemBackground});
      clickDrawable = a.getDrawable(0);
      a.recycle();
    }
    if (backgroundDrawable instanceof GradientDrawable) {
      float topCornerRadius = cornerRadius;
      float bottomCornerRadius = cornerRadius;
      if (position == 0) {
        topCornerRadius = groupCornerRadius;
      }
      if (position == getCount() - 1) {
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
      convertView.setBackgroundDrawable(new LayerDrawable(layers));
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    // TODO  when getContext is not activity context then fallback to out suw behavior
    if (PartnerConfigHelper.isGlifExpressiveEnabled(parent.getContext())
        && Build.VERSION.SDK_INT >= VANILLA_ICE_CREAM) {
      IItem item = getItem(position);
      LinearLayout linearLayout = null;
      // The ListView can not handle the margin for the child view. So we need to use the
      // LinearLayout to wrap the child view.
      // The getView will trigger several times, for the same position, when the first time
      // the convertView will be null, we should create the view which we want by ourself.
      // The second and following times, we should return the same view which we created before.
      // And for the item#onBindView, we should pass the child view with the wrap linear layout.
      if (convertView == null) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        linearLayout =
            (LinearLayout) inflater.inflate(R.layout.sud_empty_linear_layout, parent, false);
        LayoutInflater linearLayoutInflater = LayoutInflater.from(linearLayout.getContext());
        convertView = linearLayoutInflater.inflate(item.getLayoutResource(), linearLayout, false);
        linearLayout.addView(convertView);
      } else {
        linearLayout = (LinearLayout) convertView;
        convertView = linearLayout.getChildAt(0);
      }
      updateBackground(convertView, position);
      item.onBindView(convertView);
      return linearLayout;
    } else {
      IItem item = getItem(position);
      if (convertView == null) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        convertView = inflater.inflate(item.getLayoutResource(), parent, false);
      }
      item.onBindView(convertView);
      return convertView;
    }
  }

  @Override
  public void onChanged(ItemHierarchy hierarchy) {
    refreshViewTypes();
    notifyDataSetChanged();
  }

  @Override
  public void onItemRangeChanged(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    onChanged(itemHierarchy);
  }

  @Override
  public void onItemRangeInserted(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    onChanged(itemHierarchy);
  }

  @Override
  public void onItemRangeMoved(
      ItemHierarchy itemHierarchy, int fromPosition, int toPosition, int itemCount) {
    onChanged(itemHierarchy);
  }

  @Override
  public void onItemRangeRemoved(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    onChanged(itemHierarchy);
  }

  @Override
  public boolean isEnabled(int position) {
    return getItem(position).isEnabled();
  }

  public ItemHierarchy findItemById(int id) {
    return itemHierarchy.findItemById(id);
  }

  public ItemHierarchy getRootItemHierarchy() {
    return itemHierarchy;
  }

  /**
   * A helper class to pack a sparse set of integers (e.g. resource IDs) to a contiguous list of
   * integers (e.g. adapter positions), providing mapping to retrieve the original ID from a given
   * position. This is used to pack the view types of the adapter into contiguous integers from a
   * given layout resource.
   */
  private static class ViewTypes {
    private final SparseIntArray positionMap = new SparseIntArray();
    private int nextPosition = 0;

    public int add(int id) {
      if (positionMap.indexOfKey(id) < 0) {
        positionMap.put(id, nextPosition);
        nextPosition++;
      }
      return positionMap.get(id);
    }

    public int size() {
      return positionMap.size();
    }

    public int get(int id) {
      return positionMap.get(id);
    }
  }
}
