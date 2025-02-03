plugins {
    kotlin("multiplatform")
}

group = "com.mylosoftworks"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    js {
        nodejs()

        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":"))
        }
    }
}