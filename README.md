## Overview

This guide covers error types, error codes, and best practices for handling errors in the Passpoint SDK.

## Error Categories

### Authentication Errors

Occur when authentication with the backend API fails.

**Android:**
- `ApiException.Unauthorized`: Invalid credentials or expired token

**iOS:**
- `NSError` with domain "UplinkCoreClient" and code -1

**Handling:**
```kotlin
// Android
try {
    val coreClient = UplinkCoreClient.create(...)
} catch (e: ApiException.Unauthorized) {
    // Re-authenticate or show error to user
}
```

```swift
// iOS
do {
    let coreClient = await UplinkCoreClient.create(...)
} catch {
    // Handle authentication error
}
```

### Network Errors

Occur when network communication fails.

**Android:**
- `ApiException.NetworkError`: Network connectivity issues
- `ApiException.ServerError`: Server errors (5xx)

**iOS:**
- `AFError` from Alamofire for network errors

**Handling:**
```kotlin
// Android
try {
    val profile = coreClient.getAndroidPasspointProfile()
} catch (e: ApiException.NetworkError) {
    // Retry or show network error
} catch (e: ApiException.ServerError) {
    // Server error, may be transient
}
```

### Permission Errors

Occur when required permissions/entitlements are missing.

**Android:**
- `SecurityException`: Permission denied

**iOS:**
- `PasspointPermissionError.missingEntitlements`: Missing entitlements

**Handling:**
```kotlin
// Android
try {
    profileManager.installProfile(profile)
} catch (e: SecurityException) {
    // Request permissions
    profileManager.requestPermissions(activity) { ... }
}
```

```swift
// iOS
do {
    try await profileManager.installProfile(profile)
} catch let error as PasspointPermissionError {
    // Handle missing entitlements
    showEntitlementError(error)
}
```

### Installation Errors

Occur during profile installation.

**Common Causes:**
- Invalid profile configuration
- Missing required fields
- Platform-specific limitations

**Handling:**
```kotlin
// Android
val result = profileManager.installProfile(profile)
result.onFailure { error ->
    when (error) {
        is IllegalArgumentException -> {
            // Invalid profile configuration
        }
        is SecurityException -> {
            // Permission denied
        }
        else -> {
            // Other errors
        }
    }
}
```

### Validation Errors

Occur during profile validation.

**Handling:**
```kotlin
// Android
val result = profileManager.verifyProfile(profile)
result.onSuccess { validationResult ->
    if (!validationResult.isValid) {
        // Handle validation failure
        Log.e("App", validationResult.errorMessage ?: "Validation failed")
    }
}
```

## Error Codes Reference

### Android API Exception Codes

- `401`: Unauthorized
- `400`: Bad Request
- `404`: Not Found
- `500/502/503`: Server Error

### iOS Error Domains

- `UplinkCoreClient`: Core SDK errors
- `PasspointProfileInstaller`: Installation errors
- `PasspointProfileRemover`: Removal errors
- `PasspointPermissionError`: Permission/entitlement errors
- `AFError`: Network errors (Alamofire)

## Error Callback Implementation

### Android

```kotlin
val errorCallback = object : PasspointErrorCallback {
    override fun onInstallationError(error: Throwable, attempt: Int, willRetry: Boolean) {
        when {
            error is SecurityException -> {
                // Permission error - not retryable
                showPermissionError()
            }
            error is IllegalArgumentException -> {
                // Configuration error - not retryable
                showConfigurationError(error.message)
            }
            willRetry -> {
                // Will retry automatically
                Log.d("App", "Installation will retry (attempt $attempt)")
            }
            else -> {
                // Final failure
                showInstallationError(error.message)
            }
        }
    }
}
```

### iOS

```swift
class MyErrorCallback: PasspointErrorCallback {
    func onInstallationError(error: Error, attempt: Int, willRetry: Bool) {
        if let permissionError = error as? PasspointPermissionError {
            // Permission error - not retryable
            showPermissionError(permissionError)
        } else if willRetry {
            // Will retry automatically
            print("Installation will retry (attempt \(attempt))")
        } else {
            // Final failure
            showInstallationError(error.localizedDescription)
        }
    }
}
```

## Retry Strategies

The SDK implements automatic retry with exponential backoff for retryable errors.

### Retryable Errors

- Network errors
- Transient system errors
- Timeouts
- Server errors (5xx)

### Non-Retryable Errors

- Permission errors
- Configuration errors
- Unsupported operations

### Manual Retry

```kotlin
// Android
var attempts = 0
val maxAttempts = 3

suspend fun installWithRetry(profile: PasspointProfile) {
    while (attempts < maxAttempts) {
        val result = profileManager.installProfile(profile)
        result.onSuccess {
            return // Success
        }.onFailure { error ->
            attempts++
            if (attempts >= maxAttempts) {
                // Final failure
                handleError(error)
            } else {
                // Wait before retry
                delay(1000L * attempts) // Exponential backoff
            }
        }
    }
}
```

## Best Practices

1. **Always Handle Errors**: Wrap SDK calls in try-catch or Result handling
2. **Check Error Types**: Handle different error types appropriately
3. **Provide User Feedback**: Show user-friendly error messages
4. **Log Errors**: Log errors for debugging
5. **Implement Retry Logic**: For transient errors, implement retry logic
6. **Use Error Callbacks**: Implement error callbacks for installation errors

## Related Documentation

- [Lifecycle Management](lifecycle-management.md)
- [Troubleshooting](troubleshooting.md)
- [Best Practices](best-practices.md)
