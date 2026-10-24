import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
}

subprojects {
    group = "it.attendance100.mybicocca.codegen"
    version = "0.1"

    // Container projects (e.g. `:readers`) only group modules and have no sources.
    if (childProjects.isNotEmpty()) return@subprojects

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:6.0.1"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"(kotlin("test"))
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(true)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        // `gradlew test -PupdateGolden=true` rewrites the targets' golden files.
        systemProperty("codegen.updateGolden", providers.gradleProperty("updateGolden").getOrElse("false"))
    }
}
