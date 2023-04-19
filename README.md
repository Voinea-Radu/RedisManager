
# RedisManager

![Build](../../actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/badge/Version-2.0.6-red.svg)

# Table Of Contents
1. [Description](#description)
2. [How to add to your project](#how-to-add-to-your-project)
3. [How to use](#how-to-use)

## Description
Jedis based Redis Manager. This lib allows you to send targeted and broadcast events through Redis as well as offering a wrapper for the base Jedis with a more user-friendly config.

## How to add to your project


The artifact can be found at the repository https://repo.lightdream.dev or https://jitpack.io (under com.github.L1ghtDream instead of dev.lightdream)

### Maven
```xml
<repositories>
    <repository>
        <id>lightdream-repo</id>
        <url>https://repo.lightdream.dev/</url>
    </repository>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>dev.lightdream</groupId>
        <artifactId>redis-manager</artifactId>
        <version>2.0.6</version>
    </dependency>
    <dependency>
        <groupId>com.github.L1ghtDream</groupId>
        <artifactId>redis-manager</artifactId>
        <version>2.0.6</version>
    </dependency>
</dependencies>
```

### Gradle - Groovy DSL
```groovy
repositories {
    maven { url "https://repo.lightdream.dev/" }
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "dev.lightdream:redis-manager:2.0.6"
    implementation "com.github.L1ghtDream:redis-manager:2.0.6"
}
```

### Gradle - Kotlin DSL
```kotlin
repositories {
    maven("https://repo.lightdream.dev/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.lightdream:redis-manager:2.0.6")
    implementation("com.github.L1ghtDream:redis-manager:2.0.6")
}
```

If you want to use an older version that is not available in https://repo.lightdream.dev you can try using https://archive-repo.lightdream.dev


## How to use

Can be found in the [repository](/src/main/java/example)
