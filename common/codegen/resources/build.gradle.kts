// The builders of the APIs' resources: they crawl websites for the data the SDKs ship.
dependencies {
    implementation(project(":core"))
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(project(":testing"))
}
