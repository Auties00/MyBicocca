plugins {
    application
}

dependencies {
    implementation(project(":core"))
    implementation("com.github.ajalt.clikt:clikt-core:5.1.0")

    // Readers, targets and resource builders are discovered at runtime through ServiceLoader.
    runtimeOnly(project(":readers:esse3"))
    runtimeOnly(project(":readers:moodle"))
    runtimeOnly(project(":readers:openapi"))
    runtimeOnly(project(":targets:kotlin"))
    runtimeOnly(project(":targets:swift"))
    runtimeOnly(project(":targets:web"))
    runtimeOnly(project(":resources"))
}

application {
    mainClass.set("it.attendance100.mybicocca.codegen.cli.MainKt")
}

// Paths given on the command line are relative to `common/`, which holds `specs/`.
tasks.named<JavaExec>("run") {
    workingDir = rootDir.parentFile
}
