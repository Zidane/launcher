buildscript {
    repositories {
        maven("https://files.minecraftforge.net/maven")
        maven("https://repo.spongepowered.org/maven")
    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

plugins {
    // Apply the java-library plugin to add support for Java Library
    `java-library`
    id("net.minecraftforge.gradle")
    id("net.minecrell.licenser") version "0.4"
}

defaultTasks("licenseFormat", "build")

apply {
    plugin("org.spongepowered.mixin")
}

minecraft {
    mappings("snapshot", "20200119-1.14.3")
    runs {
        create("server") {
            workingDirectory(project.file("./run"))
            args.addAll(listOf("nogui", "--launchTarget", "launcher_server_dev"))
            main = "org.spongepowered.modlauncher.Main"
        }

        create("client") {
            environment("target", "client")
            workingDirectory(project.file("./run"))
            args.addAll(listOf("--launchTarget", "launcher_client_dev", "--version", "1.14.4", "--accessToken", "0"))
            main = "org.spongepowered.modlauncher.Main"
        }
    }
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://repo.spongepowered.org/maven")
}

license {
    header = file("HEADER.txt")
    newLine = false
    ext {
        this["name"] = project.name
        this["organization"] = "SpongePowered"
        this["url"] = "https://www.spongepowered.org"
    }

    include("**/*.java", "**/*.groovy", "**/*.kt")
}

dependencies {
    minecraft("net.minecraft:joined:1.14.4")
    implementation("org.spongepowered:plugin-spi:0.1.1-SNAPSHOT")
    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:21.0")
    implementation("com.google.inject:guice:4.0")
    implementation("cpw.mods:modlauncher:4.1.+")
    implementation("cpw.mods:grossjava9hacks:1.1.+")
    implementation("org.spongepowered:mixin:0.8+")
    implementation("org.spongepowered:configurate-json:3.6.1")
    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}
