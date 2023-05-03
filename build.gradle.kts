plugins {
    id("java")
}

val rlVersion = "1.9.15.3"
group = "io.novalite"
version = rlVersion + "a"

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
    compileOnly("io.novalite:novalite-commons:$version")
    compileOnly("net.runelite:client:$rlVersion")
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly("org.jetbrains:annotations:23.1.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

apply<MavenPublishPlugin>()

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

configure<PublishingExtension> {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}
