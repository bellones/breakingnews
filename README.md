# Cache store spike preliminary results

**Preliminary local findings · 12 November 2026**

**Status: INCONCLUSIVE. The store-selection spike and its ticket remain incomplete.**

The local experiment measured PostgreSQL and verified 84 small-dataset Valkey query cases. The Valkey attempt to load 100,000 rows ran out of memory before completion in the configured local environment. The required managed ElastiCache comparison was not run.

PostgreSQL remains the **pre-existing default** while the original decision gate is open. These findings establish neither final store selection nor rejection of ElastiCache. The store-specific C tickets have not been confirmed.

## Scope and criteria

The experiment ran in Docker. Both store primaries had two CPUs and 2 GiB of container memory. Valkey additionally had a 768 MiB server limit with eviction disabled. A replica and three Sentinels were provisioned locally; their presence does not prove failover behavior. No AWS resources were created. A PostgreSQL container is permitted by the guide; the missing required AWS arm is ElastiCache. (E1)

Both arms used the same deterministic input datasets: 35 semantic-test rows and 100,000 pets-like documents of approximately 1 KiB each. PostgreSQL used UNLOGGED control/data tables and the existing SQL compiler. Valkey used a prototype server-side query compiler with Search. Its observed core version was 9.1.3, with Search 1.2.1. (E1–E4)

These criteria were recorded before measurement. They are **agent-selected local screens under delegated authority**, not team-approved AWS acceptance criteria or production service levels.

| Local screen | Limit used |
| --- | --- |
| Page plus exact count | p95 ≤250 ms ordinarily; ≤500 ms for contains/search; ≤1,000 ms for deep offsets |
| Complete 100,000-row fill | ≥2,000 rows/second; ≤50 seconds |
| Incremental allocation | ≤512 MiB above the empty indexed baseline |
| Recovery | ≤30 seconds for the complete local failure/failover scenario |
| PostgreSQL repeated-cycle screen | Live heap/index size ≤2× the first cycle; vacuum maximum ≤1 second |

Scale queries used ten warmups and 100 timed repetitions. PostgreSQL ran five fill, expire, delete and ordinary-vacuum cycles. Timings cover warmed local store operations, excluding source HTTP calls, authentication, probing and full request latency. (E1, E2)

## PostgreSQL measurements

PostgreSQL loaded all 100,000 rows in each cycle and ran 12 query shapes per cycle, including exact totals and deep offsets: 6,000 timed operations overall. (E2)

| Measurement | Observed result | Meaning |
| --- | --- | --- |
| Fill duration | 1.42–1.55 seconds | Approximately 64,300–70,500 rows/second for store ingestion |
| Largest query p95 | 95.80 ms | Largest per-query/per-cycle p95, not a production percentile |
| Largest ordinary vacuum | 117.57 ms | Below the local one-second screen |
| Initial incremental disk footprint | 143.37 MiB | First-fill allocation less the empty baseline |
| Final retained disk increment | 156.48 MiB | Same live row count after five cycles and vacuum |
| Retained live heap/index ratio | 1.0912× | Approximately 9.1% growth; below the local 2× screen |
| Temporary-file activity | 2,240 files; 6.49 GiB | Counters covering queries, plans, probes and cleanup |

No selected latency, vacuum or retained-size screen was exceeded in this invocation. Five cycles in one experiment are not independent production replications. Temporary-file counters do not measure physical-device IO or cloud charges. PostgreSQL file sizes do not establish its RAM requirement or provide a direct comparison with an incomplete Valkey allocation.

## Valkey findings

All **84 reference cases passed on 35 rows**: 24 operator/null cases, 52 contains/search cases and eight sort cases. Identifiers, order and exact totals matched PostgreSQL. This establishes behavior for those small-fixture cases, not full-scale latency or operational correctness. (E3)

The scale setup acknowledged **85,968 of 100,000 rows** before an out-of-memory error, approximately **10.90 seconds** into the attempt. Allocation increased by **761.99 MiB** from the empty baseline, exceeding the added 512 MiB screen. The server limit was 768 MiB with eviction disabled. (E4)

The allocation includes indexes and transient/replication overhead. It is not completed-set resident memory, a rigorous lower bound on completed-set memory/cost, or complete-fill throughput. It does not establish that a suitably configured ElastiCache node cannot hold the workload.

Partial cleanup succeeded and no key evictions were observed. The complete recovery scenario—protecting existing complete data, freeing memory, opening the failure window and safely resuming service—was not exercised.

The original guide stops on impossible accent-insensitive contains, unacceptable deep offsets, expired entries remaining indexed, or unmanageable full-memory failures. This run ended at an **additional local memory gate**. Successful cleanup does not demonstrate the guide’s unmanageable-failure condition, and the other original conditions were not established.

An earlier local attempt triggered a Search-module abort involving a missing nullable search value. Persisting empty search shadows with separate null indicators allowed the final small cases to pass. Nonzero indexing-failure counters remain recorded; passing those queries does not prove error-free indexing. These findings require review in the managed continuation. (E3, E4, E7)

## Ten-test coverage

No managed AWS test is complete. “Partial” identifies useful local evidence. One partial memory result prevented nine scale scenarios; these are not ten functional failures or ten independently observed stop conditions.

| Test | Local result | Remaining proof |
| --- | --- | --- |
| 1. Operators and nulls | Partial: 24 small cases passed | Required workload and managed-engine parity |
| 2. Contains and search | Partial: 52 small cases passed | Semantics and acceptable latency at 100,000 rows |
| 3. Sort and tie-breaking | Partial: eight small cases passed | Full-scale order, nulls and collation |
| 4. Pagination and total | Not run at scale | Offsets 0, 50,000 and 99,900 with exact totals |
| 5. Expiry and indexing | Not run | Automatic removal and expiry-boundary correctness |
| 6. Lease and failure window | Not run | Racing clients, heartbeat, takeover and fencing |
| 7. Fill and visibility | Not run: setup fill incomplete | Three complete fills and concurrent/read-after-write visibility |
| 8. Memory and recovery | Partial: OOM/allocation and cleanup observed | Complete-set sizing and the full recovery scenario |
| 9. Caller isolation | Not run | Mandatory set scoping and negative control |
| 10. Failover integrity | Not run | Managed primary failure mid-fill and after completion |

## Cost interpretation

AWS charges attributable to this local run are **$0**. Hardware, electricity and engineering time were not priced. Published US East (N. Virginia) rates, retrieved on 12 November 2026, support the following **illustrations**, not an observed AWS bill, capacity recommendation or spending approval. (E1, E5, E6)

| Five-day Valkey illustration | Node-compute budget |
| --- | --- |
| m7g.large primary for 120 hours | $15.17 |
| Same primary plus a replica for 24 hours | $18.20 |
| r7g.large primary and replica for 120 hours each | $42.05 |

Runner, PostgreSQL, backup and transfer costs are excluded. Production cost remains unmeasured: complete Valkey size, real concurrent sets, traffic, TTLs, working-set requirements and managed performance are missing. Existing 10/50/200-set and PostgreSQL RAM-fraction calculations are sensitivities only; burstable RDS CPU-credit charges are unmodeled. The final comparison requires complete measurements and documented traffic assumptions.

## Completion and next steps

- **Ten managed tests:** not met; local partial results do not close the original AWS scope.
- **PostgreSQL comparison:** locally measured; the paired ElastiCache arm is incomplete.
- **Final decision log:** not complete; PostgreSQL remains the existing default pending the original gate.
- **C-ticket confirmation or rewrite:** not complete; no final store-specific confirmation follows from this experiment.
- **Measured production cost:** not complete; published-rate illustrations are available.
- **Cleanup:** complete for the local run. Its containers, networks and volumes were verified removed at 08:44:20.588 on 12 November 2026, America/Fortaleza. No AWS resources were provisioned by this run. (E6)

Complete the spike in the SRE-provided throw-away node-based ElastiCache environment, with the required Valkey/Search capabilities, eviction disabled and a VPC runner. Establish team acceptance criteria before that run, explicitly deciding how the local memory screen applies.

First establish complete-set sizing and investigate the module workaround/indexing counters. Then run all ten scenarios and matching PostgreSQL measurements under documented conditions, preserving evidence for genuine stop conditions. Update the cost comparison from complete measurements and traffic assumptions. Evaluate the original gate, record the final decision, confirm or rewrite the C tickets, and verify removal of all temporary resources.

Store-independent A/B work can continue in parallel. **The spike ticket remains open and inconclusive.**

## Evidence appendix

These attachments are pinned to the published repository revision. They preserve the measurements independently of later branch changes.

- **E1 — Setup and local criteria:** [Execution record](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/run.json), [pre-run screens](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/thresholds.json).
- **E2 — PostgreSQL scale:** [Five-cycle measurements, samples, plans and IO](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/postgres-scale-1.json).
- **E3 — Semantic reference and comparison:** [PostgreSQL results](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/postgres-parity-1.json), [Valkey’s 84 cases](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/valkey-parity-1.json).
- **E4 — Valkey scale:** [Incomplete fill, allocation, OOM and cleanup](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/valkey-scale-1.json).
- **E5 — Price inputs:** [Published AWS rates and retrieval details](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/aws-prices.json).
- **E6 — Resource removal:** [Local teardown verification](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/teardown.json).
- **E7 — Earlier attempt and validation:** [Earlier Valkey observations](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/integration-attempts/005-null-contains-server-abort/valkey-parity-1.json), [server crash diagnostic](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/integration-attempts/005-null-contains-server-abort/interruption-docker.log), [validation record](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/7c2fd090762fb91c1060a63565363645ae8f619a/tools/cache-store-spike/evidence/dwllt3955-20261112-r1/validation.json).
