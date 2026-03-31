# ClueVenture

A GPS-based scavenger hunt app built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, targeting Android and iOS.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Compose Multiplatform |
| Shared logic | Kotlin Multiplatform |
| Backend | Supabase (PostgREST, Auth, Realtime) |
| HTTP client | Ktor |
| Location (Android) | Google Play Services – FusedLocationProviderClient |
| Location (iOS) | CoreLocation – CLLocationManager |

## Project Structure

```
ClueVenture/
├── composeApp/                      # KMP module
│   └── src/
│       ├── commonMain/              # Shared Kotlin code
│       │   └── kotlin/com/clueventure/
│       │       ├── App.kt
│       │       ├── data/model/      # Data models
│       │       ├── data/repository/ # Supabase repository
│       │       ├── network/         # SupabaseClientProvider
│       │       ├── service/         # LocationService (expect)
│       │       └── ui/              # Screens, ViewModels, Navigation
│       ├── androidMain/             # Android actuals & MainActivity
│       └── iosMain/                 # iOS actuals & MainViewController
├── iosApp/                          # Xcode project + Swift entry point
├── database/
│   └── schema.sql                   # Supabase schema (PostGIS)
├── secrets.properties.template      # Template – copy & fill in real keys
└── gradle/
    └── libs.versions.toml           # Version catalog
```

## Getting Started

### 1 – Configure secrets

```bash
cp secrets.properties.template secrets.properties
```

Edit `secrets.properties` and fill in your Supabase project URL and anon key:

```properties
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key
```

> **secrets.properties is listed in `.gitignore` and must never be committed.**

### 2 – Set up the Supabase database

Run the SQL in `database/schema.sql` in your Supabase project's SQL editor.  
The schema requires the **PostGIS** extension (enabled automatically by the first line).

### 3 – Android

Open the project in Android Studio Arctic Fox or later, select the `composeApp` run configuration, and run on a device or emulator with Google Play Services.

### 4 – iOS

```bash
./gradlew :composeApp:assembleDebug   # optional – pre-builds the KMP framework
open iosApp/iosApp.xcodeproj
```

Build and run the `iosApp` target from Xcode (requires macOS with Xcode 15+).

## App Screens

| Screen | Description |
|--------|-------------|
| **Adventure List** | Browse and select available scavenger hunts |
| **Active Adventure** | Real-time GPS guidance to the next waypoint |
| **Quiz Question** | Answer multiple-choice questions to unlock the next clue |

## Architecture

* **Repository pattern** – `AdventureRepository` wraps all Supabase calls.
* **expect/actual** – `LocationService` is declared in `commonMain` and implemented natively in `androidMain` (Fused Location) and `iosMain` (CLLocationManager).
* **BuildKonfig** – Supabase credentials are injected at compile time from `secrets.properties` and never stored in source code.
* **Row-Level Security** – All Supabase tables use RLS; users can only read/write their own progress rows.
