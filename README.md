# Android Permissions Guide

## Overview

The Uplink Passpoint Profile SDK requires specific permissions to function correctly on Android devices. This document outlines all required permissions, their purposes, and how to handle them in your application.

## Required Permissions

### Manifest Permissions

These permissions must be declared in your `AndroidManifest.xml`:

#### 1. `ACCESS_WIFI_STATE`
- **Purpose**: Allows the app to access information about Wi-Fi networks
- **Required for**: Reading Wi-Fi state, checking network connectivity
- **Declaration**:
  ```xml
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  ```
- **Runtime Permission**: Not required (normal permission)

#### 2. `CHANGE_WIFI_STATE`
- **Purpose**: Allows the app to change Wi-Fi connectivity state
- **Required for**: Installing, modifying, and removing Passpoint profiles
- **Declaration**:
  ```xml
  <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
  ```
- **Runtime Permission**: Not required (normal permission)

#### 3. `ACCESS_FINE_LOCATION`
- **Purpose**: Required for Wi-Fi operations on Android 6.0 (API 23) and higher
- **Required for**: All Passpoint profile operations (install, list, remove)
- **Declaration**:
  ```xml
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  ```
- **Runtime Permission**: **REQUIRED** on Android 6.0+ (dangerous permission)
- **Note**: This is a location permission, but it's required by Android for Wi-Fi operations even if you're not using location features.

#### 4. `INTERNET`
- **Purpose**: Allows the app to make network requests
- **Required for**: API calls to fetch Passpoint profiles
- **Declaration**:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  ```
- **Runtime Permission**: Not required (normal permission)

## Runtime Permissions (Android 6.0+)

Starting from Android 6.0 (API level 23), `ACCESS_FINE_LOCATION` is a dangerous permission that must be requested at runtime. The SDK provides automatic permission request functionality.

### Permission Request Flow

1. **Check Permissions**: The SDK checks if all required permissions are granted
2. **Request Missing Permissions**: If permissions are missing, the SDK automatically requests them
3. **Handle Results**: The SDK handles permission grant/denial and provides user-friendly error messages

### Using the SDK's Permission Manager

The SDK includes a `PasspointPermissionManager` that handles permission checking and requesting:

```kotlin
import com.uplink.passpoint.permissions.PasspointPermissionManager

// Check permissions
val permissionManager = PasspointPermissionManager()
val status = permissionManager.checkPermissions(context)

if (!status.allGranted) {
    // Request permissions (requires Activity context)
    permissionManager.requestPermissions(activity) { result ->
        if (result.allGranted) {
            // All permissions granted, proceed with operation
        } else {
            // Show user-friendly error message
            showError(result.userFriendlyMessage)
        }
    }
}
```

### Manual Permission Handling

If you prefer to handle permissions manually:

```kotlin
// Check if permission is granted
if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
    != PackageManager.PERMISSION_GRANTED) {
    // Request permission
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSION_REQUEST_CODE
    )
}
```

## Permission Requirements by Operation

### Install Profile
- `ACCESS_FINE_LOCATION` (runtime permission required)
- `CHANGE_WIFI_STATE`
- `ACCESS_WIFI_STATE`

### List Profiles
- `ACCESS_FINE_LOCATION` (runtime permission required)
- `ACCESS_WIFI_STATE`

### Remove Profile
- `ACCESS_FINE_LOCATION` (runtime permission required)
- `CHANGE_WIFI_STATE`
- `ACCESS_WIFI_STATE`

## Android Version Considerations

### Android 5.1 and below (API < 23)
- All permissions are granted at install time
- No runtime permission requests needed

### Android 6.0+ (API 23+)
- `ACCESS_FINE_LOCATION` must be requested at runtime
- User can grant or deny permission
- App must handle permission denial gracefully

### Android 10+ (API 29+)
- Location permission is still required for Wi-Fi operations
- Additional restrictions may apply

### Android 11+ (API 30+)
- Passpoint profiles can be installed via `WifiNetworkSuggestion` API
- Location permission still required

## Manifest Configuration

### Complete Manifest Example

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.yourapp">

    <!-- Required for Passpoint SDK -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">
        <!-- Your activities -->
    </application>
</manifest>
```

## Error Messages

The SDK provides user-friendly error messages when permissions are missing:

- **Missing Location Permission**: "Location permission is required to configure Wi-Fi networks. Please grant permission in Settings."
- **Missing Wi-Fi Permission**: "Wi-Fi permission is required. Please enable it in your device settings."
- **Multiple Missing Permissions**: "All required permissions must be granted to install Passpoint profiles."

## Troubleshooting

### Permission Denied Error

**Problem**: App crashes or returns error when trying to install/list/remove profiles.

**Solution**:
1. Ensure `ACCESS_FINE_LOCATION` is declared in manifest
2. Request runtime permission before calling SDK methods
3. Check that user granted permission (not just requested)

### Permission Request Not Showing

**Problem**: Permission dialog doesn't appear.

**Solution**:
1. Ensure you're using an `Activity` context (not Application context) for permission requests
2. Check that permission is declared in manifest
3. Verify you're not requesting permission that's already granted
4. Check if user previously denied and selected "Don't ask again"

### Location Permission Required for Wi-Fi

**Problem**: Why do I need location permission for Wi-Fi operations?

**Solution**: This is an Android system requirement. Starting from Android 6.0, accessing Wi-Fi information requires location permission because Wi-Fi networks can be used to infer device location. This is a platform requirement, not an SDK requirement.

### Permission Already Granted But Still Failing

**Problem**: Permission shows as granted but SDK operations still fail.

**Solution**:
1. Verify permission is actually granted: `ContextCompat.checkSelfPermission()`
2. Check if device location services are enabled (Settings > Location)
3. Ensure you're checking permission on the correct context (Activity context)

## Best Practices

1. **Request Permissions Early**: Request permissions when the user first accesses Passpoint features, not when they try to install a profile
2. **Explain Why**: Show a rationale dialog explaining why location permission is needed for Wi-Fi operations
3. **Handle Denial Gracefully**: If user denies permission, show helpful instructions on how to grant it manually
4. **Check Before Operations**: Always check permissions before calling SDK methods
5. **Use SDK's Permission Manager**: The SDK's permission manager handles edge cases and provides consistent error messages

## Example: Complete Integration

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var passpointClient: UplinkPasspointClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        val coreClient = UplinkCoreClient.fromService()
        passpointClient = UplinkPasspointClient(coreClient)
        
        // Check and request permissions
        checkPermissions()
    }
    
    private fun checkPermissions() {
        val permissionManager = PasspointPermissionManager()
        val status = permissionManager.checkPermissions(this)
        
        if (!status.allGranted) {
            // Show rationale if needed
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationaleDialog()
            } else {
                // Request permissions
                permissionManager.requestPermissions(this) { result ->
                    if (!result.allGranted) {
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }
    
    private fun installProfile() {
        // SDK will check permissions automatically, but you can also check first
        val permissionManager = PasspointPermissionManager()
        if (!permissionManager.hasAllPermissions(this)) {
            checkPermissions()
            return
        }
        
        // Proceed with installation
        lifecycleScope.launch {
            val result = passpointClient.profileManager().installProfile(profile)
            // Handle result
        }
    }
}
```

## Additional Resources

- [Android Permissions Overview](https://developer.android.com/guide/topics/permissions/overview)
- [Request App Permissions](https://developer.android.com/training/permissions/requesting)
- [Wi-Fi Configuration Guide](https://developer.android.com/guide/topics/connectivity/wifi-suggest)

