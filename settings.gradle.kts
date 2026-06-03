rootProject.name = "OfflineEncryptor"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    plugins {
        id("java")
        id("com.gradleup.shadow") version "9.4.1" apply false
        id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
        id("com.modrinth.minotaur") version "2.+"
    }
}

include(":common", ":paper", ":velocity", ":compatibility")