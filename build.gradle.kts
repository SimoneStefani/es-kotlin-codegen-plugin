import org.gradle.api.publish.PublishingExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("com.diffplug.gradle.spotless") version "4.0.1"
    id("org.jetbrains.dokka") version "0.10.1"
    id("com.github.ben-manes.versions") version "0.28.0" // gradle dependencyUpdates -Drevision=release
    java
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")

repositories {
    mavenCentral()
}

group = "com.github.jillesvangurp"
version = "1.0"

// compile bytecode to java 8 (default is java 6)
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val elasticsearchVersion = "7.7.0"

dependencies {
    api("org.elasticsearch.client:elasticsearch-rest-high-level-client:$elasticsearchVersion")
    api("org.elasticsearch.client:elasticsearch-rest-client:$elasticsearchVersion")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.7")
    implementation("org.reflections:reflections:0.9.12")
    implementation("com.squareup:kotlinpoet:1.6.0")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    this.sourceFilesExtensions
}

gradlePlugin {
    plugins {
        create("codegen") {
            id = "com.github.jillesvangurp.codegen"
            implementationClass = "io.inbot.escodegen.EsKotlinCodeGenPlugin"
        }
    }
}

configure<PublishingExtension> {
    // for local testing
    repositories {
        maven("build/repository")
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

// Add a task to run the functional tests
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}
