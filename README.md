# iOS SDK - Mobile API Integration Guide

## Overview

The Uplink iOS SDK integrates with the Mobile API backend to provide developer authentication, Passpoint profile management, event tracking, and server event polling. This guide covers the API integration, authentication flow, and usage examples.

## Table of Contents

1. [Authentication](#authentication)
2. [API Endpoints](#api-endpoints)
3. [Error Handling](#error-handling)
4. [Usage Examples](#usage-examples)
5. [Best Practices](#best-practices)

## Authentication

### Developer Authentication Flow

The SDK uses developer credentials (appId and appKey) to authenticate with the backend. Upon initialization, the SDK automatically:

1. Collects device information (device ID, model, OS version)
2. Sends registration request to `/mobile/subscriber/register`
3. Receives JWT token from the backend
4. Stores token for subsequent API calls

### Initialization

```swift
import UplinkCoreSDK

// Collect device information
let deviceInfo = DeviceInfoCollector.collectDeviceInfo()

// Initialize service with developer credentials
let service = UplinkServiceManager.shared
Task {
    let initialized = await service.initialize(
        baseURL: "https://api-gateway.develop.uplink.xyz/v2", // Optional, defaults to dev server
        appId: "your-app-id",
        appKey: "your-app-key",
        deviceInfo: deviceInfo
    )
    
    if initialized {
        service.start()
    }
}
```

### Using the Core Client

```swift
import UplinkCoreSDK

Task {
    let client = await UplinkCoreClient.fromService(
        appId: "your-app-id",
        appKey: "your-app-key",
        deviceInfo: DeviceInfoCollector.collectDeviceInfo()
    )
    
    // Client is now authenticated and ready to use
    let httpClient = client?.getHttpClient()
    let apiService = client?.getApiService()
}
```

## API Endpoints

### 1. Register Subscriber

**Endpoint:** `POST /mobile/subscriber/register`

Registers a device/user pair and returns an authentication JWT token.

**Request:**
```swift
import UplinkCoreSDK

let request = RegisterRequest(
    appId: "your-app-id",
    appKey: "your-app-key",
    deviceId: deviceInfo.deviceId,
    deviceModel: deviceInfo.deviceModel,
    osVersion: deviceInfo.osVersion,
    osType: deviceInfo.osType
)
```

**Response:**
```swift
public struct RegisterResponse: Codable {
    public let token: String           // JWT token for authentication
    public let subscriberId: String    // Unique subscriber identifier
    public let expiresAt: String?      // Token expiration (optional)
}
```

**Usage:**
```swift
let apiService = client?.getApiService()
let response = try await apiService?.registerSubscriber(request: request)
// Token is automatically set on HttpClient
```

### 2. Get Passpoint Profile

**Endpoint:** `GET /mobile/passpoint-profile`

Retrieves the Passpoint profile for the authenticated subscriber.

**Response:**
```swift
public struct PasspointProfileResponse: Codable {
    public let profileId: String?
    public let version: String?
    public let profileData: String?
    public let createdAt: String?
    public let updatedAt: String?
}
```

**Usage:**
```swift
let profile = try await apiService?.getPasspointProfile()
```

### 3. Check Profile Status

**Endpoint:** `GET /mobile/passpoint-profile/latest`

Checks if there's an updated version of the Passpoint profile available.

**Response:**
```swift
public struct ProfileStatusResponse: Codable {
    public let status: String          // Profile status
    public let version: String?        // Current version
    public let lastUpdated: String?    // Last update timestamp
    public let hasUpdate: Bool         // Whether update is available
}
```

**Usage:**
```swift
let status = try await apiService?.getProfileStatus()
if status?.hasUpdate == true {
    // Download updated profile
}
```

### 4. Send Device Event

**Endpoint:** `POST /mobile/event`

Sends a device event to the backend for tracking and analytics.

**Request:**
```swift
let request = EventRequest(
    eventType: "profile_installed",
    payload: EventPayload(
        data: [
            "profileId": "profile-123",
            "timestamp": String(Date().timeIntervalSince1970)
        ]
    )
)
```

**Response:**
```swift
public struct EventResponse: Codable {
    public let eventId: String
    public let status: String
    public let timestamp: String
}
```

**Usage:**
```swift
let response = try await apiService?.sendEvent(request: request)
```

### 5. Poll Server Events

**Endpoint:** `GET /mobile/events`

Polls for server events that require client action (e.g., profile updates, removal requests).

**Response:**
```swift
public struct ServerEventsResponse: Codable {
    public let events: [ServerEventItem]
}

public struct ServerEventItem: Codable {
    public let eventType: String
    public let data: [String: String]
    public let timestamp: String
}
```

**Usage:**
```swift
let eventsResponse = try await apiService?.getServerEvents()
eventsResponse?.events.forEach { event in
    switch event.eventType {
    case "profile_update":
        // Handle profile update
        break
    case "profile_remove":
        // Handle profile removal
        break
    default:
        break
    }
}
```

## Error Handling

### API Errors

The SDK provides custom error types for different error scenarios:

```swift
import UplinkCoreSDK

do {
    let response = try await apiService?.registerSubscriber(request: request)
} catch let error as ApiError {
    switch error {
    case .unauthorized(let message):
        print("Authentication failed: \(message)")
    case .networkError(let message):
        print("Network error: \(message)")
    case .serverError(let message):
        print("Server error: \(message)")
    default:
        print("API error: \(error.localizedDescription)")
    }
} catch {
    print("Unexpected error: \(error.localizedDescription)")
}
```

### Error Types

- `ApiError.unauthorized(String)` - 401 Unauthorized (invalid credentials)
- `ApiError.notFound(String)` - 404 Not Found (resource not found)
- `ApiError.badRequest(String)` - 400 Bad Request (invalid request)
- `ApiError.serverError(String)` - 500+ Server errors
- `ApiError.networkError(String)` - Network connectivity issues
- `ApiError.unknown(String)` - Unknown/unexpected errors

## Usage Examples

### Complete Initialization Example

```swift
import UIKit
import UplinkCoreSDK

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Task {
            do {
                // Collect device information
                let deviceInfo = DeviceInfoCollector.collectDeviceInfo()
                
                // Initialize service
                let service = UplinkServiceManager.shared
                let initialized = await service.initialize(
                    appId: Bundle.main.object(forInfoDictionaryKey: "UplinkAppId") as! String,
                    appKey: Bundle.main.object(forInfoDictionaryKey: "UplinkAppKey") as! String,
                    deviceInfo: deviceInfo
                )
                
                if initialized {
                    service.start()
                    let client = service.getCoreClient()
                    
                    // SDK is ready to use
                    print("SDK initialized successfully")
                }
            } catch {
                print("Failed to initialize SDK: \(error.localizedDescription)")
            }
        }
    }
}
```

### Polling for Server Events

```swift
import UplinkCoreSDK

// Schedule periodic polling
let client = service.getCoreClient()
let scheduler = client?.getTaskScheduler()

scheduler?.schedule(
    taskId: "server-events-poll",
    intervalSeconds: 3600, // Poll every hour
    task: {
        Task {
            do {
                let apiService = client?.getApiService()
                let events = try await apiService?.getServerEvents()
                events?.events.forEach { event in
                    // Process event
                }
            } catch {
                print("Failed to poll events: \(error.localizedDescription)")
            }
        }
    }
)
```

### Using Async/Await with Combine

```swift
import Combine
import UplinkCoreSDK

class EventPoller {
    private var cancellables = Set<AnyCancellable>()
    
    func startPolling() {
        Timer.publish(every: 3600, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                Task {
                    await self?.pollEvents()
                }
            }
            .store(in: &cancellables)
    }
    
    private func pollEvents() async {
        guard let client = UplinkServiceManager.shared.getCoreClient(),
              let apiService = client.getApiService() else {
            return
        }
        
        do {
            let events = try await apiService.getServerEvents()
            // Process events
        } catch {
            print("Polling error: \(error.localizedDescription)")
        }
    }
}
```

## Best Practices

1. **Store Credentials Securely**: Never hardcode appId/appKey in your source code. Use Info.plist or secure keychain storage.

2. **Handle Errors Gracefully**: Always use do-catch blocks for async/await calls and provide user-friendly error messages.

3. **Implement Retry Logic**: For network errors, implement exponential backoff retry logic using async/await.

4. **Poll Efficiently**: Don't poll too frequently. Use the periodic task scheduler with appropriate intervals.

5. **Monitor Authentication**: Check `client.getIsAuthenticated()` before making API calls that require authentication.

6. **Use Task for Async Operations**: Always wrap async SDK calls in `Task { }` blocks when called from synchronous contexts.

7. **Handle Token Expiration**: Implement token refresh logic if tokens expire. Monitor for 401 errors and re-authenticate.

8. **Use Main Actor for UI Updates**: When updating UI based on API responses, ensure you're on the main actor.

```swift
Task { @MainActor in
    // Update UI here
    self.statusLabel.text = "Connected"
}
```

## Security Considerations

- **Never log credentials**: Avoid logging appId, appKey, or JWT tokens in production builds.
- **Use HTTPS only**: Always use HTTPS endpoints in production.
- **Validate responses**: Always validate API responses before using the data.
- **Secure storage**: Consider storing sensitive data using iOS Keychain.

## Troubleshooting

### Authentication Fails

- Verify appId and appKey are correct
- Check network connectivity
- Ensure device information is collected correctly
- Check console logs for detailed error messages

### Network Errors

- Verify internet connectivity
- Check if the base URL is correct
- Verify SSL certificates are valid
- Check firewall/proxy settings
- Ensure App Transport Security allows the domain

### API Errors

- Review error response details
- Check API documentation for required fields
- Verify request format matches API specification
- Contact support with error details and logs

### Async/Await Issues

- Ensure you're using `Task { }` when calling async functions from synchronous contexts
- Use `@MainActor` for UI updates
- Check for proper error handling with do-catch blocks

