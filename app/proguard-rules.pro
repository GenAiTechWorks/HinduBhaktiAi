# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

########################################
# 🔥 GENERAL SAFE RULES
########################################
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

########################################
# ✅ KEEP ACTIVITIES
########################################
-keep class * extends android.app.Activity

########################################
# ✅ ROOM DATABASE (IMPORTANT)
########################################
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.* class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

########################################
# ✅ YOUR ENTITY & DAO
########################################
-keep class com.hindu.lordpromptsai.entity.** { *; }
-keep interface com.hindu.lordpromptsai.dao.** { *; }

########################################
# ✅ YOUR MODELS (VERY IMPORTANT)
########################################
-keep class com.hindu.lordpromptsai.** { *; }

########################################
# ✅ ADMOB
########################################
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.**

########################################
# ✅ REMOVE LOGS
########################################
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

########################################
# 🔥 JSON / MODEL CLASSES
########################################
-keep class com.hindu.lordpromptsai.** {
    *;
}

########################################
# 🔥 MEDIA / EXOPLAYER
########################################
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

########################################
# 🔥 KEEP CUSTOM UTIL CLASSES
########################################
-keep class com.hindu.lordpromptsai.util.** { *; }

########################################
# 🔥 KEEP IMAGE MODEL
########################################
-keep class com.hindu.lordpromptsai.adapter.ImageItem { *; }

########################################
# 🔥 REMOVE LOGS (RELEASE CLEAN)
########################################
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}