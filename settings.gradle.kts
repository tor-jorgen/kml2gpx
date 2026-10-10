pluginManagement {
    val kotlinVersion: String = providers.gradleProperty("kotlinVersion").get()
    val shadowVersion: String = providers.gradleProperty("shadowVersion").get()
    val ktlintVersion: String = providers.gradleProperty("ktlintVersion").get()
    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.gradleup.shadow") version shadowVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    }
}

rootProject.name = "kml2gpx"
