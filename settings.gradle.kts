pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.0"
        id("com.gradleup.shadow") version "8.3.2"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "kml2gpx"
