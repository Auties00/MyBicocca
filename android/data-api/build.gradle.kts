import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
    id("org.openapi.generator") version "7.17.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
}

group = "it.attendance100.mybicocca.data.api"
version = "1.0"

dependencies {
    // Ktor HTTP Client
    implementation("io.ktor:ktor-client-core:3.3.3")

    // Jsoup for HTML parsing
    implementation("org.jsoup:jsoup:1.18.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")

    // JUnit 5 (Jupiter) for modern Kotlin testing
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.platform:junit-platform-suite")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

// Expose test sources for submodules
val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

configurations {
    create("testArtifacts") {
        extendsFrom(configurations.testImplementation.get())
    }
}

artifacts {
    add("testArtifacts", testJar)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = rootProject.group
    version = rootProject.version

    dependencies {
        // Root project
        implementation(rootProject)

        // Ktor HTTP Client
        implementation("io.ktor:ktor-client-core:3.3.3")
        implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
        implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
        implementation("io.ktor:ktor-client-okhttp:3.3.3")

        // Ktor HTTP logging
        testImplementation("io.ktor:ktor-client-logging:3.3.3")
        testImplementation("org.slf4j:slf4j-simple:2.0.17")

        // Jsoup for HTML parsing
        implementation("org.jsoup:jsoup:1.18.3")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")

        // JUnit 5 (Jupiter) for modern Kotlin testing
        testImplementation(platform("org.junit:junit-bom:6.0.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testImplementation("org.junit.platform:junit-platform-suite")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        // Coroutines testing
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    }

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
