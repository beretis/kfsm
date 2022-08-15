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

// fun configureNative(srcSetMain: KotlinSourceSet, srcSetTest: KotlinSourceSet) {
//     srcSetMain.kotlin.srcDirs = listOf("src/nativeMain/kotlin")
//     srcSetTest.kotlin.srcDirs = listOf("src/nativeTest/kotlin")
// }

kotlin {

    // if (useTarget["jvm"] != null) {
    //     jvm {
    //         mavenPublication {
    //             artifactId = "${project.name}-jvm"
    //         }
    //     }
    // }
    // if (useTarget["js"]) {
    //     if (project.extra.has("nodeInstall")) {
    //         // tasks.findByPath(":implementationKotlinsJs")?.dependsOn("kotlinNodeJsSetup")
    //         tasks.findByPath(":kotlinNodeJsSetup")?.onlyIf { true }
    //     }
    //     js("js") {
    //         nodejs()
    //         browser()
    //         compilations.main {
    //             kotlinOptions {
    //                 metaInfo = true
    //                 sourceMap = true
    //                 verbose = true
    //                 moduleKind = "umd"
    //             }
    //         }
    //     }
    // }
    // if (useTarget["mingw"]) {
    //     mingwX64("mingw") {
    //         mavenPublication {
    //             artifactId = "${project.name}-mingwX64"
    //         }
    //     }
    // }
    // if (useTarget["linux"]) {
    //     linuxX64("linux") {
    //         mavenPublication {
    //             artifactId = "${project.name}-linuxX64"
    //         }
    //     }
    // }
    // if (useTarget["macos"]) {
    //     macosX64("macos") {
    //         mavenPublication {
    //             artifactId = "${project.name}-macosX64"
    //         }
    //     }
    // }
    // if (useTarget["wasm"]) {
    //     wasm32("wasm") {
    //         mavenPublication {
    //             artifactId = "${project.name}-wasm32"
    //         }
    //     }
    // }
    // sourceSets {
    //     val commonMain by getting {
    //         dependencies {
    //             implementation kotlin("stdlib-common")
    //
    //         }
    //     }
    //     val commonTest by getting {
    //         dependencies {
    //             implementation kotlin("test-common")
    //             implementation kotlin("test-annotations-common")
    //         }
    //     }
    //     // // THI IS COMPLETELY FUCKED UP
    //     // if named("(useTargetlistOf("jvm"))") {
    //     //     named("jvmMain") {
    //     //         named("dependencies") {
    //     //             implementation kotlin("stdlib-jdk8")
    //     //         }
    //     //     }
    //     //     named("jvmTest") {
    //     //         named("dependencies") {
    //     //             implementation kotlin("test")
    //     //             implementation kotlin("test-junit")
    //     //         }
    //     //     }
    //     // }
    //     // if named("(useTargetlistOf("js"))") {
    //     //     named("jsMain") {
    //     //         named("dependencies") {
    //     //             implementation kotlin("stdlib-js")
    //     //         }
    //     //     }
    //     //     named("jsTest") {
    //     //         named("dependencies") {
    //     //             implementation kotlin("test-js")
    //     //         }
    //     //         kotlin.srcDirs = listOf("src/jsTest/kotlin")
    //     //     }
    //     // }
    //     // if named("(useTargetlistOf("mingw"))") {
    //     //     configureNative(mingwMain, mingwTest)
    //     // }
    //     // if named("(useTargetlistOf("linux"))") {
    //     //     configureNative(linuxMain, linuxTest)
    //     // }
    //     // if named("(useTargetlistOf("macos"))") {
    //     //     configureNative(macosMain, macosTest)
    //     // }
    //     // if named("(useTargetlistOf("wasm"))") {
    //     //     configureNative(wasmMain, wasmTest)
    //     // }
    // }
}
