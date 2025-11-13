plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "io.github.lumine1909"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    implementation("io.github.lumine1909:reflexion:1.0.1")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    shadowJar {
        archiveClassifier.set("")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}