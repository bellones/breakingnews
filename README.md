# DWSDK Overview

## What is DWSDK?

DWSDK (Drumwave SDK) is a comprehensive iOS SDK framework for integrating Drumwave functionality into iOS applications. The SDK provides a complete user interface for Data Savings Plan applications, including onboarding flows, multi-step forms, and data management screens.

**Key Features:**
- Full-screen modal presentation
- Light and dark theme support
- Multi-step form wizard with validation
- Brazilian address lookup (ViaCEP integration)
- Accessibility support (VoiceOver, Dynamic Type)
- React Native bridge support
- Thread-safe initialization

**Requirements:**
- iOS 13.0+
- Swift 5.0+
- Xcode 12.0+

## Core Components

### DWSDKCore

The main SDK class that handles initialization and presentation. It follows a singleton pattern for thread-safe access.

**Key Properties:**
- `shared`: Singleton instance
- `sdkVersion`: Current SDK version string ("1.0.0")
- `isInitialized`: Boolean indicating initialization status

**Key Methods:**
- `initialize(configuration:)`: Initialize the SDK (thread-safe, idempotent)
- `present(from:config:completion:)`: Present the SDK modal view controller
- `reset()`: Reset SDK state (DEBUG only, for testing)

**Usage:**
```swift
// Initialize the SDK
DWSDKCore.shared.initialize()

// Present the SDK modal
let config = DWSDKConfig(
    userName: "John Doe",
    theme: .dark,
    pendingCount: 5,
    acceptedCount: 10,
    rejectedCount: 2
)
DWSDKCore.present(from: self, config: config) { success in
    if success {
        print("SDK presented successfully")
    }
}
```

### DWSDKConfig

Configuration object that contains all settings needed to present the SDK interface.

**Properties:**
- `userName`: Optional username to display in the interface
- `theme`: Theme configuration (`.light` or `.dark`)
- `pendingCount`: Count of pending items (default: 0)
- `acceptedCount`: Count of accepted items (default: 0)
- `rejectedCount`: Count of rejected items (default: 0)

**Usage:**
```swift
let config = DWSDKConfig(
    userName: "Carolina",
    theme: .dark,
    pendingCount: 3,
    acceptedCount: 5,
    rejectedCount: 1
)
```

### DWSDKViewController

Base view controller class for all SDK UI components. Provides theme support and a customizable setup method.

**Key Features:**
- Automatic theme-based background color
- Overridable `setupUI()` method
- Theme change notifications via `didSet`

**Usage:**
```swift
class MyCustomViewController: DWSDKViewController {
    override func setupUI() {
        super.setupUI()
        // Add custom UI setup
    }
}
```

## UI Flow Architecture

The SDK provides a complete user journey through several interconnected view controllers:

### 1. Onboarding Flow (`DataSavingsPlanOnboardingViewController`)

The entry point of the SDK, displayed when `DWSDKCore.present()` is called. This screen includes:

- **Hero Banner**: Full-width image with overlay text
- **Benefits Section**: Three benefit cards (Exclusive Offers, Stay in Control, Build Data Wealth)
- **How It Works**: Step-by-step explanation
- **Apply Section**: Key metrics (locked period, fees)
- **CTA Button**: "Apply now" button that triggers the form flow

**Navigation:** On CTA tap → `DataSavingsPlanFormViewController`

### 2. Form Flow (`DataSavingsPlanFormViewController`)

A multi-step wizard that collects user information through 5 sequential steps:

#### Step 1: Personal Information (`PersonalInfoStepViewController`)
- First name and last name
- CPF (Brazilian tax ID) with automatic formatting
- Pix Key (Brazilian payment system key)

#### Step 2: Home Address (`HomeAddressStepViewController`)
- CEP (Brazilian postal code) with automatic address lookup via ViaCEP API
- Street address
- Number and complement
- Neighborhood
- State (2-letter code)

#### Step 3: Payout Method (`PayoutMethodStepViewController`)
- Bank name
- Account number (masked display)
- Routing number (masked display)
- **Optional**: Can be skipped

#### Step 4: Review & Confirm (`ReviewConfirmStepViewController`)
- Data category selection (8 categories)
- Visual earnings graph showing potential based on selections
- Category toggles with checkmarks

#### Step 5: Goal Setting (`GoalSettingStepViewController`)
- Slider for annual earnings goal (0-10,000)
- Alternative option: "Prefer not to monetize"
- Real-time goal display with currency formatting

**Navigation:**
- Each step has a "Continue" button that validates and advances
- Step 3 has an optional "Skip" button
- After Step 5 → `DataSavingsPlanSummaryViewController`

### 3. Summary/Confirmation Flow (`DataSavingsPlanSummaryViewController`)

Displays all collected information in editable cards:

- **Personal Info Card**: Name, CPF, Pix Key
- **Home Address Card**: Complete address
- **Payout Info Card**: Bank details (if provided)
- **Data Contribution Card**: Selected categories
- **Goal Card**: Selected goal or opt-out status

**Features:**
- Edit buttons on each card to return to specific steps
- "Looks good" button to submit
- Full-screen banner matching onboarding design

**Navigation:**
- Edit button → Returns to specific step in form
- "Looks good" → `ApplicationSuccessViewController`

### 4. Success Screen (`ApplicationSuccessViewController`)

Final confirmation screen displayed after successful submission:

- Success animation/image
- Confirmation message
- "Return to home" button

**Navigation:**
- "Return to home" → Dismisses modal and calls completion handler

## Theme System

The SDK includes a comprehensive theming system for consistent visual appearance.

### DWSDKTheme

Enum defining available themes:
- `.light`: Light theme with white backgrounds
- `.dark`: Dark theme with black/dark gray backgrounds (default)

### DWSDKThemeColors

Provides color palette for each theme:

**Text Colors:**
- `text.default`: Primary text color
- `text.inactive`: Secondary/muted text color
- `text.inverted`: Text for dark backgrounds (white)

**Background Colors:**
- `background.base`: Main background color
- `background.overlay`: Overlay backgrounds

**Border Colors:**
- `border.primary`: Default border color
- `border.negative`: Error state border color

### DWSDKTypography

Typography system with custom fonts:

**Font Families:**
- **Primary**: Open Sans (Regular, Medium, SemiBold, Bold)
- **Secondary**: Titillium Web (Regular, Light, SemiBold, Bold)

**Text Styles:**
- `textStyle.title`: Large title font
- `textStyle.body`: Body text font
- `textStyle.bodyBold`: Bold body text
- `primaryFont(size:weight:)`: Custom primary font
- `secondaryFont(size:weight:)`: Custom secondary font

### DWSDKSpacing

Consistent spacing system for layout:

- `xs`: Extra small (4pt)
- `small`: Small (8pt)
- `base`: Base (12pt)
- `medium`: Medium (16pt)
- `large`: Large (24pt)
- `xl`: Extra large (32pt)
- `xxl`: Extra extra large (48pt)

## Main Aspects

### Initialization

The SDK uses a thread-safe singleton pattern for initialization:

```swift
// Thread-safe initialization
DWSDKCore.shared.initialize()

// Check initialization status
if DWSDKCore.shared.isInitialized {
    // SDK is ready
}
```

**Key Features:**
- Uses `NSLock` for thread safety
- Idempotent: Can be called multiple times safely
- One-time initialization check
- Optional configuration parameter (reserved for future use)

### Presentation

The SDK presents as a full-screen modal:

```swift
DWSDKCore.present(
    from: viewController,
    config: config,
    completion: { success in
        // Handle completion
    }
)
```

**Features:**
- Automatic main queue dispatch
- Prevents duplicate presentations
- Verifies SDK initialization before presenting
- Full-screen modal presentation style
- Completion handler for async operations

### Form Flow

The multi-step form includes:

**Data Persistence:**
- Form data stored in dictionary across steps
- Data collected at each step transition
- Edit mode allows returning to specific steps

**Validation:**
- Real-time validation on each field
- Required field indicators
- Format-specific validation (CPF, CEP, phone, etc.)
- Continue button enabled only when valid

**Navigation:**
- Progress bar showing current step
- Back button to previous step
- Step counter display
- Blurred background header

### Theme System

**Dynamic Theme Switching:**
- Themes can be changed at runtime
- All UI components update automatically
- WCAG contrast ratio verification
- Custom font loading from resource bundle

**Accessibility:**
- VoiceOver support with descriptive labels
- Dynamic Type support for text scaling
- Proper accessibility traits
- Semantic HTML-like structure

### Asset Management

**Resource Bundle:**
- Assets loaded from `DWSDK.bundle` resource bundle
- Fallback to main bundle if resource bundle unavailable
- Images, fonts, and localized strings

**Bundle Resolution:**
```swift
let classBundle = Bundle(for: DWSDKCore.self)
if let resourceBundlePath = classBundle.path(forResource: "DWSDK", ofType: "bundle"),
   let resourceBundle = Bundle(path: resourceBundlePath) {
    // Load from resource bundle
}
```

### Error Handling

**Error Types:**
- Initialization errors
- Presentation errors
- Validation errors
- Network errors (ViaCEP API)

**Error Patterns:**
- Completion handlers with success/failure
- Optional error parameters
- Descriptive error messages
- Error codes for programmatic handling

## Integration Points

### Native iOS Apps

```swift
import DWSDK

// Initialize
DWSDKCore.shared.initialize()

// Present
let config = DWSDKConfig(theme: .dark, userName: "User")
DWSDKCore.present(from: self, config: config)
```

### React Native Apps

```typescript
import { openDWSDK } from '@drumwave/dwsdk-react-native';

await openDWSDK({
  theme: 'dark',
  userName: 'User',
  pendingCount: 5,
  acceptedCount: 10,
  rejectedCount: 2
});
```

See [REACT_NATIVE_INTEGRATION.md](./REACT_NATIVE_INTEGRATION.md) for detailed integration guide.

## Localization

The SDK supports localization through:

- `NSLocalizedString` for user-facing text
- `Localizable.strings` file
- Locale-aware currency formatting
- Brazilian Portuguese as primary language

## Accessibility

Full accessibility support including:

- **VoiceOver**: All UI elements have descriptive labels
- **Dynamic Type**: Text scales with system settings
- **Accessibility Traits**: Proper semantic roles
- **Accessibility Hints**: Contextual help text
- **Minimum Touch Targets**: 44x44pt minimum

## Thread Safety

- Initialization uses `NSLock` for thread safety
- UI operations automatically dispatch to main queue
- Singleton pattern ensures single instance
- No shared mutable state without synchronization

## Performance Considerations

- Lazy loading of view controllers
- Efficient constraint management
- Image caching and reuse
- Minimal memory footprint
- Optimized for 60fps animations
