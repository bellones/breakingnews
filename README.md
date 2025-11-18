# App Lifecycle & Organization Management Flow

## App Uninstall Detection & Profile Removal

```mermaid
sequenceDiagram
    participant Device
    participant UninstallDetector
    participant ProfileRemover
    participant PlatformAPI
    participant BackendAPI
    participant Logger
    
    Note over Device: App uninstalled
    
    Device->>UninstallDetector: Detect uninstall
    Note over UninstallDetector: Platform-specific detection:<br/>Android: Broadcast receiver<br/>iOS: App extension
    
    UninstallDetector->>ProfileRemover: removeAllProfiles()
    ProfileRemover->>PlatformAPI: List installed profiles
    PlatformAPI-->>ProfileRemover: Uplink profiles found
    
    loop For each Uplink profile
        ProfileRemover->>PlatformAPI: Remove profile
        PlatformAPI-->>ProfileRemover: Removal success
        ProfileRemover->>BackendAPI: POST /profiles/{id}/removed
        Note over BackendAPI: Reason: App uninstalled
    end
    
    ProfileRemover->>Logger: Log uninstall cleanup
    Logger->>BackendAPI: Upload final logs
```

## Organization Status Monitoring Flow

```mermaid
sequenceDiagram
    participant ProfileManager
    participant BackendAPI
    participant OrganizationService
    participant ProfileRemover
    participant PlatformAPI
    participant App
    
    ProfileManager->>BackendAPI: GET /profiles/{subscriberId}/update
    BackendAPI->>OrganizationService: Check organization status
    OrganizationService-->>BackendAPI: Organization status
    
    alt Organization Blocked or Removed
        BackendAPI-->>ProfileManager: Organization status: BLOCKED/REMOVED
        ProfileManager->>ProfileRemover: removeAllProfiles()
        ProfileRemover->>PlatformAPI: Remove all Uplink profiles
        PlatformAPI-->>ProfileRemover: Profiles removed
        ProfileRemover->>BackendAPI: POST /profiles/removed
        Note over BackendAPI: Reason: Organization blocked
        ProfileManager->>App: Notify profile removal
        Note over App: Show user message
    else Organization Active
        BackendAPI-->>ProfileManager: Profile update available
        ProfileManager->>ProfileManager: Continue with update
    end
```

## Background Operation Handling

```mermaid
graph TD
    A[Background Operation Requested] --> B{Background Restrictions?}
    B -->|No Restrictions| C[Execute Operation]
    B -->|Restricted| D[Queue Operation]
    
    C --> E[Operation Complete]
    
    D --> F[Wait for App Foreground]
    F --> G[App Foregrounded]
    G --> H[Process Queued Operations]
    H --> I{Operation Type?}
    I -->|Profile Update| J[Download & Install]
    I -->|Renewal| K[Renew Profile]
    I -->|Polling| L[Check for Updates]
    
    J --> E
    K --> E
    L --> E
```

## Profile Information Display Flow

```mermaid
sequenceDiagram
    participant App
    participant ProfileManager
    participant LocalStorage
    participant BackendAPI
    
    App->>ProfileManager: getProfileInfo()
    ProfileManager->>LocalStorage: Get cached profile info
    
    alt Cache Valid
        LocalStorage-->>ProfileManager: Profile info
        ProfileManager-->>App: Profile details
    else Cache Stale or Missing
        ProfileManager->>BackendAPI: GET /profiles/{id}/info
        BackendAPI-->>ProfileManager: Current profile info
        ProfileManager->>LocalStorage: Update cache
        ProfileManager-->>App: Profile details
    end
    
    Note over App: Display in settings:<br/>- Installation date<br/>- Expiration date<br/>- Status<br/>- Version
```


