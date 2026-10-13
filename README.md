# Saga Orchestrator

Saga orchestrator engine for java 17+ apps. This project is the first version of a component within a suite of libraries designed to simplify inter-service communication. 
Code documentation and usage examples will be included in future versions.

## 1. Include to project

### Maven
```xml
<repositories>
    <repository>
        <id>github-pages-maven</id>
        <url>https://avor42.github.io/java-libs/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.avor42</groupId>
        <artifactId>saga-orchestrator</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

### Gradle
By Kotlin DSL:
```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://avor42.github.io/java-libs/")
    }
}

dependencies {
    implementation("com.github.avor42:saga-orchestrator:2.0.0")
}
```

By Groovy DSL:
```groovy
repositories {
    mavenCentral()
    maven {
        url = uri("https://avor42.github.io/java-libs/")
    }
}

dependencies {
    implementation "com.github.avor42:saga-orchestrator:2.0.0"
}
```

## 2. Usage example

Coming soon...
in next versions