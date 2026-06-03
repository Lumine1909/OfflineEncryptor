repositories {
    maven("https://maven.elytrium.net/repo/") // Thank you elytrium team!
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.4.0-SNAPSHOT")
    implementation(project(":common"))
    implementation(project(":compatibility"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf("version" to rootProject.version)
        inputs.properties(props)
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}