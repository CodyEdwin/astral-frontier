plugins {
    java
    application
    id("com.android.application") version "8.2.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    // Only apply java plugin to non-Android projects
    if (name != "android") {
        apply(plugin = "java")

        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }
    /*apply(plugin = "maven-publish")
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/codyedwin/astral-frontier")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
                }
            }
        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
        }*/
}
