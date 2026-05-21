# ClueVenture

GPS-basierte Adventure-App für Android. Nutzer können eigene Schnitzeljagden erstellen, veröffentlichen und spielen — mit Echtzeit-GPS-Tracking, Nähe-Feedback, Quiz-Fragen, einem Punktesystem und einer Rangliste.

---

## Features

### Adventure-Liste
Die Ansicht zeigt alle öffentlich verfügbaren Adventures als scrollbare Liste mit Titel, Kurzbeschreibung, Schwierigkeitsgrad und geschätzter Dauer. Von hier aus kann ein Adventure gestartet oder (bei eigenen Adventures) bearbeitet werden.

### Adventure erstellen & bearbeiten
Jeder angemeldete Nutzer kann eigene Adventures anlegen. Der Editor erlaubt:

- **Metadaten**: Titel, Beschreibung, Schwierigkeitsgrad (Leicht / Mittel / Schwer), Sichtbarkeit (öffentlich / privat)
- **Startpunkt**: Auswahl per Kartenpin oder Kooridanten
- **Checkpoints**: Beliebig viele Wegpunkte auf der Karte setzen, per Drag & Drop umsortieren, einzeln löschen. Jeder Checkpoint hat:
  - Einen Namen
  - Einen Punktwert (Basis-Punkte für das Erreichen; kann nicht gesetzt sein)
  - Ein optionales Zeitlimit (in Sekunden); wird keines gesetzt, wird automatisch eines anhand der Strecke berechnet
  - Bis zu 3 Hinweise (Hinweis 0 kostenlos, Hinweise 1–2 kosten Punkte; optionales Bild pro Hinweis)
- **Quiz**: Multiple-Choice-Fragen zum Adventure, die während des Spiels freigeschaltet werden

Änderungen an Quiz-Fragen werden sofort gespeichert; Hinweise werden beim Klick auf „Speichern" gebündelt übertragen.

### Gameplay

Nach dem Start eines Adventures beginnt eine aktive Spiel-Session:

**GPS-Tracking & Checkpoint-Erkennung**
Das Gerät aktualisiert kontinuierlich die GPS-Position. Ein Checkpoint gilt als erreicht, sobald der Nutzer sich innerhalb von 5 Metern befindet. Checkpoints müssen in der festgelegten Reihenfolge besucht werden.

**Karte**
Eine interaktive Karte zeigt den aktuellen Standort, alle noch ausstehenden Checkpoints und die geplante Route. Die Karte bleibt nordorientiert.

**Nähe-Feedback (Hot/Cold)**
Sobald der Nutzer innerhalb von 50 Metern eines Checkpoints ist, erscheint ein farbiger Overlay mit Distanz-Feedback:

| Status | Entfernung | Farbe |
|--------|-----------|-------|
| Sehr Heiß | < 5 m | Rot |
| Heiß | 5–15 m | Dunkelorange |
| Warm | 15–30 m | Orange |
| Kalt | 30–50 m | Hellorange |
| Sehr Kalt | > 50 m | Blau |

**Hinweise**
Am Checkpoint kann der Nutzer bis zu drei Hinweise abrufen. Hinweis 0 ist kostenlos und wird automatisch beim Erreichen des vorherigen Checkpoints angezeigt. Hinweise 1 und 2 kosten Punkte und müssen aktiv angefordert werden. Hinweise können Text und/oder ein Bild enthalten.

**Quiz**
Während des Spiels werden Multiple-Choice-Fragen freigeschaltet (eine Frage pro 200 m Gesamtroute). Die Fragen können jederzeit beantwortet werden. Punkte für korrekte Antworten werden sofort gutgeschrieben.

**Abbruch**
Das Adventure kann jederzeit abgebrochen werden. Bereits live verdiente Checkpoint- und Quiz-Punkte bleiben erhalten; der Abschlussbonus wird nicht ausgezahlt. Der Versuch wird in der Datenbank als `is_completed = false` gespeichert.

### Punkte & Gamification

Das System berechnet Punkte aus drei unabhängigen Quellen:

#### Checkpoint-Punkte
Beim Erreichen eines Checkpoints wird ein zeitabhängiger Bonus berechnet:

- Innerhalb des Zeitlimits → volle Basispunkte
- Nach dem Zeitlimit → linearer Abfall bis auf 10 % bei doppeltem Zeitlimit
- Kein Zeitlimit konfiguriert → volle Basispunkte, keine Strafe

Das Zeitlimit wird entweder manuell pro Checkpoint gesetzt oder automatisch aus der Streckenlänge (÷ 1,2 m/s Gehgeschwindigkeit) berechnet.

#### Abschlussbonus
Beim Abschluss eines Adventures wird ein zeitbasierter Bonus berechnet. Der Maximalwert hängt vom Schwierigkeitsgrad ab:

| Schwierigkeitsgrad | Maximalpunkte |
|--------------------|---------------|
| Leicht (Standard) | 1.000 |
| Mittel | 1.500 |
| Schwer | 2.000 |

Die Punkte verfallen linear auf 10 %, wenn das Doppelte der erwarteten Zeit überschritten wird. Zusätzlich wird ein konfigurierbarer Fixbonus (`completionPoints`) aus der Adventure-Definition addiert.

#### Quiz-Punkte
Punkte für korrekte Quiz-Antworten werden direkt beim Beantworten live ins Profil geschrieben - nicht im Abschlussbonus enthalten, um Doppelzählung zu vermeiden.

### Feedback
Nach dem Abschluss (oder Abbruch) eines Adventures erscheint ein Feedback-Formular mit:
- Bewertung der Schwierigkeit (1–5 Sterne)
- Gesamtbewertung (1–5 Sterne)
- Optionalem Freitext-Kommentar

### Profil & Rangliste
Der Profil-Tab zeigt Gesamtpunkte, abgeschlossene Adventures und eine persönliche Statistik. Die Rangliste listet alle Nutzer nach Punkten sortiert — filterbar nach Zeitraum:

- **Alle Zeiten**
- **Diese Woche**
- **Dieser Monat**

### Authentifizierung
Eigenes Authentifizierungssystem (kein Supabase Auth). Nutzer registrieren sich mit E-Mail, Passwort und Nutzernamen. Passwörter werden gehasht gespeichert. Alle Datenbankzugriffe verwenden RLS mit `USING (true)`.

---

## Technologie-Stack

| Technologie | Version | Verwendung |
|-------------|---------|-----------|
| Kotlin | 2.3.20 | Programmiersprache |
| Kotlin Multiplatform | — | Code-Sharing zwischen Plattformen |
| Compose Multiplatform | 1.10.3 | Deklaratives UI-Framework |
| Material 3 | — | UI-Komponenten & Design-System |
| Supabase Kotlin SDK | 3.5.0 | Backend-Client (Datenbank, Storage) |
| postgrest-kt | 3.5.0 | PostgreSQL-Zugriff via REST |
| storage-kt | 3.5.0 | Bild-Upload (Hint-Bilder) |
| MapLibre Android SDK | 13.0.2 | Kartenrendering (OpenGL) |
| OpenMapView | 0.13.1 | Karten-Picker (Punkt auf Karte setzen) |
| Coil 3 | 3.0.4 | Asynchrones Laden von Bildern (OkHttp) |
| Google Play Services Location | 21.3.0 | GPS-Positionierung |
| kotlinx.serialization | — | JSON-Serialisierung für Supabase-DTOs |
| AndroidX Lifecycle | 2.10.0 | ViewModel & Lifecycle-aware Compose |

**Backend**: [Supabase](https://supabase.com) (gehostet) — PostgreSQL-Datenbank via PostgREST, Datei-Storage für Hinweis-Bilder.

---

## Architektur

Das Projekt folgt dem **Kotlin Multiplatform**-Pattern mit `expect`/`actual`:

```
composeApp/src/
  commonMain/   ← Shared UI (Compose) + gesamte Business-Logik
  androidMain/  ← Plattform-Implementierungen: Supabase, GPS, MapLibre
```

- Alle Berechnungen (Punkte, Proximity, Zeitlimits) sind pure Funktionen in `commonMain/AdventureModels.kt`
- Datenbankzugriffe werden als `expect`-Funktionen in `commonMain/AdventureRepository.kt` deklariert und in `androidMain/AdventureRepository.kt` mit Supabase implementiert
- UI-Composables schreiben nie direkt Punkte — immer über `updateUserPoints()`
- Keine Spiellogik in `PlatformMap.android.kt`

> iOS-Targets (`iosArm64`, `iosSimulatorArm64`) sind in `build.gradle.kts` deklariert, aber nicht implementiert. Nur der Android-Build ist funktionsfähig.

---

## Datenbankschema

Die SQL-Dateien im Projektwurzelverzeichnis richten das Schema in Supabase ein:

| Datei | Inhalt |
|-------|--------|
| `supabase-adventures-schema.sql` | Tabellen: `adventures`, `adventure_locations`, `hints` |
| `supabase-adventure-quiz-schema.sql` | Tabellen: `quiz_questions`, `quiz_answers`, `adventure_attempts`, `user_answers`, `user_progress`, `users`, `user_profiles` + RLS |
| `supabase-adventure-feedback-schema.sql` | Tabelle: `adventure_feedback` |
| `supabase-visibility-schema.sql` | Migration: `is_public`-Spalte in `adventures` |
| `supabase-hint-image-schema.sql` | Migration: `image_url`-Spalte in `hints` |
| `supabase-completed-at-trigger.sql` | Trigger: `completed_at` server-seitig setzen |
| `supabase-leaderboard-periods-schema.sql` | Views/Funktionen für Rangliste (all-time, weekly, monthly) |
| `supabase-adventures-insert.sql` | Beispiel-Adventures |
| `supabase-adventures-insert-quiz.sql` | Beispiel-Quiz-Daten |

Alle Dateien werden im Supabase SQL-Editor ausgeführt.

---

## Setup

### Voraussetzungen
- Android Studio (Ladybug oder neuer)
- JDK 17+ (via Android Studio JBR)
- Android SDK API 24+

### Build

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Debug APK bauen
C:\Users\hamac\.gradle\wrapper\dists\gradle-9.3.1-bin\...\gradle.bat :composeApp:assembleDebug

# Nur Kotlin kompilieren (schnelle Syntaxprüfung)
C:\Users\hamac\.gradle\wrapper\dists\gradle-9.3.1-bin\...\gradle.bat :composeApp:compileDebugKotlin

# Unit Tests ausführen
C:\Users\hamac\.gradle\wrapper\dists\gradle-9.3.1-bin\...\gradle.bat :composeApp:testDebugUnitTest
```

> `gradle-wrapper.jar` ist nicht im Repository. Der lokal gecachte Gradle-Distributions-Pfad wird direkt verwendet (siehe `CLAUDE.md` für den vollständigen Pfad).
