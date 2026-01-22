# All parcelable classes under com.google.android.setupcompat.logging are serialized across processes
# and need to have the same class names to avoid (de)serialization errors/mismatches.
-keepnames class com.google.android.setupcompat.logging.* implements android.os.Parcelable
-keepclassmembers class com.google.android.setupcompat.logging.* implements android.os.Parcelable {
    public static final ** CREATOR;
}