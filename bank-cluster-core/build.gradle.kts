plugins {
    id("java-library-conventions")
    id("com.google.protobuf") version libs.versions.protobufPluginVersion.get()
    idea // required for protobuf support in intellij
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.logback)
    implementation(libs.kafkaClient)
    implementation(libs.lmaxDisruptor)
    implementation(libs.agrona)
    implementation(libs.protobufJava)
    implementation(project(":bank-libs:common"))
    compileOnly("org.projectlombok:lombok:${libs.versions.lombokVersion.get()}")
    annotationProcessor("org.projectlombok:lombok:${libs.versions.lombokVersion.get()}")
}

@Suppress("DEPRECATION")
val generatedDir = file("${buildDir}/generated/src/main/java")

sourceSets {
    main {
        java.srcDirs("src/main/java", generatedDir)
    }
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:4.27.1"
    }
}
