plugins {
    java
    id("com.gradleup.shadow")
}

group = "io.github.lumine1909"
version = "2.0.4"

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

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.velocitypowered.com/snapshots/")
        maven("https://repo.viaversion.com")
    }
    dependencies {
        implementation("io.github.lumine1909:reflexion:3.1.0")
        compileOnly("io.netty:netty-all:4.1.118.Final")
        compileOnly("com.viaversion:viaversion-api:5.5.1")
    }
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}
