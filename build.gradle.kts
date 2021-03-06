plugins {
    java
}

group = "org.sandbox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.nanohttpd", "nanohttpd-nanolets", "2.3.1")
    implementation("org.apache.commons", "commons-lang3", "3.11")
    implementation("com.j256.ormlite", "ormlite-core", "5.3")
    implementation("com.j256.ormlite", "ormlite-jdbc", "5.3")
    implementation("com.h2database", "h2", "1.4.200")
    implementation("org.jetbrains", "annotations", "20.1.0")

    testImplementation("junit", "junit", "4.12")
}
