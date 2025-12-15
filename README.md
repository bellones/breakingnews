# iOS Permissions and Entitlements Guide

## Overview

The Uplink Passpoint Profile SDK requires specific entitlements to function correctly on iOS devices. Unlike Android, iOS uses entitlements (declared in the app's entitlements file) rather than runtime permissions for Passpoint functionality. This document outlines all required entitlements and how to configure them.

## Required Entitlements

### 1. `com.apple.developer.networking.HotspotConfiguration`
- **Purpose**: Allows the app to configure Wi-Fi hotspot and Passpoint profiles
- **Required for**: Installing, modifying, and removing Passpoint profiles
- **Declaration**: Must be added to your app's `.entitlements` file
- **Apple Developer Portal**: Must be enabled for your App ID

### 2. `com.apple.developer.networking.wifi-info`
- **Purpose**: Allows the app to access Wi-Fi network information
- **Required for**: Listing installed Passpoint profiles, reading Wi-Fi state
- **Declaration**: Must be added to your app's `.entitlements` file
- **Apple Developer Portal**: Must be enabled for your App ID

## Entitlements File Configuration

### Creating/Updating Entitlements File

1. **In Xcode**:
   - Select your app target
   - Go to **Signing & Capabilities** tab
   - Click **+ Capability**
   - Add **Hotspot Configuration** capability
   - Add **Wi-Fi Information** capability (if available)

2. **Manual Configuration**:
   Create or update your `.entitlements` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.developer.networking.HotspotConfiguration</key>
    <true/>
    <key>com.apple.developer.networking.wifi-info</key>
    <true/>
</dict>
</plist>
```

### Entitlements File Location

- **Xcode Project**: Usually named `YourApp.entitlements` in your project root
- **Build Settings**: Reference the entitlements file in **Code Signing Entitlements** build setting

## Apple Developer Portal Configuration

### Step 1: Enable Capabilities for App ID

1. Log in to [Apple Developer Portal](https://developer.apple.com/account)
2. Navigate to **Certificates, Identifiers & Profiles**
3. Select **Identifiers** > **App IDs**
4. Select your App ID (or create a new one)
5. Enable the following capabilities:
   - **Hotspot Configuration** (`com.apple.developer.networking.HotspotConfiguration`)
   - **Wi-Fi Information** (`com.apple.developer.networking.wifi-info`)
6. Click **Save**

### Step 2: Update Provisioning Profiles

After enabling capabilities:
1. Navigate to **Profiles** in Developer Portal
2. Select your provisioning profile (Development/Distribution)
3. Click **Edit**
4. Ensure your App ID with enabled capabilities is selected
5. Click **Generate** to create a new profile
6. Download and install the new profile in Xcode

### Step 3: Verify in Xcode

1. Open your project in Xcode
2. Select your app target
3. Go to **Signing & Capabilities** tab
4. Verify both capabilities are listed:
   - ✅ Hotspot Configuration
   - ✅ Wi-Fi Information

## Entitlement Validation

The SDK includes automatic entitlement validation. Before performing any Passpoint operations, the SDK checks if required entitlements are present.

### Using the SDK's Permission Validator

```swift
import UplinkPasspointProfileSDK

// Check entitlements
let validator = PasspointPermissionValidator()
let status = validator.validatePermissions()

if !status.allPresent {
    // Show user-friendly error message
    showError(status.userFriendlyMessage)
} else {
    // Proceed with Passpoint operations
}
```

### Manual Validation

You can also check entitlements manually:

```swift
// Check if entitlements file exists and contains required keys
if let entitlements = Bundle.main.object(forInfoDictionaryKey: "Entitlements") as? [String: Any] {
    let hasHotspotConfig = entitlements["com.apple.developer.networking.HotspotConfiguration"] as? Bool ?? false
    let hasWifiInfo = entitlements["com.apple.developer.networking.wifi-info"] as? Bool ?? false
    
    if !hasHotspotConfig || !hasWifiInfo {
        // Show error
    }
}
```

## Permission Requirements by Operation

### Install Profile
- `com.apple.developer.networking.HotspotConfiguration` (required)
- `com.apple.developer.networking.wifi-info` (required)

### List Profiles
- `com.apple.developer.networking.wifi-info` (required)
- `com.apple.developer.networking.HotspotConfiguration` (recommended)

### Remove Profile
- `com.apple.developer.networking.HotspotConfiguration` (required)
- `com.apple.developer.networking.wifi-info` (required)

## iOS Version Considerations

### iOS 11.0+
- Passpoint profile installation is supported
- `NEHotspotConfiguration` API available
- Requires entitlements

### iOS 13.0+
- Enhanced Passpoint support with `NEHotspotHS20Settings`
- Better roaming support
- All entitlements still required

### iOS 14.0+
- Additional privacy protections
- Entitlements still required for Passpoint operations

## Error Messages

The SDK provides user-friendly error messages when entitlements are missing:

- **Missing Hotspot Configuration**: "Hotspot Configuration entitlement is required. Please enable it in Xcode > Signing & Capabilities."
- **Missing Wi-Fi Info**: "Wi-Fi Info entitlement is missing. Please add it in Apple Developer Portal."
- **Multiple Missing Entitlements**: "Required entitlements are not configured. Please check your app's capabilities."

## Troubleshooting

### Entitlement Not Found Error

**Problem**: App crashes or returns error about missing entitlements.

**Solution**:
1. Verify entitlements file exists and is included in target
2. Check that entitlements are enabled in Apple Developer Portal for your App ID
3. Ensure provisioning profile includes the entitlements
4. Clean build folder and rebuild (Product > Clean Build Folder)

### Capability Not Available in Xcode

**Problem**: Can't find "Hotspot Configuration" or "Wi-Fi Information" capability in Xcode.

**Solution**:
1. Ensure you're using Xcode 11.0 or later
2. Check that your Apple Developer account has the capability enabled
3. Manually add to entitlements file (see manual configuration above)
4. Verify your Team ID is set correctly in project settings

### Provisioning Profile Issues

**Problem**: Build fails with "Provisioning profile doesn't include the required entitlement".

**Solution**:
1. Enable capabilities in Apple Developer Portal for your App ID
2. Regenerate provisioning profile in Developer Portal
3. Download and install new profile in Xcode
4. Select the correct provisioning profile in Xcode (Signing & Capabilities)
5. Clean and rebuild project

### Entitlements Not Applied

**Problem**: Entitlements file exists but capabilities don't work.

**Solution**:
1. Verify entitlements file is referenced in **Code Signing Entitlements** build setting
2. Check that file is included in target membership
3. Ensure entitlements file is in the correct location (usually project root)
4. Verify file format is correct (XML plist format)

### Simulator Limitations

**Problem**: Passpoint operations don't work in iOS Simulator.

**Solution**:
- Passpoint profile installation requires a physical device
- NetworkExtension APIs have limited support in simulator
- Test on a physical iOS device for full functionality

## Best Practices

1. **Configure Early**: Set up entitlements during initial project setup
2. **Verify in Portal**: Always verify capabilities are enabled in Apple Developer Portal
3. **Test on Device**: Test Passpoint functionality on physical devices, not simulator
4. **Check Before Operations**: Use SDK's validation before calling Passpoint methods
5. **Handle Errors Gracefully**: Show helpful error messages if entitlements are missing

## Example: Complete Integration

```swift
import UIKit
import UplinkCoreSDK
import UplinkPasspointProfileSDK

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Validate entitlements on app launch
        validateEntitlements()
        
        return true
    }
    
    private func validateEntitlements() {
        let validator = PasspointPermissionValidator()
        let status = validator.validatePermissions()
        
        if !status.allPresent {
            // Log error (in production, you might want to show an alert)
            print("⚠️ Missing entitlements: \(status.missingEntitlements)")
            print("Message: \(status.userFriendlyMessage)")
        }
    }
}

class PasspointViewController: UIViewController {
    private let passpointClient: UplinkPasspointClient
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Initialize SDK
        let coreClient = UplinkCoreClient.fromService()
        passpointClient = UplinkPasspointClient(coreClient: coreClient)
    }
    
    func installProfile() async {
        // SDK will validate entitlements automatically, but you can check first
        let validator = PasspointPermissionValidator()
        let status = validator.validatePermissions()
        
        guard status.allPresent else {
            showError(status.userFriendlyMessage)
            return
        }
        
        // Proceed with installation
        do {
            let result = try await passpointClient.getProfileManager().installProfile(profile)
            // Handle result
        } catch {
            // Handle error
        }
    }
}
```

## Xcode Project Configuration

### Using XcodeGen

If using `project.yml` with XcodeGen, add entitlements:

```yaml
targets:
  YourApp:
    entitlements:
      path: YourApp.entitlements
    settings:
      CODE_SIGN_ENTITLEMENTS: YourApp.entitlements
```

### Manual Xcode Setup

1. Select your app target
2. Go to **Build Settings**
3. Search for "Code Signing Entitlements"
4. Set value to: `YourApp.entitlements` (or path to your entitlements file)
5. Go to **Signing & Capabilities**
6. Add capabilities as described above

## Additional Resources

- [Apple Entitlements Documentation](https://developer.apple.com/documentation/bundleresources/entitlements)
- [NetworkExtension Framework](https://developer.apple.com/documentation/networkextension)
- [NEHotspotConfiguration](https://developer.apple.com/documentation/networkextension/nehotspotconfiguration)
- [App Capabilities](https://developer.apple.com/documentation/xcode/adding-capabilities-to-your-app)

