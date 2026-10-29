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

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.model.** { *; }
-keep class com.example.data.local.dao.** { *; }

# Moshi / JSON Serialization
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}

# Coroutines & Coil
-dontwarn kotlinx.coroutines.**
-keep class coil.** { *; }
