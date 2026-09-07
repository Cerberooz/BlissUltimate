plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.citizensnpcs.co/repo/")
    maven("https://repo.minebench.de/")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    // WorldGuardHook and FireTier2Gem use both APIs directly.
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.18")

    // The gem NPC abilities use Citizens implementation traits (SkinTrait/LookClose),
    // so citizens-main is required in addition to its public API.
    compileOnly("net.citizensnpcs:citizens-main:2.0.41-SNAPSHOT")
    compileOnly("maven.modrinth:Tqg6E9V7:hLSJvPNb") // UltimateAdvancementAPI 2.7.2
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

sourceSets {
    named("main") {
        // Legacy reference material lives here, but is deliberately not plugin code.
        java.exclude("cz/solstate/**")
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        // The existing resource pack and persisted gem items rely on Paper's legacy
        // String lore/display-name and integer custom-model-data compatibility APIs.
        // Keep that targeted compatibility notice out of normal builds; other lint
        // categories are not suppressed.
        options.compilerArgs.add("-Xlint:-deprecation")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
