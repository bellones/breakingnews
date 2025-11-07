# Create SDK Scaffolding

## Summary
Establish the foundational project structure and scaffolding for Uplink SDKs across Android and iOS platforms, including core SDK modules, dependency management, build configurations, and project organization.

## Description

### Background
To support the development of modular SDKs for the Uplink platform, we need a well-organized, scalable project structure that:
- Supports multiple SDK modules
- Enables code sharing and dependency management
- Follows platform-specific best practices
- Facilitates easy integration and testing
- Allows for future SDK expansion

### Scope
- **Platforms**: Android and iOS
- **SDK Modules**: 
  - Core SDK (shared utilities, HTTP client, logging)
- **Project Organization**: Repository structure, build systems, dependency management

## Technical Implementation

### Repository Structure

```
uplink-sdks/
├── android/
│   ├── uplink-core-sdk/              # Core SDK module (Android Library)
│   ├── playground/                    # Test/example application
│   ├── build.gradle                   # Root build configuration
│   ├── settings.gradle                # Module definitions
│   └── gradle/                        # Gradle wrapper
├── ios/
│   ├── UplinkSDKiOS/                  # Unified SDK project (Xcode)
│   │   ├── UplinkCoreSDK/            # Core SDK source
│   │   └── UplinkSDKiOS.xcodeproj    # Xcode project
│   └── Playground/                    # Test/example application
└── docs/                              # Documentation
```

### Android Implementation

#### Project Structure
- **Build System**: Gradle with Kotlin DSL
- **Module Type**: Android Library (AAR)
- **Language**: Kotlin
- **Minimum SDK**: API 21 (Android 5.0)

#### Core SDK Module (`uplink-core-sdk`)
**Location**: `android/uplink-core-sdk/`

**Components Created**:
- `UplinkCoreClient.kt` - Main client class
- `HttpClient.kt` - HTTP client implementation
- `PeriodicTaskScheduler.kt` - Task scheduling utilities
- `Logger.kt` - Logging utilities
- `AndroidManifest.xml` - Module manifest
- `build.gradle` - Module build configuration
- `proguard-rules.pro` - ProGuard rules

**Dependencies**:
- Retrofit 2.x for HTTP networking
- OkHttp for HTTP client
- Kotlin Coroutines for async operations

**Build Configuration**:
```gradle
- Library module configuration
- ProGuard rules for release builds
- Test source sets
- Dependency declarations
```

#### Root Build Configuration
**Files Created**:
- `android/build.gradle` - Root build script
- `android/settings.gradle` - Module inclusion
- `android/gradle.properties` - Gradle properties
- `android/gradle/wrapper/` - Gradle wrapper files

**Features**:
- Project setup supporting future modules
- Dependency version management
- Build optimization settings
- Repository configurations

### iOS Implementation

#### Unified SDK Project Structure
**Location**: `ios/UplinkSDKiOS/`

**Project Type**: Xcode Framework Project
**Configuration**: XcodeGen (project.yml)
**Language**: Swift 5.9
**Minimum iOS**: 13.0

#### Core SDK Framework (`UplinkCoreSDK`)
**Location**: `ios/UplinkSDKiOS/UplinkCoreSDK/`

**Components Created**:
- `UplinkCoreClient.swift` - Main client class
- `HTTP/HttpClient.swift` - HTTP client implementation
- `Periodic/PeriodicTaskScheduler.swift` - Task scheduling
- `Utils/Logger.swift` - Logging utilities
- `Models/` - Data models directory

**Framework Configuration**:
- Framework target type
- Swift Package Manager for dependencies
- Alamofire (~> 5.8) dependency
- Module definition
- Info.plist generation

**Build Settings**:
- Deployment target: iOS 13.0
- Swift version: 5.9
- Code signing: Automatic
- Framework output configuration

#### Project Generation
**Configuration File**: `ios/UplinkSDKiOS/project.yml`

**Features**:
- XcodeGen project definition
- Framework target definitions
- Dependency management
- Build scheme configuration
- Swift Package Manager integration

**Regeneration Command**:
```bash
cd ios/UplinkSDKiOS
xcodegen generate
```

### Dependency Architecture

#### Dependency Graph
```
Application
└── uplink-core-sdk
    └── (future SDKs will depend on core-sdk)
```

#### Key Principles
- **Core SDK**: Shared utilities, HTTP client, logging
- **Modularity**: SDK is independently buildable and testable
- **Reusability**: Core SDK provides common functionality for future SDKs
- **Extensibility**: Structure supports adding feature SDKs that depend on Core SDK

### Build Systems

#### Android
- **Gradle**: Project structure supporting future modules
- **Kotlin DSL**: Type-safe build scripts
- **Gradle Wrapper**: Version-controlled build tool
- **Build Variants**: Debug/Release configurations

#### iOS
- **Xcode**: Native IDE and build system
- **XcodeGen**: Code-based project generation
- **Swift Package Manager**: External dependencies
- **CocoaPods**: Optional for app-level dependencies

### Documentation Structure

**Created**:
- `README.md` - Repository overview and quick start
- `ios/SETUP.md` - iOS-specific setup instructions
- `ios/UplinkSDKiOS/README.md` - SDK project documentation
- Code documentation (KDoc for Android, DocC for iOS)

## Build Instructions

### Android
```bash
# Build all modules
cd android
./gradlew build

# Build individual module
./gradlew :uplink-core-sdk:build

# Generate AAR file
./gradlew :uplink-core-sdk:assembleRelease
```

### iOS
```bash
# Generate Xcode project
cd ios/UplinkSDKiOS
xcodegen generate

# Build framework
xcodebuild -project UplinkSDKiOS.xcodeproj -scheme UplinkCoreSDK -sdk iphonesimulator build
```

## Testing Instructions

### Verification Checklist
- [ ] Android modules build successfully
- [ ] iOS frameworks build successfully
- [ ] Dependencies resolve correctly
- [ ] Module structure follows conventions
- [ ] Build configurations are correct
- [ ] Documentation is in place
- [ ] Project can be opened in IDEs (Android Studio, Xcode)
- [ ] Gradle/XcodeGen configurations are valid

### Test Scenarios
1. **Build Verification**
   - Build all Android modules
   - Build all iOS frameworks
   - Verify no build errors

2. **Dependency Resolution**
   - Verify Android dependencies resolve
   - Verify iOS SPM dependencies resolve
   - Check dependency versions

3. **Project Structure**
   - Verify directory structure matches specification
   - Check file organization
   - Validate naming conventions

4. **IDE Integration**
   - Open Android project in Android Studio
   - Open iOS project in Xcode
   - Verify syntax highlighting and autocomplete work

## Acceptance Criteria

- [x] Repository structure created for Android and iOS
- [x] Core SDK module created for Android (uplink-core-sdk)
- [x] Core SDK framework created for iOS (UplinkCoreSDK)
- [x] Build configurations set up for both platforms
- [x] Dependency management configured
- [x] Gradle project structure (Android)
- [x] XcodeGen project configuration (iOS)
- [x] Basic source files and classes created
- [x] Documentation structure in place
- [x] README files created with setup instructions
- [x] Projects build successfully
- [x] Code follows platform conventions
- [x] Project structure supports future SDK additions

## Dependencies

### Android
- Android Gradle Plugin 8.x
- Gradle 8.x
- Kotlin 1.9+
- Android SDK (API 21+)
- Android Studio

### iOS
- Xcode 15.0+
- Swift 5.9+
- iOS 13.0+ SDK
- XcodeGen (for project generation)
- CocoaPods (optional, for app-level dependencies)

## Technical Decisions

### Android
- **Gradle project structure**: Supports modular SDK development and future expansion
- **Kotlin**: Modern, type-safe language
- **Retrofit/OkHttp**: Industry-standard HTTP libraries
- **Coroutines**: Native async/await support

### iOS
- **Unified Xcode project**: Project structure supporting Core SDK and future SDK frameworks
- **XcodeGen**: Code-based project configuration for maintainability
- **Swift Package Manager**: Modern dependency management
- **Framework targets**: Proper framework output for distribution

### Cross-Platform
- **Modular architecture**: SDK is independently buildable
- **Core SDK pattern**: Foundation for future feature SDKs
- **Consistent naming**: Similar structure across platforms
- **Documentation**: Comprehensive setup and usage docs

## Future Considerations

### Planned Additions
- Unit test structure
- Integration test setup
- CI/CD pipeline configuration
- Code coverage setup
- Linting/formatting rules
- Version management strategy
- Distribution mechanism (Maven, CocoaPods, SPM)

### Scalability
- Structure supports adding new SDK modules in the future
- Dependency management is extensible
- Build system can handle additional modules
- Documentation structure is maintainable
- Core SDK provides foundation for feature SDKs

## Related Issues
- Playground app creation
- SDK implementation tasks
- Documentation tasks

## Labels
`sdk-scaffolding`, `infrastructure`, `android`, `ios`, `build-system`, `project-setup`, `foundation`

