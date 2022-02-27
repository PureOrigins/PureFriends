plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("io.papermc.paperweight.userdev") version "1.3.4"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
}

group = "it.pureorigins"
version = "1.0.0"

bukkit {
    name = project.name
    version = project.version.toString()
    main = "it.pureorigins.${project.name.toLowerCase()}.${project.name}"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    compileOnly("com.github.PureOrigins:PureCommon:0.2.1")
}

afterEvaluate {
    tasks {
        shadowJar {
            mergeServiceFiles()
        }
        
        jar {
            archiveClassifier.set("")
        }
        
        shadowJar {
            archiveClassifier.set("fat")
        }
        
        reobfJar {
            outputJar.set(shadowJar.get().archiveFile)
        }
        
        build {
            dependsOn(reobfJar)
        }
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.PureOrigins"
            artifactId = project.name
            version = project.version.toString()
            
            afterEvaluate {
                from(components["kotlin"])
                artifact(tasks["kotlinSourcesJar"])
            }
        }
    }
}