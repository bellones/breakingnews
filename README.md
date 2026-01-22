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
            
            // Fetch and install
            try {
                val profileResponse = coreClient.getAndroidPasspointProfile()
                val result = profileManager.installProfileFromResponse(profileResponse)
                result.onSuccess { installResult ->
                    if (installResult.success) {
                        showMessage("Profile installed: ${installResult.profileId}")
                    } else {
                        showError("Installation failed: ${installResult.errorMessage}")
                    }
                }.onFailure { error ->
                    showError("Error: ${error.message}")
                }
            } catch (e: Exception) {
                showError("Failed: ${e.message}")
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

## Profile Installation Example

```kotlin
suspend fun installProfileExample() {
    val profileManager = passpointClient.profileManager()
    
    // Check permissions
    val permissionStatus = profileManager.checkPermissions()
    if (!permissionStatus.allGranted) {
        // Request permissions
        return
    }
    
    // Fetch profile
    val profileResponse = coreClient.getAndroidPasspointProfile()
    
    // Install
    val result = profileManager.installProfileFromResponse(profileResponse)
    result.onSuccess { installResult ->
        if (installResult.success) {
            Log.i("App", "Profile installed: ${installResult.profileId}")
        }
    }
}
```

## Profile Listing Example

```kotlin
suspend fun listProfilesExample() {
    val result = profileManager.listProfiles()
    result.onSuccess { profiles ->
        profiles.forEach { profile ->
            Log.d("App", "Profile: ${profile.friendlyName} (${profile.fqdn})")
        }
    }
}
```

## Profile Removal Example

```kotlin
suspend fun removeProfileExample(profileId: String) {
    val result = profileManager.removeProfile(profileId)
    result.onSuccess {
        Log.i("App", "Profile removed")
    }.onFailure { error ->
        Log.e("App", "Removal failed: ${error.message}")
    }
}
```

## Error Handling Example

```kotlin
suspend fun errorHandlingExample() {
    try {
        val profileResponse = coreClient.getAndroidPasspointProfile()
        val result = profileManager.installProfileFromResponse(profileResponse)
        result.onFailure { error ->
            when (error) {
                is SecurityException -> {
                    // Permission error
                    requestPermissions()
                }
                is IllegalArgumentException -> {
                    // Invalid configuration
                    showError("Invalid profile configuration")
                }
                is ApiException.NetworkError -> {
                    // Network error - retry
                    retryInstallation()
                }
                else -> {
                    // Other errors
                    showError("Installation failed: ${error.message}")
                }
            }
        }
    } catch (e: ApiException.Unauthorized) {
        // Authentication error
        reAuthenticate()
    }
}
```

## Lifecycle Management Example

```kotlin
// Manual polling
passpointClient.checkForProfileUpdates()

// Certificate monitoring
passpointClient.checkCertificateMonitoring()

// Renewal check
passpointClient.checkRenewal()
```

## Permission Handling Example

```kotlin
fun handlePermissions() {
    val profileManager = passpointClient.profileManager()
    
    if (!profileManager.hasAllPermissions()) {
        profileManager.requestPermissions(this) { result ->
            when {
                result.allGranted -> {
                    // Proceed
                }
                result.shouldShowRationale -> {
                    // Show rationale
                    showPermissionRationale()
                }
                else -> {
                    // Guide to settings
                    showSettingsDialog()
                }
            }
        }
    }
}
```
