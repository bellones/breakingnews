# DWSDK Architecture

## Overview

This document describes the internal architecture of the DWSDK: module layout, public API, data flow, and key design decisions.

## Module Layout

```
ios/DWSDK/
├── DWSDK.h                 # Umbrella header (CocoaPods)
├── Info.plist
├── Core/
│   ├── DWSDKCore.swift     # Singleton, init, present
│   └── DWSDKConfig.swift   # Config model
├── Models/
│   ├── DWSDKModel.swift    # Base model
│   └── CompanyCircleConfig.swift
└── UI/
    ├── DWSDKViewController.swift      # Base VC
    ├── DWSDKModalViewController.swift # Modal container
    ├── DataSavingsPlanOnboardingViewController.swift
    ├── DataSavingsPlanFormViewController.swift
    ├── DataSavingsPlanSummaryViewController.swift
    ├── ApplicationSuccessViewController.swift
    ├── FormStepViewController.swift
    ├── Steps/              # Form step VCs
    ├── Theme/               # Colors, typography, spacing
    ├── Views/               # Reusable views
    ├── Assets/              # Images
    └── ViaCEPService.swift  # CEP lookup
```

## Public API Surface

### Entry Points

- **DWSDKCore.shared**: Singleton access.
- **DWSDKCore.shared.initialize(configuration:)**: Initialize SDK (thread-safe, idempotent).
- **DWSDKCore.present(from:config:completion:)**: Present SDK modal from a given view controller with a config and optional completion.

### Configuration

- **DWSDKConfig(userName:theme:pendingCount:acceptedCount:rejectedCount:)**: Immutable config for presentation. All parameters optional except theme (default `.light`).

### Theme and Styling

- **DWSDKTheme**: Enum `.light`, `.dark`.
- **DWSDKViewController.theme**: Settable on base and subclasses; drives colors and typography.
- Theme colors, typography, and spacing are used internally; public usage is via `theme` and config.

### Models

- **DWSDKModel**: Base model with `identifier`, `createdAt`, `toDictionary()`.
- **CompanyCircleConfig**: Company/circle configuration model.

Other types (e.g. view controllers, custom views) are public where they need to be referenced or subclassed but are not part of the minimal “integrate and present” API.

## Data Flow

### Presentation Flow

1. Host app obtains a `UIViewController` (e.g. root VC).
2. Host creates `DWSDKConfig` with theme, optional userName, and counts.
3. Host calls `DWSDKCore.shared.initialize()` if not already initialized.
4. Host calls `DWSDKCore.present(from: viewController, config: config, completion: { ... })`.
5. `DWSDKCore`:
   - Dispatches to main queue.
   - Checks that the view controller is not already presenting.
   - Verifies SDK is initialized.
   - Instantiates `DataSavingsPlanOnboardingViewController(config:)`.
   - Wraps it in a `UINavigationController` (modal presentation style full screen).
   - Presents the navigation controller and calls the completion handler.

### Onboarding and Form Flow

1. **DataSavingsPlanOnboardingViewController**: First screen (hero, benefits, “Apply now” CTA). On CTA, it pushes or presents **DataSavingsPlanFormViewController** (depending on navigation setup).
2. **DataSavingsPlanFormViewController**: Holds a list of step view controllers and shared `formData`. It shows one step at a time, advances on “Continue”, and collects step data into `formData` via `collectStepData(from:)`.
3. **Steps**: PersonalInfo, HomeAddress, PayoutMethod, ReviewConfirm, GoalSetting. Each step exposes fields and validation; form VC reads values and merges into `formData`.
4. After the last step (Goal Setting), the form VC presents **DataSavingsPlanSummaryViewController** with the current `formData`.
5. Summary screen shows editable cards; “Edit” returns to the corresponding step; “Looks good” proceeds to terms/success.
6. **ApplicationSuccessViewController**: Shown after successful submission; “Return to home” dismisses and the host’s completion block can run.

### Form Data and Step Collection

- **formData**: Dictionary maintained by `DataSavingsPlanFormViewController`; keys include `firstName`, `lastName`, `cpf`, `pixKey`, `street`, `cep`, `selectedCategories`, `selectedGoal`, `prefersNotToMonetize`, etc.
- **collectStepData(from:)**: Called when leaving a step or when returning from summary to refresh; each step type knows how to read its own UI and write into the shared `formData`.
- Summary and success screens read from `formData` or from a config/data passed by the form VC.

## Key Design Decisions

### Singleton and Initialization

- **DWSDKCore** is a singleton so that initialization and “present” entry point are global and easy to use from any host (e.g. React Native bridge).
- Initialization is locked and idempotent so that multiple calls (e.g. from JS) do not cause issues.

### Modal-Only Presentation

- The SDK is always presented as a full-screen modal. The host does not embed SDK view controllers in its own navigation; the SDK brings its own `UINavigationController` and flow.

### Theme and Styling

- **DWSDKTheme** is the single public theme switch; **DWSDKThemeColors**, **DWSDKTypography**, and **DWSDKSpacing** centralize look and feel so that all screens and components stay consistent and can react to theme changes.

### Objective-C Compatibility

- Core types used from the bridge or from Objective-C are annotated with `@objc` and, where needed, inherit from `NSObject`. This allows React Native and legacy Objective-C apps to use the SDK without a Swift-only dependency at the call site.

### Bundle and Resources

- Images and fonts are loaded from the DWSDK resource bundle when available, with fallback to the main bundle so that the SDK works both when embedded as a pod and in development setups.
- **Bundle(for: DWSDKCore.self)** (or equivalent) is used to locate the framework bundle and its resources.

### Validation and Errors

- Form steps validate input and expose validation state; the form VC enables “Continue” only when the current step is valid.
- Presentation and bridge errors are communicated via completion handlers and Promise rejections (e.g. root VC not found, init failed, present failed) with clear error codes and messages.

## Dependencies

- **System**: Foundation, UIKit. No third-party Swift or Obj-C dependencies.
- **ViaCEP**: Used for Brazilian CEP (postal code) lookup; network calls are self-contained in **ViaCEPService** (e.g. Result-based API).

## Security and Privacy

- Sensitive fields (e.g. CPF, account numbers) are masked in summary and logs where applicable.
- No credentials or tokens are stored by the SDK; form data is held in memory for the duration of the flow.

## Testing and Debugging

- **DWSDKCore.reset()** is available in DEBUG to clear initialization state for tests.
- Bridge and presentation paths can be tested by invoking `openDWSDK` from the Chase Mock App and verifying modal presentation and dismissal.

## Related Documentation

- [SDK_OVERVIEW.md](./SDK_OVERVIEW.md) – High-level overview and usage.
- [SWIFT_PACKAGE.md](./SWIFT_PACKAGE.md) – Swift Package layout and build.
- [SPM_VS_COCOAPODS.md](./SPM_VS_COCOAPODS.md) – Distribution and dependency manager comparison.
- [REACT_NATIVE_INTEGRATION.md](./REACT_NATIVE_INTEGRATION.md) – How the Chase Mock App imports and uses the library.
