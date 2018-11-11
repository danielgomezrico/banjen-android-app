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

# Firebase 1.1.1
-keep class com.firebase.** { *; }
-keep class org.shaded.apache.** { *; }
-keepnames class com.shaded.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**

-dontwarn com.google.android.gms.internal.measurement.**
-dontwarn com.google.android.gms.measurement.internal.**
