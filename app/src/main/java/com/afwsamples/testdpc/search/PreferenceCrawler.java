package com.afwsamples.testdpc.search;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TimingLogger;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Crawl indexable fragments to index all their preferences.
 * Run adb shell setprop log.tag.PreferenceCrawler_Timer VERBOSE to see timing log.
 * At the time of writing, nexus 5x spends 27ms to finish crawling.
 */
public class PreferenceCrawler {
    private Context mContext;
    private static final String NODE_NAME_PREFERENCE_SCREEN = "PreferenceScreen";
    private static final String NODE_NAME_PREFERENCE_CATEGORY = "PreferenceCategory";
    private static final String TAG = "PreferenceCrawler_Timer";

    public PreferenceCrawler(Context context) {
        mContext = context;
    }

    public List<PreferenceIndex> doCrawl() {
        final TimingLogger logger = new TimingLogger(TAG, "doCrawl");
        List<PreferenceIndex> indexablePreferences = new ArrayList<>();
        List<IndexableFragment> indexableFragments = IndexableFragments.values();
        for (IndexableFragment indexableFragment : indexableFragments) {
            indexablePreferences.addAll(crawlSingleIndexableResource(indexableFragment));
            logger.addSplit("processed " + indexableFragment.fragmentName);
        }
        logger.addSplit("Finish crawling");
        logger.dumpToLog();
        return indexablePreferences;
    }

    /**
     * Skim through the xml preference file.
     * @return a list of indexable preference.
     */
    private List<PreferenceIndex> crawlSingleIndexableResource(
            IndexableFragment indexableFragment) {
        List<PreferenceIndex> indexablePreferences = new ArrayList<>();
        XmlPullParser parser = mContext.getResources().getXml(indexableFragment.xmlRes);
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
                String key = PreferenceXmlUtil.getDataKey(mContext, attrs);
                String title = PreferenceXmlUtil.getDataTitle(mContext, attrs);
                if (NODE_NAME_PREFERENCE_CATEGORY.equals(nodeName) || TextUtils.isEmpty(key)
                        || TextUtils.isEmpty(title)) {
                    continue;
                }
                PreferenceIndex indexablePreference =
                        new PreferenceIndex(key, title, indexableFragment.fragmentName);
                indexablePreferences.add(indexablePreference);
            }
        } catch (XmlPullParserException | IOException | ReflectiveOperationException ex) {
            Log.e(TAG, "Error in parsing a preference xml file, skip it", ex);
        }
        return indexablePreferences;
    }
}
