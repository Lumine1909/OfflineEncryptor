plugins {
    java
    id("com.gradleup.shadow")
    id("com.modrinth.minotaur")
}

group = "io.github.lumine1909"
version = "2.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.velocitypowered.com/snapshots/")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":paper"))
    implementation(project(":velocity"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        archiveBaseName.set("OfflineEncryptor")
        archiveVersion.set(version.toString())
        archiveClassifier.set("")
        mergeServiceFiles()

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        subprojects.forEach { sub ->
            dependsOn(sub.tasks.jar)
            from(sub.tasks.jar.flatMap { it.archiveFile }.map { zipTree(it) })
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
}

modrinth {
    token.set(project.findProperty("modrinthKey") as? String ?: "")
    projectId.set("offlineencryptor")
    versionNumber.set(version as String)
    versionName.set("OfflineEncryptor $version")
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    loaders.addAll("bukkit", "paper", "purpur", "folia", "velocity")

    gameVersions.addAll(generateVersions("1.20", 5, 6))
    gameVersions.addAll(generateVersions("1.21", 0, 11))
    gameVersions.addAll(generateVersions("26.1", 0, 2))
}

fun generateVersions(mm: String, start: Int, end: Int): List<String> = (start..end).map { if (it == 0) mm else "$mm.$it" }

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.velocitypowered.com/snapshots/")
    }
    dependencies {
        implementation("io.github.lumine1909:reflexion:0.5.0")
        compileOnly("io.netty:netty-all:4.1.118.Final")
    }
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}