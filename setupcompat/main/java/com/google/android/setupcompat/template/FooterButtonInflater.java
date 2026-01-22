/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.setupcompat.template;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import androidx.annotation.NonNull;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class FooterButtonInflater {
  protected final Context context;

  /**
   * Creates a new inflater instance associated with a particular Resources bundle.
   *
   * @param context The Context using to get Resources and Generate FooterButton Object
   */
  public FooterButtonInflater(@NonNull Context context) {
    this.context = context;
  }

  public Resources getResources() {
    return context.getResources();
  }

  /**
   * Inflates a new hierarchy from the specified XML resource. Throws InflaterException if there is
   * an error.
   *
   * @param resId ID for an XML resource to load (e.g. <code>R.xml.my_xml</code>)
   * @return The root of the inflated hierarchy.
   */
  public FooterButton inflate(int resId) {
    XmlResourceParser parser = getResources().getXml(resId);
    try {
      return inflate(parser);
    } finally {
      parser.close();
    }
  }

  /**
   * Inflates a new hierarchy from the specified XML node. Throws InflaterException if there is an
   * error.
   *
   * <p><em><strong>Important</strong></em>&nbsp;&nbsp;&nbsp;For performance reasons, inflation
   * relies heavily on pre-processing of XML files that is done at build time. Therefore, it is not
   * currently possible to use inflater with an XmlPullParser over a plain XML file at runtime.
   *
   * @param parser XML dom node containing the description of the hierarchy.
   * @return The root of the inflated hierarchy.
   */
  private FooterButton inflate(XmlPullParser parser) {
    final AttributeSet attrs = Xml.asAttributeSet(parser);
    FooterButton button;

    try {
      // Look for the root node.
      int type;
      while ((type = parser.next()) != XmlPullParser.START_TAG
          && type != XmlPullParser.END_DOCUMENT) {
        // continue
      }

      if (type != XmlPullParser.START_TAG) {
        throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
      }

      if (!parser.getName().equals("FooterButton")) {
        throw new InflateException(parser.getPositionDescription() + ": not a FooterButton");
      }

      button = new FooterButton(context, attrs);
    } catch (XmlPullParserException e) {
      throw new InflateException(e.getMessage(), e);
    } catch (IOException e) {
      throw new InflateException(parser.getPositionDescription() + ": " + e.getMessage(), e);
    }

    return button;
  }
}
