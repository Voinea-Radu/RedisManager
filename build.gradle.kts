plugins {
    id("java")
    id("maven-publish")
}

group = "dev.lightdream"
version = "1.10.4"

repositories {
    mavenCentral()
    maven("https://repo.lightdream.dev/")
    maven("https://mvnrepository.com/artifact/redis.clients/jedis")
    maven("https://mvnrepository.com/artifact/org.jetbrains/annotations")
    maven("https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api")
}

dependencies {

    // LightDream
    implementation("dev.lightdream:Logger:+")
    implementation("dev.lightdream:Lambda:+")
    implementation("dev.lightdream:reflections:+")

    // Lombok
    implementation("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    // Google
    implementation("com.google.code.gson:gson:2.10+")

    // Jedis
    implementation("redis.clients:jedis:4.4.0-m1")

    // JetBrains
    implementation("org.jetbrains:annotations:23.1.0")

}

configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(10, "seconds")
}

tasks.withType<Jar> {
    archiveFileName.set("${rootProject.name}.jar")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        var gitlabURL = project.findProperty("gitlab.url")
        var gitlabHeaderName = project.findProperty("gitlab.auth.header.name")
        var gitlabHeaderValue = project.findProperty("gitlab.auth.header.value")

        if (gitlabURL == null) {
            gitlabURL = ""
        }
        if (gitlabHeaderName == null) {
            gitlabHeaderName = ""
        }
        if (gitlabHeaderValue == null) {
            gitlabHeaderValue = ""
        }

        maven(url = gitlabURL as String) {
            credentials(HttpHeaderCredentials::class) {
                name = gitlabHeaderName as String
                value = gitlabHeaderValue as String
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}