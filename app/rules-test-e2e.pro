-keep class org.jacoco.agent.**

-keepclasseswithmembers class kotlin.Metadata
-keepclasseswithmembers class kotlin.collections.CollectionsKt
-keepclasseswithmembers class kotlin.ranges.RangesKt
-keepclasseswithmembers class kotlin.jvm.functions.Function1
-keepclasseswithmembers class kotlin.LazyKt
-keepclasseswithmembers class java.lang.Object
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}