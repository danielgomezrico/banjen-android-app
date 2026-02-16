# Room - keep generated database implementations (found via reflection)
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.DatabaseConfiguration

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Startup Initializer
-keep class * extends androidx.startup.Initializer { *; }
