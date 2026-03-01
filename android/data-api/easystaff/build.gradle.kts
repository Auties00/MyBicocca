plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

dependencies {
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.16")

    // JUnit 5 (Jupiter) for modern Kotlin testing
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.platform:junit-platform-suite")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    // Selenium WebDriver for automated browser authentication
    testImplementation("org.seleniumhq.selenium:selenium-java:4.39.0")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:4.39.0")
    testImplementation("org.seleniumhq.selenium:selenium-devtools-v131:4.29.0")

    // WebDriverManager for automatic browser driver management
    testImplementation("io.github.bonigarcia:webdrivermanager:6.3.3")
}