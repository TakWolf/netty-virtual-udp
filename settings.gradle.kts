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

rootProject.name = "netty-virtual-udp"

include(":netty-virtual-udp")
include(":examples:common")
include(":examples:game-server")
include(":examples:game-client")
