/*
 * Copyright (C) 2016 Google Inc.
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

package com.google.android.setupdesign.gesture;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Helper class to detect the consective-tap gestures on a view.
 *
 * <p>This class is instantiated and used similar to a GestureDetector, where onTouchEvent should be
 * called when there are MotionEvents this detector should know about.
 */
public final class ConsecutiveTapsGestureDetector {

  public interface OnConsecutiveTapsListener {
    /** Callback method when the user tapped on the target view X number of times. */
    void onConsecutiveTaps(int numOfConsecutiveTaps);
  }

  private final View view;
  private final OnConsecutiveTapsListener listener;
  private final int consecutiveTapTouchSlopSquare;
  private final int consecutiveTapTimeout;

  private int consecutiveTapsCounter = 0;
  private MotionEvent previousTapEvent;

  /**
   * @param listener The listener that responds to the gesture.
   * @param view The target view that associated with consecutive-tap gesture.
   */
  public ConsecutiveTapsGestureDetector(OnConsecutiveTapsListener listener, View view) {
    this(listener, view, ViewConfiguration.getDoubleTapTimeout());
  }

  /**
   * @param listener The listener that responds to the gesture.
   * @param view The target view that associated with consecutive-tap gesture.
   * @param consecutiveTapTimeout Maximum time in millis between two consecutive taps.
   */
  public ConsecutiveTapsGestureDetector(
      OnConsecutiveTapsListener listener, View view, int consecutiveTapTimeout) {
    this.listener = listener;
    this.view = view;
    this.consecutiveTapTimeout = consecutiveTapTimeout;
    int doubleTapSlop = ViewConfiguration.get(this.view.getContext()).getScaledDoubleTapSlop();
    consecutiveTapTouchSlopSquare = doubleTapSlop * doubleTapSlop;
  }

  /**
   * This method should be called from the relevant activity or view, typically in onTouchEvent,
   * onInterceptTouchEvent or dispatchTouchEvent.
   *
   * @param ev The motion event
   */
  public void onTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_UP) {
      Rect viewRect = new Rect();
      int[] leftTop = new int[2];
      view.getLocationOnScreen(leftTop);
      viewRect.set(
          leftTop[0], leftTop[1], leftTop[0] + view.getWidth(), leftTop[1] + view.getHeight());
      if (viewRect.contains((int) ev.getX(), (int) ev.getY())) {
        if (isConsecutiveTap(ev)) {
          consecutiveTapsCounter++;
        } else {
          consecutiveTapsCounter = 1;
        }
        listener.onConsecutiveTaps(consecutiveTapsCounter);
      } else {
        // Touch outside the target view. Reset counter.
        consecutiveTapsCounter = 0;
      }

      if (previousTapEvent != null) {
        previousTapEvent.recycle();
      }
      previousTapEvent = MotionEvent.obtain(ev);
    }
  }

  /** Resets the consecutive-tap counter to zero. */
  public void resetCounter() {
    consecutiveTapsCounter = 0;
  }

  /**
   * Returns true if the distance between consecutive tap is within {@link
   * #consecutiveTapTouchSlopSquare}. False, otherwise.
   */
  private boolean isConsecutiveTap(MotionEvent currentTapEvent) {
    if (previousTapEvent == null) {
      return false;
    }

    double deltaX = previousTapEvent.getX() - currentTapEvent.getX();
    double deltaY = previousTapEvent.getY() - currentTapEvent.getY();
    long deltaTime = currentTapEvent.getEventTime() - previousTapEvent.getEventTime();
    return (deltaX * deltaX + deltaY * deltaY <= consecutiveTapTouchSlopSquare)
        && deltaTime < consecutiveTapTimeout;
  }
}
