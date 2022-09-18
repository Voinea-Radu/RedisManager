# RedisManager

![Build](../../actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/badge/Version-1.4.6-red.svg)

A logger lib that allows the separation of debugging logs and production logs.

## Use

### Maven

```xml

<repositories>
    <repository>
        <id>lightdream-repo</id>
        <url>https://repo.lightdream.dev/repository/LightDream-API/</url>
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
    maven { url "https://repo.lightdream.dev/repository/LightDream-API/" }

    // Other repositories
}

dependencies {
    implementation "dev.lightdream:RedisManager:1.4.6"

    // Other dependencies
}
```

## Example

Can be found in the [source code](/src/main/java/dev/lightdream/redismanager/example)
