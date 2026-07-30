# FSS Android /go continuation — Post-Incident 30/90 Day Family Review Calendar (no notifications)

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Claude Code
Model: `claude-opus-5` (pinned by user as of 2026-07-30; do NOT use any other model or `codex`)
Mode: `/go` continuous execution
Git policy: **local commit only, no push** (push stays blocked until the user explicitly asks)

## Verified starting state (Hermes pre-flight, 2026-07-30 20:48 KST)

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew clean test assembleDebug lintDebug
# => BUILD SUCCESSFUL
# => testDebugUnitTest total: tests=around 760 (PostAccessibility slice) failures=0 errors=0
# => SeniorAccessibilityCoachTest: ~22 tests
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => ~1.7M
git diff --check
# => clean
```

Git state:

- HEAD: `dce7c56 docs: add accessibility coach goal doc` (paired with `cd08ccf feat: add senior accessibility coach with simple mode`)
- `main...origin/main: [ahead 52]`
- Remote: `https://github.com/goddragon957-max/family-ai-scam-shield.git` — **do not push**
- Untracked (intentional, this session's own goal doc): `docs/goals/2026-07-30-fss-android-go-mode-claude-commit-id-document-coach.md`
- Tracked deletion: `docs/goals/2026-07-30-fss-android-go-mode-claude-screenshot-privacy-guard.md` (already implemented in `5f8f3a9`; harmless)

Permission/manifest invariants (must remain true throughout):

- No `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`,
  `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `USE_EXACT_ALARM`.
- No `<uses-permission>` tag at all.
- `android:allowBackup="false"` stays in the manifest.
- No `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`,
  `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`,
  `Intent.EXTRA_STREAM`, `ClipData`, `FLAG_GRANT_READ_URI_PERMISSION`,
  `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`, `MediaStore`, `ContentObserver`,
  `FLAG_SECURE`, or any new `<receiver>`/`<service>`/`<provider>`.
- This slice must not introduce any of the above, especially `CalendarContract` / `AlarmManager`
  (this is a calendar-shaped coach, not a real calendar/scheduler — the user manually
  walks through it; never auto-schedules anything).

## Rationale

Searched `app/src/main` for `SafetyPlanStore|Schedule|NotificationManager|TimePicker|ReminderCard|Family.*FollowUp|ScheduleCard|TimeCoach|WindowOfTime` — only existing related code is `FollowUpCheckInPlanner.kt` (a single one-shot checklist for the day after filing a report). The repo has no longer-term cadence: 1 week, 1 month, 3 months, 6 months, 12 months after a scam attempt, families should re-check whether the safety plan still matches reality, whether new phone numbers / banks / health situations changed, and whether a family member has been re-targeted.

This slice adds a **Post-Incident Family Review Calendar** that is:
- deterministic and local-only,
- **NOT** a real calendar (no `CalendarContract`, no notifications, no alarms),
- a static coach that lists day-X anchor reviews and tells the user to mark them in a real
  paper or phone calendar themselves,
- wired into Privacy Center, Family Calm Debrief, and FollowUpCheckInPlanner.

The Coach is the only place in the app where "30 days from now" appears, and it is always
phrased as "the user does this manually".

## Task — Post-Incident Family Review Calendar slice

Add a deterministic, local-only calendar coach:

1. **New deterministic model/builder** (`PostIncidentReviewCalendar.kt`):
   - `PostIncidentReviewCalendarBuilder.build(consent: Boolean, readiness: FamilyReadiness, incidentCount: Int = 0, lastIncidentDaysAgo: Int? = null): PostIncidentReviewCalendar`
   - Pure/stateless, no Android imports.
   - Uses only: `consent` boolean; `readiness.level` and counts/labels (not literal safe word
     / callback / contact values); `incidentCount.coerceAtLeast(0)`; optional
     `lastIncidentDaysAgo.coerceAtLeast(0)`.
   - Anchors (fixed list, each with displayOrder 1..N):
     - **Day 0** — "방금 끝난 일 정리" (links to existing FollowUpCheckInPlanner; already exists)
     - **Day 3** — "감정 정리 + 즉시 점검"
     - **Day 7** — "1주일 점검: 단어/계좌/번호 바뀐 것"
     - **Day 14** — "2주 점검: 재연락/새 시도 여부"
     - **Day 30** — "1개월 점검: 가족 안전 계획 갱신"
     - **Day 90** — "3개월 점검: 새 가족/장기 부양 변화"
     - **Day 180** — "6개월 점검: 신분/계좌/보안 종합"
     - **Day 365** — "1년 점검: 가족 안전 계획 전면 재검토"
   - Each anchor card includes:
     - title, why-it-matters
     - what to manually recheck (Korean, deterministic, no literal safe word)
     - "이 앱에서 보기" — primary existing route action
     - "가족과 상의할 질문 3개" — only labels, never literal values
     - "이 일정 직접 표시" — instruction to write it in the user's real calendar
   - Recommendation changes deterministically for:
     - no consent → onboarding link first
     - no incident history → "아직 사건이 기록되지 않았어요" + skip entire calendar
     - `lastIncidentDaysAgo` near a known anchor → emphasize that anchor and dim far ones
     - low readiness → emphasize Day 0/3/7
     - high readiness → spread full calendar

2. **Manual-only cadence** (NO scheduler, NO notifications):
   - The Coach is rendered as a static list. There is no `AlarmManager`, no `WorkManager`,
     no `JobScheduler`, no `NotificationManager`, no `CalendarContract` API.
   - Add a Privacy-Center note explicitly saying: "이 화면은 자동으로 알림을 보내지 않습니다.
     가족과 직접 정한 일정에 가족 안전 계획 점검을 표시해 두세요."

3. **Native UI integration in `MainActivity.kt`**:
   - New `Screen` enum value `postIncidentReviewCalendar` and `renderPostIncidentReviewCalendar()`.
   - Add route buttons from:
     - Home
     - Family Calm Debrief
     - FollowUpCheckInPlanner
     - Privacy Center
   - Each anchor's "이 앱에서 보기" button delegates to existing screens (e.g., SafetyPlan,
     FamilyReadiness, PrivacyCenter, ActiveContactExitScripts, FamilyCallbackCard).

4. **Privacy Center coverage**:
   - Add the new coach to the local feature inventory.
   - Add a note that the coach stores nothing for itself; the user manually marks dates in
     their own real calendar app.
   - Confirm there are zero new permissions and zero new scheduler/notification APIs.

5. **Tests** (`PostIncidentReviewCalendarBuilderTest.kt`):
   - All anchor cards have complete fields, stable order, unique routes.
   - No-consent recommends onboarding first.
   - No-incident-history skips the entire calendar and shows a "first-time" message.
   - `lastIncidentDaysAgo` near a known anchor emphasizes that anchor and dims far ones.
   - `lastIncidentDaysAgo` clamps negative or unknown.
   - Low readiness emphasizes Day 0/3/7; high readiness spreads.
   - Share summary contains only generic labels and no safe word / callback / contact / phone /
     account / OTP / URL / resident ID / raw input.
   - Builder is deterministic for the same input.
   - Copy says: manual, local, stateless, no storage of dates, no notifications, no alarms,
     no calendar app write, no contact sync, no auto-reminder.
   - Copy avoids "100%", "safe", "blocked", "removed", "recovered", "guaranteed",
     "automatically scheduled", "자동 알림", "캘린더에 자동 저장", "스케줄러".

6. **Source-reading tests** proving:
   - `MainActivity` post-incident path uses only existing ACTION_DIAL/route helpers; no
     new permission tags.
   - `PostIncidentReviewCalendar.kt` has zero `android.*` imports, zero
     `CalendarContract`/`AlarmManager`/`WorkManager`/`JobScheduler`/`NotificationManager`
     usage.
   - No `BroadcastReceiver`/`Service`/`ContentProvider` is registered.
   - AndroidManifest still has no `<uses-permission>` tag and `allowBackup="false"`.

7. **Docs**:
   - Update `README.md` with a "Post-Incident Family Review Calendar" section explaining the
     no-automation posture and that the user is expected to write dates in their real
     calendar.
   - Add to Privacy Center copy.
   - Update any tests that count global-occurrence of certain enum ids (preserve previous
     count when adding new routes).

## Hard rules

- Real Android Kotlin project only — no web/mock rewrite, no Compose migration.
- Package stays `com.mongmong.familyshield`. Application id unchanged.
- Local-only MVP. No server, no internet, no monitoring, no analytics.
- **No new Android permissions. No new permission tags. No `<uses-permission>` ever.**
- **No scheduler, notification, alarm, calendar, clipboard, or background APIs.**
- This slice is explicitly **NOT** a real calendar integration. The user does it manually.
- `android:allowBackup="false"` stays.
- Korean copy is calm, practical, non-alarmist. Never claim "100%", "safe", "blocked",
  "removed", "recovered", "legal guarantee", "guaranteed refund",
  "automatically scheduled", or "자동 알림".
- Never push. Never delete a remote branch. Never force-push.

## Toolchain

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
```

## Final response schema

End with exactly one heading.

### DONE
- Files changed summary
- Exact verification commands + pass/fail summary
- APK path + byte size
- Local commit SHA and message
- Confirm: no push performed
- One-paragraph rationale for **the next** cohesive slice (or `STOP` if the repo is at natural saturation)

### BLOCKED
- Exact blocker
- Commands attempted + failing output
- Current git status
- Safest next step
