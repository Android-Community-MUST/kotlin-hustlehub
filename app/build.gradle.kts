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

android {
    namespace = "must.kdroiders.hustlehub"
    compileSdk = 36

    defaultConfig {
        applicationId = "must.kdroiders.hustlehub"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${project.findProperty("SUPABASE_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Coroutines & Serialization
    implementation(libs.kotlinx.coroutines.android)
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

    // Detekt formatting rules
    detektPlugins(libs.detekt.formatting)

}
