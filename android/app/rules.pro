# Startup Initializer
-keep class * extends androidx.startup.Initializer { *; }

# Room + WorkManager
#
# androidx.glance:glance-appwidget transitively depends on WorkManager, which
# depends on Room. R8 full mode (android.enableR8.fullMode=true) strips the
# no-arg constructors and reflective members that Room uses to instantiate
# its generated *_Impl classes, causing androidx.startup.InitializationProvider
# to crash on process start. Keep the reflective surface explicitly — but ONLY
# that surface: a blanket `-keep class androidx.work.** { *; }` pinned ~300KB
# of dex that the consumer rules shipped inside the WorkManager/Room AARs
# already cover.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
    public ** createInvalidationTracker();
    public void clearAllTables();
}
-keep class androidx.room.RoomDatabase$JournalMode { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Collapse all obfuscated classes into the root package: shorter names,
# better zip compression, smaller string section.
-repackageclasses

# Release builds plant no Timber tree (BuildConfig.DEBUG check), but library
# code still calls android.util.Log directly. Strip non-error log calls.
# -maximumremovedandroidloglevel is the R8-native form: it also folds
# Log.isLoggable() and removes the dead guarded blocks (4 = strip V/D/I).
-maximumremovedandroidloglevel 4
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Compose composition-tracing markers are deliberately NOT covered by the
# Compose runtime's consumer rules (so release tracing stays possible).
# Every restartable composable otherwise embeds an unobfuscated
# "Name (file.kt:line)" string in release dex. Official removal recipe:
# https://developer.android.com/develop/ui/compose/tooling/tracing
-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
    boolean isTraceInProgress();
    void traceEventStart(int,int,int,java.lang.String);
    void traceEventStart(int,java.lang.String);
    void traceEventEnd();
}

# Remove Kotlin null-check intrinsics entirely (AGP 9 R8 global option).
# The default (remove_message) already strips the parameter-name strings,
# so failure messages are gone either way; this drops the residual checks.
-processkotlinnullchecks remove

# Google Mobile Ads (play-services-ads-lite)
# Investigation (shipped consumer rules + source usage + fullMode + repackage):
# SDK ships only narrow rules (mediation adapters, LiteSdkInfo, some MobileAds
# reflectives, offline util, protos, NativeAd recordEvent).
# App directly references: AdView, AdRequest, AdSize, MobileAds.
# Banner creatives + ad fetch use many com.google.android.gms.internal.ads.*
# and internal that static analysis + full R8 can drop under -repackageclasses.
# These explicit keeps are the minimal additional surface required for ads
# to initialize, request, and render without silent no-fill or class errors.
# Kept narrow (no blanket com.google.android.gms.**) to match project style.
-keep class com.google.android.gms.ads.AdView { *; }
-keep class com.google.android.gms.ads.AdRequest { *; }
-keep class com.google.android.gms.ads.AdSize { *; }
-keep class com.google.android.gms.ads.MobileAds { *; }
-keep class com.google.android.gms.ads.AdListener { *; }
-keep class com.google.android.gms.ads.LoadAdError { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-keep class com.google.android.gms.ads.internal.** { *; }
