## Prerequisites

- Android Studio (latest stable version)
- JDK 11 or higher
- Android SDK (API level 24+)
- Gradle 7.0+
- Android device or emulator running Android 7.0 (API 24) or higher

## SDK Integration

### Step 1: Add Dependencies

Add the SDK modules to your `build.gradle` (module level):

```gradle
dependencies {
    implementation project(':uplink-core-sdk')
    implementation project(':uplink-passpoint-profile-sdk')
}
```

Or if using published artifacts:

```gradle
dependencies {
    implementation 'com.uplink:core-sdk:1.0.0'
    implementation 'com.uplink:passpoint-profile-sdk:1.0.0'
}
```

### Step 2: Configure Permissions

Add required permissions to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### Step 3: Initialize SDK

Initialize the SDK in your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Core SDK
        val deviceInfo = DeviceInfo(
            deviceId = getDeviceId(),
            deviceModel = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            osType = "Android"
        )
        
        lifecycleScope.launch {
            val coreClient = UplinkCoreClient.fromService(
                baseUrl = "https://api-gateway.develop.uplink.xyz/v2",
                appId = "your-app-id",
                appSecret = "your-app-secret",
                deviceInfo = deviceInfo,
                context = applicationContext
            )
            
            // Initialize Passpoint SDK
            val passpointClient = UplinkPasspointClient.create(
                context = applicationContext,
                coreClient = coreClient
            )
            
            // Store clients for later use
            // (e.g., in a dependency injection container)
        }
    }
}
```

## Basic Usage

### Install a Profile

```kotlin
lifecycleScope.launch {
    // Check permissions first
    val profileManager = passpointClient.profileManager()
    if (!profileManager.hasAllPermissions()) {
        profileManager.requestPermissions(activity) { result ->
            if (result.allGranted) {
                installProfile()
            }
        }
        return@launch
    }
    
    // Fetch profile from API
    val profileResponse = coreClient.getAndroidPasspointProfile()
    
    // Install profile
    val result = profileManager.installProfileFromResponse(profileResponse)
    result.onSuccess { installResult ->
        if (installResult.success) {
            Log.i("App", "Profile installed: ${installResult.profileId}")
        } else {
            Log.e("App", "Installation failed: ${installResult.errorMessage}")
        }
    }.onFailure { error ->
        Log.e("App", "Installation error: ${error.message}")
    }
}
```

### List Profiles

```kotlin
lifecycleScope.launch {
    val result = profileManager.listProfiles()
    result.onSuccess { profiles ->
        Log.i("App", "Found ${profiles.size} installed profiles")
        profiles.forEach { profile ->
            Log.d("App", "Profile: ${profile.friendlyName} (${profile.fqdn})")
        }
    }.onFailure { error ->
        Log.e("App", "Failed to list profiles: ${error.message}")
    }
}
```

### Remove a Profile

```kotlin
lifecycleScope.launch {
    val result = profileManager.removeProfile("profile-id")
    result.onSuccess {
        Log.i("App", "Profile removed successfully")
    }.onFailure { error ->
        Log.e("App", "Failed to remove profile: ${error.message}")
    }
}
```

### Validate a Profile

```kotlin
lifecycleScope.launch {
    val result = profileManager.verifyProfile(profile)
    result.onSuccess { validationResult ->
        if (validationResult.isValid) {
            Log.i("App", "Profile is valid")
        } else {
            Log.e("App", "Profile validation failed: ${validationResult.errorMessage}")
        }
    }
}
```

## Complete Integration Example

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var passpointClient: UplinkPasspointClient
    private lateinit var coreClient: UplinkCoreClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        lifecycleScope.launch {
            initializeSDK()
        }
    }
    
    private suspend fun initializeSDK() {
        val deviceInfo = DeviceInfo(
            deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
            deviceModel = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            osType = "Android"
        )
        
        coreClient = UplinkCoreClient.fromService(
            baseUrl = "https://api-gateway.develop.uplink.xyz/v2",
            appId = "your-app-id",
            appSecret = "your-app-secret",
            deviceInfo = deviceInfo,
            context = applicationContext
        ) ?: return
        
        passpointClient = UplinkPasspointClient.create(
            context = applicationContext,
            coreClient = coreClient,
            errorCallback = object : PasspointErrorCallback {
                override fun onInstallationError(error: Throwable, attempt: Int, willRetry: Boolean) {
                    Log.e("App", "Installation error (attempt $attempt, willRetry: $willRetry): ${error.message}")
                }
            }
        )
    }
    
    private fun installProfile() {
        lifecycleScope.launch {
            val profileManager = passpointClient.profileManager()
            
            // Check permissions
            if (!profileManager.hasAllPermissions()) {
                profileManager.requestPermissions(this@MainActivity) { result ->
                    if (result.allGranted) {
                        installProfile()
                    }
                }
                return@launch
            }
            
            // Fetch and install profile
            try {
                val profileResponse = coreClient.getAndroidPasspointProfile()
                val result = profileManager.installProfileFromResponse(profileResponse)
                result.onSuccess { installResult ->
                    if (installResult.success) {
                        showMessage("Profile installed successfully")
                    } else {
                        showError("Installation failed: ${installResult.errorMessage}")
                    }
                }.onFailure { error ->
                    showError("Error: ${error.message}")
                }
            } catch (e: Exception) {
                showError("Failed to fetch profile: ${e.message}")
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        passpointClient.profileManager().handlePermissionResult(requestCode, permissions, grantResults)
    }
}
```

## Next Steps

- Read the [Passpoint SDK API Reference](passpoint-sdk.md) for detailed API documentation
- Check [Permissions Guide](permissions.md) for permission handling
- See [Code Examples](examples.md) for more examples
- Review [Best Practices](../best-practices.md) for recommended patterns
