package com.afwsamples.testdpc.search;

import android.content.Context;
import androidx.annotation.XmlRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.afwsamples.testdpc.common.BaseSearchablePolicyPreferenceFragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlIndexableFragment extends BaseIndexableFragment {
    private static final String NODE_NAME_PREFERENCE_SCREEN = "PreferenceScreen";
    private static final String NODE_NAME_PREFERENCE_CATEGORY = "PreferenceCategory";
    private static final String TAG = "PreferenceCrawler_Timer";

    public @XmlRes int xmlRes;

    public XmlIndexableFragment(
            Class<? extends BaseSearchablePolicyPreferenceFragment> fragmentClass,
            @XmlRes int xmlRes) {
        super(fragmentClass);
        this.xmlRes = xmlRes;
    }

    /**
     * Skim through the xml preference file.
     * @return a list of indexable preference.
     */
    @Override
    public List<PreferenceIndex> index(Context context) {
        List<PreferenceIndex> indexablePreferences = new ArrayList<>();
        XmlPullParser parser = context.getResources().getXml(xmlRes);
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
                // Parse next until start tag is found
            }
            String nodeName = parser.getName();
            if (!NODE_NAME_PREFERENCE_SCREEN.equals(nodeName)) {
                throw new RuntimeException(
                        "XML document must start with <PreferenceScreen> tag; found"
                                + nodeName + " at " + parser.getPositionDescription());
            }

            final int outerDepth = parser.getDepth();
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                nodeName = parser.getName();
                String key = PreferenceXmlUtil.getDataKey(context, attrs);
                String title = PreferenceXmlUtil.getDataTitle(context, attrs);
                if (NODE_NAME_PREFERENCE_CATEGORY.equals(nodeName) || TextUtils.isEmpty(key)
                        || TextUtils.isEmpty(title)) {
                    continue;
                }
                PreferenceIndex indexablePreference =
                        new PreferenceIndex(key, title, fragmentName);
                indexablePreferences.add(indexablePreference);
            }
        } catch (XmlPullParserException | IOException | ReflectiveOperationException ex) {
            Log.e(TAG, "Error in parsing a preference xml file, skip it", ex);
        }
        return indexablePreferences;
    }
}
