plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "ru.itmo"
version = "1.0-SNAPSHOT"

val ideType: String = System.getenv("cfg_ide_type") ?: "idea"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.xmlgraphics:batik-swing:1.17") {
        exclude("xml-apis")
        exclude("xerces")
    }
    implementation("guru.nidi:graphviz-kotlin:0.18.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(project(mapOf("path" to ":kotlin-cfg")))
    implementation(project(mapOf("path" to ":rust-cfg")))
    implementation(project(mapOf("path" to ":js-cfg")))
    implementation(project(mapOf("path" to ":csharp-cfg")))
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    // https://www.jetbrains.com/intellij-repository/snapshots
    // https://www.jetbrains.com/intellij-repository/releases
    when (ideType) {
        "rider" -> {
            version.set("2023.2.3")
            type.set("RD")
        }
        "idea" -> {
            version.set("2023.2.5")
            type.set("IU")
        }
        else -> error("unknown cfg_ide_type=$ideType")
    }
    this.downloadSources.set(true)


    plugins.set(listOf("JavaScript", "org.jetbrains.kotlin", "com.jetbrains.rust:232.20527.39"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
