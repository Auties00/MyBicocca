// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.59.2")
    }
}

plugins {
    id("com.android.application") version "9.1.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
    id("com.google.android.gms.oss-licenses-plugin") version "0.10.10" apply false
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("org.jetbrains.kotlin.jvm") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0" apply false
}