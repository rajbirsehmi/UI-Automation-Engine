import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)
    
    testImplementation(libs.junit)
    testImplementation(libs.lint)
    testImplementation(libs.lint.tests)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Lint-Registry-v2" to "com.sehmi.engine.lint.EngineIssueRegistry")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.github.rajbirsehmi.UI-Engine"
            artifactId = "engine-lint"
            version = "0.3.0-alpha"
        }
    }
}
