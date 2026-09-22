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

*UAT-01 — Damage UI (Android)*
Dent and scratch options are cut off.
Expected: Dent / Scratch labels and buttons fully visible and usable.
Note: Layout / safe area. Not Stripe. Confirm if Friday-only or older.

*UAT-02 — Apple Wallet on Stripe reader (Android)*
Pay with Apple Wallet through the Stripe reader. App shows payment successful. Charge does not appear in the Stripe Dashboard.
Expected: Ticket is paid only if the PaymentIntent succeeded on the lot connected account. If Stripe did not capture, the app must not mark the ticket paid.
Note: Confirm they checked the connected account (not the platform) and test vs live. Current app should verify the PI before mark-paid. Reproduce on the Friday build vs current.

*UAT-03 — SMS (iOS and Android)*
Cannot send SMS messages.
Expected: SMS sends, or the app shows a clear error.
Note: Likely out of Stripe scope. Confirm lot SMS, user permission, and Twilio. Track separately if pre-existing.

*UAT-04 — Card tap does nothing (iOS)*
Stripe reader is connected. Tap the green Card button. Nothing happens. Same in Attendant mode.
Expected: Collect starts (overlay / present card), or a clear connect-reader / Tap to Pay error.
Note: Reader may be paired but the session is not ready. iOS Tap to Pay on TestFlight is a separate known block. This item is reader + Card.

*UAT-05 — Reader beeps success (iOS)*
If a message appears, tapping the physical reader beeps as if payment succeeded.
Expected: Same as UAT-04. Ticket is paid only after server verify.
Note: Same fail-closed rule as UAT-02.

*UAT-06 — Freeze after cash (iOS)*
After a cash payment the app freezes on that screen. User has to force quit.
Expected: Return to the ticket / list. Ticket paid as cash. No Stripe charge.
Note: Pay at Stand and Attendant. Related to UAT-09.

*UAT-07 — Freeze after manual card entry (iOS)*
After keyed / manual CC entry the app is frozen.
Expected: Success leaves the screen with the ticket paid, or an error and the ticket stays unpaid.
Note: Stripe.js keyed path. Confirm backend create_keyed and verify_keyed are deployed.

*UAT-08 — Reports time (iOS and Android)*
When running reports, the time does not reflect correctly.
Expected: Start / end times match the lot / operator timezone.
Note: Likely pre-existing, not Stripe. Split if confirmed.

*UAT-09 — Freeze on payment screen (iOS)*
After opening the payment screen the app freezes, cash or card. User has to quit and reopen. Same on Attendant.
Expected: Cash and card can be completed without killing the app.
Note: May share a root cause with UAT-06 and UAT-07 (post-pay navigation, overlay, or WebView).

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
