package com.afwsamples.testdpc.search;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * Util class to retrieve some values of attributes in preference xml.
 * To achieve this, we need to:
 * 1. Obtain the resource id of certain attributes that we care such as title and key.
 * 2. Obtain the value of those attribute {@link TypedArray#peekValue(int)}.
 */
public class PreferenceXmlUtil {

    public static String getDataTitle(Context context, AttributeSet attrs)
            throws ReflectiveOperationException {
        return getData(context, attrs, android.R.attr.title);
    }

    public static String getDataKey(Context context, AttributeSet attrs)
            throws ReflectiveOperationException {
        return getData(context, attrs, android.R.attr.key);
    }

    private static String getData(Context context, AttributeSet set, int attribute)
            throws ReflectiveOperationException {
        final TypedArray sa = context.obtainStyledAttributes(set, new int[] {attribute});
        try {
            final TypedValue tv = sa.peekValue(0);
            CharSequence data = null;
            if (tv != null && tv.type == TypedValue.TYPE_STRING) {
                if (tv.resourceId != 0) {
                    data = context.getText(tv.resourceId);
                } else {
                    data = tv.string;
                }
            }
            return (data != null) ? data.toString() : null;
        } finally {
            sa.recycle();
        }
    }
}
