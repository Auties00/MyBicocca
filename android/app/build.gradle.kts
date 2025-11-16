plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.android.gms.oss-licenses-plugin")
}

android {
  namespace = "it.attendance100.mybicocca"
  compileSdk = 36


  defaultConfig {
    applicationId = "it.attendance100.mybicocca"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  // androidResources{
  //     generateLocaleConfig = true
  // }

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
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.17.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
  implementation("androidx.activity:activity-compose:1.11.0")
  implementation(platform("androidx.compose:compose-bom:2025.11.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("com.google.android.material:material:1.13.0")
  implementation("androidx.compose.foundation:foundation:1.9.4")
  implementation("androidx.compose.ui:ui-tooling-preview:1.9.4")
  implementation("androidx.compose.ui:ui-text-google-fonts:1.9.4")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
  androidTestImplementation(platform("androidx.compose:compose-bom:2025.11.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
  implementation("io.coil-kt:coil-compose:2.7.0")
  implementation("androidx.navigation:navigation-compose:2.9.6")
  implementation("com.google.android.gms:play-services-oss-licenses:17.3.0")
  implementation("androidx.biometric:biometric:1.1.0")

  // Vico for charts
  implementation("com.patrykandpatrick.vico:compose:2.3.5")
  implementation("com.patrykandpatrick.vico:compose-m3:2.3.5")
  implementation("com.patrykandpatrick.vico:core:2.3.5")
  debugImplementation("androidx.compose.ui:ui-tooling:1.9.4")
}