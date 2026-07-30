# FSS Android /go continuation — Screenshot Privacy Guard coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Claude Code (or Codex, either can execute this)
Model target: `fable` (or any available Claude model, fallback to `sonnet`/`opus` if `fable` is
session-limited; do NOT retry on limit — pick the best available and proceed)
Mode: `/go` continuous execution
Git policy: **local commit only, no push** (push stays blocked until the user explicitly asks)

## Verified starting state (Claude pre-flight, 2026-07-30)

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 1m 11s
# => testDebugUnitTest total: tests=699 failures=0 errors=0
# => IdDocumentSelfieCapturePauseCoachTest: 18/18 passing
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1,640,638 bytes
git diff --check
# => clean
```

Git state:

- HEAD: `3bf71f0 feat: add id document selfie capture pause coach`
- `main...origin/main: [ahead 49]`
- Remote: `https://github.com/goddragon957-max/family-ai-scam-shield.git`
- Working tree: clean except this goal file itself.

## Why this slice

The repo already has ~20 near-identical "pause coach" screens covering money, links, SIM ports,
doorstep handoffs, credentials, and now ID document/selfie capture. Four candidates were
evaluated for the next slice:

- **First-time onboarding overlay** — already substantially covered by the existing
  `Screen.ONBOARDING` consent screen plus `Screen.FLOW_ROUTER` ("지금 뭐부터 할지 고르기",
  reachable from Home). Not a real gap.
- **Post-report 24h family check-in** — already covered by `FollowUpCheckInPlanner.kt`
  ("며칠 뒤 다시 확인할 것"), which has checkpoints from tonight through this month. Not a real
  gap.
- **Family protector handoff note** — partially covered by `TrustedHelperHandoffCard.kt` and
  `TrustedHelperReviewMode.kt` (asking specific helper roles for specific help), though a
  boundaries-explainer note is not identical. Partial overlap, weaker candidate.
- **Screenshot Privacy Guard** ("증거 화면캡처 가드") — **zero existing coverage.** No file in
  `app/src/main/java/com/mongmong/familyshield/` mentions screenshots, `FLAG_SECURE`, or any
  screenshot-adjacent concept. This is the clearest gap and the one a real family would actually
  hit: once someone has a redacted incident record, risk-signal explainer, or evidence-kit
  checklist open on screen, they will often screenshot it to text a family member — and nothing
  in the app currently reminds them that screenshots land in the normal camera roll, unprotected,
  and can carry more than they intended (e.g. surrounding notification content, other visible
  fields) into a shared photo/album that syncs to cloud backups outside this app's control.

## Task — Implement `ScreenshotPrivacyGuardCoach`

Follow the **exact same pattern** as the most recently landed coach,
`IdDocumentSelfieCapturePauseCoach` (`app/src/main/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoach.kt`
+ `app/src/test/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoachTest.kt`), which is
the cleanest current template: a deterministic builder object, a fixed ordered set of cards, a
`recommendedCardIds`/`recommendedActions` scoring function driven only by
`ScamAnalysis?.riskLevel` / `matchedSignals`, `FamilyReadiness.level`, and incident count, plus an
exhaustive `privacySafetyNotes()` boundary list and a `familyShareSummary` for manual
`ACTION_SEND` sharing. Read that file and its test file fully before writing anything.

### Scope for the new coach

Screen name suggestion: `SCREENSHOT_PRIVACY_GUARD_COACH` /
`ScreenshotPrivacyGuardCoachBuilder` / `ScreenshotPrivacyGuardCoach` / `ScreenshotPrivacyGuardCard`
(rename freely if a clearer name emerges, but keep it consistent across builder, screen, tests,
Privacy Center, and README).

This is a **static informational reminder**, not a detector:

- **Do not** use `WindowManager.LayoutParams.FLAG_SECURE` — the app explicitly avoids forcing
  screen-capture blocking globally (would break legitimate sharing/screenshots elsewhere and was
  ruled out for this app already).
- **Do not** attempt to detect actual screenshot events. Real detection would require either a
  `MediaStore` `ContentObserver` (needs storage read access) or an accessibility service — both
  forbidden by this app's permission invariants (see Hard rules below). There is no permitted way
  to know a screenshot happened, and this coach must not pretend otherwise.
- Instead, the coach is a manually-opened reminder screen (like every other coach here) that a
  user reads *before or after* they take a screenshot of an incident/evidence screen in this app,
  explaining in plain Korean copy: screenshots are not protected by this app, they land in the
  normal gallery/camera roll, they may sync to cloud photo backups the family does not control,
  they can capture more of the screen than intended, and what to do about it manually (e.g. delete
  the screenshot after sharing it deliberately, avoid screenshotting screens with unredacted
  values, prefer this app's own `ACTION_SEND` share buttons which only send the fixed
  `familyShareSummary`/prepared text instead of a raw screenshot).

Standards to match (same as every prior slice):

- Deterministic local builder/model, no state, no persistence, no new fields on `SafetyPlan`,
  `IncidentStore`, etc.
- One screen (`Screen.SCREENSHOT_PRIVACY_GUARD_COACH` or similar) reachable from `Screen.HOME` and
  from **at least two** other existing screens that plausibly show sensitive incident content on
  screen — good candidates: `Screen.RESULT` (analysis result), `Screen.RISK_SIGNAL_EXPLAINERS`,
  `Screen.EVIDENCE_HANDOFF_PRIVACY_COACH`, `Screen.EXPOSURE_IMPACT_TRIAGE`,
  `Screen.TIMELINE_BRIEFING`, `Screen.LOG`, `Screen.EVIDENCE` / evidence kit. Wire in from a
  reasonable subset (the ID document coach wired into nearly every screen — you do not need to go
  that far, but cover Home plus the screens that most plausibly hold something worth
  screenshotting).
- `ACTION_SEND` text-only manual share of a fixed `familyShareSummary` (same shape as other
  coaches' share summaries — no file/URI attachment, no auto-target).
- Privacy Center inventory (`PrivacyCenter.kt`) updated with a no-collection boundary statement and
  a no-mutation statement for this coach, following the exact pattern of
  `idDocumentSelfieBoundaryStatement` / `idDocumentSelfieMutationStatement` added in the prior
  commit (`git show 3bf71f0 -- app/src/main/java/com/mongmong/familyshield/PrivacyCenter.kt`).
- README updated: bump the numbered feature list, add a dedicated section describing the coach
  (mirror the "ID Document & Selfie Capture Pause Coach" section format).
- Regression tests: extend `PrivacyPostureRegressionTest.kt` and `PrivacyCenterBuilderTest.kt`
  covering the same invariants as the ID document coach's additions (no permission strings, no
  `FLAG_SECURE`, no `ContentObserver`, no `MediaStore`, no accessibility APIs, no new persisted
  state, share summary contains no raw incident values), plus a full new
  `ScreenshotPrivacyGuardCoachTest.kt` covering card ordering, recommendation determinism across
  signal/readiness/incident-count combinations, and the "no android.* import" / no-permission
  checks the other coach tests already run.

### Steps

1. Read `IdDocumentSelfieCapturePauseCoach.kt`, `IdDocumentSelfieCapturePauseCoachTest.kt`, and the
   relevant `PrivacyCenter.kt` / `MainActivity.kt` diffs from commit `3bf71f0` as the template.
2. Design the card set (suggest 4–6 cards covering: before-you-screenshot reminder, after-you-
   screenshot cleanup reminder, cloud-backup/gallery-sync awareness, prefer-the-app's-own-share-
   button reminder, family-sharing-channel reminder) — keep the same calm, non-alarmist, no-
   guarantee tone as the rest of the app (see Hard rules below).
3. Implement the builder, wire the screen into `MainActivity.kt`, update `PrivacyCenter.kt`,
   update `README.md`, write/extend tests.
4. Verify:
   ```bash
   export JAVA_HOME="$PWD/.tools/jdk-17"
   export PATH="$JAVA_HOME/bin:$PATH"
   export ANDROID_HOME="$PWD/.tools/android-sdk"
   ./gradlew clean test assembleDebug lintDebug
   git diff --check
   ```
5. Confirm the APK builds and is a similar size to before (~1.6–1.7M).
6. Stage only the files you intentionally changed and commit as a single coherent commit, e.g.
   `feat: add screenshot privacy guard coach`.
7. Do **not** push.

## Permission/manifest invariants (all must remain true)

- No `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`,
  `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- No `<uses-permission>` tag at all.
- `android:allowBackup="false"` stays in the manifest.
- No `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`,
  `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`,
  `Intent.EXTRA_STREAM`, `ClipData`, `FLAG_GRANT_READ_URI_PERMISSION`, `ClipboardManager`,
  `getPrimaryClip`, `setPrimaryClip`.
- No `FLAG_SECURE`, no `ContentObserver`/`MediaStore` screenshot detection, no accessibility
  service, no `OnPreDrawListener`/window-callback screenshot-detection tricks.

## Hard rules

- Real Android Kotlin project only — no web/mock rewrite, no Compose migration.
- Package stays `com.mongmong.familyshield`. Application id unchanged.
- Local-only, no server, no internet, no monitoring.
- No new permissions. No scheduler/notification/clipboard APIs.
- Korean copy is calm, practical, non-alarmist. Never claim "100% safe", "blocked", "removed",
  "recovered", "legal guarantee", or "guaranteed refund". Also never claim the app "detects",
  "blocks", or "protects against" screenshots — it explicitly does not and must not imply it does.
- Never push. Never delete a remote branch. Never force-push.
- If verification fails, do not commit — write a truthful `BLOCKED` handoff instead.

## Toolchain

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
```

`scripts/bootstrap-android.sh` may be inspected but the local toolchain is already in place; do
not try to re-bootstrap unless verification is blocked by a missing tool.

## GitHub auth note

`gh` may be authenticated for `SoSyn2ne` and/or `goddragon957-max`. **Do not push.** Push is
blocked until the user explicitly asks for it.

## Final response schema

### DONE
- Files changed summary
- Exact verification commands + pass/fail summary
- APK path + byte size
- Local commit SHA(s) and message(s)
- Confirm: no push performed

### BLOCKED
- Exact blocker
- Commands attempted + failing output
- Current git status (clean/dirty, ahead count)
- Safest next step for the next worker to take
