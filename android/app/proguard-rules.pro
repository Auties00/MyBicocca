# MyBicocca R8 configuration.
#
# Most third-party libraries on the classpath (Compose, Hilt/Dagger, Room, Coil,
# Ktor, Media3, MapLibre, Firebase, kotlinx.serialization, OSS-licenses) ship their
# own consumer ProGuard rules, so the bulk of keep logic is contributed automatically.
# The rules below cover the gaps that consumer rules do not, plus belt-and-suspenders
# keeps for reflection-driven code paths in this app.

# Keep readable crash traces: map line numbers, hide the original file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Attributes needed by reflective / serialization / generic-type machinery.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# -------------------------------------------------------------------------------------
# kotlinx.serialization
# The compiler plugin generates synthetic $serializer classes resolved by name at
# runtime. kotlinx-serialization ships consumer rules, but pin the generated members
# explicitly so every @Serializable type in the app and in the data-api DTO modules
# keeps its serializer regardless of how aggressively the rest is shrunk.
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class ** {
    public static **$* *;
}
-keep,includedescriptorclasses class it.attendance100.mybicocca.**$$serializer { *; }
-keepclassmembers class it.attendance100.mybicocca.** {
    *** Companion;
}
# Enum entries are matched by name during (de)serialization.
-keepclassmembers enum it.attendance100.mybicocca.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# -------------------------------------------------------------------------------------
# Jsoup — used to parse the scraped Esse3 / Moodle HTML. Reflection-free in practice,
# but it pulls in optional XML APIs that R8 cannot resolve on Android.
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# -------------------------------------------------------------------------------------
# Ktor / coroutines / slf4j — silence references to JVM-only optional integrations
# that are never reached on Android.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn kotlinx.coroutines.debug.**
-dontwarn io.ktor.**

# -------------------------------------------------------------------------------------
# MapLibre invokes a number of methods from native (JNI) code by name. Its AAR ships
# consumer rules, but keep the native-bridge surface defensively.
-keep class org.maplibre.android.** { *; }
-dontwarn org.maplibre.android.**

# -------------------------------------------------------------------------------------
# Standard Android keeps.
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep,allowobfuscation,allowshrinking class * extends java.lang.annotation.Annotation
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keep class com.google.android.gms.oss.licenses.** { *; }
-keep interface com.google.android.gms.oss.licenses.** { *; }
