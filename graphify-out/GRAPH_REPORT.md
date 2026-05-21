# Graph Report - /mnt/c/Daten/Studium/Entwicklung/clue-venture  (2026-05-20)

## Corpus Check
- 55 files · ~82,984 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 407 nodes · 467 edges · 31 communities detected
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 14 edges (avg confidence: 0.83)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Repository Operations|Repository Operations]]
- [[_COMMUNITY_Adventure Domain Models|Adventure Domain Models]]
- [[_COMMUNITY_Supabase Entities|Supabase Entities]]
- [[_COMMUNITY_Feature Specifications|Feature Specifications]]
- [[_COMMUNITY_Compose App UI|Compose App UI]]
- [[_COMMUNITY_Launcher Icon Assets|Launcher Icon Assets]]
- [[_COMMUNITY_Map Platform Integration|Map Platform Integration]]
- [[_COMMUNITY_Gameplay UI Flow|Gameplay UI Flow]]
- [[_COMMUNITY_Android Location Service|Android Location Service]]
- [[_COMMUNITY_Shared Location API|Shared Location API]]
- [[_COMMUNITY_iOS Compose Hosting|iOS Compose Hosting]]
- [[_COMMUNITY_Android Round Icons|Android Round Icons]]
- [[_COMMUNITY_iOS App Icon|iOS App Icon]]
- [[_COMMUNITY_Android Main Activity|Android Main Activity]]
- [[_COMMUNITY_Common Test Coverage|Common Test Coverage]]
- [[_COMMUNITY_Quiz UI|Quiz UI]]
- [[_COMMUNITY_HDPI Clue Icon|HDPI Clue Icon]]
- [[_COMMUNITY_XXXHDPI Clue Icon|XXXHDPI Clue Icon]]
- [[_COMMUNITY_Proximity UI|Proximity UI]]
- [[_COMMUNITY_Authentication UI|Authentication UI]]
- [[_COMMUNITY_MDPI Clue Icon|MDPI Clue Icon]]
- [[_COMMUNITY_XXXHDPI Android Icon|XXXHDPI Android Icon]]
- [[_COMMUNITY_Android Platform API|Android Platform API]]
- [[_COMMUNITY_Greeting Sample|Greeting Sample]]
- [[_COMMUNITY_iOS App Entry|iOS App Entry]]
- [[_COMMUNITY_Shared Platform API|Shared Platform API]]
- [[_COMMUNITY_HDPI Android Icon|HDPI Android Icon]]
- [[_COMMUNITY_Android Image Picker|Android Image Picker]]
- [[_COMMUNITY_Adventure List Wrapper|Adventure List Wrapper]]
- [[_COMMUNITY_Shared Image Picker|Shared Image Picker]]
- [[_COMMUNITY_Shared Map API|Shared Map API]]

## God Nodes (most connected - your core abstractions)
1. `Android` - 9 edges
2. `AndroidLocationService` - 7 edges
3. `LocationService` - 7 edges
4. `ComposeAppCommonTest` - 6 edges
5. `Root Graph Report Summary` - 6 edges
6. `MainActivity` - 5 edges
7. `ComposeView` - 5 edges
8. `ClueVenture GPS Adventure App` - 5 edges
9. `Active Gameplay Session` - 5 edges
10. `Round green Android launcher icon` - 5 edges

## Surprising Connections (you probably didn't know these)
- `Graphify Knowledge Graph Tooling` --references--> `Root Graph Report Summary`  [EXTRACTED]
  AGENTS.md → graphify-out/GRAPH_REPORT.md
- `ClueVenture GPS Adventure App` --references--> `iOS Targets Declared but Unsupported`  [EXTRACTED]
  README.md → CLAUDE.md
- `Gamification Points System` --conceptually_related_to--> `Points System`  [INFERRED]
  CLAUDE.md → features/Gamification.md
- `Adventure Create and Edit Workflow` --conceptually_related_to--> `Abenteuer Feature Specification`  [INFERRED]
  README.md → features/Abenteuer.md
- `Active Gameplay Session` --references--> `Hint System`  [EXTRACTED]
  README.md → features/Gamification.md

## Hyperedges (group relationships)
- **Documented KMP Architecture** — claude_kotlin_multiplatform_architecture, claude_commonmain_business_logic, claude_androidmain_platform_implementations, readme_clueventure_app [EXTRACTED 1.00]
- **Feature Specifications** — abenteuer_feature_specification, benutzer_feature_specification, gamification_feature_specification [EXTRACTED 1.00]
- **Gameplay Gamification Loop** — readme_gameplay_session, gamification_points_system, gamification_hint_system, gamification_puzzle_system, readme_profile_leaderboard [INFERRED 0.88]
- **ic_launcher_clue hdpi visual composition** — composeapp_android_mipmap_hdpi_ic_launcher_clue_png, android_hdpi_launcher_icon_asset, clue_venture_launcher_brand_mark, teal_abstract_clue_route_symbol, orange_highlight_markers, white_icon_background [INFERRED 0.88]
- **The file is an Android hdpi round launcher bitmap asset whose visual identity is a green circular icon with an Android robot symbol.** — file:composeApp/src/androidMain/res/mipmap-hdpi/ic_launcher_round.png, image:android_round_launcher_icon_green_robot, platform:android, resource_density:hdpi [INFERRED 0.75]
- **The file is an Android mdpi launcher bitmap asset whose visual identity is a green icon with an Android robot symbol.** — file:composeApp/src/androidMain/res/mipmap-mdpi/ic_launcher.png, image:android_launcher_icon_green_robot, platform:android, resource_density:mdpi [INFERRED 0.75]
- **Clue Icon Visual System** — ic_launcher_clue_teal_magnifying_glass_mark, ic_launcher_clue_orange_clue_accents, ic_launcher_clue_white_icon_background [INFERRED 0.76]
- **The file is an Android mdpi round launcher bitmap asset whose visual identity is a green circular icon with an Android robot symbol.** — file:composeApp/src/androidMain/res/mipmap-mdpi/ic_launcher_round.png, image:android_round_launcher_icon_green_robot, platform:android, resource_density:mdpi, resource_shape:round_launcher_icon [INFERRED 0.75]
- **The file is an Android xhdpi launcher bitmap asset whose visual identity is a green icon with an Android robot symbol.** — file:composeApp/src/androidMain/res/mipmap-xhdpi/ic_launcher.png, image:android_launcher_icon_green_robot, platform:android, resource_density:xhdpi [INFERRED 0.75]
- **The file is an Android xhdpi launcher bitmap asset whose visual identity is a clue/investigation mark built from teal magnifying-glass shapes and orange accents.** — file:composeApp/src/androidMain/res/mipmap-xhdpi/ic_launcher_clue.png, image:clue_launcher_icon_magnifying_glass, platform:android, resource_density:xhdpi [INFERRED 0.75]
- **Round Launcher Icon Composition** — ic_launcher_round_android_round_launcher_icon, ic_launcher_round_green_circular_background, ic_launcher_round_white_ghost_mascot, ic_launcher_round_android_mipmap_xhdpi_asset [EXTRACTED 1.00]
- **The file is an Android xxhdpi standard launcher bitmap asset whose visual identity is a green rounded-square icon with an Android robot symbol.** — file:composeApp/src/androidMain/res/mipmap-xxhdpi/ic_launcher.png, image:android_launcher_icon_green_robot_xxhdpi, platform:android, resource_density:xxhdpi, resource_shape:standard_launcher_icon [INFERRED 0.75]
- **The file is an Android xxhdpi custom launcher bitmap whose composition presents a clue-investigation visual identity through a pale background, teal clue glyph, and orange evidence accents.** — file:composeApp/src/androidMain/res/mipmap-xxhdpi/ic_launcher_clue.png, image:clue_venture_launcher_icon, visual:pale_icon_background, visual:teal_clue_glyph, visual:orange_clue_accents, concept:clue_investigation_theme, platform:android, resource_density:xxhdpi, resource_variant:clue_launcher_icon [INFERRED 0.75]
- **The file is an Android xxhdpi round launcher bitmap asset whose visual identity is a green circular icon with an Android robot symbol.** — file:composeApp/src/androidMain/res/mipmap-xxhdpi/ic_launcher_round.png, image:android_round_launcher_icon_green_robot, platform:android, resource_density:xxhdpi, resource_variant:round_launcher_icon [INFERRED 0.75]
- **Android Launcher Icon Visual Composition** — ic_launcher_android_launcher_icon, ic_launcher_android_robot_logo, ic_launcher_green_grid_background, ic_launcher_material_drop_shadow [EXTRACTED 1.00]
- **Clue Launcher Icon Visual Composition** — ic_launcher_clue_app_icon, ic_launcher_clue_teal_magnifying_glass, ic_launcher_clue_orange_question_mark, ic_launcher_clue_white_background [EXTRACTED 0.92]
- **Clue Investigation Semantics** — ic_launcher_clue_teal_magnifying_glass, ic_launcher_clue_orange_question_mark, ic_launcher_clue_investigation_identity [INFERRED 0.86]
- **Android Round Launcher Icon Composition** — ic_launcher_round_android_launcher_icon, ic_launcher_round_green_circular_background, ic_launcher_round_white_android_mascot, ic_launcher_round_android_antennae, ic_launcher_round_mascot_eye_cutouts [EXTRACTED 1.00]
- **The file is a 1024-pixel iOS app icon asset whose visual identity combines a white canvas, teal magnifying-glass/search symbol, and warm clue/path accents for clue-adventure branding.** — file:iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png, image:ios_app_icon_clue_search_motif, platform:ios, resource_variant:ios_app_icon_1024, concept:clue_adventure_branding [INFERRED 0.75]

## Communities

### Community 0 - "Repository Operations"
Cohesion: 0.07
Nodes (43): AndroidSessionStorage, appendAdventureLocations(), authenticateUser(), cancelAdventureAttempt(), createAdventure(), createQuizQuestion(), currentTimeMillis(), deleteAdventure() (+35 more)

### Community 1 - "Adventure Domain Models"
Cohesion: 0.05
Nodes (24): Adventure, AdventureAttempt, AdventureDraft, AdventureFeedback, AdventureFeedbackDraft, AdventureLocation, AdventureLocationDraft, AdventureMetadataDraft (+16 more)

### Community 2 - "Supabase Entities"
Cohesion: 0.06
Nodes (32): AdventureAttemptCancelEntity, AdventureAttemptEntity, AdventureAttemptFinishEntity, AdventureAttemptInsertEntity, AdventureAttemptMarkCompleteEntity, AdventureEntity, AdventureFeedbackEntity, AdventureFeedbackInsertEntity (+24 more)

### Community 3 - "Feature Specifications"
Cohesion: 0.07
Nodes (36): Difficulty Levels Leicht Medium Schwer, Abenteuer Feature Specification, Station / Ort Concept, Graphify Knowledge Graph Tooling, Run graphify update after code modifications, Device Hash Identification, Benutzer Feature Specification, Administrator and User Groups (+28 more)

### Community 4 - "Compose App UI"
Cohesion: 0.06
Nodes (8): BottomBarIcons, BottomTab, Edit, EditLocationItem, Existing, MapPickerTarget, New, QuizEditorState

### Community 5 - "Launcher Icon Assets"
Cohesion: 0.09
Nodes (24): clue investigation theme, Green Android launcher icon, Green Android launcher icon, Round green Android launcher icon, Clue launcher icon with magnifying glass, Clue Venture launcher icon, Android, mipmap-hdpi (+16 more)

### Community 6 - "Map Platform Integration"
Cohesion: 0.17
Nodes (20): applyAdventurePinsOverlay(), applyPickerOverlay(), applyRouteOverlay(), buildRouteUrl(), ensureAdventurePinsLayer(), ensureLocationLayer(), ensurePickerLayer(), ensureRouteLayers() (+12 more)

### Community 7 - "Gameplay UI Flow"
Cohesion: 0.17
Nodes (2): AdventureGameCallbacks, GameState

### Community 8 - "Android Location Service"
Cohesion: 0.24
Nodes (3): AndroidLocationService, getLocationService(), initializeLocationService()

### Community 9 - "Shared Location API"
Cohesion: 0.25
Nodes (2): getLocationService(), LocationService

### Community 10 - "iOS Compose Hosting"
Cohesion: 0.29
Nodes (4): ComposeView, ContentView, UIViewControllerRepresentable, View

### Community 11 - "Android Round Icons"
Cohesion: 0.32
Nodes (8): Android Antennae, Android Launcher Round Icon, Android mipmap-xhdpi Asset, Android Round Launcher Icon, Green Circular Background, Mascot Eye Cutouts, White Android Mascot, White Ghost-Like Mascot

### Community 12 - "iOS App Icon"
Cohesion: 0.29
Nodes (7): clue adventure branding, iOS app icon with clue search motif, iOS, 1024 pixel iOS app icon, orange yellow clue path accents, teal magnifying glass symbol, white square icon canvas

### Community 13 - "Android Main Activity"
Cohesion: 0.33
Nodes (2): AppAndroidPreview(), MainActivity

### Community 14 - "Common Test Coverage"
Cohesion: 0.29
Nodes (1): ComposeAppCommonTest

### Community 15 - "Quiz UI"
Cohesion: 0.53
Nodes (4): AnswerButton(), answerLabelColor(), answerOrderLabel(), QuizScreen()

### Community 16 - "HDPI Clue Icon"
Cohesion: 0.33
Nodes (5): Android hdpi launcher icon asset, Clue Venture launcher brand mark, orange highlight markers, teal abstract clue or route symbol, white icon background

### Community 17 - "XXXHDPI Clue Icon"
Cohesion: 0.4
Nodes (6): Clue App Launcher Icon, Investigation Game Identity, Orange Question Mark Clue, Teal Magnifying Glass Symbol, White Icon Background, XXXHDPI Clue Launcher Asset

### Community 18 - "Proximity UI"
Cohesion: 0.6
Nodes (3): ProximityAlertPopup(), proximityDistanceText(), ProximityFeatureScreen()

### Community 19 - "Authentication UI"
Cohesion: 0.6
Nodes (3): AuthScreen(), LoginScreenUI(), RegisterScreenUI()

### Community 20 - "MDPI Clue Icon"
Cohesion: 0.6
Nodes (5): Clue Android Launcher Icon, Clue App Branding, Orange Clue Accents, Teal Magnifying Glass Mark, White Icon Background

### Community 21 - "XXXHDPI Android Icon"
Cohesion: 0.5
Nodes (5): Android Launcher Icon, Android Robot Logo, Green Grid Background, Material Drop Shadow, XXXHDPI Launcher Density Asset

### Community 22 - "Android Platform API"
Cohesion: 0.67
Nodes (2): AndroidPlatform, getPlatform()

### Community 24 - "Greeting Sample"
Cohesion: 0.5
Nodes (1): Greeting

### Community 25 - "iOS App Entry"
Cohesion: 0.5
Nodes (2): App, iOSApp

### Community 26 - "Shared Platform API"
Cohesion: 0.67
Nodes (2): getPlatform(), Platform

### Community 27 - "HDPI Android Icon"
Cohesion: 0.67
Nodes (4): Green Rounded Square Background, Launcher Icon Image Asset, Mobile App Identity, White Android Robot Mascot

### Community 28 - "Android Image Picker"
Cohesion: 0.67
Nodes (1): rememberImagePicker()

### Community 29 - "Adventure List Wrapper"
Cohesion: 0.67
Nodes (1): AdventureListScreenWrapper()

### Community 30 - "Shared Image Picker"
Cohesion: 0.67
Nodes (1): rememberImagePicker()

### Community 31 - "Shared Map API"
Cohesion: 0.67
Nodes (1): PlatformMap()

## Knowledge Gaps
- **105 isolated node(s):** `AndroidSessionStorage`, `AdventureEntity`, `AdventureInsertEntity`, `AdventureUpdateEntity`, `AdventureLocationAdventureIdEntity` (+100 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Gameplay UI Flow`** (12 nodes): `AdventureGameCallbacks`, `AdventureGameScreen()`, `AdventurePointsSummary()`, `FeedbackSubmitHint()`, `FullscreenImageDialog()`, `GameState`, `HintBottomSheet()`, `HintContent()`, `NavigationIndicator()`, `PointsRow()`, `StarRatingInput()`, `AdventureGameScreen.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Shared Location API`** (9 nodes): `LocationService.kt`, `getLocationService()`, `LocationService`, `.getCurrentLocation()`, `.hasLocationPermissions()`, `.requestLocationPermissions()`, `.startLocationTracking()`, `.stopLocationTracking()`, `LocationService.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Main Activity`** (7 nodes): `MainActivity.kt`, `AppAndroidPreview()`, `MainActivity`, `.hideSystemBars()`, `.onCreate()`, `.onWindowFocusChanged()`, `MainActivity.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Common Test Coverage`** (7 nodes): `ComposeAppCommonTest.kt`, `ComposeAppCommonTest`, `.availableQuestionCountIsClampedAndSubtractsAnsweredQuestions()`, `.example()`, `.unlockedQuestionsAreBasedOnRouteDistance()`, `.unlockedQuestionSlotsOnlyIncreaseWhenRouteDistanceChanges()`, `ComposeAppCommonTest.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Platform API`** (4 nodes): `Platform.android.kt`, `Platform.android.kt`, `AndroidPlatform`, `getPlatform()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Greeting Sample`** (4 nodes): `Greeting.kt`, `Greeting`, `.greet()`, `Greeting.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `iOS App Entry`** (4 nodes): `App`, `iOSApp`, `iOSApp.swift`, `iOSApp.swift`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Shared Platform API`** (4 nodes): `Platform.kt`, `Platform.kt`, `getPlatform()`, `Platform`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android Image Picker`** (3 nodes): `ImagePicker.android.kt`, `rememberImagePicker()`, `ImagePicker.android.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Adventure List Wrapper`** (3 nodes): `AdventureListScreenWrapper()`, `AdventureListScreenWrapper.kt`, `AdventureListScreenWrapper.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Shared Image Picker`** (3 nodes): `ImagePicker.kt`, `rememberImagePicker()`, `ImagePicker.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Shared Map API`** (3 nodes): `PlatformMap.kt`, `PlatformMap.kt`, `PlatformMap()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `AndroidSessionStorage`, `AdventureEntity`, `AdventureInsertEntity` to the rest of the system?**
  _105 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Repository Operations` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Adventure Domain Models` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `Supabase Entities` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Feature Specifications` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Compose App UI` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Launcher Icon Assets` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._