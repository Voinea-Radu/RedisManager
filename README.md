# RedisManager

![Build](../../actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/badge/Version-3.0.9-red.svg)

# Table Of Contents

1. [Description](#description)
2. [How to add to your project](#how-to-add-to-your-project)
3. [How to use](#how-to-use)

## Description

Jedis based Redis Manager. This lib allows you to send targeted and broadcast events through Redis as well as offering a wrapper for the base Jedis with a more user-friendly config.

## How to add to your project

The artifact can be found at the repository https://repo.lightdream.dev or https://jitpack.io (under
com.github.L1ghtDream instead of dev.lightdream)

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
        <version>3.0.9</version>
    </dependency>
    <dependency>
        <groupId>com.github.L1ghtDream</groupId>
        <artifactId>redis-manager</artifactId>
        <version>3.0.9</version>
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
    implementation "dev.lightdream:redis-manager:3.0.9"
    implementation "com.github.L1ghtDream:redis-manager:3.0.9"
}
```

### Gradle - Kotlin DSL

```kotlin
repositories {
    maven("https://repo.lightdream.dev/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.lightdream:redis-manager:3.0.9")
    implementation("com.github.L1ghtDream:redis-manager:3.0.9")
}
```

If you want to use an older version that is not available in https://repo.lightdream.dev you can try
using https://archive-repo.lightdream.dev

## How to use

### Create the main class

```java
public class ExampleMain implements RedisMain {

    private final RedisManager redisManager;
    private final RedisConfig redisConfig;
    private final Reflections reflections;
    private final Gson gson;

    public ExampleMain(Reflections reflections, Gson gson) {
        initializeRedisMain();

        this.reflections = reflections;
        this.redisConfig = new RedisConfig(); // This would usually be loaded from disk using a library like FileManager
        this.gson = gson;

        this.redisManager = new RedisManager();
    }

    @Override
    public @NotNull RedisManager getRedisManager() {
        return redisManager;
    }

    @Override
    public @NotNull RedisConfig getRedisConfig() {
        return redisConfig;
    }

    @Override
    public @NotNull Reflections getReflections() {
        return reflections;
    }

    @Override
    public @NotNull Gson getGson() {
        return gson;
    }
}
```

### Creating event

```java
public class ExampleEvent extends RedisEvent<Boolean> {

    public int data1;
    public String data2;

    public ExampleEvent(String redisTarget, int data1, String data2) {
        super(redisTarget);
        this.data1 = data1;
        this.data2 = data2;
    }
}
```

### Listen for events
```java
public class ExampleListener {
    @RedisEventHandler(autoRegister = true)
    public void onPing(PingEvent event) {
        event.respond(true);
    }
}
```

### Sending events
More methods for the RedisEvent class can be found in the JavaDocs

```java
public class ExampleListener {
    public void sendEvent(){
        new PingEvent("example_target").sendAndWait();
    }
}
```
