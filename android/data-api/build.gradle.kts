import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
    id("de.jensklingenberg.ktorfit") version "2.7.1" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
}

group = "it.attendance100.mybicocca.data.api"
version = "1.0"

// Configuration for AspectJ weaver agent
val aspectjWeaver: Configuration by configurations.creating

dependencies {
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("io.ktor:ktor-http:3.3.3")

    // AspectJ for DTO logging during tests
    testImplementation("org.aspectj:aspectjrt:1.9.22")
    aspectjWeaver("org.aspectj:aspectjweaver:1.9.22")
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
        implementation(rootProject)
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
