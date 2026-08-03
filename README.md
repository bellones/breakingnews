# Study: Oversized images uploaded to storage (Oobeo Operator App)

**Requested by:** Midhet (backend / bucket review)  
**App:** Oobeo Valet Operator (`oob_app_5` / `OobeoApp`)  
**Date:** 2026-08-03  
**Environment impact:** Production S3 / photograph storage via `POST photographs/upload/`

---

## Summary

Client photo uploads (visit/car detail photos and validation photos) can still land in the bucket larger than intended. The app **has** a compression path (`@bam.tech/react-native-image-resizer`), but several gaps mean full-resolution captures often skip resize, use incorrect metadata, or fall back to the original file.

**Likely outcome in the bucket:** multi‑MB JPEGs (especially modern Android/iPhone sensors), instead of a consistent ~200–500KB / ≤1080px asset.

---

## Upload pipeline (current)

```
Camera takePhoto (VisionCamera)
    → validateImageDimensions()   // LOG ONLY — does NOT resize pixels
    → submitPhoto / submitValidationPhoto
        → FileUploader.upload()
            → processImageForUpload()
                → compressImageForDetailPhotos()  // real resize (when it runs)
                → validateImageDimensions()       // log only again
            → POST {API_URL}photographs/upload/  (multipart)
```

| Step | File | Behavior |
|------|------|----------|
| Capture | `CarDetails.js`, `useReceives.js`, `useCarDetails.js`, validation modals | `react-native-vision-camera` `takePhoto` |
| Pre-submit “validate” | `imageUtils.validateImageDimensions` | Computes optimal size but **does not rewrite the file** |
| Upload | `submitPhoto.js` → `FileUploader.js` | Calls `processImageForUpload` then multipart POST |
| Compress | `imageUtils.compressImageForDetailPhotos` | ImageResizer → max ~1080px, JPEG quality **50**, skip if ≤**900KB** and dims ≤**1600** |
| Endpoint | `photographs/upload/` | Backend stores to bucket |

**Not the same path:** License plate OCR uses `compressImageForLicensePlate` / ViewShot (target ~480×360, ~200KB) for scanning APIs — separate from visit photograph bucket uploads.

---

## Intended limits (as coded today)

| Setting | Value | Where |
|---------|-------|--------|
| Camera format preference | ≤1080px max side | `cameraUtils.getOptimalCameraFormat` |
| Detail compress max dimension | Param default **1600**; actual resize uses `getOptimalImageDimensions` default **1080** | `compressImageForDetailPhotos` |
| Skip compress if file ≤ | **900 KB** | `compressImageForDetailPhotos` |
| JPEG quality when compressing | **50** | `compressImageForDetailPhotos` |
| Upload timeout | 30s | `FileUploader._headers` |

Comments in code still say “1080px max” and “camera quality settings instead of post-processing,” but **format selection alone does not guarantee capture resolution** (especially iOS).

---

## Root causes (why bucket objects are still large)

### 1. `validateImageDimensions` does not resize (misleading name)

```js
// imageUtils.js — only logs; returns same uri
console.warn('Image will be uploaded at full size...');
```

Callers (`CarDetails`, Receive, etc.) treat this as “processed,” but the file on disk is unchanged until `FileUploader` runs.

### 2. Fake width/height from camera **format**, not the photo file

Example from `CarDetails.js`:

```js
const validatedData = await validateImageDimensions({
  ...data,
  width: formatRef.current?.videoWidth || 1920,
  height: formatRef.current?.videoHeight || 1080,
});
```

VisionCamera often captures at **sensor resolution** (e.g. 4032×3024) while metadata passed downstream is the selected format (e.g. 1080×720).

**Impact on skip logic in `compressImageForDetailPhotos`:**

```js
const meetsDimensions = imageData.width <= 1600 && imageData.height <= 1600;
const meetsSize = fileInfo.sizeKB <= 900;
if (meetsSize && meetsDimensions) return imageData; // skip — NO resize
```

If the **reported** dims are ≤1600 and the file happens to be ≤900KB, compression is skipped even when the **true** resolution is much higher.  
If width/height are missing entirely, compress **returns immediately** with the original file.

### 3. Skip threshold (900KB) is still large for storage / mobile networks

Even when “within limits,” ~900KB × N photos per visit adds up quickly in S3 and slows uploads on poor LTE.

### 4. Failures silently upload originals

| Failure | Fallback |
|---------|----------|
| `compressImageForDetailPhotos` throws | returns original `imageData` |
| `processImageForUpload` throws | returns original |
| `FileUploader.upload` processing catch | POSTs **original** multipart body |

Any ImageResizer / path / iOS URI glitch → full-size upload.

### 5. Capture settings favor quality over size

`getOptimalPhotoOptions` uses `qualityPrioritization: 'quality'`.  
Validation flows use `'speed'` (better), but detail photos do not.

### 6. No hard max bytes before POST

There is Alert text for “File too large” in `submitPhoto`, but no client-side reject if compressed size still exceeds a budget (e.g. 500KB). Backend/bucket receives whatever multipart sends.

### 7. Batch uploads multiply cost

Receive / CarDetails can queue **multiple** photos (`Promise.all` / `forEach` submit). Large per-file size × count = timeouts (already partially why compress was added) and bucket bloat.

---

## Flows that hit the bucket

| UX | Entry | Upload helper |
|----|--------|----------------|
| Receive / Park photos | `useReceives` / `ReceiveContainer` / `CarDetails` | `submitPhoto(image, visitId, carId)` |
| Deferred photos after visit create | same, from `imageArray` | `submitPhoto` batch |
| Pay-at-stand validation image | `usePayAtStands` | `submitValidationPhoto` |
| Event parking validation image | `useEventParkingPayments` | `submitValidationPhoto` |

All share `FileUploader` → same compress gaps.

---

## Recommended fix (product + engineering)

### Target policy (proposed)

| Metric | Target |
|--------|--------|
| Max dimension | **1280px** (or 1080px) longest side |
| JPEG quality | **60–70** (tune vs readability for damage/plate context) |
| Max file size after compress | **≤ 400–500 KB** (hard retry/recompress if over) |
| Format | Always JPEG for upload (no PNG) |
| Metadata | Read **actual** image dimensions (Image.getSize / resizer output), never format videoWidth/Height |

### Implementation outline

1. **Unify** one `preparePhotographForUpload(uri)` used by all capture paths.  
2. Always run ImageResizer with `onlyScaleDown: true` unless file already ≤ target bytes **and** actual dims ≤ max.  
3. If still over max KB → second pass (lower quality / smaller max edge).  
4. If still over → block upload + user message (do not POST original).  
5. Fix callers to stop injecting format dimensions; use `Image.getSize` or takePhoto metadata.  
6. Prefer `qualityPrioritization: 'balanced'` or `'speed'` for detail capture.  
7. Add metrics: log `originalBytes`, `finalBytes`, `resized` to Crashlytics/Bugsnag custom keys for Midhet’s validation.  
8. Unit tests for compress skip/resize/fallback; fixture images at 12MP and 2MP.

### Out of scope (follow-ups)

- Server-side image transcoding on `photographs/upload/` (defense in depth — recommended later).  
- License-plate OCR pipeline (already more aggressive).  
- Historical bucket cleanup / reprocess of old oversized objects.

---

## Acceptance criteria (for implementation ticket)

- [ ] Visit detail photo upload: longest side ≤ 1280px (or agreed limit)  
- [ ] Typical outdoor photo ≤ 500KB after client compress  
- [ ] Missing/incorrect width/height never causes skip of compress  
- [ ] Compress failure does **not** upload multi‑MB original without retry/block  
- [ ] Validation photos use the same pipeline  
- [ ] Logging shows before/after size in release builds (remote-friendly)  
- [ ] QA: Pixel + recent iPhone, 1 and 4 photos per visit, slow network  

---

## Jira ticket (copy-paste)

### Summary

```
Oobeo app: reduce visit/validation photo size before S3 upload (oversized bucket objects)
```

### Description

```markdown
h2. Context

Midhet reported that images uploaded from the Oobeo Operator app to the photograph storage bucket are significantly larger than expected.

Client: all clients using visit/car detail photos and validation photos  
Product: Oobeo Valet Operator App  
Endpoint: {{POST photographs/upload/}}

h2. Diagnosis (app study)

Upload path: VisionCamera {{takePhoto}} → {{validateImageDimensions}} (log only) → {{FileUploader}} → {{processImageForUpload}} → {{compressImageForDetailPhotos}} → multipart upload.

Gaps causing oversized objects:

# {{validateImageDimensions}} does *not* resize pixels (name is misleading).
# Call sites pass *camera format* width/height (e.g. videoWidth/videoHeight or 1920x1080 defaults), not actual captured photo dimensions — can incorrectly skip compression when size ≤ 900KB.
# Compress skip threshold is 900KB / 1600px — still large for storage and flaky networks.
# On ImageResizer/processing failure, code falls back to uploading the *original* full-resolution file.
# Capture uses {{qualityPrioritization: 'quality'}}; format selection ≤1080px is not guaranteed by the OS/camera stack.

Evidence in code: {{app/lib/imageUtils.js}}, {{app/lib/FileUploader.js}}, {{app/components/CarDetails.js}}, {{app/containers/receives/useReceives.js}}, {{app/lib/submitPhoto.js}}.

Full write-up: {{OobeoApp/docs/image-upload-size-study.md}}

h2. Proposed solution

# Single prepare-for-upload helper for all photograph uploads.
# Always compress using real dimensions (Image.getSize / resizer); target ≤1280px longest side, JPEG quality ~60–70, hard cap ~400–500KB with second-pass recompress.
# Never upload original on compress failure without user-visible failure / retry.
# Stop using format videoWidth/Height as photo dimensions.
# Add before/after size logging (Crashlytics attributes) for backend validation.
# Tests + QA on Android + iOS with multi-photo visits.

h2. Acceptance criteria

# Uploaded visit/validation photos ≤ agreed max dimension and ~≤500KB typical.
# No skip of compress due to missing/fake metadata.
# No silent full-size fallback on compress errors.
# Midhet confirms sample objects in bucket meet size policy after release.

h2. Severity

S2 High (storage cost, upload timeouts, poor field network UX) — platform-wide.
```

### Labels / fields (suggested)

| Field | Value |
|-------|--------|
| Project | OOB |
| Type | Story / Bug |
| Priority | High |
| Labels | `mobile`, `photos`, `s3`, `performance`, `storage` |
| Component | Operator App — Photographs |
| Linked | Backend bucket review (Midhet) |

---

## References

- `@bam.tech/react-native-image-resizer` in `package.json`  
- `FileUploader.js` → `photographs/upload/`  
- Prior camera work: format selection in `cameraUtils.js` (1080 preference) — necessary but insufficient alone
