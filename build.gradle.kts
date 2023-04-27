plugins {
    id("java")
}

group = "io.novalite"
version = "1.9.15.2a"

repositories {
    mavenCentral()
    maven("https://repo.runelite.net")
    mavenLocal()
    flatDir {
        dirs("libs")
    }
}

dependencies {
    implementation(files("libs/allatori-annotations.jar"))
    compileOnly("io.novalite:novalite-commons:1.9.15.2a")
    compileOnly("net.runelite:client:1.9.15.2")
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly(group = "org.jetbrains", name = "annotations", version = "23.1.0")//version in kotlin-stdlib is 13.0
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

apply<MavenPublishPlugin>()

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

configure<PublishingExtension> {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}
