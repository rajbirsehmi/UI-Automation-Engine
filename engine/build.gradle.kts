plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    id("maven-publish")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.dokka)
}

android {
    namespace = "com.sehmi.engine"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
        resourcePrefix = "engine_"
        consumerProguardFiles("consumer-rules.keep")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }

    publishing {
        multipleVariants("engine") {
            allVariants()
            withJavadocJar()
            withSourcesJar()
        }
    }

    flavorDimensions += "di"
    productFlavors {
        create("standard") {
            dimension = "di"
        }
        create("hilt") {
            dimension = "di"
            testInstrumentationRunner = "com.sehmi.engine.HiltTestRunner"
        }
    }
}

// Task to generate a Dokka-based Javadoc JAR
val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn("dokkaGeneratePublicationHtml")
    from(layout.buildDirectory.dir("dokka/html"))
    archiveClassifier.set("javadoc")
}

dependencies {
    // Pure Jetpack Compose UI Testing Framework
    api(platform(libs.androidx.compose.bom))

    // UiAutomator for system-level actions
    api(libs.androidx.uiautomator)

    // Core Android Test Library dependencies for the Engine
    api(libs.junit)
    api(libs.androidx.junit)
    api(libs.androidx.core.ktx)
    api(libs.androidx.test.runner)
    api(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.compose.ui)
    androidTestImplementation(libs.androidx.compose.foundation)

    // Logging (Log4j2)
    api(libs.log4j.api)
    implementation(libs.log4j.core)

    // Embed custom lint rules into the AAR
    lintPublish(project(":engine-lint"))

    // Hilt DI (Only included in the 'hilt' flavor)
    "hiltImplementation"(libs.hilt.android)
    "hiltImplementation"(libs.hilt.testing)
    "kspHilt"(libs.hilt.compiler)
    "androidTestHiltImplementation"(libs.hilt.testing)
    "kspAndroidTestHilt"(libs.hilt.compiler)
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            groupId = "com.github.rajbirsehmi.UI-Engine"
            artifactId = "robot-testing-engine"
            version = "0.3.0-alpha"

            afterEvaluate {
                from(components["engine"])
                artifact(tasks.named("dokkaJavadocJar"))
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/rajbirsehmi/UI-Engine")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
