import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow")
    kotlin("jvm")
}

group = "org.kml2gpx"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.dom4j:dom4j:2.1.4")
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("kml2gpx")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "org.kml2gpx.MainKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
