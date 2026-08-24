pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://www.cursemaven.com") { content { includeGroup("curse.maven") } }
    }
}

rootProject.name = "immersive-weathering-sampler"

