plugins {
    id("java-library")
    id("maven-publish")
}

group = "dev.lightdream"
version = libs.versions.project.get()

repositories {
    mavenCentral()
    maven("https://repo.lightdream.dev/")
    maven("https://mvnrepository.com/artifact/redis.clients/jedis")
    maven("https://mvnrepository.com/artifact/org.jetbrains/annotations")
    maven("https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api")
}

dependencies {
    // LightDream
    api(libs.lightdream.logger)
    api(libs.lightdream.lambda)
    api(libs.lightdream.messagebuilder)
    api(libs.lightdream.filemanager)

    // Jedis
    api(libs.jedis)

    // Utils
    api(libs.reflections)
    api(libs.gson)

    // Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    api(libs.jetbrains.annotations)
    annotationProcessor(libs.jetbrains.annotations)
    testCompileOnly(libs.jetbrains.annotations)
    testAnnotationProcessor(libs.jetbrains.annotations)

    // Tests
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)

}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
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
        val githubURL = project.findProperty("github.url") ?: ""
        val githubUsername = project.findProperty("github.auth.username") ?: ""
        val githubPassword = project.findProperty("github.auth.password") ?: ""

        val lightdreamURL = project.findProperty("lightdream.url") ?: ""
        val lightdreamUsername = project.findProperty("lightdream.auth.username") ?: ""
        val lightdreamPassword = project.findProperty("lightdream.auth.password") ?: ""

        maven(url = githubURL as String) {
            name = "github"
            credentials(PasswordCredentials::class) {
                username = githubUsername as String
                password = githubPassword as String
            }
        }

        maven(url = lightdreamURL as String) {
            name = "self"
            credentials(PasswordCredentials::class) {
                username = lightdreamUsername as String
                password = lightdreamPassword as String
            }
        }
    }
}


tasks.register("publishGitHub") {
    dependsOn("publishMavenPublicationToGithubRepository")
    description = "Publishes to GitHub"
}

tasks.register("publishSelf") {
    dependsOn("publishMavenPublicationToSelfRepository")
    description = "Publishes to Self hosted repository"
}
