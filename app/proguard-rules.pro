-optimizations code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 10
-allowaccessmodification

# Crashlytics
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile,LineNumberTable,*Annotation*

# Adsense
#-keep public class com.google.android.gms.ads.** { public *; }
#-keep public class com.google.ads.** { public *; }

# Kotlin
-dontwarn kotlin.**

-keep class org.jacoco.agent.**
