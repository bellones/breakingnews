# Create Playground App for Testing SDKs

## Summary
Create a cross-platform Playground application for testing and demonstrating Uplink SDK functionality on both Android and iOS platforms.

## Description

### Background
To facilitate development, testing, and demonstration of the Uplink SDKs, we need a dedicated Playground application that allows developers to interact with SDK features in a controlled environment. This app serves as both a testing tool and a reference implementation.

### Scope
- **Platforms**: Android and iOS
- **Purpose**: Testing and demonstration of Uplink SDK features
- **SDKs Integrated**:
  - UplinkCoreSDK (Core utilities, HTTP client, logging)

## Technical Implementation

### iOS Implementation

#### Project Structure
- **Location**: `ios/Playground/`
- **Project Type**: iOS Application (Xcode workspace)
- **Dependencies**:
  - UplinkCoreSDK (via unified UplinkSDKiOS project)
  - SnapKit (~> 5.6.0) for UI layout
  - Alamofire (via UplinkCoreSDK SPM dependency)

#### Features Implemented
1. **Core SDK Testing UI**
   - HTTP client testing interface
   - Logger testing interface
   - Task scheduler testing interface
   - Status display area

2. **Architecture**
   - MVVM pattern with ViewModels
   - Programmatic UI using SnapKit
   - Async/await for SDK operations

3. **Project Configuration**
   - Xcode workspace with unified SDK project integration
   - CocoaPods for dependency management
   - Automatic framework embedding
   - Build scripts for SDK framework integration

#### Key Files
- `AppDelegate.swift` - Application entry point
- `CoreSDKTestViewController.swift` - Main view controller
- `CoreSDKTestView.swift` - UI layout
- `CoreSDKViewModel.swift` - Business logic
- `project.yml` - XcodeGen configuration
- `Podfile` - CocoaPods dependencies

### Android Implementation

#### Project Structure
- **Location**: `android/playground/`
- **Project Type**: Android Application (Gradle)
- **Dependencies**:
  - UplinkCoreSDK (local module)

#### Features Implemented
1. **Core SDK Testing UI**
   - HTTP client testing interface
   - Logger testing interface
   - Task scheduler testing interface
   - Material Design components
   - Kotlin coroutines for async operations

2. **Architecture**
   - MVVM pattern
   - ViewBinding for UI
   - Coroutines for async SDK calls

#### Key Files
- `MainActivity.kt` - Main activity
- `CoreSDKViewModel.kt` - ViewModel
- `activity_main.xml` - UI layout
- `build.gradle` - Dependencies configuration

## Build Instructions

### iOS
```bash
cd ios/Playground
pod install
open Playground.xcworkspace
# Build and run in Xcode
```

### Android
```bash
cd android
./gradlew :playground:assembleDebug
# Install on device/emulator
```

## Testing Instructions

### Manual Testing
1. Launch the Playground app on device/simulator
2. Test Core SDK functionality:
   - **HTTP Client**: Test HTTP requests (GET, POST, DELETE)
   - **Logger**: Test logging functionality (debug, info, error)
   - **Task Scheduler**: Test periodic task scheduling
3. Verify status messages are displayed correctly
4. Test error handling scenarios

### Test Scenarios
- [ ] App launches successfully
- [ ] UI elements are visible and properly laid out
- [ ] HTTP client operations work correctly
- [ ] Logger displays log messages appropriately
- [ ] Task scheduler schedules and executes tasks
- [ ] Error messages are displayed appropriately
- [ ] App handles network errors gracefully
- [ ] App works on both simulator/emulator and physical devices

## Acceptance Criteria

- [x] Playground app created for iOS
- [x] Playground app created for Android
- [x] Both apps integrate UplinkCoreSDK
- [x] UI provides buttons for all major Core SDK operations
- [x] Status/feedback is displayed to user
- [x] Apps build successfully
- [x] Apps run on simulators/emulators
- [x] Apps can be installed on physical devices
- [x] Documentation provided for setup and usage

## Dependencies

### iOS
- Xcode 15.0+
- iOS 13.0+
- CocoaPods
- XcodeGen (for project generation)

### Android
- Android Studio
- Android SDK (API level 21+)
- Gradle

## Notes

- The iOS app uses a unified SDK project structure (UplinkSDKiOS) containing the Core SDK framework
- Framework embedding is handled automatically via build scripts
- The Android app uses local module dependencies
- Both implementations follow platform-specific best practices
- The apps serve as reference implementations for Core SDK integration

## Related Issues
- SDK development tickets
- Documentation tasks

## Labels
`playground-app`, `testing`, `ios`, `android`, `sdk-integration`, `documentation`

