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
    implementation(libs.lmaxDisruptor)
    implementation(libs.kafkaClient)
    implementation(project(":bank-libs:common"))
    implementation(project(":bank-libs:bank-cluster-proto"))
    implementation(project(":bank-cluster-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(libs.mysqlConnector)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

@Suppress("DEPRECATION")
val generatedDir = file("${buildDir}/generated/src/main/java")
val codecGeneration = configurations.create("codecGeneration")

application {
    mainClass.set("gc.garcol.bankcluster.BankClusterApplication")
}

sourceSets {
    main {
        java.srcDirs("src/main/java", generatedDir)
    }
}
