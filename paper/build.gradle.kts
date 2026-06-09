plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
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
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}