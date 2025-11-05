# Collective Network SDK - Native Implementation (Android Kotlin & iOS Swift)

## 🎯 Overview

This guide details the implementation of the Uplink collective network SDK in **Android (Kotlin)** and **iOS (Swift)**, focusing on:
- WiFi network detection and verification
- Router registration in collective network
- Multiple router management
- Collective network statistics
- Coverage verification

---

## 📱 Project Structure

```
uplink-network-sdk/
├── android/
│   └── uplink-network-sdk/
│       ├── build.gradle
│       └── src/main/java/com/uplink/network/
│           ├── UplinkNetworkClient.kt
│           ├── network/
│           │   ├── NetworkDetector.kt
│           │   └── WifiInfo.kt
│           ├── router/
│           │   ├── RouterService.kt
│           │   ├── RouterRegistry.kt
│           │   └── models/
│           │       ├── Router.kt
│           │       └── RouterRegistrationStatus.kt
│           ├── collective/
│           │   └── CollectiveNetworkService.kt
│           └── api/
│               ├── HttpClient.kt
│               └── ApiEndpoints.kt
├── ios/
│   └── UplinkNetworkSDK/
│       ├── UplinkNetworkSDK.podspec
│       ├── Sources/
│       │   ├── UplinkNetworkClient.swift
│       │   ├── Network/
│       │   │   ├── NetworkDetector.swift
│       │   │   └── WifiInfo.swift
│       │   ├── Router/
│       │   │   ├── RouterService.swift
│       │   │   ├── RouterRegistry.swift
│       │   │   └── Models/
│       │   │       ├── Router.swift
│       │   │       └── RouterRegistrationStatus.swift
│       │   ├── Collective/
│       │   │   └── CollectiveNetworkService.swift
│       │   └── API/
│       │       ├── HttpClient.swift
│       │       └── ApiEndpoints.swift
│       └── Tests/
└── README.md
```

---

## 🤖 Android (Kotlin) - Implementation

### 1. build.gradle (Module)

```kotlin
plugins {
    id 'com.android.library'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-parcelize'
    id 'kotlinx-serialization'
}

android {
    namespace 'com.uplink.network'
    compileSdk 34

    defaultConfig {
        minSdk 24
        targetSdk 34

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // Serialization
    implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0'
    
    // WiFi & Network Info
    implementation 'com.github.pwittchen:NetworkEvents:3.0.2'
    
    // Location
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
}
```

### 2. Main Client (`UplinkNetworkClient.kt`)

```kotlin
package com.uplink.network

import com.uplink.network.collective.CollectiveNetworkService
import com.uplink.network.network.NetworkDetector
import com.uplink.network.router.RouterService

/**
 * Main client for interacting with the Uplink collective network
 */
class UplinkNetworkClient private constructor(
    private val apiHost: String,
    private var accessToken: String? = null
) {
    private val networkDetector: NetworkDetector = NetworkDetector()
    private val routerService: RouterService = RouterService(apiHost, accessToken)
    private val collectiveService: CollectiveNetworkService = 
        CollectiveNetworkService(apiHost, accessToken)

    companion object {
        /**
         * Creates a new client instance
         */
        fun create(
            apiHost: String,
            accessToken: String? = null
        ): UplinkNetworkClient {
            return UplinkNetworkClient(apiHost, accessToken)
        }
    }

    /**
     * WiFi network detector
     */
    fun network(): NetworkDetector = networkDetector

    /**
     * Router management service
     */
    fun routers(): RouterService = routerService

    /**
     * Collective network service
     */
    fun collective(): CollectiveNetworkService = collectiveService

    /**
     * Update authentication token
     */
    fun setAccessToken(token: String?) {
        accessToken = token
        routerService.setAccessToken(token)
        collectiveService.setAccessToken(token)
    }
}
```

### 3. Network Detector (`NetworkDetector.kt`)

```kotlin
package com.uplink.network.network

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress

/**
 * Detects and monitors available WiFi networks
 */
class NetworkDetector(private val context: Context) {
    
    private val wifiManager: WifiManager = 
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _wifiState = MutableStateFlow<WifiInfo?>(null)
    val wifiState: Flow<WifiInfo?> = _wifiState.asStateFlow()

    /**
     * Get current WiFi network information
     */
    suspend fun getCurrentWifi(): WifiInfo? {
        return try {
            val wifiInfo = wifiManager.connectionInfo
            
            if (wifiInfo.ssid == "<unknown ssid>") {
                return null
            }
            
            val ssid = wifiInfo.ssid.removeSurrounding("\"")
            val bssid = wifiInfo.bssid
            val hasInternet = checkInternetConnection()
            val signalStrength = wifiInfo.rssi
            
            WifiInfo(
                ssid = ssid,
                bssid = bssid,
                hasInternet = hasInternet,
                signalStrength = signalStrength,
                isConnected = true
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if connected to a WiFi network
     */
    fun isConnectedToWifi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    /**
     * Check if has internet connection
     */
    suspend fun hasInternetConnection(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            val hasInternet = capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) == true &&
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )
            
            // Double check with ping
            if (hasInternet) {
                val inetAddress = InetAddress.getByName("8.8.8.8")
                inetAddress.isReachable(3000)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check internet connection (internal method)
     */
    private fun checkInternetConnection(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Start monitoring network changes
     */
    fun startMonitoring() {
        // Implement network change listener
        // Use NetworkCallback or BroadcastReceiver
    }

    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        // Stop listeners
    }
}

/**
 * WiFi network information
 */
data class WifiInfo(
    val ssid: String?,
    val bssid: String?,
    val hasInternet: Boolean,
    val signalStrength: Int,
    val isConnected: Boolean
)
```

### 4. Router Service (`RouterService.kt`)

```kotlin
package com.uplink.network.router

import com.uplink.network.api.HttpClient
import com.uplink.network.router.models.Router
import com.uplink.network.router.models.RouterRegistrationStatus
import com.uplink.network.router.models.RegisterRouterRequest
import com.uplink.network.router.models.CoverageResult
import com.uplink.network.router.models.MyRoutersStats

/**
 * Manages routers in the collective network
 */
class RouterService(
    private val apiHost: String,
    private var accessToken: String? = null
) {
    private val httpClient: HttpClient = HttpClient(apiHost, accessToken)

    fun setAccessToken(token: String?) {
        accessToken = token
        httpClient.setAccessToken(token)
    }

    /**
     * Verify if a network is registered in the collective network
     */
    suspend fun verifyRegistration(
        ssid: String,
        bssid: String
    ): Result<RouterRegistrationStatus> {
        return try {
            val response = httpClient.get<RouterRegistrationStatusResponse>(
                endpoint = "/v1/mobile/community/router/verify",
                queryParams = mapOf(
                    "ssid" to ssid,
                    "bssid" to bssid
                )
            )
            
            Result.success(
                RouterRegistrationStatus(
                    isRegistered = response.registered,
                    registeredByMe = response.registeredByMe,
                    router = response.router?.toDomain()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register new router in the collective network
     */
    suspend fun registerRouter(
        request: RegisterRouterRequest
    ): Result<Router> {
        return try {
            val response = httpClient.post<RouterResponse>(
                endpoint = "/v1/mobile/community/router/register",
                body = request.toDto()
            )
            
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * List all routers registered by the user
     */
    suspend fun getMyRouters(
        page: Int = 1,
        pageSize: Int = 100
    ): Result<List<Router>> {
        return try {
            val response = httpClient.get<RoutersResponse>(
                endpoint = "/v1/mobile/community/router",
                queryParams = mapOf(
                    "page" to page.toString(),
                    "pageSize" to pageSize.toString(),
                    "filter[status][0]" to "registered",
                    "filter[status][1]" to "validated"
                )
            )
            
            Result.success(response.routers.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get details of a specific router
     */
    suspend fun getRouterDetails(routerId: String): Result<Router> {
        return try {
            val response = httpClient.get<RouterResponse>(
                endpoint = "/v1/mobile/community/router/$routerId"
            )
            
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify coverage at a location
     */
    suspend fun verifyCoverage(
        latitude: Double,
        longitude: Double
    ): Result<CoverageResult> {
        return try {
            // Implement coverage verification
            // Can be an API call or local validation
            Result.success(CoverageResult(hasCoverage = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete router from the collective network
     */
    suspend fun deleteRouter(routerId: String): Result<Unit> {
        return try {
            httpClient.delete(
                endpoint = "/v1/mobile/community/router/$routerId"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get statistics of user's routers
     */
    suspend fun getMyRoutersStats(): Result<MyRoutersStats> {
        return try {
            val response = httpClient.get<NetworkStatsResponse>(
                endpoint = "/v1/mobile/account/network-stats"
            )
            
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 5. Data Models (`Router.kt`)

```kotlin
package com.uplink.network.router.models

import kotlinx.serialization.Serializable

/**
 * Router model
 */
@Serializable
data class Router(
    val id: String? = null,
    val name: String? = null,
    val ssid: String? = null,
    val macAddress: String? = null,
    val manufacturer: String? = null,
    val model: String? = null,
    val venueType: VenueType? = null,
    val networkType: NetworkType? = null,
    val isIndoor: Boolean? = null,
    val location: Location? = null,
    val status: RouterStatus? = null,
    val signalStrength: Int? = null,
    val rewards: String? = null,
    val mapCells: Int? = null,
    val sevenDaysPoints: Double? = null,
    val lastCheck: String? = null,
    val isConnected: Boolean? = null
)

@Serializable
enum class VenueType {
    RESIDENTIAL,
    COMMERCIAL,
    PUBLIC,
    OTHER
}

@Serializable
enum class NetworkType {
    WIFI_2_4GHZ,
    WIFI_5GHZ,
    WIFI_6GHZ
}

@Serializable
enum class RouterStatus {
    REGISTERED,
    VALIDATED,
    PENDING
}

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * Registration status in collective network
 */
data class RouterRegistrationStatus(
    val isRegistered: Boolean,
    val registeredByMe: Boolean,
    val router: Router? = null
)

/**
 * Data for router registration
 */
@Serializable
data class RegisterRouterRequest(
    val ssid: String,
    val bssid: String,
    val location: Location,
    val venueType: VenueType? = null,
    val isIndoor: Boolean = true,
    val name: String? = null,
    val rewards: String? = null
)

/**
 * Coverage verification result
 */
data class CoverageResult(
    val hasCoverage: Boolean,
    val message: String? = null,
    val nearbyRouters: List<Router>? = null
)

/**
 * User's router statistics
 */
data class MyRoutersStats(
    val totalRouters: Int,
    val activeRouters: Int,
    val totalMapCells: Int,
    val totalPoints: Double,
    val sevenDaysPoints: Double
)
```

### 6. HTTP Client (`HttpClient.kt`)

```kotlin
package com.uplink.network.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * HTTP client for API communication
 */
class HttpClient(
    private val baseUrl: String,
    private var accessToken: String? = null
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
            
            requestBuilder.addHeader("Content-Type", "application/json")
            
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    suspend fun <T> get(
        endpoint: String,
        queryParams: Map<String, String>? = null
    ): T where T : Any {
        val urlBuilder = HttpUrl.parse("$baseUrl$endpoint")?.newBuilder()
            ?: throw IllegalArgumentException("Invalid URL")
        
        queryParams?.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()

        return executeRequest(request)
    }

    suspend fun <T, B> post(
        endpoint: String,
        body: B
    ): T where T : Any {
        val jsonBody = json.encodeToString(
            body!!::class.serializer() as kotlinx.serialization.KSerializer<Any>,
            body
        )
        
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .post(requestBody)
            .build()

        return executeRequest(request)
    }

    suspend fun delete(endpoint: String) {
        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> executeRequest(request: Request): T {
        return suspendCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            // Deserialize response
                            val result = json.decodeFromString<T>(body)
                            continuation.resumeWith(Result.success(result))
                        } else {
                            continuation.resumeWith(
                                Result.failure(
                                    IOException("HTTP ${response.code}: $body")
                                )
                            )
                        }
                    } catch (e: Exception) {
                        continuation.resumeWith(Result.failure(e))
                    }
                }
            })
        }
    }
}
```

---

## 🍎 iOS (Swift) - Implementation

### 1. Podspec (`UplinkNetworkSDK.podspec`)

```ruby
Pod::Spec.new do |spec|
  spec.name             = "UplinkNetworkSDK"
  spec.version          = "1.0.0"
  spec.summary          = "SDK for Uplink Collective Network Management"
  spec.description      = "SDK for managing routers and collective networks in Uplink"
  
  spec.homepage         = "https://uplink.xyz"
  spec.license          = { :type => "MIT" }
  spec.author           = { "Uplink" => "dev@uplink.xyz" }
  
  spec.platform         = :ios, "13.0"
  spec.swift_version    = "5.9"
  
  spec.source           = { :git => "...", :tag => "#{spec.version}" }
  spec.source_files     = "Sources/**/*.swift"
  
  spec.dependency 'Alamofire', '~> 5.8'
  spec.dependency 'SystemConfiguration'
  spec.dependency 'CoreLocation'
end
```

### 2. Main Client (`UplinkNetworkClient.swift`)

```swift
import Foundation

/**
 * Main client for interacting with the Uplink collective network
 */
public class UplinkNetworkClient {
    private let apiHost: String
    private var accessToken: String?
    
    private let networkDetector: NetworkDetector
    private let routerService: RouterService
    private let collectiveService: CollectiveNetworkService
    
    /**
     * Creates a new client instance
     */
    public init(apiHost: String, accessToken: String? = nil) {
        self.apiHost = apiHost
        self.accessToken = accessToken
        
        self.networkDetector = NetworkDetector()
        self.routerService = RouterService(apiHost: apiHost, accessToken: accessToken)
        self.collectiveService = CollectiveNetworkService(apiHost: apiHost, accessToken: accessToken)
    }
    
    /**
     * WiFi network detector
     */
    public func network() -> NetworkDetector {
        return networkDetector
    }
    
    /**
     * Router management service
     */
    public func routers() -> RouterService {
        return routerService
    }
    
    /**
     * Collective network service
     */
    public func collective() -> CollectiveNetworkService {
        return collectiveService
    }
    
    /**
     * Update authentication token
     */
    public func setAccessToken(_ token: String?) {
        self.accessToken = token
        routerService.setAccessToken(token)
        collectiveService.setAccessToken(token)
    }
}
```

### 3. Network Detector (`NetworkDetector.swift`)

```swift
import Foundation
import SystemConfiguration
import Network

/**
 * Detects and monitors available WiFi networks
 */
public class NetworkDetector {
    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "NetworkMonitor")
    
    /**
     * Get current WiFi network information
     */
    public func getCurrentWifi() async throws -> WifiInfo? {
        // iOS does not allow direct access to SSID due to privacy concerns
        // Need to use a different approach or request permissions
        
        // Check connectivity
        let isConnected = await checkConnectivity()
        let hasInternet = await checkInternetConnection()
        
        // In production, you will need to use:
        // 1. NetworkExtension framework (requires special configuration)
        // 2. Request user permissions
        // 3. Use system-specific APIs
        
        return WifiInfo(
            ssid: nil, // Requires special permissions
            bssid: nil,
            hasInternet: hasInternet,
            signalStrength: nil,
            isConnected: isConnected
        )
    }
    
    /**
     * Check if connected to a WiFi network
     */
    public func isConnectedToWifi() async -> Bool {
        return await checkConnectivity()
    }
    
    /**
     * Check if has internet connection
     */
    public func hasInternetConnection() async -> Bool {
        return await checkInternetConnection()
    }
    
    /**
     * Start monitoring network changes
     */
    public func startMonitoring(handler: @escaping (WifiInfo?) -> Void) {
        monitor.pathUpdateHandler = { path in
            let wifiInfo = WifiInfo(
                ssid: nil,
                bssid: nil,
                hasInternet: path.status == .satisfied,
                signalStrength: nil,
                isConnected: path.usesInterfaceType(.wifi)
            )
            handler(wifiInfo)
        }
        monitor.start(queue: queue)
    }
    
    /**
     * Stop monitoring
     */
    public func stopMonitoring() {
        monitor.cancel()
    }
    
    private func checkConnectivity() async -> Bool {
        return await withCheckedContinuation { continuation in
            let monitor = NWPathMonitor()
            monitor.pathUpdateHandler = { path in
                continuation.resume(returning: path.usesInterfaceType(.wifi))
                monitor.cancel()
            }
            monitor.start(queue: queue)
        }
    }
    
    private func checkInternetConnection() async -> Bool {
        return await withCheckedContinuation { continuation in
            let monitor = NWPathMonitor()
            monitor.pathUpdateHandler = { path in
                continuation.resume(returning: path.status == .satisfied)
                monitor.cancel()
            }
            monitor.start(queue: queue)
        }
    }
}

/**
 * WiFi network information
 */
public struct WifiInfo {
    public let ssid: String?
    public let bssid: String?
    public let hasInternet: Bool
    public let signalStrength: Int?
    public let isConnected: Bool
    
    public init(ssid: String?, bssid: String?, hasInternet: Bool, signalStrength: Int?, isConnected: Bool) {
        self.ssid = ssid
        self.bssid = bssid
        self.hasInternet = hasInternet
        self.signalStrength = signalStrength
        self.isConnected = isConnected
    }
}
```

### 4. Router Service (`RouterService.swift`)

```swift
import Foundation
import Alamofire

/**
 * Manages routers in the collective network
 */
public class RouterService {
    private let apiHost: String
    private var accessToken: String?
    private let httpClient: HttpClient
    
    public init(apiHost: String, accessToken: String? = nil) {
        self.apiHost = apiHost
        self.accessToken = accessToken
        self.httpClient = HttpClient(baseURL: apiHost, accessToken: accessToken)
    }
    
    public func setAccessToken(_ token: String?) {
        self.accessToken = token
        httpClient.setAccessToken(token)
    }
    
    /**
     * Verify if a network is registered in the collective network
     */
    public func verifyRegistration(ssid: String, bssid: String) async throws -> RouterRegistrationStatus {
        let response = try await httpClient.get(
            endpoint: "/v1/mobile/community/router/verify",
            parameters: ["ssid": ssid, "bssid": bssid]
        ) as RouterRegistrationStatusResponse
        
        return RouterRegistrationStatus(
            isRegistered: response.registered,
            registeredByMe: response.registeredByMe,
            router: response.router?.toDomain()
        )
    }
    
    /**
     * Register new router in the collective network
     */
    public func registerRouter(_ request: RegisterRouterRequest) async throws -> Router {
        let response = try await httpClient.post(
            endpoint: "/v1/mobile/community/router/register",
            body: request
        ) as RouterResponse
        
        return response.toDomain()
    }
    
    /**
     * List all routers registered by the user
     */
    public func getMyRouters(page: Int = 1, pageSize: Int = 100) async throws -> [Router] {
        let parameters: [String: Any] = [
            "page": page,
            "pageSize": pageSize,
            "filter[status][0]": "registered",
            "filter[status][1]": "validated"
        ]
        
        let response = try await httpClient.get(
            endpoint: "/v1/mobile/community/router",
            parameters: parameters
        ) as RoutersResponse
        
        return response.routers.map { $0.toDomain() }
    }
    
    /**
     * Get details of a specific router
     */
    public func getRouterDetails(routerId: String) async throws -> Router {
        let response = try await httpClient.get(
            endpoint: "/v1/mobile/community/router/\(routerId)"
        ) as RouterResponse
        
        return response.toDomain()
    }
    
    /**
     * Verify coverage at a location
     */
    public func verifyCoverage(latitude: Double, longitude: Double) async throws -> CoverageResult {
        // Implement coverage verification
        return CoverageResult(hasCoverage: true)
    }
    
    /**
     * Delete router from the collective network
     */
    public func deleteRouter(routerId: String) async throws {
        try await httpClient.delete(endpoint: "/v1/mobile/community/router/\(routerId)")
    }
    
    /**
     * Get statistics of user's routers
     */
    public func getMyRoutersStats() async throws -> MyRoutersStats {
        let response = try await httpClient.get(
            endpoint: "/v1/mobile/account/network-stats"
        ) as NetworkStatsResponse
        
        return response.toDomain()
    }
}
```

### 5. Data Models (`Router.swift`)

```swift
import Foundation

/**
 * Router model
 */
public struct Router: Codable {
    public let id: String?
    public let name: String?
    public let ssid: String?
    public let macAddress: String?
    public let manufacturer: String?
    public let model: String?
    public let venueType: VenueType?
    public let networkType: NetworkType?
    public let isIndoor: Bool?
    public let location: Location?
    public let status: RouterStatus?
    public let signalStrength: Int?
    public let rewards: String?
    public let mapCells: Int?
    public let sevenDaysPoints: Double?
    public let lastCheck: String?
    public let isConnected: Bool?
}

public enum VenueType: String, Codable {
    case residential
    case commercial
    case `public`
    case other
}

public enum NetworkType: String, Codable {
    case wifi2_4ghz = "WIFI_2_4GHZ"
    case wifi5ghz = "WIFI_5GHZ"
    case wifi6ghz = "WIFI_6GHZ"
}

public enum RouterStatus: String, Codable {
    case registered
    case validated
    case pending
}

public struct Location: Codable {
    public let latitude: Double
    public let longitude: Double
}

/**
 * Registration status in collective network
 */
public struct RouterRegistrationStatus {
    public let isRegistered: Bool
    public let registeredByMe: Bool
    public let router: Router?
}

/**
 * Data for router registration
 */
public struct RegisterRouterRequest: Codable {
    public let ssid: String
    public let bssid: String
    public let location: Location
    public let venueType: VenueType?
    public let isIndoor: Bool
    public let name: String?
    public let rewards: String?
}

/**
 * Coverage verification result
 */
public struct CoverageResult {
    public let hasCoverage: Bool
    public let message: String?
    public let nearbyRouters: [Router]?
}

/**
 * User's router statistics
 */
public struct MyRoutersStats {
    public let totalRouters: Int
    public let activeRouters: Int
    public let totalMapCells: Int
    public let totalPoints: Double
    public let sevenDaysPoints: Double
}
```

### 6. HTTP Client (`HttpClient.swift`)

```swift
import Foundation
import Alamofire

/**
 * HTTP client for API communication
 */
class HttpClient {
    private let baseURL: String
    private var accessToken: String?
    private let session: Session
    
    init(baseURL: String, accessToken: String? = nil) {
        self.baseURL = baseURL
        self.accessToken = accessToken
        
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        
        self.session = Session(configuration: configuration)
    }
    
    func setAccessToken(_ token: String?) {
        self.accessToken = token
    }
    
    func get<T: Decodable>(
        endpoint: String,
        parameters: [String: Any]? = nil
    ) async throws -> T {
        return try await withCheckedThrowingContinuation { continuation in
            var headers: HTTPHeaders = [
                "Content-Type": "application/json"
            ]
            
            if let token = accessToken {
                headers["Authorization"] = "Bearer \(token)"
            }
            
            session.request(
                "\(baseURL)\(endpoint)",
                method: .get,
                parameters: parameters,
                encoding: URLEncoding.queryString,
                headers: headers
            )
            .validate()
            .responseDecodable(of: T.self) { response in
                switch response.result {
                case .success(let value):
                    continuation.resume(returning: value)
                case .failure(let error):
                    continuation.resume(throwing: error)
                }
            }
        }
    }
    
    func post<T: Decodable, B: Encodable>(
        endpoint: String,
        body: B
    ) async throws -> T {
        return try await withCheckedThrowingContinuation { continuation in
            var headers: HTTPHeaders = [
                "Content-Type": "application/json"
            ]
            
            if let token = accessToken {
                headers["Authorization"] = "Bearer \(token)"
            }
            
            session.request(
                "\(baseURL)\(endpoint)",
                method: .post,
                parameters: body,
                encoder: JSONParameterEncoder.default,
                headers: headers
            )
            .validate()
            .responseDecodable(of: T.self) { response in
                switch response.result {
                case .success(let value):
                    continuation.resume(returning: value)
                case .failure(let error):
                    continuation.resume(throwing: error)
                }
            }
        }
    }
    
    func delete(endpoint: String) async throws {
        return try await withCheckedThrowingContinuation { continuation in
            var headers: HTTPHeaders = [
                "Content-Type": "application/json"
            ]
            
            if let token = accessToken {
                headers["Authorization"] = "Bearer \(token)"
            }
            
            session.request(
                "\(baseURL)\(endpoint)",
                method: .delete,
                headers: headers
            )
            .validate()
            .response { response in
                if let error = response.error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume()
                }
            }
        }
    }
}
```

---

## 📝 Usage Examples

### Android (Kotlin)

```kotlin
// 1. Initialize client
val client = UplinkNetworkClient.create(
    apiHost = "https://api-gateway.uplink.xyz",
    accessToken = "your-token"
)

// 2. Detect current WiFi network
val wifiInfo = client.network().getCurrentWifi()

// 3. Verify if registered
val status = client.routers().verifyRegistration(
    ssid = wifiInfo?.ssid ?: "",
    bssid = wifiInfo?.bssid ?: ""
)

status.onSuccess { registrationStatus ->
    if (!registrationStatus.isRegistered) {
        // 4. Register router
        val request = RegisterRouterRequest(
            ssid = wifiInfo?.ssid ?: "",
            bssid = wifiInfo?.bssid ?: "",
            location = Location(latitude = -23.5505, longitude = -46.6333),
            isIndoor = true
        )
        
        client.routers().registerRouter(request)
            .onSuccess { router ->
                println("Router registered: ${router.id}")
            }
    }
}

// 5. List my routers
client.routers().getMyRouters()
    .onSuccess { routers ->
        println("Total routers: ${routers.size}")
    }
```

### iOS (Swift)

```swift
// 1. Initialize client
let client = UplinkNetworkClient(
    apiHost: "https://api-gateway.uplink.xyz",
    accessToken: "your-token"
)

// 2. Detect current WiFi network
let wifiInfo = try await client.network().getCurrentWifi()

// 3. Verify if registered
let status = try await client.routers().verifyRegistration(
    ssid: wifiInfo?.ssid ?? "",
    bssid: wifiInfo?.bssid ?? ""
)

if !status.isRegistered {
    // 4. Register router
    let request = RegisterRouterRequest(
        ssid: wifiInfo?.ssid ?? "",
        bssid: wifiInfo?.bssid ?? "",
        location: Location(latitude: -23.5505, longitude: -46.6333),
        isIndoor: true
    )
    
    let router = try await client.routers().registerRouter(request)
    print("Router registered: \(router.id ?? "")")
}

// 5. List my routers
let routers = try await client.routers().getMyRouters()
print("Total routers: \(routers.count)")
```

---

## ⚠️ Important Notes

### Android
- Requires permissions: `ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`, `INTERNET`, `ACCESS_FINE_LOCATION`
- SSID/BSSID can be obtained directly via `WifiManager`
- Use `NetworkCallback` for real-time monitoring

### iOS
- **Limitation**: iOS does not allow direct access to SSID due to privacy concerns
- Requires `NetworkExtension` framework or special permissions
- Alternative: request SSID via `NEHotspotHelper` (requires special configuration)
- For production, consider using `CaptiveNetwork` framework (deprecated but still functional)

### Both Platforms
- Implement cache for network verification
- Add retry logic for requests
- Implement configurable logging
- Robust error handling
- Support for cancellation of asynchronous operations

---

## ✅ Implementation Checklist

- [ ] Android/iOS project structure
- [ ] Dependency configuration (build.gradle / Podspec)
- [ ] Implement NetworkDetector
- [ ] Implement RouterService
- [ ] Implement HttpClient
- [ ] Create data models
- [ ] Implement cache
- [ ] Add unit tests
- [ ] API documentation
- [ ] Usage examples
- [ ] Error handling
- [ ] Configurable logging
