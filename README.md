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
3. EAS + CI preview + TestFlight
4. New gateway endpoints in `dpay.dpay-poc-main`, then mobile follow-ups

---

## 1. UI polish

### dPay Home polish (rest of screen)

**Description**  
Finish Home against PdW 18. Header (Drumwave orb + frosted bar) is already DWLLT-3169. Align title/subtitle, `StatsRow`, CTAs, orbit, and bottom nav with Figma.

**Out of scope:** `GET /wallet/me` (separate ticket).

**Dependencies:** DWLLT-3169

---

### dPay Login polish

**Description**  
Match `/login` to Figma (form, wordmark, Face ID row, typography). Landing Sign in already uses Titillium 18 / `#070707` via `labelClassName`. Login still uses the default `Button` label.

**Dependencies:** DWLLT-3169

---

### dPay Scan to Pay polish

**Description**  
Figma pass on `/pay/scan`: “QR code” header, Scan / Show tabs, camera viewport + frame, spinner, invalid/error/retry states.

**Out of scope:** settle / live confirmation mapping.

**Dependencies:** DWLLT-3096

---

### dPay Show to Pay polish

**Description**  
Figma pass on the Show tab: white QR card, countdown, cashier hint, Paying from / `Fund` row. Payload stays mocked until the consumer QR API exists.

**Dependencies:** DWLLT-3097

---

### dPay Confirmation polish

**Description**  
Figma pass on `/pay/confirmation`: success orb, display title, order-details card, “Show my receipt” CTA. Receipt fields stay mocked until settle/receipt wiring.

**Dependencies:** DWLLT-3157

---

### dPay inverse Button default (optional)

**Description**  
Promote landing Sign in typography (Titillium Semibold 18px, tracking `-0.72px`, `#070707`) to all `variant="inverse"` buttons so Home / login / confirmation match without per-screen `labelClassName`.

**Dependencies:** DWLLT-3169

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

**Dependencies:** Product: list vs single receipt. Confirmation polish optional.

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
Call register/send instead of SecureStore-only + local Coupa demo. Real Expo tokens need an EAS `projectId` and a dev/prod build (not Expo Go).

**Dependencies:** Push register/send + EAS project ticket.

---

### Reset password UI

**Description**  
Add the Reset control on `/login` once the auth endpoint exists.

**Dependencies:** Password reset endpoint.

---

## 5. CI/CD

CI today is static only. There is **no** `eas.json`. Device installs today are local Xcode (Drumwave team PLA / personal team workaround).

### EAS project + eas.json

**Description**  
Create the Expo/EAS project, add `eas.json` (`development` / `preview` / `production`), set `projectId` in `app.config.ts`. Required for remote push tokens and any cloud build.

**Dependencies:** none (unblocks push + preview builds).

---

### CI: EAS preview build

**Description**  
On PR or `main`, run `eas build` (iOS first). Distribute an internal install so reviewers are not blocked on local signing.

**Dependencies:** EAS project + Apple team / PLA / device registration.

---

### CD: TestFlight (and Play later)

**Description**  
Submit the production/preview profile to TestFlight after merge to `main`. Android Play internal is a follow-up if Android is v1.

**Dependencies:** EAS preview build + signing.

---

### Apple signing / PLA / devices

**Description**  
Account Holder accepts the current Program License Agreement. Register physical devices on team `BCPQ5494A7`. Without this, Drumwave-signed `expo run:ios --device` and EAS iOS fail (personal team is a local workaround only).

**Dependencies:** none. Blocks CI iOS + TestFlight.

---

### CI: CodeArtifact (only if needed)

**Description**  
Add registry auth to GitHub Actions if `pnpm install --frozen-lockfile` starts failing on private packages. Workspace packages (`@data-reserve/*`) do not need this today.

**Dependencies:** only if CI install breaks.

---

### Android first-class / Play internal (optional)

**Description**  
Treat Android as a v1 target: EAS Android profile, emulator/device runbook, Play internal track.

**Dependencies:** EAS project. Product: iOS-only v1 vs both.

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
dPay Home polish (rest of screen)
dPay Login polish
dPay Scan to Pay polish
dPay Show to Pay polish
dPay Confirmation polish
dPay inverse Button default
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
EAS project + eas.json
CI: EAS preview build
CD: TestFlight
Apple signing / PLA / devices
```
