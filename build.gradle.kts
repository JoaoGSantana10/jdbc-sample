plugins {
    id("java")
}

group = "br.com.dio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lombokVersion = "1.18.42"

dependencies {
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.flywaydb:flyway-core:10.11.1")
    implementation("org.flywaydb:flyway-mysql:10.11.1")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}
