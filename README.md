## Overview

The iOS Passpoint SDK provides comprehensive Passpoint (Hotspot 2.0) profile management for iOS applications. It supports iOS 13.0 and higher.

## Important iOS Limitations

- **Profile Removal**: iOS does not support programmatic removal of Passpoint profiles. Users must remove profiles manually from Settings > Wi-Fi > Passpoint profiles.
- **Profile Listing**: iOS API limitations mean the SDK uses a local cache to track installed profiles. The system API cannot list all Passpoint configurations.
- **Background Tasks**: Requires proper background task registration in `Info.plist` and `AppDelegate`.

## UplinkPasspointClient

The main client class for the Passpoint SDK. Provides access to profile management and lifecycle services.

### Initialization

#### `init`

Creates a new Passpoint SDK client instance.

```swift
public init(
    coreClient: UplinkCoreClient? = nil,
    apiBaseURL: String? = nil,
    accessToken: String? = nil,
    errorCallback: PasspointErrorCallback? = nil
)
```

**Parameters:**
- `coreClient`: Optional core SDK client for shared utilities
- `apiBaseURL`: Optional API base URL for backend integration
- `accessToken`: Optional access token for authenticated requests
- `errorCallback`: Optional callback for installation error notifications

**Example:**
```swift
let passpointClient = UplinkPasspointClient(
    coreClient: coreClient,
    errorCallback: MyErrorCallback()
)
```

### Profile Manager Access

#### `getProfileManager`

Gets the Passpoint profile manager.

```swift
public func getProfileManager() -> PasspointProfileManager
```

**Returns:** `PasspointProfileManager` instance

**Example:**
```swift
let profileManager = passpointClient.getProfileManager()
```

### Token Management

#### `setAccessToken`

Updates the access token for API requests.

```swift
public func setAccessToken(_ token: String?)
```

**Parameters:**
- `token`: New access token (or `nil` to clear)

### Profile Fetching

#### `fetchIOSPasspointProfile`

Fetches iOS Passpoint profile from external endpoint with automatic subscriber creation.

```swift
public func fetchIOSPasspointProfile() async throws -> IOSPasspointProfileResponse
```

**Returns:** `IOSPasspointProfileResponse` containing profile data

**Throws:** Error if the request fails

**Note:** Automatically creates a subscriber if one doesn't exist

**Example:**
```swift
do {
    let profileResponse = try await passpointClient.fetchIOSPasspointProfile()
    // Use profileResponse to install profile
} catch {
    print("Failed to fetch profile: \(error.localizedDescription)")
}
```

### Lifecycle Methods

#### `checkForProfileUpdates`

Manually triggers a poll check for profile updates. Useful for app launch scenarios or when push notifications fail.

```swift
public func checkForProfileUpdates() async throws
```

**Example:**
```swift
try await passpointClient.checkForProfileUpdates()
```

#### `shouldPollForUpdates`

Checks if polling should be triggered (e.g., on app launch).

```swift
public func shouldPollForUpdates() -> Bool
```

**Returns:** `true` if polling should be triggered, `false` otherwise

**Example:**
```swift
if passpointClient.shouldPollForUpdates() {
    try await passpointClient.checkForProfileUpdates()
}
```

#### `checkCertificateMonitoring`

Manually triggers certificate monitoring check. Useful for testing or manual refresh.

```swift
public func checkCertificateMonitoring() async throws
```

**Example:**
```swift
try await passpointClient.checkCertificateMonitoring()
```

#### `checkRenewal`

Manually triggers renewal check. Useful for testing or manual refresh.

```swift
public func checkRenewal() async throws
```

**Example:**
```swift
try await passpointClient.checkRenewal()
```

#### `stop`

Stops all lifecycle services.

```swift
public func stop()
```

**Example:**
```swift
passpointClient.stop()
```

### Notification Handler

#### `getNotificationHandler`

Gets the notification handler for push notification integration.

```swift
public func getNotificationHandler() -> PasspointNotificationHandler
```

**Returns:** `PasspointNotificationHandler` instance

**Example:**
```swift
let notificationHandler = passpointClient.getNotificationHandler()
// Use notificationHandler to handle push notifications
```

### Logging Utilities

#### `getLogFilePath`

Gets the log file path.

```swift
public func getLogFilePath() -> String?
```

**Returns:** Path to the Passpoint SDK log file, or `nil` if unavailable

#### `readLogFile`

Reads the log file contents.

```swift
public func readLogFile() -> String?
```

**Returns:** Full contents of the Passpoint SDK log file, or `nil` if unavailable

#### `getLogFileURL`

Gets the log file URL for sharing.

```swift
public func getLogFileURL() -> URL?
```

**Returns:** URL to the log file, or `nil` if unavailable

## PasspointProfileManager

Manages Passpoint profile operations including installation, listing, removal, and validation.

### Profile Installation

#### `installProfile`

Installs a Passpoint profile programmatically.

```swift
public func installProfile(_ profile: PasspointProfile) async throws -> ProfileInstallResult
```

**Parameters:**
- `profile`: The Passpoint profile to install

**Returns:** `ProfileInstallResult` containing installation status

**Throws:** Error if installation fails

**Example:**
```swift
let profile = PasspointProfile(
    id: "profile-123",
    friendlyName: "My Network",
    realm: "example.com",
    fqdn: "wifi.example.com",
    homeSpFqdn: "wifi.example.com",
    credential: PasspointCredential(
        username: "user@example.com",
        password: "password",
        realm: "example.com"
    )
)

do {
    let result = try await profileManager.installProfile(profile)
    if result.success {
        print("Profile installed: \(result.profileId ?? "unknown")")
    } else {
        print("Installation failed: \(result.errorMessage ?? "Unknown error")")
    }
} catch {
    print("Installation error: \(error.localizedDescription)")
}
```

#### `installProfileFromResponse`

Installs a Passpoint profile from API response with retry logic.

```swift
public func installProfileFromResponse(_ profileResponse: IOSPasspointProfileResponse) async throws -> ProfileInstallResult
```

**Parameters:**
- `profileResponse`: The iOS Passpoint profile response from API

**Returns:** `ProfileInstallResult` containing installation status

**Throws:** Error if installation fails

**Note:** Automatically checks for duplicate profiles before installation

**Example:**
```swift
do {
    let profileResponse = try await passpointClient.fetchIOSPasspointProfile()
    let result = try await profileManager.installProfileFromResponse(profileResponse)
    if result.success {
        print("Profile installed successfully")
    }
} catch {
    print("Failed to install profile: \(error.localizedDescription)")
}
```

### Profile Listing

#### `listProfiles`

Lists all installed Passpoint profiles.

```swift
public func listProfiles() async throws -> [PasspointProfile]
```

**Returns:** Array of installed Passpoint profiles

**Throws:** Error if listing fails

**Note:** iOS API limitations mean this uses a local cache. Only profiles installed via the SDK will be listed.

**Example:**
```swift
do {
    let profiles = try await profileManager.listProfiles()
    print("Found \(profiles.count) installed profiles")
    for profile in profiles {
        print("Profile: \(profile.friendlyName) (\(profile.fqdn))")
    }
} catch {
    print("Failed to list profiles: \(error.localizedDescription)")
}
```

### Profile Removal

#### `removeProfile`

Removes a Passpoint profile.

```swift
public func removeProfile(_ profileId: String) async throws
```

**Parameters:**
- `profileId`: The ID of the profile to remove (can be uniqueId or fqdn)

**Throws:** Error if removal fails

**Important:** iOS does not support programmatic removal of Passpoint profiles. This method will:
1. Clean up the cached profile data
2. Throw an error indicating the iOS limitation
3. Users must remove profiles manually from Settings > Wi-Fi > Passpoint profiles

**Example:**
```swift
do {
    try await profileManager.removeProfile("profile-123")
    print("Profile removed successfully")
} catch {
    print("Failed to remove profile: \(error.localizedDescription)")
    // On iOS, this will always fail with a limitation error
    // The cache is cleaned up, but the system profile remains
}
```

### Profile Validation

#### `verifyProfile`

Verifies if a Passpoint profile is valid.

```swift
public func verifyProfile(_ profile: PasspointProfile) async throws -> ProfileValidationResult
```

**Parameters:**
- `profile`: The profile to validate

**Returns:** `ProfileValidationResult` containing validation details

**Throws:** Error if validation fails

**Example:**
```swift
do {
    let result = try await profileManager.verifyProfile(profile)
    if result.isValid {
        print("Profile is valid")
        if result.isExpired {
            print("Profile certificate is expired")
        }
    } else {
        print("Profile validation failed: \(result.errorMessage ?? "Unknown error")")
    }
} catch {
    print("Validation error: \(error.localizedDescription)")
}
```

### Permission/Entitlement Management

#### `checkPermissions`

Checks if all required permissions/entitlements are present.

```swift
public func checkPermissions() -> PermissionStatus
```

**Returns:** `PermissionStatus` with details about permission state

**Example:**
```swift
let permissionStatus = profileManager.checkPermissions()
if permissionStatus.allPresent {
    // Proceed with Passpoint operations
} else {
    print("Missing entitlements: \(permissionStatus.missingPermissions.joined(separator: ", "))")
}
```

#### `validateEntitlements`

Validates entitlements for Passpoint operations.

```swift
public func validateEntitlements() -> EntitlementStatus
```

**Returns:** `EntitlementStatus` with details about entitlement state

**Example:**
```swift
let entitlementStatus = profileManager.validateEntitlements()
if entitlementStatus.allPresent {
    // Entitlements are valid
} else {
    print("Missing entitlements: \(entitlementStatus.missingEntitlements.joined(separator: ", "))")
}
```

#### `getMissingEntitlements`

Gets list of missing entitlements.

```swift
public func getMissingEntitlements() -> [String]
```

**Returns:** List of entitlement keys that are not present

**Example:**
```swift
let missing = profileManager.getMissingEntitlements()
if !missing.isEmpty {
    print("Missing entitlements: \(missing.joined(separator: ", "))")
}
```

### Profile Information

#### `getProfileInfo`

Gets profile information for display in app settings.

```swift
public func getProfileInfo(profileId: String) async throws -> PasspointProfileInfo?
```

**Parameters:**
- `profileId`: The ID of the profile

**Returns:** Profile information or `nil` if not found

**Example:**
```swift
do {
    if let profileInfo = try await profileManager.getProfileInfo("profile-123") {
        print("Profile installed: \(profileInfo.installationDate)")
        print("Profile expires: \(profileInfo.expirationDate?.description ?? "Never")")
        print("Profile status: \(profileInfo.status)")
    }
} catch {
    print("Failed to get profile info: \(error.localizedDescription)")
}
```

#### `getAllProfileInfo`

Gets profile information for all installed profiles.

```swift
public func getAllProfileInfo() async throws -> [PasspointProfileInfo]
```

**Returns:** Array of profile information

**Example:**
```swift
do {
    let allProfileInfo = try await profileManager.getAllProfileInfo()
    for info in allProfileInfo {
        print("Profile \(info.profileId): \(info.status)")
    }
} catch {
    print("Failed to get profile info: \(error.localizedDescription)")
}
```

### Logging Utilities

The profile manager also provides logging utilities (same as `UplinkPasspointClient`):

- `getLogFilePath() -> String?`
- `readLogFile() -> String?`
- `getLogFileURL() -> URL?`

## Data Models

### PasspointProfile

Represents a Passpoint profile.

```swift
public struct PasspointProfile: Codable {
    public let id: String?
    public let friendlyName: String
    public let realm: String
    public let fqdn: String
    public let homeSpFqdn: String
    public let credential: PasspointCredential
    public let policy: PasspointPolicy?
    public let metadata: [String: String]?
}
```

### PasspointCredential

Represents Passpoint credentials.

```swift
public struct PasspointCredential: Codable {
    public let username: String?
    public let password: String?
    public let certificate: String? // Base64 encoded
    public let privateKey: String? // Base64 encoded
    public let realm: String
}
```

### PasspointPolicy

Represents Passpoint policy settings.

```swift
public struct PasspointPolicy: Codable {
    public let minHomeDownlinkBandwidth: Int?
    public let minHomeUplinkBandwidth: Int?
    public let minRoamingDownlinkBandwidth: Int?
    public let minRoamingUplinkBandwidth: Int?
    public let excludedSsidList: [String]?
    public let maximumBssLoadValue: Int?
}
```

### ProfileInstallResult

Result of profile installation.

```swift
public struct ProfileInstallResult {
    public let success: Bool
    public let profileId: String?
    public let errorMessage: String?
    public let errorCode: Int?
}
```

### ProfileValidationResult

Result of profile validation.

```swift
public struct ProfileValidationResult {
    public let isValid: Bool
    public let isExpired: Bool
    public let expirationDate: Date?
    public let certificateValid: Bool
    public let errorMessage: String?
}
```

### IOSPasspointProfileResponse

Response model from the iOS Passpoint profile API endpoint.

```swift
public struct IOSPasspointProfileResponse: Codable {
    public let schemaVersion: Int
    public let profileId: String
    public let profileName: String
    public let homeSp: HomeSp
    public let eap: Eap
    public let credential: Credential
    public let trust: Trust
}
```

## Lifecycle Management

The SDK provides automatic lifecycle management through the `PasspointLifecycleOrchestrator`. This includes:

- **Profile Polling**: Checks for profile updates at least once daily (requires background task registration)
- **Certificate Monitoring**: Monitors certificate expiration dates
- **Renewal Scheduling**: Automatically renews profiles 90 days before expiration
- **Installation Retry**: Retries failed installations with exponential backoff

See [Lifecycle Management](../lifecycle-management.md) for detailed documentation.

## Error Handling

### PasspointErrorCallback

Protocol for receiving installation error notifications.

```swift
public protocol PasspointErrorCallback {
    func onInstallationError(error: Error, attempt: Int, willRetry: Bool)
}
```

**Parameters:**
- `error`: The error that occurred
- `attempt`: The current attempt number (1-based)
- `willRetry`: Whether the system will retry the installation

**Example:**
```swift
class MyErrorCallback: PasspointErrorCallback {
    func onInstallationError(error: Error, attempt: Int, willRetry: Bool) {
        print("Installation error (attempt \(attempt), willRetry: \(willRetry)): \(error.localizedDescription)")
        
        if !willRetry {
            // Final failure, notify user
            showErrorToUser("Failed to install profile: \(error.localizedDescription)")
        }
    }
}

let passpointClient = UplinkPasspointClient(
    coreClient: coreClient,
    errorCallback: MyErrorCallback()
)
```

### Error Types

The SDK may throw various errors:
- `PasspointPermissionError`: Missing entitlements
- `NSError`: System errors (domain: "PasspointProfileInstaller", "PasspointProfileRemover", etc.)
- Network errors from HTTP client

See [Error Handling](../error-handling.md) for detailed documentation.

## Background Task Registration

iOS requires background task registration for polling. Add to your `Info.plist`:

```xml
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.uplink.passpoint.profilePolling</string>
</array>
```

Register in `AppDelegate`:

```swift
func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    PasspointProfilePoller.registerBackgroundTask()
    return true
}
```

## Best Practices

1. **Initialize Once**: Create the Passpoint client once and reuse it
2. **Check Entitlements**: Always check entitlements before operations
3. **Handle Errors**: Implement `PasspointErrorCallback` for error notifications
4. **Use Lifecycle Management**: Let the SDK handle automatic polling and renewal
5. **Register Background Tasks**: Ensure background task registration for polling
6. **Handle iOS Limitations**: Inform users about manual profile removal requirement
7. **Use async/await**: The SDK uses modern Swift concurrency

## Related Documentation

- [Core SDK API Reference](core-sdk.md)
- [Entitlements Guide](entitlements.md)
- [Getting Started Guide](getting-started.md)
- [Lifecycle Management](../lifecycle-management.md)
- [Error Handling](../error-handling.md)
- [Code Examples](examples.md)
