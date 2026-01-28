# Swift Package Manager vs CocoaPods

## Overview

DWSDK supports both Swift Package Manager (SPM) and CocoaPods for dependency management. This document compares the two approaches and explains when to use each.

## Quick Comparison

| Feature                   | Swift Package Manager   | CocoaPods                 |
| ------------------------- | ----------------------- | ------------------------- |
| **Native Integration**    | ✅ Built into Xcode     | ❌ Requires separate tool |
| **Setup Complexity**      | ✅ Simple               | ⚠️ Requires Podfile       |
| **Dependency Resolution** | ✅ Fast                 | ⚠️ Can be slower          |
| **Subspecs**              | ❌ Not supported        | ✅ Supported              |
| **Resource Bundles**      | ⚠️ Limited (Swift 5.3+) | ✅ Full support           |
| **Workspace Required**    | ❌ No                   | ✅ Yes (.xcworkspace)     |
| **React Native**          | ⚠️ Limited support      | ✅ Full support           |
| **Maturity**              | ⚠️ Newer (2019+)        | ✅ Mature (2011+)         |

## Swift Package Manager (SPM)

### Advantages

**1. Native Xcode Integration**

- Built directly into Xcode (11+)
- No additional tools required
- Seamless workflow

**2. Faster Dependency Resolution**

- Parallel dependency resolution
- Incremental builds
- Better caching

**3. Simpler Setup**

- Just add package URL in Xcode
- No Podfile needed
- No `pod install` command

**4. Better for Open Source**

- Standard Swift ecosystem tool
- Works with GitHub, GitLab, etc.
- Version pinning via Git tags

**5. Modern Swift Features**

- Supports Swift 5.3+ resource bundles
- Better module system
- Swift-only (no Objective-C bridge needed)

### Disadvantages

**1. Limited Subspec Support**

- Cannot split package into subspecs
- All or nothing inclusion
- Larger binary size if only need part

**2. Resource Bundle Limitations**

- Resource bundle support added in Swift 5.3
- Requires explicit resource declarations
- More complex resource loading

**3. React Native Integration**

- React Native primarily uses CocoaPods
- SPM support is limited
- May require additional setup

**4. Workspace Management**

- Cannot use with existing `.xcworkspace`
- Conflicts with CocoaPods workspaces
- Must choose one or the other

### DWSDK SPM Implementation

**Package.swift Structure:**

```swift
let package = Package(
    name: "DWSDK",
    platforms: [.iOS(.v13)],
    products: [
        .library(name: "DWSDK", targets: ["DWSDK"])
    ],
    targets: [
        .target(
            name: "DWSDK",
            dependencies: [],
            path: "DWSDK",
            exclude: ["Info.plist"]
        )
    ]
)
```

**Characteristics:**

- Single target (`DWSDK`)
- All code included together
- Simple, flat structure
- Resources handled via bundle resolution

**Usage:**

```swift
// In Xcode: File > Add Packages...
// Or in Package.swift:
dependencies: [
    .package(url: "https://github.com/reddrummer/dwallet.mobile-sdk.git", from: "1.0.0")
]
```

## CocoaPods

### Advantages

**1. Mature Ecosystem**

- Established since 2011
- Large community
- Extensive documentation

**2. Subspec Support**

- Can split into modular components
- Selective inclusion
- Smaller binary size

**3. Better Resource Bundle Management**

- Native resource bundle support
- Explicit resource declarations
- Easier asset management

**4. React Native Integration**

- React Native uses CocoaPods by default
- Seamless integration
- Well-documented process

**5. Complex Dependency Trees**

- Better handling of transitive dependencies
- Dependency version resolution
- Conflict resolution

### Disadvantages

**1. Additional Tool Required**

- Must install CocoaPods gem
- Requires Ruby environment
- Separate from Xcode

**2. Workspace Requirement**

- Creates `.xcworkspace` file
- Must use workspace, not project
- Can conflict with SPM

**3. Slower Dependency Resolution**

- Sequential resolution
- Can be slow for large projects
- Less efficient caching

**4. Podfile Maintenance**

- Requires Podfile configuration
- Must run `pod install` after changes
- Lock file management

### DWSDK CocoaPods Implementation

**Podspec Structure:**

```ruby
Pod::Spec.new do |spec|
  spec.name = "DWSDK"
  spec.version = "1.0.0"

  # Default subspec includes all
  spec.default_subspecs = "Default"

  spec.subspec "Default" do |default|
    default.dependency "DWSDK/Core"
    default.dependency "DWSDK/Models"
    default.dependency "DWSDK/UI"
  end

  spec.subspec "Core" do |core|
    core.source_files = "DWSDK/Core/**/*.{h,m,swift}"
  end

  spec.subspec "Models" do |models|
    models.source_files = "DWSDK/Models/**/*.{h,m,swift}"
  end

  spec.subspec "UI" do |ui|
    ui.source_files = "DWSDK/UI/**/*.{h,m,swift}"
    ui.resource_bundles = {
      "DWSDK" => ["DWSDK/UI/Assets/**/*.png"]
    }
    ui.resources = "DWSDK/UI/Theme/Fonts/**/*.ttf"
  end
end
```

**Characteristics:**

- Modular subspecs (Core, Models, UI)
- Explicit resource bundle declarations
- Selective inclusion possible
- Better resource management

**Usage:**

```ruby
# Podfile
platform :ios, '13.0'
pod 'DWSDK', :path => '../ios'

# Or for specific subspecs:
pod 'DWSDK/Core'
pod 'DWSDK/UI'
```

## When to Use Each

### Use Swift Package Manager When:

✅ **New iOS Projects**

- Starting fresh with modern tooling
- No existing CocoaPods dependencies
- Want native Xcode integration

✅ **Swift-Only Projects**

- Pure Swift codebase
- No React Native integration
- Modern Swift features needed

✅ **Open Source Distribution**

- Publishing to GitHub/GitLab
- Want standard Swift ecosystem
- Simple dependency structure

✅ **Fast Development Cycle**

- Need quick iteration
- Want faster builds
- Minimal setup overhead

### Use CocoaPods When:

✅ **React Native Projects**

- React Native uses CocoaPods by default
- Need bridge module support
- Existing CocoaPods setup

✅ **Legacy Projects**

- Existing CocoaPods infrastructure
- Complex dependency trees
- Team familiar with CocoaPods

✅ **Modular Inclusion Needed**

- Only need specific components
- Want to minimize binary size
- Need subspec granularity

✅ **Resource Bundle Requirements**

- Complex asset management
- Need explicit resource control
- Multiple resource bundles

## DWSDK Implementation Differences

### Structure Comparison

**SPM Structure:**

```
DWSDK/
├── Core/
├── Models/
├── UI/
└── DWSDK.h
```

- Single target
- All code included
- Simple import: `import DWSDK`

**CocoaPods Structure:**

```
DWSDK/
├── Core/        (subspec)
├── Models/      (subspec)
└── UI/          (subspec)
```

- Multiple subspecs
- Selective inclusion
- Same import: `import DWSDK`

### Resource Handling

**SPM:**

- Resources loaded via `Bundle(for: DWSDKCore.self)`
- Fallback to main bundle
- Programmatic resource resolution

**CocoaPods:**

- Explicit resource bundle: `"DWSDK"`
- Fonts as resources
- Images in resource bundle
- Automatic bundle resolution

### Import Statement

Both use the same import:

```swift
import DWSDK
```

The underlying module structure is identical; only the distribution method differs.

## Migration Guide

### From CocoaPods to SPM

**Steps:**

1. Remove CocoaPods:

   ```bash
   pod deintegrate
   rm Podfile Podfile.lock
   ```

2. Remove workspace:
   - Close `.xcworkspace`
   - Use `.xcodeproj` instead

3. Add SPM package:
   - In Xcode: **File** > **Add Packages...**
   - Enter repository URL
   - Select version

4. Update imports:
   - No changes needed (same `import DWSDK`)

5. Update resource loading:
   - Verify bundle resolution code
   - Test asset loading

### From SPM to CocoaPods

**Steps:**

1. Remove SPM package:
   - In Xcode: **File** > **Packages** > **Remove Package**

2. Create Podfile:

   ```ruby
   platform :ios, '13.0'
   pod 'DWSDK', :path => '../ios'
   ```

3. Install pods:

   ```bash
   pod install
   ```

4. Open workspace:
   - Use `.xcworkspace` file
   - Not `.xcodeproj`

5. Update imports:
   - No changes needed (same `import DWSDK`)

## React Native Considerations

### CocoaPods (Recommended for React Native)

**Why CocoaPods:**

- React Native uses CocoaPods by default
- Bridge modules integrate seamlessly
- Podfile already exists
- Native module linking works automatically

**Setup:**

```ruby
# Podfile (in React Native app)
pod 'DWSDK', :path => '../../../ios'
```

**Bridge Module:**

- `DWSDKModule.swift` in app's iOS directory
- Automatically linked via CocoaPods
- No additional configuration needed

### SPM with React Native

**Challenges:**

- React Native doesn't natively support SPM
- Bridge modules require manual linking
- Additional setup complexity
- Not recommended for React Native projects

**If You Must Use SPM:**

1. Add package to Xcode project
2. Manually link bridge module files
3. Configure build settings
4. Test thoroughly

## Best Practices

### For New Projects

1. **Evaluate Requirements:**
   - React Native? → Use CocoaPods
   - Pure iOS? → Consider SPM
   - Legacy codebase? → Use CocoaPods

2. **Team Familiarity:**
   - Team knows CocoaPods? → Use CocoaPods
   - Team prefers SPM? → Use SPM

3. **Dependency Complexity:**
   - Simple dependencies? → SPM
   - Complex tree? → CocoaPods

### For Existing Projects

1. **Stick with Current System:**
   - Don't migrate unless necessary
   - Both work equally well
   - Migration has overhead

2. **If Migrating:**
   - Test thoroughly
   - Update documentation
   - Train team on new system

## Conclusion

Both Swift Package Manager and CocoaPods are valid choices for DWSDK:

- **SPM**: Better for new, Swift-only projects with native Xcode integration
- **CocoaPods**: Better for React Native, legacy projects, and modular inclusion

DWSDK supports both equally well, so choose based on your project's specific needs and constraints.

## Additional Resources

- [Swift Package Manager Documentation](https://swift.org/package-manager/)
- [CocoaPods Documentation](https://guides.cocoapods.org/)
- [DWSDK Swift Package Guide](./SWIFT_PACKAGE.md)
- [DWSDK React Native Integration](./REACT_NATIVE_INTEGRATION.md)
