plugins {
    id("java")
    id("war")
}

group = "gaetanl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // SERVER
    implementation("org.springframework:spring-webmvc:6.0.12")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // TOMCAT
    // Careful with the chosen version of Tomcat,
    // Version 10 is compiled with/for Java 20,
    // Version 11 is compiled with/for java 21
    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.13")
    implementation("org.apache.tomcat:tomcat-jasper:10.1.13")

    // PERSISTENCE
        // JPA
        implementation("org.springframework.data:spring-data-jpa:3.1.4")

        // Hibernate
        implementation("org.hibernate.orm:hibernate-community-dialects:6.3.0.Final")
        implementation("org.hibernate:hibernate-entitymanager:5.6.15.Final")

        // H2
        implementation("com.h2database:h2:2.2.224")

    // TEST
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}