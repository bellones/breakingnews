# Overview

The Uplink Core SDK provides foundational services for authentication, HTTP communication, logging, task scheduling, and license management. It serves as the base layer for all Uplink SDK modules.

## UplinkCoreClient

The main client class for the Core SDK. Provides access to authentication, HTTP client, and utility services.

### Initialization

#### `create`

Creates a new Core SDK client instance and authenticates with the backend.

```kotlin
suspend fun create(
    baseUrl: String,
    appId: String,
    appSecret: String,
    deviceInfo: DeviceInfo,
    context: Context? = null
): UplinkCoreClient
```

**Parameters:**
- `baseUrl`: Base URL for API communication (e.g., `"https://api-gateway.develop.uplink.xyz/v2"`)
- `appId`: Developer application ID
- `appSecret`: Developer application secret
- `deviceInfo`: Device information for registration
- `context`: Application context (optional, required for credential storage)

**Returns:** Authenticated `UplinkCoreClient` instance

**Throws:** `ApiException` if authentication fails

**Example:**
```kotlin
val deviceInfo = DeviceInfo(
    deviceId = getDeviceId(),
    deviceModel = Build.MODEL,
    osVersion = Build.VERSION.RELEASE,
    osType = "Android"
)

val coreClient = UplinkCoreClient.create(
    baseUrl = "https://api-gateway.develop.uplink.xyz/v2",
    appId = "your-app-id",
    appSecret = "your-app-secret",
    deviceInfo = deviceInfo,
    context = applicationContext
)
```

#### `fromService`

Gets the core client from the service singleton. Initializes and starts the service if not already done.

```kotlin
suspend fun fromService(
    baseUrl: String = "https://api-gateway.develop.uplink.xyz/v2",
    appId: String,
    appSecret: String,
    deviceInfo: DeviceInfo,
    context: Context? = null
): UplinkCoreClient?
```

**Parameters:**
- `baseUrl`: Base URL for API communication (defaults to development server)
- `appId`: Developer application ID
- `appSecret`: Developer application secret
- `deviceInfo`: Device information for registration
- `context`: Application context (optional, required for credential storage)

**Returns:** `UplinkCoreClient` instance from the service, or `null` if service initialization fails

**Example:**
```kotlin
val coreClient = UplinkCoreClient.fromService(
    baseUrl = "https://api-gateway.develop.uplink.xyz/v2",
    appId = "your-app-id",
    appSecret = "your-app-secret",
    deviceInfo = deviceInfo,
    context = applicationContext
)
```

#### `createMock`

Creates a mock Core SDK client instance for testing. Skips authentication and initializes with mock values.

```kotlin
fun createMock(
    baseUrl: String = "https://api-gateway.develop.uplink.xyz/v2",
    context: Context? = null
): UplinkCoreClient
```

**Parameters:**
- `baseUrl`: Base URL for API communication (defaults to development server)
- `context`: Application context (optional)

**Returns:** Mock `UplinkCoreClient` instance with mock authentication

**Example:**
```kotlin
val mockClient = UplinkCoreClient.createMock(
    baseUrl = "https://api-gateway.develop.uplink.xyz/v2",
    context = applicationContext
)
```

### Authentication

#### `refreshTokenIfNeeded`

Refreshes the authentication token if it has expired. Re-authenticates using stored credentials.

```kotlin
suspend fun refreshTokenIfNeeded()
```

**Throws:** `ApiException` if re-authentication fails

**Example:**
```kotlin
try {
    coreClient.refreshTokenIfNeeded()
} catch (e: ApiException) {
    Log.e("App", "Failed to refresh token: ${e.message}")
}
```

### Subscriber Management

#### `createSubscriber`

Creates a subscriber manually. Uses organizationId from the stored JWT token.

```kotlin
suspend fun createSubscriber(): Result<SubscriberResponse>
```

**Returns:** `Result<SubscriberResponse>` containing subscriber ID or error

**Example:**
```kotlin
val result = coreClient.createSubscriber()
result.onSuccess { subscriber ->
    Log.i("App", "Subscriber created: ${subscriber.subscriberId}")
}.onFailure { error ->
    Log.e("App", "Failed to create subscriber: ${error.message}")
}
```

### Token Management

#### `setAccessToken`

Updates the access token for API requests.

```kotlin
fun setAccessToken(token: String?)
```

**Parameters:**
- `token`: New access token (or `null` to clear)

**Example:**
```kotlin
coreClient.setAccessToken("new-jwt-token")
```

#### `isAuthenticated`

Checks if the client is authenticated.

```kotlin
fun isAuthenticated(): Boolean
```

**Returns:** `true` if authenticated, `false` otherwise

**Example:**
```kotlin
if (coreClient.isAuthenticated()) {
    // Proceed with authenticated operations
}
```

### Utility Accessors

#### `httpClient`

Gets the HTTP client for API communication.

```kotlin
fun httpClient(): HttpClient
```

**Returns:** `HttpClient` instance

**Example:**
```kotlin
val httpClient = coreClient.httpClient()
```

#### `taskScheduler`

Gets the periodic task scheduler.

```kotlin
fun taskScheduler(): PeriodicTaskScheduler
```

**Returns:** `PeriodicTaskScheduler` instance

**Example:**
```kotlin
val scheduler = coreClient.taskScheduler()
```

#### `logger`

Gets the logger utility.

```kotlin
fun logger(): Logger
```

**Returns:** `Logger` instance

**Example:**
```kotlin
val logger = coreClient.logger()
logger.info("Application started")
```

#### `licenseController`

Gets the license controller for managing module and feature access.

```kotlin
fun licenseController(): LicenseController
```

**Returns:** `LicenseController` instance

**Example:**
```kotlin
val licenseController = coreClient.licenseController()
if (licenseController.isModuleAllowed("passpoint-profile-sdk")) {
    // Use Passpoint SDK
}
```

### Passpoint Profile Fetching

#### `getAndroidPasspointProfile`

Gets Android Passpoint profile from the backend API with automatic subscriber creation.

```kotlin
suspend fun getAndroidPasspointProfile(): AndroidPasspointProfileResponse
```

**Returns:** `AndroidPasspointProfileResponse` containing profile data

**Throws:** `ApiException` if the request fails

**Note:** Automatically creates a subscriber if one doesn't exist

**Example:**
```kotlin
try {
    val profileResponse = coreClient.getAndroidPasspointProfile()
    // Use profileResponse to install profile
} catch (e: ApiException) {
    Log.e("App", "Failed to fetch profile: ${e.message}")
}
```

## Service Architecture

### UplinkService

Singleton service manager for the Core SDK. Provides centralized access to the SDK client with lifecycle management.

#### Getting the Service Instance

```kotlin
val service = UplinkService.getInstance()
```

#### Service Lifecycle

##### `initialize`

Initializes the service with developer credentials. Authenticates with backend using appId/appSecret and receives JWT token.

```kotlin
suspend fun initialize(
    baseUrl: String = "https://api-gateway.develop.uplink.xyz/v2",
    appId: String,
    appSecret: String,
    deviceInfo: DeviceInfo,
    context: Context? = null
): Boolean
```

**Parameters:**
- `baseUrl`: Base URL for API communication (defaults to development server)
- `appId`: Developer app ID
- `appSecret`: Developer app secret
- `deviceInfo`: Device information
- `context`: Application context (optional, required for credential storage)

**Returns:** `true` if initialization was successful, `false` if already initialized

**Example:**
```kotlin
val initialized = service.initialize(
    baseUrl = "https://api-gateway.develop.uplink.xyz/v2",
    appId = "your-app-id",
    appSecret = "your-app-secret",
    deviceInfo = deviceInfo,
    context = applicationContext
)
```

##### `start`

Starts the service. Service must be initialized before starting.

```kotlin
suspend fun start(): Boolean
```

**Returns:** `true` if service was started, `false` if already running or not initialized

**Example:**
```kotlin
val started = service.start()
```

##### `stop`

Stops the service. This does not destroy the client, just marks the service as stopped.

```kotlin
suspend fun stop(): Boolean
```

**Returns:** `true` if service was stopped, `false` if not running

**Example:**
```kotlin
service.stop()
```

#### Service Status

##### `isInitialized`

Checks if the service is initialized.

```kotlin
suspend fun isInitialized(): Boolean
```

**Returns:** `true` if initialized, `false` otherwise

##### `isRunning`

Checks if the service is running.

```kotlin
suspend fun isRunning(): Boolean
```

**Returns:** `true` if running, `false` otherwise

#### Getting the Core Client

##### `getCoreClient`

Gets the core client instance. Service must be initialized before accessing the client.

```kotlin
suspend fun getCoreClient(): UplinkCoreClient?
```

**Returns:** `UplinkCoreClient` instance or `null` if not initialized

**Example:**
```kotlin
val coreClient = service.getCoreClient()
```

#### Token Management

##### `setAccessToken`

Updates the access token for the current client.

```kotlin
suspend fun setAccessToken(token: String?)
```

**Parameters:**
- `token`: New access token (or `null` to clear)

## HTTP Client

The `HttpClient` class provides methods for making HTTP requests to the backend API.

### Request Methods

#### `get`

Performs a GET request.

```kotlin
suspend inline fun <reified T> get(
    endpoint: String,
    queryParams: Map<String, String>? = null
): T
```

**Parameters:**
- `endpoint`: API endpoint path (e.g., `"/mobile/subscriber/register"`)
- `queryParams`: Optional query parameters

**Returns:** Deserialized response object of type `T`

**Throws:** `ApiException` if the request fails

#### `post`

Performs a POST request.

```kotlin
suspend inline fun <reified T, reified B> post(
    endpoint: String,
    body: B
): T
```

**Parameters:**
- `endpoint`: API endpoint path
- `body`: Request body object (will be serialized to JSON)

**Returns:** Deserialized response object of type `T`

**Throws:** `ApiException` if the request fails

#### `delete`

Performs a DELETE request.

```kotlin
suspend fun delete(endpoint: String)
```

**Parameters:**
- `endpoint`: API endpoint path

**Throws:** `ApiException` if the request fails

#### `getFromUrl`

Performs a GET request to an external URL (not using baseUrl).

```kotlin
suspend inline fun <reified T> getFromUrl(url: String): T
```

**Parameters:**
- `url`: Full URL to request

**Returns:** Deserialized response object of type `T`

**Throws:** `ApiException` if the request fails

### Authentication Headers

The HTTP client automatically adds the `Authorization: Bearer {token}` header to all requests when an access token is set.

### Error Handling

The HTTP client maps HTTP errors to `ApiException` types:
- `401 Unauthorized` → `ApiException.Unauthorized`
- `404 Not Found` → `ApiException.NotFound`
- `400 Bad Request` → `ApiException.BadRequest`
- `500/502/503 Server Error` → `ApiException.ServerError`
- Network errors → `ApiException.NetworkError`
- Other errors → `ApiException.Unknown`

## License Controller

The `LicenseController` class manages access to SDK modules and features based on license information.

### Setting a License

```kotlin
fun setLicense(licenseInfo: LicenseInfo)
```

**Parameters:**
- `licenseInfo`: License information containing allowed modules and features

**Example:**
```kotlin
val licenseInfo = LicenseInfo(
    licenseKey = "LICENSE-KEY-123",
    features = setOf("profile-installation", "profile-management"),
    modules = setOf("passpoint-profile-sdk", "network-sdk"),
    expirationDate = System.currentTimeMillis() / 1000 + (365 * 24 * 60 * 60), // 1 year from now
    isActive = true
)

licenseController.setLicense(licenseInfo)
```

### Checking Module Access

```kotlin
fun isModuleAllowed(moduleName: String): Boolean
```

**Parameters:**
- `moduleName`: Name of the module to check (e.g., `"passpoint-profile-sdk"`)

**Returns:** `true` if the module is allowed, `false` otherwise

**Example:**
```kotlin
if (licenseController.isModuleAllowed("passpoint-profile-sdk")) {
    // Use Passpoint Profile SDK
} else {
    // Handle access denied
}
```

### Checking Feature Access

```kotlin
fun isFeatureAllowed(featureName: String): Boolean
```

**Parameters:**
- `featureName`: Name of the feature to check (e.g., `"profile-installation"`)

**Returns:** `true` if the feature is allowed, `false` otherwise

**Example:**
```kotlin
if (licenseController.isFeatureAllowed("profile-installation")) {
    // Use profile installation feature
} else {
    // Handle access denied
}
```

### License Validation

```kotlin
fun isLicenseValid(): Boolean
```

**Returns:** `true` if license is active and not expired, `false` otherwise

**Example:**
```kotlin
if (licenseController.isLicenseValid()) {
    // License is valid
} else {
    // License is invalid or expired
}
```

### Getting License Information

```kotlin
fun getLicense(): LicenseInfo?
fun getAllowedModules(): Set<String>
fun getAllowedFeatures(): Set<String>
```

**Example:**
```kotlin
val license = licenseController.getLicense()
val allowedModules = licenseController.getAllowedModules()
val allowedFeatures = licenseController.getAllowedFeatures()
```

### Clearing License

```kotlin
fun clearLicense()
```

Clears the current license information.

## Data Models

### DeviceInfo

Device information structure for registration.

```kotlin
data class DeviceInfo(
    val deviceId: String,
    val deviceModel: String,
    val osVersion: String,
    val osType: String
)
```

### LicenseInfo

License information structure.

```kotlin
data class LicenseInfo(
    val licenseKey: String,
    val features: Set<String>,
    val modules: Set<String>,
    val expirationDate: Long? = null, // Unix timestamp, null for perpetual
    val isActive: Boolean = true
)
```

## Error Handling

The Core SDK uses a sealed class hierarchy for API exceptions:

```kotlin
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Unauthorized(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class NotFound(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class BadRequest(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class ServerError(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class NetworkError(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class Unknown(message: String, cause: Throwable? = null) : ApiException(message, cause)
}
```

**Example Error Handling:**
```kotlin
try {
    val coreClient = UplinkCoreClient.create(...)
} catch (e: ApiException.Unauthorized) {
    Log.e("App", "Authentication failed: ${e.message}")
} catch (e: ApiException.NetworkError) {
    Log.e("App", "Network error: ${e.message}")
} catch (e: ApiException) {
    Log.e("App", "API error: ${e.message}")
}
```

## Best Practices

1. **Initialize Early**: Initialize the Core SDK in your `Application.onCreate()` method
2. **Use Service Pattern**: Use `UplinkService` for singleton access to the client
3. **Handle Errors**: Always wrap SDK calls in try-catch blocks
4. **Check Authentication**: Verify `isAuthenticated()` before making authenticated requests
5. **Refresh Tokens**: The SDK automatically refreshes tokens, but you can manually call `refreshTokenIfNeeded()` if needed
6. **License Validation**: Always check license validity before using SDK modules

## Related Documentation

- [Passpoint SDK API Reference](../android/passpoint-sdk.md)
- [API Endpoints](../api-endpoints.md)
- [Getting Started Guide](../android/getting-started.md)
