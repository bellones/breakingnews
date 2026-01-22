## Common Issues and Solutions

### Authentication Failures

**Issue**: Authentication fails with 401 Unauthorized

**Solutions**:
1. Verify `appId` and `appSecret` are correct
2. Check that credentials are enabled in the backend
3. Ensure network connectivity
4. Check token expiration and refresh

**Debug Steps**:
- Check logs for authentication errors
- Verify credentials in code
- Test with curl to verify backend

### Profile Installation Failures

**Issue**: Profile installation fails

**Solutions**:
1. Check permissions/entitlements are granted
2. Verify profile configuration is valid
3. Check network connectivity
4. Review error messages for specific issues

**Common Errors**:
- `SecurityException` (Android): Missing permissions
- `PasspointPermissionError` (iOS): Missing entitlements
- `IllegalArgumentException`: Invalid profile configuration

### Permission/Entitlement Issues

**Android - Missing Permissions**:

1. Verify permissions in `AndroidManifest.xml`
2. Request runtime permissions
3. Check permission status before operations

**iOS - Missing Entitlements**:

1. Verify entitlements in Apple Developer Portal
2. Check entitlements file in Xcode
3. Ensure provisioning profile includes entitlements
4. Clean and rebuild project

### Network Connectivity Issues

**Issue**: API calls fail with network errors

**Solutions**:
1. Check internet connectivity
2. Verify base URL is correct
3. Check firewall/proxy settings
4. Verify SSL certificates

### Certificate Validation Issues

**Issue**: Certificate validation fails

**Solutions**:
1. Verify certificate format (PEM, Base64)
2. Check certificate expiration
3. Verify CA certificate chain
4. Review certificate parsing errors in logs

### iOS-Specific Issues

**Background Task Not Working**:

1. Verify `BGTaskSchedulerPermittedIdentifiers` in `Info.plist`
2. Ensure background task is registered in `AppDelegate`
3. Check background modes are enabled
4. Test on physical device (simulator limitations)

**Profile Removal Not Working**:

- This is an iOS limitation
- Users must remove manually from Settings
- SDK cleans cache but cannot remove system profile

### Android-Specific Issues

**WorkManager Not Running**:

1. Verify WorkManager dependency
2. Check battery optimization settings
3. Ensure app has necessary permissions
4. Test background execution

## Debug Logging

### Android

Enable logging:

```kotlin
// Logs are automatically written to logcat
// Filter by tag: "PasspointProfileInstaller", "PasspointProfileLister", etc.
```

### iOS

Access log file:

```swift
// Get log file path
let logPath = passpointClient.getLogFilePath()

// Read log file
let logContents = passpointClient.readLogFile()

// Get log file URL for sharing
let logURL = passpointClient.getLogFileURL()
```

## Log Analysis

### Common Log Patterns

**Installation Success**:
```
✅ Profile installed successfully: profile-123
```

**Installation Failure**:
```
❌ Installation failed: Permission denied
```

**Polling**:
```
Checking for profile updates from backend API...
Successfully fetched profile from API: profile-123
```

## Getting Help

1. Check logs for error messages
2. Review error codes and descriptions
3. Verify configuration (permissions, entitlements, base URL)
4. Test with minimal example
5. Contact SDK support with:
   - Error messages
   - Log files
   - Platform and version
   - Steps to reproduce

## Related Documentation

- [Error Handling](error-handling.md)
- [Best Practices](best-practices.md)
- [Permissions Guide](../android/permissions.md)
- [Entitlements Guide](../ios/entitlements.md)
