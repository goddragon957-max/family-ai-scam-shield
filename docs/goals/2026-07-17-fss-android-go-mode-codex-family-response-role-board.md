# Codex Goal — `/go` continuation: Family Response Role Board

Date: 2026-07-17 03:24 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: continue `/go` while Claude Sonnet is still session-limited until 04:10 Asia/Seoul. The previous Codex slice was independently verified by Hermes.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Follow-up Check-in Planner slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.2M, 1194841 bytes
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
POST_NOTIFICATIONS=False
SCHEDULE_EXACT_ALARM=False
RECEIVE_BOOT_COMPLETED=False
allowBackup=false=True
uses-permission-tag=False
Scheduler/API scan:
AlarmManager=[]
NotificationManager=[]
NotificationCompat=[]
WorkManager=[]
JobScheduler=[]
CalendarContract=[]
ForegroundService=[]
BroadcastReceiver=[]
FollowUpCheckInPlannerBuilderTest: debug/release tests=14 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=16 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
TrustedHelperReviewModeBuilderTest: debug/release tests=13 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 25` of `origin/main`
- latest local commits:
  - `a621b0e feat: add follow-up check-in planner`
  - `cf4ddfb feat: add trusted helper review mode`
  - `5860ce5 feat: add exposure impact triage`
  - `19d9a9b feat: add family readiness roadmap`
  - `f4297de feat: add family safety drill coach`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now covers immediate stop, exposure triage, helper review, follow-up checks, recovery, account/device lockdown, evidence, official help, family alerts, readiness, and drills. The next cohesive slice is **Family Response Role Board**: a stateless family coordination board that turns the existing response flows into generic roles, so a family can divide work without entering names, contacts, passwords, safe words, or notes.

This is intentionally different from `TrustedHelperHandoffCard` and `TrustedHelperReviewMode`: those guide which helper to ask and how a helper should review. This slice is a **team role board** for coordinating who does which safe task, with no stored assignments.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/TrustedHelperHandoffCard.kt`
- `app/src/main/java/com/mongmong/familyshield/TrustedHelperReviewMode.kt`
- `app/src/main/java/com/mongmong/familyshield/FollowUpCheckInPlanner.kt`
- `app/src/main/java/com/mongmong/familyshield/ExposureImpactTriage.kt`
- `app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialReportPrep.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyAlertMessagePack.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/SafeVerificationCallScripts.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadinessRoadmap.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilySafetyDrillCoach.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentTimelineBriefing.kt`
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
- no notification, calendar, alarm, background-worker, or reminder permissions/APIs
- phone actions must use `ACTION_DIAL`, not `CALL_PHONE`
- no automatic surveillance; user-pasted/shared text only
- calm, practical Korean copy
- `android:allowBackup="false"`
- no build artifacts or local toolchain files in commits

## `/go` task: Family Response Role Board slice

Implement a focused local-only screen that lets a family coordinate generic response roles. It should never ask for family member names, phone numbers, passwords, OTPs, safe word answers, exact account numbers, or raw scam text. It should not store assignments or checkmarks.

1. **New deterministic role-board model**
   - Add a pure Kotlin builder/model, e.g. `FamilyResponseRoleBoardBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - value-less `FamilyReadiness`;
     - optional saved incident count as an `Int` if easy to pass without exposing incidents.
   - Output should include:
     - Korean title and intro;
     - stable role cards in order;
     - each role card has id, role label, mission, safe handoff sentence, category, when to use, do-not-do boundaries, route actions, and priority;
     - recommended role ids/actions based on current analysis signals/readiness/incident count;
     - privacy/safety notes;
     - safe family share summary.
   - Generic role cards should cover at least:
     - **멈춤 진행자**: stop pressure, calm lead, quick stop/helper review;
     - **가족 본인확인 담당**: callback and safe call scripts, never reveal safe word answer/contact values;
     - **노출 분류 담당**: exposure triage only by action category, not raw notes;
     - **계정·기기 보호 담당**: account lockdown/recovery, no password/OTP collection;
     - **증거 보존 담당**: evidence kit/timeline, no raw-link re-open, no broad forwarding;
     - **공식 도움 연결 담당**: official help/report prep, no legal/financial guarantee;
     - **가족 경고 전달 담당**: family alert messages, no raw scam text/secrets;
     - **후속 점검 담당**: follow-up planner/readiness/drill/privacy review, no reminders/monitoring.
   - Recommendation logic should be deterministic:
     - no analysis/no signals: recommend pause lead, callback verifier, exposure sorter, follow-up coordinator; avoid saying safe;
     - high risk or money/credential/payment/safe-account/gift/crypto signals: recommend account protector, official help connector, evidence keeper, follow-up coordinator;
     - app/remote/screen-share signals: recommend account/device protector, evidence keeper, trusted helper review;
     - link/smishing signals: recommend exposure sorter, evidence keeper, family alert relay, privacy/follow-up;
     - family/deepfake/number-change signals: recommend callback verifier and family alert relay;
     - secrecy/impersonation pressure: recommend pause lead, exposure sorter, official help connector;
     - if incident count > 0: include evidence keeper and follow-up coordinator with timeline action;
     - low readiness: include callback verifier and readiness/drill actions.
   - Safe family share summary must include only role labels/categories/action labels and generic handoff sentences. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns, and family-member names from inputs.

2. **Visible UI integration**
   - Add Home CTA near family/helper/follow-up items, e.g. “가족 역할 나눠 대응하기”.
   - Add current-result CTA if appropriate, e.g. “이 신호로 가족 역할 나누기”.
   - Add a CTA from Trusted Helper Review or Follow-up Planner if straightforward, but do not break existing behavior.
   - Screen should display:
     - title/intro;
     - recommended roles first;
     - all remaining roles;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Exposure Impact Triage;
     - Trusted Helper Review Mode;
     - Trusted Helper Handoff Card;
     - Checker / current Result;
     - Family Callback Card;
     - Safe Verification Call Scripts;
     - Family Alert Message Pack;
     - Family Readiness Roadmap;
     - Family Safety Drill Coach;
     - Follow-up Check-in Planner;
     - Recovery Plan 24h;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Official Report Prep;
     - Incident Timeline Briefing;
     - Safety Flow Router;
     - Privacy Center.
   - Add “가족에게 역할 보드 공유” using text-only `ACTION_SEND`; pass only the builder’s safe family share summary.
   - Do not store selected role, role assignment, family member name, check-in answer, notes, completion state, checklist state, score, read progress, share history, or role-board history.
   - Do not create Android notifications, alarms, calendar events, WorkManager jobs, jobs/services, foreground services, background workers, or scheduled reminders.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape messages, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate full official-help/account-lockdown/evidence/recovery content.
   - The role board must phrase coordination as **manual generic roles**, not named assignments or automated task management.
   - It must never claim the family has verified truth, watched accounts/devices/messages, scanned links, removed malware, froze accounts/cards, scheduled reminders, recovered money, or received legal advice.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy and avoid shame/blame.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default recommendation includes pause lead, callback verifier, exposure sorter, follow-up coordinator, and avoids safe verdict;
     - high-risk/financial/credential/safe-account/gift/crypto/payment signals recommend account protector, official connector, evidence keeper, follow-up coordinator;
     - remote-control/app/screen-share recommends account/device protector, evidence keeper, trusted helper review without malware-removal guarantee;
     - link/smishing recommends exposure sorter/evidence/family alert/privacy follow-up without auto-scan claims;
     - family/deepfake/number-change recommends callback verifier and family alert relay;
     - secrecy/impersonation includes pause/exposure sorter/official connector;
     - incident count > 0 includes evidence keeper/timeline/follow-up coordinator without incident data leaks;
     - low readiness includes callback verifier/readiness roadmap/drill; ready copy differs calmly;
     - every role card has role label, mission, safe handoff sentence, category, when-to-use, do-not-do boundaries, primary action, stable id/order;
     - copy states generic manual roles and never asks for names, phone numbers, passwords, OTP values, PINs, safe-word answers, exact account numbers, or raw links;
     - share summary includes role/action labels and generic handoff sentences but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/local/no storage/no contact reading/no auto-send/no monitoring/no network/no incident mutation/no scheduler;
     - copy does not imply guaranteed safety/proof, automatic monitoring, notification scheduling, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, named assignment storage, or access to secrets.
   - Add/adjust privacy regression tests to ensure the role board screen is stateless and share helper sends only builder summary.
   - Add regression assertions that `MainActivity.kt` and this model do **not** use `ContactsContract`, `SmsManager`, `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `ForegroundService`, `BroadcastReceiver`, or persistent role/assignment/checkmark state.

5. **Docs**
   - Update `README.md` to document Family Response Role Board as local-only, stateless, generic role coordination; it adds no permissions, creates no notifications/alarms/calendar entries/workers, reads no contacts/messages, auto-sends nothing, stores no names/assignments/selections/history, and mutates no incidents.

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
feat: add family response role board
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Family Response Role Board slice, or a truthful blocker handoff exists with exact evidence.
