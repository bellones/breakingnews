# Release notes — Oobeo Valet 6.0.18.691 (Week 1)

**Build:** 6.0.18 / 691  
**Branch:** `release/ios-6.0.18-691-week1`  
**Platforms:** iOS + Android  
**Type:** Weekly hotfix (bug fixes only)  
**Date:** 8 September 2026

This drop is for QA / client validation (including Risetek custom amount). It is **not** a store downgrade. Live store version is **7.0.1** and store versions only go up; the next store build that includes these fixes will be **7.0.2+**.

---

## What’s in this build

### OOB-443 — Board / visits refresh
Parked, Requested, and Ready should update from Pusher without a full list reload, and should not keep refetching after the app is backgrounded.

### OOB-420 — Photo upload size
Receive / car photos are resized before upload so large images do not stall the lot.

### OOB-207 — SMS keyboard (especially iOS)
Keyboard should not cover the SMS field when messaging from a reservation.

### OOB-306 — Attendant History
History should open without crashing on lots with empty or odd transactions.

### Custom amount (attendant)
Amount is entered in **cents**. Example: type `580` → **$5.80** (not $580). This is the Risetek / keyed custom-amount fix.
