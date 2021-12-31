plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.2")
}