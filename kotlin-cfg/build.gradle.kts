plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "dev.bukreev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.cretz.kastree:kastree-ast-psi:0.4.0")
    implementation("guru.nidi:graphviz-kotlin:0.18.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
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
    mainClass.set("MainKt")
}