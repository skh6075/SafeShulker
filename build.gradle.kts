plugins {
    kotlin("jvm") version "2.0.10"
    `maven-publish`
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.skh6075.bukkit.safeshulker"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
}

tasks {
    shadowJar {
        archiveFileName.set("SafeShulker.jar")
        destinationDirectory.set(file("F:\\bukkit\\SafeShulker\\build-results"))
    }
}