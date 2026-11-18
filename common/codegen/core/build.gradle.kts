dependencies {
    api(project(":model"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.yaml:snakeyaml:2.3")
    testImplementation(project(":testing"))
}
