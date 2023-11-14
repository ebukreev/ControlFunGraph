rootProject.name = "control-fun-graph"

include(
    "control-fun-graph-intellij",
    "kotlin-cfg",
    "rust-cfg",
    "csharp-cfg"
)

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
