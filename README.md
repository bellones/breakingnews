# dPay Mobile App — Architecture Decision Record

> **Ticket 1 deliverable.** Approved direction for adding the dPay React Native app to `business-dwallet.data-reserve-web`.  
> Companion docs: `docs/REACT_NATIVE_APP_HANDOFF.md`, `docs/DPAY_POC_API_CONTRACT.md`.

## Decision summary

| Decision | Choice | Rationale |
|---|---|---|
| App name | `dpay` | Product name from mocks and POC |
| App location | `apps/dpay` | Follows split-app pattern (`apps/business`, `apps/personal`) |
| Framework | **Expo + React Native** | Camera, push, biometrics, fast iteration |
| Styling | **NativeWind v4** + shared tokens | Tailwind-like classes; cannot reuse web DOM components |
| Navigation | **Expo Router** | File-based routes, deep links for notifications |
| State | TanStack Query + Zustand | Matches web patterns |
| Forms | React Hook Form + Zod | Matches web patterns |
| i18n | `react-i18next` + `@data-reserve/i18n` | Avoid `next-intl` on mobile |
| Backend | **Direct to dPay POC gateway** | Not via dWallet web `/api/proxy` |
| Auth | JWT from gateway + SecureStore | POC uses Bearer JWT, not NextAuth cookies |
| Biometrics | Expo Local Authentication | Face ID / Touch ID for session unlock |

## Monorepo layout (proposed)

```text
apps/dpay/                          # Expo React Native app
packages/mobile-design-tokens/      # Colors, spacing, typography (from web theme)
packages/mobile-components/         # RN Button, Typography, Screen, NavBar, etc.
packages/mobile-api-client/         # Gateway client + React Query hooks
packages/types/                     # Extend with dPay POC types (existing package)
```

Do **not** add mobile code to `packages/web-shell`, `design-system-primitives`, or `dwallet-shared-components` — those remain web/DOM.

## Backend integration strategy

```
┌─────────────┐     Bearer JWT      ┌──────────────────────┐
│  apps/dpay  │ ──────────────────► │  dpay-poc-gateway    │
│  (RN/Expo)  │   /api/auth/*       │  localhost:4020/api  │
│             │   /api/wallet/*     │  (or drumwave.dev)   │
└─────────────┘                     └──────────────────────┘
       │
       │  NOT used for v1
       ▼
┌─────────────────────────┐
│ business-dwallet-web    │
│ /api/proxy/ownership    │  ← NextAuth + cookies; wrong model for RN
└─────────────────────────┘
```

POC clone location: `/Users/mac/Downloads/dpay.dpay-poc-main`

Local gateway: `docker compose up` in POC repo → `http://localhost:4020/api`

## Screen → API → web reference mapping

| # | dPay screen (mock) | Gateway API | Web POC reference | dWallet web reference |
|---|---|---|---|---|
| 1 | Push notification | **Gap** — no POC endpoint | — | — |
| 2 | Login / landing | `POST /auth/login`, `POST /auth/signup` | `dpay-poc-dwallet/src/app/page.tsx` | `LandingPageBR`, `SignInForm` |
| 3 | Home dashboard | `GET /wallet/me` | `dpay-poc-dwallet/src/app/home/page.tsx` | `PersonalHomePage`, `RequestStatusDisplay`, `ProfileBubbleDataRequestAnimation` |
| 4 | QR Scan | Parse QR → `GET /wallet/session/:id` | `dpay-poc-dwallet/src/app/scan/page.tsx`, `QrPayScanner.tsx` | `Tabs` (toggle pattern only) |
| 5 | Show to Pay | **Gap** — no consumer QR display API | — | `PixCard` (QR render + countdown logic only) |

## Component reuse matrix

| Reuse level | What |
|---|---|
| **Direct (types/logic)** | `@data-reserve/types`, `parsePaySessionId` logic, settle payload shapes, i18n strings |
| **Adapt (visual parity)** | Button, Typography, gradients, stats row, payment source row, bottom nav |
| **Rebuild (native)** | Camera/scanner, Face ID, push notifications, orbital animation, QR FAB |
| **Do not import** | Any `packages/design-system-primitives` or `dwallet-shared-components` `.tsx` UI |

See `docs/REACT_NATIVE_APP_HANDOFF.md` for full reuse boundaries.

## Auth & Face ID flow

```
App launch
  ├─ No stored token → Login screen
  ├─ Stored token + biometrics enabled → Face ID prompt
  │     ├─ Success → load GET /auth/me → Home
  │     └─ Fail → Login screen (password fallback)
  └─ Stored token + biometrics off → GET /auth/me → Home

Login success
  └─ Store token in SecureStore
  └─ Optionally enable biometrics (device capability check)
```

POC web stores token in `localStorage` (`dpay_token`, `dpay_user`). Mobile equivalent:

- `SecureStore`: `dpay_token`, `dpay_user` (JSON), `dpay_biometric_enabled`

**No Cognito / NextAuth / Ownership API for v1** — dPay POC uses its own JWT auth against Postgres.

## Native capabilities

| Capability | Library (proposed) | Ticket |
|---|---|---|
| Camera + QR scan | `expo-camera` + `expo-barcode-scanner` (or `react-native-vision-camera`) | 7 |
| QR display | `react-native-qrcode-svg` | 8 |
| Face ID | `expo-local-authentication` | 3 |
| Push notifications | `expo-notifications` | 9 |
| Secure storage | `expo-secure-store` | 3 |

Web POC scanner uses browser `BarcodeDetector` API (`QrPayScanner.tsx`) — not portable to RN; must use native modules.

## Design tokens strategy

Extract from:

- `design-system.shared-components-web/tailwind.config` (preset)
- `packages/dwallet-shared-components/src/constants/colorTokens.ts`
- dPay POC gradients in `dpay-poc-dwallet` (e.g. `#4700ff`, `#070707` backgrounds)

Create `packages/mobile-design-tokens` as single source, consumed by NativeWind config in `apps/dpay`.

Key dPay visual tokens from POC:

```css
/* Personal avatar / accent gradient */
radial-gradient(100.27% 100.27% at 26.43% 94.29%, #4700ff 0%, #2f3367 76.61%, #4700ff 99.99%)

/* Background */
#070707
```

## First vertical slice (MVP)

Recommended implementation order for Tickets 2–10:

1. Bootstrap app + tokens + API client
2. Login screen (gateway auth)
3. Face ID unlock
4. Home screen (`GET /wallet/me`)
5. QR Scan → session review → settle
6. Show to Pay UI shell (blocked on backend gap unless mocked)
7. Push notifications (blocked on backend gap unless local-only demo)

## Known gaps / follow-up tickets (backend)

| Gap | Impact | Suggested owner |
|---|---|---|
| No consumer “Show to Pay” QR generation API | Mock #5 cannot be fully functional | `dpay-poc-gateway` team |
| No push notification registration API | Mock #1 cannot be end-to-end | `dpay-poc-gateway` or platform team |
| Payments require savings plans (not main wallet balance) | Home “Paying from” must show plan balances | Already in POC — document in UX |
| No integration with Ownership/Cognito | dPay RN is separate from dWallet web auth | By design for POC |

## Turbo / pnpm integration (Ticket 2 prep)

Add to root `package.json`:

```json
"dev:dpay": "pnpm --filter dpay dev",
"lint:dpay": "pnpm --filter dpay lint"
```

Workspace entry: `apps/dpay/package.json` with name `@data-reserve/dpay` or `dpay-mobile`.

## Security constraints

- JWT stored in SecureStore only
- No `JWT_SECRET` in mobile bundle
- No merchant credentials in consumer app
- Certificate pinning: evaluate for production (out of POC scope)
- Biometrics unlocks stored session — does not replace server auth on token expiry

## Open questions for team review

1. Should dPay RN live in **this repo** or **dpay.dpay-poc-main** monorepo?
   - **Recommendation:** This repo for shared types/i18n; gateway stays in POC repo.
2. Should Show to Pay wait for new gateway API or ship Scan-only MVP first?
   - **Recommendation:** Scan-only MVP first.
3. Reuse dWallet Personal branding on login or dPay-specific branding?
   - **Recommendation:** Mocks show “dWallet. PERSONAL” on login and dPay in notifications — confirm with design.
4. Connect to deployed gateway (`drumwave.dev`) or local Docker only for dev?
   - **Recommendation:** Both via `DPAY_GATEWAY_URL` env switch.

## Approval checklist (Ticket 1)

- [x] POC gateway endpoints catalogued → `docs/DPAY_POC_API_CONTRACT.md`
- [x] Mobile architecture decisions documented → this file
- [x] Reuse vs rebuild matrix defined → `docs/REACT_NATIVE_APP_HANDOFF.md` + tables above
- [x] Backend gaps identified (Show to Pay, push)
- [x] First vertical slice defined
- [ ] Team review / sign-off
- [ ] Jira epic linked
