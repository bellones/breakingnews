# Jira ticket (paste into Description)

**Summary:** [UAT] Friday operator build — Chase team findings (Stripe + shared Pay at Stand)

**Type:** Bug (parent) — split the table below into subtasks if needed
**Priority:** High
**Components:** Operator App, Payments / Stripe Terminal
**Environment:** Staging app + prod app noted in UAT; Friday build given to Chase’s team
**Affects versions:** Friday UAT cut (pre e2e retest)
**Labels:** `uat` `stripe-terminal` `operator-app`

---

## Summary

Chase’s team UAT on the Friday operator build. Several payment and shared-screen issues on Android and iOS (Pay at Stand and Attendant). This ticket is the punch list from their notes. Fix, then run the e2e plan on a new cut *before* the next UAT handoff. App + backend must ship together for Stripe.

## Environment

* Platform: Android + iOS
* Surfaces: Valet Pay at Stand, Attendant / event
* Pathways: Stripe reader, Apple Wallet on reader, keyed / manual CC, cash
* Also reported (may be pre-existing): SMS, reports time, dent/scratch UI

## Findings

||ID||Area||OS||Steps / what they saw||Expected||Notes||
|UAT-01|Damage UI|Android|Dent and scratch options are cut off|Full Dent / Scratch labels and buttons usable|`DamageAssessmentModal` — likely layout / safe area, not Stripe. Confirm if Friday-only or older.|
|UAT-02|Stripe / Apple Wallet|Android|Pay with Apple Wallet via Stripe reader. App shows payment successful. Charge **does not** appear in Stripe Dashboard|Ticket paid **only** if PI `succeeded` on the **lot connected account**. If Stripe did not capture, app must fail closed (not mark paid)|Check they looked at the connected account (not platform) and test vs live. Current app should verify PI before mark-paid. Reproduce on Friday build vs current.|
|UAT-03|SMS|iOS + Android|Cannot send SMS|SMS sends (or a clear error)|Likely **out of Stripe scope**. Confirm lot SMS / user permission / Twilio. Track separately if confirmed pre-existing.|
|UAT-04|Stripe reader — Card tap|iOS|Reader connected. Tap green **Card**. Nothing happens|Collect starts (overlay / present card) or a clear “connect reader / TTP” error|Same on Attendant. Reader may be paired but session not ready. iOS TTP / App Store entitlement is a **known separate block** — this report is **reader + Card**.|
|UAT-05|Stripe reader — Card tap (variant)|iOS|If a message appears, tapping the physical reader beeps “success”|Same as UAT-04 + ticket paid only after verify|Align with UAT-02 / fail-closed.|
|UAT-06|Cash — freeze|iOS|Complete cash payment → app frozen on that screen. Force quit|Return to ticket / list; ticket paid cash; no Stripe PI|Also reported after entering payment screen for cash **or** card (UAT-09). Pay at Stand + Attendant.|
|UAT-07|Keyed / manual CC — freeze|iOS|After manual CC entry, app frozen|Success → ticket paid + leave screen, or error and stay unpaid|Stripe.js keyed path. Confirm backend `create_keyed` / `verify_keyed` deployed.|
|UAT-08|Reports time|iOS + Android|Reports time does not reflect correctly|Start/end times match local lot / operator timezone|Likely **pre-existing**, not Stripe. Split if confirmed.|
|UAT-09|Payment screen freeze|iOS|After opening payment screen, freeze on cash **or** card. Quit + reopen. Same on Attendant|Can complete cash or card without killing the app|May be the same root cause as UAT-06 / UAT-07 (post-pay navigation / overlay / WebView).|

## Acceptance

* Each in-scope item reproduced on current staging app + matching backend, or marked cannot-repro / out of scope with evidence.
* UAT-02 / UAT-05: no “paid in app, missing in Stripe.” Fail closed if verify fails. PI on connected account.
* UAT-04 / UAT-05: Card with a connected reader starts collect on iOS (Pay at Stand and Attendant).
* UAT-06 / UAT-07 / UAT-09: cash and keyed complete without freeze; ticket state matches Stripe/cash.
* UAT-01 / UAT-03 / UAT-08: fix if this cut caused them; otherwise child tickets, not a UAT blocker for Stripe.
* New build walks the e2e plan before Chase’s team.

## Out of scope / do not treat as Stripe UAT blockers until confirmed

* iOS Tap to Pay / Apple Pay on **TestFlight** until Apple enables the App Store TTP entitlement (separate).
* Full Square / reservation / tips matrix (optional one smoke only).
* SMS and reports timezone unless this Friday cut changed those screens.

## Test notes for the fixer

* Lot: `payment_gateway = stripe_terminal`, valid `tml_*`, matching Stripe mode.
* Dashboard: connected account for that lot.
* After each card: ticket status + PI id + Dashboard.
* Attach: device + OS, app version, lot, ticket #, pathway, PI id, screenshot.
