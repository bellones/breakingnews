## Overview

The Android Passpoint SDK provides comprehensive Passpoint (Hotspot 2.0) profile management for Android applications. It supports profile installation, listing, removal, validation, and automatic lifecycle management.

## UplinkPasspointClient

The main client class for the Passpoint SDK. Provides access to profile management and lifecycle services.

### Initialization

#### `create`

Creates a new Passpoint SDK client instance.

```kotlin
fun create(
    context: Context? = null,
    coreClient: UplinkCoreClient? = null,
    apiBaseUrl: String? = null,
    accessToken: String? = null,
    errorCallback: PasspointErrorCallback? = null
): UplinkPasspointClient
```

**Parameters:**
- `context`: Application context (required for listing/installing profiles)
- `coreClient`: Optional core SDK client for shared utilities
- `apiBaseUrl`: Optional API base URL for backend integration
- `accessToken`: Optional access token for authenticated requests
- `errorCallback`: Optional callback for installation error notifications

**Returns:** `UplinkPasspointClient` instance

**Example:**
```kotlin
val passpointClient = UplinkPasspointClient.create(
    context = applicationContext,
    coreClient = coreClient,
    errorCallback = object : PasspointErrorCallback {
        override fun onInstallationError(error: Throwable, attempt: Int, willRetry: Boolean) {
            Log.e("App", "Installation error (attempt $attempt, willRetry: $willRetry): ${error.message}")
        }
    }
)
```

### Profile Manager Access

#### `profileManager`

Gets the Passpoint profile manager.

```kotlin
fun profileManager(): PasspointProfileManager
```

**Returns:** `PasspointProfileManager` instance

**Example:**
```kotlin
val profileManager = passpointClient.profileManager()
```

### Token Management

#### `setAccessToken`

Updates the access token for API requests.

```kotlin
fun setAccessToken(token: String?)
```

**Parameters:**
- `token`: New access token (or `null` to clear)

### Lifecycle Methods

#### `checkForProfileUpdates`

Manually triggers a poll check for profile updates. Useful for app launch scenarios or when push notifications fail.

```kotlin
suspend fun checkForProfileUpdates()
```

**Example:**
```kotlin
passpointClient.checkForProfileUpdates()
```

#### `shouldPollForUpdates`

Checks if polling should be triggered (e.g., on app launch).

```kotlin
fun shouldPollForUpdates(): Boolean
```

**Returns:** `true` if polling should be triggered, `false` otherwise

**Example:**
```kotlin
if (passpointClient.shouldPollForUpdates()) {
    passpointClient.checkForProfileUpdates()
}
```

#### `checkCertificateMonitoring`

Manually triggers certificate monitoring check. Useful for testing or manual refresh.

```kotlin
suspend fun checkCertificateMonitoring()
```

**Example:**
```kotlin
passpointClient.checkCertificateMonitoring()
```

#### `checkRenewal`

Manually triggers renewal check. Useful for testing or manual refresh.

```kotlin
suspend fun checkRenewal()
```

**Example:**
```kotlin
passpointClient.checkRenewal()
```

#### `stop`

Stops all lifecycle services.

```kotlin
fun stop()
```

**Example:**
```kotlin
passpointClient.stop()
```

### Notification Handler

#### `getNotificationHandler`

Gets the notification handler for push notification integration.

```kotlin
fun getNotificationHandler(): PasspointNotificationHandler?
```

**Returns:** `PasspointNotificationHandler` instance or `null`

**Example:**
```kotlin
val notificationHandler = passpointClient.getNotificationHandler()
// Use notificationHandler to handle push notifications
```

## PasspointProfileManager

Manages Passpoint profile operations including installation, listing, removal, and validation.

### Profile Installation

#### `installProfile`

Installs a Passpoint profile programmatically.

```kotlin
suspend fun installProfile(profile: PasspointProfile): Result<ProfileInstallResult>
```

**Parameters:**
- `profile`: The Passpoint profile to install

**Returns:** `Result<ProfileInstallResult>` containing installation status

**Example:**
```kotlin
val profile = PasspointProfile(
    id = "profile-123",
    friendlyName = "My Network",
    realm = "example.com",
    fqdn = "wifi.example.com",
    homeSpFqdn = "wifi.example.com",
    credential = PasspointCredential(
        username = "user@example.com",
        password = "password",
        realm = "example.com"
    )
)

val result = profileManager.installProfile(profile)
result.onSuccess { installResult ->
    if (installResult.success) {
        Log.i("App", "Profile installed: ${installResult.profileId}")
    } else {
        Log.e("App", "Installation failed: ${installResult.errorMessage}")
    }
}.onFailure { error ->
    Log.e("App", "Installation error: ${error.message}")
}
```

#### `installProfileFromResponse`

Installs a Passpoint profile from API response with retry logic.

```kotlin
suspend fun installProfileFromResponse(
    profileResponse: AndroidPasspointProfileResponse
): Result<ProfileInstallResult>
```

**Parameters:**
- `profileResponse`: The Android Passpoint profile response from API

**Returns:** `Result<ProfileInstallResult>` containing installation status

**Example:**
```kotlin
val profileResponse = coreClient.getAndroidPasspointProfile()
val result = profileManager.installProfileFromResponse(profileResponse)
result.onSuccess { installResult ->
    if (installResult.success) {
        Log.i("App", "Profile installed: ${installResult.profileId}")
    }
}
```

### Profile Listing

#### `listProfiles`

Lists all installed Passpoint profiles.

```kotlin
suspend fun listProfiles(): Result<List<PasspointProfile>>
```

**Returns:** `Result<List<PasspointProfile>>` containing list of installed profiles

**Example:**
```kotlin
val result = profileManager.listProfiles()
result.onSuccess { profiles ->
    Log.i("App", "Found ${profiles.size} installed profiles")
    profiles.forEach { profile ->
        Log.d("App", "Profile: ${profile.friendlyName} (${profile.fqdn})")
    }
}.onFailure { error ->
    Log.e("App", "Failed to list profiles: ${error.message}")
}
```

### Profile Removal

#### `removeProfile`

Removes a Passpoint profile.

```kotlin
suspend fun removeProfile(profileId: String): Result<Unit>
```

**Parameters:**
- `profileId`: The ID of the profile to remove (can be uniqueId or fqdn)

**Returns:** `Result<Unit>` indicating success or failure

**Example:**
```kotlin
val result = profileManager.removeProfile("profile-123")
result.onSuccess {
    Log.i("App", "Profile removed successfully")
}.onFailure { error ->
    Log.e("App", "Failed to remove profile: ${error.message}")
}
```

### Profile Validation

#### `verifyProfile`

Verifies if a Passpoint profile is valid.

```kotlin
suspend fun verifyProfile(profile: PasspointProfile): Result<ProfileValidationResult>
```

**Parameters:**
- `profile`: The profile to validate

**Returns:** `Result<ProfileValidationResult>` containing validation details

**Example:**
```kotlin
val result = profileManager.verifyProfile(profile)
result.onSuccess { validationResult ->
    if (validationResult.isValid) {
        Log.i("App", "Profile is valid")
        if (validationResult.isExpired) {
            Log.w("App", "Profile certificate is expired")
        }
    } else {
        Log.e("App", "Profile validation failed: ${validationResult.errorMessage}")
    }
}
```

### Permission Management

#### `checkPermissions`

Checks if all required permissions are granted.

```kotlin
fun checkPermissions(): PermissionStatus
```

**Returns:** `PermissionStatus` with details about permission state

**Example:**
```kotlin
val permissionStatus = profileManager.checkPermissions()
if (permissionStatus.allGranted) {
    // Proceed with Passpoint operations
} else {
    Log.w("App", "Missing permissions: ${permissionStatus.missingPermissions.joinToString()}")
}
```

#### `requestPermissions`

Requests missing permissions.

```kotlin
fun requestPermissions(
    activity: Activity,
    callback: (PermissionResult) -> Unit
)
```

**Parameters:**
- `activity`: The activity to use for permission request
- `callback`: Callback to receive permission request result

**Example:**
```kotlin
profileManager.requestPermissions(this) { result ->
    if (result.allGranted) {
        // Permissions granted, proceed
    } else {
        // Handle permission denial
    }
}
```

#### `handlePermissionResult`

Handles the result of a permission request. Call this from `Activity.onRequestPermissionsResult()`.

```kotlin
fun handlePermissionResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
): PermissionResult?
```

**Parameters:**
- `requestCode`: The request code from `onRequestPermissionsResult`
- `permissions`: The permissions array from `onRequestPermissionsResult`
- `grantResults`: The grant results array from `onRequestPermissionsResult`

**Returns:** `PermissionResult` with the final permission state, or `null` if request code doesn't match

**Example:**
```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    
    profileManager.handlePermissionResult(requestCode, permissions, grantResults)?.let { result ->
        if (result.allGranted) {
            // Permissions granted
        }
    }
}
```

#### `hasAllPermissions`

Checks if all required permissions are granted (quick check).

```kotlin
fun hasAllPermissions(): Boolean
```

**Returns:** `true` if all permissions are granted, `false` otherwise

### Profile Information

#### `getProfileInfo`

Gets profile information for display in app settings.

```kotlin
suspend fun getProfileInfo(profileId: String): PasspointProfileInfo?
```

**Parameters:**
- `profileId`: The ID of the profile

**Returns:** Profile information or `null` if not found

**Example:**
```kotlin
val profileInfo = profileManager.getProfileInfo("profile-123")
profileInfo?.let {
    Log.i("App", "Profile installed: ${it.installationDate}")
    Log.i("App", "Profile expires: ${it.expirationDate}")
    Log.i("App", "Profile status: ${it.status}")
}
```

#### `getAllProfileInfo`

Gets profile information for all installed profiles.

```kotlin
suspend fun getAllProfileInfo(): List<PasspointProfileInfo>
```

**Returns:** List of profile information

**Example:**
```kotlin
val allProfileInfo = profileManager.getAllProfileInfo()
allProfileInfo.forEach { info ->
    Log.d("App", "Profile ${info.profileId}: ${info.status}")
}
```

## Data Models

### PasspointProfile

Represents a Passpoint profile.

```kotlin
data class PasspointProfile(
    val id: String? = null,
    val friendlyName: String,
    val realm: String,
    val fqdn: String,
    val homeSpFqdn: String,
    val credential: PasspointCredential,
    val policy: PasspointPolicy? = null,
    val metadata: Map<String, String>? = null
)
```

**Fields:**
- `id`: Optional unique identifier for the profile
- `friendlyName`: User-friendly name for the profile
- `realm`: Realm identifier
- `fqdn`: Fully qualified domain name
- `homeSpFqdn`: Home service provider FQDN
- `credential`: Passpoint credential (username/password or certificate)
- `policy`: Optional Passpoint policy
- `metadata`: Optional metadata map

### PasspointCredential

Represents Passpoint credentials.

```kotlin
data class PasspointCredential(
    val username: String? = null,
    val password: String? = null,
    val certificate: String? = null, // Base64 encoded
    val privateKey: String? = null, // Base64 encoded
    val realm: String
)
```

**Fields:**
- `username`: Optional username for EAP-TTLS
- `password`: Optional password for EAP-TTLS
- `certificate`: Optional Base64-encoded certificate for EAP-TLS
- `privateKey`: Optional Base64-encoded private key for EAP-TLS
- `realm`: Realm identifier

### PasspointPolicy

Represents Passpoint policy settings.

```kotlin
data class PasspointPolicy(
    val minHomeDownlinkBandwidth: Int? = null,
    val minHomeUplinkBandwidth: Int? = null,
    val minRoamingDownlinkBandwidth: Int? = null,
    val minRoamingUplinkBandwidth: Int? = null,
    val excludedSsidList: List<String>? = null,
    val maximumBssLoadValue: Int? = null
)
```

### ProfileInstallResult

Result of profile installation.

```kotlin
data class ProfileInstallResult(
    val success: Boolean,
    val profileId: String? = null,
    val errorMessage: String? = null,
    val errorCode: Int? = null
)
```

**Fields:**
- `success`: Whether installation was successful
- `profileId`: ID of the installed profile (if successful)
- `errorMessage`: Error message (if failed)
- `errorCode`: Error code (if failed)

### ProfileValidationResult

Result of profile validation.

```kotlin
data class ProfileValidationResult(
    val isValid: Boolean,
    val isExpired: Boolean = false,
    val expirationDate: Long? = null,
    val certificateValid: Boolean = true,
    val errorMessage: String? = null
)
```

**Fields:**
- `isValid`: Whether the profile is valid
- `isExpired`: Whether the certificate is expired
- `expirationDate`: Certificate expiration date (Unix timestamp)
- `certificateValid`: Whether the certificate is valid
- `errorMessage`: Error message (if validation failed)

### AndroidPasspointProfileResponse

Response model from the Android Passpoint profile API endpoint.

```kotlin
data class AndroidPasspointProfileResponse(
    val schemaVersion: Int,
    val profileId: String,
    val profileName: String,
    val homeSp: HomeSp,
    val credential: PasspointCredentialResponse
)
```

## Lifecycle Management

The SDK provides automatic lifecycle management through the `PasspointLifecycleOrchestrator`. This includes:

- **Profile Polling**: Checks for profile updates at least once daily
- **Certificate Monitoring**: Monitors certificate expiration dates
- **Renewal Scheduling**: Automatically renews profiles 90 days before expiration
- **Installation Retry**: Retries failed installations with exponential backoff

See [Lifecycle Management](../lifecycle-management.md) for detailed documentation.

## Error Handling

### PasspointErrorCallback

Interface for receiving installation error notifications.

```kotlin
interface PasspointErrorCallback {
    fun onInstallationError(error: Throwable, attempt: Int, willRetry: Boolean)
}
```

**Parameters:**
- `error`: The error that occurred
- `attempt`: The current attempt number (1-based)
- `willRetry`: Whether the system will retry the installation

**Example:**
```kotlin
val errorCallback = object : PasspointErrorCallback {
    override fun onInstallationError(error: Throwable, attempt: Int, willRetry: Boolean) {
        Log.e("App", "Installation error (attempt $attempt, willRetry: $willRetry): ${error.message}")
        
        if (!willRetry) {
            // Final failure, notify user
            showErrorToUser("Failed to install profile: ${error.message}")
        }
    }
}

val passpointClient = UplinkPasspointClient.create(
    context = applicationContext,
    coreClient = coreClient,
    errorCallback = errorCallback
)
```

### Error Types

The SDK may throw various exceptions:
- `SecurityException`: Permission denied
- `IllegalArgumentException`: Invalid profile configuration
- `IllegalStateException`: Invalid state (e.g., missing context)
- `ApiException`: API communication errors
- `RuntimeException`: General runtime errors

See [Error Handling](../error-handling.md) for detailed documentation.

## Best Practices

1. **Initialize Once**: Create the Passpoint client once and reuse it
2. **Check Permissions**: Always check permissions before operations
3. **Handle Errors**: Implement `PasspointErrorCallback` for error notifications
4. **Use Lifecycle Management**: Let the SDK handle automatic polling and renewal
5. **Validate Profiles**: Validate profiles before installation when possible
6. **Handle Results**: Always check `Result` success/failure states

## Related Documentation

- [Core SDK API Reference](core-sdk.md)
- [Permissions Guide](permissions.md)
- [Getting Started Guide](getting-started.md)
- [Lifecycle Management](../lifecycle-management.md)
- [Error Handling](../error-handling.md)
- [Code Examples](examples.md)
