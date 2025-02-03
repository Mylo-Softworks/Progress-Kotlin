import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.0"
    `maven-publish`
}

group = "com.mylosoftworks"
version = "1.0"

repositories {
    mavenCentral()
}

publishing {
    publications {
        // Not needed since this is KMP
//        create<MavenPublication>("maven") {
//            from(components["kotlin"])
//            groupId = "com.mylosoftworks"
//            artifactId = "Progress"
//            version = "1.0"
//        }
    }
}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        jvmMain.dependencies {
            implementation("org.jline:jansi:3.26.3")
        }
    }
}