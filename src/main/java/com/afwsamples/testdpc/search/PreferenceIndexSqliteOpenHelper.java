package com.afwsamples.testdpc.search;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/** Manage the preference index database. */
// TODO(b/203757850): Restore cached search index in release
public class PreferenceIndexSqliteOpenHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "preference_index.db";
  private static final int DATABASE_VERSION = 1;
  private static final String CREATE_TABLE_PREFERENCE_INDEX =
      "CREATE TABLE "
          + PreferenceIndexTable.TABLE_NAME
          + " ("
          + PreferenceIndexTable._ID
          + " INTEGER PRIMARY KEY,"
          + PreferenceIndexTable.KEY
          + " TEXT NOT NULL,"
          + PreferenceIndexTable.TITLE
          + " TEXT NOT NULL,"
          + PreferenceIndexTable.FRAGMENT_CLASS
          + " TEXT NOT NULL"
          + ");";
  private static final String CREATE_FTS_TABLE =
      "CREATE VIRTUAL TABLE "
          + PreferenceIndexFtsTable.TABLE_NAME
          + " USING fts4 (content='"
          + PreferenceIndexTable.TABLE_NAME
          + "', "
          + PreferenceIndexTable.TITLE
          + ");";
  private static final String REBUILD_FTS_SQL =
      "INSERT INTO "
          + PreferenceIndexFtsTable.TABLE_NAME
          + "("
          + PreferenceIndexFtsTable.TABLE_NAME
          + ") VALUES('rebuild')";
  private static final String LOOKUP_SQL =
      "SELECT * FROM "
          + PreferenceIndexTable.TABLE_NAME
          + " WHERE _id IN (SELECT "
          + PreferenceIndexFtsTable.DOC_ID
          + " FROM "
          + PreferenceIndexFtsTable.TABLE_NAME
          + " WHERE "
          + PreferenceIndexFtsTable.TABLE_NAME
          + " MATCH ?) AND "
          + PreferenceIndexTable.FRAGMENT_CLASS
          + " IN(";

  private static PreferenceIndexSqliteOpenHelper sInstance;
  private static boolean sIndexed = false;

  private Context mContext;

  private PreferenceIndexSqliteOpenHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    mContext = context.getApplicationContext();
  }

  public static synchronized PreferenceIndexSqliteOpenHelper getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new PreferenceIndexSqliteOpenHelper(context);
    }
    return sInstance;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TABLE_PREFERENCE_INDEX);
    db.execSQL(CREATE_FTS_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

  private void clearDatabase() {
    getWritableDatabase().delete(PreferenceIndexTable.TABLE_NAME, null, null);
  }

  public void insertIndexablePreferences(List<PreferenceIndex> preferenceIndexList) {
    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    try {
      for (PreferenceIndex preferenceIndex : preferenceIndexList) {
        db.insert(
            PreferenceIndexTable.TABLE_NAME,
            null,
            PreferenceIndexTable.toContentValues(preferenceIndex));
      }
      // Rebuild the fts table.
      db.execSQL(REBUILD_FTS_SQL);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  /**
   * @param query the words to lookup
   * @param targetFragments the fragments you are searching for
   * @return the list of preferences that match the query
   */
  public List<PreferenceIndex> lookup(String query, List<String> targetFragments) {
    updateIndexIfNeeded();
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = null;
    try {
      String[] selectionArgs = {query + "*"};
      cursor = db.rawQuery(buildLookupSQL(targetFragments), selectionArgs);
      List<PreferenceIndex> preferenceIndexList = new ArrayList<>();
      while (cursor.moveToNext()) {
        preferenceIndexList.add(PreferenceIndexTable.fromCursor(cursor));
      }
      return preferenceIndexList;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private String buildLookupSQL(List<String> targetFragments) {
    StringBuilder stringBuilder = new StringBuilder(LOOKUP_SQL);
    for (String fragment : targetFragments) {
      DatabaseUtils.appendEscapedSQLString(stringBuilder, fragment);
      stringBuilder.append(",");
    }
    stringBuilder.setLength(stringBuilder.length() - 1); // Strip the last comma
    stringBuilder.append(")");
    return stringBuilder.toString();
  }

  private void updateIndexIfNeeded() {
    if (shouldUpdateIndex()) {
      updateIndex();
      sIndexed = true;
    }
  }

  private boolean shouldUpdateIndex() {
    return !sIndexed;
  }

  private void updateIndex() {
    clearDatabase();
    PreferenceCrawler preferenceCrawler = new PreferenceCrawler(mContext);
    List<PreferenceIndex> preferenceIndexList = preferenceCrawler.doCrawl();
    insertIndexablePreferences(preferenceIndexList);
  }

  private static class PreferenceIndexTable {
    private static final String _ID = "_id";
    /** Key of preference. */
    private static final String KEY = "key";
    /** Title of preference. */
    private static final String TITLE = "title";
    /** Class of fragment holding the preference. */
    private static final String FRAGMENT_CLASS = "fragment_class";

    private static final String TABLE_NAME = "preference_index";

    static ContentValues toContentValues(PreferenceIndex preferenceIndex) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(KEY, preferenceIndex.key);
      contentValues.put(TITLE, preferenceIndex.title);
      contentValues.put(FRAGMENT_CLASS, preferenceIndex.fragmentClass);
      return contentValues;
    }

    static PreferenceIndex fromCursor(Cursor cursor) {
      final int INDEX_KEY = cursor.getColumnIndex(KEY);
      final int TITLE_INDEX = cursor.getColumnIndex(TITLE);
      final int FRAGMENT_CLASS_INDEX = cursor.getColumnIndex(FRAGMENT_CLASS);
      String key = cursor.getString(INDEX_KEY);
      String title = cursor.getString(TITLE_INDEX);
      String fragmentClass = cursor.getString(FRAGMENT_CLASS_INDEX);
      return new PreferenceIndex(key, title, fragmentClass);
    }
  }

  /**
   * It is full text search table. We indexed {@link PreferenceIndexTable#TITLE} so that we can have
   * full text search on it.
   */
  private static class PreferenceIndexFtsTable {
    private static final String TABLE_NAME = "preference_index_fts";
    /** It is the predefined column represents the id column in the table being indexed. */
    private static final String DOC_ID = "docid";
  }
}
