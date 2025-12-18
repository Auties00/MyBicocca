// Plugins
plugins {
    id("org.jetbrains.kotlin.jvm")
}

// Dependencies
dependencies {
    // JUnit 5 (Jupiter) for modern Kotlin testing
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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

    // Retrofit & OkHttp for test API clients
    testImplementation("com.squareup.retrofit2:retrofit:3.0.0")
    testImplementation("com.squareup.retrofit2:converter-gson:3.0.0")
    testImplementation("com.squareup.okhttp3:okhttp:5.3.2")
    testImplementation("com.squareup.okhttp3:logging-interceptor:5.3.2")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.retrofit2:converter-scalars:3.0.0")

    // Gson
    implementation("com.google.code.gson:gson:2.13.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // OkHttp (Retrofit dependency)
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
}