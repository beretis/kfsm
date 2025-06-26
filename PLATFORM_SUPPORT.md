# KFSM Platform Support

This document outlines the comprehensive platform support available in the KFSM (Kotlin Finite State Machine) library.

## Supported Platforms

### JVM Platform
- **Target**: JVM 1.8+
- **Artifact**: `kfsm-jvm`
- **Features**: Full FSM functionality with coroutines support
- **Test Command**: `./gradlew jvmTest`

### JavaScript Platform
- **Target**: Browser and Node.js
- **Artifact**: `kfsm-js`
- **Module Type**: UMD (Universal Module Definition)
- **Features**: Full FSM functionality with JS coroutines
- **Test Command**: `./gradlew jsTest`

### WebAssembly Platform
- **Target**: WASM32
- **Artifact**: `kfsm-wasm32`
- **Features**: Native performance in web browsers
- **Test Command**: `./gradlew wasmTest`

## Native Platforms

### iOS Platform Support
- **iOS ARM64** (Device): `kfsm-iosArm64`
- **iOS X64** (Intel Simulator): `kfsm-iosX64` 
- **iOS ARM64 Simulator** (Apple Silicon): `kfsm-iosSimulatorArm64`
- **Features**: Native iOS app integration
- **Test Commands**: 
  - `./gradlew iosArm64Test`
  - `./gradlew iosX64Test`
  - `./gradlew iosSimulatorArm64Test`

### Android Platform Support
- **Target**: Android via JVM artifact
- **Artifact**: Use `kfsm-jvm` dependency
- **Integration**: Works with Android projects through Gradle
- **Sample Projects**: Available (see main README.adoc)
- **Features**: Full compatibility with Android runtime

### macOS Platform Support
- **macOS X64** (Intel): `kfsm-macosX64`
- **macOS ARM64** (Apple Silicon): `kfsm-macosArm64`
- **Features**: Native macOS application support
- **Test Commands**:
  - `./gradlew macosX64Test`
  - `./gradlew macosArm64Test`

### Linux Platform Support
- **Linux X64**: `kfsm-linuxX64`
- **Linux ARM64**: `kfsm-linuxArm64`
- **Features**: Native Linux application support
- **Test Commands**:
  - `./gradlew linuxX64Test`
  - `./gradlew linuxArm64Test`

### Windows Platform Support
- **Windows X64** (MinGW): `kfsm-mingwX64`
- **Features**: Native Windows application support
- **Test Command**: `./gradlew mingwTest`

## Platform Configuration

### Build Profiles
The library uses build profiles to control which platforms are built:

- **Default Profile**: `jvm,default,ios,macos,linux`
- **With JavaScript**: `jvm,js,default,ios,macos,linux`
- **All Platforms**: `jvm,js,default,ios,macos,linux,mingw,wasm`
- **Mobile Focus**: `jvm,ios,android`
- **Web Focus**: `js,wasm`

### Gradle Configuration
```gradle
// Default build (excludes JS)
profile=jvm,default,ios,macos,linux

// Include JavaScript
profile=jvm,js,default,ios,macos,linux

// All platforms
profile=jvm,js,default,ios,macos,linux,mingw,wasm
```

### Building with JavaScript
JavaScript support is available but not included in default builds. To build with JS support:

```bash
# Using gradle property
./gradlew build -Pprofile=jvm,js,default,ios,macos,linux

# Or modify gradle.properties temporarily
defaultProfile=jvm,js,default,ios,macos,linux
```

## Platform-Specific Features

### AsyncTimer Implementation
Each native platform has its own `AsyncTimer` implementation:
- **JVM**: Uses Java threading and schedulers
- **JS**: Uses JavaScript setTimeout/clearTimeout
- **Native**: Platform-specific timer implementations

### Coroutines Support
All platforms support Kotlin coroutines with appropriate implementations:
- **kotlinx-coroutines-core**: Common coroutines
- **kotlinx-coroutines-jdk8**: JVM-specific features
- **kotlinx-coroutines-core-js**: JavaScript event loop integration

## Usage Examples

### Gradle Dependency Configuration

#### JVM/Android Projects
```gradle
dependencies {
    implementation 'com.fireball.kfsm:kfsm-jvm:1.0.0'
}
```

#### Multiplatform Projects
```gradle
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation 'com.fireball.kfsm:kfsm:1.0.0'
            }
        }
    }
}
```

## Testing Platform Support

### Run All Platform Tests
```bash
./gradlew check
```

### Run Specific Platform Tests
```bash
# iOS Tests
./gradlew iosArm64Test iosX64Test

# macOS Tests  
./gradlew macosX64Test macosArm64Test

# Linux Tests
./gradlew linuxX64Test linuxArm64Test

# JVM and JS Tests
./gradlew jvmTest jsTest
```

## Cross-Platform Development

The KFSM library provides a consistent API across all platforms:

1. **State Machine Definition**: Identical across platforms
2. **Event Handling**: Consistent behavior
3. **Coroutines Integration**: Platform-appropriate implementations
4. **Testing**: Shared test suites run on all platforms

## Platform Requirements

- **Kotlin**: 1.9.20+
- **Gradle**: 8.5+
- **JVM**: Java 8+
- **Native Targets**: Appropriate platform toolchains

This comprehensive platform support enables KFSM to be used in:
- Mobile applications (iOS, Android)
- Web applications (Browser, Node.js)
- Desktop applications (macOS, Linux, Windows)
- Server applications (JVM)
- Embedded systems (Native targets)