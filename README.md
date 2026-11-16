# Cache store spike managed results

16 November 2026 · DWLLT-3955 · Evidence cutoff 18:29 UTC

## Current verdict

**Retain PostgreSQL as the provisional baseline under the tested configuration. The managed comparison remains incomplete.** Valkey demonstrated working query semantics on the small fixture, leases, isolation and sustained ingestion. Several measured scale queries exceeded the frozen latency limits; expiry correctness and full-memory recovery remain unresolved, and failover acceptance is incomplete. These results concern this prototype and configuration, not an optimized performance limit or a general rejection of Valkey.

The original rule remains unchanged: switch only if Valkey passes all ten tests and fixes a measured PostgreSQL problem. No selected PostgreSQL screen was exceeded. Later diagnostics cannot retroactively pass the stopped original run or clear its expiry finding.

## What ran

The original managed run stopped at expiry test 5. At the user's request, a separately frozen continuation investigated the other nine tests through **15 planned cases**: small and scale cases plus two failover phases. **13 cases have completed receipts: eight passed, three failed latency limits and two stopped.** The fourteenth case, mid-fill failover, ran but has an incomplete final safety receipt. The fifteenth, failover after completion, did not run. These are cases, not fifteen different tests. Tests 6, 7 and 9 passed every planned phase.

The environment was node-based ElastiCache Valkey **9.0.0**, one primary and one replica on cache.m7g.large, TLS and noeviction. The existing PostgreSQL **17.12** measurements used an isolated two-CPU, 4-GiB runner container. The same fixtures, entity/spec identity and numerical criteria were retained. Limits were agent-selected under user delegation and frozen before writes, not team-approved production service levels.

The continuation reused the original PostgreSQL results as immutable semantic references. It did not rerun PostgreSQL performance. See the [diagnostic report](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-d1/DIAGNOSTIC-RESULTS.md) and [extracted measurements](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-d1/recorded-diagnostic-metrics.md).

## Ten-test disposition

1. **Operators and NULL behavior:** 24 small-fixture query cases passed. At 100,000 rows, the range query matched IDs/order and total **47,059**, but p95 was **392.563 ms against 250 ms**.
2. **Literal contains and search:** 52 small-fixture cases passed. The scale accent-contains query matched IDs/order and total **11,428**, but p95 was **1,204.965 ms against 500 ms**.
3. **Sorting:** eight small-fixture cases passed. The scale numeric-sort query matched IDs/order and total **100,000**, but p95 was **1,200.244 ms against 250 ms**.
4. **Pagination and exact totals:** stopped on the first, offset-zero query: p95 **1,207.895 ms against 1,000 ms**, with matching IDs/order and total **94,117**. Offsets 50,000 and 99,900 were not reached.
5. **Expiry and index lifecycle:** the original managed expiry stop remains. After server-confirmed expiry, the exact total was **one**, but the page was **empty**. Later checks found four relevant keys absent. **No expired row was returned.** Internal cause remains unproven; the remaining index-lifecycle checks were not reached.
6. **Leases and failure windows:** passed on both fixtures. Two racing clients produced one owner; heartbeat, takeover, stale-owner mutation rejection and failure-window reopening were observed.
7. **Fill and visibility:** passed three complete 100,000-row fills, detailed below.
8. **Memory and noeviction recovery:** entered the scenario, then stopped at a compound cleanup/refusal/state assertion. The failing subcondition and recovery timing were not serialized; this does not establish an unmanageable AWS memory failure.
9. **Caller isolation:** passed on both fixtures. Overlapping IDs and the same source hash remained independent; the unscoped negative control matched two other-caller documents while the scoped query matched none.
10. **Failover:** mid-fill was attempted but acceptance is incomplete; after-completion was not attempted.

Each failed scale query above retained **100 measured samples** after ten warmups. Its matching rows do not imply that later queries in the same method ran. The 84 small-fixture semantic passes are separate from scale coverage.

## Fill and memory observations

The three concurrent-read fills took **42.732, 42.508 and 43.285 seconds**, delivering **2,310–2,353 rows/second**, within the frozen minimum 2,000 rows/second and maximum 50 seconds. All acknowledged and immediately exposed 100,000 rows; observed counts progressed monotonically. Concurrent count-query p95 ranged **27.385–28.606 ms**. This narrower query workload does not contradict the slower filter/sort measurements above.

The complete 100,000-row setup for test 8 increased server allocation by **891,741,792 bytes, or 850.431 MiB**, over its empty-index baseline, below the 1,536 MiB screen. This includes document/index and store overhead; it is not required resident RAM or production sizing proof. The setup allocation is not a passing OOM-recovery result.

## Failover limits and cleanup

One AWS TestFailover request was accepted. The observer did not establish primary promotion and readable-fixture recovery within its 30-second criterion. A later replacement wait also timed out, and its retained history is incomplete; event-history reconciliation remains unavailable. This does **not** prove a 30-second service outage, a ten-minute AWS outage or data loss.

Immediate failover-fixture cleanup still counted 52 indexed rows. Subsequent global namespace cleanup removed **100,004 owned keys and the created index**, leaving none. At **18:29:28 UTC**, the [postflight](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-d1/postflight.json) recorded a healthy group with the primary changed from node 001 to 002. Those later observations do not establish recovery latency or pass the missing second phase. See the [raw failover attempt](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-d1/diagnostics/s10-mid/raw.json).

## PostgreSQL reference and remaining work

The original five 100,000-row cycles measured fills **2.099–2.374 seconds**, maximum query p95 **132.447 ms**, maximum ordinary vacuum **222.350 ms**, and retained heap/index growth **1.0912×**. Disk growth was **143.367 MiB initially and 156.484 MiB retained**. This synthetic warm-cache run is reference context, not a fresh paired comparison with the continuation. [PostgreSQL measurement scope](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-r1/postgres-measurement-summary.md).

Separate probes for the missing deep offsets, obsolete-index collection and individual OOM-recovery conditions are prepared but **not executed at this cutoff**, pending reconciliation. They cannot replace the original expiry result.

## Costs and publication

The [updated cost model](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-d1/cost-addendum.md) estimates **$24.73 of core infrastructure through 18:29 UTC**, assuming continuous availability and using the previously recorded creation times and dated rates. This is a limited scenario, not a bill or final total; ancillary services and later runtime are excluded. The measured complete-set allocation is now a capacity input, but production cost per set remains unvalidated. Cost Explorer access was denied; billed cost is unknown.

The [decision-log entry](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-d1/full-plan-decision-log-update.md) and [C-ticket handoff](https://github.com/reddrummer/dwallet.filter-engine-monorepo/blob/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d/tools/cache-store-spike/evidence/dwllt3955-aws-20261116-d1/c-tickets-update.md) are prepared for manual review/publication; no remote Confluence or C-ticket update is claimed. Actual C-ticket IDs remain unknown. AWS infrastructure remains provisioned at the user's request. Teardown is deferred; the recorded deadline is **18 November, 14:00 UTC / 11:00 Fortaleza**. Namespace cleanup does not establish infrastructure removal.

Report and evidence snapshot: [commit c85d373](https://github.com/reddrummer/dwallet.filter-engine-monorepo/commit/c85d37305cf5d60ecc794864dee1f5e25f3ffb0d).
