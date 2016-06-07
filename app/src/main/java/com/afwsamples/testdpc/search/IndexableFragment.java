package com.afwsamples.testdpc.search;

import android.content.Context;
import android.support.annotation.XmlRes;
import android.util.Log;

import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;

public class IndexableFragment {
    private static final String TAG = "IndexableFragment";

    public String fragmentName;
    public @XmlRes int xmlRes;

    public IndexableFragment(Class<? extends BaseSearchablePolicyPreferenceFragment> fragmentClass,
            @XmlRes int xmlRes) {
        this.fragmentName = fragmentClass.getName();
        this.xmlRes = xmlRes;
    }

    public boolean isAvailable(Context context) {
        try {
            Class<BaseSearchablePolicyPreferenceFragment> clazz =
                    (Class<BaseSearchablePolicyPreferenceFragment>)
                            Class.forName(this.fragmentName);
            BaseSearchablePolicyPreferenceFragment fragment = clazz.newInstance();
            return fragment.isAvailable(context);
        } catch (ClassNotFoundException | java.lang.InstantiationException | IllegalStateException
                | IllegalAccessException e) {
            Log.e(TAG, "isAvailable error", e);
        }
        return false;
    }
}
