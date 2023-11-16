plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "me.vldf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.graalvm.js:js:23.0.1")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "MainKt"
}
