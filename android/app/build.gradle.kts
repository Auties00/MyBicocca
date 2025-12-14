import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

// Constants
val elearningSpec = "$rootDir/openapi/elearning/elearning.yaml"
val elearningBaseDir = "$buildDir/generated/openapi/elearning"

val bicoccappSpec = "$rootDir/openapi/bicoccapp/bicoccapp.yaml"
val bicoccappBaseDir = "$buildDir/generated/openapi/bicoccapp"

// Plugins
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.openapi.generator")
}

// Android config
android {
    namespace = "it.attendance100.mybicocca"
    compileSdk = 36

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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs(
                // "$elearningBaseDir/src/main/kotlin",
                "$bicoccappBaseDir/src/main/kotlin"
            )
        }
    }
}

ksp {
    arg("correctErrorTypes", "true")
}

// Dependencies
dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.10.0")
    implementation("androidx.wear.compose:compose-material3:1.5.6")

    // Core library desugaring for java.time API on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-android-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.10.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.9.6")
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

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.retrofit2:converter-scalars:3.0.0")

    // Moshi
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // OkHttp (Retrofit dependency)
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
}

// 1. Elearning codegen
val elearningTask = tasks.register<GenerateTask>("generateElearningClient") {
    generatorName.set("kotlin")
    inputSpec.set(elearningSpec)
    outputDir.set(elearningBaseDir)
    apiPackage.set("it.attendance100.mybicocca.data.api.elearning")
    modelPackage.set("it.attendance100.mybicocca.domain.model.elearning")
    packageName.set("it.attendance100.mybicocca.data.api.elearning")
    skipValidateSpec.set(true)
    configOptions.set(
        mapOf(
            "library" to "jvm-retrofit2",
            "serializationLibrary" to "moshi",
            "useCoroutines" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "collectionType" to "list",
            "supportNullable" to "true"
        )
    )
}

// 2. Bicoccapp codegen
val bicoccappTask = tasks.register<GenerateTask>("generateBicoccappClient") {
    generatorName.set("kotlin")
    inputSpec.set(bicoccappSpec)
    outputDir.set(bicoccappBaseDir)
    apiPackage.set("it.attendance100.mybicocca.data.api.bicoccapp")
    modelPackage.set("it.attendance100.mybicocca.domain.model.bicoccapp")
    packageName.set("it.attendance100.mybicocca.data.api.bicoccapp")
    configOptions.set(
        mapOf(
            "library" to "jvm-retrofit2",
            "serializationLibrary" to "moshi",
            "useCoroutines" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "collectionType" to "list",
            "supportNullable" to "true"
        )
    )
}

// TODO: S3

// Generate both APIs together
val generateAllApis = tasks.register("generateAllApis") {
    dependsOn(
        elearningTask,
        bicoccappTask
    )
}

// Hook into Android's pre-build task
tasks.named("preBuild") {
    dependsOn(generateAllApis)
}