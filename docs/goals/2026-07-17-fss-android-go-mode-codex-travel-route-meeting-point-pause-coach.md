# FSS Android /go continuation — Travel Route & Meeting Point Pause Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `e72328a30f2d01b714550d60034b512e6ed54975` — `feat: add doorstep handoff refusal coach`

Parent evaluator reconciled the Hermes background notification: wrapper showed exit code 1/trailing `logout`, but the process log and final output had `CODEX_RC=0`, a local commit, and a DONE summary.

Parent-side repo evidence after the commit:

```bash
git status --short --branch --ignored=matching
# ## main...origin/main [ahead 46]
# !! .gradle/
# !! .tools/
# !! app/build/
# !! local.properties

git show --stat --oneline --summary HEAD
# e72328a feat: add doorstep handoff refusal coach
# 8 files changed, 3776 insertions(+), 41 deletions(-)
```

Parent verification command:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
python3 - <<'PY'
from pathlib import Path
apk = Path('app/build/outputs/apk/debug/app-debug.apk')
print(f'APK_EXISTS={apk.exists()}')
if apk.exists(): print(f'APK_BYTES={apk.stat().st_size}')
PY
git diff --check
git status --short --branch --ignored=matching
```

Result: `BUILD SUCCESSFUL`; APK exists with `1594739` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 46 commits.

Focused JUnit XML after the doorstep slice:

- `DoorstepVisitorHandoffRefusalCoachTest`: debug 17/17, release 17/17, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 27/27, release 27/27, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 37/37, release 37/37, failures 0, errors 0, skipped 0.

Manifest/source evidence:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, media/storage, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>`, no `<service>`, no `<receiver>`.
- Bounded doorstep slice implementation scan after stripping string literals: `SLICE_CODE_FORBIDDEN_HITS=[]`.
- The raw bounded slice text contains `CCTV` only in Korean explanatory privacy copy saying the coach does not access CCTV/camera/doorbell/intercom. It is not code/API usage.
- `DoorstepVisitorHandoffRefusalCoach.kt` has no Android imports and no raw analysis field access (`input`, `score`, `actionPlan`, `summary`, redacted clue).
- UI uses `incidentCount = incidents.count()` and `FamilyReadinessBuilder.build(safetyPlan.load())`; no `incidents.load`, no persistence mutations.
- Share helper uses exactly manual `Intent.ACTION_SEND`, `text/plain`, `putExtra(Intent.EXTRA_TEXT, summary)`, and no `Intent.EXTRA_STREAM`.
- Expected repo-wide legacy hits remain only in established official-help/report paths (`Intent.ACTION_DIAL`, `Uri.parse`, `tel:`, `OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`) plus explanatory privacy-copy tokens (`OCR`, `RECORD_AUDIO`) and new feature class/copy hits.

Parent source inspection found no obvious doorstep privacy/routing issue: `DoorstepVisitorHandoffRefusalCoachBuilder` uses only `analysis?.riskLevel`, `analysis?.matchedSignals`, `readiness.level`, and `incidentCount.coerceAtLeast(0)`; cards and UI show fixed value-free refusal scripts and route to existing flows; README/Privacy Center document no permissions/network/location/map/address/visitor identity/courier tracking/official lookup/dial/SMS/contact/media/file/clipboard/credential/account/device/scheduler/storage/mutation behavior.

## Product intent for next cohesive slice

Add a **Travel Route & Meeting Point Pause Coach** for the moment a scammer is directing the user to move: leave home/work, take a taxi/subway/bus, drive, walk to an ATM/bank/police/prosecutor/FSS office/courier counter/parking lot/lobby/locker/store, meet a person, follow a route, keep the call open, hide the destination, or arrive by a deadline.

This slice should be a local, stateless, value-free coach that gives short “stop moving / do not travel / do not meet / do not reveal route” scripts and routes into existing app flows. It must **not** use maps/location/navigation/geocoder/address validation, route planning, ride-hailing/taxi APIs, transit APIs, calendar/scheduling, courier tracking, meeting scheduling, emergency dispatch, official lookup, dial/SMS/contact/call-state, bank/payment/card/account actions, device settings, evidence/file/photo/media attachment, or persistent state.

Good user story:

> “Someone says I must go somewhere right now — bank, ATM, police station, courthouse, courier counter, parking lot, convenience store, lobby, or a meeting point — while keeping the call open and hiding it from family. Before I move or tell them where I am, show me a short stop script and safe existing routes without recording my address, route, destination, vehicle, fare, amount, person, or incident details.”

This complements cash/courier handoff, bank/ATM staff disclosure, doorstep refusal, official pressure, and active-contact exit. It is about **movement and meeting-point pressure** rather than direct cash/card/payment implementation.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `TravelRouteMeetingPointPauseCoach.kt`:
   - `TravelRouteMeetingPointPauseCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): TravelRouteMeetingPointPauseCoach`
   - Pure/stateless; no Android imports.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as a boolean/count category only
   - Must not use/display/share raw analysis input, score, action plan, summary, redacted clue, incident bodies/previews, `IncidentPreviewRedactor`, saved incident contents, `SafetyPlan.safeWord`, callback script literals, contact labels/numbers, official directory entries/numbers, exact home/work/building/lobby/unit/address/current location/route/destination/landmark/parking/elevator/intercom/door/locker/store/station/vehicle/taxi/bus/subway/ride/fare/arrival/departure/ETA values, visitor/courier/security/repair/police/prosecutor/FSS/bank/platform names, phone numbers, URLs/domains, QR/barcode/code values, package/app/file/media/URI values, cash amounts/serials/banknotes, account/card/bank/payment/wallet/transaction values, OTP/PIN/password/security answers, resident IDs, device IDs, caller ID/call state/SMS/contact/notification state, recipient/helper/lawyer/investigator/courier/driver names, case/report/submission/receipt/status values, arrived/departed/boarded/routed/scheduled/met/delivered/collected/deposited/handoff/payment/refund/chargeback/recovery/freeze/cancel/block/trace outcomes, or legal/financial result values.

2. Pause/script cards:
   - Fixed manual cards, for example:
     - “출발하기 전 통화가 정한 이동 지시 멈추기”
     - “목적지·현재 위치·교통수단·도착 시간을 말하지 않기”
     - “은행·ATM·기관·법원·경찰서로 가라는 압박을 현실 점검으로 분리하기”
     - “택배/퀵/보관함/주차장/로비 만남 지시를 값 없이 멈추기”
     - “통화·채팅을 끊고 이동 대신 가까운 안전한 사람/장소에서 대기하기”
     - “가족·보호자·공식 도움·신고 준비·후속 점검으로 이어갈 다음 경로 고르기”
   - Each card should include:
     - id/display order/title
     - movement/meeting pressure situation sentence
     - short value-free pause/refusal script
     - why it matters
     - do-not-travel/navigate/share-location/meet/handoff/say/share/attach/record boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, financial/cash/payment signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`), official-pressure signals (`IMPERSONATION`, `SECRECY`, `SAFE_ACCOUNT_FRAUD`), live-call/app/link/courier-control signals (`LIVE_CALL_CONTROL`, `APP_INSTALL`, `LINK`, `PARCEL_INVITATION_SMISHING`), credential/OTP/gift-card signals (`CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), recovery-promise contexts (`BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), family emergency/deepfake/phone-number-change, saved incident count, and low/in-progress/ready readiness.
   - No-analysis/default should be conservative: show all pause cards and never claim a route, destination, visitor, courier, guard, driver, staff member, police/prosecutor/FSS/bank/platform person, document, case, parcel, address, route, meeting, travel, vehicle, payment, request, handoff, device, account, card, or report is official, verified, safe, valid, accepted, submitted, received, protected, legal, resolved, complete, blocked, cancelled, reversed, recovered, refunded, frozen, traced, delivered, collected, deposited, scheduled, arrived, departed, boarded, guaranteed, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `travelRouteMeetingPointPauseCoachScreen()`.
   - Add route buttons from relevant screens:
     - Home dashboard/readiness section.
     - Analysis result.
     - Quick Stop.
     - Cash Withdrawal & Courier Handoff Pause Coach.
     - Bank/ATM Staff Disclosure Script Coach.
     - Doorstep Visitor & Home Handoff Refusal Coach.
     - Money Transfer Pause Gate.
     - Official Pressure Reality Check Coach.
     - Active Contact Exit Scripts.
     - Recovery Promise Reality Check Coach.
     - Link & App Install Quarantine Coach.
     - Gift Card Code Pause Coach.
     - Credential Secret Freeze Coach.
     - Account & Device Lockdown Checklist.
     - Remote Access Cutoff Coach.
     - Exposure Impact Triage.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - New Number Quarantine Coach.
     - Voice Impersonation Reality Check Coach.
     - Evidence Handoff Privacy Coach.
     - Evidence Kit / Incident Timeline if appropriate.
     - Official Help Directory and Official Report Prep Card.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate route planning, location lookup, map/navigation, destination validation, official lookup, dialing, browsing, report submission, legal advice, ride hailing, transit, courier tracking, meeting scheduling, bank/cash/card/account/payment, device settings/freeze, evidence/media/file/photo, contact/SMS, or family/contact implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.

4. Privacy Center / README:
   - Update `PrivacyCenter.kt` inventory/copy so this new coach is named as local/manual/no-permission/no-network/no-map/no-location/no-route/no-navigation/no-geocoder/no-address validation/no current-location/home/work/building/lobby/unit/elevator/parking/intercom/door/locker/store/station values/no route planning/no traffic/transit/taxi/ride-hailing APIs/no meeting scheduling/no courier tracking/no official lookup/no caller-ID/phone validation/no document/case validation/no report submission/no dial/no SMS/no contacts/calls/notifications/no QR/barcode/OCR/camera/no file/media/no evidence attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/gift-card action/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `TravelRouteMeetingPointPauseCoachTest.kt`.
   - Update privacy/source regression tests and Privacy Center tests.
   - Include tests proving:
     - default/no-analysis is conservative, recommends all pause cards/actions, and avoids unsafe official/verified/safe/completed/status claims.
     - financial/cash/payment signals route to cash/courier pause, bank staff disclosure, doorstep refusal, money pause, active-contact exit, official pressure, separate-channel verification, account/exposure/evidence/helper/help/report without cash/card/payment/account actions or map/location/address/route/destination/vehicle behavior.
     - official pressure/secrecy routes to official pressure, active contact exit, separate-channel verification, family alert/debrief, evidence/helper/help/report without official-number lookup, destination/staff/visitor/driver identity check, document/case validation, dial/browser/SMS/report submission.
     - live-call/app/link/courier-like pressure routes to active contact exit, link/app quarantine, remote cutoff, cash/courier pause, doorstep refusal, exposure/account/privacy without call-state APIs, app/device/settings/package APIs, maps/location/route/navigation/courier tracking, travel scheduling, or remote disconnect/device cleaning claims.
     - credential/OTP/gift-card signals route to credential freeze, gift-card pause, account lockdown, safe paste/privacy, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card/bank/wallet/gift-card/cash values.
     - family emergency/deepfake/phone-number-change routes to separate-channel verification, family callback, voice reality, new-number quarantine, safe-word reset, safe call scripts, family alert without contact/safe-word/callback values or contacts/calls/SMS.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness routes to safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make route/destination/meeting/request/travel/handoff safe/trusted/official.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic pause/card/route/action labels and no raw input, redacted clue, plan literals, safe-word/callback/contact values, phone numbers, addresses/routes/locations/home/work/unit/lobby/parking/intercom/door/locker/store/station/vehicle/taxi/bus/subway/ride/fare/arrival/departure/ETA/staff/courier/driver IDs, URLs/domains, long numbers, QR/barcode/code/package/app/file/media/URI/path values, account/card/bank/wallet/transaction/amount/cash serial/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, agency/helper/lawyer/investigator/staff/courier/visitor/driver names, case/report/submission/receipt IDs, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, arrived/departed/boarded/routed/scheduled/met/delivered/collected/deposited/handoff/payment/refund/chargeback/recovery/freeze/cancel/block/trace status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no location/map/route/navigation/geocoder/address validation/no current location/destination/traffic/transit/taxi/ride hailing/no route planning/no meeting scheduling/no courier tracking/no bank/cash/card/payment action/no official lookup/no phone/caller/document/case validation/no report submission/no dial/call/SMS/no contacts/call-log/notifications/no QR/barcode/OCR/camera/no file/photo/media/no attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no permissions/no scheduler.
   - Add/extend source-reading tests proving model and bounded UI slice avoid all forbidden APIs/values. Allow `openHelpDirectory(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)` only in the action router as a route to the existing official-help screen.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No map/location/navigation/geocoder/address validation, route planning, current-location detection, traffic/transit/taxi/ride-hailing APIs, meeting scheduling, courier/parcel/locker tracking, official lookup, emergency dispatch, report submission, legal/case lookup, caller-ID checking, phone-number validation, or document validation.
- No bank/ATM/cash/card/account/payment operation, refund/chargeback/recovery/freeze/cancel/trace, app/device/settings mutation, or financial/legal automation.
- No network/browser/webview/map/geocoder/navigation APIs and no `Intent.ACTION_VIEW`.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs.
- No QR/barcode scan, OCR, camera, image analysis, screenshot, file/media picker, attachment, stream, URI share, upload, or evidence sync.
- No PackageManager/app inventory/app label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats/notification-listener APIs, app permission/grant/revoke APIs, malware scanning/antivirus APIs, or device-control behavior.
- No display/share/use of official directory phone numbers, agency/bank/branch/store/brand/staff/helper/lawyer/investigator/courier/visitor/driver names, exact home/work/building/lobby/unit/address/route/current-location/destination/meeting/parking/intercom/door/locker/store/station/vehicle/taxi/bus/subway/ride/fare/arrival/departure/ETA values, cash amounts/serials/banknotes, exact links/domains, QR/barcode/code values, package/app/file/media/URI values, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, device IDs, exact phone/channel/profile values, or any arrived/departed/boarded/routed/scheduled/met/delivered/collected/deposited/handoff/payment/refund/chargeback/recovery/freeze/cancel/block/trace status values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, location/map/route/traffic/transit/taxi/ride/courier/parcel/locker/meeting/home/door state, bank/ATM/cash/payment/card/account state, link/app/package/permission/device state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status.
- No alarms, reminders, CalendarContract, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/wallet/link/app/location/route/destination/vehicle/staff/visitor/courier/driver/cash/device/result value.
- Do not claim a route, destination, meeting point, vehicle, taxi, bus, subway, driver, visitor, staff member, helper, courier, investigator, institution, bank, branch, address, home, door, lobby, locker, parcel, cash, document, case, report, payment, account, card, bank action, wallet, transaction, credential, login, device, location, handoff, purchase, redemption, balance, chargeback, refund, freeze, trace, cancellation, recovery, app install/removal/permission, or malware/device state is official, real, safe, secure, verified, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, recovered, refunded, frozen, protected, legal, trusted, delivered, collected, deposited, scheduled, arrived, departed, boarded, malware-free, guaranteed, or problem-free.

## Verification gate before commit

Run from repo root:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
python3 - <<'PY'
from pathlib import Path
import re
manifest = Path('app/src/main/AndroidManifest.xml').read_text()
for token in ['CALL_PHONE','READ_SMS','SEND_SMS','RECEIVE_SMS','READ_CALL_LOG','READ_CONTACTS','WRITE_CONTACTS','READ_PHONE_STATE','ANSWER_PHONE_CALLS','RECORD_AUDIO','CAMERA','READ_MEDIA_IMAGES','READ_MEDIA_VIDEO','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','INTERNET','ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','PACKAGE_USAGE_STATS','QUERY_ALL_PACKAGES','BIND_ACCESSIBILITY_SERVICE','BIND_DEVICE_ADMIN','BIND_NOTIFICATION_LISTENER_SERVICE','BIND_VPN_SERVICE','REQUEST_INSTALL_PACKAGES']:
    print(f'{token}={token in manifest}')
needle='android:allowBackup="false"'
print(f'allowBackup=false={needle in manifest}')
print(f'uses-permission-tag={"<uses-permission" in manifest}')
service_or_receiver = bool(re.search(r'<(?:service|receiver)\\b', manifest))
print(f'service-or-receiver={service_or_receiver}')
PY
python3 - <<'PY'
from pathlib import Path
apk = Path('app/build/outputs/apk/debug/app-debug.apk')
print(f'APK_EXISTS={apk.exists()}')
if apk.exists(): print(f'APK_BYTES={apk.stat().st_size}')
PY
git diff --check
git status --short --branch --ignored=matching
```

Also inspect focused JUnit XML for `TravelRouteMeetingPointPauseCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Travel Route slice itself does not contain the forbidden implementation APIs or values. `openHelpDirectory(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)` is allowed only in the UI action router.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-travel-route-meeting-point-pause-coach.md
git commit -m "feat: add travel route pause coach"
```

Do **not** push.

## Final response schema

Return a concise final summary with:

- Files changed count and key files.
- Verification command and result.
- Focused JUnit counts for the new tests plus Privacy Center and privacy regression tests.
- Manifest/API scan result.
- APK path and byte size.
- Commit SHA and message.
- Worktree status and ahead count.
- Explicit `No push performed`.
