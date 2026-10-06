plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.avor42"
version = "0.0.8"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "Saga Orchestrator Library"
                url = "https://github.com/aVor42/saga-orchestrator"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPages"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    api("org.springframework:spring-tx:6.2.11")
    api("org.apache.kafka:kafka-clients:3.9.1")

}