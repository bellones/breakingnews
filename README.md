# dPay Mobile — End-to-end flow & backend integration

**Author:** dPay mobile  
**Repo:** [`reddrummer/dwallet.dpay-mobile`](https://github.com/reddrummer/dwallet.dpay-mobile)

## Document Purpose

This document describes the end-to-end consumer flow in the dPay mobile app (Expo / React Native) and what still needs to be wired to the dPay POC gateway. It is intended for internal developers and system architects.

Default local mode is **mock** (`EXPO_PUBLIC_DPAY_USE_MOCK=true`). Set `false` to hit the live gateway.

## Implementation Status Legend

| Mark | Meaning |
|---|---|
| ✅ | Complete |
| 🚧 | In Progress |
| 🚨 | Important |
| 🪿 | Mocked |
| 🤷‍♂️ | Not started |
| ❓ | Needs Decision |

## Related Links

| Resource | Link |
|---|---|
| Figma | [PdW 18 — dPay](https://www.figma.com/design/RYwByrYgpdMaOLrZt1FYPu/PdW-18--dPay-?node-id=1098-164918&p=f&m=dev) |
| Architecture | [DPAY_MOBILE_ARCHITECTURE.md](./DPAY_MOBILE_ARCHITECTURE.md) |
| API contract | [DPAY_POC_API_CONTRACT.md](./DPAY_POC_API_CONTRACT.md) |
| Dev runbook | [DPAY_MOBILE_DEV_RUNBOOK.md](./DPAY_MOBILE_DEV_RUNBOOK.md) |
| Agent context | [AGENT_CONTEXT.md](./AGENT_CONTEXT.md) |
| QR scan handover | [QR_SCAN_HANDOVER.md](./QR_SCAN_HANDOVER.md) |
| Home handover | [PDW_HOME_HANDOVER.md](./PDW_HOME_HANDOVER.md) |
| POC gateway | [`reddrummer/dpay.dpay-poc`](https://github.com/reddrummer/dpay.dpay-poc) |
| Dev gateway | `https://dpay-poc-gateway.drumwave.dev/api` |
| Gateway Swagger | _TODO: paste POC swagger_ |

## Shipped tickets

| Ticket | Title | Status |
|---|---|---|
| [DWLLT-3090](https://drumwave.atlassian.net/browse/DWLLT-3090) | Bootstrap shell and platform tokens | ✅ Merged |
| [DWLLT-3091](https://drumwave.atlassian.net/browse/DWLLT-3091) | Mobile API client, auth session, Face ID | ✅ Merged |
| [DWLLT-3092](https://drumwave.atlassian.net/browse/DWLLT-3092) | dPay-Mobile design system & shared UI components | ✅ Merged |
| [DWLLT-3094](https://drumwave.atlassian.net/browse/DWLLT-3094) | dPay-Screen: Login / Landing | ✅ Merged |
| [DWLLT-3095](https://drumwave.atlassian.net/browse/DWLLT-3095) | dPay-Screen: Home Page | ✅ Merged |
| [DWLLT-3096](https://drumwave.atlassian.net/browse/DWLLT-3096) | dPay-QR Scan flow (camera + scanner + screen) | ✅ Merged |
| [DWLLT-3097](https://drumwave.atlassian.net/browse/DWLLT-3097) | dPay show to pay QR code screen | ✅ Merged |
| [DWLLT-3098](https://drumwave.atlassian.net/browse/DWLLT-3098) | dPay Push notifications & deep-link entry | ✅ Merged |
| [DWLLT-3099](https://drumwave.atlassian.net/browse/DWLLT-3099) | dPay shared types and tests | ✅ Merged |
| [DWLLT-3157](https://drumwave.atlassian.net/browse/DWLLT-3157) | dPay purchase confirmation screen | 🚧 [PR #10](https://github.com/reddrummer/dwallet.dpay-mobile/pull/10) |

## Happy path (what the app does today)

```text
Landing `/`
  → Login `/login`  (POST /auth/login, or any credentials in mock)
  → Face ID unlock on later launches (stored JWT + GET /auth/me)
  → Home `/home`
       ├─ QR FAB → Scan to Pay `/pay/scan`
       │     ├─ Mock: wait 2s → `/pay/confirmation`
       │     └─ Live: parse QR → GET /wallet/session/:id → `/pay/confirmation`
       ├─ Show to Pay tab (same route)
       │     └─ Mock QR + 5min countdown; after 2s → `/pay/confirmation`
       └─ Push demo (mock only): local banner → `/pay/scan`
  → Confirmation `/pay/confirmation`  (always mock receipt today)
       └─ Show my receipt → `/requests` (placeholder)
```

Validated path: **login → home → Show QR / scan QR → purchase confirmation**.

---

## 💻 UI Questions

| # | Page Screenshot | Questions & Answers w/ Endpoints | Jira Stories | Dependencies |
|---|---|---|---|---|
| 1 | _TODO: landing_ | **Landing `/`.** Sign in CTA. If a JWT is already in SecureStore and Face ID is enrolled, unlock here instead of going to the form. No gateway call on first visit. | [DWLLT-3094](https://drumwave.atlassian.net/browse/DWLLT-3094) Login / Landing — Done | ✅ Face ID uses stored session only. Does **not** replace `POST /auth/login` when the token is gone. |
| 2 | _TODO: login_ | **Login `/login`.** `POST /auth/login` `{ email, password }` → `{ token, user }`. CUSTOMER only. Mock: any credentials, prefilled `alex@example.com` / `demo`. **Reset password omitted** — POC has no forgot-password endpoint. | [DWLLT-3094](https://drumwave.atlassian.net/browse/DWLLT-3094) — Done | 🚨 Client also has `POST /auth/signup` but the UI does not expose signup. 🤷‍♂️ Reset password. |
| 3 | _TODO: home_ | **Home `/home`.** Greeting from session name. Orbit companies + request stats are **local mock** (`mockHome.ts`). CTAs: Request more data → `/companies`, View my requests → `/requests`. Client already has `GET /wallet/me` but Home does **not** render it. | [DWLLT-3095](https://drumwave.atlassian.net/browse/DWLLT-3095) Home — Done | 🪿 Orbit / pending-accepted-rejected. ❓ Should Home show `GET /wallet/me` (balance, plans, recent tx) or stay PdW-style request stats? Ownership APIs are **not** on the dPay gateway. |
| 4 | _TODO: scan_ | **Scan to Pay `/pay/scan`.** Camera (`expo-camera`) → `parsePayQr` (URL `/pay/{id}`, JSON `{ sessionId }`, or raw id ≥10). Live: `GET /wallet/session/:sessionId` (claim). `200` → confirmation. `403/409/410/404` → error, no success retry. Network/`408` → bounded retry (3). Mock: skip claim, wait 2s, go to confirmation. | [DWLLT-3096](https://drumwave.atlassian.net/browse/DWLLT-3096) QR Scan — Done | 🚨 Claim is a **state-changing GET**. Same customer retry while `SCANNED` is `200` (idempotent). **Settle is not called.** 🤷‍♂️ `POST /wallet/session/:id/settle`. |
| 5 | _TODO: show-to-pay_ | **Show to Pay** (tab on `/pay/scan`, not `/pay/show`). Local rotating QR (`show-to-pay` JSON), 5:00 countdown, Fund row (`dSavings smart` / `$50`). Mock: after 2s → confirmation. **No consumer display-QR API** on the gateway today. | [DWLLT-3097](https://drumwave.atlassian.net/browse/DWLLT-3097) Show to Pay — Done | 🪿 Entire panel. 🤷‍♂️ Backend ticket in `dpay.dpay-poc-main` for customer-presented QR + refresh. Edit source is a no-op. `/pay/show` is a leftover placeholder — do not add UI there. |
| 6 | _TODO: confirmation_ | **Purchase confirmation `/pay/confirmation`.** Success orb + order card: vendor, transaction ID, payment method, amount paid, data value earned. CTA **Show my receipt** → `/requests`. Always `generateMockPurchaseConfirmation()` (`Coupa Cafe`, `23SDYSK31`, `$3.25`, `Ð0.12`) — even after a live claim. | [DWLLT-3157](https://drumwave.atlassian.net/browse/DWLLT-3157) Confirmation — 🚧 PR | 🪿 Receipt fields. 🤷‍♂️ Consumer receipt / settled-payment detail endpoint. Do not invent one. |
| 7 | _TODO: push-demo_ | **Push + deep link.** Home asks notification permission and stores Expo token in SecureStore (`dpay.devicePushToken`). Simulator / Expo Go → `ExponentPushToken[mock-dpay-device]`. Mock CTA **Try a pay notification**: local banner after ~2s → `/pay/scan`. Allowlist: `/home`, `/companies`, `/requests`, `/accounts`, `/offers`, `/pay/scan`. Logged-out tap stashes href → `/login` → continue. | [DWLLT-3098](https://drumwave.atlassian.net/browse/DWLLT-3098) Push & deep link — Done | 🪿 Local notification only. 🤷‍♂️ Gateway device-token register / send. **Do not add a guessed `POST` to `GatewayClient`.** Remote tokens need an EAS `projectId` + dev build. |
| 8 | _TODO: placeholders_ | **Placeholder tabs.** `/companies`, `/requests`, `/accounts` (Savings), `/offers` — shell + bottom nav only. Receipt CTA and Home CTAs land here. | [DWLLT-3092](https://drumwave.atlassian.net/browse/DWLLT-3092) Design system — Done | 🤷‍♂️ Product screens. ❓ Which of these are in dPay mobile v1 vs PdW-only? |
| 9 | _TODO: design-system_ | **Shared UI.** `@data-reserve/mobile-components`: Header, TopNav, BottomNav, SegmentedTabs, StatsRow, Fund, ProfileOrbit, AnimatedGradientBackground, Button, Typography, Card. App screens wrap with i18n + router. | [DWLLT-3092](https://drumwave.atlassian.net/browse/DWLLT-3092) — Done | ✅ No web DOM imports (`design-system-primitives`, `dwallet-shared-components`). |
| 10 | — | **Shared types + i18n.** `@data-reserve/types` (wallet, session, QR, settle, notify). `@data-reserve/i18n` `en-US` / `pt-BR` `dpay` JSON + locale parity test. Not the web/Ditto package. | [DWLLT-3099](https://drumwave.atlassian.net/browse/DWLLT-3099) Shared types & tests — Done | ✅ Client re-exports types. |

---

## 🛠️ Domain Specific Questions

| # | Question | Endpoint / Approach | Tickets | Dependencies |
|---|---|---|---|---|
| 1 | After a successful scan claim, should mobile settle immediately or show a review / plan-picker first? Today we skip settle and jump to a mock receipt. | Existing: `POST /wallet/session/:sessionId/settle` `{ source: "plan", planCode }` or split / Apple/Google Pay. **Not in `GatewayClient` yet.** `{ source: "wallet" }` is rejected. | Mobile follow-up + existing POC settle | ❓ UX. 🤷‍♂️ Client method + UI. |
| 2 | What API generates the customer-presented Show to Pay QR and refresh timer? | **Does not exist.** Current POC is merchant-presented QR only (`GET /wallet/session/:id` after scan). Session `expiresAt` is merchant-side. | Backend in `dpay.dpay-poc-main` | ❓ Path/payload (`POST /wallet/display-qr` or similar). Do not invent the path in the app. |
| 3 | What API fills purchase confirmation (vendor, tx id, method, amount, Ð earned)? | **Does not exist.** Closest existing: settle response / `GET /wallet/transactions` / `GET /wallet/ledger/:transactionId`. Confirmation screen does not call any of these. | [DWLLT-3157](https://drumwave.atlassian.net/browse/DWLLT-3157) + backend receipt | ❓ New receipt DTO vs reuse transaction summary. |
| 4 | How does the device register for remote pay push? | **Does not exist.** Token stays in SecureStore only. | [DWLLT-3098](https://drumwave.atlassian.net/browse/DWLLT-3098) + backend register/send | ❓ Gateway vs Expo/APNs/FCM service. Out of POC scope today. |
| 5 | Should Home use wallet data or keep PdW request stats? | Client: `GET /wallet/me` (balance, plans, recent tx). UI: `mockHome.ts` orbit + pending/accepted/rejected. | [DWLLT-3095](https://drumwave.atlassian.net/browse/DWLLT-3095) | ❓ Product. Ownership/search is not on this gateway. |
| 6 | Forgot / reset password? | **No POC endpoint.** Ticket 5 omitted the control on purpose. | [DWLLT-3094](https://drumwave.atlassian.net/browse/DWLLT-3094) | 🤷‍♂️ Backend first. |
| 7 | Live session updates (cashier settled, expired)? | POC WebSocket `…/ws/session`. Env `EXPO_PUBLIC_DPAY_WS_URL` documented, **not wired**. | Architecture Ticket 1 | ❓ WS vs poll `GET /wallet/session/:id` vs POS-only. |
| 8 | Where does “Show my receipt” go? | Today `/requests` placeholder. History API exists: `GET /wallet/transactions`. | [DWLLT-3157](https://drumwave.atlassian.net/browse/DWLLT-3157) | ❓ Receipt list vs single ledger `GET /wallet/ledger/:transactionId`. |
| 9 | Signup in the consumer app? | `POST /auth/signup` is on the client. Login UI is credentials-only. | [DWLLT-3091](https://drumwave.atlassian.net/browse/DWLLT-3091) | ❓ In-app signup vs POC web only. |
| 10 | Change payment source on Show to Pay? | Fund `onEdit` is a no-op. Settle already supports `planCode` / allocations. | [DWLLT-3097](https://drumwave.atlassian.net/browse/DWLLT-3097) | 🤷‍♂️ Source picker UI + live plans from `GET /wallet/me`. |

---

## Next steps — backend integration

Order is the shortest path from “demo UI” to “real pay”. Do **not** add guessed paths to `GatewayClient` before the gateway ships them.

### 1. Wire what already exists on the gateway

| Step | Mobile work | Gateway | Status |
|---|---|---|---|
| 1.1 | Call `GET /wallet/me` on Home (or a wallet strip) instead of only `mockHome.ts` | ✅ Exists | 🤷‍♂️ |
| 1.2 | Add `settleSession` to `GatewayClient` → `POST /wallet/session/:id/settle` after claim | ✅ Exists | 🤷‍♂️ |
| 1.3 | Decide review vs auto-settle, then navigate to confirmation with **settle/tx payload** (not `generateMockPurchaseConfirmation`) | ✅ Settle body documented | ❓ + 🤷‍♂️ |
| 1.4 | Optional: transaction list / receipt from `GET /wallet/transactions` + `GET /wallet/ledger/:id` | ✅ Exists | 🤷‍♂️ |
| 1.5 | Optional: session WS (`EXPO_PUBLIC_DPAY_WS_URL`) for live SETTLING → SETTLED | ✅ POC WS | 🤷‍♂️ |

### 2. Backend tickets still required (gaps)

| Gap | Why mobile is blocked | Suggested follow-up | Status |
|---|---|---|---|
| Customer-presented QR | Show to Pay cannot leave mock | `POST` (or equivalent) to create/refresh a consumer display QR + `expiresAt` | 🤷‍♂️ |
| Confirmation / receipt DTO | Screen always shows Coupa Cafe fixture | Return vendor, tx id, method, amount, data value after settle — or document reuse of transaction/ledger | ❓ |
| Device push register / send | Token never leaves the phone; demo is local-only | Register/unregister device token + pay-ready push payload `{ deepLink, merchantName, sessionId }` | 🤷‍♂️ |
| Password reset | Login cannot offer Reset | New auth endpoint, then Ticket 5 control | 🤷‍♂️ |

### 3. Keep mocked until product/backend decide

| Surface | Mock source | Notes |
|---|---|---|
| Home orbit + request counts | `src/mock-data/mockHome.ts` | PdW ownership/search is out of this gateway |
| Show to Pay QR + balance row | `src/mock-data/mockShowToPay.ts` | Until consumer QR API |
| Confirmation card | `src/mock-data/mockPurchaseConfirmation.ts` | Until receipt/settle mapping |
| Push demo CTA | `src/mock-data/mockNotification.ts` | Until remote send exists |
| `/companies` `/requests` `/accounts` `/offers` | `PlaceholderScreen` | Not pay-critical |

### 4. Integration test plan (when mock is off)

1. `EXPO_PUBLIC_DPAY_USE_MOCK=false` and a device-reachable `EXPO_PUBLIC_DPAY_GATEWAY_URL`.
2. `POST /auth/signup` or known CUSTOMER → login → Face ID on second launch.
3. `GET /auth/me` rejects MERCHANT.
4. Scan a POS QR (`QR_DISPLAYED`) → claim `200` → settle `200` → confirmation shows **gateway** fields.
5. Re-claim same session as same user → `200` / still `SCANNED`. Other user → `403`. Expired → `410`.
6. Show to Pay stays mocked until the consumer QR endpoint exists.
7. Push: permission + local token persist; remote send only after register API.

---

## Gateway surface vs app usage

| Method | Path | In client | Used by UI | Notes |
|---|---|---|---|---|
| `POST` | `/auth/login` | ✅ | ✅ `/login` | |
| `POST` | `/auth/signup` | ✅ | 🤷‍♂️ | No signup screen |
| `GET` | `/auth/me` | ✅ | ✅ session unlock | |
| `GET` | `/wallet/me` | ✅ | 🪿 Home ignores it | |
| `GET` | `/wallet/transactions` | ✅ | 🤷‍♂️ | |
| `GET` | `/wallet/session/:id` | ✅ | ✅ live scan only | Mock scan skips this |
| `POST` | `/wallet/session/:id/settle` | 🤷‍♂️ | 🤷‍♂️ | Documented, not implemented |
| `GET` | `/wallet/ledger/:id` | 🤷‍♂️ | 🤷‍♂️ | Documented, not in client |
| — | consumer display QR | — | 🪿 Show to Pay | Gap |
| — | receipt / confirmation | — | 🪿 Confirmation | Gap |
| — | push register / send | — | 🪿 SecureStore + local notify | Gap |
| `GET` | `/health` | — | — | Ops only |
| `*` | `/pos/*` `/business/*` | — | — | Merchant — do not call |

---

## Notes

- Consumer mobile talks **directly** to the dPay POC gateway (Bearer JWT). Not Cognito, NextAuth, Ownership, or `/api/proxy`.
- Do not ship `JWT_SECRET` or M2M credentials in the app.
- i18n stays in `packages/i18n` (`en-US` / `pt-BR`). Do not import web Ditto `@data-reserve/i18n`.
- Screenshot column is a placeholder — drop Figma / device shots in Confluence when you publish this page.
