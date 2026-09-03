# =============================================================================
# HustleHub App - ProGuard / R8 Rules
# =============================================================================

# Keep source file names and line numbers for crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signatures & annotations (required for Retrofit, Moshi, Gson, Firebase)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# =============================================================================
# FIREBASE & GOOGLE PLAY SERVICES
# =============================================================================
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Auth (CRITICAL: prevents auth callbacks and recaptcha stripping)
-keep class com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** { *; }

# Firebase Messaging (FCM: service and tokens must not be stripped)
-keep class com.google.firebase.messaging.** { *; }
-keepclassmembers class com.google.firebase.messaging.** { *; }

# Firebase Analytics & Crashlytics
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keep public class * extends java.lang.Exception

# Firebase Database & Firestore
-keep class com.google.firebase.database.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** { *; }

# Firebase Sessions & Installations
-keep class com.google.firebase.sessions.** { *; }
-keep class com.google.firebase.installations.** { *; }

# Google Play Services & Play Integrity / Recaptcha
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**
-keep class com.google.android.recaptcha.** { *; }
-dontwarn com.google.android.recaptcha.**

# =============================================================================
# GOOGLE SIGN-IN / CREDENTIAL MANAGER
# =============================================================================
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keepclassmembers class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# =============================================================================
# APP DATA MODELS & DTOs
# =============================================================================
-keep class must.kdroiders.hustlehub.**.dto.** { *; }
-keepclassmembers class must.kdroiders.hustlehub.**.dto.** { *; }
-keep class must.kdroiders.hustlehub.**.model.** { *; }
-keepclassmembers class must.kdroiders.hustlehub.**.model.** { *; }
-keep class must.kdroiders.hustlehub.**.entity.** { *; }
-keepclassmembers class must.kdroiders.hustlehub.**.entity.** { *; }
-keep class must.kdroiders.hustlehub.data.model.** { *; }
-keepclassmembers class must.kdroiders.hustlehub.data.model.** { *; }

# Respect Android's native @Keep annotation
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# =============================================================================
# SERIALIZATION (Moshi, Gson, Kotlinx.Serialization)
# =============================================================================
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.google.gson.annotations.SerializedName <fields>;
}

-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-dontwarn kotlinx.serialization.**

# =============================================================================
# NETWORKING (Retrofit, OkHttp, Okio)
# =============================================================================
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep interface must.kdroiders.hustlehub.**.remote.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# =============================================================================
# WEBSOCKET & STOMP (Krossbow & Ktor)
# =============================================================================
-dontwarn org.hildan.krossbow.**
-keep class org.hildan.krossbow.** { *; }

-dontwarn io.ktor.util.KtorDsl
-dontwarn io.ktor.utils.io.core.ByteReadPacket
-dontwarn io.ktor.utils.io.core.Input
-dontwarn io.ktor.**

# =============================================================================
# GOOGLE AI (Gemini)
# =============================================================================
-dontwarn com.google.ai.client.generativeai.**
-keep class com.google.ai.client.generativeai.** { *; }

# =============================================================================
# HILT & VIEWMODELS
# =============================================================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-dontwarn dagger.hilt.**
-dontwarn dagger.**

-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class *

-keep class * extends androidx.lifecycle.ViewModel {
    @javax.inject.Inject <init>(...);
    <init>();
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    @javax.inject.Inject <init>(...);
}

-keep class **_HiltModules* { *; }
-keep class **_Factory { *; }
-keep class **_Provide*Factory { *; }
-keep class **_MembersInjector { *; }

# =============================================================================
# GOOGLE MAPS & LOCATION
# =============================================================================
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.maps.model.** { *; }
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.maps.android.**

# =============================================================================
# ROOM (Local Database)
# =============================================================================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# =============================================================================
# COIL (Image Loading)
# =============================================================================
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# =============================================================================
# KOTLIN COROUTINES
# =============================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# =============================================================================
# ANDROID INTERNALS & IPC
# =============================================================================
# Keep Parcelable implementations across activities
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =============================================================================
# WORKMANAGER & HILWORKER (Background Uploads & Sync)
# =============================================================================
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep @androidx.hilt.work.HiltWorker class * { *; }

# =============================================================================
# MEDIA3 / EXOPLAYER (Voice Notes Playback)
# =============================================================================
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.extractor.** { *; }
-keep class androidx.media3.common.** { *; }
-dontwarn androidx.media3.**

# =============================================================================
# JETPACK SECURITY / TINK (EncryptedSharedPreferences & E2EE Key Storage)
# =============================================================================
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# =============================================================================
# SHORTCUTBADGER (App Icon Launcher Unread Badges)
# =============================================================================
-keep class me.leolin.shortcutbadger.** { *; }
-keep class me.leolin.shortcutbadger.impl.** { *; }
-dontwarn me.leolin.shortcutbadger.**

# =============================================================================
# TIMBER (Strip Debug & Verbose Logging in Release)
# =============================================================================
-dontwarn timber.log.**
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
}