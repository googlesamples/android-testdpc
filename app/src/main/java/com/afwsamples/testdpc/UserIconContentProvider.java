/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.afwsamples.testdpc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Content Provider used to upload user icons through {@code adb shell content}.
 *
 * <p>It stores the files in the user's {@link Environment#DIRECTORY_PICTURES} directory, under the
 * sub-directory {@link #USER_ICONS_DIR}.
 *
 * <p>Note: it's based on {@code ManagedFileContentProvider} from {@code tools/tradefederation}.
 */
public final class UserIconContentProvider extends ContentProvider {

    private static final String TAG = UserIconContentProvider.class.getSimpleName();
    private static final String USER_ICONS_DIR = "UserIcons";

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ABSOLUTE_PATH = "absolute_path";
    private static final String COLUMN_DIRECTORY = "is_directory";
    private static final String COLUMN_MIME_TYPE = "mime_type";
    private static final String COLUMN_METADATA = "metadata";

    private static final String[] COLUMNS =
            new String[] {
                COLUMN_NAME,
                COLUMN_ABSOLUTE_PATH,
                COLUMN_DIRECTORY,
                COLUMN_MIME_TYPE,
                COLUMN_METADATA
            };

    private static final MimeTypeMap MIME_MAP = MimeTypeMap.getSingleton();

    static final String AUTHORITY = "com.afwsamples.testdpc.usericoncontentprovider";

    private final Map<Uri, ContentValues> mFileTracker = new HashMap<>();

    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * Use a content URI with absolute device path embedded to get information about a file or a
     * directory on the device.
     *
     * @param uri A content uri that contains the path to the desired file/directory.
     * @param projection - not supported.
     * @param selection - not supported.
     * @param selectionArgs - not supported.
     * @param sortOrder - not supported.
     * @return A {@link Cursor} containing the results of the query. Cursor contains a single row
     *     for files and for directories it returns one row for each {@link File} returned by {@link
     *     File#listFiles()}.
     */
    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {
        File file = getFileForUri(uri);
        Log.v(TAG, "Query: " + uri);
        if ("/".equals(file.getAbsolutePath())) {
            // Querying the root will list all the known file (inserted)
            MatrixCursor cursor = new MatrixCursor(COLUMNS, mFileTracker.size());
            for (Map.Entry<Uri, ContentValues> path : mFileTracker.entrySet()) {
                Log.v(TAG, "Adding path " + path);
                String metadata = path.getValue().getAsString(COLUMN_METADATA);
                cursor.addRow(getRow(COLUMNS, getFileForUri(path.getKey()), metadata));
            }
            return cursor;
        }

        if (!file.exists()) {
            Log.e(TAG, "Query - File from uri: '" + uri + "' doesn't exists");
            return null;
        }

        if (!file.isDirectory()) {
            // Just return the information about the file itself.
            MatrixCursor cursor = new MatrixCursor(COLUMNS, 1);
            cursor.addRow(getRow(COLUMNS, file, /* metadata= */ null));
            return cursor;
        }

        // Otherwise return the content of the directory - similar to doing ls command.
        File[] files = file.listFiles();
        sortFilesByAbsolutePath(files);
        MatrixCursor cursor = new MatrixCursor(COLUMNS, files.length + 1);
        for (File child : files) {
            cursor.addRow(getRow(COLUMNS, child, /* metadata= */ null));
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return getType(getFileForUri(uri));
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        String extra = "";
        File file = getFileForUri(uri);
        Log.v(TAG, "insert(): uri=" + uri + ", file=" + file);
        if (!file.exists()) {
            Log.e(TAG, "Insert - File from uri: '" + uri + "' doesn't exist");
            return null;
        }
        if (mFileTracker.get(uri) != null) {
            Log.e(TAG, "Insert - File from uri: '" + uri + "' already exists, ignoring");
            return null;
        }
        mFileTracker.put(uri, contentValues);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        Log.v(TAG, "delete(): uri=" + uri);
        // Stop Tracking the File of directory if it was tracked and delete it from the disk
        mFileTracker.remove(uri);
        File file = getFileForUri(uri);
        int num = recursiveDelete(file);
        return num;
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {
        File file = getFileForUri(uri);
        if (!file.exists()) {
            Log.e(TAG, "Update - File from uri: '" + uri +"' doesn't exist");
            return 0;
        }
        if (mFileTracker.get(uri) == null) {
            Log.e(TAG, "Update - File from uri: '" + uri +"' isn't tracked yet, use insert");
            return 0;
        }
        mFileTracker.put(uri, values);
        return 1;
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        File file = getFileForUri(uri);
        int fileMode = modeToMode(mode);
        Log.v(TAG, "openFile(): uri=" + uri + ", mode=" + mode + "(" + fileMode + ")");

        if ((fileMode & ParcelFileDescriptor.MODE_CREATE) == ParcelFileDescriptor.MODE_CREATE) {
            Log.v(TAG, "Creating file " + file);
            // If the file is being created, create all its parent directories that don't already
            // exist.
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                Log.v(TAG, "Creating parents for " + file);
                boolean created = parentFile.mkdirs();
                if (!created) {
                    throw new FileNotFoundException("Could not created parent dirs for " + file);
                }
            }
            if (!mFileTracker.containsKey(uri)) {
                // Track the file, if not already tracked.
                mFileTracker.put(uri, new ContentValues());
            }
        }
        ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, fileMode);
        Log.v(TAG, "Returning FD " + fd.getFd() + " for " + file.getAbsoluteFile());
        return fd;
    }

    private Object[] getRow(String[] columns, File file, String metadata) {
        Object[] values = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            values[i] = getColumnValue(columns[i], file, metadata);
        }
        return values;
    }

    private Object getColumnValue(String columnName, File file, String metadata) {
        Object value = null;
        if (COLUMN_NAME.equals(columnName)) {
            value = file.getName();
        } else if (COLUMN_ABSOLUTE_PATH.equals(columnName)) {
            value = file.getAbsolutePath();
        } else if (COLUMN_DIRECTORY.equals(columnName)) {
            value = file.isDirectory();
        } else if (COLUMN_METADATA.equals(columnName)) {
            value = metadata;
        } else if (COLUMN_MIME_TYPE.equals(columnName)) {
            value = file.isDirectory() ? null : getType(file);
        }
        return value;
    }

    private String getType(@NonNull File file) {
        int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            String extension = file.getName().substring(lastDot + 1);
            String mime = MIME_MAP.getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    /**
     * Gets the file for the given name.
     *
     * <p>Note: if the name contains path separators, they'll be ignored and just the file name will
     * be used.
     */
    static File getFile(@NonNull Context context, @NonNull String name) {
        File fullFile = new File(name);
        String baseFile = fullFile.getName();
        File file = new File(getStorageDirectory(context), baseFile);
        Log.v(TAG, "getFile(" + name + "): returning " + file);
        return file;
    }

    /**
     * Gets the directory where the user icons are stored.
     */
    static File getStorageDirectory(@NonNull Context context) {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), USER_ICONS_DIR);
    }

    private File getFileForUri(@NonNull Uri uri) {
        String uriPath = uri.getPath();

        File file = getFile(getContext(), uriPath);
        Log.v(TAG, "getFileForUri(" + uri + "): returning " + file);
        return file;
    }

    /** Copied from FileProvider.java. */
    private static int modeToMode(String mode) {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits =
                    ParcelFileDescriptor.MODE_WRITE_ONLY
                            | ParcelFileDescriptor.MODE_CREATE
                            | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits =
                    ParcelFileDescriptor.MODE_WRITE_ONLY
                            | ParcelFileDescriptor.MODE_CREATE
                            | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits =
                    ParcelFileDescriptor.MODE_READ_WRITE
                            | ParcelFileDescriptor.MODE_CREATE
                            | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        return modeBits;
    }

    /**
     * Recursively delete given file or directory and all its contents.
     *
     * @param rootDir the directory or file to be deleted; can be null
     * @return The number of deleted files.
     */
    private int recursiveDelete(@Nullable File rootDir) {
        Log.v(TAG, "recursiveDelete(): rootDir=" + rootDir);
        if (rootDir == null) return 0;
        int count = 0;
        if (rootDir.isDirectory()) {
            File[] childFiles = rootDir.listFiles();
            if (childFiles != null) {
                for (File child : childFiles) {
                    count += recursiveDelete(child);
                }
            }
        }
        rootDir.delete();
        return ++count;
    }

    private static void sortFilesByAbsolutePath(@NonNull File[] files) {
        Arrays.sort(files,(f1, f2) -> f1.getAbsolutePath().compareTo(f2.getAbsolutePath()));
    }
}
