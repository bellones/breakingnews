## Title

PdW | Contribution settings | Dedicated opt-out layout when disabling non-contributing categories

## Type

Story / UI Enhancement

## User Story

As a Personal dWallet user with multiple DSP plans,  
I want a clear layout when I choose to stop contributing categories,  
so I understand which categories will be deactivated and the impact before saving.

## Context

In the **Manage contribution** flow (`/accounts/[dsAccountId]/manage/contribution`), when the user taps to **add categories** in the deactivated / non-contributing section, the app opens the **turn-off picker** (`activeCheckboxPlanId === unattributed`).

**Current behavior (works correctly):**
- Checking categories removes them from plans and deactivates them (ownership → `null`)
- Changes persist via `useUpdateDataSavingsAccounts` on save
- Plan grouping (DSP header + checkbox list) partially exists today

**Problem (UX/UI gap):**
- There is no **dedicated layout** for this opt-out flow, per Subash’s design
- The screen reuses the generic contribution edit header/copy (“select title”, description, info box), which confuses user intent
- The **impact warning** (red destructive text) before the CTA is missing
- Copy and visual hierarchy do not match the attached Figma

## Design reference

Figma / Subash mock (attached):
- Title: **Contribution settings**
- Subtitle: **Choose categories that you'd like to stop contributing.**
- Secondary text: **These settings will be applied to any future contributions.**
- List grouped by plan (e.g. dSavings Smart, dSavings Fitness) with per-category checkboxes
- Warning (red): **Data in selected categories will be turned off—it won't be licensed or monetized for future offers.**
- Primary CTA: **Save changes**

## Technical scope (reference)

| Area | File / route |
|------|----------------|
| Main page | `apps/web/src/personal/components/pages/plan-contribution/NewEditContributionsPage.tsx` |
| Route | `apps/web/src/app/personal/accounts/[dsAccountId]/manage/contribution/page.tsx` |
| Turn-off mode | `isTurnOffPickerMode` (`activeCheckboxPlanId === DSA_CONTRIBUTION_SECTION_ID_UNATTRIBUTED`) |
| Checkbox list | `DsaContributionCheckboxList.tsx` |
| Plan header | `DsaContributionDspHeader.tsx` |
| i18n namespace | `savings-plan-manage` (+ possibly `shared` for CTA) |

**Note:** Existing keys like `contributions.categories-off.title/description` may be reused or extended. New strings must be added in **Ditto** (do not edit READONLY files under `src/i18n/ditto/`).

## Acceptance Criteria

### Layout / copy (turn-off picker mode)
- [ ] When entering the disable-categories flow (tap “+” on unattributed/turned-off section), the screen shows the Figma layout, **not** the generic contribution selection header
- [ ] Title, subtitle, and secondary text match the design (via Ditto)
- [ ] Categories are shown grouped by DSP plan, with plan header + checkbox per category
- [ ] Impact warning in destructive/red styling appears above the save button
- [ ] CTA shows **Save changes** in this mode

### Behavior (no regression)
- [ ] Checking a category still removes it from the plan and deactivates it
- [ ] Categories that are the sole contribution on a plan remain disabled (cannot be turned off) — preserve current behavior
- [ ] Save stays disabled until there is a draft change (`hasChange`)
- [ ] Back returns to main drag mode without incorrect state loss — preserve current behavior

### Quality
- [ ] Tests updated in `apps/web/tests/app/personal/accounts/[dsAccountId]/manage/contribution/page.test.tsx`
- [ ] Storybook updated (turn-off picker variant), if applicable
- [ ] Strings added in Ditto before merge

## Out of scope

- Ownership/persistence logic changes (already works)
- **Add categories from other plans** picker mode (normal checkbox mode) — separate layout/ticket if needed
- `PlanContributionSettingsPage` (join flow at `/plans/[dspId]/contribution`) — unless design also applies there

## How to test

1. Enable feature flag `pdwMultipleDspRelease`
2. User with **2+ DSP plans** and active categories across plans
3. Go to **Manage plan → Contribution settings** (`/accounts/{dsaId}/manage/contribution`)
4. Tap **“+”** on the deactivated / non-contributing section
5. Verify Figma layout, copy, and red warning
6. Check categories (e.g. Travel & Transportation on dSavings Fitness) → **Save changes**
7. Confirm categories were removed/deactivated on plans after refresh

## Dependencies

- Final design from Subash (Figma link, if available)
- Copy/strings in Ditto (`savings-plan-manage` namespace)

## Suggested labels

`PdW`, `DSP`, `Contribution`, `UX`

## Epic / component

Personal dWallet — Data Savings Plans / Manage Contribution
