# Cache store spike managed results

16 November 2026 · DWLLT-3955

## Decision

**Retain PostgreSQL.** The frozen decision rule allows switching to Valkey only if it passes all ten tests and fixes a measured PostgreSQL problem. The real ElastiCache run reached the original expiry stop condition in test 5. The matching PostgreSQL measurements exceeded none of the selected limits. The [decision summary](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/summary.json) verifies that the criteria were frozen before measurement.

This is **one observed stop and nine tests not executed**, not ten failures. The stop legitimately ended the Valkey arm. Store decision evidence is complete. Publication and actual C-ticket updates remain manual; infrastructure destruction is intentionally deferred to the user, and billed cost remains unknown.

## Environment and protocol

The run used node-based ElastiCache Valkey **9.0.0**, one primary and one replica on `cache.m7g.large`, TLS and `noeviction`. The authenticated preflight measured **5,140,907,060 bytes** of usable `maxmemory`. Supplementary read-only checks confirmed TLS hostname rejection, an out-of-namespace key denial and a denied synthetic secret ARN. The secret denial jointly exercises IAM and the endpoint, rather than isolating their policies. A command probe was unverified and an alternative-principal fixture was not available; no permissions were expanded. PostgreSQL **17.12** ran in an isolated container on the same ARM64 AWS runner, limited to two CPUs and 4 GiB.

[SRE PR #2684](https://github.com/reddrummer/sre.infrastructure/pull/2684) supplied the dedicated private Secrets Manager endpoint. The [successful preflight](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/managed-preflight.json) and [frozen run manifest](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/run.json) establish the managed environment; earlier Docker observations were not counted as AWS results.

The ten-test protocol and its four guide stop conditions were preserved: impossible required text semantics, unacceptable deep pagination, expired results remaining queryable, and unmanageable full-memory recovery. Numerical limits were selected by the implementing agent under the user's delegation and frozen before this run. They are spike criteria, not team-approved production service levels. No thresholds were adjusted after observing results.

## Expiry finding

The 35-row lifecycle fixture ran two TTL probes. The first passed. In the second, the server clock was approximately **50 ms past the common expiry before querying**, and 51 ms past afterward. The search exact total was **1**, while the returned page contained **zero rows**. Subsequent checks found all four relevant document/control keys absent. Index document counts were two before those existence checks and zero afterward.

Expired data therefore contributed to an exact total and triggered the frozen expiry criterion. **No expired row was returned in the recorded page.** The internal cause and which operation changed index state are unproven. The observation does not establish that entries persisted for two seconds or indefinitely. A separate count-before-expiry/page-after-expiry boundary observation was retained as context and was not the stop reason.

The [independent verification](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/expiry-verification.md) checked both recorded probes against the raw result, server times, commands and frozen criteria; it did not rerun the experiment. See the [raw lifecycle evidence](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/valkey-lifecycle-1.json).

## Ten-test coverage

1. Operator, NULL, NOT, array and enum parity — not executed.
2. Literal contains and text/search semantics — not executed.
3. Sorting, null placement and deterministic ties — not executed.
4. Deep pagination and exact totals — not executed.
5. Expiry and entity/spec index lifecycle — **stopped on the second TTL probe**; remaining lifecycle checks were not reached.
6. Leases, fencing and failure windows — not executed.
7. Fill throughput, immediate visibility and concurrent reads — not executed.
8. Complete-set memory and real noeviction OOM recovery — not executed.
9. Caller isolation — not executed.
10. Managed failover and complete-set integrity — not executed.

The 100,000-row Valkey phase did not run. Complete-set sizing, production node requirements, failover behavior and OOM recovery remain unmeasured; neither this small fixture nor historical local OOM data supplies those results.

## Matching PostgreSQL measurements

The fresh baseline completed five 100,000-row fill/delete-and-vacuum cycles, **6,000 measured query operations** and 600 warmups. Each operation included its page and exact total. The separate 35-row PostgreSQL oracle exercised 84 cases; these are not Valkey parity passes.

- Complete ingestion took **2.099–2.374 seconds**, or **42,118–47,632 rows/second**.
- Maximum query p95 was **132.447 ms**, at offset 99,900. Maximum text/search p95 was **75.907 ms**.
- Maximum live-set ordinary vacuum was **222.350 ms**. Retained heap/index size grew **1.0912×** between equal live sets after vacuum.
- Initial disk growth was **143.367 MiB**; final retained growth was **156.484 MiB**. These footprints do not establish required RAM.
- Database counters recorded **6.494 GiB** of temporary data. The separate host window recorded **799.543 MiB** of device writes. These are different measurements and cannot be added or treated as billable IO requests.

No selected PostgreSQL limit was exceeded. This synthetic warm-cache experiment has five cycles within one run, not five independent replications or production capacity proof. The [measurement report](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/postgres-measurement-summary.md) explains the sampling and IO scope.

## Costs and publication

[Cost Explorer access was denied](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/cost-explorer-attempt-observation.json), so the actual AWS bill is unknown. The [updated runtime cost model](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/cost-update.md) estimates approximately **$23.34** for the observed core interval through 14:31 UTC, including node/endpoint hourly rounding. This excludes later runtime and ancillary services; it is not a final total. Published unit rates and runtime illustrations remain estimates with service-specific exclusions. Valkey production cost per complete set cannot be calculated from this stopped run. See the [pricing qualifications](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/pricing-notes.md).

The [full-plan decision entry](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/full-plan-decision-log-update.md) and [C-ticket handoff](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/c-tickets-update.md) are **unpublished manual drafts**. Publish the reviewed PostgreSQL decision on the spike page and full-plan decision log, then confirm or rewrite the actual store-specific C tickets. Their identifiers were not supplied; no remote update or invented ticket confirmation is claimed.

## Evidence export and deferred infrastructure teardown

The complete disposable bucket was copied outside AWS: **19 objects**, **63,454,794 bytes**, stable before/after metadata, verified source/run archive hashes and verified individual frozen dataset hashes. The [permanent export receipt](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/permanent-bucket-export.json) identifies the Desktop destination. The compressed 100,000-row input remains there, avoiding a 62 MB generated file in Git.

Harness cleanup is complete: **40 owned Valkey keys, both created indexes, PostgreSQL schemas, containers, anonymous volumes and the introduced 497 MB PostgreSQL image were removed**. [Cleanup receipt](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/measurement-job-result.json).

**AWS infrastructure destruction is deferred at the user's explicit request.** The user will execute the prepared **draft** [SRE teardown PR #2685](https://github.com/reddrummer/sre.infrastructure/pull/2685) later. A destroy plan was requested; no destroy apply was requested or performed by this run. Resources are intentionally retained and continue to accrue applicable charges. The last cost estimate above ends at its observation time and does not cover this later runtime.

The [handoff receipt](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/4551f69916a3f2cb497e28d3ba2ad27ac4b2bae4/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/aws-infrastructure-teardown.json) records the deferral, verified export and exact unit. The closure inventory contains 20 identities. Profile/role/EC2 relationships corroborate the instance-profile identity; direct `GetInstanceProfile` remains denied. Keep that limitation visible during later absence verification and preserve the frozen manifest. No AWS absence or completed ticket closure is claimed.

Before the later apply, refresh and review the destroy plan for this unit only. Preserve the shared VPC, subnet group, old Secrets Manager endpoint/security group and private DNS. After destruction, check each saved resource identity independently, then retire the unit configuration.

The recorded deadline remains **18 November 2026, 14:00 UTC / 11:00 Fortaleza**. There is no automatic deletion. Full-plan publication, actual C-ticket updates and the user-run infrastructure teardown remain separate closure steps.
