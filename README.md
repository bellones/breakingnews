# Report: Firebase Apple SDK and CocoaPods sunset — impact on Valet Mate Parker (React Native)

## Executive summary

Firebase will **stop publishing new versions of the Apple (iOS) SDK on CocoaPods after October 2026**. Older CocoaPods releases stay available, but **you stop getting new native iOS Firebase releases** if you stay on CocoaPods-only integration.

This app **does integrate the Firebase Apple SDK through CocoaPods** today (via an explicit `Podfile` and **React Native Firebase**). So the announcement **applies to the iOS side** of this project. It does **not** change **Android** Gradle-based Firebase setup.

The **“uplink-chain-stg / uplink-chain-dev / uplink-chain”** names in the email are **Firebase/Google Cloud project identifiers** in the console; they may or may not match this repo’s name. **Compliance is per app / per iOS integration**, not per git repo label.

---

## Current state in this codebase

1. **JavaScript layer**  
   - Uses **`@react-native-firebase/app`**, **analytics**, **crashlytics**, and **messaging** (v20.3.0 in `package.json`).

2. **iOS native layer**  
   - **`ios/Podfile`** declares **CocoaPods** dependencies, including:
     - `Firebase`, `FirebaseCore`, `GoogleUtilities` (with modular headers).
   - **`ios/Podfile.lock`** resolves the **Firebase Apple SDK** (e.g. **10.29.0**) and related pods (**FirebaseAnalytics**, **FirebaseCrashlytics**, **FirebaseMessaging**, etc.).

3. **Swift bootstrap**  
   - `AppDelegate.swift` imports **`Firebase`** and calls **`FirebaseApp.configure()`**.

4. **SPM**  
   - There is **no** root `Package.swift` for app dependencies; iOS Firebase today is **CocoaPods-driven**.

**Conclusion:** This app is in the category **“Apple platform project using Firebase Apple SDK via CocoaPods”** for iOS. The email’s carve-out for “if you’re not using CocoaPods” **does not apply** to your iOS build.

---

## Impact (by timeline)

| Period | Effect on this app |
|--------|-------------------|
| **From ~May 2026** | Possible **deprecation warnings** during `pod install` / `pod update`. **Builds should still work.** |
| **After October 2026** | **No new Firebase Apple SDK versions** published to CocoaPods. Staying on CocoaPods means **no new native iOS Firebase features, fixes, or security updates** through pod version bumps. |
| **December 2026 (registry read-only)** | Broader CocoaPods ecosystem change; **existing** Firebase pod specs that were already published **remain** usable for installs that don’t need unpublished updates — but **Firebase’s policy** is the limiting factor for *new* SDK releases, not only the registry. |

**Android:** Unaffected by this **CocoaPods** decision; your **`google-services.json`**, Crashlytics Gradle plugin, etc. continue on the Android path unless Google announces separate changes.

---

## What you need to do to stay aligned

1. **Treat this as an iOS delivery / dependency strategy project (before October 2026).**  
   Goal: ensure future **native** Firebase updates reach the app **without** relying on new CocoaPods drops.

2. **Follow upstream guidance in order of ownership:**  
   - **Firebase:** official migration to **Swift Package Manager** or **manual** install of the Apple SDK.  
   - **React Native Firebase:** how `@react-native-firebase/*` will consume Firebase on iOS once CocoaPods is no longer the shipping channel for new SDK versions (their docs/changelog/issues are the source of truth for RN-specific steps).

3. **Plan realistically for React Native**  
   Migration is not only “add SPM in Xcode”; it must remain compatible with **React Native’s** pod integration and **RNFirebase’s** native modules. Typically you will:  
   - Upgrade **React Native Firebase** (and React Native) to versions that support the **supported** Firebase install method;  
   - Adjust **iOS** integration (Pods vs SPM / hybrid) per those versions’ docs;  
   - Re-verify **Analytics, Crashlytics, Messaging**, and **`AppDelegate`** / **push** setup after the change.

4. **Operational housekeeping**  
   - Map which **Firebase console projects** (`uplink-chain-*` vs others) own **this** app’s `GoogleService-Info.plist` / bundle ID — so owners know which commercial “projects” must be migrated in lockstep.  
   - CI: expect **noisy but non-fatal** pod warnings from May 2026 until you migrate.

5. **If you stay on CocoaPods past October 2026 without migrating**  
   The app **can still build** using **already-published** pods, but **you accept stagnation and risk** on the native Firebase stack (bugs, Apple OS changes, future compliance).

---

## Summary

| Topic | Detail |
|-------|--------|
| **Impact** | **High for long-term iOS maintainability**, **low for immediate breakage**. |
| **iOS** | Stack **today depends on Firebase via CocoaPods**; migrating **before October 2026** is required for **ongoing Firebase Apple SDK updates**. |
| **Action** | Plan an **iOS migration path** coordinated with **`@react-native-firebase`** releases and Firebase’s migration guide. |
| **Android** | **Not** impacted by this specific CocoaPods change. |

---

## Reference: original communication (high level)

- **May 2026 onward:** Possible deprecation warnings on `pod install` / `pod update` (non-breaking).
- **October 2026:** Firebase stops publishing **new** versions to CocoaPods.
- **December 2, 2026:** Public CocoaPods registry moves to **read-only** mode.
- **Action:** Migrate Apple projects using Firebase Apple SDK via CocoaPods to **Swift Package Manager** or **manual installation**.

---

*Generated for repository: `valet-mate-parker`. Content reflects codebase state at time of writing (`ios/Podfile`, `ios/Podfile.lock`, `package.json`, `AppDelegate.swift`).*
