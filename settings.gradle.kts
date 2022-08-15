pluginManagement {
    repositories {
        mavenLocal()
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/temporary")
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("com.gradle.enterprise") version "3.6.3"
}
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

rootProject.name = "kfsm"
