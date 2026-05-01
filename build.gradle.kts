plugins {
    id("java-library")
    alias(libs.plugins.jmh)
}

group = "dev.simplified"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // JetBrains Annotations
    api(libs.annotations)

    // Logging
    api(libs.log4j2.api)

    // Gson (optional - only required by consumers that opt into the
    // dev.simplified.collection.gson.ConcurrentTypeAdapterFactory SPI)
    compileOnly(libs.gson)
    testImplementation(libs.gson)

    // Lombok Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // Tests
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.launcher)

    // Benchmarks (JMH)
    jmh(libs.jmh.core)
    jmh(libs.jmh.generator)
}

tasks {
    test {
        useJUnitPlatform {
            excludeTags("slow")
        }
    }
    register<Test>("slowTest") {
        description = "Runs slow integration tests (shutdown, thread leak detection)"
        group = "verification"
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
        useJUnitPlatform {
            includeTags("slow")
        }
    }
}

jmh {
    val include = providers.gradleProperty("jmhInclude").orNull
    if (include != null) includes.set(listOf(include))
    val forkProp = providers.gradleProperty("jmhFork").orNull
    if (forkProp != null) fork.set(forkProp.toInt())
    val warmupProp = providers.gradleProperty("jmhWarmup").orNull
    if (warmupProp != null) warmupIterations.set(warmupProp.toInt())
    val iterProp = providers.gradleProperty("jmhIter").orNull
    if (iterProp != null) iterations.set(iterProp.toInt())
}
