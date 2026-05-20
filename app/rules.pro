# Startup Initializer
-keep class * extends androidx.startup.Initializer { *; }

# Room + WorkManager
#
# androidx.glance:glance-appwidget transitively depends on WorkManager, which
# depends on Room. R8 full mode (android.enableR8.fullMode=true) strips the
# no-arg constructors and reflective members that Room uses to instantiate
# its generated *_Impl classes, causing androidx.startup.InitializationProvider
# to crash on process start. Keep the reflective surface explicitly.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
    public ** createInvalidationTracker();
    public void clearAllTables();
}
-keep class androidx.room.RoomDatabase$JournalMode { *; }
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
