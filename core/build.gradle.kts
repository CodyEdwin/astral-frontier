plugins {
    `java-library`
    eclipse
}

val gdxVersion: String by project
val gdxGltfVersion: String by project
val kryonetVersion: String by project
val kryoVersion: String by project
val gsonVersion: String by project
val gdxAiVersion: String by project

dependencies {
    // LibGDX Core
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-bullet:$gdxVersion")

    // GLTF support for animated 3D models
    api("com.github.mgsx-dev.gdx-gltf:gltf:$gdxGltfVersion")

    // Networking - exclude old kryo from kryonet to avoid conflicts
    api("com.esotericsoftware:kryonet:$kryonetVersion") {
        exclude(group = "com.esotericsoftware.kryo", module = "kryo")
        exclude(group = "com.esotericsoftware.minlog", module = "minlog")
    }
    api("com.esotericsoftware:kryo:$kryoVersion")

    // JSON Processing
    api("com.google.code.gson:gson:$gsonVersion")

    // AI
    api("com.badlogicgames.gdx:gdx-ai:$gdxAiVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
