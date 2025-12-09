plugins {
    id("com.android.application")
}

val gdxVersion: String by project

// Create natives configuration first
val natives: Configuration by configurations.creating

android {
    namespace = "com.astral.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.astral.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDirs("src/main/java")
            res.srcDirs("src/main/res")
            assets.srcDirs("../assets")
            jniLibs.srcDirs("libs")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/robovm/ios/robovm.xml",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {
    implementation(project(":core"))

    // LibGDX Android Backend
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")

    // LibGDX Native libraries for Android
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")

    // Bullet Physics for Android
    implementation("com.badlogicgames.gdx:gdx-bullet:$gdxVersion")
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-x86_64")

    // Desugaring for Java 11+ features on older Android
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}

// Copy native libraries to jniLibs
tasks.register("copyAndroidNatives") {
    doFirst {
        val armeabi = file("libs/armeabi-v7a/")
        val arm64 = file("libs/arm64-v8a/")
        val x86 = file("libs/x86/")
        val x86_64 = file("libs/x86_64/")

        armeabi.mkdirs()
        arm64.mkdirs()
        x86.mkdirs()
        x86_64.mkdirs()

        natives.files.forEach { jar ->
            val outputDir = when {
                jar.name.contains("natives-armeabi-v7a") -> armeabi
                jar.name.contains("natives-arm64-v8a") -> arm64
                jar.name.contains("natives-x86_64") -> x86_64
                jar.name.contains("natives-x86") -> x86
                else -> null
            }
            if (outputDir != null) {
                copy {
                    from(zipTree(jar))
                    into(outputDir)
                    include("*.so")
                }
            }
        }
    }
}

tasks.matching { it.name.contains("merge") && it.name.contains("JniLibFolders") }.configureEach {
    dependsOn("copyAndroidNatives")
}
