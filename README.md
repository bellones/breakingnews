# Authentication investigation and proposed tickets

Reviewed on 28 October 2026. Ticket descriptions are in English for the project backlog.

**Publication status:** drafts only; no Jira issues were created. `AUTH-01` through `AUTH-03` are document references, not Jira keys. The local Jira CLI reported that it was not authenticated. The Atlassian integration was available but not connected at review time, so existing issues, ownership and duplication could not be checked. The repository workflow names project RES and parent RES-1067; verify the parent and existing backlog before publishing, particularly for backend-owned work.

## Recommendation

Use three consolidated delivery tickets: AUTH-01 validates the current native integration; AUTH-02 owns the architecture decision and required backend authentication capabilities; AUTH-03 integrates those capabilities in the app, including Google SSO and biometric unlock. AUTH-01 and the discovery phase of AUTH-02 can start in parallel. AUTH-03 implementation depends on the accepted backend contract and available endpoints. These are broader delivery items, not a commitment to finish all three in one sprint.

The current app already has password authentication, secure persistence and renewal safeguards. The main gaps are evidence from real iOS/Android networking, a server-supported session that survives access-token expiry, and a native Google sign-in contract. Biometric locking can protect an existing local session; it cannot make an expired legacy credential renewable.

## Evidence and limits

Source snapshots inspected locally, without fetching remote changes:

- Mobile: `45ba6b2909f843a7d93b8375776587f467ef8034`.
- Identity API: `cdf4c71092eb8380c9ad14a41469e2d539d4ef3f`.

Confirmed in source:

1. **The password contract matches the app's basic approach.** `POST /api/auth/login` accepts username, password and portal type, returns user info with a token, and attaches a fingerprint cookie. The app sends `portalType: 'RP'`, uses `credentials: 'include'`, reads JWT `exp`, and stores the session through the secure-store abstraction. Normal responses use camelCase; the resident failed-login result explicitly serializes PascalCase `ReasonCode` and `Message` with HTTP 404, matching the app's mapping. Sources: [login controller](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Controllers/AuthController.cs#L91), [resident error result](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Helper/RPFailedLoginResult.cs#L38), [JSON formatting](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/App_Start/WebApiConfig.cs#L196), [mobile strategy](../../packages/authentication/src/identity-strategy.ts), [native storage](../../packages/storage/src/device.ts).
2. **Legacy renewal is not a refresh-token flow.** `/api/auth/renew` is authorized, reads the existing bearer token and fingerprint cookie, reloads roles, and issues another token. Its policy rejects a request naming a different portal. The app discards expired tokens before attempting renewal. No refresh-credential or revocation endpoint was found in the inspected identity source. Sources: [renewal action](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Controllers/AuthController.cs#L511), [renewal policy](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Helper/TokenRenewalPolicy.cs#L54), [session manager](../../packages/authentication/src/session-manager.ts).
3. **A universal 24-hour token lifetime is not established by this repository.** Password login and renewal pass `new TimeSpan()` to `JwtHelper`, whose implementation is in the external `CiraNet.Api.Common` package. Google token generation explicitly uses `JwtTokenExpirationHours`, defaulting to 8 hours. The mobile ADR and package instructions currently say 24 hours. Actual password, Google and renewed-token lifetimes and cookie attributes need verification against the shared package and target environment. The app already schedules from `exp`. Sources: [Google token generation](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Services/Token/TokenGenerationService.cs#L48), [expiration configuration](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Services/Configuration/AppConfigurationService.cs#L74), [mobile ADR](../adr/0006-authentication.md), [authentication instructions](../../packages/authentication/CLAUDE.md).
4. **Google SSO exists, but its app-facing handoff is web-oriented.** The server uses OWIN for the Google challenge and callback, generates a cached one-time code, redirects to an allowed SPA origin at `/google-callback?code=...`, and exchanges the code for user info, a JWT and its cookie. `GoogleCodeExchangeDto` contains only `Code` and `PortalType`; no native PKCE parameters or OIDC discovery implementation were found. OWIN state handling and one-time-code consumption already exist; the gap is a supported native handoff, not an absence of all OAuth protections. Sources: [Google endpoints](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Controllers/AuthController.cs#L881), [redirect builder](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Services/Redirect/RedirectUrlBuilder.cs#L123), [code exchange DTO](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Dtos/GoogleCodeExchangeDto.cs), [code consumption](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Services/OAuth/OneTimeCodeService.cs#L120).
5. **Current mobile sign-out is local.** The password strategy supplies no `signOut` implementation. The session manager deletes the secure credential and triggers application cleanup; it exposes an optional server hook for a future strategy. Cache, ability and telemetry cleanup already exist. This does not establish server-side revocation of a previously issued token or successful erasure when secure storage fails. Sources: [strategy](../../packages/authentication/src/identity-strategy.ts), [session manager](../../packages/authentication/src/session-manager.ts), [provider cleanup](../../apps/community/src/providers.tsx), [documented storage-failure behavior](../../packages/authentication/CLAUDE.md).
6. **reCAPTCHA validation is conditional.** Password login checks it only when `RecaptchaToken` is supplied; the app supplies none. This does not prove that the deployment lacks throttling, account lockout, WAF protections or other controls. Those controls require an owner and environment review. Source: [login validation](https://gitlab.com/therealmanage/ciranet/api/ciranet-identity-api/-/blob/cdf4c71092eb8380c9ad14a41469e2d539d4ef3f/CiraNet.Identity.Api/Controllers/AuthController.cs#L93).

This was a source-level integration investigation. No credentials were used and no deployed login, physical device, cookie persistence or production controls were tested. Shared JWT generation/validation is delegated to `CiraNet.Api.Common` 3.0.0; its internals were not audited. Existing mobile tests were inspected. A targeted test command for authentication, API client and storage was attempted, but pnpm began dependency resolution and policy verification; it was interrupted before tests executed. Backend tests were not run: the project requires Windows/.NET Framework 4.7.2 and Visual Studio MSBuild. No application implementation was changed.

The 30-day session, biometric and Google SSO requirements below come from [ADR 0006](../adr/0006-authentication.md); product must confirm their release priority and exact semantics. Native OAuth recommendations use the system browser and PKCE as described in [RFC 8252](https://www.rfc-editor.org/rfc/rfc8252.html). Refresh-credential replay protection should follow the applicable guidance in [RFC 9700](https://www.rfc-editor.org/rfc/rfc9700.html#section-4.14).

## AUTH-01 — Validate and harden identity-api authentication on iOS and Android

**Suggested type:** Task. **Priority:** P1. **Proposed owner:** Mobile with Identity/QA support. **Dependencies:** approved non-production environment, test accounts, installable native builds, and access to identity-api and resident-api. Can start before the long-term architecture decision.

**Problem:** The current password flow matches the basic backend contract, but demo responses and automated tests do not establish that native networking, fingerprint cookies and secure storage work together against the real services.

**Scope:** Validate the existing password flow on both platforms, fix bounded integration defects and correct the authentication documentation. Record contract gaps under AUTH-02 and app changes under AUTH-03.

**Acceptance criteria:**

- Demonstrate real password sign-in with `portalType: 'RP'`, followed by a protected resident-api call, on physical iOS and Android devices with demo mode disabled. Record builds, backend/OS versions, devices and redacted results; identify any simulator-only checks.
- Verify secure-session and fingerprint-cookie persistence independently across kill/relaunch and foreground resume. Record cookie attributes and cross-host scope without recording its value. Confirm actual token lifetimes and shared-library defaults; correct the unconditional 24-hour claims.
- Verify near-expiry renewal, expired-token behavior, wrong-portal rejection and absent/mismatched cookie rejection. Exercise concurrent calls and confirm single-flight renewal and at most one authenticated retry after renewal.
- Cover invalid credentials, locked/unapproved accounts, offline/timeout and server outages. Transient renewal failures preserve only a still-valid legacy session.
- Verify sign-out, relaunch and account switching erase the credential and clear user caches and permissions. Report secure-deletion failures separately; distinguish local cleanup from server revocation.
- Attach redacted evidence, implemented fixes and remaining limitations. Mobile and Identity/QA reviewers record whether the existing password flow meets the agreed integration milestone.

**Evidence:** findings 1–3 and 5. **Outcome:** demonstrated native interoperability and a documented baseline for the new session model.

## AUTH-02 — Define and deliver the backend authentication contract for mobile

**Suggested type:** Story. **Priority:** P1. **Proposed owner:** Identity/backend lead with Mobile, Product and Security; Patrick to help identify the decision owner. **Dependencies:** environment/configuration access for discovery; use AUTH-01 findings as available. Backend implementation follows an explicit architecture checkpoint within this ticket.

**Problem:** Legacy renewal requires a valid access token, Google SSO returns to a web SPA, and deployed native login protections have not been established by this source review. The backend needs an agreed and implemented contract for long-lived native sessions and Google sign-in.

**Scope:** Consolidate the architecture decision, renewable/revocable sessions, native Google handoff and native login protection policy in one backend delivery item. A documented decision alone does not complete this ticket.

**Acceptance criteria:**

- **Architecture checkpoint:** choose an identity-api/provider OAuth/OIDC flow with Authorization Code + PKCE or a BFF-managed mobile session. Product confirms the meaning of 30 days, release scope, reauthentication triggers and session/logout policies. Record owners and update ADR 0006 and the authentication boundary in ADR 0005.
- For a BFF solution, demonstrate how it obtains valid upstream authorization after the legacy JWT expires, without storing/replaying the user's password. Publish endpoint, credential, expiry, error and migration contracts before app implementation starts.
- Implement and test session issuance and renewal beyond access-token expiry within the approved session policy, including per-device identity, protected credential storage, rotation/replay defense and concurrency behavior.
- Implement revocation/logout and test expired, revoked and replayed credentials, account disabling/credential changes and portal/audience isolation. Document the remaining lifetime of issued access tokens and how resource APIs enforce any immediate-revocation promise.
- Provide a native Google system-browser/code-exchange contract with registered callbacks, PKCE S256, request correlation, short-lived single-use codes and client/redirect/portal binding. Reject wrong verifiers, redirects and replay; keep tokens out of callback URLs. Preserve existing web sign-in.
- Define browser/native/BFF cookie ownership and prove how native code exchange obtains any required fingerprint cookie. Cover cancellation, missing/unlinked accounts and backend failures; avoid silent account creation unless explicitly required by Product.
- Inventory deployed lockout, throttling and monitoring controls, resolve the optional-reCAPTCHA question, and implement any approved gaps. Document the enforcement points, error contract and rollout; assess attestation only if justified by the agreed policy.
- Supply working non-production endpoints, redacted contract examples and backend/integration tests for AUTH-03. Record review of the architecture and security decisions and verify legacy portal compatibility.

**Evidence:** findings 2–6; [ADR 0005](../adr/0005-mobile-bff.md). **Outcome:** an agreed, working backend contract that supports the mobile authentication requirements.

## AUTH-03 — Integrate persistent sessions, Google sign-in and biometric unlock in the app

**Suggested type:** Story. **Priority:** P1 for session integration; Product confirms Google/biometric delivery order within this scope. **Proposed owner:** Mobile with Identity/QA support. **Dependencies:** accepted AUTH-02 contract and working backend endpoints; use AUTH-01 as the native baseline. Estimate after the architecture checkpoint.

**Problem:** The app currently stores only an access token and expiry, discards expired tokens and exposes a password-specific sign-in interface. It does not consume renewable server sessions or provide native Google sign-in and biometric unlock.

**Acceptance criteria:**

- Extend authentication-owned types, strategies and the shared session manager to consume AUTH-02. Persist renewable credentials only through secure storage; support safe migration or reauthentication for the old stored format. Never persist passwords or expose credentials to key/value storage, logs, analytics or URLs.
- Restore and renew the session after access-token expiry within the server policy. Preserve single-flight renewal, bounded calls, session isolation during sign-out/account changes and lifecycle ordering before protected requests. Distinguish transient outages from terminal expiry/revocation; never treat an expired access token as usable authorization.
- Integrate server logout/revocation and local cleanup. Define and test offline revocation and secure-deletion/rotation-persistence failures so they are reported and do not silently resurrect a signed-out session.
- Add accessible, localized Google sign-in through the system browser using the approved PKCE contract and registered callbacks for each platform/environment. Handle cold-start/resume callbacks, cancellation, timeout, missing/unlinked accounts and unsolicited/replayed results; establish the session through the same lifecycle as password sign-in.
- Add biometric opt-in and unlock using the approved platform/secure-credential protection. Product defines lock timing and fallback; gate authenticated content and requests until successful unlock. Cover failure, cancellation, lockout, unavailable/not-enrolled biometrics and enrollment changes.
- After biometric unlock, validate or renew the server session as required. An expired/revoked renewable credential still requires server reauthentication. Clear biometric association and previous-user content on sign-out/account switch.
- Consume the agreed login-protection/error contract. Keep user-facing messages localized and safe; verify password sign-in remains functional.
- Add meaningful behavior tests for the new lifecycle and flows, then demonstrate password and Google sign-in, protected API access, restart/resume, expiry, biometric unlock, sign-out and account switching on physical iOS and Android devices. Update authentication and lifecycle documentation with the actual behavior.

**Evidence:** findings 1–5; [authentication types](../../packages/authentication/src/types.ts), [session manager](../../packages/authentication/src/session-manager.ts), [native storage](../../packages/storage/src/device.ts). **Outcome:** a complete native app integration with the approved backend session model, Google sign-in and local biometric protection.

## Relationship to the earlier questions

- **Question 2 (real native validation):** AUTH-01 is the immediate delivery ticket.
- **Question 3 (long-term auth):** AUTH-02 decides and delivers the backend model, native Google contract and login protection policy; AUTH-03 integrates sessions, Google sign-in and biometric unlock in the app.
- **Questions 1 and 6 (next sprint and foundation readiness):** use AUTH-01 evidence and the AUTH-02 architecture checkpoint as a proposed initial sprint outcome, subject to the team's milestone. AUTH-02 remains open until its backend delivery criteria are met; AUTH-03 completes the new native flows. Passing password integration is a distinct milestone from delivering all long-term auth requirements.
- **Question 4 (BFF):** AUTH-02 resolves only its authentication boundary. BFF hosting, general feature contracts and the first non-auth feature remain separate work.
- **Question 5 (authorization):** role/community rules and server-side enforcement need their own workstream. `CanImpersonate`, JWT role claims and successful sign-in do not establish authorization for every app action. Impersonation is already referenced as RES-1034 in ADR 0006; check that issue before creating another.

Before publishing, search existing RES and Identity/backend issues for these scopes, verify parent/type/priority, replace document references with real issue links, and record actual assignees and dependencies. No estimates or delivery dates are asserted here.
