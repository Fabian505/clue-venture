# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build

The `gradle-wrapper.jar` is not committed to the repo. Use the locally cached Gradle distribution directly:

```powershell
# Compile Android debug (fastest check — no APK)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
C:\Users\hamac\.gradle\wrapper\dists\gradle-9.3.1-bin\23ovyewtku6u96viwx3xl3oks\gradle-9.3.1\bin\gradle.bat :composeApp:compileDebugKotlin

# Build APK
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
C:\Users\hamac\.gradle\wrapper\dists\gradle-9.3.1-bin\23ovyewtku6u96viwx3xl3oks\gradle-9.3.1\bin\gradle.bat :composeApp:assembleDebug

# Run tests
C:\Users\hamac\.gradle\wrapper\dists\gradle-9.3.1-bin\23ovyewtku6u96viwx3xl3oks\gradle-9.3.1\bin\gradle.bat :composeApp:testDebugUnitTest
```

iOS targets (`iosArm64`, `iosSimulatorArm64`) are declared in `build.gradle.kts` but no `iosMain` source directory exists — all `expect` functions lack iOS `actual` implementations. The Android build succeeds; iOS is not supported.

## Architecture

Kotlin Multiplatform + Compose Multiplatform project. All UI and business logic live in `commonMain`; platform-specific implementations are in `androidMain` using the `expect`/`actual` pattern.

```
composeApp/src/
  commonMain/   <- shared UI (Compose) + domain logic
  androidMain/  <- Supabase client, GPS, MapLibre
```

**Backend**: Supabase (PostgreSQL via postgrest-kt). The app uses **custom auth** (not Supabase Auth), so all RLS policies use `USING (true)`. The Supabase client is in `androidMain/SupabaseClient.kt`.

## Key Files

| File | Role |
|------|------|
| `commonMain/AdventureModels.kt` | All domain models + pure functions: `distanceTo`, `proximityStatus`, `calculateCheckpointPoints`, `calculateAdventurePoints`, `unlockedQuestionsForRouteDistance` |
| `commonMain/AdventureEntity.kt` | Supabase DTOs (`@Serializable`) + `toXxx()` conversion functions |
| `commonMain/AdventureRepository.kt` | `expect` declarations for all repository functions |
| `androidMain/AdventureRepository.kt` | Supabase `actual` implementations |
| `commonMain/App.kt` | Top-level navigation; `CreateAdventureScreen`, `EditAdventureScreen`, `ProfileAndLeaderboardTab` composables |
| `commonMain/AdventureGameScreen.kt` | Active adventure gameplay: checkpoint detection, hint panel, quiz trigger, points award |
| `commonMain/AdventureQuizUI.kt` | Quiz question/answer UI shown during gameplay |
| `commonMain/AdventureProximityUI.kt` | Hot/cold proximity feedback overlay |
| `commonMain/AuthUI.kt` | Login and registration screen |
| `androidMain/PlatformMap.android.kt` | MapLibre map rendering — no game logic here |
| `androidMain/LocationService.android.kt` | GPS location updates |

## SQL Schema Files (project root)

| File | Contents |
|------|----------|
| `supabase-adventures-schema.sql` | `adventures`, `adventure_locations`, `hints` tables |
| `supabase-adventure-quiz-schema.sql` | `quiz_questions`, `quiz_answers`, `adventure_attempts`, `user_answers`, `user_progress`, `users`, `user_profiles` tables + RLS policies |
| `supabase-adventure-feedback-schema.sql` | `adventure_feedback` table |
| `supabase-adventures-insert.sql` | Sample adventure data |
| `supabase-adventures-insert-quiz.sql` | Sample quiz data |
| `supabase-visibility-schema.sql` | Adds `is_public` column to `adventures` (migration) |
| `supabase-hint-image-schema.sql` | Adds `image_url` column to `hints` (migration) |
| `supabase-completed-at-trigger.sql` | Server-side trigger: sets `completed_at` when `is_completed` changes |
| `supabase-leaderboard-periods-schema.sql` | Leaderboard views/functions for all-time, weekly, monthly periods |

Run these in the Supabase SQL editor when setting up a new environment.

## Gamification System

### Points
- **Checkpoint**: `calculateCheckpointPoints(basePoints, elapsedSeconds, timeLimitSeconds?)` — linear decay to 10% floor at 2× the time limit
- **Time limit**: `effectiveTimeLimitSeconds(manual?, prevPoint, currPoint)` — uses manual override or derives from distance at 1.2 m/s walking pace
- **Adventure completion**: `calculateAdventurePoints(timeSpentSeconds, expectedSeconds, difficulty?)` — time-based bonus, max 1000/1500/2000 (easy/medium/hard), decays to 10% floor; `adventure.completionPoints` flat bonus added on top
- **Quiz**: Awarded live per correct answer via `updateUserPoints()` RPC — NOT included in completion bonus to avoid double-counting
- **Leaderboard periods**: `LeaderboardPeriod` enum — `ALL`, `WEEK`, `MONTH`

### Cancel behaviour
Cancelling an attempt marks `is_completed = false` and `points_earned = 0` in `adventure_attempts`. However, checkpoint and quiz points already awarded live via `updateUserPoints()` during the run are **not rolled back** — the player keeps them. Only the completion bonus is withheld.

### Hints (per AdventureLocation, max 3)
- Hint 0: free, shown on arrival at previous checkpoint
- Hint 1-2: cost points, user-triggered
- Each hint can optionally have an `imageUrl` (stored in Supabase Storage)

### Quiz Questions (per Adventure)
- Multiple-choice, shown during gameplay
- Available question count derived from `calculateAvailableQuestionCount(total, unlockedSlots, answered)`

## Conventions

- Business logic exclusively in `commonMain` — pure functions, no platform dependencies
- Never write points directly from UI composables — always go through `updateUserPoints()`
- No gamification logic in `PlatformMap.android.kt`
- `expect`/`actual` split: `commonMain/AdventureRepository.kt` declares interfaces; `androidMain/AdventureRepository.kt` implements with Supabase
- Quiz operations in `EditAdventureScreen` are saved **immediately** (not batched); hint changes are batched and saved on "Speichern"
- Adventure IDs are `String` in domain models but `Long` in the DB — conversion happens inside the `actual` repository
