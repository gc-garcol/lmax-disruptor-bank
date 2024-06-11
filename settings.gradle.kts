pluginManagement {
    // Include 'plugins build' to define convention plugins.
    includeBuild("bank-libs/build-logic")
}
plugins {
    // Apply plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
include(
    "bank-libs:common",
    "bank-core",
    "bank-app"
)
rootProject.name = "lmax-disruptor-bank"
