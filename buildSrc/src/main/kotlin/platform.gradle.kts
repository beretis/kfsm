import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/*
 * Copyright (c) 2022. Open JumpCO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    kotlin("multiplatform")
}

val targetList = listOf("mingw", "linux", "macos", "js", "jvm", "wasm")
project.extra.set("useTarget", mutableMapOf<String, Boolean>())
val useTarget: MutableMap<String, Boolean> = project.extra["useTarget"] as MutableMap<String, Boolean>

val defaultProfile: String by project

targetList.forEach { target ->
    useTarget[target] = true
}

val profile = defaultProfile

if (profile.contains("default")) {
    if (org.gradle.internal.os.OperatingSystem.current().isWindows()) {
        useTarget["mingw"] = true
        logger.lifecycle("Detected ${org.gradle.internal.os.OperatingSystem.current()} using mingw")
    }
    if (org.gradle.internal.os.OperatingSystem.current().isLinux()) {
        useTarget["linux"] = true
        logger.lifecycle("Detected ${org.gradle.internal.os.OperatingSystem.current()} using linux")
    }
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX()) {
        useTarget["macos"] = true
        logger.lifecycle("Detected ${org.gradle.internal.os.OperatingSystem.current()} using macos")
    }
}
if (profile.contains("linux")) {
    useTarget["linux"] = true
}
if (profile.contains("mingw")) {
    useTarget["mingw"] = true
}
if (profile.contains("macos")) {
    useTarget["macos"] = true
}
if (profile.contains("wasm")) {
    useTarget["wasm"] = true
}

fun configureNative(srcSetMain: KotlinSourceSet, srcSetTest: KotlinSourceSet) {
    srcSetMain.kotlin.srcDirs("src/nativeMain/kotlin")
    srcSetTest.kotlin.srcDirs("src/nativeTest/kotlin")
}

kotlin {

    if (useTarget["jvm"] != null) {
        jvm {
            mavenPublication {
                artifactId = "${project.name}-jvm"
            }
        }
    }
    if (useTarget["js"] != null) {
        if (project.extra.has("nodeInstall")) {
            // tasks.findByPath(":implementationKotlinsJs")?.dependsOn("kotlinNodeJsSetup")
            tasks.findByPath(":kotlinNodeJsSetup")?.onlyIf { true }
        }
        js("js") {
            nodejs()
            browser()
            compilations.named("main").apply {
                this.get().kotlinOptions.apply {
                    metaInfo = true
                    sourceMap = true
                    verbose = true
                    moduleKind = "umd"
                }
            }
        }
    }
    if (useTarget["mingw"] != null) {
        mingwX64("mingw") {
            mavenPublication {
                artifactId = "${project.name}-mingwX64"
            }
        }
    }
    if (useTarget["linux"] != null) {
        linuxX64("linux") {
            mavenPublication {
                artifactId = "${project.name}-linuxX64"
            }
        }
    }
    if (useTarget["macos"] != null) {
        macosX64("macos") {
            mavenPublication {
                artifactId = "${project.name}-macosX64"
            }
        }
    }
    if (useTarget["wasm"] != null) {
        wasm32("wasm") {
            mavenPublication {
                artifactId = "${project.name}-wasm32"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:test-common")
                implementation("org.jetbrains.kotlin:test-annotations-common")
            }
        }
        // THI IS COMPLETELY FUCKED UP
        if (useTarget["jvm"] != null) {
            val jvmMain by getting {
                dependencies {
                    implementation("org.jetbrains.kotlin:stdlib-jvm")
                }
            }
            val jvmTest by getting {
                dependencies {
                    implementation("org.jetbrains.kotlin:test-junit")
                    implementation("org.jetbrains.kotlin:test")
                }
            }
        }
        if (useTarget["js"] != null) {
            val jsMain by getting {
                dependencies {
                    implementation("org.jetbrains.kotlin:stdlib-js")
                }
            }
            val jsTest by getting {
                dependencies {
                    implementation("org.jetbrains.kotlin:test-js")
                }
                kotlin.srcDirs("src/jsTest/kotlin")
            }
        }
        if (useTarget["mingw"] != null) {
            val mingwMain by getting
            val mingwTest by getting
            configureNative(mingwMain, mingwTest)
        }
        if (useTarget["linux"] != null) {
            val linuxMain by getting
            val linuxTest by getting
            configureNative(linuxMain, linuxTest)
        }
        if (useTarget["macos"] != null) {
            val macosMain by getting
            val macosTest by getting
            configureNative(macosMain, macosTest)
        }
        if (useTarget["wasm"] != null) {
            val wasmMain by getting
            val wasmTest by getting
            configureNative(wasmMain, wasmTest)
        }
    }
}
