val kml2gpxVersion = project.findProperty("kml2gpxVersion") as String
val javaLanguageVersion = project.findProperty("javaLanguageVersion") as String

plugins {
    id("com.gradleup.shadow")
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

group = "org.kml2gpx"
version = kml2gpxVersion

repositories {
    mavenCentral()
}

val dom4jVersion = "2.2.0"
val junitVersion = "6.1.3"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.dom4j:dom4j:$dom4jVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    manifest {
        attributes(mapOf("Main-Class" to "org.kml2gpx.MainKt"))
    }
}

tasks {
    build {
        // Generate fatjar on build
        dependsOn(shadowJar)
    }
}
