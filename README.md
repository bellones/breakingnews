# EnforcePlus — Silent Citation Photo Loss on Issue

**Component:** EnforcePlus officer app (React Native) — citation issuance flow  
**Severity:** High — data loss with zero visibility to officer, admin, or system logs  
**Type:** Bug / Investigation findings (engineering confirmed in code)  
**Related citation (example):** `8025-CYKG-81` (Shamrock Parking / Atlas 236)  
**Firestore ticket path:** `/clients/RqvJ9Oz4E0s53xbW1ESb/locations/rS7slIsIsM19ixKNsswt/tickets/0B9xapyuPXWow5po9ryo`  
**Investigation date:** 2026-07-21  

---

## Summary

When an officer issues a citation with photos, the app can **create the citation successfully while silently dropping the photos**. No error is shown to the officer, nothing durable is logged, and there is no retry across app restarts. Once it happens, photos are effectively **permanently unrecoverable**.

Code investigation **confirms** the reported root cause in `issueTicket()` (`src/actions/vehicleActions.js`).

---

## User / operational impact

* Citations can lack legally/operationally important photo evidence.
* Often discovered only when a client disputes the citation — long after the stop.
* Officer has no signal that photos failed; cannot self-correct in the field.
* No fleet metrics/alerts exist to detect how often this occurs.

---

## How it happens (confirmed in code)

### Primary file

* `src/actions/vehicleActions.js` — `issueTicket()` (~lines 667–877)
* `src/tools/index.js` — `uploadImageToStorageAsync()` (~lines 194–309)
* Caller: `src/containers/IssueTicket/IssueTicket.js` — `proceed()` awaits ticket create, then navigates to Print

### Step-by-step

1. Officer attaches photos and taps generate/issue.
2. Each photo must finish **resize → crop → upload → getDownloadURL** inside a **shared** budget of **`MAX_UPLOAD_WAIT_MS = 3500`** (3.5 seconds total for all photos, not per photo).
3. Any photo that times out or errors is pushed to an in-memory `backgroundQueue` instead of being attached to the ticket payload.
4. The ticket document is created immediately with:
   * `images: tempImageUrls.length > 0 ? tempImageUrls : null`
   * If nothing finished in 3.5s → **`images: null` from creation**.
5. App treats issuance as success and navigates to Print.
6. Deferred photos are uploaded via `backgroundQueue.forEach(async …)`:
   * **No await**
   * **No retry**
   * **Not persisted** (lost if JS thread suspends / app backgrounds / navigate away)
   * On failure: only `console.log('Background image upload failed', e)` — not Crashlytics, not Firestore, not UI

### Storage naming (blocks recovery)

Upload path pattern:

`/citiation_images/{officerUid}_{timestampMs}_idx{n}_r{random}.jpg`

* Typo folder: `citiation_images` (not `citation_images`)
* **No ticket ID**, plate, location, or violation number in path/filename
* Cannot reliably correlate Storage objects to a ticket after the fact (confirmed operationally on this case)

---

## Evidence for citation 8025-CYKG-81

* Ticket `images` field: **`null`** (never populated)
* No Storage object found for this officer near `created_at` (`2026-07-19T02:23:11.310Z` UTC) matching the cited vehicle (white Kia SUV, plate A9429338)
* Closest-in-time Storage candidates were unrelated stops
* Conclusion: photo(s) most likely **never left the device successfully** (capture/upload failure or abandoned background work), not merely a failed Firestore link after upload

---

## Code references (for engineering)

| Behavior | Location |
|---|---|
| Shared 3.5s upload budget + `backgroundQueue` | `vehicleActions.js` ~691–716 |
| Ticket created with `images: null` if empty | `vehicleActions.js` ~749 |
| Fire-and-forget background upload + `arrayUnion` | `vehicleActions.js` ~819–846 |
| Failure = `console.log` only | `vehicleActions.js` ~843–844 |
| Storage filename without ticket ID | `tools/index.js` ~286–290 |
| UI navigates on ticket create with no photo check | `IssueTicket.js` `proceed()` ~448–480 |

### Related (same class of risk)

* **Print** placement photo (`Print.js`): also fire-and-forget upload after print; ticket ID exists for write-back, but failures are still poorly surfaced
* **Chalk** flow: safer — awaits upload before writing docs; path includes vehicle/wheel context

### Separate bug found nearby

* After successful ticket create, `removeViolation` / user+location activity dispatches (~871–875) are **unreachable** because all branches `return` earlier. Worth fixing in the same area of `issueTicket`.

---

## Why this is High

* Silent data loss
* Zero officer visibility
* Zero durable logging / observability
* Unrecoverable after the fact due to Storage naming
* Real production citation confirmed with `images: null`

---

## Suggested fix directions

> Not prescriptive — for engineering to prioritize.

### P0 — stop silent loss

1. Do not treat citation as “done” for the officer until photos are confirmed uploaded, **or** show a clear pending/failed indicator if they are not.
2. Persist a **local retry queue** (e.g. AsyncStorage + files copied to app documents) keyed by `ticketId`, resume on next launch/foreground.
3. Surface upload failures in-app (toast/banner) instead of only `console.log`; also record to Crashlytics / durable log.

### P1 — make failures recoverable & measurable

4. Embed ticket ID in Storage path, e.g. `citiation_images/{clientId}/{locationId}/{ticketId}/{n}.jpg` (create ticket first, then upload).
5. Add metrics/alerts: citations issued with intended photos vs final `images` length.
6. Prefer: create ticket → upload with known ID → `arrayUnion` → clear pending flag.

### P2 — hygiene

7. Fix unreachable `removeViolation` / activity logging in `issueTicket`.
8. Align Print / other capture flows with the same retry + surfacing pattern.
9. Fix folder typo when migrating path scheme.

---

## Acceptance criteria (proposed)

* [ ] If officer attached N photos, ticket ends with N images **or** officer sees explicit pending/failed state with retry
* [ ] Background/deferred uploads survive app backgrounding and restart
* [ ] Upload / attach failures are visible to officer and logged durably (Crashlytics or equivalent)
* [ ] New uploads include `ticketId` in Storage path for auditability
* [ ] QA can reproduce timeout path (slow network) and verify no silent `images: null` without UI warning

---

## Suggested QA checks

1. Issue citation with 1–4 photos on slow network / airplane mode mid-upload → verify UI does not silently proceed as fully successful without photo state.
2. Kill app during deferred upload → relaunch → pending photos still attach to same ticket.
3. Confirm Firestore ticket `images` populated after success.
4. Confirm Storage objects live under ticket-scoped path (after fix).
5. Regression: chalk + Print placement photo still work.

---

## Attachments / links

* Investigation performed against current EnforcePlus RN codebase (`issueTicket` / `uploadImageToStorageAsync`)
* Example ticket: `0B9xapyuPXWow5po9ryo` / violation `8025-CYKG-81`
