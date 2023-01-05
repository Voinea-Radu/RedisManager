# ${{ env.REPOSITORY_NAME }}

![Build](https://github.com/${{ env.REPOSITORY }}/actions/workflows/build.yml/badge.svg)
![Version](https://img.shields.io/badge/Version-${{ env.VERSION }}-red.svg)

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
        <groupId>${{ env.GROUP }}</groupId>
        <artifactId>${{ env.ARTIFACT }}</artifactId>
        <version>${{ env.VERSION }}</version>
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
        <groupId>com.github.${{ env.GITHUB_USERNAME }}</groupId>
        <artifactId>${{ env.ARTIFACT }}</artifactId>
        <version>${{ env.VERSION }}</version>
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
    implementation "${{ env.GROUP }}:${{ env.ARTIFACT }}:${{ env.VERSION }}"
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
    implementation "com.github.${{ env.GITHUB_USERNAME }}:${{ env.ARTIFACT }}:${{ env.VERSION }}"
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
    implementation("${{ env.GROUP }}:${{ env.ARTIFACT }}:${{ env.VERSION }}")
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
    implementation("com.github.${{ env.GITHUB_USERNAME }}:${{ env.ARTIFACT }}:${{ env.VERSION }}")
}
```



</details>

  </blockquote></details>

</blockquote></details>




