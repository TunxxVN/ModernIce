buildscript {
    repositories { maven("https://maven.architectury.dev") }
    dependencies { classpath("dev.architectury:architectury-pack200:0.1.3") }
}

plugins {
    id("gg.essential.loom") version "1.9.26"
    java
}

val mod_version: String by project
version = mod_version
group = "com.tunxxvn.modernice"
base { archivesName.set("ModernIce-1.8.9-forge") }

val generateBuildInfo by tasks.registering {
    inputs.property("version", mod_version)
    val generated = layout.buildDirectory.dir("generated/sources/buildInfo")
    outputs.dir(generated)
    doLast {
        val source = generated.get().file("com/tunxxvn/modernice/BuildInfo.java").asFile
        source.parentFile.mkdirs()
        source.writeText("package com.tunxxvn.modernice;\npublic final class BuildInfo { public static final String VERSION = \"$mod_version\"; private BuildInfo() {} }\n")
    }
}

sourceSets.main { java.srcDir(generateBuildInfo) }

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val legacyLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    forge { pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter()) }
    runConfigs {
        "client" {
            property("fml.coreMods.load", "com.tunxxvn.modernice.core.ModernIceLoadingPlugin")
        }
        remove(getByName("server"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
}

tasks {
    withType<JavaCompile>().configureEach { options.release.set(8) }
    named<JavaExec>("runClient") { javaLauncher.set(legacyLauncher) }
    processResources {
        inputs.property("version", mod_version)
        filesMatching("mcmod.info") { expand("version" to mod_version) }
    }
    jar {
        manifest.attributes(
            "FMLCorePlugin" to "com.tunxxvn.modernice.core.ModernIceLoadingPlugin",
            "FMLCorePluginContainsFMLMod" to "true",
            "ModSide" to "CLIENT"
        )
    }
    remapJar { archiveClassifier.set("") }
}
