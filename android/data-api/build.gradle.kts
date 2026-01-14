import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0" apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
    id("de.jensklingenberg.ktorfit") version "2.7.1" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
}

group = "it.attendance100.mybicocca.data.api"
version = "1.0"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = rootProject.group
    version = rootProject.version

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
