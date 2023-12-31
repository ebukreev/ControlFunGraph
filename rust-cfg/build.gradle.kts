plugins {
    kotlin("jvm") version "1.9.20"
    application
    antlr
}

group = "me.avrong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

application {
    mainClass.set("MainKt")
}