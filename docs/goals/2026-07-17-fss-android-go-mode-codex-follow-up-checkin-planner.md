# Codex Goal — `/go` continuation: Follow-up Check-in Planner

Date: 2026-07-17 02:52 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: continue `/go` while Claude Sonnet is still session-limited until 04:10 Asia/Seoul. The previous Codex slice was independently verified by Hermes.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Trusted Helper Review Mode slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.2M, 1177757 bytes
git diff --check
# => clean
```

Additional Hermes checks:

```text
AndroidManifest permissions:
CALL_PHONE=False
READ_SMS=False
READ_CALL_LOG=False
READ_CONTACTS=False
RECORD_AUDIO=False
INTERNET=False
allowBackup=false=True
uses-permission-tag=False
TrustedHelperReviewModeBuilderTest: debug/release tests=13 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=15 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
ExposureImpactTriageBuilderTest: debug/release tests=14 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 24` of `origin/main`
- latest local commits:
  - `cf4ddfb feat: add trusted helper review mode`
  - `5860ce5 feat: add exposure impact triage`
  - `19d9a9b feat: add family readiness roadmap`
  - `f4297de feat: add family safety drill coach`
  - `419bb4f feat: add risk signal explainers`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has immediate stop, exposure triage, helper review, 24-hour recovery, account/device lockdown, evidence, official help, readiness, and drill flows. The next cohesive slice is **Follow-up Check-in Planner**: a stateless manual follow-up plan for the days after a suspicious contact or possible exposure. It must not schedule reminders or create notifications; it only shows what the family should manually check later.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialReportPrep.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentTimelineBriefing.kt`
- `app/src/main/java/com/mongmong/familyshield/ExposureImpactTriage.kt`
- `app/src/main/java/com/mongmong/familyshield/TrustedHelperReviewMode.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadinessRoadmap.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilySafetyDrillCoach.kt`
- `app/src/main/java/com/mongmong/familyshield/PrivacyCenter.kt`
- tests under `app/src/test/java/com/mongmong/familyshield/`

## Product identity / non-negotiables

This is a Korean Android app for a **family AI voice-phishing guardian**.

Keep:

- real Android Kotlin project, no web/mock rewrite
- package/application id: `com.mongmong.familyshield`
- simple native Android Views, no Compose migration
- local-only MVP: no server, no internet/network analysis
- no SMS/call/contact/microphone/internet permissions
- no notification, calendar, alarm, background-worker, or reminder permissions/APIs for this slice
- phone actions must use `ACTION_DIAL`, not `CALL_PHONE`
- no automatic surveillance; user-pasted/shared text only
- calm, practical Korean copy
- `android:allowBackup="false"`
- no build artifacts or local toolchain files in commits

## `/go` task: Follow-up Check-in Planner slice

Implement a focused local-only screen that helps a family manually check back after a suspicious contact or possible exposure. It should complement the 24-hour recovery plan by covering later checkpoints, without scheduling alarms/notifications or storing checkmarks.

1. **New deterministic follow-up model**
   - Add a pure Kotlin builder/model, e.g. `FollowUpCheckInPlannerBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - value-less `FamilyReadiness`;
     - optional saved incident count as an `Int` if easy to pass without exposing incidents.
   - Output should include:
     - Korean title and intro;
     - stable follow-up checkpoint cards in order;
     - each checkpoint has id, time label, category, purpose, manual check steps, what not to do, route actions, priority;
     - recommended checkpoint ids/actions based on current analysis signals/readiness/incident count;
     - privacy/safety notes;
     - safe family share summary.
   - Checkpoints should cover at least:
     - **오늘 밤 다시 안정시키기**: no further contact, review quick stop/helper/exposure triage;
     - **내일 오전 금융·계정 확인**: bank/card/account activity, official help/account lockdown;
     - **2~3일 기기·메신저 관찰**: remote app/session changes, evidence/account/helper;
     - **이번 주 가족 경고·콜백 점검**: family alert/callback/safe-call;
     - **이번 주 공식 접수·증거 정리**: official help/report prep/evidence/timeline;
     - **1~2주 명세·알림 재확인**: statements, login notices, unknown changes, privacy center;
     - **이번 달 가족 준비 보강**: readiness roadmap/drill/safety plan;
     - **불안 재발 시 다시 멈춤**: quick stop/exposure triage/trusted helper review.
   - Recommendation logic should be deterministic:
     - no analysis/no signals: recommend tonight stabilization, family callback/week readiness, uncertainty stop; avoid saying safe;
     - high risk or financial/credential/safe-account/gift/crypto/payment signals: recommend next-morning finance/account, official/evidence, 1~2 week statement review;
     - app/remote/screen-share signals: recommend 2~3 day device/session observation and account/evidence/helper;
     - link/smishing signals: recommend 2~3 day account/device observation, family alert, privacy review; no auto-scan claims;
     - family/deepfake/number-change signals: recommend family callback/week alert/safe-call;
     - secrecy/impersonation pressure: recommend tonight stop, trusted helper review, official help;
     - if incident count > 0: include timeline/evidence/report-prep follow-up;
     - low readiness: include month readiness roadmap and drill.
   - Safe family share summary must include only checkpoint labels/categories/action labels and generic manual check steps. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA near recovery/family/help items, e.g. “며칠 뒤 다시 확인할 것”.
   - Add current-result CTA if appropriate, e.g. “이 신호로 후속 점검 보기”.
   - Add a CTA from 24-hour recovery screen if straightforward, but do not break existing behavior.
   - Screen should display:
     - title/intro;
     - recommended checkpoints first;
     - all remaining checkpoints;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Exposure Impact Triage;
     - Trusted Helper Review Mode;
     - Checker / current Result;
     - Family Callback Card;
     - Safe Verification Call Scripts;
     - Family Alert Message Pack;
     - Family Readiness Roadmap;
     - Family Safety Drill Coach;
     - Recovery Plan 24h;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Official Report Prep;
     - Incident Timeline Briefing;
     - Safety Flow Router;
     - Privacy Center.
   - Add “가족에게 후속 점검 공유” using text-only `ACTION_SEND`; pass only the builder’s safe family share summary.
   - Do not store selected checkpoint, check-in answer, notes, reminder schedule, completion state, checklist state, score, read progress, share history, or follow-up history.
   - Do not create Android notifications, alarms, calendar events, WorkManager jobs, jobs/services, foreground services, background workers, or scheduled reminders.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape messages, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate full official-help/account-lockdown/evidence/recovery content.
   - The screen must phrase follow-ups as **manual family checks**, not automated monitoring.
   - It must never claim it watched accounts/devices/messages, scanned links, removed malware, froze accounts/cards, scheduled reminders, recovered money, or provided legal advice.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy and avoid shame/blame.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default recommendation includes tonight stabilization, family callback/readiness, uncertainty stop, and avoids safe verdict;
     - high-risk/financial/credential/safe-account/gift/crypto/payment signals recommend finance/account, official/evidence, statement review actions;
     - remote-control/app/screen-share recommends device/session observation and account/evidence/helper without malware-removal guarantee;
     - link/smishing recommends device/account observation/family alert/privacy review without auto-scan claims;
     - family/deepfake/number-change recommends family callback/week alert/safe-call;
     - secrecy/impersonation includes stop/helper/official help;
     - incident count > 0 includes timeline/evidence/report-prep follow-up, but no incident data leaks;
     - low readiness includes readiness roadmap and drill; ready copy differs calmly;
     - every checkpoint has time label, category, purpose, manual steps, do-not-do notes, primary action, stable id/order;
     - copy states manual-only and never uses notifications/alarms/calendar/reminder scheduling/background work wording as a promise;
     - share summary includes checkpoint/action labels and generic manual steps but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/local/no storage/no contact reading/no auto-send/no monitoring/no network/no incident mutation/no scheduler;
     - copy does not imply guaranteed safety/proof, automatic monitoring, notification scheduling, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or access to secrets.
   - Add/adjust privacy regression tests to ensure the follow-up screen is stateless and share helper sends only builder summary.
   - Add regression assertions that `MainActivity.kt` and this model do **not** use `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `ForegroundService`, `BroadcastReceiver`, or persistent reminder/checkmark state.

5. **Docs**
   - Update `README.md` to document Follow-up Check-in Planner as local-only, stateless, manual follow-up guidance; it adds no permissions, creates no notifications/alarms/calendar entries/workers, reads no contacts/messages, auto-sends nothing, stores no follow-up selections/history, and mutates no incidents.

## Verification gates

Run fresh gates before claiming completion:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
ls -lh app/build/outputs/apk/debug/app-debug.apk
git diff --check
```

## Git policy

- Create one local commit only if verification passes.
- Do not push.
- Do not commit `.hermes/`, `.gradle/`, `.tools/`, `app/build/`, `local.properties`, or other machine-local files.
- It is okay to commit this goal file with the implementation if useful, but do not include transient logs.

Suggested commit message:

```text
feat: add follow-up check-in planner
```

## Final response schema

End with exactly one status heading.

### DONE
Include:
- files changed summary
- exact verification commands and pass summary
- APK path and size
- local commit SHA

### BLOCKED
Include:
- exact blocker
- commands attempted
- files changed / whether git tree is dirty
- next command Hermes should run

Completion condition: the repo has a verified local Android debug APK and a local commit for the Follow-up Check-in Planner slice, or a truthful blocker handoff exists with exact evidence.
