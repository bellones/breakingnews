# Changelog

## Recent Fixes

### Android

#### Playground App
- **Google Play Store Publishing**: Added support for publishing the Android Playground app to Google Play Store for internal testing
  - Configured keystore generation and signing
  - Added AAB (Android App Bundle) build configuration
  - Set up release build configuration for Play Store distribution

---

### iOS

#### SDK Fixes

- **Background Task Registration**: Fixed duplicate background task registration crash
  - Added Objective-C helper (`BGTaskRegistrationHelper`) to safely handle `NSException` during task registration
  - Implemented thread-safe registration guard to prevent duplicate registrations
  - Added proper bridging header configuration

- **Endpoint Configuration**: Fixed incorrect endpoint usage for iOS Passpoint profile downloads
  - Corrected `fetchIOSPasspointProfile()` to use the correct iOS-specific endpoint
  - Added validation and logging to ensure platform-specific endpoints are used

- **Entitlements Validation**: Improved entitlements checking logic
  - Enhanced logging for entitlement validation
  - Improved error handling when entitlements are missing

- **Profile Installation**: Enhanced profile installation logic
  - Added duplicate profile detection (by FQDN or ID) before installation
  - Improved `NEHotspotHS20Settings` configuration to always set required fields (even if empty)
  - Added detailed logging for profile installation process

- **Profile Removal**: Fixed profile removal functionality
  - Corrected cache removal logic to properly find and remove profiles by ID or FQDN
  - Ensured `UserDefaults.synchronize()` is called after cache updates
  - Improved error handling and user feedback

#### Playground App Fixes

- **Info.plist Configuration**: Added missing background task identifier
  - Added `BGTaskSchedulerPermittedIdentifiers` with `com.uplink.passpoint.profilePolling` identifier

- **Double-Tap Crash**: Fixed crash when double-clicking Passpoint profile
  - Added debounce mechanism and `isProcessing` flag to prevent multiple simultaneous operations
  - Implemented tap handling protection in both button taps and table view row selections

- **UI Flow Improvements**: Enhanced profile download and installation flow
  - Added "Download Again" button option when a profile is already loaded
  - Implemented `resetProfileState()` to allow re-downloading profiles without navigating away
  - Improved button state management between download and install actions

- **Multiple Profile Handling**: Improved handling of multiple installed profiles
  - Added duplicate profile detection and prevention
  - Enhanced cache management to remove existing profiles before adding new ones
  - Improved UI feedback when attempting to install duplicate profiles

- **CocoaPods Integration**: Fixed framework linking issues
  - Resolved `Pods_Playground.framework` not found errors
  - Performed clean `pod deintegrate` and `pod install` to fix dependency issues
  - Updated framework search paths and build configurations

---

## Notes

- All fixes have been tested and verified
- iOS fixes require rebuilding the SDK frameworks and Playground app
- Android Play Store publishing requires proper keystore configuration
