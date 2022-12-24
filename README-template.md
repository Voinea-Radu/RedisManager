# Logger

[![Build](../../actions/workflows/build.yml/badge.svg)](../../actions)
[![Version](https://img.shields.io/badge/Version-${{ env.VERSION }}-red.svg)](https://gitlab.com/lightdream-dev/api/-/packages)

## Use

<details>
  <summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>lightdream</id>
        <url>https://repo.lightdream.dev/</url>
    </repository>
    <!-- Other repositories -->
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.lightdream</groupId>
        <artifactId>${{ env.ARTIFACT }}</artifactId>
        <version>${{ env.VERSION }}</version>
    </dependency>
    <!-- Other dependencies -->
</dependencies>
```
</details>

<details>
  <summary>Gradle</summary>

```groovy
repositories {
    maven { url "https://repo.lightdream.dev/" }

    // Other repositories
}

dependencies {
    implementation "dev.lightdream:${{ env.ARTIFACT }}:${{ env.VERSION }}"

    // Other dependencies
}
```
</details>
