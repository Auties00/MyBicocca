plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

application {
    mainClass.set("it.attendance100.mybicocca.codegen.esse3.MainKt")
}

dependencies {
    implementation("io.swagger.parser.v3:swagger-parser:2.1.25")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}
