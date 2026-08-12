# EnforcePlus Codebase Check — production-v3 `/visits/` Polling Spike Report

**Date:** 2026-08-12  
**Checked repository:** EnforcePlus officer app (`enforceplus`, React Native)  
**Source report:** production-v3 `/visits/` polling spike — investigation report (2026-08-06 to 2026-08-10)  
**Audience of original report:** mobile (`oob_app_5`) and backend (`oob_web`) developers  
**Purpose of this note:** Confirm whether the patterns / bugs described in that report exist in the EnforcePlus app today  

---

## Verdict

**None of the `/visits/` polling mechanisms or related bugs described in the investigation report exist in the EnforcePlus codebase.**

The original report concerns **OobeoApp** (valet / board mobile client) and **oobeo_api** (web/API backend), not EnforcePlus (parking enforcement officer app).

---

## Scope of search

Searched the EnforcePlus repo for:

- `/visits/`, `keywordType=status`, `visitalertsV2`
- `parkedPoller`, `requestedPoller`, `readyPoller`, `reservationsPoller`, `alertPoller`
- `getAllCars`, `createActivity`, `swimlaneInterval`
- `requestahead`, Pusher / `pusherEvents`
- `stopAlertPoller`, `useTabs`, `carsData.js`
- Related terms: `oobeo`, `valet`, `swimlane`

Also scanned `src/` for polling patterns (`setInterval`, pollers, Pusher).

---

## Findings matrix

| Item from investigation report | Present in EnforcePlus? | Notes |
|---|---|---|
| `/visits/` board-refresh polling | **No** | No references to `/visits/` or status-poll query shapes |
| Three swimlane pollers (`parked` / `requested` / `ready`) | **No** | Not found |
| `getAllCars(true)` after every activity | **No** | Not found |
| RequestAhead Pusher fan-out → REST refetch | **No** | No Pusher client usage found |
| Missing `stopAlertPoller()` on app background | **No** | File/pattern does not exist here |
| `swimlaneInterval` (60s backend constant) | **No** | Not found |
| Event 1 ALB `/visits/*` cutover to v4 | **N/A** | Infra/routing; not EnforcePlus app code |
| Event 2 unexplained `/visits/` volume gap | **N/A** | Applies to OobeoApp + production-v3 traffic, not this app |

---

## What EnforcePlus *does* poll

The only notable recurring timer found in app JS is in `src/App.js`: a **60-second interval** that checks expired **watch alerts** (local notification / watch-list logic). That path is unrelated to `/visits/` board polling and would not drive the production-v3 ASG spike described in the report.

EnforcePlus otherwise uses Firebase (Firestore listeners, Storage, etc.) for enforcement workflows (scan, whitelist, citations, violations)—not the Oobeo valet `/visits/` API.

---

## Conclusion

- **Do not treat EnforcePlus as in-scope** for the Aug 7 / Aug 8 `/visits/` polling spike remediation.
- Confirmed mobile fixes from the report (e.g. `stopAlertPoller()` in `useTabs.js`, reducing `getAllCars` refetch fan-out, Pusher payload consumption, session/install ID on API requests) belong in the **OobeoApp** repository (`OobeoApp/app/...`), not here.
- Backend / ALB recommendations remain with **oob_web** / infra as stated in the original report.

---
