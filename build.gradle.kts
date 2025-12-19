import java.util.Properties

plugins {
    java
    id("com.github.johnrengelman.shadow")
}

// Load gradle.properties for version information
val properties = Properties()
val propertiesFile = rootProject.file("gradle.properties")
if (propertiesFile.exists()) {
    propertiesFile.inputStream().use { properties.load(it) }
}

val javaVersion = properties.getProperty("java.version", "21")
val paperVersion = properties.getProperty("paper.version", "1.21.1-R0.1-SNAPSHOT")

group = "com.randomteleport"
version = "1.0.0"

// Configure Java version
java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

// Configure repositories
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

// Configure dependencies
dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
}

// Configure Shadow plugin for fat JAR
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    archiveBaseName.set(project.name)
}

// Make shadowJar the default build task
tasks.named("build") {
    dependsOn("shadowJar")
}

// Process resources to replace placeholders in plugin.yml
tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("name", project.name)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(
            "version" to project.version,
            "name" to project.name
        )
    }
}
