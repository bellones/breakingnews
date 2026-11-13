# Cache store spike preliminary local findings

**Updated 13 November 2026 · History from 12 November retained**

**Status: INCONCLUSIVE. The store-selection spike and its ticket remain incomplete.**

The 13 November local execution stopped at test 5. Approximately 50 ms after expiry by the Valkey server's clock, the exact total was one while the page was empty. PostgreSQL completed five 100,000-row cycles without exceeding the selected local screens. The required managed ElastiCache comparison was not run.

PostgreSQL remains the pre-existing default while the original decision gate is open. The final store decision and confirmation or rewriting of the C tickets remain pending. The 84 Valkey semantic passes and incomplete fill from 12 November are retained below as history, separate from the current execution.

## Scope and criteria

Both experiments ran in Docker. On 13 November, both store primaries had two CPUs and 4 GiB of container memory. Valkey used a 3 GiB server limit with eviction disabled, a replica and three Sentinels. The observed runtimes were PostgreSQL 17.11 and Valkey 9.1.3 with Search 1.2.1. No AWS resources were created. The local topology does not establish managed failover behavior; the guide permits a PostgreSQL container, while the required managed ElastiCache arm remains outstanding. (N1–N4)

The updated scope uses one Valkey search index per entity and spec hash, using the same spec hash that identifies the PostgreSQL data table. Callers and source-data hashes identify sets within that index. A changed spec creates another index. Test 5 covers expiry, retirement and removal of an obsolete index and its documents while preserving live sets and outstanding fills. Document TTL and removal of the index definition are separate operations.

The two arms shared a canonical fixture descriptor and deterministic datasets: 35 semantic-test rows and 100,000 pets-like documents of 1 KiB each. PostgreSQL consumed the full scale dataset; the final Valkey lifecycle invocation stopped on the small fixture before scale. The fixture's spec revision changed without changing field types, testing identity and cleanup rather than a production schema migration. (N1–N3)

These local criteria were frozen before measurement at 12:26:59 UTC on 13 November. They are agent-selected engineering screens under delegated authority, not team-approved AWS acceptance criteria or production service levels.

| Local screen | Limit used on 13 November |
| --- | --- |
| Page plus exact count | p95 ≤250 ms ordinarily; ≤500 ms contains/search; ≤1,000 ms pagination/deep offsets |
| Complete 100,000-row fill | ≥2,000 rows/second; ≤50 seconds; setup plus initial fill ≤60 seconds |
| Incremental allocation | ≤1,536 MiB above the empty indexed baseline |
| Recovery | ≤30 seconds for the complete local failure/failover scenario |
| PostgreSQL repeated-cycle screen | Live heap/index size ≤2× the first cycle; vacuum maximum ≤1 second |

The 12 November profile had two CPUs and 2 GiB per primary, a 768 MiB Valkey server limit and a 512 MiB allocation screen. Its results remain unchanged. The larger 13 November profile was frozen separately to allow two concurrent complete sets; it does not reinterpret the earlier OOM. Its Valkey scale allocation and recovery limits were not exercised after the expiry stop. (E1, N1)

Scale queries use ten warmups and 100 measured repetitions. PostgreSQL ran five fill, expire, delete and ordinary-vacuum cycles. Timings cover warmed store operations, excluding source HTTP, authentication, probing and full request latency. Three independent problem runs and 30% improvement were additional local screens, not requirements of the original ticket or approved AWS criteria.

## PostgreSQL measurements

On 13 November, PostgreSQL loaded all 100,000 rows in each cycle and ran 12 query shapes per cycle, including exact totals and deep offsets: 6,000 timed operations overall. It used UNLOGGED control and entity/spec data tables with the existing SQL compiler. (N2)

| Measurement | Observed result on 13 November | Meaning |
| --- | --- | --- |
| Fill duration | 1.30–1.47 seconds | Approximately 67,900–77,100 rows/second for complete store ingestion |
| Largest query p95 | 89.94 ms | Largest per-query/per-cycle p95; full-result offset 99,900 in cycle two |
| Largest ordinary vacuum | 115.46 ms | Below the local one-second screen |
| Initial incremental disk footprint | 143.37 MiB | First-fill allocation less the empty baseline |
| Final retained disk increment | 156.48 MiB | Same live row count after five cycles and vacuum |
| Retained live heap/index ratio | 1.0912× | Approximately 9.1% growth; below the local 2× screen |
| Temporary-file activity | 2,240 files; 6.49 GiB | Counters covering queries, plans, probes and cleanup |

No selected latency, vacuum or retained-size screen was exceeded. Five cycles in one experiment are not independent production replications. Temporary-file counters do not measure physical-device IO or cloud charges. PostgreSQL file sizes do not establish its RAM requirement or provide a direct comparison with unmeasured Valkey sizing.

For comparison, the separate 12 November run recorded fills of 1.42–1.55 seconds, a largest p95 of 95.80 ms and maximum ordinary vacuum of 117.57 ms. Its disk increments, retained ratio and temporary-file totals matched the rounded values above. Resource profiles differed; these observations do not establish a causal performance improvement. (E2)

## Valkey findings

### 13 November 2026

The lifecycle phase ran before query parity and scale. The first expiry probe passed. The second of three planned probes triggered the expiry stop, and the third probe and subsequent final acceptance scenarios were not run. (N3)

| Post-expiry observation in the second probe | Recorded result |
| --- | --- |
| Server clock before and after the query | 50.028 ms and 51.660 ms beyond the absolute expiry |
| Search exact total | 1 |
| Returned page | Empty |
| Existence of both control/document pairs | All four absent in subsequent checks |
| Indexed documents before and after existence checks | 2, then 0 |

This establishes a stale count and index entries observed after server-confirmed expiry. No expired row was returned in the page. The cause and the operation responsible for the transition to zero are unproven. The stop occurred before the two-second index poll, control-validity guard and retirement/sweep; none of those later checks passed in the final invocation.

An earlier 13 November diagnostic demonstrated limited spec-rotation/sweep, live-set and lease protection checks. Its isolation negative control then failed on unsupported bare-wildcard syntax. That command was corrected to an indexed non-null-ID predicate, but the final expiry stop prevented renewed isolation acceptance. Another earlier expiry stop without server timestamps remains archived without a proven cause. These diagnostics do not replace the final stopped protocol. (N6)

No 100,000-row Valkey fill, latency, complete-set allocation, recovery or failover measurement was accepted in this execution. A pre/post-query control-validity guard could reject expired sets and trigger refill; that is a proposed mitigation, not a tested fix or an override of the stop criterion.

### 12 November 2026 history

All 84 reference cases passed on 35 rows: 24 operator/null cases, 52 contains/search cases and eight sort cases. Identifiers, order and exact totals matched PostgreSQL. These are historical small-fixture results, not current-run passes or proof of full-scale latency and operational correctness. (E3)

The scale setup acknowledged 85,968 of 100,000 rows before OOM, approximately 10.90 seconds into the attempt. Allocation increased by 761.99 MiB from the empty baseline, exceeding the added 512 MiB screen, with a 768 MiB server limit and eviction disabled. (E4)

That allocation includes indexes and transient/replication overhead. It is not completed-set resident memory, a rigorous lower bound on completed-set memory/cost, or complete-fill throughput. It does not establish that a suitably configured ElastiCache node cannot hold the workload.

Partial cleanup succeeded and no key evictions were observed. The complete recovery scenario—protecting existing complete data, freeing memory, opening the failure window and safely resuming service—was not exercised. The run ended at an additional local memory gate; successful cleanup did not demonstrate unmanageable full-memory recovery.

An earlier attempt triggered a Search-module abort involving a missing nullable search value. Persisting empty search shadows with separate null indicators allowed the final small cases to pass. Nonzero indexing-failure counters remain recorded; those passes do not prove error-free indexing. These findings remain context for the managed continuation. (E3, E4, E7)

## Ten test coverage

The original ten scenarios and stop conditions remain unchanged. The current execution records one observed expiry stop and nine final scenarios prevented by it. Historical partial observations are shown separately. No managed AWS test is complete.

| Test | 13 November final execution | 12 November history and remaining proof |
| --- | --- | --- |
| 1. Operators and nulls | Not run after test 5 | 24 small cases passed historically; full workload and managed parity pending |
| 2. Contains and search | Not run after test 5 | 52 small cases passed historically; semantics and latency at 100,000 rows pending |
| 3. Sort and tie-breaking | Not run after test 5 | Eight small cases passed historically; full-scale order, nulls and collation pending |
| 4. Pagination and total | Not run after test 5 | Not run at scale; offsets 0, 50,000 and 99,900 with exact totals pending |
| 5. Expiry and indexing | **STOP:** stale post-expiry count in probe two | Not run historically; expiry correctness and safe entity/spec index collection pending |
| 6. Lease and failure window | Not run in final invocation | Not run historically; racing clients, heartbeat, takeover and fencing pending |
| 7. Fill and visibility | Not run; scale prevented | Earlier setup fill incomplete; three complete fills and concurrent visibility pending |
| 8. Memory and recovery | Not run; scale prevented | Earlier partial OOM/allocation and cleanup; complete sizing and full recovery pending |
| 9. Caller isolation | Not run in final invocation | Not run historically; mandatory set scoping and negative control pending |
| 10. Failover integrity | Not run; scale prevented | Not run historically; managed primary failure mid-fill and after completion pending |

The four guide stops remain impossible accent-insensitive contains, unacceptable deep offsets, expired entries remaining indexed, or unmanageable full-memory failures. The observed stale count/index entries triggered the expiry scenario. Physical removal of an empty index definition is a separate lifecycle question. A technical aggregate that propagates one stop across missing tests must not be read as ten independently observed stops or failures.

## Cost interpretation

AWS charges attributable to both local runs are **$0**; no AWS resources were created. Hardware, electricity and engineering time were not priced. Published US East rates retrieved on 13 November support the illustrations below; they are not an incurred AWS bill, capacity recommendation or spending approval. The 12 November price inputs remain preserved separately. (N5, E5)

| Five-day Valkey illustration | Node-compute budget |
| --- | --- |
| m7g.large primary for 120 hours | $15.17 |
| Same primary plus a replica for 24 hours | $18.20 |
| r7g.large primary and replica for 120 hours each | $42.05 |

Runner, PostgreSQL, backup and transfer costs are excluded. Production cost remains unmeasured: complete Valkey size, real concurrent sets, traffic, TTLs, working-set requirements and managed performance are missing. Existing 10/50/200-set and PostgreSQL RAM-fraction calculations remain sensitivities only; burstable RDS CPU-credit charges are unmodeled. Neither the old partial allocation nor the absent current scale result supports a Valkey production price.

## Completion and next steps

- **Ten managed tests:** not complete. The local expiry stop is recorded; the original managed scope remains open.
- **PostgreSQL comparison:** measured locally in both runs; the paired ElastiCache arm remains incomplete.
- **Final decision log:** pending. PostgreSQL remains the existing default under the original rule.
- **C-ticket confirmation or rewrite:** pending; no final store-specific confirmation follows from these local experiments.
- **Measured production cost:** incomplete; published-rate illustrations and measured PostgreSQL disk inputs are available.
- **Harness validation:** 115 automated checks passed, with no failures or skips. These are separate from the stopped store experiment. (N4)
- **Cleanup:** local containers, networks and volumes were verified removed at 08:44:20.588 on 12 November and 09:51:13.034 on 13 November, America/Fortaleza. No AWS resources were provisioned. (E6, N4) Subsequent disk cleanup removed the two unused spike images and disposable generated files, preserving reports and raw evidence.

Investigate the stale-count behavior on the exact managed engine before resuming acceptance. Use the SRE-provided throw-away node-based ElastiCache environment with the required Valkey/Search capabilities, eviction disabled, suitable capacity and a VPC runner. Establish team acceptance criteria before that run; local delegated screens do not become managed acceptance criteria automatically.

Record eligible measurements or the approved stop that ends them. Review the earlier module workaround and indexing counters, update the cost comparison from complete measurements and documented traffic assumptions, and evaluate the original gate: PostgreSQL stays unless Valkey passes all ten tests and fixes a measured PostgreSQL problem. Then record the final decision, confirm or rewrite the C tickets, and verify removal of temporary resources.

Store-independent A/B work can continue in parallel. **The spike ticket remains open and inconclusive.**

## Evidence appendix

References E1–E7 retain their original meaning for 12 November. References N1–N6 support the 13 November follow-up. All links point to immutable revisions; raw measurements remain unchanged.

### 13 November 2026 evidence

- **N1 — Setup and shared identity:** [Execution record](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/run.json), [pre-run screens](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/thresholds.json), [entity/spec descriptor](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/entity-spec.json).
- **N2 — PostgreSQL scale:** [Five-cycle measurements, samples, plans and IO](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/postgres-scale-1.json).
- **N3 — Valkey expiry:** [Final lifecycle results and both instrumented probes](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/valkey-lifecycle-1.json).
- **N4 — Validation and cleanup:** [115 automated checks](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/validation.json), [13 November teardown](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/teardown.json).
- **N5 — Current price inputs:** [Published AWS rates and retrieval details](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/aws-prices.json).
- **N6 — Retained diagnostics:** [Limited sweep/isolation investigation](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/integration-attempts/003-confirmed-unscoped-wildcard-syntax/valkey-lifecycle-1.json), [earlier expiry stop without server timestamps](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/b66687c0cfd59fb0e0f5310f45573cf9198fba6d/tools/cache-store-spike/evidence/dwllt3955-20261113-r2/integration-attempts/002-expiry-stop-without-clock-observations/valkey-lifecycle-1.json).

### 12 November 2026 evidence

- **E1 — Setup and local criteria:** [Execution record](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/run.json), [pre-run screens](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/thresholds.json).
- **E2 — PostgreSQL scale:** [Five-cycle measurements, samples, plans and IO](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/postgres-scale-1.json).
- **E3 — Semantic reference and comparison:** [PostgreSQL results](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/postgres-parity-1.json), [Valkey’s 84 cases](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/valkey-parity-1.json).
- **E4 — Valkey scale:** [Incomplete fill, allocation, OOM and cleanup](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/valkey-scale-1.json).
- **E5 — Price inputs:** [Published AWS rates and retrieval details](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/aws-prices.json).
- **E6 — Resource removal:** [Local teardown verification](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/teardown.json).
- **E7 — Earlier attempt and validation:** [Earlier Valkey observations](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/integration-attempts/005-null-contains-server-abort/valkey-parity-1.json), [server crash diagnostic](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/integration-attempts/005-null-contains-server-abort/interruption-docker.log), [validation record](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/validation.json).
