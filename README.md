This ticket is the polish pass: Meena puts the missing Personal routes on the sandbox `/v1` map; mobile drops the fallbacks and finishes live Home behavior.

---

## Description

Meena confirmed that sandbox `/dwallet/*` and `/dsavings/*` calls land on **offers / DSP / certs** upstream. The app does **not** call `dpay-poc-gateway` or `offers-service` directly; the Offers tab is a no-op.

Do not invent a path on mobile until Meena publishes a `/dwallet` equivalent (or documents the Ownership path on `/v1`).

---

## Gateway — endpoints for Meena to unlock

### Already called — must work without 403 / “route missing”

| Method | Path | Used by | Today |
|---|---|---|---|
| `POST` | `/dwallet/auth/signin` | Login | Live (`walletType: "personal"` only) |
| `GET` | `/user/{id}` | Session | Live |
| `GET` | `/dwallet/me` | Home “Paying from” + DSA fallback | Live. Keep `planBalances[]` with **per-plan** `balanceUsd`, not only the wallet-level balance |
| `GET` | `/dwallet/businesses?term=` | My data → Request more data | Often 403 → falls back to the login orbit |
| `POST` | `/dwallet/data-request` | Submit request | 404/405 → `POST /person/me/data-request` |
| `GET` | `/dsavings/certificates` | My data → Claim data | Hits certs/DSP. Retries **without** `status=PENDING&role=owner` on 400/404 |
| `GET` | `/dsavings/data-savings-accounts` | Home + Savings DSA cards | Hits DSP. Retries without `page`/`pageSize` on 400/404/405; failure falls back to `/dwallet/me` plans |

Accept query/pagination on `/dsavings/certificates` and `/dsavings/data-savings-accounts` so the client can stop the bare-path retry.

### Missing on the `/v1` map — Claim data is broken without these

| Method | Path (or a `/dwallet` equivalent Meena names) | Used by | Today |
|---|---|---|---|
| `GET` | `/person/relationships` (fallback: `/proxy/ownership/person/relationships`) | Hide already-decided invitations | 404/405 treated as “no decisions” — decided certs can reappear |
| `POST` | `/person/relationships` body `{ dWalletId, accepted }` | Claim / dismiss CTA | Route missing — CTA shows a live error |
| `POST` | `/business/by-dwallet/bulk` (fallback: `/proxy/ownership/business/by-dwallet/bulk`) | Claim card name / logo / color | Ownership path; not on `/v1` |

### Home parity with Personal web (Meena to confirm the path)

| Capability | Web today | Mobile today | Ask |
|---|---|---|---|
| Live orbit + request list | `GET /api/bff/business/with-data-requests` | Login snapshot (`account.dataRequests` / `relationships`). Ownership search 403s | A sandbox `/dwallet` (or documented) list the app can refetch after login and after a new request |
| Offers | Personal `PersonalOffersSection` | Placeholder — **no** `offers-service` call | Only if Offers is v1: publish the list path. Do not guess |

---

## Mobile — Home improvements after the routes are live

- **Orbit + counters:** refetch live data; do not freeze the login snapshot. Update after Request more data and after Claim/dismiss.
- **Request more data:** use `GET /dwallet/businesses` as the real catalog; drop the orbit fallback when search returns 200.
- **Submit:** `POST /dwallet/data-request` only once that path is on `/v1`.
- **Claim data:** `GET` relationships hides decided certs; `POST` accept/dismiss returns 200 (stay on the tab, remove the card). Load company chrome from the bulk route. Chevron stays visual until a details route exists.
- **DSA (Home + Savings):** prefer `GET /dsavings/data-savings-accounts`. Keep `/dwallet/me` as fallback only. Never copy `wallet.balanceUsd` onto a plan; omit the amount when there is no per-plan balance.
- **Offers:** leave the placeholder until Meena publishes the endpoint.
- **Visual (no new API):** title/subtitle, `StatsRow`, CTAs, orbit, bottom nav vs PdW 18. DWLLT-3169 already covered landing + the Home **header** only.

---

## Out of scope

- Live settle / confirmation
- Show to Pay consumer QR
- Push register / send
- Password reset
- EAS Build

**Dependencies:** DWLLT-3318…3322 (Home tickets 1–5), Claim data follow-up. Gateway first.

---

## How to Test

1. Sign in live. Home: “Paying from” comes from `GET /dwallet/me`; the DSA card comes from `GET /dsavings/data-savings-accounts` (no mock catalog).
2. Request more data: search returns 200 without falling back to the login orbit; submit uses only `/dwallet/data-request`. Home counters and orbit update.
3. Claim data: certificate list loads; `GET` relationships hides decided items; Claim/dismiss returns 200 and the card disappears.
4. The Savings tab lists the same cards. When a plan has no `balanceUsd`, the card omits the amount (it does not repeat the wallet balance).
5. Offers stays a placeholder until Meena publishes the path.

---
