/*
 * Copyright (c) 2022. Open JumpCO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    maven ( url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/temporary" )
    maven ( url = "https://repo1.maven.org/maven2/" )
    maven("https://plugins.gradle.org/m2")

}

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.6.21"))
    implementation(gradleApi())
    implementation(localGroovy())
}
