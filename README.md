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
2. Wire existing gateway APIs (wallet, settle, confirmation, history)
3. Fastlane + GitHub Actions (build → TestFlight)
4. New gateway endpoints in `dpay.dpay-poc-main`, then mobile follow-ups

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

## 2. Backend integration — mobile (gateway already exists)

Client already has `getWallet` / `getTransactions`. Home does not call them. `useWalletQueries` and `mapTransactionHistory` exist. **Settle is not on `GatewayClient`.**

### Wire GET /wallet/me on Home

**Description**  
Render wallet / plan balances from `GET /wallet/me` instead of only `mockHome.ts` orbit + request counts. Product must decide: wallet strip vs PdW pending/accepted/rejected (ownership is not on this gateway).

**Dependencies:** DWLLT-3095. Product decision.

---

### Settle after scan claim

**Description**  
Add `settleSession` → `POST /wallet/session/:id/settle`. After a live `GET /wallet/session/:id` claim, settle (auto vs plan-picker / review is a product decision). Mock scan may keep the 2s demo until confirmation uses real data.

**Do not** invent new paths. Settle body is already in the API contract (`plan` / `plans` / `external`). `{ source: "wallet" }` is rejected.

**Dependencies:** DWLLT-3096. Product: review vs auto-settle.

---

### Confirmation from settle / transaction

**Description**  
Stop using `generateMockPurchaseConfirmation()` (Coupa Cafe / `$3.25` / `Ð0.12`). Map vendor, tx id, method, amount, data value from settle response or `GET /wallet/transactions` / ledger.

**Dependencies:** Settle ticket, **or** gateway receipt DTO if settle/tx is not enough.

---

### Transaction history / receipt destination

**Description**  
“Show my receipt” today goes to `/requests` placeholder. Use `GET /wallet/transactions` (mapper already in `mapTransactionHistory`) and optionally `GET /wallet/ledger/:id`. Add ledger to `GatewayClient` only if the screen needs it.

**Dependencies:** Product: list vs single receipt. Remaining screen polish optional.

---

### Session WebSocket (optional v1)

**Description**  
`EXPO_PUBLIC_DPAY_WS_URL` (`…/ws/session`) is documented and unused. Subscribe for SETTLING → SETTLED / FAILED instead of only navigating after settle.

**Dependencies:** Settle ticket. Product: WS vs poll.

---

## 3. Backend gaps — create in `dpay.dpay-poc-main` first

Do **not** add guessed paths to `GatewayClient`.

### Consumer Show to Pay QR + refresh

**Description**  
POC is merchant-presented QR only. Need an endpoint to create/refresh a **customer-presented** display QR + `expiresAt`. Until then Show to Pay stays mocked.

**Blocks:** mobile “Wire Show to Pay to gateway”.

---

### Confirmation / receipt DTO

**Description**  
Either a consumer receipt/order-detail payload or a written decision to reuse settle + transaction/ledger. Confirmation cannot leave the Coupa fixture without this or a complete settle mapping.

**Blocks:** “Confirmation from settle / transaction” if existing shapes are insufficient.

---

### Device push register / send

**Description**  
Register/unregister Expo device token and send a pay-ready payload `{ deepLink, merchantName, sessionId }`. Mobile already persists `dpay.devicePushToken` locally. Demo is a **local** notification.

**Blocks:** mobile “Wire remote push”.

---

### Password reset endpoint

**Description**  
POC has no forgot-password API. Ticket 5 omitted the control on purpose.

**Blocks:** mobile “Reset password UI”.

---

## 4. Mobile follow-ups (after gateway tickets)

### Wire Show to Pay to gateway

**Description**  
Replace `generateMockShowToPay()` with the consumer QR API. Keep 5:00 refresh if the backend returns `expiresAt`.

**Dependencies:** Consumer Show to Pay QR ticket.

---

### Wire remote push

**Description**  
Call register/send instead of SecureStore-only + local Coupa demo. Real Expo push tokens still need an EAS `projectId` (push only — builds stay on Fastlane) and a signed dev/prod binary (not Expo Go).

**Dependencies:** Push register/send + Fastlane iOS build (or a local signed binary).

---

### Reset password UI

**Description**  
Add the Reset control on `/login` once the auth endpoint exists.

**Dependencies:** Password reset endpoint.

---

## 5. CI/CD (Fastlane)

CI today is static only (typecheck / lint / test). Native `ios/` already exists (`com.drumwave.dpay`, team `BCPQ5494A7`). Device installs are local Xcode (Drumwave PLA / personal-team workaround).

**Decision:** ship and sign with **Fastlane** (`match` + `gym` + `pilot`), not EAS Build. Expo stays the app framework; Fastlane owns archive and store upload.

### Fastlane iOS (match + gym + pilot)

**Description**  
Add `fastlane/` (Gemfile, `Fastfile`, `Appfile`, `Matchfile`). Lanes:

- `ios build` — `gym` Debug/Release from `ios/dPay.xcworkspace` / scheme `dPay`
- `ios beta` — `match` (appstore) → `gym` → `pilot` (TestFlight)
- `ios certificates` — `match` development + appstore for team `BCPQ5494A7`

Use existing bundle id `com.drumwave.dpay`. Do not switch CI to the personal team.

**Dependencies:** Apple signing / PLA / devices. Match repo or S3 + App Store Connect API key in GitHub secrets.

---

### CI: Fastlane build on GitHub Actions

**Description**  
`macos` job on PR and/or `main`: `pnpm install` → `pod install` → `bundle exec fastlane ios build` (or `ios beta` on `main`). Cache Pods / DerivedData. Keep the existing static-checks job.

**Dependencies:** Fastlane iOS ticket + signing secrets (`MATCH_PASSWORD`, `APP_STORE_CONNECT_API_KEY`, match git URL).

---

### CD: TestFlight via Fastlane

**Description**  
On merge to `main`, run `fastlane ios beta` and upload to TestFlight (`pilot`). Internal testers get a build without local Xcode.

**Dependencies:** Fastlane iOS + CI job + signing.

---

### Apple signing / PLA / devices

**Description**  
Account Holder accepts the current Program License Agreement. Register devices and let `match` create/sync profiles for `BCPQ5494A7`. Without this, `expo run:ios --device` and Fastlane `gym`/`pilot` fail (personal team is a local workaround only).

**Dependencies:** none. Blocks Fastlane iOS + TestFlight.

---

### CI: CodeArtifact (only if needed)

**Description**  
Add registry auth to GitHub Actions if `pnpm install --frozen-lockfile` starts failing on private packages. Workspace packages (`@data-reserve/*`) do not need this today.

**Dependencies:** only if CI install breaks.

---

### Android Fastlane / Play internal (optional)

**Description**  
If Android is v1: Fastlane `gradle` + `supply` (or `upload_to_play_store`) for an internal track. Product: iOS-only v1 vs both.

**Dependencies:** Fastlane iOS first (same lane conventions). Product decision.

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
Settle after scan claim
Confirmation from settle / transaction
Transaction history / receipt destination
Session WebSocket (optional)
[POC] Consumer Show to Pay QR + refresh
[POC] Confirmation / receipt DTO
[POC] Device push register / send
[POC] Password reset endpoint
Wire Show to Pay to gateway
Wire remote push
Reset password UI
Fastlane iOS (match + gym + pilot)
CI: Fastlane build on GitHub Actions
CD: TestFlight via Fastlane
Apple signing / PLA / devices
```
