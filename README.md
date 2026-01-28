# Swift Package Creation Guide

## Overview

DWSDK is distributed as a Swift Package, allowing easy integration into iOS projects using Swift Package Manager (SPM). This document explains how the Swift Package is structured and how to work with it.

## Package.swift Structure

The `Package.swift` file is located at `ios/Package.swift` and defines the Swift Package manifest:

```swift
// swift-tools-version:5.0
import PackageDescription

let package = Package(
    name: "DWSDK",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "DWSDK",
            targets: ["DWSDK"]
        ),
    ],
    dependencies: [
        // Add your dependencies here
    ],
    targets: [
        .target(
            name: "DWSDK",
            dependencies: [],
            path: "DWSDK",
            exclude: ["Info.plist"]
        ),
    ],
    swiftLanguageVersions: [.v5]
)
```

## Package Manifest Details

### Platform Requirements

```swift
platforms: [
    .iOS(.v13)
]
```

- **Minimum iOS Version**: 13.0
- This ensures compatibility with the SDK's UIKit-based implementation
- All iOS 13+ features are available (e.g., `UIWindowScene`)

### Product Definition

```swift
products: [
    .library(
        name: "DWSDK",
        targets: ["DWSDK"]
    ),
]
```

- **Product Type**: Dynamic library (default)
- **Product Name**: `DWSDK` (matches the framework name)
- **Target**: Links to the `DWSDK` target

### Target Configuration

```swift
.target(
    name: "DWSDK",
    dependencies: [],
    path: "DWSDK",
    exclude: ["Info.plist"]
)
```

**Key Properties:**

- **name**: `"DWSDK"` - Target identifier
- **dependencies**: Currently empty (no external dependencies)
- **path**: `"DWSDK"` - Relative path to source directory
- **exclude**: `["Info.plist"]` - Files to exclude from package

**Source Directory Structure:**

```
ios/
├── Package.swift
└── DWSDK/
    ├── Core/
    │   ├── DWSDKCore.swift
    │   └── DWSDKConfig.swift
    ├── Models/
    │   └── DWSDKModel.swift
    ├── UI/
    │   ├── *.swift (all UI files)
    │   └── Assets/ (images)
    └── DWSDK.h (umbrella header)
```

### Path Exclusions

The `Info.plist` file is excluded because:

- It's not needed for Swift Package distribution
- SPM handles Info.plist generation automatically
- Prevents conflicts with app's Info.plist

### Swift Language Version

```swift
swiftLanguageVersions: [.v5]
```

- Requires Swift 5.0 or later
- Ensures compatibility with modern Swift features
- Matches the `swift-tools-version` declaration

## Building the Package

### Command-Line Building

Build the package from the command line:

```bash
cd ios
swift build
```

**Output:**

- Builds the package for the current platform
- Creates artifacts in `.build/` directory
- Validates package structure and dependencies

### Xcode Integration

**Adding the Package to Xcode:**

1. Open your Xcode project
2. Go to **File** > **Add Packages...**
3. Enter the repository URL:
   ```
   https://github.com/reddrummer/dwallet.mobile-sdk.git
   ```
4. Select the version (e.g., `1.0.0`)
5. Click **Add Package**

**Local Development:**

For local development, you can add the package using a local path:

1. In Xcode, go to **File** > **Add Packages...**
2. Click **Add Local...**
3. Navigate to the `ios/` directory
4. Select the directory containing `Package.swift`

**Alternative: Using Package.swift in Your Project**

If your project uses SPM, add to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/reddrummer/dwallet.mobile-sdk.git", from: "1.0.0")
]
```

### Testing the Package

**Unit Tests:**

Create a test target in `Package.swift`:

```swift
.target(
    name: "DWSDK",
    dependencies: [],
    path: "DWSDK",
    exclude: ["Info.plist"]
),
.testTarget(
    name: "DWSDKTests",
    dependencies: ["DWSDK"],
    path: "Tests"
)
```

**Running Tests:**

```bash
swift test
```

Or in Xcode:

- Select the test scheme
- Press `⌘U` to run tests

## Resource Bundle Handling

### Current Implementation

The current `Package.swift` does not explicitly define resources. Resources (images, fonts) are handled differently:

**Images:**

- Currently loaded via bundle resolution in code
- Uses `Bundle(for: DWSDKCore.self)` to find resource bundle
- Falls back to main bundle if resource bundle not found

**Fonts:**

- Font files are in `DWSDK/UI/Theme/Fonts/`
- Loaded programmatically using `UIFont` registration
- Registered in `DWSDKTypography` initialization

### Future Resource Bundle Support

For explicit resource bundle support in SPM, you would add:

```swift
.target(
    name: "DWSDK",
    dependencies: [],
    path: "DWSDK",
    exclude: ["Info.plist"],
    resources: [
        .process("UI/Assets"),
        .process("UI/Theme/Fonts")
    ]
)
```

**Note:** This requires Swift 5.3+ and proper resource access patterns.

## Package Distribution

### Version Tagging

Swift Package Manager uses Git tags for versioning:

```bash
git tag 1.0.0
git push origin 1.0.0
```

**Version Format:**

- Semantic versioning (major.minor.patch)
- Examples: `1.0.0`, `1.1.0`, `2.0.0`

### Repository Requirements

For SPM to work, the repository must:

- Be publicly accessible (or use authentication)
- Have valid Git tags
- Contain `Package.swift` at the root or specified path
- Have source files in the correct structure

### Local vs Remote Packages

**Local Package (Development):**

```swift
.package(path: "../dwallet.mobile-sdk/ios")
```

**Remote Package (Production):**

```swift
.package(url: "https://github.com/reddrummer/dwallet.mobile-sdk.git", from: "1.0.0")
```

## Package Dependencies

### Current Dependencies

The package currently has no external dependencies:

```swift
dependencies: [
    // Add your dependencies here
]
```

### Adding Dependencies

To add a dependency:

```swift
dependencies: [
    .package(url: "https://github.com/example/library.git", from: "1.0.0")
],
targets: [
    .target(
        name: "DWSDK",
        dependencies: ["LibraryName"],  // Add here
        path: "DWSDK",
        exclude: ["Info.plist"]
    ),
]
```

### System Frameworks

System frameworks are automatically available:

- `Foundation`
- `UIKit`
- No need to declare in dependencies

## Package Resolution

### Resolving Dependencies

When you add the package, Xcode automatically:

1. Clones the repository
2. Resolves version constraints
3. Downloads dependencies
4. Builds the package

**Manual Resolution:**

```bash
swift package resolve
```

### Updating Packages

**In Xcode:**

- **File** > **Packages** > **Update to Latest Package Versions**

**Command Line:**

```bash
swift package update
```

## Package Structure Best Practices

### Directory Organization

```
ios/
├── Package.swift          # Package manifest
├── DWSDK/                 # Source directory
│   ├── Core/             # Core functionality
│   ├── Models/           # Data models
│   ├── UI/               # UI components
│   └── DWSDK.h           # Umbrella header
└── Tests/                # Test files (if added)
```

### File Organization

- Group related files in subdirectories
- Use clear naming conventions
- Keep public API in root or clearly marked
- Private implementation details in subdirectories

### Module Map

SPM automatically generates a module map from:

- The package name (`DWSDK`)
- Public headers (if any)
- Swift files with `public` or `@objc public` declarations

## Troubleshooting

### Common Issues

**1. Package Not Found**

- Verify repository URL is correct
- Check Git tags exist
- Ensure `Package.swift` is in the correct location

**2. Build Errors**

- Check Swift version compatibility
- Verify platform requirements
- Ensure all dependencies are resolved

**3. Resource Loading Issues**

- Verify bundle resolution code
- Check resource file paths
- Ensure resources are included in build

**4. Import Errors**

```swift
import DWSDK  // Should work if package is properly added
```

**Solutions:**

- Clean build folder (`⌘⇧K`)
- Reset package caches
- Re-add the package

### Debugging Package Issues

**Check Package Resolution:**

```bash
swift package show-dependencies
```

**Verify Package Structure:**

```bash
swift package describe
```

**Check Build Settings:**

- In Xcode: **Build Settings** > **Swift Compiler**
- Verify Swift version matches `swiftLanguageVersions`

## Comparison with CocoaPods

See [SPM_VS_COCOAPODS.md](./SPM_VS_COCOAPODS.md) for a detailed comparison between Swift Package Manager and CocoaPods, including when to use each.

## Next Steps

- Review [SDK_OVERVIEW.md](./SDK_OVERVIEW.md) for SDK usage
- Check [ARCHITECTURE.md](./ARCHITECTURE.md) for component details
- See [REACT_NATIVE_INTEGRATION.md](./REACT_NATIVE_INTEGRATION.md) for React Native setup
