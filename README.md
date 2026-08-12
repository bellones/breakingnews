# OobeoApp Mobile `/visits/` Polling Current-State Report

**Date reviewed:** 2026-08-12  
**Repository:** `oob_app_5 / OobeoApp`  
**Branch reviewed:** `square-token-minting`  
**Scope:** Mobile app only. Backend `oob_web`, ALB rules, nginx logs, CloudWatch, and production traffic metrics were not re-validated in this check.

## Summary

The mobile-side mechanisms described in the `production-v3 /visits/ polling spike` investigation are still present in the current OobeoApp codebase.

The app still uses three independent swimlane pollers for `/visits/`, still performs full three-lane refetches after activity creation, still refetches lanes in response to Pusher activity/payment events, and still lacks a stable client install/session identifier in outgoing API requests. The specific `stopAlertPoller()` background gap is also still present.

This review does not prove these mechanisms caused the Aug 8 unexplained traffic gap. It confirms that the mobile behaviors identified in the prior report still exist and could continue contributing to elevated request volume.

## Confirmed Findings

### 1. Three independent `/visits/` pollers still exist

`app/actions/carsData.js` defines separate pollers for parked, requested, and ready lanes:

- `startParkedPoller()`
- `startRequestedPoller()`
- `startReadyPoller()`

Each poller is guarded against duplicate interval creation with an `if (!poller)` check, so the code does not intentionally multiply intervals inside one JS runtime. However, each active device can still run up to three independent `/visits/` polling loops.

Important current-state note: the interval is read from `locationData.featureFlags.swimlaneInterval`, but the mobile fallback is `10000ms`. If the backend does not provide the feature flag, the app falls back to 10 seconds, not 60 seconds.

Relevant code:

- `app/actions/carsData.js`
  - `startParkedPoller()`
  - `startRequestedPoller()`
  - `startReadyPoller()`
  - `getCarsData()`, which calls `APIService("GET", "visits/?...")`

### 2. `getAllCars(true)` still expands to three `/visits/` requests

`getAllCars(silent)` still dispatches all three lane fetches:

- `getAllParkedCars(silent, 1)`
- `getAllRequestedCars(silent, 1)`
- `getAllReadyCars(silent, 1)`

That means any call to `getAllCars(true)` still creates three `/visits/` requests, one per swimlane.

Relevant code:

- `app/actions/carsData.js`
  - `getAllCars()`

### 3. `createActivity()` still triggers a full three-lane refetch

`app/actions/activity.js` still calls `dispatch(getAllCars(true))` after a successful `activities/` POST.

This means each successful activity transition, such as park, request, ready, or deliver, can still trigger three extra `/visits/` requests in addition to regular polling.

The code also calls `dispatch(getCarAlerts())`, which targets alert endpoints rather than `/visits/`.

Relevant code:

- `app/actions/activity.js`
  - `createActivity()`
  - success handler after `APIService("post", "activities/", ...)`

### 4. Pusher event fan-out still triggers REST refetches

`app/containers/tabs/useTabs.js` still handles Pusher events by refetching data from REST:

- `activity_type_id === 4` triggers `getAllParkedCars(true)` and reservations refresh
- `activity_type_id === 3` triggers `getAllRequestedCars(true)`
- `activity_type_id === 5` triggers `getAllReadyCars(true)` and reservations refresh
- `payment` events with `status === true` trigger `getAllCars(true)`, which expands to all three lanes
- `reservation` events trigger reservations refresh

This means every subscribed device on the location channel can independently refetch data when a Pusher event arrives. The mobile app does not consume the Pusher payload directly to update local state without a REST round trip.

Relevant code:

- `app/containers/tabs/useTabs.js`
  - `pusherEvents`
  - `initializePusherClient`

### 5. The background handler still omits `stopAlertPoller()`

The app-state background handler in `app/containers/tabs/useTabs.js` still stops these pollers/checks:

- `stopParkedPoller()`
- `stopRequestedPoller()`
- `stopReadyPoller()`
- `stopReservationsPoller()`
- `clearChecks()`

It still does not call `stopAlertPoller()` in that specific background transition path.

This does not explain `/visits/` traffic, because alert polling hits `visitalertsV2/`, not `/visits/`. It is still a valid mobile bug and an easy cleanup.

Relevant code:

- `app/containers/tabs/useTabs.js`
  - `handleAppStateChange()`
- `app/actions/carsData.js`
  - `startAlertPoller()`
  - `stopAlertPoller()`
  - `getCarAlerts()`

### 6. No stable install/session identifier is sent with API requests

The shared request layers do not add a stable device, install, or session identifier to outgoing requests.

`app/lib/APIService.js` sends:

- `Accept`
- `Content-Type`
- `Authorization`, when auth is available

`app/lib/api.js` similarly builds request headers around:

- `Accept`
- `Content-Type`
- `dataType`
- `Authorization`, when auth is available
- caller-provided custom headers

No default header such as `X-Device-ID`, `X-Install-ID`, `X-Session-ID`, or equivalent is added.

`react-native-device-info` is used elsewhere in the app, but not as a default API request identifier.

Relevant code:

- `app/lib/APIService.js`
- `app/lib/api.js`
- `app/containers/drawerContents/useDrawerContents.js`, which uses `DeviceInfo.getUniqueId()` for Square notice acknowledgement, not API request tagging

## Current-State Assessment

The report's mobile-side findings are still valid for the current app:

- Baseline three-lane `/visits/` polling still exists.
- Activity success still triggers a full board refresh.
- Pusher events still cause REST refetches on subscribed devices.
- The alert poller background-stop gap still exists.
- The app still lacks a stable request-level install/session identifier.

The only nuance found during this review is the polling interval fallback. The original report describes a backend-served 60-second `swimlaneInterval`; the mobile code currently falls back to 10 seconds if that feature flag is missing.

## Recommendations

### Short-term mobile fixes

1. Add `stopAlertPoller()` to the background branch in `useTabs.js`.
2. Add a stable client identifier to all API requests, preferably as a header, for example `X-Oobeo-Install-ID` or `X-Oobeo-Session-ID`.
3. Reduce `createActivity()` refetch amplification by refreshing only the affected lane(s), not all three lanes.
4. Revisit Pusher handling so devices can update local state from the event payload where possible, instead of issuing REST refetches for every event.

### Investigation support

To close the unexplained Aug 8 request-volume gap, backend logs need a way to distinguish:

- more physical devices,
- the same device polling more often,
- app restarts creating new sessions,
- duplicated client loops,
- real activity-driven refetches.

The most practical mobile contribution is adding a stable per-install or per-session request identifier.

## Conclusion

Yes, the mobile app still contains the key polling and refetch patterns described in the investigation report. None of the confirmed mobile-side contributors appear to have been removed in the current code. The app should be updated if the goal is to reduce `/visits/` request amplification and make future traffic investigations diagnosable from backend logs.
