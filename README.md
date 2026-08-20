# dPay Mobile — remaining Jira tickets

Backlog to create under the dPay mobile epic. Do **not** reopen tickets that already shipped.

Figma: [PdW 18 — dPay](https://www.figma.com/design/RYwByrYgpdMaOLrZt1FYPu/PdW-18--dPay-?node-id=1098-164918&p=f&m=dev)  
Gateway contract: [DPAY_POC_API_CONTRACT.md](./DPAY_POC_API_CONTRACT.md)  
CI today: `.github/workflows/ci.yml` (install + typecheck + lint + test on PR/`main`)

## Already shipped — do not recreate

| Ticket | Scope |
|---|---|
| DWLLT-3090 | Bootstrap, tokens, platform shell |
| DWLLT-3091 | API client, auth session, Face ID |
| DWLLT-3092 | Design system / shared UI |
| DWLLT-3094 | Landing + Login (reset password omitted) |
| DWLLT-3095 | Home dashboard |
| DWLLT-3096 | Scan to Pay |
| DWLLT-3097 | Show to Pay (mocked QR) |
| DWLLT-3098 | Push + deep link (local token / local demo) |
| DWLLT-3099 | Shared types, i18n, tests |
| DWLLT-3157 | Purchase confirmation (mock receipt) |
| DWLLT-3169 | Landing + Home **header** polish only |

## Suggested order

1. UI polish leftovers (no backend)
2. Live pay (settle + confirmation + receipt)
3. Fastlane + GitHub Actions (build → TestFlight)
4. Show to Pay QR (POC + mobile) and push / reset password (POC + mobile)

---

## 1. UI polish

### dPay remaining screen polish

**Description**  
Single Figma pass for everything DWLLT-3169 did **not** cover. Landing wordmark / `PERSONAL` / Sign in and the Home header (Drumwave orb + frosted bar) stay out — they are already shipped.

Include all of:

- **Home (rest):** title/subtitle, `StatsRow`, CTAs, orbit, bottom nav
- **Login:** form, wordmark, Face ID row, typography
- **Scan to Pay:** “QR code” header, Scan / Show tabs, camera viewport + frame, spinner, invalid/error/retry
- **Show to Pay:** white QR card, countdown, cashier hint, Paying from / `Fund` row (payload stays mocked)
- **Confirmation:** success orb, display title, order-details card, “Show my receipt” CTA (fields stay mocked)
- **Inverse `Button` default:** promote landing Sign in type (Titillium Semibold 18px, tracking `-0.72px`, `#070707`) so Home / login / confirmation do not need per-screen `labelClassName`

**Out of scope:** `GET /wallet/me`, settle, live confirmation mapping, consumer QR API.

**Dependencies:** DWLLT-3169, DWLLT-3094, DWLLT-3096, DWLLT-3097, DWLLT-3157

---

## 2. Backend — three tickets

Client already has `getWallet` / `getTransactions`. Home does not call them. `useWalletQueries` and `mapTransactionHistory` exist. **Settle is not on `GatewayClient`.** Do **not** invent new paths.

`GET /wallet/me` stays its own small ticket (not in the three below).

### Wire GET /wallet/me on Home

**Description**  
Render wallet / plan balances from `GET /wallet/me` instead of only `mockHome.ts` orbit + request counts. Product must decide: wallet strip vs PdW pending/accepted/rejected (ownership is not on this gateway).

**Dependencies:** DWLLT-3095. Product decision.

---

### 1 — dPay live pay (settle + confirmation + receipt)

**Repos:** `dwallet.dpay-mobile` (and POC only if settle/tx cannot fill the confirmation card).

**Description**  
Close the live Scan → pay → receipt path in one ticket:

- Add `settleSession` → `POST /wallet/session/:id/settle` after a live claim. Body is already in the contract (`plan` / `plans` / `external`). `{ source: "wallet" }` is rejected. Product: auto-settle vs plan-picker / review.
- Stop `generateMockPurchaseConfirmation()` (Coupa Cafe / `$3.25` / `Ð0.12`). Map vendor, tx id, method, amount, data value from settle and/or `GET /wallet/transactions` / ledger.
- “Show my receipt” leaves `/requests`. Use `GET /wallet/transactions` (`mapTransactionHistory` exists). Add `GET /wallet/ledger/:id` to the client only if the screen needs it.
- Receipt DTO: **reuse settle + tx/ledger**. Open a POC endpoint only if those shapes are not enough — do not guess a path.

**Out of scope:** session WebSocket (`EXPO_PUBLIC_DPAY_WS_URL`). Navigate after settle; WS later if needed.

**Dependencies:** DWLLT-3096, DWLLT-3157. Product: review vs auto-settle; list vs single receipt.

---

### 2 — Show to Pay consumer QR (POC + mobile)

**Repos:** `dpay.dpay-poc-main` first, then `dwallet.dpay-mobile`.

**Description**  
POC is merchant-presented QR only. Add an endpoint to create/refresh a **customer-presented** display QR + `expiresAt`. Then replace `generateMockShowToPay()` on the Show tab. Keep a ~5:00 refresh if the backend returns `expiresAt`.

Until the endpoint exists, Show to Pay stays mocked. Do not add a guessed path to `GatewayClient`.

**Dependencies:** DWLLT-3097. Backend before mobile wire.

---

### 3 — Push register/send and password reset (POC + mobile)

**Repos:** `dpay.dpay-poc-main` first, then `dwallet.dpay-mobile`.

**Description**  
Two remaining auth/notify gaps, same ticket:

- **Push:** register/unregister Expo device token and send `{ deepLink, merchantName, sessionId }`. Mobile already stores `dpay.devicePushToken`. Then replace the local Coupa demo with register/send. Real Expo tokens need an EAS `projectId` (push only — builds stay on Fastlane) and a signed binary (not Expo Go).
- **Password reset:** new POC auth endpoint, then the Reset control on `/login` (omitted in Ticket 5 on purpose).

Do not invent paths. Ship gateway first, then the mobile wires in the same ticket (or two PRs under this Jira).

**Dependencies:** DWLLT-3098, DWLLT-3094. Fastlane iOS build (or a local signed binary) for real push tokens.

---

## 5. CI/CD (Fastlane) — three tickets

CI today is static only (typecheck / lint / test). Native `ios/` already exists (`com.drumwave.dpay`, team `BCPQ5494A7`). Device installs are local Xcode (Drumwave PLA / personal-team workaround).

**Decision:** ship and sign with **Fastlane** (`match` + `gym` + `pilot`), not EAS Build. Expo stays the app framework; Fastlane owns archive and store upload.

### 1 — Apple signing / PLA / devices

**Description**  
Unblock Drumwave signing before any Fastlane lane.

- Account Holder accepts the current Program License Agreement
- Register physical devices on team `BCPQ5494A7`
- Decide match storage (git repo or S3) and App Store Connect API key

Without this, `expo run:ios --device`, `gym`, and `pilot` fail. Personal team is a local workaround only — do not use it in CI.

**Dependencies:** none. Blocks the two tickets below.

---

### 2 — Fastlane iOS CI/CD (match + gym + TestFlight)

**Description**  
One ticket for lanes + GitHub Actions + TestFlight:

- Add `fastlane/` (Gemfile, `Fastfile`, `Appfile`, `Matchfile`). Bundle id `com.drumwave.dpay`.
  - `ios build` — `gym` from `ios/dPay.xcworkspace` / scheme `dPay`
  - `ios beta` — `match` (appstore) → `gym` → `pilot`
  - `ios certificates` — `match` development + appstore for `BCPQ5494A7`
- `macos` job: `pnpm install` → `pod install` → `fastlane ios build` on PR; `fastlane ios beta` on `main`. Keep the existing static-checks job. Cache Pods / DerivedData.
- Secrets: `MATCH_PASSWORD`, `APP_STORE_CONNECT_API_KEY`, match git/S3 URL.
- CodeArtifact auth on Actions only if `pnpm install --frozen-lockfile` starts failing (workspace `@data-reserve/*` does not need it today).

**Dependencies:** Apple signing / PLA / devices.

---

### 3 — Android Fastlane / Play internal (optional)

**Description**  
If Android is v1: Fastlane `gradle` + `supply` (or `upload_to_play_store`) for an internal track, same lane conventions as iOS. Product: iOS-only v1 vs both.

**Dependencies:** Fastlane iOS CI/CD. Product decision.

---

## 6. Product decisions — do not open until decided

| Surface | Today | Open a ticket? |
|---|---|---|
| `/companies` `/requests` `/accounts` `/offers` | `PlaceholderScreen` | Only if in mobile v1 |
| In-app signup | `POST /auth/signup` on client, no screen | Only if consumer signup is in-app |
| Payment source picker | Fund `onEdit` is a no-op | After wallet + settle |
| `/pay/show` | Leftover placeholder | **No** — do not add UI there |

---

## Copy-paste titles

```text
dPay remaining screen polish
Wire GET /wallet/me on Home
dPay live pay (settle + confirmation + receipt)
Show to Pay consumer QR (POC + mobile)
Push register/send and password reset (POC + mobile)
Apple signing / PLA / devices
Fastlane iOS CI/CD (match + gym + TestFlight)
Android Fastlane / Play internal (optional)
```
