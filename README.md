## Overview

The Passpoint SDK uses encrypted storage for profile data and maintains a local cache for profile tracking and metadata.

## Encrypted Storage

### Android

Uses `EncryptedSharedPreferences` for secure storage:

- Profile data encryption
- Credential storage
- Profile metadata
- Installation timestamps
- Expiration dates
- Profile status

### iOS

Uses Keychain for secure storage:

- Profile data encryption
- Credential storage
- Profile metadata
- Installation timestamps
- Expiration dates
- Profile status

## Profile Cache

### Cache Structure

The cache maintains:
- Profile IDs and FQDNs
- Installation dates
- Expiration dates
- Profile status
- Last poll time

### Cache Operations

#### Android

```kotlin
// Cache is managed internally by the SDK
// Access via ProfileInfoService
val profileInfo = profileManager.getProfileInfo("profile-id")
val allInfo = profileManager.getAllProfileInfo()
```

#### iOS

```swift
// Cache is managed internally by the SDK
// Access via ProfileInfoService
let profileInfo = try await profileManager.getProfileInfo(profileId: "profile-id")
let allInfo = try await profileManager.getAllProfileInfo()
```

## Profile Information Service

Provides access to profile metadata:

### Get Profile Info

```kotlin
// Android
val profileInfo = profileManager.getProfileInfo("profile-id")
profileInfo?.let {
    Log.i("App", "Installed: ${it.installationDate}")
    Log.i("App", "Expires: ${it.expirationDate}")
    Log.i("App", "Status: ${it.status}")
}
```

```swift
// iOS
if let profileInfo = try await profileManager.getProfileInfo(profileId: "profile-id") {
    print("Installed: \(profileInfo.installationDate)")
    print("Expires: \(profileInfo.expirationDate?.description ?? "Never")")
    print("Status: \(profileInfo.status)")
}
```

### Get All Profile Info

```kotlin
// Android
val allInfo = profileManager.getAllProfileInfo()
allInfo.forEach { info ->
    Log.d("App", "Profile ${info.profileId}: ${info.status}")
}
```

```swift
// iOS
let allInfo = try await profileManager.getAllProfileInfo()
for info in allInfo {
    print("Profile \(info.profileId): \(info.status)")
}
```

## Profile Status Tracking

The SDK tracks profile status:

- **ACTIVE**: Profile is installed and valid
- **EXPIRED**: Certificate has expired
- **PENDING_RENEWAL**: Certificate is within renewal window (90 days before expiration)

## Cache Synchronization

The cache is automatically synchronized when:
- Profile is installed
- Profile is removed
- Profile is updated
- Certificate monitoring detects changes

## Security Considerations

1. **Encryption**: All sensitive data is encrypted at rest
2. **Keychain/EncryptedSharedPreferences**: Uses platform secure storage
3. **No Plain Text**: Credentials are never stored in plain text
4. **Automatic Cleanup**: Cache is cleaned when profiles are removed

## Best Practices

1. **Trust the Cache**: The SDK manages cache automatically
2. **Check Status**: Use profile info to check status before operations
3. **Monitor Expiration**: Use expiration dates for renewal planning
4. **Don't Modify Cache**: Don't directly modify cache data

## Related Documentation

- [Lifecycle Management](lifecycle-management.md)
- [Error Handling](error-handling.md)
