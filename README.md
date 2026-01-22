## Prerequisites

- Xcode 14.0 or higher
- iOS 13.0 or higher
- CocoaPods (`sudo gem install cocoapods`) or Swift Package Manager
- Apple Developer account with entitlements configured

## SDK Integration

### Step 1: Add Dependencies

#### Using CocoaPods

Add to your `Podfile`:

```ruby
pod 'UplinkCoreSDK', :path => '../UplinkSDKiOS'
pod 'UplinkPasspointProfileSDK', :path => '../UplinkSDKiOS'
```

Then run:

```bash
pod install
```

#### Using Swift Package Manager

Add the SDK packages to your project dependencies.

### Step 2: Configure Entitlements

1. Enable capabilities in Apple Developer Portal (see [Entitlements Guide](entitlements.md))
2. Add entitlements file to your project
3. Configure in Xcode Signing & Capabilities

### Step 3: Configure Background Tasks

Add to `Info.plist`:

```xml
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.uplink.passpoint.profilePolling</string>
</array>
```

### Step 4: Initialize SDK

Initialize the SDK in your `AppDelegate`:

```swift
import UIKit
import UplinkCoreSDK
import UplinkPasspointProfileSDK

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var coreClient: UplinkCoreClient?
    var passpointClient: UplinkPasspointClient?
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Register background task
        PasspointProfilePoller.registerBackgroundTask()
        
        // Initialize SDK
        Task {
            await initializeSDK()
        }
        
        return true
    }
    
    private func initializeSDK() async {
        let deviceInfo = DeviceInfo(
            deviceId: UIDevice.current.identifierForVendor?.uuidString ?? "unknown",
            deviceModel: UIDevice.current.model,
            osVersion: UIDevice.current.systemVersion,
            osType: "iOS"
        )
        
        coreClient = await UplinkCoreClient.create(
            baseURL: "https://api-gateway.develop.uplink.xyz/v2",
            appId: "your-app-id",
            appSecret: "your-app-secret",
            deviceInfo: deviceInfo
        )
        
        passpointClient = UplinkPasspointClient(
            coreClient: coreClient,
            errorCallback: MyErrorCallback()
        )
    }
}

class MyErrorCallback: PasspointErrorCallback {
    func onInstallationError(error: Error, attempt: Int, willRetry: Bool) {
        print("Installation error (attempt \(attempt), willRetry: \(willRetry)): \(error.localizedDescription)")
    }
}
```

## Basic Usage

### Install a Profile

```swift
Task {
    // Check entitlements first
    let profileManager = passpointClient.getProfileManager()
    let permissionStatus = profileManager.checkPermissions()
    guard permissionStatus.allPresent else {
        print("Missing entitlements: \(permissionStatus.missingPermissions.joined(separator: ", "))")
        return
    }
    
    do {
        // Fetch profile from API
        let profileResponse = try await passpointClient.fetchIOSPasspointProfile()
        
        // Install profile
        let result = try await profileManager.installProfileFromResponse(profileResponse)
        if result.success {
            print("Profile installed: \(result.profileId ?? "unknown")")
        } else {
            print("Installation failed: \(result.errorMessage ?? "Unknown error")")
        }
    } catch {
        print("Error: \(error.localizedDescription)")
    }
}
```

### List Profiles

```swift
Task {
    do {
        let profiles = try await profileManager.listProfiles()
        print("Found \(profiles.count) installed profiles")
        for profile in profiles {
            print("Profile: \(profile.friendlyName) (\(profile.fqdn))")
        }
    } catch {
        print("Failed to list profiles: \(error.localizedDescription)")
    }
}
```

### Remove a Profile

```swift
Task {
    do {
        try await profileManager.removeProfile("profile-id")
        print("Profile removed successfully")
    } catch {
        // On iOS, this will always fail with a limitation error
        // The cache is cleaned up, but users must remove manually from Settings
        print("Note: iOS requires manual removal from Settings > Wi-Fi > Passpoint profiles")
    }
}
```

### Validate a Profile

```swift
Task {
    do {
        let result = try await profileManager.verifyProfile(profile)
        if result.isValid {
            print("Profile is valid")
        } else {
            print("Profile validation failed: \(result.errorMessage ?? "Unknown error")")
        }
    } catch {
        print("Validation error: \(error.localizedDescription)")
    }
}
```

## Complete Integration Example

```swift
import UIKit
import UplinkCoreSDK
import UplinkPasspointProfileSDK

class ViewController: UIViewController {
    var coreClient: UplinkCoreClient?
    var passpointClient: UplinkPasspointClient?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Task {
            await initializeSDK()
        }
    }
    
    private func initializeSDK() async {
        let deviceInfo = DeviceInfo(
            deviceId: UIDevice.current.identifierForVendor?.uuidString ?? "unknown",
            deviceModel: UIDevice.current.model,
            osVersion: UIDevice.current.systemVersion,
            osType: "iOS"
        )
        
        coreClient = await UplinkCoreClient.create(
            baseURL: "https://api-gateway.develop.uplink.xyz/v2",
            appId: "your-app-id",
            appSecret: "your-app-secret",
            deviceInfo: deviceInfo
        )
        
        passpointClient = UplinkPasspointClient(
            coreClient: coreClient,
            errorCallback: MyErrorCallback()
        )
    }
    
    @IBAction func installProfileTapped(_ sender: UIButton) {
        Task {
            await installProfile()
        }
    }
    
    private func installProfile() async {
        guard let passpointClient = passpointClient else { return }
        
        let profileManager = passpointClient.getProfileManager()
        
        // Check entitlements
        let permissionStatus = profileManager.checkPermissions()
        guard permissionStatus.allPresent else {
            showAlert(title: "Missing Entitlements", message: permissionStatus.userFriendlyMessage)
            return
        }
        
        do {
            // Fetch and install profile
            let profileResponse = try await passpointClient.fetchIOSPasspointProfile()
            let result = try await profileManager.installProfileFromResponse(profileResponse)
            
            if result.success {
                showAlert(title: "Success", message: "Profile installed successfully")
            } else {
                showAlert(title: "Error", message: result.errorMessage ?? "Unknown error")
            }
        } catch {
            showAlert(title: "Error", message: error.localizedDescription)
        }
    }
    
    private func showAlert(title: String, message: String) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default))
            self.present(alert, animated: true)
        }
    }
}

class MyErrorCallback: PasspointErrorCallback {
    func onInstallationError(error: Error, attempt: Int, willRetry: Bool) {
        print("Installation error (attempt \(attempt), willRetry: \(willRetry)): \(error.localizedDescription)")
    }
}
```

## Next Steps

- Read the [Passpoint SDK API Reference](passpoint-sdk.md) for detailed API documentation
- Check [Entitlements Guide](entitlements.md) for entitlement configuration
- See [Code Examples](examples.md) for more examples
- Review [Best Practices](../best-practices.md) for recommended patterns
