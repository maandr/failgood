plugins {
    java
    `maven-publish`
    signing
}

val pub = "mavenJava-${project.name}"
publishing {
    repositories {
        maven {
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.properties["ossrhUsername"] as String?
                password = project.properties["ossrhPassword"] as String?
            }
        }
    }


    publications {
        create<MavenPublication>(pub) {
            from(components["java"])
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            pom {
                name.set("Fail Fast")
                description.set("a fast test runner for kotlin")
                url.set("https://github.com/christophsturm/failfast")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("christophsturm")
                        name.set("Christoph Sturm")
                        email.set("me@christophsturm.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/christophsturm/failfast.git")
                    developerConnection.set("scm:git:git@github.com:christophsturm/failfast.git")
                    url.set("https://github.com/christophsturm/failfast/")
                }
            }
        }
    }
}
java {
    @Suppress("UnstableApiUsage")
    withJavadocJar()
    @Suppress("UnstableApiUsage")
    withSourcesJar()
}

signing {
    sign(publishing.publications[pub])
}