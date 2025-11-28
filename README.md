# Android SDK - Mobile API Integration Guide

## Overview

The Uplink Android SDK integrates with the Mobile API backend to provide developer authentication, Passpoint profile management, event tracking, and server event polling. This guide covers the API integration, authentication flow, and usage examples.

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

```kotlin
import com.uplink.core.service.UplinkService
import com.uplink.core.utils.DeviceInfoCollector
import android.content.Context

// Collect device information
val context: Context = // Your application context
val deviceInfo = DeviceInfoCollector.collectDeviceInfo(context)

// Initialize service with developer credentials
val service = UplinkService.getInstance()
lifecycleScope.launch {
    val initialized = service.initialize(
        baseUrl = "https://api-gateway.develop.uplink.xyz/v2", // Optional, defaults to dev server
        appId = "your-app-id",
        appKey = "your-app-key",
        deviceInfo = deviceInfo
    )
    
    if (initialized) {
        service.start()
    }
}
```

### Using the Core Client

```kotlin
import com.uplink.core.UplinkCoreClient
import com.uplink.core.utils.DeviceInfoCollector

lifecycleScope.launch {
    val client = UplinkCoreClient.fromService(
        appId = "your-app-id",
        appKey = "your-app-key",
        deviceInfo = DeviceInfoCollector.collectDeviceInfo(context)
    )
    
    // Client is now authenticated and ready to use
    val httpClient = client?.httpClient()
    val apiService = client?.getApiService()
}
```

## API Endpoints

### 1. Register Subscriber

**Endpoint:** `POST /mobile/subscriber/register`

Registers a device/user pair and returns an authentication JWT token.

**Request:**
```kotlin
import com.uplink.core.api.models.RegisterRequest

val request = RegisterRequest(
    appId = "your-app-id",
    appKey = "your-app-key",
    deviceId = deviceInfo.deviceId,
    deviceModel = deviceInfo.deviceModel,
    osVersion = deviceInfo.osVersion,
    osType = deviceInfo.osType
)
```

**Response:**
```kotlin
import com.uplink.core.api.models.RegisterResponse

data class RegisterResponse(
    val token: String,           // JWT token for authentication
    val subscriberId: String,    // Unique subscriber identifier
    val expiresAt: String?       // Token expiration (optional)
)
```

**Usage:**
```kotlin
val apiService = client?.getApiService()
val response = apiService?.registerSubscriber(request)
// Token is automatically set on HttpClient
```

### 2. Get Passpoint Profile

**Endpoint:** `GET /mobile/passpoint-profile`

Retrieves the Passpoint profile for the authenticated subscriber.

**Response:**
```kotlin
import com.uplink.core.api.models.PasspointProfileResponse

data class PasspointProfileResponse(
    val profileId: String?,
    val version: String?,
    val profileData: String?,
    val createdAt: String?,
    val updatedAt: String?
)
```

**Usage:**
```kotlin
val profile = apiService?.getPasspointProfile()
```

### 3. Check Profile Status

**Endpoint:** `GET /mobile/passpoint-profile/latest`

Checks if there's an updated version of the Passpoint profile available.

**Response:**
```kotlin
import com.uplink.core.api.models.ProfileStatusResponse

data class ProfileStatusResponse(
    val status: String,          // Profile status
    val version: String?,        // Current version
    val lastUpdated: String?,    // Last update timestamp
    val hasUpdate: Boolean       // Whether update is available
)
```

**Usage:**
```kotlin
val status = apiService?.getProfileStatus()
if (status?.hasUpdate == true) {
    // Download updated profile
}
```

### 4. Send Device Event

**Endpoint:** `POST /mobile/event`

Sends a device event to the backend for tracking and analytics.

**Request:**
```kotlin
import com.uplink.core.api.models.EventRequest
import com.uplink.core.api.models.EventPayload

val request = EventRequest(
    eventType = "profile_installed",
    payload = EventPayload(
        data = mapOf(
            "profileId" to "profile-123",
            "timestamp" to System.currentTimeMillis().toString()
        )
    )
)
```

**Response:**
```kotlin
import com.uplink.core.api.models.EventResponse

data class EventResponse(
    val eventId: String,
    val status: String,
    val timestamp: String
)
```

**Usage:**
```kotlin
val response = apiService?.sendEvent(request)
```

### 5. Poll Server Events

**Endpoint:** `GET /mobile/events`

Polls for server events that require client action (e.g., profile updates, removal requests).

**Response:**
```kotlin
import com.uplink.core.api.models.ServerEventsResponse
import com.uplink.core.api.models.ServerEventItem

data class ServerEventsResponse(
    val events: List<ServerEventItem>
)

data class ServerEventItem(
    val eventType: String,
    val data: Map<String, String>,
    val timestamp: String
)
```

**Usage:**
```kotlin
val eventsResponse = apiService?.getServerEvents()
eventsResponse?.events?.forEach { event ->
    when (event.eventType) {
        "profile_update" -> {
            // Handle profile update
        }
        "profile_remove" -> {
            // Handle profile removal
        }
    }
}
```

## Error Handling

### API Exceptions

The SDK provides custom exception types for different error scenarios:

```kotlin
import com.uplink.core.api.ApiException

try {
    val response = apiService?.registerSubscriber(request)
} catch (e: ApiException.Unauthorized) {
    // Invalid appId/appKey
    Log.e("Auth", "Authentication failed: ${e.message}")
} catch (e: ApiException.NetworkError) {
    // Network connectivity issues
    Log.e("Network", "Network error: ${e.message}")
} catch (e: ApiException.ServerError) {
    // Server-side error (500+)
    Log.e("Server", "Server error: ${e.message}")
} catch (e: ApiException) {
    // Other API errors
    Log.e("API", "API error: ${e.message}")
}
```

### Error Types

- `ApiException.Unauthorized` - 401 Unauthorized (invalid credentials)
- `ApiException.NotFound` - 404 Not Found (resource not found)
- `ApiException.BadRequest` - 400 Bad Request (invalid request)
- `ApiException.ServerError` - 500+ Server errors
- `ApiException.NetworkError` - Network connectivity issues
- `ApiException.Unknown` - Unknown/unexpected errors

## Usage Examples

### Complete Initialization Example

```kotlin
import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.uplink.core.service.UplinkService
import com.uplink.core.utils.DeviceInfoCollector
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            try {
                // Collect device information
                val deviceInfo = DeviceInfoCollector.collectDeviceInfo(this@MainActivity)
                
                // Initialize service
                val service = UplinkService.getInstance()
                val initialized = service.initialize(
                    appId = BuildConfig.UPLINK_APP_ID,
                    appKey = BuildConfig.UPLINK_APP_KEY,
                    deviceInfo = deviceInfo
                )
                
                if (initialized) {
                    service.start()
                    val client = service.getCoreClient()
                    
                    // SDK is ready to use
                    Log.d("SDK", "SDK initialized successfully")
                }
            } catch (e: Exception) {
                Log.e("SDK", "Failed to initialize SDK: ${e.message}", e)
            }
        }
    }
}
```

### Polling for Server Events

```kotlin
import com.uplink.core.periodic.PeriodicTaskScheduler

// Schedule periodic polling
val client = service.getCoreClient()
val scheduler = client?.taskScheduler()

scheduler?.schedule(
    taskId = "server-events-poll",
    intervalSeconds = 3600, // Poll every hour
    task = {
        try {
            val apiService = client?.getApiService()
            val events = apiService?.getServerEvents()
            events?.events?.forEach { event ->
                // Process event
            }
        } catch (e: Exception) {
            Log.e("Polling", "Failed to poll events: ${e.message}", e)
        }
    }
)
```

## Best Practices

1. **Store Credentials Securely**: Never hardcode appId/appKey in your source code. Use `BuildConfig` or secure storage.

2. **Handle Errors Gracefully**: Always wrap API calls in try-catch blocks and provide user-friendly error messages.

3. **Implement Retry Logic**: For network errors, implement exponential backoff retry logic.

4. **Poll Efficiently**: Don't poll too frequently. Use the periodic task scheduler with appropriate intervals.

5. **Monitor Authentication**: Check `client.isAuthenticated()` before making API calls that require authentication.

6. **Use Lifecycle-Aware Components**: Initialize the SDK in `Application.onCreate()` or `Activity.onCreate()` using lifecycle-aware coroutines.

7. **Handle Token Expiration**: Implement token refresh logic if tokens expire. Monitor for 401 errors and re-authenticate.

## Security Considerations

- **Never log credentials**: Avoid logging appId, appKey, or JWT tokens in production builds.
- **Use HTTPS only**: Always use HTTPS endpoints in production.
- **Validate responses**: Always validate API responses before using the data.
- **Secure storage**: Consider storing sensitive data using Android Keystore.

## Troubleshooting

### Authentication Fails

- Verify appId and appKey are correct
- Check network connectivity
- Ensure device information is collected correctly
- Check logs for detailed error messages

### Network Errors

- Verify internet connectivity
- Check if the base URL is correct
- Verify SSL certificates are valid
- Check firewall/proxy settings

### API Errors

- Review error response details
- Check API documentation for required fields
- Verify request format matches API specification
- Contact support with error details and logs

