dependencies {
    implementation(project(":common"))
    compileOnly("com.viaversion:viaversion-api:5.12.2")
    compileOnly("cn.dreeam.leaf:leaf-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(files("libs/FastLoginBukkit.jar"))
    compileOnly(files("libs/AuthMe-6.0.0-Paper.jar"))
    compileOnly("com.velocitypowered:velocity-api:3.6.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.6.0-SNAPSHOT")
}