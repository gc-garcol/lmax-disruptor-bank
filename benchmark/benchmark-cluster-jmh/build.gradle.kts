plugins {
    id("java-application-conventions")
    id("me.champeau.jmh") version "0.7.2"
}

group = "gc.garcol"
version = "0.0.1"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.logback)
    implementation(libs.lmaxDisruptor)
    implementation(project(":bank-libs:common"))
    implementation(project(":bank-libs:bank-cluster-proto"))
    compileOnly("org.projectlombok:lombok:${libs.versions.lombokVersion.get()}")
    annotationProcessor("org.projectlombok:lombok:${libs.versions.lombokVersion.get()}")

    // Protobuf
    runtimeOnly("io.grpc:grpc-netty-shaded:${libs.versions.grpcVersion.get()}")
    implementation("io.grpc:grpc-services:${libs.versions.grpcVersion.get()}")
    implementation("io.grpc:grpc-protobuf:${libs.versions.grpcVersion.get()}")
    implementation("io.grpc:grpc-stub:${libs.versions.grpcVersion.get()}")
    compileOnly("org.apache.tomcat:annotations-api:${libs.versions.annotationsApiVersion.get()}")
    implementation("com.google.protobuf:protobuf-java-util:${libs.versions.protocVersion.get()}")
}

tasks.test {
    useJUnitPlatform()
}
