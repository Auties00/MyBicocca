import java.util.Properties

// Plugins
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Google Maps key — kept out of VCS in local.properties (already git-ignored). Falls back to
// an empty string so the project still builds without it (the map tiles just render blank).
val mapsApiKey: String = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}.getProperty("MAPS_API_KEY", "")

// Android config
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore_release_key")
            storePassword = "mybicocca"
            keyAlias = "releasekey"
            keyPassword = "mybicocca"
        }
    }
    namespace = "it.attendance100.mybicocca"
    compileSdk = 37

    defaultConfig {
        applicationId = "it.attendance100.mybicocca"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Injected into the Google Maps <meta-data> in the manifest.
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("correctErrorTypes", "true")
}

// Dependencies
dependencies {
    // Data API modules
    implementation("it.attendance100.mybicocca.data.remote:esse3:1.0")
    implementation("it.attendance100.mybicocca.data.remote:easystaff:1.0")
    implementation("it.attendance100.mybicocca.data.remote:elearning:1.0")

    // Android
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.5.0-alpha19")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.10.0")
    implementation("androidx.compose.foundation:foundation:1.11.2")
    implementation("androidx.compose.ui:ui-graphics:1.10.0")

    // Core library desugaring for java.time API on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // ViewModel + Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    // ProcessLifecycleOwner — app-wide foreground/background signal for the app lock.
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Android Test dependencies
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.05.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Navigation 3. runtime = NavKey/NavBackStack, ui = NavDisplay,
    // lifecycle-viewmodel-navigation3 = rememberViewModelStoreNavEntryDecorator for VM scoping.
    implementation("androidx.navigation3:navigation3-runtime:1.1.0")
    implementation("androidx.navigation3:navigation3-ui:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.3.0")
    implementation("androidx.biometric:biometric:1.1.0")

    // Vico for charts
    implementation("com.patrykandpatrick.vico:compose:2.3.6")
    implementation("com.patrykandpatrick.vico:compose-m3:2.3.6")
    implementation("com.patrykandpatrick.vico:core:2.3.6")
    debugImplementation("androidx.compose.ui:ui-tooling:1.10.0")

    // Haze for blur
    implementation("dev.chrisbanes.haze:haze:1.7.1")
    implementation("dev.chrisbanes.haze:haze-materials:1.7.1")

    // Kotlinx Serialization (for Room type converters)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // ML Kit GenAI (Gemini Nano on supported devices): search-query interpretation + dictation.
    // Both self-gate at runtime — unsupported devices fall back to deterministic search and
    // the platform SpeechRecognizer.
    implementation("com.google.mlkit:genai-prompt:1.0.0-beta2")
    implementation("com.google.mlkit:genai-speech-recognition:1.0.0-alpha1")

    // Jsoup — HTML parsing of the syllabus htmlContent the Moodle public-info
    // scrape gives us. Data-api ships it as `implementation`, so we need our own
    // explicit dep to use it in the app's mappers.
    implementation("org.jsoup:jsoup:1.18.3")

    // Ktor
    implementation("io.ktor:ktor-io:3.3.3")
    implementation("io.ktor:ktor-client-core:3.3.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Shimmer effect for loading screens
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.3")

    // Google Maps — campus map (maps-compose) + the underlying SDK.
    implementation("com.google.maps.android:maps-compose:6.4.1")
    implementation("com.google.android.gms:play-services-maps:19.2.0")

    // Email validation
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))

    // Media3 — Kaltura video playback in elearning. Compose-native UI (1.10+),
    // no PlayerView/AndroidView interop.
    val media3 = "1.10.1"
    implementation("androidx.media3:media3-exoplayer:$media3")
    implementation("androidx.media3:media3-exoplayer-hls:$media3")
    implementation("androidx.media3:media3-session:$media3")
    implementation("androidx.media3:media3-ui-compose-material3:$media3")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Custom Tabs — in-app browser for external university pages (news, elections)
    implementation("androidx.browser:browser:1.9.0")

    // Device Type detection for UI adaptations
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0")
}
