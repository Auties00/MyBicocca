pluginManagement {
    repositories {
        mavenLocal()
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

rootProject.name = "MyBicoccaDataAPI"
include(":bicoccapp")
include(":elearning")
include(":esse3-scraper")
include(":esse3")
include(":esse3:codegen")
include(":easystaff")