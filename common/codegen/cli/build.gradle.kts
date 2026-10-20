plugins {
    application
}

dependencies {
    implementation(project(":core"))

    // Readers and targets are discovered at runtime through ServiceLoader.
    runtimeOnly(project(":readers:esse3"))
    runtimeOnly(project(":targets:kotlin"))
    runtimeOnly(project(":targets:swift"))
    runtimeOnly(project(":targets:web"))
}

application {
    mainClass.set("it.attendance100.mybicocca.codegen.cli.MainKt")
}

// Paths given on the command line are relative to `common/`, which holds `specs/`.
tasks.named<JavaExec>("run") {
    workingDir = rootDir.parentFile
}
