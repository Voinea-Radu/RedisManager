# RedisManager

![Build](../../actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/badge/Version-1.4.6-red.svg)

A lib that allows the creation of redis events to facilitate inter server communication.

## Use

### Maven

```xml

<repositories>
    <repository>
        <id>lightdream-repo</id>
        <url>https://repo.lightdream.dev/</url>
    </repository>
    <!-- Other repositories -->
</repositories>
```

```xml

<dependencies>
    <dependency>
        <groupId>dev.lightdream</groupId>
        <artifactId>RedisManager</artifactId>
        <version>1.4.6</version>
    </dependency>
    <!-- Other dependencies -->
</dependencies>
```

### Gradle

```groovy
repositories {
    maven { url "https://repo.lightdream.dev/" }

    // Other repositories
}

dependencies {
    implementation "dev.lightdream:RedisManager:1.4.6"

    // Other dependencies
}
```

## Example

Can be found in the [source code](/src/main/java/dev/lightdream/redismanager/example)
