import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.5.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    // id("io.papermc.paperweight.userdev") version "1.7.1"
}

group = rootProject.group
val rootDependencyDir = "${rootProject.group}.dependencies"
val dependencyDir = "${group}.bukkit.dependencies"
version = rootProject.version
base {
    archivesName.set("${rootProject.name}-Bukkit")
}

dependencies {
    // paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    implementation("org.incendo:cloud-paper:${VersionConstants.cloudMinecraftVersion}")
    implementation("org.bstats:bstats-bukkit:${VersionConstants.bstatsVersion}")
    implementation(project(":Common"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

tasks.withType<ShadowJar> {
    relocate("org.bstats", "${dependencyDir}.bstats")
}

bukkit {
    name = "ServerUtils"
    main = "net.frankheijden.serverutils.bukkit.ServerUtils"
    description = "A server utility"
    apiVersion = "1.21"
    website = "https://github.com/FrankHeijden/ServerUtils"
    softDepend = listOf("ServerUtilsUpdater")
    authors = listOf("FrankHeijden")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
        legacyPluginLoading.set(true)
    }
}
