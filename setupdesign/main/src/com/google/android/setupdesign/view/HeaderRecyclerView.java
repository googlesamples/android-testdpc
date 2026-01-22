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

package com.google.android.setupdesign.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import com.google.android.setupdesign.DividerItemDecoration;
import com.google.android.setupdesign.R;

/**
 * A RecyclerView that can display a header item at the start of the list. The header can be set by
 * {@code app:sudHeader} in XML. Note that the header will not be inflated until a layout manager is
 * set.
 */
public class HeaderRecyclerView extends RecyclerView {

  private static class HeaderViewHolder extends ViewHolder
      implements DividerItemDecoration.DividedViewHolder {

    HeaderViewHolder(View itemView) {
      super(itemView);
    }

    @Override
    public boolean isDividerAllowedAbove() {
      return false;
    }

    @Override
    public boolean isDividerAllowedBelow() {
      return false;
    }
  }

  /**
   * An adapter that can optionally add one header item to the RecyclerView.
   *
   * @param <CVH> Type of the content view holder. i.e. view holder type of the wrapped adapter.
   */
  public static class HeaderAdapter<CVH extends ViewHolder>
      extends RecyclerView.Adapter<ViewHolder> {

    private static final int HEADER_VIEW_TYPE = Integer.MAX_VALUE;

    private final RecyclerView.Adapter<CVH> adapter;
    private View header;

    private final AdapterDataObserver observer =
        new AdapterDataObserver() {

          @Override
          public void onChanged() {
            notifyDataSetChanged();
          }

          @Override
          public void onItemRangeChanged(int positionStart, int itemCount) {
            if (header != null) {
              positionStart++;
            }
            notifyItemRangeChanged(positionStart, itemCount);
          }

          @Override
          public void onItemRangeInserted(int positionStart, int itemCount) {
            if (header != null) {
              positionStart++;
            }
            notifyItemRangeInserted(positionStart, itemCount);
          }

          @Override
          public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (header != null) {
              fromPosition++;
              toPosition++;
            }
            // Why is there no notifyItemRangeMoved?
            for (int i = 0; i < itemCount; i++) {
              notifyItemMoved(fromPosition + i, toPosition + i);
            }
          }

          @Override
          public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (header != null) {
              positionStart++;
            }
            notifyItemRangeRemoved(positionStart, itemCount);
          }
        };

    public HeaderAdapter(RecyclerView.Adapter<CVH> adapter) {
      this.adapter = adapter;
      this.adapter.registerAdapterDataObserver(observer);
      setHasStableIds(this.adapter.hasStableIds());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      // Returning the same view (header) results in crash ".. but view is not a real child."
      // The framework creates more than one instance of header because of "disappear"
      // animations applied on the header and this necessitates creation of another header
      // view to use after the animation. We work around this restriction by returning an
      // empty FrameLayout to which the header is attached using #onBindViewHolder method.
      if (viewType == HEADER_VIEW_TYPE) {
        FrameLayout frameLayout = new FrameLayout(parent.getContext());
        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        frameLayout.setLayoutParams(params);
        return new HeaderViewHolder(frameLayout);
      } else {
        return adapter.onCreateViewHolder(parent, viewType);
      }
    }

    @Override
    @SuppressWarnings("unchecked") // Non-header position always return type CVH
    public void onBindViewHolder(ViewHolder holder, int position) {
      if (header != null) {
        position--;
      }

      if (holder instanceof HeaderViewHolder) {
        if (header == null) {
          throw new IllegalStateException("HeaderViewHolder cannot find mHeader");
        }
        if (header.getParent() != null) {
          ((ViewGroup) header.getParent()).removeView(header);
        }
        FrameLayout mHeaderParent = (FrameLayout) holder.itemView;
        mHeaderParent.addView(header);
      } else {
        adapter.onBindViewHolder((CVH) holder, position);
      }
    }

    @Override
    public int getItemViewType(int position) {
      if (header != null) {
        position--;
      }
      if (position < 0) {
        return HEADER_VIEW_TYPE;
      }
      return adapter.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
      int count = adapter.getItemCount();
      if (header != null) {
        count++;
      }
      return count;
    }

    @Override
    public long getItemId(int position) {
      if (header != null) {
        position--;
      }
      if (position < 0) {
        return Long.MAX_VALUE;
      }
      return adapter.getItemId(position);
    }

    public void setHeader(View header) {
      this.header = header;
    }

    public RecyclerView.Adapter<CVH> getWrappedAdapter() {
      return adapter;
    }
  }

  private View header;
  private int headerRes;

  public HeaderRecyclerView(Context context) {
    super(context);
    init(null, 0);
  }

  public HeaderRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs, 0);
  }

  public HeaderRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr);
  }

  private void init(AttributeSet attrs, int defStyleAttr) {
    if (isInEditMode()) {
      return;
    }

    final TypedArray a =
        getContext()
            .obtainStyledAttributes(attrs, R.styleable.SudHeaderRecyclerView, defStyleAttr, 0);
    headerRes = a.getResourceId(R.styleable.SudHeaderRecyclerView_sudHeader, 0);
    a.recycle();
  }

  @Override
  public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);

    // Decoration-only headers should not count as an item for accessibility, adjust the
    // accessibility event to account for that.
    final int numberOfHeaders = header != null ? 1 : 0;
    event.setItemCount(event.getItemCount() - numberOfHeaders);
    event.setFromIndex(Math.max(event.getFromIndex() - numberOfHeaders, 0));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      event.setToIndex(Math.max(event.getToIndex() - numberOfHeaders, 0));
    }
  }

  private boolean handleDpadDown() {
    View focusedView = findFocus();
    if (focusedView == null) {
      return false;
    }

    int[] focusdLocationInWindow = new int[2];
    int[] myLocationInWindow = new int[2];

    focusedView.getLocationInWindow(focusdLocationInWindow);
    getLocationInWindow(myLocationInWindow);

    int offset =
        (focusdLocationInWindow[1] + focusedView.getMeasuredHeight())
            - (myLocationInWindow[1] + getMeasuredHeight());

    /*
      (focusdLocationInWindow[1] + focusedView.getMeasuredHeight())
      is the bottom position of focused view

      (myLocationInWindow[1] + getMeasuredHeight())
      is the bottom position of recycler view

      If the bottom of focused view is out of recycler view, means we need to scroll down to show
      more detail

      We scroll 70% of recycler view to make sure user can have 30% of previous information, to make
      sure user can keep reading easily.
    */
    if (offset > 0) {
      // We expect only scroll 70% of recycler view
      int scrollLength = (int) (getMeasuredHeight() * 0.7f);
      smoothScrollBy(0, Math.min(scrollLength, offset));
      return true;
    }

    return false;
  }

  private boolean handleDpadUp() {
    View focusedView = findFocus();
    if (focusedView == null) {
      return false;
    }

    int[] focusedLocationInWindow = new int[2];
    int[] myLocationInWindow = new int[2];

    focusedView.getLocationInWindow(focusedLocationInWindow);
    getLocationInWindow(myLocationInWindow);

    int offset = (focusedLocationInWindow[1] - myLocationInWindow[1]);

    /*
      focusedLocationInWindow[1] is top of focused view
      myLocationInWindow[1] is top of recycler view

      If top of focused view is higher than recycler view we need scroll up to show more information
      we try to scroll up 70% of recycler view ot scroll up to the top of focused view
    */
    if (offset < 0) {
      // We expect only scroll 70% of recycler view
      int scrollLength = (int) (getMeasuredHeight() * -0.7f);

      smoothScrollBy(0, Math.max(scrollLength, offset));
      return true;
    }
    return false;
  }

  private boolean shouldHandleActionUp = false;

  private boolean handleKeyEvent(KeyEvent keyEvent) {
    if (shouldHandleActionUp && keyEvent.getAction() == KeyEvent.ACTION_UP) {
      shouldHandleActionUp = false;
      return true;
    } else if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
      boolean eventHandled = false;
      switch (keyEvent.getKeyCode()) {
        case KeyEvent.KEYCODE_DPAD_DOWN:
          eventHandled = handleDpadDown();
          break;
        case KeyEvent.KEYCODE_DPAD_UP:
          eventHandled = handleDpadUp();
          break;
        default: // fall out
      }
      shouldHandleActionUp = eventHandled;
      return eventHandled;
    }
    return false;
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (handleKeyEvent(event)) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  /** Gets the header view of this RecyclerView, or {@code null} if there are no headers. */
  public View getHeader() {
    return header;
  }

  /**
   * Set the view to use as the header of this recycler view. Note: This must be called before
   * setAdapter.
   */
  public void setHeader(View header) {
    this.header = header;
  }

  @Override
  public void setLayoutManager(LayoutManager layout) {
    super.setLayoutManager(layout);
    if (layout != null && header == null && headerRes != 0) {
      // Inflating a child view requires the layout manager to be set. Check here to see if
      // any header item is specified in XML and inflate them.
      final LayoutInflater inflater = LayoutInflater.from(getContext());
      header = inflater.inflate(headerRes, this, false);
    }
  }

  @Override
  @SuppressWarnings("rawtypes,unchecked") // RecyclerView.setAdapter uses raw type :(
  public void setAdapter(Adapter adapter) {
    if (header != null && adapter != null) {
      final HeaderAdapter headerAdapter = new HeaderAdapter(adapter);
      headerAdapter.setHeader(header);
      adapter = headerAdapter;
    }
    super.setAdapter(adapter);
  }
}
