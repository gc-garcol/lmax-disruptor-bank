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
    implementation(libs.kafkaClient)
    implementation(project(":bank-libs:common"))
    implementation(project(":bank-libs:bank-cluster-proto"))
    implementation(project(":bank-cluster-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    implementation("com.lmax:disruptor:4.0.0")
}

@Suppress("DEPRECATION")
val generatedDir = file("${buildDir}/generated/src/main/java")
val codecGeneration = configurations.create("codecGeneration")

application {
    mainClass.set("gc.garcol.bankapp.BankApplication")
}

sourceSets {
    main {
        java.srcDirs("src/main/java", generatedDir)
    }
}
