## Overview

The iOS Passpoint SDK requires specific entitlements to function correctly. Entitlements are configured in the Apple Developer Portal and in your Xcode project. This guide covers all required entitlements and configuration steps.

## Required Entitlements

The SDK requires the following entitlements:

- `com.apple.developer.networking.HotspotConfiguration`: Required for installing and managing Passpoint profiles
- `com.apple.developer.networking.wifi-info`: Required for Wi-Fi information access

## Apple Developer Portal Setup

### Step 1: Enable Capabilities

1. Log in to [Apple Developer Portal](https://developer.apple.com/account)
2. Navigate to **Certificates, Identifiers & Profiles**
3. Select **Identifiers** > **App IDs**
4. Select your App ID or create a new one
5. Enable the following capabilities:
   - **Hotspot Configuration**
   - **Wi-Fi Information**
6. Save the changes

### Step 2: Update Provisioning Profiles

After enabling capabilities, update your provisioning profiles:
1. Navigate to **Profiles**
2. Select your provisioning profile
3. Click **Edit**
4. Ensure the updated App ID is selected
5. Download and install the updated provisioning profile

## Xcode Configuration

### Step 1: Add Entitlements File

1. In Xcode, select your project
2. Go to **Signing & Capabilities** tab
3. Click **+ Capability**
4. Add **Hotspot Configuration**
5. Add **Wi-Fi Information**

Alternatively, manually add to your entitlements file:

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

### Step 2: Configure Signing

1. In **Signing & Capabilities** tab
2. Select your **Team**
3. Ensure **Automatically manage signing** is enabled, or manually select the provisioning profile
4. Verify that both entitlements appear in the capabilities list

## Background Task Registration

For profile polling, register the background task identifier in `Info.plist`:

```xml
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.uplink.passpoint.profilePolling</string>
</array>
```

### AppDelegate Registration

Register the background task in your `AppDelegate`:

```swift
import UIKit
import BackgroundTasks

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Register background task for Passpoint polling
        PasspointProfilePoller.registerBackgroundTask()
        
        return true
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        // Schedule background task
        scheduleBackgroundTask()
    }
    
    private func scheduleBackgroundTask() {
        let request = BGProcessingTaskRequest(identifier: "com.uplink.passpoint.profilePolling")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 1 * 60 * 60) // 1 hour from now
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule background task: \(error)")
        }
    }
}
```

## Entitlement Validation

The SDK provides methods to check entitlements:

```swift
let profileManager = passpointClient.getProfileManager()

// Check permissions/entitlements
let permissionStatus = profileManager.checkPermissions()
if permissionStatus.allPresent {
    // Entitlements are valid, proceed
} else {
    // Missing entitlements
    print("Missing entitlements: \(permissionStatus.missingPermissions.joined(separator: ", "))")
    print("Message: \(permissionStatus.userFriendlyMessage)")
}

// Validate entitlements specifically
let entitlementStatus = profileManager.validateEntitlements()
if entitlementStatus.allPresent {
    // All entitlements present
} else {
    // Missing entitlements
    let missing = profileManager.getMissingEntitlements()
    print("Missing: \(missing.joined(separator: ", "))")
}
```

## Common Issues

### Entitlement Not Found

**Issue**: SDK reports missing entitlements even after configuration

**Solutions**:
1. Verify entitlements are enabled in Apple Developer Portal
2. Ensure provisioning profile includes the entitlements
3. Clean and rebuild the project
4. Verify entitlements file is included in the target
5. Check that the correct Team is selected in Xcode

### Background Task Not Registered

**Issue**: Background task registration fails

**Solutions**:
1. Verify `BGTaskSchedulerPermittedIdentifiers` is in `Info.plist`
2. Ensure background task identifier matches exactly
3. Register in `AppDelegate.application(_:didFinishLaunchingWithOptions:)`
4. Check that background modes are enabled in capabilities

### Automatic Signing Issues

**Issue**: Xcode shows signing errors

**Solutions**:
1. Ensure your Team ID is set in project settings
2. Verify App ID has required capabilities enabled
3. Let Xcode automatically manage signing, or manually select provisioning profile
4. Clean derived data and rebuild

## Verification Checklist

- [ ] Entitlements enabled in Apple Developer Portal
- [ ] Provisioning profile updated and installed
- [ ] Entitlements file configured in Xcode
- [ ] Capabilities added in Signing & Capabilities
- [ ] Background task identifier in Info.plist
- [ ] Background task registered in AppDelegate
- [ ] SDK entitlement validation passes

## Best Practices

1. **Configure Early**: Set up entitlements before integrating the SDK
2. **Verify Setup**: Use SDK's entitlement validation methods
3. **Handle Missing Entitlements**: Provide clear error messages to users
4. **Test on Device**: Entitlements don't work in simulator for some features
5. **Document Requirements**: Inform users about required capabilities

## Related Documentation

- [Passpoint SDK API Reference](passpoint-sdk.md)
- [Getting Started Guide](getting-started.md)
- [Code Examples](examples.md)
- [Troubleshooting](../troubleshooting.md)
