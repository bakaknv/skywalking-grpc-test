import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val protobufVersion = "3.11.4"
val grpcVersion = "1.28.1"
val coroutinesVersion = "1.3.5"
val testContainersVersion = "1.14.1"

plugins {
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.google.protobuf") version "0.8.8"
    id("com.google.cloud.tools.jib") version "1.8.0"
    kotlin("jvm") version "1.3.71"
    kotlin("kapt") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
    idea
}

group = "nkg.example"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
    mavenCentral()
}
apply(plugin = "com.google.cloud.tools.jib")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(group = "com.google.protobuf", name = "protobuf-java", version = protobufVersion)
    implementation(group = "io.grpc", name = "grpc-protobuf", version = grpcVersion)
    implementation(group = "io.grpc", name = "grpc-core", version = grpcVersion)
    implementation(group = "io.grpc", name = "grpc-stub", version = grpcVersion)
    implementation(group = "io.grpc", name = "grpc-netty-shaded", version = grpcVersion)
    implementation(group = "org.springframework.boot", name = "spring-boot-starter")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-actuator")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-web")
    implementation(group = "io.micrometer", name = "micrometer-core")
    implementation(group = "io.micrometer", name = "micrometer-registry-jmx")
    implementation(group = "io.micrometer", name = "micrometer-registry-statsd")
    implementation(group = "org.apache.kafka", name = "kafka-clients", version = "2.5.0")

    if (JavaVersion.current().isJava9Compatible) {
        implementation(group = "javax.annotation", name = "javax.annotation-api", version = "1.3.2")
    }
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-reactor", version = coroutinesVersion)
    implementation(group = "org.slf4j", name = "jcl-over-slf4j", version = "1.7.30")
    implementation(group = "com.github.aalobaidi", name = "aerospike-reactor", version = "0.1")
    implementation(group = "com.aerospike", name = "aerospike-client", version = "4.1.5")
    implementation(group = "io.netty", name = "netty-all", version = "4.1.22.Final")

    compileClasspath(group = "org.springframework.boot", name = "spring-boot-gradle-plugin", version = "2.2.6.RELEASE")
    compileClasspath(
        group = "com.google.cloud.tools.jib",
        name = "com.google.cloud.tools.jib.gradle.plugin",
        version = "1.8.0"
    )

    testImplementation(group = "com.nhaarman.mockitokotlin2", name = "mockito-kotlin", version = "2.2.0")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.6.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation(group = "org.testcontainers", name = "testcontainers", version = testContainersVersion)
    testImplementation(group = "org.testcontainers", name = "junit-jupiter", version = testContainersVersion)
    testImplementation(group = "org.testcontainers", name = "kafka", version = testContainersVersion)

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.6.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
