import java.util.Properties

// Plugins
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

/**
 * Release signing credentials, resolved from the gitignored `keystore.properties` at the
 * Gradle root when present, otherwise from the `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`,
 * `KEY_ALIAS`, and `KEY_PASSWORD` environment variables so CI can sign without a
 * checked-in secrets file. Keys: `storeFile` (path relative to the Gradle root),
 * `storePassword`, `keyAlias`, `keyPassword`. When neither source provides a keystore,
 * the release build is produced unsigned.
 */
val keystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

fun signingCredential(propertyKey: String, environmentKey: String): String? =
    keystoreProperties.getProperty(propertyKey) ?: System.getenv(environmentKey)

// Android config
android {
    signingConfigs {
        create("release") {
            val storePath = signingCredential("storeFile", "KEYSTORE_FILE")
            if (storePath != null) {
                storeFile = rootProject.file(storePath)
                storePassword = signingCredential("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingCredential("keyAlias", "KEY_ALIAS")
                keyPassword = signingCredential("keyPassword", "KEY_PASSWORD")
            }
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
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }

    /**
     * Per-ABI APK splits. MapLibre and the ML Kit barcode engine ship large native
     * libraries; a universal APK carries all four ABIs (~64 MB of .so), of which the
     * x86/x86_64 copies only matter to emulators and the handful of x86 Chromebooks.
     * Splitting emits one APK per ABI so a real device downloads only its own .so set,
     * and the universal APK is kept as an install-anywhere fallback (and for emulators).
     */
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
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
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/NOTICE.md"
        }
    }

    androidResources {
        // The offline basemap (.pmtiles) is read via memory-mapped, offset-based range requests
        // straight out of the APK. If AGP deflates it into the package those byte offsets no longer
        // line up with the stored bytes, so MapLibre's PMTilesFileSource reads garbage for the root
        // directory and aborts the process with a zlib "incorrect header check". Store it raw.
        noCompress += "pmtiles"

        // The app's UI ships only Italian (default) and English; locales_config.xml offers just
        // those two. Transitive AndroidX / Play Services / Material artifacts bundle dozens of
        // other translations that would otherwise bloat resources.arsc. Package only it + en.
        localeFilters += listOf("it", "en")
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
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
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Dependencies
dependencies {
    // Data API modules
    implementation("it.attendance100.mybicocca.data.remote:esse3:1.0")
    implementation("it.attendance100.mybicocca.data.remote:esse3-scraper:1.0") // Legacy Esse3 web-scrape client (Shibboleth SAML cookie session) for the flows with no REST surface(autocertificazioni)
    implementation("it.attendance100.mybicocca.data.remote:easystaff:1.0")
    implementation("it.attendance100.mybicocca.data.remote:affluences:1.0")
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

    // Unit Test dependencies (pure-JVM, JUnit 4 — Robolectric/Compose require JUnit 4)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("io.mockk:mockk:1.14.2")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation("com.google.truth:truth:1.4.4")

    // Android-runtime unit tests (Wave 2 — Robolectric): Room DAO, Hilt graph, Compose behaviour
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit-ktx:1.2.1")
    testImplementation("androidx.room:room-testing:2.8.4")
    testImplementation("com.google.dagger:hilt-android-testing:2.59.2")
    kspTest("com.google.dagger:hilt-android-compiler:2.59.2")

    // Android Test dependencies (instrumented — device/emulator: the Robolectric-deferred UI interactions)
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.03.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.14.2")
    androidTestImplementation("com.google.truth:truth:1.4.4")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("app.cash.turbine:turbine:1.2.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Navigation 3
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

    // ML Kit GenAI (Gemini Nano on supported devices): voice dictation.
    implementation("com.google.mlkit:genai-speech-recognition:1.0.0-alpha1")

    // HTML parsing of the syllabus htmlContent the Moodle public-info scrape gives us.
    implementation("org.jsoup:jsoup:1.18.3")

    // Ktor
    implementation("io.ktor:ktor-io:3.3.3")
    implementation("io.ktor:ktor-client-core:3.3.3")

    // Firebase Performance Monitoring
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-perf")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Shimmer effect for loading screens
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.3")

    // MapLibre
    implementation("org.maplibre.gl:android-sdk:13.2.0")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.2")

    // Email validation
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))

    // Media3
    val media3 = "1.10.1"
    implementation("androidx.media3:media3-exoplayer:$media3")
    implementation("androidx.media3:media3-exoplayer-hls:$media3")
    implementation("androidx.media3:media3-session:$media3")
    implementation("androidx.media3:media3-ui-compose-material3:$media3")

    // In-app file viewer (elearning course files)
    implementation("me.saket.telephoto:zoomable:0.19.0")
    implementation("me.saket.telephoto:zoomable-image-coil:0.19.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    implementation("io.coil-kt:coil-svg:2.7.0")
    implementation("dev.snipme:highlights:1.1.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Custom Tabs — in-app browser for external university pages (news, elections)
    implementation("androidx.browser:browser:1.9.0")

    // Device Type detection for UI adaptations
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0")

    // CameraX + ML Kit barcode
    val cameraX = "1.4.2"
    implementation("androidx.camera:camera-core:$cameraX")
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
}
