# Security: Leaked Firebase Admin SDK Key in oob_app_5

**Status:** Repo fix done locally — pending commit/merge  
**Priority:** High  
**Labels:** `security`, `firebase`, `secrets`, `oob_app_5`

| Field | Value |
|---|---|
| **Project** | OOB |
| **Issue type** | Task (or Security / Bug) |
| **Reporter** | Midhet Dulovic (incidental finding) |
| **Repository** | `oob_app_5` / `OobeoApp` |

---

## Summary

Remove leaked Firebase Admin SDK service account key from `oob_app_5` repository.

A Firebase Admin SDK service account private key was committed to the mobile app repository. Admin SDK keys grant full Firebase project admin access (bypassing security rules) and must never be stored in a client/mobile repo.

| Item | Detail |
|---|---|
| **File** | `OobeoApp/oobeo-valetware-firebase-adminsdk-icgk0-4d28c6a06c.json` |
| **Firebase project** | `oobeo-valetware` (legacy; current mobile Crashlytics uses `oobeo-prod`) |
| **Service account** | `firebase-adminsdk-icgk0@oobeo-valetware.iam.gserviceaccount.com` |

---

## Diagnosis

### Exposure

- File was tracked in git (`git ls-files`) and **not** covered by `.gitignore`.
- Key type: Firebase Admin SDK service account JSON (full project admin access).

### Who introduced it

| Field | Value |
|---|---|
| **Author** | Matt Ormrod (`matt.ormrod@oobeo.com`) |
| **Commit** | `2b4c1c00` — *"EV amends"* |
| **Date** | 2024-07-12 |

```bash
git log --follow -- OobeoApp/oobeo-valetware-firebase-adminsdk-icgk0-4d28c6a06c.json
```

### App impact

**None.** The mobile app does not reference this file in code. The app uses client SDK configs only:

- `firebase/production/google-services.json` (Android)
- `firebase/production/GoogleService-Info.plist` (iOS)

The admin key appears to be an orphaned/accidental commit from 2024.

### Risk

Anyone with repo access (or full git history) could use the key for full admin access to Firebase project `oobeo-valetware`.

---

## Solution (repo remediation)

### Done locally (pending commit/merge)

1. **Removed** `OobeoApp/oobeo-valetware-firebase-adminsdk-icgk0-4d28c6a06c.json` from git tracking (`git rm`).
2. **Updated** `OobeoApp/.gitignore` to block future commits:
   - `*firebase-adminsdk*.json`
   - `**/firebase-adminsdk*.json`
   - `oobeo-valetware-firebase-adminsdk-*.json`
3. **Added** security note to `OobeoApp/firebase/README.md` — Admin SDK keys must not be committed to this mobile repo.

### Pending

- Commit and merge these changes to `master`.

---

## Remaining actions (manual — required)

### 1. Rotate / revoke compromised key (URGENT)

Treat the key as **compromised**:

1. Firebase Console → project **oobeo-valetware** → Project Settings → Service Accounts
2. Delete/revoke service account key ID `4d28c6a06c...` (`firebase-adminsdk-icgk0`)
3. Only generate a replacement if a **backend/server** still needs it
4. Store any new key in a secret manager / CI env var — **never** in `oob_app_5`

### 2. Git history scrub (team decision)

The key remains in git history since 2024-07-12.

| Scenario | Recommendation |
|---|---|
| Private repo, limited access | Remove from HEAD + key rotation may be sufficient |
| Broader exposure (forks, former contractors, public mirror) | Use `git filter-repo` or BFG Repo-Cleaner to purge the file from history |

**Warning:** History rewrite requires coordinated force-push — do not do unilaterally.

Example (if team approves):

```bash
# git filter-repo --path OobeoApp/oobeo-valetware-firebase-adminsdk-icgk0-4d28c6a06c.json --invert-paths
```

### 3. Confirm no backend dependency

Verify whether any server/backend still uses `oobeo-valetware` Admin SDK credentials before deleting the service account entirely.

---

## Acceptance criteria

- [ ] Admin SDK JSON removed from repo default branch
- [ ] `.gitignore` prevents re-addition of `*firebase-adminsdk*.json`
- [ ] Compromised key revoked in Firebase Console (`oobeo-valetware`)
- [ ] Team decision documented on whether git history scrub is required
- [ ] No Admin SDK keys loaded from committed files anywhere in `oob_app_5`

---

## Jira ticket (copy-paste)

**Summary:**

```
Remove leaked Firebase Admin SDK service account key from oob_app_5 repo
```

**Resolution comment (after merge):**

```
Repo fix merged: Admin SDK JSON removed and gitignore updated.

Still required: revoke key in Firebase Console (oobeo-valetware → firebase-adminsdk-icgk0).
Mobile app was never consuming this file — no app release needed for this fix.
```

---

## References

- Commit that introduced the key: `2b4c1c00` (Matt Ormrod, 2024-07-12)
- Current mobile Firebase configs: `OobeoApp/firebase/production/` (project `oobeo-prod`)
- Original reporter: Midhet Dulovic — found during unrelated RequestAhead/valet-app investigation
- Related doc: [firebase-crashlytics.md](./firebase-crashlytics.md)
