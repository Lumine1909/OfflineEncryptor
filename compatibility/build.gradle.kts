repositories {
    maven("https://repo.viaversion.com")
    maven("https://maven.leafmc.one/snapshots/")
    maven("https://repo.william278.net/velocity") // Thank you william278!
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.nickuc.com/maven-releases/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.viaversion:viaversion-api:5.7.1")
    compileOnly("cn.dreeam.leaf:leaf-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(files("libs/FastLoginBukkit.jar"))
    compileOnly("fr.xephi:authme-core:6.0.0")
    compileOnly("com.nickuc.openlogin:openlogin-universal:1.3")
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.4.0-SNAPSHOT")
}