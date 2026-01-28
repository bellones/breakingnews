# React Native Integration with Chase Mock App

## Overview

This document explains how the DWSDK library is integrated with the Chase Mock App (a React Native application). It covers the bridge architecture, CocoaPods setup, configuration flow, and end-to-end data flow from JavaScript to native SDK presentation.

## Architecture Overview

The integration follows this flow:

1. **JavaScript** calls `openDWSDK(options)` from the TypeScript wrapper
2. **TypeScript** validates options, applies defaults (e.g. theme from system), and calls the native module
3. **Native module** (`DWSDKModule`) receives the call, parses options into `DWSDKConfig`, finds the root view controller, initializes the SDK if needed, and calls `DWSDKCore.present(from:config:completion:)`
4. **DWSDKCore** presents the SDK modal (Data Savings Plan onboarding) and invokes the completion handler
5. **Promise** resolves or rejects back to JavaScript

## Repository and Path Layout

```
dwallet.mobile-sdk/
├── ios/                    # DWSDK framework (CocoaPods source)
│   ├── DWSDK.podspec
│   ├── DWSDK/
│   ├── Package.swift
│   ├── DWSDKModule.m       # Reference implementation
│   └── DWSDKModule.swift   # Reference implementation
├── apps/
│   └── mock-chase/         # React Native app
│       ├── ios/
│       │   ├── Podfile     # References DWSDK via path
│       │   ├── DWSDKModule.m
│       │   └── DWSDKModule.swift
│       └── src/
├── DWSDK.ts                # TypeScript/JS API
├── index.ts
└── package.json
```

The Chase Mock App lives under `apps/mock-chase/`. The native iOS app pulls in DWSDK via CocoaPods using a **local path** to the repo’s `ios/` directory.

## How the Library Is Imported (CocoaPods)

### Podfile Configuration

In the Chase Mock App, the Podfile is at `apps/mock-chase/ios/Podfile`:

```ruby
platform :ios, '13.0'
# ...

target 'MockChase' do
  config = use_native_modules!
  use_react_native!(...)

  # DWSDK Framework - using local path
  pod 'DWSDK', :path => '../../../ios'
  # ...
end
```

**Important details:**

- **Path**: `:path => '../../../ios'` is relative to the Podfile’s directory (`apps/mock-chase/ios/`), so it points to the repo root’s `ios/` folder (where `DWSDK.podspec` and the `DWSDK/` source live).
- CocoaPods uses that path to find `DWSDK.podspec` and build the DWSDK target; the built product is linked into the MockChase app target.
- After `pod install`, you must open **MockChase.xcworkspace** (not the `.xcodeproj`).

### Installation Steps

1. From repo root, install JS dependencies:
   ```bash
   npm install
   ```
2. Install iOS pods:
   ```bash
   cd apps/mock-chase/ios
   pod install
   ```
3. Open the workspace:
   ```bash
   open MockChase.xcworkspace
   ```

## React Native Bridge Module

The bridge consists of **Objective-C registration** and **Swift implementation**. Both live in the **app** project (`apps/mock-chase/ios/`). The pod provides the `DWSDK` framework; the app provides the bridge that uses it.

### 1. Objective-C Registration (`DWSDKModule.m`)

Location: `apps/mock-chase/ios/DWSDKModule.m`

Registers the native module and declares the method JavaScript can call:

```objc
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(DWSDKModule, NSObject)

RCT_EXTERN_METHOD(openDWSDKFlow:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolver
                  rejecter:(RCTPromiseRejectBlock)rejecter)

@end
```

- `RCT_EXTERN_MODULE(DWSDKModule, NSObject)` exposes the Swift class as `"DWSDKModule"`.
- `RCT_EXTERN_METHOD(openDWSDKFlow:...)` exposes the Promise-based method.

### 2. Swift Implementation (`DWSDKModule.swift`)

Location: `apps/mock-chase/ios/DWSDKModule.swift`

The Swift class conforms to `RCTBridgeModule` and implements `openDWSDKFlow`:

- **Parse options**: Builds a `DWSDKConfig` from the `options` dictionary (theme, userName, pendingCount, acceptedCount, rejectedCount).
- **Root view controller**: Finds the current key window’s root view controller.
- **Initialize SDK**: If needed, calls `DWSDKCore.shared.initialize()`.
- **Present**: Calls `DWSDKCore.present(from: rootViewController, config: config, completion: { success in ... })`.
- **Promise**: Calls `resolver(["success": true])` or `rejecter(...)`.

All work is done on the main queue.

### 3. TypeScript/JavaScript API (`DWSDK.ts`)

Location: `dwallet.mobile-sdk/DWSDK.ts`

- **Theme**: If not provided, uses system color scheme.
- **Options**: Maps `OpenDWSDKOptions` to the dictionary expected by the native module.
- **Validation**: Ensures counts are non-negative integers.
- **Native call**: `NativeModules.DWSDKModule.openDWSDKFlow(options, resolver, rejecter)` and returns a Promise.

## Configuration Flow (Options to DWSDKConfig)

1. **JS/TS**: App calls `openDWSDK({ theme, userName, pendingCount, acceptedCount, rejectedCount })`.
2. **TS**: Applies defaults, validates, then calls `DWSDKModule.openDWSDKFlow(nativeOptions, resolver, rejecter)`.
3. **Swift**: `parseOptions(_ options:)` maps dictionary to `DWSDKTheme`, optional userName, and counts (default 0, validated >= 0).
4. **Swift**: Creates `DWSDKConfig(...)` and passes it to `DWSDKCore.present(from:config:completion:)`.

## How the Chase Mock App Uses the SDK

### Home screen (`home.tsx`)

- Imports `openDWSDK` from `@drumwave/dwsdk-react-native`.
- Passes options (e.g. userName, theme, mock counts).
- On button press, calls `await openDWSDK({ ... })` and handles success/error.

### Data Savings Plan banner (`DataSavingsPlanBanner.tsx`)

- Renders a banner; button opens the SDK.
- Calls `openDWSDK({ theme: bannerTheme })` (and optionally other options).
- Handles loading and errors.

In both cases: **user action** → **openDWSDK()** → **DWSDKModule.openDWSDKFlow** → **DWSDKCore.present** → **native full-screen SDK UI**.

## Summary: How the Library Works and Is Imported

- **Native**: DWSDK is imported into the Chase Mock App as a **CocoaPods pod** from the local path `../../../ios`. The app links against the pod and uses `DWSDKCore` and `DWSDKConfig`.
- **Bridge**: The **React Native bridge** is in the **app** (`DWSDKModule.m` + `DWSDKModule.swift`), which imports the `DWSDK` module and uses it to initialize and present the SDK with a config from JS options.
- **JS**: The app imports `openDWSDK` from the dwallet.mobile-sdk package; that API calls `NativeModules.DWSDKModule.openDWSDKFlow` and triggers the native flow.

## Troubleshooting

- **"DWSDKModule is not available"**: Ensure `DWSDKModule.m` and `DWSDKModule.swift` are in the app target and you opened `.xcworkspace` after `pod install`.
- **"Could not find root view controller"**: Ensure the app has a visible window and root view controller when `openDWSDKFlow` runs.
- **Pod not found**: From `apps/mock-chase/ios`, `../../../ios` must point to the folder containing `DWSDK.podspec` and `DWSDK/`.
- **Theme or counts not applied**: Check that JS options match what `parseOptions` expects (theme string, numeric counts).

See also: [SDK_OVERVIEW.md](./SDK_OVERVIEW.md), [ARCHITECTURE.md](./ARCHITECTURE.md), [SPM_VS_COCOAPODS.md](./SPM_VS_COCOAPODS.md).
