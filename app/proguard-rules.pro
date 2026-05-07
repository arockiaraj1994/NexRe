-keep class com.mindshift.nexre.data.remote.model.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep class com.squareup.moshi.** { *; }

# errorprone annotations are compile-time only — not present at runtime
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# Tink (used by EncryptedSharedPreferences)
-dontwarn com.google.crypto.tink.**

# OkHttp / Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
