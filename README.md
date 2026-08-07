## IFR M2M Token Wiring Validation Report — Web UI Only

### Scope

Validated the IFR user-management call sites that are reachable from the current web applications in the dev environment.

This report intentionally excludes Admin UI / Cognito console validation. Because of that, Cognito side-effects such as group membership, `dWalletId` attributes, phone attributes, and user enabled/disabled state were **not directly verified** here. Those require either Admin UI, Cognito access, Ownership outbox inspection, or backend logs.

### Environment

- Business app: dev environment
- Personal app: dev environment
- Validation method: browser UI + DevTools Network
- No functional code changes were made as part of this validation.

---

## Summary

### Reachable Through Web UI

The following catalogue items are reachable from the current web apps:

- `POST /business`
- `DELETE /business/:id/employees/:employeeId`
- `PATCH /person/:id` for phone set/update
- `PATCH /person/:id` for phone clear, via `DELETE /api/bff/phone`
- `POST /auth/phone/verify`, via `POST /api/bff/phone/verify`

### Not Reachable Through Current Web UI

The following catalogue items were not validated through the web UI because no current screen in this repo appears to trigger them directly:

- `DELETE /employee/:id`
- `DELETE /person/me`
- `DELETE /person/:id`
- `PUT /business/:id/employees/:employeeId`
- `PATCH /employee/:id/role`
- `POST /employee/:id/restore`
- `POST /trash/recover/:entityType/:entityId`
- `POST /trash/recover/:trashId`

These should be validated separately through direct API calls, backend tooling, Ownership logs, or the internal admin surface.

---

## Detailed Results

### Business App — Manage Users

Catalogue coverage:

- `DELETE /business/:id/employees/:employeeId`
- Expected outbox side-effects:
  - `UNSET_DWALLET`
  - `REMOVE_ORG_ROLE`

Screen tested:

- Business app → `/account/users`

Observed:

- The Manage Users screen loaded successfully.
- The employees/invites data request returned a successful response.
- The UI displayed the current account administrator and account manager invitation card.
- Delete and Resend actions were visible for the invite/user card.

Result:

- Precondition confirmed: authenticated Business user can load the user-management screen and fetch user data successfully.
- Destructive delete action was not confirmed in the provided evidence.
- Cognito side-effects were not verified because Admin UI/Cognito validation was out of scope.

Status:

- Partially validated from UI.
- Still needs execution of the delete action and backend/outbox confirmation.

---

### Business App — Business Creation / Email Verification Flow

Catalogue coverage:

- `POST /business`
- Expected outbox side-effects:
  - `SET_DWALLET`
  - `ADD_ORG_ROLE`

Screen tested:

- Business account verification/onboarding flow

Observed:

- The email verification screen was reached.
- Verification failed with:
  - `400 Bad Request`
  - `Code not found`

Result:

- The flow did not reach a successful `POST /business` call in the captured evidence.
- Because the verification code failed, the business creation side-effects could not be validated from UI.

Status:

- Not validated.
- Blocked by invalid/missing verification code.

Follow-up:

- Retry with a fresh verification code or a test account whose email verification can complete successfully.

---

### Personal App — Phone Number Clear

Catalogue coverage:

- `PATCH /person/:id`
- Expected outbox side-effect:
  - `DELETE_PHONE_FROM_COGNITO`

Screen tested:

- Personal app → `/profile/phone`

Observed:

- The phone number screen loaded.
- Removing the phone number triggered an error in the UI:
  - “Failed to delete phone number. Please try again.”
- DevTools Network showed the request returning:
  - `{"error":"Internal Server Error"}`

Expected behavior:

- `DELETE /api/bff/phone` should return 2xx.
- The BFF should call Ownership `PATCH /person/:id` with:
  - `phoneNumber: null`
  - `mfaEnabled: false`
- Ownership should enqueue/process `DELETE_PHONE_FROM_COGNITO`.

Actual behavior:

- The UI request failed with an internal server error.

Status:

- Failed.

Follow-up bug candidate:

- Investigate why `DELETE /api/bff/phone` returns 500 in dev.
- Capture server logs for the BFF route and Ownership response.
- Confirm whether the failure is related to missing/invalid auth headers, M2M token generation, or Ownership-side validation.

---

### Personal App — Phone Number Update / OTP Verification

Catalogue coverage:

- `PATCH /person/:id`
- `POST /auth/phone/verify`
- Expected side-effects:
  - `SYNC_PHONE_TO_COGNITO`
  - `MARK_PHONE_VERIFIED`

Screen tested:

- Personal app → phone update flow
- OTP verification screen

Observed:

- The user reached the phone verification screen.
- The request flow included the phone verification endpoint path.
- OTP verification was not completed in the captured evidence.

Result:

- The UI flow can reach the OTP verification step.
- Successful `POST /auth/phone/verify` and the Cognito phone verification side-effect were not confirmed.

Status:

- Partially validated.
- Needs a valid OTP to complete verification and confirm a 2xx response.

---

## Additional Observations

### Missing Auth Header Error

One captured network response showed:

```json
{
  "ok": false,
  "statusText": "Error",
  "error": "No X-User-Access-Token header found",
  "statusCode": 500
}
```

This should be investigated if it appears on one of the IFR-triggering endpoint calls. It may indicate that a request reached the backend without the expected user access token context.

Since the IFR change is specifically about M2M token wiring, this should be separated carefully from user-token forwarding issues.

### Certificate Warning On Dev Link

One browser screen showed a Chrome certificate warning for a `*.drumwave.dev` URL:

- `NET::ERR_CERT_COMMON_NAME_INVALID`

This blocked or interrupted navigation through that dev link. It is likely environment/certificate related and not directly related to IFR M2M token wiring, but it affected test flow reliability.

---

## Acceptance Criteria Status

- [ ] `DELETE /employee/:id` succeeds and Cognito user is deactivated.
  - Not validated through web UI. No reachable screen found.

- [ ] `DELETE /person/me` and `DELETE /person/:id` succeed and deactivate Cognito user via outbox.
  - Not validated through web UI. Current delete-account screen uses `POST /support/delete-account`, not `DELETE /person/me`.

- [ ] `PUT /business/:id/employees/:employeeId` and `POST /business` succeed and set `dWalletId` attribute + org role.
  - `PUT /business/:id/employees/:employeeId`: not validated through web UI.
  - `POST /business`: not validated because onboarding/email verification was blocked by `400 Code not found`.

- [ ] `PATCH /person/:id` succeeds and syncs phone number to Cognito, both set and clear cases.
  - Set/update: partially validated, flow reached OTP step.
  - Clear: failed via UI, `DELETE /api/bff/phone` returned 500.

- [ ] `POST /auth/phone/verify` succeeds and marks the phone attribute after OTP verification.
  - Partially validated. OTP screen reached, but successful verification not confirmed.

- [ ] `DELETE /business/:id/employees/:employeeId` succeeds and unsets `dWalletId` + removes org role.
  - Partially validated. Manage Users screen and user data loaded successfully. Delete action still needs execution and backend/outbox confirmation.

- [ ] `PATCH /employee/:id/role` succeeds and updates employee Cognito group.
  - Not validated through web UI. No reachable screen found.

- [ ] `POST /employee/:id/restore` succeeds and reactivates Cognito user.
  - Not validated through web UI. No reachable screen found.

- [ ] `POST /trash/recover/:entityType/:entityId` and `POST /trash/recover/:trashId` succeed and reactivate Cognito user via outbox retry path.
  - Not validated through web UI. No reachable screen found.

- [ ] Any failures found are logged with request/response details and filed as follow-up bugs.
  - Failure captured: `DELETE /api/bff/phone` returned 500 / Internal Server Error.
  - Additional observation: `No X-User-Access-Token header found` response captured.
  - Additional observation: dev URL certificate issue blocked navigation.

---

## Recommended Follow-Ups

1. File a follow-up bug for `DELETE /api/bff/phone` returning 500 in dev.
2. Re-test phone verification with a valid OTP to confirm `POST /auth/phone/verify`.
3. Re-test Business onboarding with a fresh verification code to confirm `POST /business`.
4. Validate non-web reachable endpoints through API/backend tooling:
   - `DELETE /employee/:id`
   - `DELETE /person/me`
   - `DELETE /person/:id`
   - `PUT /business/:id/employees/:employeeId`
   - `PATCH /employee/:id/role`
   - restore/trash recovery endpoints.
5. For all outbox-based effects, confirm processing through Ownership outbox logs or database state, since this could not be verified from the browser alone.
