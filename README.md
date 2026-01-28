# Backend API Calls by Screen

This document lists **all backend API calls** made by the app, grouped by screen/flow. Use it to debug errors on the initial screen, router listing, personal details, and to verify why users might be sent back to the initial/login screen (e.g. auth/session handling).

**Base URL** for all API calls below (except TestClient): `LinkConfiguration.apiUrl` → `https://{apiHost}/`

- **Staging:** `https://api-gateway.staging.uplink.xyz/`
- **Dev:** `https://api-gateway.develop.uplink.xyz/`
- **Prod:** `https://api-gateway.uplink.xyz/`

All these requests use the **same Dio client** (`ApiClient`) with the session interceptor (Bearer token) and Firebase App Check where noted.

---

## 1. Initial / Splash screen

**Flow:** `SplashBloc` → connection check → permissions → **AutoLoginUseCase** (no direct API from splash except optional test).

| API Call                       | Method | Path                             | When                                                                                          | Notes                                                                                                         |
| ------------------------------ | ------ | -------------------------------- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| **Verify token (credentials)** | GET    | `/v1/mobile/account/credentials` | After splash, when restoring session (AutoLoginUseCase → setAuthorizationToken → verifyToken) | If this fails (401 or invalid), user is treated as unauthenticated. **Critical for “initial screen” errors.** |

**Optional (dev/test):** Splash can call a **separate test client** (base `http://192.168.30.236:3001/`) with App Check:

- GET `/firebase/testApp` (with `X-Firebase-AppCheck` header) — see `splash_page.dart` `callApiWithDio()` (currently not awaited in main flow).

**No other backend API calls are made on the splash screen itself.**  
Errors on “initial screen” are likely from:

1. **GET `/v1/mobile/account/credentials`** (verify token) failing or returning invalid.
2. Redirect logic: if auth state becomes **expired** → user is sent to **Splash**. If **unauthorized** → user is sent to **Explorer (login)**.

---

## 2. Dashboard (first tab after login / “initial” home)

**Flow:** `HomePage` → `DashboardCubit.init()` → `loadData()`.

| API Call                  | Method | Path                                          | When                    | Notes                                                                                  |
| ------------------------- | ------ | --------------------------------------------- | ----------------------- | -------------------------------------------------------------------------------------- |
| **Network stats**         | GET    | `/v1/mobile/account/network-stats`            | Dashboard load          | Via `GetMyNetworksStatsUsecase`                                                        |
| **Earning stats (KPI)**   | GET    | `/v1/mobile/account/kpi`                      | Dashboard load          | Via `GetEarningStatsUsecase`. Has try/catch and fallback to zeroes; can still rethrow. |
| **Banners**               | GET    | `/v1/mobile/banners`                          | Dashboard load          | Via `GetBannersUsecase`                                                                |
| **Surge areas (near me)** | GET    | `/v1/mobile/surge/near-to-me?lat=...&lng=...` | After location resolved | Via `GetSurgeAreasUsecase`; called after `getCurrentLocation()`                        |

All four are triggered together on dashboard init; the first three are in `Future.wait`, then surge areas are loaded with location.  
**Any 401 from these** is handled by `SessionInterceptor` → refresh requested → eventually can lead to **unauthorized** and redirect to login.

---

## 3. Registered routers (My Networks / Routers tab)

**Flow:** `MyNetworksCubit.init()` → `GetRegisteredRoutersUseCase` → `RouterRepositoryImpl.getRegisteredRouters()` → **CommunityService.getRouters()**.

| API Call                    | Method | Path                                                                                                               | When                                   | Notes                                                  |
| --------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------ | -------------------------------------- | ------------------------------------------------------ |
| **List registered routers** | GET    | `/v1/mobile/account/community-router?pageSize=100&page=1&filter[status][0]=registered&filter[status][1]=validated` | When opening My Networks / routers tab | **Main call for “listing registered routers” errors.** |

Other router-related calls (same screen/flow):

| API Call                          | Method | Path                                                                         | When                                                                  |
| --------------------------------- | ------ | ---------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| **Router details**                | GET    | `/v1/mobile/account/community-router/{id}`                                   | When opening a specific router (e.g. RouterDetailsPage)               |
| **Is router registered (auth)**   | GET    | `/v1/mobile/account/community-router/verifyMacAddress?macAddress=...`        | When checking if current WiFi router is already registered            |
| **Is router registered (public)** | GET    | `/v1/mobile/account/community-router/verifyMacAddress/public?macAddress=...` | Public check (e.g. when not logged in)                                |
| **Register router**               | POST   | `/v1/mobile/account/community-router`                                        | Router registration (body: router payload); uses **requiresAppCheck** |
| **Delete router**                 | DELETE | `/v1/mobile/account/community-router/{id}`                                   | Delete registered router                                              |

---

## 4. Personal details / Profile

**In-app “Profile” screen (Account → Profile information):**  
Opens a **WebView** to `LinkConfiguration.profileUrl`:

- **Staging:** `https://portal.staging.uplink.xyz/account/details?tab=Details`
- **Dev:** `https://portal.develop.uplink.xyz/account/details?tab=Details`
- **Prod:** `https://portal.uplink.xyz/account/details?tab=Details`

So **no direct app-to-backend API call** for that screen; the portal page in the WebView may call its own APIs.

**App backend API that can be used for “personal details” (e.g. account data):**

| API Call         | Method | Path                         | When                                              | Notes                                                                                                                                                                                                 |
| ---------------- | ------ | ---------------------------- | ------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User profile** | GET    | `/v1/mobile/account/profile` | When code calls `AccountService.getUserProfile()` | Currently **not** used by ProfilePage (which uses WebView). Used only if some flow calls `AccountRepository.getUserProfile()`. **Worth checking backend** if “personal details” errors refer to this. |

So “checking personal details” errors could be:

1. **WebView** loading `profileUrl` (portal) or APIs called by that page.
2. **GET `/v1/mobile/account/profile`** if any flow uses it (e.g. future or other screens).

---

## 5. Account / Post-login / Logout

| API Call                             | Method | Path                                                    | When                                  | Notes                                                                   |
| ------------------------------------ | ------ | ------------------------------------------------------- | ------------------------------------- | ----------------------------------------------------------------------- |
| **Post-login (device registration)** | POST   | `/v1/mobile/account/device`                             | Right after auth becomes “authorized” | Body: `appId`, `notificationConfig`. Failures can affect session setup. |
| **Notification config (get)**        | GET    | `/v1/mobile/account/device/{appId}/notification/config` | When reading notification settings    | `appId` from Firebase token (or `'none'`)                               |
| **Notification config (put)**        | PUT    | `/v1/mobile/account/device/{appId}/notification/config` | When saving notification settings     | Same path as above                                                      |
| **Pre-logout**                       | DELETE | `/v1/mobile/account/device/{appId}`                     | Before logout                         | Called in `AuthenticationBloc` before clearing session                  |

---

## 6. Auth and “back to initial screen”

- **GET `/v1/mobile/account/credentials`** is used to **verify token** after restore (splash/auto-login) and when setting a new token (e.g. after web login).
- **401** on any **ApiClient** request is handled by **SessionInterceptor** → `onRefreshTokenRequested()` → effectively triggers auth re-check. After that, if token is cleared or invalid:
  - Auth state can become **unauthorized** → router redirects to **Explorer (login)**.
  - If something ever sets **expired** (e.g. `setAuthorizationToken(..., expired: true)`), router redirects to **Splash** and shows “Session expired”.

So “going back to initial screen” can be:

- **Splash:** auth state **expired** (see `router_data.dart` → `_onSessionExpired`).
- **Login (Explorer):** auth state **unauthorized** when visiting a route that `requiresAuth`.

Relevant backend calls for that behavior:

- **GET `/v1/mobile/account/credentials`** (verify token).
- Any of the dashboard/router/account calls above returning **401**.

---

## 7. Other API calls (Explorer/Map, etc.)

**Mobile (dashboard already listed):**

- `/v1/mobile/account/network-stats` — dashboard
- `/v1/mobile/account/kpi` — dashboard
- `/v1/mobile/banners` — dashboard
- `/v1/mobile/surge/near-to-me` — dashboard

**Map service (Explorer tab / map flows):**

- GET `/map/network-observation?ne=&sw=&heatmap=`
- GET `/map/community-router?ne=&sw=&heatmap=&status=`
- GET `/map/surge/?heatmap=&multiplierRange=`
- POST `/map` (coverage/surge hexagon data)
- GET `/search/location?query=&language=&country=&limit=`
- GET `/map/pois/{h3Index}`
- GET `/community-router/growth?days=&status=`
- GET `/surge/{surgeId}`

(Plus Mapbox geocoding is external: `https://api.mapbox.com/...`.)

---

## 8. Quick reference – paths only

```text
# Account / Auth (critical for initial and “back to initial”)
GET  /v1/mobile/account/credentials
GET  /v1/mobile/account/profile
POST /v1/mobile/account/device
GET  /v1/mobile/account/device/{appId}/notification/config
PUT  /v1/mobile/account/device/{appId}/notification/config
DELETE /v1/mobile/account/device/{appId}
DELETE /v1/mobile/account/

# Dashboard (first screen)
GET  /v1/mobile/account/network-stats
GET  /v1/mobile/account/kpi
GET  /v1/mobile/banners
GET  /v1/mobile/surge/near-to-me?lat=&lng=

# Routers (listing and actions)
GET  /v1/mobile/account/community-router?pageSize=&page=&filter[status][0]=...
GET  /v1/mobile/account/community-router/{id}
POST /v1/mobile/account/community-router  (App Check)
DELETE /v1/mobile/account/community-router/{id}
GET  /v1/mobile/account/community-router/verifyMacAddress?macAddress=
GET  /v1/mobile/account/community-router/verifyMacAddress/public?macAddress=
```

---

## 9. Where to add debug logs

- **Splash / initial:** `AuthRepositoryImpl.setAuthorizationToken`, `AccountServiceImpl.verifyToken` (GET credentials), and router redirect in `RouterData._handleRedirect` (auth state: expired vs unauthorized).
- **Dashboard:** `DashboardCubit.loadData` and the four use cases (network-stats, kpi, banners, surge/near-to-me).
- **Routers:** `CommunityServiceImpl.getRouters` and `RouterRepositoryImpl.getRegisteredRouters`.
- **Personal details:** If you add or use `getUserProfile()` in the app, log in `AccountServiceImpl.getUserProfile`; for the WebView, debug the portal URL and any failing requests in the WebView (e.g. via portal or backend logs).

This gives the backend team a full list of API calls for those screens and clarifies what to check when users see errors or are sent back to the initial screen.
