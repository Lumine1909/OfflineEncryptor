repositories {
    maven("https://repo.viaversion.com")
    maven("https://maven.leafmc.one/snapshots/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.viaversion:viaversion-api:5.7.1")
    compileOnly("cn.dreeam.leaf:leaf-api:1.21.11-R0.1-SNAPSHOT")
}