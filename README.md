## Overview

The Uplink Core SDK provides foundational services for authentication, HTTP communication, logging, task scheduling, and license management. It serves as the base layer for all Uplink SDK modules.

## UplinkCoreClient

The main client class for the Core SDK. Provides access to authentication, HTTP client, and utility services.

### Initialization

#### `init`

Creates a new Core SDK client instance without authentication.

```swift
public init(baseURL: String? = nil, accessToken: String? = nil)
```

**Parameters:**
- `baseURL`: Optional base URL for API communication
- `accessToken`: Optional access token for authenticated requests

**Example:**
```swift
let coreClient = UplinkCoreClient(
    baseURL: "https://api-gateway.develop.uplink.xyz/v2",
    accessToken: nil
)
```

#### `create`

Creates a new Core SDK client instance with authentication.

```swift
public static func create(
    baseURL: String,
    appId: String,
    appSecret: String,
    deviceInfo: DeviceInfo
) async -> UplinkCoreClient
```

**Parameters:**
- `baseURL`: Base URL for API communication (e.g., `"https://api-gateway.develop.uplink.xyz/v2"`)
- `appId`: Developer application ID
- `appSecret`: Developer application secret
- `deviceInfo`: Device information for registration

**Returns:** Authenticated `UplinkCoreClient` instance

**Throws:** Error if authentication fails

**Example:**
```swift
let deviceInfo = DeviceInfo(
    deviceId: getDeviceId(),
    deviceModel: UIDevice.current.model,
    osVersion: UIDevice.current.systemVersion,
    osType: "iOS"
)

let coreClient = await UplinkCoreClient.create(
    baseURL: "https://api-gateway.develop.uplink.xyz/v2",
    appId: "your-app-id",
    appSecret: "your-app-secret",
    deviceInfo: deviceInfo
)
```

#### `fromService`

Gets the core client from the service singleton. Initializes and starts the service if not already done.

```swift
public static func fromService(
    baseURL: String? = nil,
    accessToken: String? = nil
) -> UplinkCoreClient?
```

**Parameters:**
- `baseURL`: Optional base URL for API communication
- `accessToken`: Optional access token for authenticated requests

**Returns:** `UplinkCoreClient` instance from the service, or `nil` if service is not initialized

**Note:** The service must be initialized separately using `UplinkServiceManager.shared.initialize()`

**Example:**
```swift
let coreClient = UplinkCoreClient.fromService(
    baseURL: "https://api-gateway.develop.uplink.xyz/v2",
    accessToken: nil
)
```

### Authentication

#### `refreshTokenIfNeeded`

Refreshes the authentication token if it has expired. Re-authenticates using stored credentials.

```swift
public func refreshTokenIfNeeded() async
```

**Example:**
```swift
await coreClient.refreshTokenIfNeeded()
```

### Subscriber Management

#### `createSubscriber`

Creates a subscriber manually. Uses organizationId from the stored JWT token.

```swift
public func createSubscriber() async throws -> SubscriberResponse
```

**Returns:** `SubscriberResponse` containing subscriber ID

**Throws:** Error if subscriber creation fails

**Example:**
```swift
do {
    let subscriber = try await coreClient.createSubscriber()
    print("Subscriber created: \(subscriber.subscriberId)")
} catch {
    print("Failed to create subscriber: \(error.localizedDescription)")
}
```

### Token Management

#### `setAccessToken`

Updates the access token for API requests.

```swift
public func setAccessToken(_ token: String?)
```

**Parameters:**
- `token`: New access token (or `nil` to clear)

**Example:**
```swift
coreClient.setAccessToken("new-jwt-token")
```

#### `getIsAuthenticated`

Checks if the client is authenticated.

```swift
public func getIsAuthenticated() -> Bool
```

**Returns:** `true` if authenticated, `false` otherwise

**Example:**
```swift
if coreClient.getIsAuthenticated() {
    // Proceed with authenticated operations
}
```

### Utility Accessors

#### `getHttpClient`

Gets the HTTP client for API communication.

```swift
public func getHttpClient() -> HttpClient?
```

**Returns:** `HttpClient` instance or `nil` if not initialized

**Example:**
```swift
if let httpClient = coreClient.getHttpClient() {
    // Use HTTP client
}
```

#### `getTaskScheduler`

Gets the periodic task scheduler.

```swift
public func getTaskScheduler() -> PeriodicTaskScheduler
```

**Returns:** `PeriodicTaskScheduler` instance

**Example:**
```swift
let scheduler = coreClient.getTaskScheduler()
```

#### `getLogger`

Gets the logger utility.

```swift
public func getLogger() -> Logger
```

**Returns:** `Logger` instance

**Example:**
```swift
let logger = coreClient.getLogger()
logger.info("Application started")
```

#### `getLicenseController`

Gets the license controller for managing module and feature access.

```swift
public func getLicenseController() -> LicenseController
```

**Returns:** `LicenseController` instance

**Example:**
```swift
let licenseController = coreClient.getLicenseController()
if licenseController.isModuleAllowed("PasspointProfileSDK") {
    // Use Passpoint SDK
}
```

## Service Architecture

### UplinkServiceManager

Singleton service manager for the Core SDK. Provides centralized access to the SDK client with lifecycle management.

#### Getting the Service Instance

```swift
let service = UplinkServiceManager.shared
```

#### Service Lifecycle

##### `initialize`

Initializes the service with developer credentials. Authenticates with backend using appId/appSecret and receives JWT token.

```swift
@discardableResult
public func initialize(
    baseURL: String = "https://api-gateway.develop.uplink.xyz/v2",
    appId: String,
    appKey: String,
    deviceInfo: DeviceInfo
) async -> Bool
```

**Parameters:**
- `baseURL`: Base URL for API communication (defaults to development server)
- `appId`: Developer app ID
- `appKey`: Developer app secret
- `deviceInfo`: Device information

**Returns:** `true` if initialization was successful, `false` if already initialized

**Example:**
```swift
let initialized = await service.initialize(
    baseURL: "https://api-gateway.develop.uplink.xyz/v2",
    appId: "your-app-id",
    appKey: "your-app-secret",
    deviceInfo: deviceInfo
)
```

##### `start`

Starts the service. Service must be initialized before starting.

```swift
@discardableResult
public func start() -> Bool
```

**Returns:** `true` if service was started, `false` if already running or not initialized

**Example:**
```swift
let started = service.start()
```

##### `stop`

Stops the service. This does not destroy the client, just marks the service as stopped.

```swift
@discardableResult
public func stop() -> Bool
```

**Returns:** `true` if service was stopped, `false` if not running

**Example:**
```swift
service.stop()
```

#### Service Status

##### `getIsInitialized`

Checks if the service is initialized.

```swift
public func getIsInitialized() -> Bool
```

**Returns:** `true` if initialized, `false` otherwise

##### `getIsRunning`

Checks if the service is running.

```swift
public func getIsRunning() -> Bool
```

**Returns:** `true` if running, `false` otherwise

#### Getting the Core Client

##### `getCoreClient`

Gets the core client instance. Service must be initialized before accessing the client.

```swift
public func getCoreClient() -> UplinkCoreClient?
```

**Returns:** `UplinkCoreClient` instance or `nil` if not initialized

**Example:**
```swift
let coreClient = service.getCoreClient()
```

#### Token Management

##### `setAccessToken`

Updates the access token for the current client.

```swift
public func setAccessToken(_ token: String?)
```

**Parameters:**
- `token`: New access token (or `nil` to clear)

## HTTP Client

The `HttpClient` class provides methods for making HTTP requests to the backend API.

### Request Methods

#### `get`

Performs a GET request.

```swift
public func get<T: Decodable>(
    endpoint: String,
    parameters: [String: Any]? = nil
) async throws -> T
```

**Parameters:**
- `endpoint`: API endpoint path (e.g., `"/mobile/subscriber/register"`)
- `parameters`: Optional query parameters

**Returns:** Deserialized response object of type `T`

**Throws:** Error if the request fails

**Example:**
```swift
let response: SubscriberResponse = try await httpClient.get(
    endpoint: "/subscriber",
    parameters: nil
)
```

#### `post`

Performs a POST request.

```swift
public func post<T: Decodable, B: Encodable>(
    endpoint: String,
    body: B
) async throws -> T
```

**Parameters:**
- `endpoint`: API endpoint path
- `body`: Request body object (will be serialized to JSON)

**Returns:** Deserialized response object of type `T`

**Throws:** Error if the request fails

**Example:**
```swift
let request = AppAuthRequest(appId: "app-id", appSecret: "app-secret")
let response: AppAuthResponse = try await httpClient.post(
    endpoint: "/auth/app",
    body: request
)
```

#### `delete`

Performs a DELETE request.

```swift
public func delete(endpoint: String) async throws
```

**Parameters:**
- `endpoint`: API endpoint path

**Throws:** Error if the request fails

#### `getFromUrl`

Performs a GET request to an external URL (not using baseURL).

```swift
public func getFromUrl<T: Decodable>(url: String) async throws -> T
```

**Parameters:**
- `url`: Full URL to request

**Returns:** Deserialized response object of type `T`

**Throws:** Error if the request fails

**Example:**
```swift
let response: IOSPasspointProfileResponse = try await httpClient.getFromUrl(
    url: "https://radiustest2.uplink.xyz/passpoint/sdk/ios"
)
```

### Authentication Headers

The HTTP client automatically adds the `Authorization: Bearer {token}` header to all requests when an access token is set.

### Error Handling

HTTP errors are thrown as `Error` types. The SDK uses Alamofire for HTTP requests, which provides detailed error information.

**Example Error Handling:**
```swift
do {
    let response = try await httpClient.get(...)
} catch {
    if let afError = error as? AFError {
        switch afError {
        case .responseValidationFailed:
            print("Response validation failed")
        case .responseSerializationFailed:
            print("Response serialization failed")
        default:
            print("HTTP error: \(afError.localizedDescription)")
        }
    } else {
        print("Error: \(error.localizedDescription)")
    }
}
```

## License Controller

The `LicenseController` class manages access to SDK modules and features based on license information.

### Setting a License

```swift
public func setLicense(_ licenseInfo: LicenseInfo)
```

**Parameters:**
- `licenseInfo`: License information containing allowed modules and features

**Example:**
```swift
let expirationDate = Calendar.current.date(byAdding: .year, value: 1, to: Date())
let licenseInfo = LicenseInfo(
    licenseKey: "LICENSE-KEY-123",
    features: ["profile-installation", "profile-management"],
    modules: ["PasspointProfileSDK", "NetworkSDK"],
    expirationDate: expirationDate,
    isActive: true
)

licenseController.setLicense(licenseInfo)
```

### Checking Module Access

```swift
public func isModuleAllowed(_ moduleName: String) -> Bool
```

**Parameters:**
- `moduleName`: Name of the module to check (e.g., `"PasspointProfileSDK"`)

**Returns:** `true` if the module is allowed, `false` otherwise

**Example:**
```swift
if licenseController.isModuleAllowed("PasspointProfileSDK") {
    // Use Passpoint Profile SDK
} else {
    // Handle access denied
}
```

### Checking Feature Access

```swift
public func isFeatureAllowed(_ featureName: String) -> Bool
```

**Parameters:**
- `featureName`: Name of the feature to check (e.g., `"profile-installation"`)

**Returns:** `true` if the feature is allowed, `false` otherwise

**Example:**
```swift
if licenseController.isFeatureAllowed("profile-installation") {
    // Use profile installation feature
} else {
    // Handle access denied
}
```

### License Validation

```swift
public func isLicenseValid() -> Bool
```

**Returns:** `true` if license is active and not expired, `false` otherwise

**Example:**
```swift
if licenseController.isLicenseValid() {
    // License is valid
} else {
    // License is invalid or expired
}
```

### Getting License Information

```swift
public func getLicense() -> LicenseInfo?
public func getAllowedModules() -> Set<String>
public func getAllowedFeatures() -> Set<String>
```

**Example:**
```swift
if let license = licenseController.getLicense() {
    print("License Key: \(license.licenseKey)")
    print("Allowed Modules: \(license.modules)")
    print("Allowed Features: \(license.features)")
}

let allowedModules = licenseController.getAllowedModules()
let allowedFeatures = licenseController.getAllowedFeatures()
```

### Clearing License

```swift
public func clearLicense()
```

Clears the current license information.

## Data Models

### DeviceInfo

Device information structure for registration.

```swift
public struct DeviceInfo {
    public let deviceId: String
    public let deviceModel: String
    public let osVersion: String
    public let osType: String
}
```

### LicenseInfo

License information structure.

```swift
public struct LicenseInfo {
    public let licenseKey: String
    public let features: Set<String>
    public let modules: Set<String>
    public let expirationDate: Date? // nil for perpetual licenses
    public let isActive: Bool
}
```

## Error Handling

The Core SDK uses Swift's standard error handling with `Error` protocol. HTTP errors are typically `AFError` from Alamofire.

**Example Error Handling:**
```swift
do {
    let coreClient = await UplinkCoreClient.create(...)
} catch {
    print("Error: \(error.localizedDescription)")
    // Handle error appropriately
}
```

## Best Practices

1. **Initialize Early**: Initialize the Core SDK in your `AppDelegate.application(_:didFinishLaunchingWithOptions:)` method
2. **Use Service Pattern**: Use `UplinkServiceManager.shared` for singleton access to the client
3. **Handle Errors**: Always wrap SDK calls in do-catch blocks
4. **Check Authentication**: Verify `getIsAuthenticated()` before making authenticated requests
5. **Refresh Tokens**: The SDK automatically refreshes tokens, but you can manually call `refreshTokenIfNeeded()` if needed
6. **License Validation**: Always check license validity before using SDK modules
7. **Use async/await**: The SDK uses modern Swift concurrency, so use async/await for all SDK calls

## Related Documentation

- [Passpoint SDK API Reference](../ios/passpoint-sdk.md)
- [API Endpoints](../api-endpoints.md)
- [Getting Started Guide](../ios/getting-started.md)

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
