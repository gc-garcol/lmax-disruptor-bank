plugins {
    id("java-library-conventions")
}

dependencies {
    implementation(project(":bank-libs:common"))
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("com.lmax:disruptor:4.0.0")
    implementation("org.agrona:agrona:1.21.2")
}

@Suppress("DEPRECATION")
val generatedDir = file("${buildDir}/generated/src/main/java")

sourceSets {
    main {
        java.srcDirs("src/main/java", generatedDir)
    }
}
