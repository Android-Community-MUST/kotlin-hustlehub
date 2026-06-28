import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.secrets.gradle.plugin)
}

fun keysProperty(
    key: String,
    defaults: String = "",
): String {
    val props = Properties()
    val file = File(rootProject.projectDir, "keys.properties")
    if (file.exists()) FileInputStream(file).use { props.load(it) }
    return props.getProperty(key, defaults)
}

android {
    namespace = "must.kdroiders.hustlehub"
    compileSdk = 37

    defaultConfig {
        applicationId = "must.kdroiders.hustlehub"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "GEMINI_API_KEY", "\"${keysProperty("gemini.api.key")}\"")
        buildConfigField("String", "MAPS_API_KEY", "\"${keysProperty("maps.api.key")}\"")
        buildConfigField("String", "BASE_URL", "\"${keysProperty("BASE_URL", "http://10.0.2.2:8080/api/v1/")}\"")
        buildConfigField("String", "WS_BASE_URL", "\"${keysProperty("WS_BASE_URL", "ws://10.0.2.2:8080/ws")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${keysProperty("GOOGLE_WEB_CLIENT_ID", "")}\"")
        resValue("string", "google_maps_key", keysProperty("maps.api.key"))
        resValue("string", "google_web_client_id", keysProperty("GOOGLE_WEB_CLIENT_ID", ""))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.adaptive.navigation3)
    // Hilt ViewModel support for Compose (hiltViewModel() used throughout existing screens)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.kotlin.metadata.jvm)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // STOMP WebSocket Client (Krossbow)
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.okhttp)

    // Media3 / ExoPlayer — voice note playback, audio focus, speed control
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    // Coroutines & Serialization
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)

    // Image Loading
    implementation(libs.coil.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)

    // Google Maps & Location
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)

    // Gemini AI
    implementation(libs.generativeai)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.okhttp)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.robolectric:robolectric:4.11.1")

    // Timber for logging
    implementation(libs.timber)

    // DataStore Preferences
    implementation(libs.datastore.preferences)

    // Credentials & Google Authentication
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)

    // Detekt formatting rules
    detektPlugins(libs.detekt.formatting)
}
