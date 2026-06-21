## Summary

Personal join flow (multi-DSP / DWLLT-2512) fails when `POST /api/bff/personal/join-dsp` PATCHes a source DSA that has no linked currency. The DSP API returns HTTP 500:

```text
Account {dsaId} has no associated currency
```

Example DSA: `22a1ace2-3099-4e9c-b1be-84f0d823cf39`

---

## Steps to reproduce

1. Sign in to Personal dWallet in DEV with a user who has an existing DSA created without a linked currency.
2. Start join flow: `/plans/{cdspId}` → contribution → terms.
3. Submit terms (triggers `POST /api/bff/personal/join-dsp`).
4. Observe failure when the BFF PATCHes the source DSA to remove reassigned categories.

---

## Expected behavior

- DSA creation always includes a valid currency, **or**
- PATCH/update endpoints return a clear client error (4xx) when currency is missing, **or**
- Missing currency is backfilled/healed so category updates succeed.

---

## Actual behavior

HTTP 500 from DSP API during `PATCH /v1/data-savings-accounts/{dsaId}` (or related account operations).

Example response:

```json
{
  "ok": false,
  "statusCode": 500,
  "error": "",
  "statusText": "HTTP 500: Account 22a1ace2-3099-4e9c-b1be-84f0d823cf39 has no associated currency"
}
```

---

## Technical notes

- **Web BFF:** `apps/web/src/app/api/bff/personal/join-dsp/route.ts` — PATCHes source DSAs before `POST /v2/data-savings-plans/applications`.
- **DSP API:** `data-savings-account.service.ts` — throws `InternalServerErrorException` when `account.currency?.code` is missing in `updateAccount`, `getAccountBalance`, `mapAccountToResponse`, etc.
- **DSA provisioning:** `createDsaFromApplication` only connects currency when `app.currency` is present on the application; applications without `earningsGoal.currency` can produce DSAs with no currency.

---

## Impact

Blocks join-a-plan flow for affected users in DEV (and potentially prod if bad data exists). Downstream UX (welcome screen, `/plans?dsaId=...`) is never reached.

---

## Acceptance criteria

- [ ] New DSAs are created with a required/default currency for the plan region.
- [ ] Existing DSAs missing currency are identified and remediated (migration/script) or handled gracefully.
- [ ] Account update/balance endpoints do not return opaque 500 for missing currency; use actionable 4xx where appropriate.
- [ ] Join flow succeeds end-to-end for a user moving categories from an existing DSA to a new commercial plan.

---
