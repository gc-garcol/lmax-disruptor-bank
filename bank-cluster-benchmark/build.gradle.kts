plugins {
    id("java-application-conventions")
    id("org.springframework.boot") version libs.versions.springBoot.get()
    id("io.spring.dependency-management") version libs.versions.springDependencyManagement.get()
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
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

@Suppress("DEPRECATION")
val generatedDir = file("${buildDir}/generated/src/main/java")

application {
    mainClass.set("gc.garcol.bankclusterbenchmark.BankClusterBenchmarkApplication")
}

sourceSets {
    main {
        java.srcDirs("src/main/java", generatedDir)
    }
}

tasks {
    task("run-benchmark-tool", JavaExec::class) {
        group = "run"
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass.set("gc.garcol.bankclusterbenchmark.BankClusterBenchmarkApplication")
    }
}
