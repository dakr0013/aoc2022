plugins {
    kotlin("jvm") version "1.7.22"
}

repositories {
    mavenCentral()
}

tasks {
    sourceSets {
        main {
            java.srcDirs("src")
        }
    }

    wrapper {
        gradleVersion = "7.6"
    }

    test {
        useJUnitPlatform()
    }
}

//tasks.test {
//    useJUnitPlatform()
//}

dependencies {
    implementation(kotlin("test"))
}