package com.afwsamples.testdpc.search;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.lang.reflect.Field;

/**
 * Util class to retrieve some values of attributes in preference xml.
 * To achieve this, we need to:
 * 1. Obtain the array android.R$styleable.Preference through reflection.
 *    Cache is introduced to reduce the performance overhead introduced by reflection.
 * 2. Obtain the resource id of certain attributes that we care such as title and key using
 *    reflection. Again, cache is introduced.
 * 3. Obtain the value of those attribute {@link TypedArray#peekValue(int)}.
 */
public class PreferenceXmlUtil {
    private static Integer sPreferenceTitleId;
    private static Integer sPreferenceKeyId;
    private static int[] sPreferenceStyleArray;

    public static String getDataTitle(Context context, AttributeSet attrs)
            throws ReflectiveOperationException {
        return getData(context, attrs, getPreferenceTitleId());
    }

    public static String getDataKey(Context context, AttributeSet attrs)
            throws ReflectiveOperationException {
        return getData(context, attrs, getPreferenceKeyId());
    }

    private static String getData(Context context, AttributeSet set, int resId)
            throws ReflectiveOperationException {
        int[] attrs = getPreferenceStyleArray();
        final TypedArray sa = context.obtainStyledAttributes(set, attrs);
        try {
            final TypedValue tv = sa.peekValue(resId);
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

    private static int getPreferenceTitleId() throws ReflectiveOperationException {
        if (sPreferenceTitleId == null) {
            sPreferenceTitleId = getStyleableId("Preference_title");
        }
        return sPreferenceTitleId;
    }

    private static int getPreferenceKeyId() throws ReflectiveOperationException {
        if (sPreferenceKeyId == null) {
            sPreferenceKeyId = getStyleableId("Preference_key");
        }
        return sPreferenceKeyId;
    }

    private static int[] getPreferenceStyleArray() throws ReflectiveOperationException {
        if (sPreferenceStyleArray == null) {
            sPreferenceStyleArray = getStyleableArray("Preference");
        }
        return sPreferenceStyleArray;
    }

    private static int getStyleableId(String name) throws ReflectiveOperationException {
        Field field = Class.forName("android.R$styleable").getDeclaredField(name);
        return (int) field.get(null);
    }

    private static final int[] getStyleableArray(String name) throws ReflectiveOperationException {
        Field field = Class.forName("android.R$styleable").getDeclaredField(name);
        return (int[]) field.get(null);
    }
}
