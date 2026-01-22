# Complete Integration Example

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
        PasspointProfilePoller.registerBackgroundTask()
        
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

class ViewController: UIViewController {
    var passpointClient: UplinkPasspointClient?
    
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
            let profileResponse = try await passpointClient.fetchIOSPasspointProfile()
            let result = try await profileManager.installProfileFromResponse(profileResponse)
            
            if result.success {
                showAlert(title: "Success", message: "Profile installed")
            } else {
                showAlert(title: "Error", message: result.errorMessage ?? "Unknown error")
            }
        } catch {
            showAlert(title: "Error", message: error.localizedDescription)
        }
    }
}

class MyErrorCallback: PasspointErrorCallback {
    func onInstallationError(error: Error, attempt: Int, willRetry: Bool) {
        print("Installation error (attempt \(attempt), willRetry: \(willRetry)): \(error.localizedDescription)")
    }
}
```

## Profile Installation Example

```swift
func installProfileExample() async {
    let profileManager = passpointClient.getProfileManager()
    
    // Check entitlements
    guard profileManager.checkPermissions().allPresent else {
        return
    }
    
    do {
        let profileResponse = try await passpointClient.fetchIOSPasspointProfile()
        let result = try await profileManager.installProfileFromResponse(profileResponse)
        
        if result.success {
            print("Profile installed: \(result.profileId ?? "unknown")")
        }
    } catch {
        print("Error: \(error.localizedDescription)")
    }
}
```

## Profile Listing Example

```swift
func listProfilesExample() async {
    do {
        let profiles = try await profileManager.listProfiles()
        for profile in profiles {
            print("Profile: \(profile.friendlyName) (\(profile.fqdn))")
        }
    } catch {
        print("Failed: \(error.localizedDescription)")
    }
}
```

## Profile Removal Example

```swift
func removeProfileExample(profileId: String) async {
    do {
        try await profileManager.removeProfile(profileId)
        print("Profile removed")
    } catch {
        // iOS limitation: Always fails with error
        // Cache is cleaned, but user must remove manually
        print("Note: Manual removal required from Settings")
    }
}
```

## Error Handling Example

```swift
func errorHandlingExample() async {
    do {
        let profileResponse = try await passpointClient.fetchIOSPasspointProfile()
        let result = try await profileManager.installProfileFromResponse(profileResponse)
        
        if !result.success {
            print("Installation failed: \(result.errorMessage ?? "Unknown error")")
        }
    } catch let error as PasspointPermissionError {
        // Permission error
        showEntitlementError(error)
    } catch {
        // Other errors
        print("Error: \(error.localizedDescription)")
    }
}
```

## Lifecycle Management Example

```swift
// Manual polling
try await passpointClient.checkForProfileUpdates()

// Certificate monitoring
try await passpointClient.checkCertificateMonitoring()

// Renewal check
try await passpointClient.checkRenewal()
```

## Entitlement Checking Example

```swift
func checkEntitlements() {
    let profileManager = passpointClient.getProfileManager()
    
    let permissionStatus = profileManager.checkPermissions()
    if !permissionStatus.allPresent {
        let missing = profileManager.getMissingEntitlements()
        print("Missing entitlements: \(missing.joined(separator: ", "))")
        print("Message: \(permissionStatus.userFriendlyMessage)")
    }
}
```
