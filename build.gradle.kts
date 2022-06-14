import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getenv

/*
 * Copyright 2021 Guiabolso
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    kotlin("jvm") version "1.4.31"
    `maven-publish`
    signing
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
}

group = "br.com.guiabolso"
version = getenv("RELEASE_VERSION") ?: "local"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    
    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.3.1")
}

detekt {
    autoCorrect = true
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val javadoc = tasks.named("javadoc")
val javadocsJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles java doc to jar"
    archiveClassifier.set("javadoc")
    from(javadoc)
}

publishing {

    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getenv("OSSRH_USERNAME")
                password = getenv("OSSRH_PASSWORD")
            }
        }
    }

    publications.register("mavenJava", MavenPublication::class) {
        from(components["java"])
        artifact(javadocsJar)
        artifact(sourcesJar.get())

//        Uncomment and fixme
//        pom {
//            name.set("Events Protocol")
//            description.set("Events Protocol")
//            url.set("https://github.com/GuiaBolso/events-protocol")
//
//            scm {
//                url.set("https://github.com/GuiaBolso/events-protocol")
//                connection.set("scm:git:https://github.com/GuiaBolso/events-protocol")
//            }
//
//            licenses {
//                license {
//                    name.set("Apache-2.0")
//                    url.set("https://opensource.org/licenses/Apache-2.0")
//                }
//            }
//
//            developers {
//                developer {
//                    id.set("Guiabolso")
//                    name.set("Guiabolso")
//                }
//            }
//        }
    }

}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    useGpgCmd()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    sign((extensions.getByName("publishing") as PublishingExtension).publications)
}