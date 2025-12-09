pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "astral-frontier"

include("core")
include("desktop")
include("server")
include("android")
