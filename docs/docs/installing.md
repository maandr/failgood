md title="docs/hello.md" {1-4}
---
sidebar_label: 'Installing' sidebar_position: 3
---

# First Steps

Let's say your job is to build a library that can reverse palindromes. You want to make sure that your code is really
production ready so you decide to write it using TDD. Also you want to use kotlin because it offers great performance (
JVM), and still just enough functional programming and immutability. But jvm and kotlin libraries are so enterprisey, so
you wonder is there something really lightweight and fast?

Then you find failgood. It has all the upsides of the jvm and kotlin but no bloat. This must be the right test runner to
build you string reverser.

you either

### create a new gradle project

create a new directory and paste this into `build.gradle.kts`

```kotlin
plugins {
    kotlin("jvm") version "1.5.30"
}


dependencies {
    testImplementation("dev.failgood:failgood:0.4.6")
    testImplementation("io.strikt:strikt-core:0.32.0")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}
```

next you write your first test:
`mkdir -p src/main/test/kotlin`

and there you create your first test: `ReverserTest.kt`

```
package com.bigcorp.reverser

import failgood.Test
import failgood.describe
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@Test
class ReverserTest {
    val context = describe(Reverser::class) {
        test("it can reverse palindromes") {
            expectThat(Reverser.reverse("racecar")).isEqualTo("racecar")
        }
    }
}
```
