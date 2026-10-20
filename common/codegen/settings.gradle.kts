pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "mybicocca-codegen"

include(":model")
include(":testing")
include(":core")
include(":readers:esse3")
include(":readers:moodle")
include(":readers:openapi")
include(":targets:kotlin")
include(":targets:swift")
include(":targets:web")
include(":cli")
