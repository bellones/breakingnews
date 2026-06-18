# Square Multi-Location QA — Test Status

**Environment:** Staging (Square Sandbox)  
**Date:** June 17, 2026  
**Tester:** Thiago  
**Verified with:** Carsen, Midhet

---

## Setup

Created 3 locations under the same operator with Square payments configured using the same authorization credentials:

| Location | Admin URL |
|----------|-----------|
| 1528 | https://staging.oobeo.com/admin/oobeo_api/location/1528/options |
| 2447 | https://staging.oobeo.com/admin/oobeo_api/location/2447/options |
| 2448 | https://staging.oobeo.com/admin/oobeo_api/location/2448/options |

---

## Test Results

| # | Test Case | Status |
|---|-----------|--------|
| 1 | Re-authorize on 1 location — that location has the payment device connected and set up; after re-authorization, payments can be processed via the payment device | ✅ **PASS** |
| 2 | Re-authorize on 1 location, but the payment device is set up and running on one of the other 2 locations — after re-authorization, the payment device should work without issues | ✅ **PASS** |
| 3 | Connect the payment device to the phone, wait 1 hour — the payment device should work and be able to process payment | ⏳ **DONE** — awaiting token screenshots from Carsen |
| 4 | Connect the payment device and wait 70 minutes — the token should be automatically fixed, and we should receive an email report about the fix | 🔲 **PENDING** |

---

## Test 1 & 2 — Notes

- Logout and location switch scenarios were tested and worked as expected.
- Observed and verified with Carsen and Midhet.

---

## Test 3 — 1-Hour Wait Scenario

After waiting ~1 hour with the reader connected, a payment attempt was made. Square authorization remained valid throughout — no re-authorization was required. The payment flow was canceled manually on the mock reader UI (not a token/auth failure).

### App Logs

```
Square authorize START {target: 'staging', environment: 'SANDBOX', authorizationState: 'AUTHORIZED'}
Square authorize OK Already authorized, skipping
Square authorize AFTER {target: 'staging', environment: 'SANDBOX', authorizationState: 'AUTHORIZED'}
Square Mock Reader UI requested {environment: 'SANDBOX', authorizationState: 'AUTHORIZED'}
Square Mock Reader UI shown

Square authorize START {target: 'staging', environment: 'SANDBOX', authorizationState: 'AUTHORIZED'}
Square authorize OK Already authorized, skipping
Square authorize AFTER {target: 'staging', environment: 'SANDBOX', authorizationState: 'AUTHORIZED'}
Square Mock Reader UI requested {environment: 'SANDBOX', authorizationState: 'AUTHORIZED'}
Square Mock Reader UI shown

Error: The payment was canceled.
```

### Key Observations

- `authorizationState: "AUTHORIZED"` — reader session stayed authorized after 1 hour
- `environment: "SANDBOX"` / `target: "staging"` — correct staging/sandbox configuration
- `"Already authorized, skipping"` — no token refresh or re-pairing needed at checkout time
- Mock Reader UI displayed successfully
- `"The payment was canceled"` — user canceled the payment on the mock reader UI; **not** an auth/token error

### Still Needed (Test 3)

Token screenshots from Carsen for the test location:

- [ ] `square_mobile_auth_token` (`authorization_code` + `expires_at`) — before and after the 1-hour wait
- [ ] `square_access_token` / `square_token_expiry` (merchant OAuth token)

---

## Test 4 — 70-Minute Auto-Fix + Email Report (Pending)

### Steps

1. Connect reader and confirm payment works
2. Note exact start time
3. Leave app open with reader paired — no logout, location switch, or manual re-authorization
4. Wait **70 minutes**
5. Attempt a payment without manual intervention
6. Confirm token was auto-refreshed on the backend
7. Confirm email report was received about the fix

### Open Questions for Backend

- [ ] Is the mobile auth token refresh job running on staging?
- [ ] Which inbox receives the auto-fix email report?

---

## Summary

Setup and Tests 1–2 are complete across locations 1528, 2447, and 2448. Test 3 (1-hour wait with reader connected) has been executed — app logs confirm the reader stayed authorized; awaiting backend token screenshots from Carsen to fully close out Test 3. Test 4 (70-minute auto-fix + email report) is next.
