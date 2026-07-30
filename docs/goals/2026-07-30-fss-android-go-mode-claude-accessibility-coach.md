# FSS Android /go continuation — Senior Accessibility Coach (큰글씨 · 읽어주기 · 고대비 · 간단 모드)

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Claude Code (primary) or Codex `gpt-5.6-sol` ultra (fallback if Claude model errors)
Model target: `sonnet` for Claude, `gpt-5.6-sol` for Codex. Do NOT retry model/limit errors — pick the best available and proceed.
Mode: `/go` continuous execution
Git policy: **local commit only, no push** (push stays blocked until the user explicitly asks)

## Verified starting state (Hermes pre-flight, 2026-07-30 round 1 close)

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew clean test assembleDebug lintDebug
# => BUILD SUCCESSFUL
# => testDebugUnitTest total: tests=719 failures=0 errors=0
# => ScreenshotPrivacyGuardCoachTest: 19/19
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1,656,497 bytes
git diff --check
# => clean
```

Git state:

- HEAD: `5f8f3a9 feat: add screenshot privacy guard coach`
- `main...origin/main: [ahead 50]`
- Remote: `https://github.com/goddragon957-max/family-ai-scam-shield.git`
- Untracked (intentional, this session's own goal doc): `docs/goals/2026-07-30-fss-android-go-mode-claude-commit-id-document-coach.md`

Permission/manifest invariants (must remain true throughout):

- No `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`,
  `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- No `<uses-permission>` tag at all.
- `android:allowBackup="false"` stays in the manifest.
- Source must not introduce `AlarmManager`, `NotificationManager`, `NotificationCompat`,
  `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`,
  `ContactsContract`, `SmsManager`, `Intent.EXTRA_STREAM`, `ClipData`,
  `FLAG_GRANT_READ_URI_PERMISSION`, `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`,
  `MediaStore`, `ContentObserver`, `FLAG_SECURE`, or any new `<receiver>`/`<service>`/`<provider>`.

## Rationale

Grep across `app/src/main` for `fontScale`, `TtsSpan`, `TextToSpeech`, `accessibility`, `Accessibility`, `큰글씨`, `senior`, `고대비`, `read.aloud` returned **zero matches**. The repo has dozens of safety/pause coaches but no usability coach for the actual end-user: a 60–80 year-old family member who is the primary target of the very scams this app protects against. They cannot read body-text in the existing app, cannot hear what the safety coaches say, and have no high-contrast option.

This slice gives the app an **Accessibility & Senior-Readability Coach** that respects the privacy-first, no-permission, no-network posture: it only coaches the family member on how to use the phone's built-in Android system settings (font size, display size, bold text, high-contrast text, screen magnification, Select-to-Speak, TalkBack) plus an **in-app simple-mode** static card set that the existing app can render immediately without new permissions.

## Task — Accessibility & Senior-Readability Coach slice

Add a deterministic, local-only Accessibility Coach:

1. **New deterministic model/builder** (`AccessibilityCoach.kt`):
   - `AccessibilityCoachBuilder.build(consent: Boolean, readiness: FamilyReadiness, incidentCount: Int = 0): AccessibilityCoach`
   - Pure/stateless, no Android imports, no Compose, no TTS engine, no actual font mutation.
   - Uses only:
     - `consent` boolean
     - `readiness.level` and counts/labels — **not** safe word / callback / contact values
     - `incidentCount.coerceAtLeast(0)` as a count category only
   - Outputs a fixed card set covering:
     - "시스템 글자 크기 키우기" — Android Settings → Display → Font size (안드로이드 시스템 설정에서 글자 크기 조절)
     - "화면 크기도 키우기" — Display size (디스플레이 크기)
     - "굵은 글자 / 고대비 텍스트 켜기" — Bold text / High contrast text
     - "확대 / 돋보기" — Magnification / Magnifier (접근성 → 화면 확대, triple-tap-to-zoom)
     - "글자 읽어주기" — Select-to-Speak / TalkBack
     - "흔들어서 읽기" — Read-aloud shortcuts
     - "가족 안전단어 / 콜백 스크립트" — 큰 글씨로 한 번 더 보여주는 곳 안내 (only by id/route label, never literal value)
     - "긴급 신고 번호" — 112 / 1394 / 182 (경찰 신고·피해 신고) — display only, no dialer mutation
     - "지금 분석 결과" — 분석 결과 화면 큰 글씨 안내
     - "단순 모드" — simple mode toggle hint (in-app, static, persisted via existing SharedPreferences key only, never overwrites user data)
   - Each card includes:
     - title, why-it-matters (Korean), step-by-step manual instructions for system settings
     - "이 앱에서 직접 해주는 것" (in-app) vs "안드로이드 시스템 설정에서 해야 하는 것" (out-of-app) — explicit split
     - primary existing route action
     - optional secondary existing route actions
   - Recommendation changes deterministically for:
     - no consent → onboarding link first
     - low/in-progress readiness → readiness roadmap
     - incident present → analysis result + evidence kit
     - high readiness + consent → simple-mode hint + privacy center

2. **Simple Mode in-app static** (no new permission, no system service mutation):
   - A new `SimpleModePreference` that stores a single boolean in existing `SharedPreferences` (key: `pref_simple_mode`).
   - When ON, the existing `MainActivity` shows a `simpleModeHome()` rendering:
     - Three giant buttons only:
       1) "수상한 문자 / 통화 분석" → existing checker
       2) "지금 가족에게 전화" → existing safety plan + ACTION_DIAL route to a placeholder family contact (use `Intent.ACTION_DIAL`, never `ACTION_CALL`)
       3) "긴급 신고" → existing official help directory (112/1394/182)
   - Simple Mode toggle lives in Privacy Center (and a small top-right link on Home in non-simple mode).
   - Simple Mode toggle changes only the home layout and font size hints in static copy. It does **not** mutate any other coach or read any external state.
   - All text in simple mode is 22sp+ with at least 1.5 line height and high contrast. No icon-only buttons.

3. **Native UI integration in `MainActivity.kt`**:
   - New `Screen` enum value `accessibilityCoach` and `renderAccessibilityCoach()`.
   - Add route buttons from Home, Privacy Center, Safety Flow Router, Family Calm Debrief.
   - Add "단순 모드 켜기/끄기" toggle button in Privacy Center.
   - Add "지금 글자 크게 보기" prompt on the accessibility coach screen itself, **as a static reminder card** that directs the user to the Android system settings. Do **not** mutate `Configuration.fontScale` programmatically (that's an invasive behavior change the user has not opted into). Just teach the steps.

4. **Privacy Center coverage**:
   - Add the new coach to the local feature inventory.
   - Add a note that simple mode stores one boolean in local prefs, and the coach does not change the phone's system font size / display size / contrast / TalkBack settings — only explains them.
   - Confirm there are zero new permissions.

5. **Tests** (`AccessibilityCoachBuilderTest.kt`):
   - All cards have complete fields, stable order, unique routes, all action IDs map to existing flows.
   - No-analysis/default coach is conservative.
   - Consent / readiness / incident-driven recommendations are deterministic and only escalate, never auto-apply.
   - Share summary contains only generic labels and no safe-word / callback / contact / phone / account / OTP / URL / resident-ID / raw input.
   - Builder is deterministic and clamps negative incident counts.
   - Copy says: manual, local, stateless, no storage of choices beyond the existing pref flag, no contacts read, no network, no permissions, no scheduler, no system settings mutation, no font auto-scale, no TalkBack auto-toggle, no auto-dial, no auto-share.
   - Copy avoids "100%", "safe", "blocked", "removed", "recovered", "guaranteed", "자동으로 글자 크게" (we explicitly do NOT auto-scale).
   - Privacy Posture Regression: add source scan for new APIs explicitly listed above as forbidden.

6. **Source-reading tests** proving:
   - `MainActivity` simple-mode path uses only the existing `ACTION_DIAL` (never `ACTION_CALL`), no new permission tags.
   - `AccessibilityCoach.kt` has zero `android.*` imports and zero `Configuration`/`TextToSpeech`/`TtsSpan`/`AccessibilityManager` usage.
   - `SimpleModePreference` writes only to existing SharedPreferences with a single boolean key.

7. **Docs**:
   - Update `README.md`: add a "Senior Accessibility Coach" section explaining the in-app vs system-setting split, the simple-mode toggle, and the privacy posture (no auto-mutation of system settings).
   - Add to Privacy Center copy.

## Hard rules

- Real Android Kotlin project only — no web/mock rewrite, no Compose migration.
- Package stays `com.mongmong.familyshield`. Application id unchanged.
- Local-only MVP. No server, no internet, no monitoring, no analytics.
- **No new Android permissions. No new permission tags. No `<uses-permission>` ever.**
- No mutation of `Configuration`, `AccessibilityManager`, `WindowManager`, `TextToSpeech`, or any system service from this slice.
- `android:allowBackup="false"` stays.
- Korean copy is calm, practical, non-alarmist. Never claim "100%", "safe", "blocked", "removed", "recovered", "legal guarantee", "guaranteed refund", or "automatically resized" — the system settings change is always the user's manual action.
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
