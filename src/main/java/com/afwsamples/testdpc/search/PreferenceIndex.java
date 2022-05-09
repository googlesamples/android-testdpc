package com.afwsamples.testdpc.search;

/** Represent index of a preference object. */
public class PreferenceIndex {

  /** Key of preference. */
  public String key;
  /** Title of preference. */
  public String title;
  /** Class of fragment holding the preference. */
  public String fragmentClass;

  public PreferenceIndex(String key, String title, String fragmentClass) {
    this.key = key;
    this.title = title;
    this.fragmentClass = fragmentClass;
  }
}
