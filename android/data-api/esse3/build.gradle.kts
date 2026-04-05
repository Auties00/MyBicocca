plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}
dependencies {
    testImplementation(kotlin("test"))
}