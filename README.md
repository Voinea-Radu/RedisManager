![Build](../../actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/badge/Version-1.10.3-red.svg)

## Use

If you want to use an older version that is not avanible in https://repo.lightdream.dev you can try using https://archive-repo.lightdream.dev

<details>
  <summary>Maven</summary><blockquote>
  <details><summary>repo.lightdream.dev</summary>

```xml
<repositories>
    <repository>
        <id>lightdream-repo</id>
        <url>https://repo.lightdream.dev/</url>
    </repository>
</repositories>
```

```xml
<dependenies>
    <dependency>
        <groupId>dev.lightdream</groupId>
        <artifactId>redis-manager</artifactId>
        <version>1.10.3</version>
    </dependency>
</dependenies>
```

  </details>

  <details><summary  style="padding-left:25px">jitpack.io</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>com.github.L1ghtDream</groupId>
        <artifactId>redis-manager</artifactId>
        <version>1.10.3</version>
    </dependency>
</dependencies>
```

</blockquote></details>

</details>

<details><summary>Gradle</summary><blockquote>

  <details><summary>Groovy</summary><blockquote>

  <details><summary>repo.lightdream.dev</summary>

```groovy
repositories {
    maven("https://repo.lightdream.dev/")
}
```

```groovy
dependencies {
    implementation "dev.lightdream:redis-manager:1.10.3"
}
```
  </details>

  <details><summary>jitpack.io</summary>

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

```groovy
dependencies {
    implementation "com.github.L1ghtDream:redis-manager:1.10.3"
}
```
  </details>
</blockquote></details>

  <details>
    <summary>Kotlin</summary><blockquote>

  <details>
<summary>repo.lightdream.dev</summary>

```groovy
repositories {
    maven { url "https://repo.lightdream.dev/" }
}
```

```groovy
dependencies {
    implementation("dev.lightdream:redis-manager:1.10.3")
}
```
  </details>
  <details>
  <summary style="padding-left:50px">jitpack.io</summary>

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

```kotlin
dependencies {
    implementation("com.github.L1ghtDream:redis-manager:1.10.3")
}
```



</details>

  </blockquote></details>

</blockquote></details>



