# Chase findings — operator app 7.0.4 (770)

Cross-check of the 7.0.4 (770) test plan against the build Chase ran. The fee section of that plan is out of date: it says the app sends `applicationFeeAmount`. In this build the app does not send the fee. The server creates the PaymentIntent at `terminal/create_payment_intent/` and applies the fee with `get_split_settlement_fee`.

## Passing — no change

ST-01, ST-02, ST-04, ST-05, ST-07, KY-01, KY-03, FEE-01, LN-01, LN-02, LN-03, LN-04.

The `application_fee_amount` error from the earlier 770 run is resolved (ST-01).

## Needs a look

### Parker pay link shows only "Pay at stand" and "No Gateway Link"

**Where:** checkout website (`oob_web/checkout`), not the operator app.

**Plan:** the Terminal cases use Pay at Stand and Event Parking inside the operator app. The plan does not cover the customer pay link.

**What the code does:** `getGateway()` only routes `stripe`, `square`, `authorizenet`, `vanco`, and `tpp`. `stripe_terminal` has no route, so the pay button becomes "No Gateway Link". "Pay at stand" still shows when the location allows mobile card or cash. Valets are left with the reader or keyed entry in the app.

**Change:** yes, if a Stripe Terminal location must also take a card on the customer link. Add `stripe_terminal` next to `stripe` so the link opens `/checkout/stripe`. The online `create_payment_intent` endpoint already charges the connected account and does not require the gateway to be `stripe`. No operator-app change.

### FEE-03 — fee larger than the ticket

**Where:** backend.

**Plan:** when the fee is zero, or greater than or equal to the ticket amount, the fee is not sent.

**What Chase saw:** a $1.30 fee on a $1.00 ticket. Stripe kept the full $1.00 and the merchant netted $0.

**What the code does:** that guard used to live in the app. It was removed when PaymentIntent creation moved to the server. `get_split_settlement_fee` adds the percentage and the fixed amount and does not compare the result with the ticket. `terminal/create_payment_intent/` sends that fee as `application_fee_amount`.

**Change:** yes, on the backend. Do not send `application_fee_amount` when the computed fee is greater than or equal to the ticket amount, or reject the charge. The operator app does not send the fee, so it does not need a change for this case.

### FEE-02 — no fee / zero fee

**Where:** location setup. No code change.

**Plan:** a location payload with no fee field still completes the charge, and `applicationFeeAmount` is not sent.

**What Chase saw:** the admin screen will not produce a "no fee" location. The fields show a $0.30 + 3% minimum.

**What the code does:** the inputs allow 0. $0.30 and 3% are the defaults. If both valet fields on the location are 0, the server uses the client-level valet fee. Zeroing only the location does not create the case in the plan.

**How to set it up:** as a superuser, set Valet fixed value and percentage to 0 on the location and on the client. The charge then completes with no `application_fee_amount`.

### ST-06 — double tap

**Where:** operator app. No change.

**Plan:** a second tap on Card, while the first checkout is open, says a payment is already in progress.

**What Chase saw:** no message. The reader beeps twice. The first charge completes. No duplicate charge.

**What the code does:** the app has that alert (`A card payment is already in progress`). The checkout overlay covers the Card button, so a second tap does not reach it. Two beeps with one charge are the reader announcing the card and the approval.

**Result:** pass. The message only appears if Card is tapped again while a checkout is already running underneath the overlay.

### ST-03 — cancel after the reader beeps

**Where:** operator app. No change.

**Plan:** if Stripe has already captured, the app marks the ticket paid. It must not report the payment as cancelled while a charge exists.

**What Chase saw:** after the beep there is no Cancel button on iOS or Android.

**What the code does:** Cancel is shown only on the Present card step. After the beep the phase is processing, and the button is hidden on both platforms.

**Result:** pass. That is the intended behavior. Mark ST-03 as pass.

## Not tested yet

### SQ-01 to SQ-04

**Where:** operator app behavior, production location data. No code change for this finding.

**Plan:** on a Square location, Enter card manually is hidden unless both Authorize.net fields are set (`authorize_merchant_login_id` and `authorize_merchant_transaction_key`), on the location or on `location.client`. One field alone stays blocked. This is keyed entry, not Square reader checkout. Square reader checkout is not in this version.

**Blocker:** Square cannot be turned on in staging. This section needs a build pointed at production. No backend change is required to explain the result.

### KY-02 — MOTO flag

**Where:** operator app for this build. Backend only if MOTO is added later.

**Plan:** the WebView request does not set `payment_method_options.card.moto`. The payment still completes. MOTO is not in this build.

**Change:** none for 7.0.4 (770). Dev can confirm the WebView request has no `moto` field. A real MOTO flag would be a later backend change.

### KY-04 — bank authentication error

**Where:** Stripe's bank page inside the keyed-card WebView. Not an Oobeo API error.

**Plan:** the failure is the bank page, the ticket stays unpaid, and the error is not from Oobeo's API.

**How to verify:** Stripe test card `4000000000003220` (any future expiry, any CVC) opens that page. Pass: a payment-failed alert with the message from that page, and the ticket stays unpaid. Fail: the ticket is marked paid, or the error comes from Oobeo's API.

**Change:** none unless that card fails the wrong way.
