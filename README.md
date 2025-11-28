## Overview

The Uplink Mobile SDK provides a comprehensive solution for integrating Uplink platform features into Android and iOS applications. This guide covers the complete integration process, including authentication, API usage, and best practices for both platforms.

**API Documentation:** [Mobile API 0.1.0](https://app.swaggerhub.com/apis/uplink-8d9/MobileAPI/0.1.0)

## Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Platform-Specific Guides](#platform-specific-guides)
5. [Error Handling](#error-handling)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)
8. [Security Considerations](#security-considerations)

## Getting Started

### Prerequisites

- **Android**: Minimum API level 24 (Android 7.0)
- **iOS**: Minimum iOS 13.0
- **Developer Credentials**: App ID and App Key (provided by Uplink team)
- **Network Access**: HTTPS connectivity to `api-gateway.develop.uplink.xyz/v2`

### Installation

#### Android

Add the SDK dependencies to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.uplink:core-sdk:1.0.0'
    implementation 'com.uplink:passpoint-profile-sdk:1.0.0'
}
```

#### iOS

Add the SDK via CocoaPods or Swift Package Manager:

**CocoaPods:**
```ruby
pod 'UplinkCoreSDK'
pod 'UplinkPasspointProfileSDK'
```

**Swift Package Manager:**
Add the repository URL to your project dependencies.

## Authentication

### Overview

The SDK uses developer credentials (App ID and App Key) to authenticate with the backend. The authentication flow is automatic and happens during SDK initialization:

1. SDK collects device information (device ID, model, OS version)
2. Sends registration request to `/mobile/subscriber/register`
3. Receives JWT token from backend
4. Token is automatically used for all subsequent API calls

### Developer Credentials

**App ID** and **App Key** are provided by the Uplink team. These credentials:
- Identify your application in the Uplink platform
- Are used for initial authentication
- Should be stored securely (never hardcoded)

**Note:** Currently, credentials are provided directly (email, SMS, etc.). In the future, they will be available through a developer portal.

### Authentication Flow

```
┌─────────────┐
│   App       │
│  (SDK)      │
└──────┬──────┘
       │
       │ 1. Initialize with appId/appKey
       │    + Device Info
       ▼
┌─────────────────────┐
│  POST /mobile/      │
│  subscriber/register│
└──────┬──────────────┘
       │
       │ 2. JWT Token
       ▼
┌─────────────┐
│   SDK       │
│  (Ready)    │
└─────────────┘
```

### Android Authentication

```kotlin
import com.uplink.core.service.UplinkService
import com.uplink.core.utils.DeviceInfoCollector
import android.content.Context

// Collect device information
val deviceInfo = DeviceInfoCollector.collectDeviceInfo(context)

// Initialize service
val service = UplinkService.getInstance()
lifecycleScope.launch {
    val initialized = service.initialize(
        baseUrl = "https://api-gateway.develop.uplink.xyz/v2",
        appId = "your-app-id",
        appKey = "your-app-key",
        deviceInfo = deviceInfo
    )
    
    if (initialized) {
        service.start()
        // SDK is ready to use
    }
}
```

### iOS Authentication

```swift
import UplinkCoreSDK

// Collect device information
let deviceInfo = DeviceInfoCollector.collectDeviceInfo()

// Initialize service
let service = UplinkServiceManager.shared
Task {
    let initialized = await service.initialize(
        baseURL: "https://api-gateway.develop.uplink.xyz/v2",
        appId: "your-app-id",
        appKey: "your-app-key",
        deviceInfo: deviceInfo
    )
    
    if initialized {
        service.start()
        // SDK is ready to use
    }
}
```

## API Endpoints

### 1. Register Subscriber

**Endpoint:** `POST /mobile/subscriber/register`

Registers a device/user pair and returns an authentication JWT token. This is called automatically during SDK initialization.

**Request Body:**
```json
{
  "appId": "string",
  "appKey": "string",
  "deviceId": "string",
  "deviceModel": "string",
  "osVersion": "string",
  "osType": "string"
}
```

**Response:**
```json
{
  "token": "jwt-token-string",
  "subscriberId": "subscriber-uuid",
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

**Status Codes:**
- `200` - Successful registration
- `201` - Resource created
- `401` - Unauthorized (invalid credentials)

### 2. Get Passpoint Profile

**Endpoint:** `GET /mobile/passpoint-profile`

Retrieves the Passpoint profile for the authenticated subscriber.

**Headers:**
- `Authorization: Bearer <jwt-token>`

**Response:**
```json
{
  "profileId": "profile-uuid",
  "version": "1.0.0",
  "profileData": "base64-encoded-profile",
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-01-01T00:00:00Z"
}
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized
- `404` - Profile not found

### 3. Check Profile Status

**Endpoint:** `GET /mobile/passpoint-profile/latest`

Checks if there's an updated version of the Passpoint profile available.

**Headers:**
- `Authorization: Bearer <jwt-token>`

**Response:**
```json
{
  "status": "active",
  "version": "1.0.0",
  "lastUpdated": "2025-01-01T00:00:00Z",
  "hasUpdate": false
}
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized

### 4. Send Device Event

**Endpoint:** `POST /mobile/event`

Sends a device event to the backend for tracking and analytics.

**Headers:**
- `Authorization: Bearer <jwt-token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "eventType": "profile_installed",
  "payload": {
    "data": {
      "profileId": "profile-uuid",
      "timestamp": "2025-01-01T00:00:00Z"
    }
  }
}
```

**Response:**
```json
{
  "eventId": "event-uuid",
  "status": "success",
  "timestamp": "2025-01-01T00:00:00Z"
}
```

**Status Codes:**
- `200` - Success
- `400` - Bad Request
- `401` - Unauthorized

### 5. Poll Server Events

**Endpoint:** `GET /mobile/events`

Polls for server events that require client action (e.g., profile updates, removal requests).

**Headers:**
- `Authorization: Bearer <jwt-token>`

**Response:**
```json
{
  "events": [
    {
      "eventType": "profile_update",
      "data": {
        "profileId": "profile-uuid",
        "version": "1.0.1"
      },
      "timestamp": "2025-01-01T00:00:00Z"
    }
  ]
}
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized

**Event Types:**
- `profile_update` - Profile update available
- `profile_remove` - Profile should be removed
- `subscriber_suspended` - Subscriber account suspended

## Platform-Specific Guides

### Android

See [Android SDK API Integration Guide](android/API_INTEGRATION.md) for:
- Detailed Android implementation examples
- Kotlin coroutines usage
- Error handling patterns
- Lifecycle management

### iOS

See [iOS SDK API Integration Guide](ios/API_INTEGRATION.md) for:
- Detailed iOS implementation examples
- Swift async/await usage
- Error handling patterns
- Combine integration

## Error Handling

### Error Types

Both platforms provide similar error handling:

#### Android

```kotlin
import com.uplink.core.api.ApiException

try {
    val response = apiService.registerSubscriber(request)
} catch (e: ApiException.Unauthorized) {
    // Invalid credentials
} catch (e: ApiException.NetworkError) {
    // Network issues
} catch (e: ApiException.ServerError) {
    // Server errors (500+)
} catch (e: ApiException) {
    // Other API errors
}
```

#### iOS

```swift
import UplinkCoreSDK

do {
    let response = try await apiService.registerSubscriber(request: request)
} catch let error as ApiError {
    switch error {
    case .unauthorized(let message):
        // Invalid credentials
    case .networkError(let message):
        // Network issues
    case .serverError(let message):
        // Server errors (500+)
    default:
        // Other API errors
    }
}
```

### Common Error Scenarios

| Error | Cause | Solution |
|-------|-------|----------|
| `401 Unauthorized` | Invalid appId/appKey | Verify credentials are correct |
| `404 Not Found` | Resource doesn't exist | Check if resource was created |
| `400 Bad Request` | Invalid request format | Verify request body matches API spec |
| `500 Server Error` | Backend issue | Retry with exponential backoff |
| Network Error | Connectivity issue | Check internet connection |

## Best Practices

### 1. Credential Management

**DO:**
- Store credentials in secure storage (Android Keystore, iOS Keychain)
- Use build configuration files (BuildConfig, Info.plist)
- Rotate credentials if compromised

**DON'T:**
- Hardcode credentials in source code
- Commit credentials to version control
- Log credentials in production builds

### 2. Error Handling

**DO:**
- Implement comprehensive error handling
- Provide user-friendly error messages
- Log errors for debugging (without sensitive data)
- Implement retry logic for transient errors

**DON'T:**
- Ignore errors silently
- Expose technical error details to end users
- Retry indefinitely without backoff

### 3. Network Operations

**DO:**
- Use appropriate polling intervals (not too frequent)
- Implement offline queue for events
- Handle network state changes
- Use background tasks for long operations

**DON'T:**
- Poll too frequently (waste battery/data)
- Block UI thread with network calls
- Make synchronous network calls

### 4. Security

**DO:**
- Use HTTPS only in production
- Validate API responses
- Implement certificate pinning (optional)
- Store sensitive data securely

**DON'T:**
- Use HTTP in production
- Trust API responses without validation
- Store tokens in plain text

### 5. Performance

**DO:**
- Cache profile data when appropriate
- Batch events when possible
- Use appropriate timeouts
- Monitor SDK performance

**DON'T:**
- Make unnecessary API calls
- Cache sensitive data insecurely
- Use very short timeouts
- Ignore performance metrics

## Troubleshooting

### Authentication Issues

**Problem:** Authentication fails with 401 error

**Solutions:**
1. Verify appId and appKey are correct
2. Check network connectivity
3. Ensure device information is collected correctly
4. Verify base URL is correct
5. Check logs for detailed error messages

### Network Issues

**Problem:** Network errors or timeouts

**Solutions:**
1. Verify internet connectivity
2. Check if base URL is accessible
3. Verify SSL certificates are valid
4. Check firewall/proxy settings
5. Ensure App Transport Security (iOS) allows the domain

### Profile Issues

**Problem:** Profile not found or update not detected

**Solutions:**
1. Verify subscriber is registered
2. Check authentication token is valid
3. Verify profile exists for subscriber
4. Check profile status endpoint response
5. Review API logs for errors

### Platform-Specific Issues

#### Android

- **Coroutine Issues**: Ensure you're using appropriate coroutine scope
- **Context Issues**: Use application context for long-lived operations
- **Permissions**: Ensure required permissions are granted

#### iOS

- **Async/Await Issues**: Use `Task { }` when calling async from sync context
- **Main Actor**: Use `@MainActor` for UI updates
- **App Transport Security**: Configure ATS for custom domains

## Security Considerations

### Credential Security

- **Storage**: Use platform secure storage (Keystore/Keychain)
- **Transmission**: Always use HTTPS
- **Logging**: Never log credentials in production
- **Rotation**: Implement credential rotation mechanism

### Token Management

- **Storage**: Store JWT tokens securely
- **Expiration**: Handle token expiration gracefully
- **Refresh**: Implement token refresh if needed
- **Validation**: Validate token before use

### Data Protection

- **Encryption**: Encrypt sensitive data at rest
- **Transmission**: Use TLS 1.2+ for all communications
- **Validation**: Validate all API responses
- **Sanitization**: Sanitize user inputs

### Best Practices

1. **Never log sensitive data** in production builds
2. **Use certificate pinning** for additional security (optional)
3. **Implement rate limiting** to prevent abuse
4. **Monitor for suspicious activity**
5. **Keep SDK updated** with latest security patches

## Support and Resources

### Documentation

- **API Reference**: [SwaggerHub](https://app.swaggerhub.com/apis/uplink-8d9/MobileAPI/0.1.0)
- **Android Guide**: [Android API Integration](android/API_INTEGRATION.md)
- **iOS Guide**: [iOS API Integration](ios/API_INTEGRATION.md)

### Getting Help

- **Technical Support**: Contact Uplink support team
- **Bug Reports**: Report issues with detailed logs and steps to reproduce
- **Feature Requests**: Submit through appropriate channels

### Version Information

- **SDK Version**: 1.0.0
- **API Version**: 0.1.0
- **Last Updated**: 2025-01-01

---

**Note:** This documentation is subject to change as the API evolves. Always refer to the latest API documentation for the most up-to-date information.
