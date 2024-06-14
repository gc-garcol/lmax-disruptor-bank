plugins {
    id("java-library-conventions")
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.logback)
    implementation(libs.kafkaClient)
    implementation(libs.lmaxDisruptor)
    implementation(libs.agrona)
    implementation(project(":bank-libs:common"))
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

@Suppress("DEPRECATION")
val generatedDir = file("${buildDir}/generated/src/main/java")

sourceSets {
    main {
        java.srcDirs("src/main/java", generatedDir)
    }
}
