package com.afwsamples.testdpc.search;

import android.content.Context;
import android.util.TimingLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Crawl indexable fragments to index all their preferences.
 * Run adb shell setprop log.tag.PreferenceCrawler_Timer VERBOSE to see timing log.
 * At the time of writing, nexus 5x spends 27ms to finish crawling.
 */
public class PreferenceCrawler {
    private Context mContext;
    private static final String TAG = "PreferenceCrawler_Timer";

    public PreferenceCrawler(Context context) {
        mContext = context;
    }

    public List<PreferenceIndex> doCrawl() {
        final TimingLogger logger = new TimingLogger(TAG, "doCrawl");
        List<PreferenceIndex> indexablePreferences = new ArrayList<>();
        List<BaseIndexableFragment> indexableFragments = IndexableFragments.values();
        for (BaseIndexableFragment indexableFragment : indexableFragments) {
            indexablePreferences.addAll(indexableFragment.index(mContext));
            logger.addSplit("processed " + indexableFragment.fragmentName);
        }
        logger.addSplit("Finish crawling");
        logger.dumpToLog();
        return indexablePreferences;
    }
}
