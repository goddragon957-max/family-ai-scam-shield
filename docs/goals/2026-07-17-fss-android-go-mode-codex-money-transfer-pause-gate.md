# FSS Android /go continuation — Money Transfer Pause Gate

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `9538544aa2fef5e2a3d239f37894ed0f9220756a` — `feat: add active contact exit scripts`

Parent evaluator reconciled the previous Hermes background notification, confirmed worker log had `### DONE`, `CODEX_RC=0`, and independently reran verification.

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

Result: `BUILD SUCCESSFUL`; APK exists with `1259005` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main`.

Focused JUnit XML evidence before this slice:

- `ActiveContactExitScriptsBuilderTest`: debug 16/16, release 16/16, failures 0, errors 0.
- `PrivacyCenterBuilderTest`: debug 10/10, release 10/10, failures 0, errors 0.
- `PrivacyPostureRegressionTest`: debug 20/20, release 20/20, failures 0, errors 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, `Intent.EXTRA_STREAM`, `ClipData`, URI grant flag, `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`, `TextWatcher`, `addTextChangedListener`, `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, or `PhoneStateListener`.
- Repo-wide source scan has one existing legacy `Intent.ACTION_DIAL` reference in `MainActivity.kt`; parent slice scan proved the active-contact slice does **not** contain `ACTION_DIAL` or `ACTION_CALL`.

## Product intent for next cohesive slice

Add a **Money Transfer Pause Gate** screen/card: when the scam pressure is about sending money, gift cards, crypto, a “safe account”, refund/loan fees, overseas payment, or revealing financial credentials, the user needs a simple manual “do not pay yet” checkpoint before any payment.

This should feel like a private family AI guardian, not a bank integration or payment blocker. It should help the family say:

1. No payment, gift card, crypto transfer, or account move before separate verification.
2. Do not trust safe-account / refund / loan-fee / urgent-family narratives from the current contact.
3. Do not share OTP/PIN/password/card/account details.
4. Use known official channels and trusted family callback.
5. Preserve evidence and choose existing app routes manually.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `MoneyTransferPauseGate.kt`:
   - `MoneyTransferPauseGateBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): MoneyTransferPauseGate`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level`, counts/labels only — **not** safe word/callback/contact values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, raw/pasted text, incident bodies, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, account/card numbers, OTP/PIN, resident IDs, or safety plan literal values.

2. Gate cards:
   - Fixed manual cards, for example:
     - “송금·결제 멈춤 선언”
     - “안전계좌·환급·대출 수수료 의심”
     - “상품권·기프티콘·가상자산 멈춤”
     - “OTP/PIN/비밀번호·카드정보 거절”
     - “가족/기관 별도 확인”
     - “증거·공식 도움으로 이어가기”
   - Each card should include:
     - title/label
     - pause sentence to read manually
     - why it matters
     - do-not-do boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high-risk, urgent transfer, credentials/OTP, safe-account fraud, loan/refund fee, gift card/gifticon, crypto, overseas payment alert, family/deepfake/number-change, secrecy/impersonation, saved incident, and low/in-progress readiness.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `moneyTransferPauseGateScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near Quick Stop / Active Contact Exit Scripts.
     - Analysis result screen.
     - Quick Stop screen.
     - Active Contact Exit Scripts.
     - Account Lockdown Checklist.
     - Official Help / Report Prep if appropriate.
     - Safety Flow Router or Recovery Plan if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate bank/official/account/evidence implementation.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add payment APIs, banking APIs, network calls, dial/call/SMS automation, or anything that claims to block, freeze, recover, refund, or transfer money.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new pause gate is named as local/manual/no-permission/no-network/no-payment-access/no-bank-integration/no-storage/no auto-send/no automatic blocking/freezing/recovery.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `MoneyTransferPauseGateBuilderTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default gate is conservative and does not call anything safe.
     - high risk / urgent transfer recommend payment pause, credentials refusal, account/evidence/official/follow-up routes.
     - credentials/OTP recommend no-code-sharing and account protection without claiming account freeze.
     - safe-account, loan/refund fee, gift card/gifticon, crypto, and overseas payment signals each recommend relevant pause cards and official/evidence/account routes.
     - family/deepfake/number-change recommends family callback before any money movement.
     - secrecy/impersonation recommends not keeping the payment secret and official route.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds callback/readiness/family support while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic pause/card/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, account/card/OTP/PIN/resident-like values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no contacts/no auto-send/no network/no permissions/no scheduler/no mutation/no payment access/no bank integration/no freeze/recovery automation.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “blocked”, no malware removal, no account/card auto-freeze, no payment cancellation guarantee, no refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling/payment action.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `SmsManager`, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, call/SMS automation APIs, and payment/banking automation APIs. Explicitly forbid `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `SmsManager`, `ClipboardManager`, `Payment`, `BillingClient`, `NfcAdapter`, `AccountManager`, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls.
- No automatic SMS/call/contact/clipboard reading.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No call blocking, call screening, auto dial, auto hang-up, auto SMS, or recipient choosing.
- No automatic redaction, scanning, monitoring, blocking, removal, freezing, transfer, refund, or recovery.
- No mutation of pasted text, call state, contacts, incident log, safety plan, account/card/bank/device settings, or money movement.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for card choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, payment status, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users to paste names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, or raw scam text.
- Do not claim official/legal/financial outcome, safety, scam certainty, malware removal, account/card freeze, payment cancellation, refund, recovery, blocking, or automatic protection.

## Implementation record

This slice is implemented as a deterministic, Android-free Kotlin builder with six fixed manual pause cards,
enum-ordered recommendations, count-only saved-incident presence, and native `MainActivity` rendering. Every card
action delegates to an existing app screen. The gate adds no payment or banking integration, financial API, NFC,
account-manager access, permission, network call, scheduler, background work, or feature-specific persistence.

Home, analysis result, Quick Stop, Active Contact Exit Scripts, Account Lockdown, Safety Flow Router, Recovery
Plan, Official Help, and Report Prep link to the gate. Its only outward action is a user-triggered `ACTION_SEND`
`text/plain` chooser that receives the builder-produced `familyShareSummary`; it never receives raw analysis,
redacted clues, incident bodies, safety-plan literals, contacts, phone/account/card values, or credential values.

Privacy Center, README, builder tests, and source-reading regressions inventory the gate as local-only, stateless,
manual, no-storage, no-auto-send, no-scheduler, no-mutation, no-payment-access, and no-bank-integration. The
regressions also preserve the no-permission manifest, disabled backup, and repository-wide scheduler, clipboard,
call/SMS automation, payment-library, NFC, and Android account-manager invariants.

## Verification gate before commit

Run from repo root:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
python3 - <<'PY'
from pathlib import Path
manifest = Path('app/src/main/AndroidManifest.xml').read_text()
for token in ['CALL_PHONE','READ_SMS','READ_CALL_LOG','READ_CONTACTS','RECORD_AUDIO','INTERNET','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED']:
    print(f'{token}={token in manifest}')
needle='android:allowBackup="false"'
print(f'allowBackup=false={needle in manifest}')
print(f'uses-permission-tag={"<uses-permission" in manifest}')
needles = [
    'AlarmManager','NotificationManager','NotificationCompat','WorkManager','JobScheduler',
    'CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS',
    'SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','ContactsContract','SmsManager',
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION',
    'ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener',
    'Intent.ACTION_CALL','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener',
    'BillingClient','NfcAdapter','AccountManager'
]
paths = list(Path('app/src/main').rglob('*.kt')) + [Path('app/src/main/AndroidManifest.xml')]
for needle in needles:
    hits = [str(p) for p in paths if needle in p.read_text(errors='ignore')]
    print(f'{needle}={hits}')
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

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `MoneyTransferPauseGateBuilderTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-money-transfer-pause-gate.md
git commit -m "feat: add money transfer pause gate"
```

Do **not** push.

## Final response schema

End with either:

### DONE

- What changed.
- Files updated count.
- Verification commands and results.
- Focused JUnit counts for the new test and privacy regressions.
- APK path and byte size.
- Local commit SHA and message.
- Confirm no push performed.

or:

### BLOCKED

- Exact blocker.
- What changed, if anything.
- Commands attempted and failing output.
- Current git status.
- Safest next step.
