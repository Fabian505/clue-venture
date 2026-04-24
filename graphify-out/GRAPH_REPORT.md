# Graph Report - clue-venture  (2026-04-24)

## Corpus Check
- 26 files · ~31,063 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 121 nodes · 104 edges · 13 communities detected
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 1 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 4 edges
2. `ComposeView` - 4 edges
3. `getAdventures()` - 2 edges
4. `createAdventure()` - 2 edges
5. `deleteAdventure()` - 2 edges
6. `getAdventureLocations()` - 2 edges
7. `deleteAdventureLocation()` - 2 edges
8. `reorderAdventureLocations()` - 2 edges
9. `updateAdventure()` - 2 edges
10. `appendAdventureLocations()` - 2 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities

### Community 0 - "Community 0"
Cohesion: 0.09
Nodes (6): BottomBarIcons, BottomTab, EditLocationItem, Existing, MapPickerTarget, New

### Community 2 - "Community 2"
Cohesion: 0.36
Nodes (8): appendAdventureLocations(), createAdventure(), deleteAdventure(), deleteAdventureLocation(), getAdventureLocations(), getAdventures(), reorderAdventureLocations(), updateAdventure()

### Community 3 - "Community 3"
Cohesion: 0.22
Nodes (6): AdventureEntity, AdventureInsertEntity, AdventureLocationEntity, AdventureLocationInsertEntity, AdventureLocationOrderUpdateEntity, AdventureUpdateEntity

### Community 5 - "Community 5"
Cohesion: 0.22
Nodes (5): ComposeView, ContentView, MainViewController(), UIViewControllerRepresentable, View

### Community 6 - "Community 6"
Cohesion: 0.25
Nodes (6): Adventure, AdventureDraft, AdventureLocation, AdventureLocationDraft, AdventureMetadataDraft, GeoPoint

### Community 7 - "Community 7"
Cohesion: 0.33
Nodes (1): MainActivity

### Community 8 - "Community 8"
Cohesion: 0.67
Nodes (1): AndroidPlatform

### Community 9 - "Community 9"
Cohesion: 0.67
Nodes (1): Greeting

### Community 10 - "Community 10"
Cohesion: 0.67
Nodes (1): Platform

### Community 11 - "Community 11"
Cohesion: 0.67
Nodes (1): ComposeAppCommonTest

### Community 12 - "Community 12"
Cohesion: 0.67
Nodes (1): IOSPlatform

### Community 13 - "Community 13"
Cohesion: 0.67
Nodes (2): App, iOSApp

### Community 14 - "Community 14"
Cohesion: 1.0
Nodes (1): TodoItem

## Knowledge Gaps
- **22 isolated node(s):** `AndroidPlatform`, `TodoItem`, `AdventureEntity`, `AdventureInsertEntity`, `AdventureUpdateEntity` (+17 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 7`** (6 nodes): `MainActivity.kt`, `AppAndroidPreview()`, `MainActivity`, `.hideSystemBars()`, `.onCreate()`, `.onWindowFocusChanged()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 8`** (3 nodes): `Platform.android.kt`, `AndroidPlatform`, `getPlatform()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 9`** (3 nodes): `Greeting.kt`, `Greeting`, `.greet()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 10`** (3 nodes): `Platform.kt`, `getPlatform()`, `Platform`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 11`** (3 nodes): `ComposeAppCommonTest.kt`, `ComposeAppCommonTest`, `.example()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 12`** (3 nodes): `Platform.ios.kt`, `getPlatform()`, `IOSPlatform`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 13`** (3 nodes): `App`, `iOSApp`, `iOSApp.swift`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 14`** (2 nodes): `TodoItem.kt`, `TodoItem`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `AndroidPlatform`, `TodoItem`, `AdventureEntity` to the rest of the system?**
  _22 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.13 - nodes in this community are weakly interconnected._