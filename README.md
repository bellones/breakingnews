# OobeoApp hardcoded secrets

**Type:** Bug / Security  
**Priority:** Critical  
**Component:** OobeoApp (React Native)  
**Labels:** security, hardcoded-secret, technical-debt

## Summary

OobeoApp ships multiple live credentials hardcoded in source and native config, rather than injected via secure runtime / build-time config. All of them are already in git history and several are extractable from a decompiled APK/IPA. The first find was a PlateRecognizer ALPR token in `LicenseScanner.js`; a follow-up scan of the same repo found additional secrets of equal or higher severity, including a Firebase Admin private key and the Android release keystore plus its passwords.

## Details

**Discovered:** during an unrelated migration-analysis exploration of the OobeoApp codebase, then confirmed with a full-repo secrets scan on 2026-08-17.

The original ticket asked to follow “the existing pattern” of `react-native-dotenv` + EAS build-time env injection, consistent with `app/constants.js`. That pattern is only half-implemented today:

- `react-native-dotenv` is installed and wired in `babel.config.js`.
- `app/constants.js` has `// const target = TARGET;` commented out and hardcodes `const target = 'production'`.
- `OobeoApp/.env` is **committed** and is **not** in `.gitignore`. It currently only contains `TARGET=local`. Any new secret written there will leak again.
- `eas.json` only injects `TARGET` and Square application IDs (public identifiers).

Hardcoded credentials confirmed in tree (values truncated — do not paste full secrets into tickets, Slack, or git):

| Secret | File(s) | What it is |
|---|---|---|
| PlateRecognizer API token | `app/components/LicenseScanner.js` | `Authorization: Token b5ccb6f1...` on `https://api.platerecognizer.com/v1/plate-reader/` |
| Firebase Admin SDK private key | `OobeoApp/oobeo-valetware-firebase-adminsdk-icgk0-4d28c6a06c.json` | Server service-account key for `firebase-adminsdk-icgk0@oobeo-valetware.iam.gserviceaccount.com`. Not used by the mobile app at runtime — should never have been in this repo. |
| Android release keystore + passwords | `android/app/keystore.jks` (tracked; `.gitignore` has `*.keystore` but not `*.jks`); `android/gradle.properties` (`MYAPP_RELEASE_STORE_PASSWORD` / `MYAPP_RELEASE_KEY_PASSWORD` = `9u2dxfpvaa1...`) | Full Play-signing material. `build.gradle` release signing reads these properties. |
| OAuth client id + secret | `app/constants.js` (`API_CLIENT_ID: 'Xav3ui75y7uZJ20v...'`, `API_CLIENT_SECRET: '8Pt5JQjJb3TUGPCi...'`) | Password-grant client used in `app/actions/userData.js` for login and token refresh. |
| Airship app secrets | `ios/AirshipConfig.plist`; `android/app/src/main/assets/airshipconfig.properties` (`NySOJCGY...`, `EGQz_0TR...`) | Production and development app key + app secret. |
| Square Reader SDK Maven password | `android/gradle.properties` (`SQUARE_READER_SDK_REPOSITORY_PASSWORD=boypbcwa5sfo...`) | Download credential for Square’s private artifact repo (not a payment token). The Square application id next to it is a public identifier. |

Out of scope for this ticket (public by design — restrict in the vendor console if needed, do not treat as secrets):

- Pusher client keys in `app/constants.js`
- Square application ids in `eas.json` / `build.gradle`
- Bugsnag `apiKey` in `Info.plist` / `AndroidManifest.xml`
- Firebase/Google client `AIza...` keys in `google-services.json` / `GoogleService-Info.plist`
- Standard Android `debug.keystore` + password `android`

## Risk

- Every item above is already in git history, so rotation alone is not sufficient — the exposure predates any fix.
- Anyone with source or build access (including a decompiled release APK/IPA) can extract client-side tokens (PlateRecognizer, OAuth client secret, Airship secrets) and reuse them.
- The Firebase Admin JSON is a **server** credential: repo access is enough to act as admin on project `oobeo-valetware` (Auth, Firestore, Storage, etc.).
- The release keystore **and** its passwords together allow signing APKs as `com.oobeo.valet`.
- The Square Maven password allows pulling Reader SDK artifacts under this account.
- Writing replacements into the committed `.env` would create a new leak.

## Acceptance Criteria

- All hardcoded secrets listed above are removed from source, native config, and git tracking (`LicenseScanner.js`, `constants.js`, `gradle.properties`, `keystore.jks`, Airship plist/properties, Firebase Admin JSON).
- Secrets are sourced from secure runtime / build-time config instead:
  - JS secrets (PlateRecognizer token, `API_CLIENT_ID`, `API_CLIENT_SECRET`, `TARGET`): finish `react-native-dotenv` + EAS env injection; re-enable `import { TARGET } from '@env'`.
  - Android signing + Square Maven password: EAS secrets / CI env / uncommitted `~/.gradle/gradle.properties`.
  - Airship: EAS env / Xcode build settings / generated gitignored `airshipconfig.properties`.
  - Firebase Admin JSON: do not ship in this repo at all; keep only in the backend/CI secret store if still needed.
- `.gitignore` updated for `.env`, `*.jks`, `*firebase-adminsdk*.json`, and `*-adminsdk-*.json`. Commit only `.env.example` with empty placeholders.
- No plaintext secret remains anywhere in the diff or in new config files committed to the repo.
- Vendor-side rotation (cannot be done from the codebase alone — flag to each vendor-account owner):
  - PlateRecognizer: revoke/rotate the exposed token; provision the new token only via env.
  - Firebase: disable/delete the leaked service account; issue a new one only to the backend/CI secret store.
  - Play Console: if Play App Signing is in use, rotate the upload key (keep the Play-held app key); otherwise enroll in Play App Signing and rotate the upload key.
  - API/Django: revoke/rotate the OAuth client; provision the new pair only via EAS/CI secrets.
  - Airship: rotate production and development app secrets; provision only via the secure config path.
  - Square: rotate the Reader SDK repository password; provision only via the secure config path.
- Confirm a staging build still works for: license-plate scan, login + token refresh, Android signing/install, push notifications, and Square Reader SDK dependency resolution.

## Notes

Suggested order of vendor rotation (highest blast radius first): Firebase Admin → Android keystore / Play upload key → OAuth client → Airship → Square Maven password → PlateRecognizer.

Mobile OAuth secrets remain extractable from a binary even after env injection. Rotation + env injection stops **repo** leakage; a follow-up should move this app to a public client / PKCE and keep the confidential client on the server only.
