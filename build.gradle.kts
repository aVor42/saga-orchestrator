plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.avor42"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
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

    implementation("org.slf4j:slf4j-api:2.0.17")
}