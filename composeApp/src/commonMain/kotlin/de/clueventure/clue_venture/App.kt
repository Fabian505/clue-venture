package de.clueventure.clue_venture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

private object BottomBarIcons {
    val Left: ImageVector by lazy {
        ImageVector.Builder(
            name = "LeftMenu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 5f)
                lineTo(21f, 5f)
                lineTo(21f, 7f)
                lineTo(3f, 7f)
                close()
                moveTo(3f, 11f)
                lineTo(17f, 11f)
                lineTo(17f, 13f)
                lineTo(3f, 13f)
                close()
                moveTo(3f, 17f)
                lineTo(21f, 17f)
                lineTo(21f, 19f)
                lineTo(3f, 19f)
                close()
            }
        }.build()
    }

    val Map: ImageVector by lazy {
        ImageVector.Builder(
            name = "Map",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 5f)
                lineTo(8f, 3f)
                lineTo(15f, 6f)
                lineTo(21f, 4f)
                lineTo(21f, 19f)
                lineTo(16f, 21f)
                lineTo(9f, 18f)
                lineTo(3f, 20f)
                close()
                moveTo(9f, 6f)
                lineTo(9f, 15f)
                lineTo(15f, 18f)
                lineTo(15f, 9f)
                close()
            }
        }.build()
    }

    val Right: ImageVector by lazy {
        ImageVector.Builder(
            name = "RightSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 6f)
                lineTo(20f, 6f)
                lineTo(20f, 8f)
                lineTo(4f, 8f)
                close()
                moveTo(9f, 4f)
                lineTo(11f, 4f)
                lineTo(11f, 10f)
                lineTo(9f, 10f)
                close()
                moveTo(4f, 11f)
                lineTo(20f, 11f)
                lineTo(20f, 13f)
                lineTo(4f, 13f)
                close()
                moveTo(14f, 9f)
                lineTo(16f, 9f)
                lineTo(16f, 15f)
                lineTo(14f, 15f)
                close()
                moveTo(4f, 16f)
                lineTo(20f, 16f)
                lineTo(20f, 18f)
                lineTo(4f, 18f)
                close()
                moveTo(7f, 14f)
                lineTo(9f, 14f)
                lineTo(9f, 20f)
                lineTo(7f, 20f)
                close()
            }
        }.build()
    }
}

private enum class BottomTab(
    val icon: ImageVector,
    val contentDescription: String,
) {
    Left(icon = BottomBarIcons.Left, contentDescription = "Menue links"),
    Map(icon = BottomBarIcons.Map, contentDescription = "Karte"),
    Right(icon = BottomBarIcons.Right, contentDescription = "Menue rechts"),
}

private enum class MapPickerTarget {
    StartPoint,
    AdventureLocation,
}

private const val START_ADVENTURE_MAX_DISTANCE_METERS = 10.0

@Composable
@Preview
fun App() {
    val appScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isSessionLoaded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(BottomTab.Map) }
    var searchQuery by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var routeTargets by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var routedAdventureId by remember { mutableStateOf<String?>(null) }
    var showCreateAdventureDialog by remember { mutableStateOf(false) }
    var editingAdventure by remember { mutableStateOf<Adventure?>(null) }
    var adventurePendingDeletion by remember { mutableStateOf<Adventure?>(null) }
    var isDeletingAdventure by remember { mutableStateOf(false) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    var adventurePendingStart by remember { mutableStateOf<Adventure?>(null) }
    var isStartingAdventure by remember { mutableStateOf(false) }
    var startErrorMessage by remember { mutableStateOf<String?>(null) }
    var adventureRefreshKey by remember { mutableStateOf(0) }
    var activeAdventure by remember { mutableStateOf<Adventure?>(null) }
    var activeAdventureAttempt by remember { mutableStateOf<AdventureAttempt?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isOnline by remember { mutableStateOf(true) }
    var adventures by remember { mutableStateOf<List<Adventure>>(emptyList()) }
    var isLoadingAdventures by remember { mutableStateOf(true) }
    var loadAdventuresError by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        isOnline = isNetworkAvailable()
        val unregister = observeConnectivity { connected -> isOnline = connected }
        onDispose { unregister() }
    }

    LaunchedEffect(Unit) {
        currentUser = getCurrentUser()
        isSessionLoaded = true
    }

    LaunchedEffect(adventureRefreshKey) {
        isLoadingAdventures = true
        loadAdventuresError = false
        runCatching { getAdventures() }
            .onSuccess { adventures = it }
            .onFailure { adventures = emptyList(); loadAdventuresError = true }
        isLoadingAdventures = false
    }

    DisposableEffect(currentUser?.id, activeAdventure?.id) {
        if (currentUser == null || activeAdventure != null) {
            return@DisposableEffect onDispose { }
        }

        val locationService = runCatching { getLocationService() }.getOrNull()
            ?: return@DisposableEffect onDispose { }

        appScope.launch {
            locationService.startLocationTracking(interval = 5000) { location ->
                currentLocation = location.point
            }
        }

        onDispose {
            appScope.launch {
                locationService.stopLocationTracking()
            }
        }
    }

    fun clearActiveAdventure() {
        routeTargets = emptyList()
        routedAdventureId = null
        activeAdventure = null
        activeAdventureAttempt = null
    }

    fun clearStartRoute() {
        routeTargets = emptyList()
        routedAdventureId = null
    }

    fun resetAuthenticatedUiState() {
        clearActiveAdventure()
        selectedTab = BottomTab.Map
        searchQuery = ""
        showCreateAdventureDialog = false
        editingAdventure = null
        adventurePendingDeletion = null
        isDeletingAdventure = false
        deleteErrorMessage = null
        adventurePendingStart = null
        isStartingAdventure = false
        startErrorMessage = null
    }

    MaterialTheme {
        if (!isSessionLoaded) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (currentUser == null) {
            // Show login/register screen if not authenticated
            AuthScreen(
                onLoginSuccess = {
                    currentUser = it
                },
                onNavigateToAdventureList = { },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Show main app after authentication
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
                topBar = {
                    if (!isOnline) {
                        OfflineBanner()
                    }
                },
                bottomBar = {
                    if (editingAdventure == null && !showCreateAdventureDialog && activeAdventure == null) {
                        NavigationBar(
                            modifier = Modifier.height(64.dp),
                        ) {
                            BottomTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = {
                                        if (selectedTab == BottomTab.Map && tab != BottomTab.Map && activeAdventure == null) {
                                            clearStartRoute()
                                        }
                                        selectedTab = tab
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.contentDescription,
                                        )
                                    },
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                // Show game screen if adventure is active
                activeAdventure?.let { adventure ->
                    AdventureGameScreen(
                        adventure = adventure,
                        userId = currentUser?.id ?: return@let,
                        currentLocation = currentLocation,
                        initialAttempt = activeAdventureAttempt,
                        snackbarHostState = snackbarHostState,
                        callbacks = AdventureGameCallbacks(
                            onAdventureComplete = { points ->
                                appScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "🎉 $points Punkte verdient!",
                                        withDismissAction = true,
                                        duration = androidx.compose.material3.SnackbarDuration.Long,
                                    )
                                }
                                clearActiveAdventure()
                                adventureRefreshKey += 1
                            },
                            onClose = {
                                clearActiveAdventure()
                            },
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                } ?: run {
                    editingAdventure?.let { adventure ->
                        EditAdventureScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            adventure = adventure,
                            currentLocation = currentLocation,
                            snackbarHostState = snackbarHostState,
                            onDismiss = { editingAdventure = null },
                            onAdventureUpdated = {
                                adventureRefreshKey += 1
                            },
                        )
                    } ?: if (showCreateAdventureDialog) {
                        CreateAdventureScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            currentLocation = currentLocation,
                            snackbarHostState = snackbarHostState,
                            onDismiss = { showCreateAdventureDialog = false },
                            onAdventureCreated = {
                                showCreateAdventureDialog = false
                                adventureRefreshKey += 1
                            },
                        )
                    } else when (selectedTab) {
                        BottomTab.Left -> AdventureListScreenWrapper(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            currentLocation = currentLocation,
                            currentUserId = currentUser?.id,
                            adventures = adventures,
                            isLoading = isLoadingAdventures,
                            loadError = loadAdventuresError,
                            onNavigateToStart = { adventure ->
                                routedAdventureId = adventure.id
                                routeTargets = listOf(adventure.startPoint)
                                activeAdventureAttempt = null
                                selectedTab = BottomTab.Map
                            },
                            onStartAdventure = { adventure ->
                                // show start confirmation & perform distance check before activating
                                adventurePendingStart = adventure
                            },
                            onCreateAdventure = { showCreateAdventureDialog = true },
                            onEditAdventure = { adventure ->
                                editingAdventure = adventure
                            },
                            onDeleteAdventure = { adventure ->
                                adventurePendingDeletion = adventure
                                deleteErrorMessage = null
                            },
                        )

                        BottomTab.Map -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        ) {
                            PlatformMap(
                                modifier = Modifier.fillMaxSize(),
                                routeTargets = routeTargets,
                                onCurrentLocationChanged = { currentLocation = it },
                                adventurePins = if (routeTargets.isEmpty()) adventures else emptyList(),
                                onAdventurePinClicked = { adventure ->
                                    routedAdventureId = adventure.id
                                    routeTargets = listOf(adventure.startPoint)
                                },
                            )
                        }

                        BottomTab.Right -> ProfileAndLeaderboardTab(
                            currentUser = currentUser!!,
                            onLogout = {
                                appScope.launch {
                                    logoutUser()
                                    resetAuthenticatedUiState()
                                    currentUser = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        )
                    }
                }
            }

            adventurePendingDeletion?.let { adventure ->
                AlertDialog(
                    onDismissRequest = {
                        if (!isDeletingAdventure) {
                            adventurePendingDeletion = null
                        }
                    },
                    title = { Text("Abenteuer loeschen") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Willst du '${adventure.title}' wirklich loeschen?")
                            Text(
                                "Alle zugehoerigen Orte werden ebenfalls geloescht.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (deleteErrorMessage != null) {
                                Text(
                                    text = deleteErrorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isDeletingAdventure = true
                                deleteErrorMessage = null
                                appScope.launch {
                                    runCatching {
                                        deleteAdventure(adventure.id)
                                    }.onSuccess {
                                        if (routedAdventureId == adventure.id || activeAdventure?.id == adventure.id) {
                                            clearActiveAdventure()
                                        }
                                        if (editingAdventure?.id == adventure.id) {
                                            editingAdventure = null
                                        }
                                        adventurePendingDeletion = null
                                        adventureRefreshKey += 1
                                    }.onFailure { throwable ->
                                        deleteErrorMessage =
                                            throwable.message ?: "Abenteuer konnte nicht geloescht werden."
                                    }
                                    isDeletingAdventure = false
                                }
                            },
                            enabled = !isDeletingAdventure,
                        ) {
                            if (isDeletingAdventure) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Loeschen")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { adventurePendingDeletion = null },
                            enabled = !isDeletingAdventure,
                        ) {
                            Text("Abbrechen")
                        }
                    },
                )
            }

            adventurePendingStart?.let { adventure ->
                AlertDialog(
                    onDismissRequest = {
                        if (!isStartingAdventure) {
                            adventurePendingStart = null
                        }
                    },
                    title = { Text("Abenteuer starten") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Willst du '${adventure.title}' jetzt starten?")
                            Text(
                                text = "Schwierigkeit: ${adventure.difficulty ?: "Unbekannt"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Dauer: ${adventure.estimatedDurationMinutes?.let { "$it min" } ?: "Unbekannt"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = startDialogDistanceLabel(adventure.startPoint, currentLocation),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (startErrorMessage != null) {
                                Text(
                                    text = startErrorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isStartingAdventure = true
                                startErrorMessage = null
                                appScope.launch {
                                    if (!canStartAdventure(adventure.startPoint, currentLocation)) {
                                        startErrorMessage = "Du musst innerhalb von 10 m am Startpunkt sein."
                                        isStartingAdventure = false
                                        return@launch
                                    }

                                    runCatching {
                                        val locations = getAdventureLocations(adventure.id)
                                        if (locations.isEmpty()) {
                                            throw IllegalStateException("Dieses Abenteuer hat noch keine Orte.")
                                        }
                                        val user = currentUser
                                            ?: throw IllegalStateException("Kein Benutzer angemeldet.")
                                        val attempt = getCurrentAttemptForAdventure(adventure.id, user.id)
                                            ?: startAdventureAttempt(adventure.id, user.id)
                                        val sortedLocations = locations.sortedBy { it.orderIndex }
                                        val adventureWithLocations = adventure.copy(locations = sortedLocations)
                                        routedAdventureId = null
                                        routeTargets = emptyList()
                                        activeAdventure = adventureWithLocations
                                        activeAdventureAttempt = attempt
                                        selectedTab = BottomTab.Map
                                    }.onSuccess {
                                        adventurePendingStart = null
                                        snackbarHostState.showSnackbar(
                                            message = "Abenteuer gestartet: ${adventure.title}",
                                            withDismissAction = true,
                                        )
                                    }.onFailure { throwable ->
                                        startErrorMessage =
                                            throwable.message ?: "Abenteuer konnte nicht gestartet werden."
                                    }

                                    isStartingAdventure = false
                                }
                            },
                            enabled = !isStartingAdventure,
                        ) {
                            if (isStartingAdventure) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Starten")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { adventurePendingStart = null },
                            enabled = !isStartingAdventure,
                        ) {
                            Text("Abbrechen")
                        }
                    },
                )
            }

        }
    }
}

private val DIFFICULTY_OPTIONS = listOf("Leicht", "Mittel", "Schwer")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DifficultyDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value.ifBlank { "Keine Angabe" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Schwierigkeit (optional)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Keine Angabe") },
                onClick = { onValueChange(""); expanded = false },
            )
            DIFFICULTY_OPTIONS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onValueChange(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun CreateAdventureScreen(
    modifier: Modifier = Modifier,
    currentLocation: GeoPoint?,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onAdventureCreated: (Adventure) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var startLatitude by remember { mutableStateOf("") }
    var startLongitude by remember { mutableStateOf("") }

    var locationName by remember { mutableStateOf("") }
    var locationLatitude by remember { mutableStateOf("") }
    var locationLongitude by remember { mutableStateOf("") }
    var locations by remember { mutableStateOf<List<AdventureLocationDraft>>(emptyList()) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mapPickerTarget by remember { mutableStateOf<MapPickerTarget?>(null) }
    var mapPickedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var expandedHintSections by remember { mutableStateOf(emptySet<String>()) }
    var quizQuestions by remember { mutableStateOf(listOf<QuizQuestionDraft>()) }
    var quizEditorState by remember { mutableStateOf<QuizEditorState?>(null) }

    fun addLocation() {
        val parsedLatitude = locationLatitude.toDoubleOrNull()
        val parsedLongitude = locationLongitude.toDoubleOrNull()

        when {
            locationName.isBlank() -> errorMessage = "Bitte gib einen Namen fuer den Ort ein."
            parsedLatitude == null || parsedLongitude == null -> {
                errorMessage = "Bitte gib gueltige Koordinaten fuer den Ort ein."
            }

            !isValidLatitude(parsedLatitude) || !isValidLongitude(parsedLongitude) -> {
                errorMessage = "Koordinaten muessen in gueltigen Bereichen liegen."
            }

            else -> {
                errorMessage = null
                locations = locations + AdventureLocationDraft(
                    name = locationName.trim(),
                    point = GeoPoint(latitude = parsedLatitude, longitude = parsedLongitude),
                )
                locationName = ""
                locationLatitude = ""
                locationLongitude = ""
            }
        }
    }

    fun submitCreateAdventure() {
        val parsedStartLatitude = startLatitude.toDoubleOrNull()
        val parsedStartLongitude = startLongitude.toDoubleOrNull()

        when {
            title.isBlank() -> errorMessage = "Bitte gib einen Titel ein."
            summary.isBlank() -> errorMessage = "Bitte gib eine Kurzbeschreibung ein."
            parsedStartLatitude == null || parsedStartLongitude == null -> {
                errorMessage = "Bitte gib gueltige Startkoordinaten ein."
            }

            !isValidLatitude(parsedStartLatitude) || !isValidLongitude(parsedStartLongitude) -> {
                errorMessage = "Startkoordinaten muessen in gueltigen Bereichen liegen."
            }

            locations.isEmpty() -> {
                errorMessage = "Bitte fuege mindestens einen Ort hinzu."
            }

            else -> {
                errorMessage = null
                isSubmitting = true
                scope.launch {
                    runCatching {
                        val startPoint = GeoPoint(
                            latitude = parsedStartLatitude,
                            longitude = parsedStartLongitude,
                        )
                        val calculatedDurationMinutes = calculateAdventureDurationMinutes(
                            startPoint = startPoint,
                            locations = locations,
                        )

                        createAdventure(
                            AdventureDraft(
                                title = title.trim(),
                                summary = summary.trim(),
                                startPoint = startPoint,
                                difficulty = difficulty.takeIf { it.isNotBlank() }?.trim(),
                                estimatedDurationMinutes = calculatedDurationMinutes,
                                locations = locations,
                                quizQuestions = quizQuestions,
                                isPublic = isPublic,
                            ),
                        )
                    }.onSuccess { createdAdventure ->
                        onAdventureCreated(createdAdventure)
                    }.onFailure { throwable ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                throwable.message ?: "Abenteuer konnte nicht gespeichert werden.",
                                withDismissAction = true,
                            )
                        }
                    }
                    isSubmitting = false
                }
            }
        }
    }

    if (mapPickerTarget != null) {
        MapPointPickerScreen(
            modifier = modifier,
            title = when (mapPickerTarget) {
                MapPickerTarget.StartPoint -> "Startpunkt auf Karte waehlen"
                MapPickerTarget.AdventureLocation -> "Ort auf Karte waehlen"
                null -> "Ort auf Karte waehlen"
            },
            selectedPoint = mapPickedPoint,
            onCancel = {
                mapPickerTarget = null
                mapPickedPoint = null
            },
            onPointSelected = { selected ->
                mapPickedPoint = selected
            },
            onConfirm = { selected ->
                when (mapPickerTarget) {
                    MapPickerTarget.StartPoint -> {
                        startLatitude = selected.latitude.toString()
                        startLongitude = selected.longitude.toString()
                    }

                    MapPickerTarget.AdventureLocation -> {
                        if (locationName.isBlank()) {
                            locationName = "Ort ${locations.size + 1}"
                        }
                        locationLatitude = selected.latitude.toString()
                        locationLongitude = selected.longitude.toString()
                    }

                    null -> Unit
                }
                errorMessage = null
                mapPickerTarget = null
                mapPickedPoint = null
            },
        )
        return
    }

    if (quizEditorState != null) {
        QuizQuestionEditorScreen(
            modifier = modifier,
            initialDraft = (quizEditorState as? QuizEditorState.Edit)?.draft,
            onDismiss = { quizEditorState = null },
            onConfirm = { draft ->
                when (val state = quizEditorState) {
                    QuizEditorState.New -> quizQuestions = quizQuestions + draft
                    is QuizEditorState.Edit -> quizQuestions = quizQuestions.mapIndexed { i, q ->
                        if (i == state.index) draft else q
                    }
                    null -> Unit
                }
                quizEditorState = null
            },
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Neues Abenteuer",
                style = MaterialTheme.typography.headlineSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                    Text("Abbrechen")
                }
                Button(onClick = { submitCreateAdventure() }, enabled = !isSubmitting) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Speichern")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (currentLocation == null) {
                item {
                    Text(
                        text = "Kein aktueller Standort verfuegbar. Oeffne kurz die Kartenansicht, damit GPS geladen wird.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titel") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = summary,
                    onValueChange = {
                        summary = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kurzbeschreibung") },
                )
            }
            item {
                DifficultyDropdown(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Öffentlich", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = if (isPublic) "Sichtbar für alle Nutzer" else "Nur für dich sichtbar (Entwurf)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                    )
                }
            }
            item {
                Text(
                    text = "Dauer wird automatisch aus Route und Orten berechnet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Text(
                    text = "Startpunkt",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                Button(
                    onClick = {
                        val location = currentLocation ?: return@Button
                        startLatitude = location.latitude.toString()
                        startLongitude = location.longitude.toString()
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentLocation != null,
                ) {
                    Text("Aktuellen Standort als Startpunkt nutzen")
                }
            }
            item {
                Button(
                    onClick = {
                        mapPickedPoint = parseGeoPoint(startLatitude, startLongitude)
                        mapPickerTarget = MapPickerTarget.StartPoint
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                ) {
                    Text("Startpunkt auf Karte waehlen")
                }
            }
            item {
                OutlinedTextField(
                    value = startLatitude,
                    onValueChange = { startLatitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Start Latitude") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = startLongitude,
                    onValueChange = { startLongitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Start Longitude") },
                    singleLine = true,
                )
            }
            item {
                Text(
                    text = "Orte fuer das Abenteuer",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                Button(
                    onClick = {
                        val location = currentLocation ?: return@Button
                        if (locationName.isBlank()) {
                            locationName = "Ort ${locations.size + 1}"
                        }
                        locationLatitude = location.latitude.toString()
                        locationLongitude = location.longitude.toString()
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentLocation != null,
                ) {
                    Text("Aktuellen Standort fuer Ort nutzen")
                }
            }
            item {
                Button(
                    onClick = {
                        mapPickedPoint = parseGeoPoint(locationLatitude, locationLongitude)
                        mapPickerTarget = MapPickerTarget.AdventureLocation
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                ) {
                    Text("Ort auf Karte waehlen")
                }
            }
            item {
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ort Name") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = locationLatitude,
                    onValueChange = { locationLatitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ort Latitude") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = locationLongitude,
                    onValueChange = { locationLongitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ort Longitude") },
                    singleLine = true,
                )
            }
            item {
                Button(
                    onClick = { addLocation() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Ort hinzufuegen")
                }
            }
            if (locations.isNotEmpty()) {
                item {
                    Text(
                        text = "Hinzugefuegte Orte",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                itemsIndexed(
                    items = locations,
                    key = { _, location -> location.name + location.point.latitude + location.point.longitude }) { index, location ->
                    val locationKey = "${location.name}${location.point.latitude}${location.point.longitude}"
                    val hintsExpanded = locationKey in expandedHintSections
                    var uploadingHintIndex by remember { mutableStateOf<Int?>(null) }
                    val pickImage0 = rememberImagePicker { bytes ->
                        if (bytes != null) scope.launch {
                            uploadingHintIndex = 0
                            runCatching { uploadHintImage(bytes) }
                                .onSuccess { url -> locations = locations.withUpdatedLocationHintImage(index, 0, url) }
                                .onFailure { snackbarHostState.showSnackbar("Bild konnte nicht hochgeladen werden: ${it.message}", withDismissAction = true) }
                            uploadingHintIndex = null
                        }
                    }
                    val pickImage1 = rememberImagePicker { bytes ->
                        if (bytes != null) scope.launch {
                            uploadingHintIndex = 1
                            runCatching { uploadHintImage(bytes) }
                                .onSuccess { url -> locations = locations.withUpdatedLocationHintImage(index, 1, url) }
                                .onFailure { snackbarHostState.showSnackbar("Bild konnte nicht hochgeladen werden: ${it.message}", withDismissAction = true) }
                            uploadingHintIndex = null
                        }
                    }
                    val pickImage2 = rememberImagePicker { bytes ->
                        if (bytes != null) scope.launch {
                            uploadingHintIndex = 2
                            runCatching { uploadHintImage(bytes) }
                                .onSuccess { url -> locations = locations.withUpdatedLocationHintImage(index, 2, url) }
                                .onFailure { snackbarHostState.showSnackbar("Bild konnte nicht hochgeladen werden: ${it.message}", withDismissAction = true) }
                            uploadingHintIndex = null
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${index + 1}. ${location.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    expandedHintSections = if (hintsExpanded)
                                        expandedHintSections - locationKey
                                    else
                                        expandedHintSections + locationKey
                                }) {
                                    Text(if (hintsExpanded) "Hints ▲" else "Hints ▼")
                                }
                                TextButton(
                                    onClick = {
                                        if (index > 0) locations = locations.move(index, index - 1)
                                    },
                                    enabled = !isSubmitting && index > 0,
                                ) { Text("Hoch") }
                                TextButton(
                                    onClick = {
                                        if (index < locations.lastIndex) locations = locations.move(index, index + 1)
                                    },
                                    enabled = !isSubmitting && index < locations.lastIndex,
                                ) { Text("Runter") }
                                TextButton(
                                    onClick = {
                                        expandedHintSections = expandedHintSections - locationKey
                                        locations = locations - location
                                    },
                                    enabled = !isSubmitting,
                                ) { Text("Entfernen") }
                            }
                        }
                        if (hintsExpanded) {
                            Column(
                                modifier = Modifier.padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("Hints", style = MaterialTheme.typography.labelMedium)
                                OutlinedTextField(
                                    value = location.hints.textFor(0),
                                    onValueChange = { text ->
                                        locations = locations.withUpdatedLocationHint(index, 0, text)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Hint 0 (gratis, beim Ankommen gezeigt)") },
                                )
                                HintImagePickerRow(
                                    imageUrl = location.hints.imageUrlFor(0),
                                    isUploading = uploadingHintIndex == 0,
                                    onPickImage = pickImage0,
                                    onRemoveImage = { locations = locations.withUpdatedLocationHintImage(index, 0, null) },
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = location.hints.textFor(1),
                                        onValueChange = { text ->
                                            locations = locations.withUpdatedLocationHint(index, 1, text, location.hints.costFor(1))
                                        },
                                        modifier = Modifier.weight(2f),
                                        label = { Text("Hint 1 (kostenpflichtig)") },
                                    )
                                    OutlinedTextField(
                                        value = location.hints.costFor(1).takeIf { location.hints.textFor(1).isNotBlank() }?.toString() ?: "",
                                        onValueChange = { costText ->
                                            val cost = costText.toIntOrNull() ?: 0
                                            val text = location.hints.textFor(1)
                                            if (text.isNotBlank()) {
                                                locations = locations.withUpdatedLocationHint(index, 1, text, cost)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Pts") },
                                        singleLine = true,
                                        enabled = location.hints.textFor(1).isNotBlank(),
                                    )
                                }
                                HintImagePickerRow(
                                    imageUrl = location.hints.imageUrlFor(1),
                                    isUploading = uploadingHintIndex == 1,
                                    onPickImage = pickImage1,
                                    onRemoveImage = { locations = locations.withUpdatedLocationHintImage(index, 1, null) },
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = location.hints.textFor(2),
                                        onValueChange = { text ->
                                            locations = locations.withUpdatedLocationHint(index, 2, text, location.hints.costFor(2))
                                        },
                                        modifier = Modifier.weight(2f),
                                        label = { Text("Hint 2 (kostenpflichtig)") },
                                    )
                                    OutlinedTextField(
                                        value = location.hints.costFor(2).takeIf { location.hints.textFor(2).isNotBlank() }?.toString() ?: "",
                                        onValueChange = { costText ->
                                            val cost = costText.toIntOrNull() ?: 0
                                            val text = location.hints.textFor(2)
                                            if (text.isNotBlank()) {
                                                locations = locations.withUpdatedLocationHint(index, 2, text, cost)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Pts") },
                                        singleLine = true,
                                        enabled = location.hints.textFor(2).isNotBlank(),
                                    )
                                }
                                HintImagePickerRow(
                                    imageUrl = location.hints.imageUrlFor(2),
                                    isUploading = uploadingHintIndex == 2,
                                    onPickImage = pickImage2,
                                    onRemoveImage = { locations = locations.withUpdatedLocationHintImage(index, 2, null) },
                                )
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Quiz-Fragen",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                Button(
                    onClick = { quizEditorState = QuizEditorState.New },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                ) {
                    Text("+ Frage hinzufuegen")
                }
            }
            if (quizQuestions.isNotEmpty()) {
                itemsIndexed(quizQuestions, key = { i, _ -> "quiz-create-$i" }) { index, question ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Frage ${index + 1}: ${question.questionText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            question.answers.sortedBy { it.answerOrder }.forEachIndexed { i, answer ->
                                val label = listOf("A", "B", "C", "D").getOrElse(i) { "${i + 1}" }
                                Text(
                                    text = "${if (answer.isCorrect) "✓ " else ""}$label: ${answer.answerText}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (answer.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { quizEditorState = QuizEditorState.Edit(index, question) }) {
                                    Text("Bearbeiten")
                                }
                                TextButton(onClick = {
                                    quizQuestions = quizQuestions.filterIndexed { i, _ -> i != index }
                                }) {
                                    Text("Loeschen")
                                }
                            }
                        }
                    }
                }
            }
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditAdventureScreen(
    modifier: Modifier = Modifier,
    adventure: Adventure,
    currentLocation: GeoPoint?,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onAdventureUpdated: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var title by remember(adventure.id) { mutableStateOf(adventure.title) }
    var summary by remember(adventure.id) { mutableStateOf(adventure.summary) }
    var difficulty by remember(adventure.id) { mutableStateOf(adventure.difficulty.orEmpty()) }
    var isPublic by remember(adventure.id) { mutableStateOf(adventure.isPublic) }
    var startLatitude by remember(adventure.id) { mutableStateOf(adventure.startPoint.latitude.toString()) }
    var startLongitude by remember(adventure.id) { mutableStateOf(adventure.startPoint.longitude.toString()) }

    var locationName by remember(adventure.id) { mutableStateOf("") }
    var locationLatitude by remember(adventure.id) { mutableStateOf("") }
    var locationLongitude by remember(adventure.id) { mutableStateOf("") }
    var orderedLocations by remember(adventure.id) { mutableStateOf<List<EditLocationItem>>(emptyList()) }
    var removedExistingLocations by remember(adventure.id) { mutableStateOf<List<AdventureLocation>>(emptyList()) }
    var nextNewLocationId by remember(adventure.id) { mutableStateOf(0) }

    var isLoadingLocations by remember(adventure.id) { mutableStateOf(true) }
    var isSubmitting by remember(adventure.id) { mutableStateOf(false) }
    var errorMessage by remember(adventure.id) { mutableStateOf<String?>(null) }
    var mapPickerTarget by remember(adventure.id) { mutableStateOf<MapPickerTarget?>(null) }
    var mapPickedPoint by remember(adventure.id) { mutableStateOf<GeoPoint?>(null) }
    var hintDraftsByKey by remember(adventure.id) { mutableStateOf<Map<String, List<HintDraft>>>(emptyMap()) }
    var expandedHintSections by remember(adventure.id) { mutableStateOf(emptySet<String>()) }
    var quizQuestions by remember(adventure.id) { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoadingQuizQuestions by remember(adventure.id) { mutableStateOf(true) }
    var isSavingQuizQuestion by remember(adventure.id) { mutableStateOf(false) }
    var quizEditorState by remember(adventure.id) { mutableStateOf<QuizEditorState?>(null) }

    LaunchedEffect(adventure.id) {
        isLoadingLocations = true
        isLoadingQuizQuestions = true
        errorMessage = null
        removedExistingLocations = emptyList()
        nextNewLocationId = 0
        runCatching {
            getAdventureLocations(adventure.id)
        }.onSuccess { locations ->
            val sorted = locations.sortedBy { it.orderIndex }
            orderedLocations = sorted.map { location -> EditLocationItem.Existing(location) }
            hintDraftsByKey = sorted.associate { loc ->
                "existing-${loc.orderIndex}" to loc.hints.map { HintDraft(it.hintIndex, it.text, it.pointCost, it.imageUrl) }
            }
        }.onFailure {
            scope.launch {
                snackbarHostState.showSnackbar("Orte konnten nicht geladen werden.", withDismissAction = true)
            }
        }
        isLoadingLocations = false
        runCatching {
            getQuizQuestions(adventure.id)
        }.onSuccess { questions ->
            quizQuestions = questions
        }.onFailure {
            scope.launch {
                snackbarHostState.showSnackbar("Quiz-Fragen konnten nicht geladen werden.", withDismissAction = true)
            }
        }
        isLoadingQuizQuestions = false
    }

    fun addNewLocation() {
        val parsedLatitude = locationLatitude.toDoubleOrNull()
        val parsedLongitude = locationLongitude.toDoubleOrNull()

        when {
            locationName.isBlank() -> errorMessage = "Bitte gib einen Namen fuer den Ort ein."
            parsedLatitude == null || parsedLongitude == null -> {
                errorMessage = "Bitte gib gueltige Koordinaten fuer den Ort ein."
            }

            !isValidLatitude(parsedLatitude) || !isValidLongitude(parsedLongitude) -> {
                errorMessage = "Koordinaten muessen in gueltigen Bereichen liegen."
            }

            else -> {
                errorMessage = null
                val newItem = EditLocationItem.New(
                    localId = nextNewLocationId,
                    draft = AdventureLocationDraft(
                        name = locationName.trim(),
                        point = GeoPoint(latitude = parsedLatitude, longitude = parsedLongitude),
                    ),
                )
                orderedLocations = orderedLocations + newItem
                hintDraftsByKey = hintDraftsByKey + (newItem.key to emptyList())
                nextNewLocationId += 1
                locationName = ""
                locationLatitude = ""
                locationLongitude = ""
            }
        }
    }

    fun submitAdventureUpdate() {
        val parsedStartLatitude = startLatitude.toDoubleOrNull()
        val parsedStartLongitude = startLongitude.toDoubleOrNull()

        when {
            title.isBlank() -> errorMessage = "Bitte gib einen Titel ein."
            summary.isBlank() -> errorMessage = "Bitte gib eine Kurzbeschreibung ein."
            parsedStartLatitude == null || parsedStartLongitude == null -> {
                errorMessage = "Bitte gib gueltige Startkoordinaten ein."
            }

            !isValidLatitude(parsedStartLatitude) || !isValidLongitude(parsedStartLongitude) -> {
                errorMessage = "Startkoordinaten muessen in gueltigen Bereichen liegen."
            }

            orderedLocations.isEmpty() -> {
                errorMessage = "Bitte fuege mindestens einen Ort hinzu."
            }

            else -> {
                errorMessage = null
                isSubmitting = true
                scope.launch {
                    runCatching {
                        val startPoint = GeoPoint(
                            latitude = parsedStartLatitude,
                            longitude = parsedStartLongitude,
                        )
                        val finalLocationDrafts = orderedLocations.map { locationItem ->
                            when (locationItem) {
                                is EditLocationItem.Existing -> AdventureLocationDraft(
                                    name = locationItem.location.name,
                                    point = locationItem.location.point,
                                )

                                is EditLocationItem.New -> locationItem.draft
                            }
                        }
                        val calculatedDurationMinutes = calculateAdventureDurationMinutes(
                            startPoint = startPoint,
                            locations = finalLocationDrafts,
                        )
                        val removedOrderIndexes = removedExistingLocations.map { it.orderIndex }
                        val newLocationItems = orderedLocations.mapNotNull { locationItem ->
                            locationItem as? EditLocationItem.New
                        }
                        val newLocationDrafts = newLocationItems.map { it.draft }
                        val appendedLocationsByLocalId = mutableMapOf<Int, AdventureLocation>()

                        updateAdventure(
                            adventureId = adventure.id,
                            draft = AdventureMetadataDraft(
                                title = title.trim(),
                                summary = summary.trim(),
                                startPoint = startPoint,
                                difficulty = difficulty.takeIf { it.isNotBlank() }?.trim(),
                                estimatedDurationMinutes = calculatedDurationMinutes,
                                isPublic = isPublic,
                            ),
                        )

                        if (removedOrderIndexes.isNotEmpty()) {
                            removedOrderIndexes.sorted().forEach { orderIndex ->
                                deleteAdventureLocation(adventure.id, orderIndex)
                            }
                        }

                        if (newLocationDrafts.isNotEmpty()) {
                            val appendedLocations = appendAdventureLocations(adventure.id, newLocationDrafts)
                            newLocationItems.zip(appendedLocations).forEach { (newItem, appendedLocation) ->
                                appendedLocationsByLocalId[newItem.localId] = appendedLocation
                            }
                        }

                        val finalOrderIndexes = orderedLocations.map { locationItem ->
                            when (locationItem) {
                                is EditLocationItem.Existing -> locationItem.location.orderIndex
                                is EditLocationItem.New -> appendedLocationsByLocalId[locationItem.localId]?.orderIndex
                                    ?: throw IllegalStateException("Missing order index for new location.")
                            }
                        }

                        if (finalOrderIndexes.isNotEmpty()) {
                            reorderAdventureLocations(adventure.id, finalOrderIndexes)
                        }

                        // Save hints for all locations
                        for (locationItem in orderedLocations) {
                            val hints = hintDraftsByKey[locationItem.key] ?: emptyList()
                            val locationId = when (locationItem) {
                                is EditLocationItem.Existing -> locationItem.location.id
                                is EditLocationItem.New -> appendedLocationsByLocalId[locationItem.localId]?.id ?: -1L
                            }
                            if (locationId > 0L) {
                                saveHintsForLocation(locationId, hints)
                            }
                        }
                    }.onSuccess {
                        onAdventureUpdated()
                        onDismiss()
                    }.onFailure { throwable ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                throwable.message ?: "Abenteuer konnte nicht aktualisiert werden.",
                                withDismissAction = true,
                            )
                        }
                    }
                    isSubmitting = false
                }
            }
        }
    }

    if (mapPickerTarget != null) {
        MapPointPickerScreen(
            modifier = modifier,
            title = when (mapPickerTarget) {
                MapPickerTarget.StartPoint -> "Startpunkt auf Karte waehlen"
                MapPickerTarget.AdventureLocation -> "Ort auf Karte waehlen"
                null -> "Ort auf Karte waehlen"
            },
            selectedPoint = mapPickedPoint,
            onCancel = {
                mapPickerTarget = null
                mapPickedPoint = null
            },
            onPointSelected = { selected ->
                mapPickedPoint = selected
            },
            onConfirm = { selected ->
                when (mapPickerTarget) {
                    MapPickerTarget.StartPoint -> {
                        startLatitude = selected.latitude.toString()
                        startLongitude = selected.longitude.toString()
                    }

                    MapPickerTarget.AdventureLocation -> {
                        if (locationName.isBlank()) {
                            locationName = "Ort ${orderedLocations.size + 1}"
                        }
                        locationLatitude = selected.latitude.toString()
                        locationLongitude = selected.longitude.toString()
                    }

                    null -> Unit
                }
                errorMessage = null
                mapPickerTarget = null
                mapPickedPoint = null
            },
        )
        return
    }

    if (quizEditorState != null) {
        QuizQuestionEditorScreen(
            modifier = modifier,
            initialDraft = (quizEditorState as? QuizEditorState.Edit)?.draft,
            onDismiss = { quizEditorState = null },
            onConfirm = { draft ->
                val state = quizEditorState ?: return@QuizQuestionEditorScreen
                scope.launch {
                    isSavingQuizQuestion = true
                    runCatching {
                        when (state) {
                            QuizEditorState.New -> {
                                val nextOrderIndex = (quizQuestions.maxOfOrNull { it.orderIndex } ?: -1) + 1
                                val newQuestion = createQuizQuestion(
                                    adventureId = adventure.id,
                                    orderIndex = nextOrderIndex,
                                    draft = draft,
                                )
                                quizQuestions = quizQuestions + newQuestion
                            }
                            is QuizEditorState.Edit -> {
                                val existing = quizQuestions[state.index]
                                deleteQuizQuestion(existing.id)
                                val updated = createQuizQuestion(
                                    adventureId = adventure.id,
                                    orderIndex = existing.orderIndex,
                                    draft = draft,
                                )
                                quizQuestions = quizQuestions.mapIndexed { i, q ->
                                    if (i == state.index) updated else q
                                }
                            }
                        }
                    }.onFailure { e ->
                        snackbarHostState.showSnackbar(
                            "Frage konnte nicht gespeichert werden: ${e.message}",
                            withDismissAction = true,
                        )
                    }
                    isSavingQuizQuestion = false
                    quizEditorState = null
                }
            },
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Abenteuer bearbeiten",
                style = MaterialTheme.typography.headlineSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                    Text("Abbrechen")
                }
                Button(onClick = { submitAdventureUpdate() }, enabled = !isSubmitting) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Speichern")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titel") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = summary,
                    onValueChange = {
                        summary = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kurzbeschreibung") },
                )
            }
            item {
                DifficultyDropdown(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Öffentlich", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = if (isPublic) "Sichtbar für alle Nutzer" else "Nur für dich sichtbar (Entwurf)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                    )
                }
            }
            item {
                Text(
                    text = "Dauer wird automatisch aus Route und Orten berechnet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Text(
                    text = "Startpunkt",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                Button(
                    onClick = {
                        val location = currentLocation ?: return@Button
                        startLatitude = location.latitude.toString()
                        startLongitude = location.longitude.toString()
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentLocation != null,
                ) {
                    Text("Aktuellen Standort als Startpunkt nutzen")
                }
            }
            item {
                Button(
                    onClick = {
                        mapPickedPoint = parseGeoPoint(startLatitude, startLongitude)
                        mapPickerTarget = MapPickerTarget.StartPoint
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                ) {
                    Text("Startpunkt auf Karte waehlen")
                }
            }
            item {
                OutlinedTextField(
                    value = startLatitude,
                    onValueChange = { startLatitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Start Latitude") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = startLongitude,
                    onValueChange = { startLongitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Start Longitude") },
                    singleLine = true,
                )
            }
            item {
                Text(
                    text = "Orte in Reihenfolge",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            if (isLoadingLocations) {
                item {
                    Text("Orte werden geladen...", style = MaterialTheme.typography.bodySmall)
                }
            } else if (orderedLocations.isEmpty()) {
                item {
                    Text("Noch keine Orte gespeichert.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                itemsIndexed(
                    orderedLocations,
                    key = { _, locationItem -> locationItem.key }) { index, locationItem ->
                    val locationNameText = when (locationItem) {
                        is EditLocationItem.Existing -> locationItem.location.name
                        is EditLocationItem.New -> locationItem.draft.name
                    }
                    val key = locationItem.key
                    val hintsExpanded = key in expandedHintSections
                    val currentHints = hintDraftsByKey[key] ?: emptyList()
                    var uploadingHintIndex by remember { mutableStateOf<Int?>(null) }
                    val pickImage0 = rememberImagePicker { bytes ->
                        if (bytes != null) scope.launch {
                            uploadingHintIndex = 0
                            runCatching { uploadHintImage(bytes) }
                                .onSuccess { url -> hintDraftsByKey = hintDraftsByKey + (key to (hintDraftsByKey[key] ?: emptyList()).withUpdatedHintImage(0, url)) }
                                .onFailure { snackbarHostState.showSnackbar("Bild konnte nicht hochgeladen werden: ${it.message}", withDismissAction = true) }
                            uploadingHintIndex = null
                        }
                    }
                    val pickImage1 = rememberImagePicker { bytes ->
                        if (bytes != null) scope.launch {
                            uploadingHintIndex = 1
                            runCatching { uploadHintImage(bytes) }
                                .onSuccess { url -> hintDraftsByKey = hintDraftsByKey + (key to (hintDraftsByKey[key] ?: emptyList()).withUpdatedHintImage(1, url)) }
                                .onFailure { snackbarHostState.showSnackbar("Bild konnte nicht hochgeladen werden: ${it.message}", withDismissAction = true) }
                            uploadingHintIndex = null
                        }
                    }
                    val pickImage2 = rememberImagePicker { bytes ->
                        if (bytes != null) scope.launch {
                            uploadingHintIndex = 2
                            runCatching { uploadHintImage(bytes) }
                                .onSuccess { url -> hintDraftsByKey = hintDraftsByKey + (key to (hintDraftsByKey[key] ?: emptyList()).withUpdatedHintImage(2, url)) }
                                .onFailure { snackbarHostState.showSnackbar("Bild konnte nicht hochgeladen werden: ${it.message}", withDismissAction = true) }
                            uploadingHintIndex = null
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${index + 1}. $locationNameText",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    expandedHintSections = if (hintsExpanded)
                                        expandedHintSections - key
                                    else
                                        expandedHintSections + key
                                }) {
                                    Text(if (hintsExpanded) "Hints ▲" else "Hints ▼")
                                }
                                TextButton(
                                    onClick = {
                                        if (index > 0) orderedLocations = orderedLocations.move(index, index - 1)
                                    },
                                    enabled = !isSubmitting && index > 0,
                                ) { Text("Hoch") }
                                TextButton(
                                    onClick = {
                                        if (index < orderedLocations.lastIndex) orderedLocations = orderedLocations.move(index, index + 1)
                                    },
                                    enabled = !isSubmitting && index < orderedLocations.lastIndex,
                                ) { Text("Runter") }
                                TextButton(
                                    onClick = {
                                        orderedLocations = orderedLocations - locationItem
                                        hintDraftsByKey = hintDraftsByKey - key
                                        expandedHintSections = expandedHintSections - key
                                        if (locationItem is EditLocationItem.Existing) {
                                            removedExistingLocations =
                                                (removedExistingLocations + locationItem.location)
                                                    .sortedBy { it.orderIndex }
                                        }
                                    },
                                    enabled = !isSubmitting,
                                ) { Text("Entfernen") }
                            }
                        }
                        if (hintsExpanded) {
                            Column(
                                modifier = Modifier.padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("Hints", style = MaterialTheme.typography.labelMedium)
                                OutlinedTextField(
                                    value = currentHints.textFor(0),
                                    onValueChange = { text ->
                                        hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHint(0, text))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Hint 0 (gratis, beim Ankommen gezeigt)") },
                                )
                                HintImagePickerRow(
                                    imageUrl = currentHints.imageUrlFor(0),
                                    isUploading = uploadingHintIndex == 0,
                                    onPickImage = pickImage0,
                                    onRemoveImage = {
                                        hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHintImage(0, null))
                                    },
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentHints.textFor(1),
                                        onValueChange = { text ->
                                            hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHint(1, text, currentHints.costFor(1)))
                                        },
                                        modifier = Modifier.weight(2f),
                                        label = { Text("Hint 1 (kostenpflichtig)") },
                                    )
                                    OutlinedTextField(
                                        value = currentHints.costFor(1).takeIf { currentHints.textFor(1).isNotBlank() }?.toString() ?: "",
                                        onValueChange = { costText ->
                                            val cost = costText.toIntOrNull() ?: 0
                                            val text = currentHints.textFor(1)
                                            if (text.isNotBlank()) {
                                                hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHint(1, text, cost))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Pts") },
                                        singleLine = true,
                                        enabled = currentHints.textFor(1).isNotBlank(),
                                    )
                                }
                                HintImagePickerRow(
                                    imageUrl = currentHints.imageUrlFor(1),
                                    isUploading = uploadingHintIndex == 1,
                                    onPickImage = pickImage1,
                                    onRemoveImage = {
                                        hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHintImage(1, null))
                                    },
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentHints.textFor(2),
                                        onValueChange = { text ->
                                            hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHint(2, text, currentHints.costFor(2)))
                                        },
                                        modifier = Modifier.weight(2f),
                                        label = { Text("Hint 2 (kostenpflichtig)") },
                                    )
                                    OutlinedTextField(
                                        value = currentHints.costFor(2).takeIf { currentHints.textFor(2).isNotBlank() }?.toString() ?: "",
                                        onValueChange = { costText ->
                                            val cost = costText.toIntOrNull() ?: 0
                                            val text = currentHints.textFor(2)
                                            if (text.isNotBlank()) {
                                                hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHint(2, text, cost))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Pts") },
                                        singleLine = true,
                                        enabled = currentHints.textFor(2).isNotBlank(),
                                    )
                                }
                                HintImagePickerRow(
                                    imageUrl = currentHints.imageUrlFor(2),
                                    isUploading = uploadingHintIndex == 2,
                                    onPickImage = pickImage2,
                                    onRemoveImage = {
                                        hintDraftsByKey = hintDraftsByKey + (key to currentHints.withUpdatedHintImage(2, null))
                                    },
                                )
                            }
                        }
                    }
                }
            }
            if (removedExistingLocations.isNotEmpty()) {
                item {
                    Text(
                        text = "Zur Entfernung markierte Orte",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                items(removedExistingLocations, key = { it.orderIndex }) { location ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${location.orderIndex + 1}. ${location.name}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(
                            onClick = {
                                removedExistingLocations =
                                    removedExistingLocations.filterNot { it.orderIndex == location.orderIndex }
                                val restoredItem = EditLocationItem.Existing(location)
                                orderedLocations = orderedLocations + restoredItem
                                hintDraftsByKey = hintDraftsByKey + (restoredItem.key to
                                    location.hints.map { HintDraft(it.hintIndex, it.text, it.pointCost, it.imageUrl) })
                            },
                            enabled = !isSubmitting,
                        ) {
                            Text("Wiederherstellen")
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Neue Orte hinzufuegen",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                Button(
                    onClick = {
                        val location = currentLocation ?: return@Button
                        if (locationName.isBlank()) {
                            locationName = "Ort ${orderedLocations.size + 1}"
                        }
                        locationLatitude = location.latitude.toString()
                        locationLongitude = location.longitude.toString()
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentLocation != null,
                ) {
                    Text("Aktuellen Standort fuer Ort nutzen")
                }
            }
            item {
                Button(
                    onClick = {
                        mapPickedPoint = parseGeoPoint(locationLatitude, locationLongitude)
                        mapPickerTarget = MapPickerTarget.AdventureLocation
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                ) {
                    Text("Ort auf Karte waehlen")
                }
            }
            item {
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ort Name") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = locationLatitude,
                    onValueChange = { locationLatitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ort Latitude") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = locationLongitude,
                    onValueChange = { locationLongitude = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ort Longitude") },
                    singleLine = true,
                )
            }
            item {
                Button(
                    onClick = { addNewLocation() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                ) {
                    Text("Ort hinzufuegen")
                }
            }
            item {
                Text(
                    text = "Quiz-Fragen",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                Button(
                    onClick = { quizEditorState = QuizEditorState.New },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting && !isSavingQuizQuestion,
                ) {
                    if (isSavingQuizQuestion) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("+ Frage hinzufuegen")
                    }
                }
            }
            if (isLoadingQuizQuestions) {
                item {
                    Text("Fragen werden geladen...", style = MaterialTheme.typography.bodySmall)
                }
            } else if (quizQuestions.isNotEmpty()) {
                itemsIndexed(quizQuestions, key = { _, q -> "edit-quiz-${q.id}" }) { index, question ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Frage ${index + 1}: ${question.questionText}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            question.answers.sortedBy { it.answerOrder }.forEachIndexed { i, answer ->
                                val label = listOf("A", "B", "C", "D").getOrElse(i) { "${i + 1}" }
                                Text(
                                    text = "${if (answer.isCorrect) "✓ " else ""}$label: ${answer.answerText}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (answer.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        quizEditorState = QuizEditorState.Edit(
                                            index,
                                            QuizQuestionDraft(
                                                questionText = question.questionText,
                                                answers = question.answers.map {
                                                    QuizAnswerDraft(it.answerText, it.isCorrect, it.answerOrder)
                                                },
                                            ),
                                        )
                                    },
                                    enabled = !isSavingQuizQuestion,
                                ) { Text("Bearbeiten") }
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            isSavingQuizQuestion = true
                                            runCatching { deleteQuizQuestion(question.id) }
                                                .onSuccess { quizQuestions = quizQuestions - question }
                                                .onFailure { e -> errorMessage = "Frage konnte nicht geloescht werden: ${e.message}" }
                                            isSavingQuizQuestion = false
                                        }
                                    },
                                    enabled = !isSavingQuizQuestion,
                                ) { Text("Loeschen") }
                            }
                        }
                    }
                }
            }
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPointPickerScreen(
    modifier: Modifier = Modifier,
    title: String,
    selectedPoint: GeoPoint?,
    onCancel: () -> Unit,
    onPointSelected: (GeoPoint) -> Unit,
    onConfirm: (GeoPoint) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onCancel) {
                Text("Abbrechen")
            }
            Button(
                onClick = {
                    selectedPoint?.let(onConfirm)
                },
                enabled = selectedPoint != null,
            ) {
                Text("Uebernehmen")
            }
        }

        Text(
            text = "Tippe auf die Karte, um Koordinaten zu setzen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        PlatformMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp),
            enablePointSelection = true,
            selectedPoint = selectedPoint,
            onMapPointSelected = onPointSelected,
        )
    }
}

@Composable
private fun HintImagePickerRow(
    imageUrl: String?,
    isUploading: Boolean,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text("Bild wird hochgeladen...", style = MaterialTheme.typography.bodySmall)
        } else if (imageUrl != null) {
            Text(
                text = "Bild gesetzt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRemoveImage) { Text("Entfernen") }
        } else {
            TextButton(onClick = onPickImage) { Text("Bild auswählen") }
        }
    }
}

private sealed interface QuizEditorState {
    data object New : QuizEditorState
    data class Edit(val index: Int, val draft: QuizQuestionDraft) : QuizEditorState
}

private fun List<HintDraft>.textFor(hintIndex: Int): String =
    find { it.hintIndex == hintIndex }?.text ?: ""

private fun List<HintDraft>.costFor(hintIndex: Int): Int =
    find { it.hintIndex == hintIndex }?.pointCost ?: 0

private fun List<HintDraft>.imageUrlFor(hintIndex: Int): String? =
    find { it.hintIndex == hintIndex }?.imageUrl

private fun List<HintDraft>.withUpdatedHint(hintIndex: Int, text: String, pointCost: Int = 0): List<HintDraft> {
    val existing = toMutableList()
    val pos = existing.indexOfFirst { it.hintIndex == hintIndex }
    val currentImageUrl = existing.getOrNull(pos)?.imageUrl
    if (text.isBlank() && currentImageUrl == null) {
        if (pos >= 0) existing.removeAt(pos)
    } else {
        val updated = HintDraft(hintIndex, text, pointCost, currentImageUrl)
        if (pos >= 0) existing[pos] = updated else existing.add(updated)
    }
    return existing.sortedBy { it.hintIndex }
}

private fun List<HintDraft>.withUpdatedHintImage(hintIndex: Int, imageUrl: String?): List<HintDraft> {
    val existing = toMutableList()
    val pos = existing.indexOfFirst { it.hintIndex == hintIndex }
    val current = existing.getOrNull(pos) ?: HintDraft(hintIndex, "")
    if (current.text.isBlank() && imageUrl == null) {
        if (pos >= 0) existing.removeAt(pos)
        return existing.sortedBy { it.hintIndex }
    }
    val updated = current.copy(imageUrl = imageUrl)
    if (pos >= 0) existing[pos] = updated else existing.add(updated)
    return existing.sortedBy { it.hintIndex }
}

private fun List<AdventureLocationDraft>.withUpdatedLocationHint(
    locationIndex: Int,
    hintIndex: Int,
    text: String,
    pointCost: Int = 0,
): List<AdventureLocationDraft> = mapIndexed { i, loc ->
    if (i != locationIndex) loc
    else loc.copy(hints = loc.hints.withUpdatedHint(hintIndex, text, pointCost))
}

private fun List<AdventureLocationDraft>.withUpdatedLocationHintImage(
    locationIndex: Int,
    hintIndex: Int,
    imageUrl: String?,
): List<AdventureLocationDraft> = mapIndexed { i, loc ->
    if (i != locationIndex) loc
    else loc.copy(hints = loc.hints.withUpdatedHintImage(hintIndex, imageUrl))
}

private sealed interface EditLocationItem {
    val key: String

    data class Existing(val location: AdventureLocation) : EditLocationItem {
        override val key: String = "existing-${location.orderIndex}"
    }

    data class New(val localId: Int, val draft: AdventureLocationDraft) : EditLocationItem {
        override val key: String = "new-$localId"
    }
}

private fun <T> List<T>.move(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) {
        return this
    }

    val mutable = toMutableList()
    val movedItem = mutable.removeAt(fromIndex)
    mutable.add(toIndex, movedItem)
    return mutable
}

@Composable
private fun QuizQuestionEditorScreen(
    modifier: Modifier = Modifier,
    initialDraft: QuizQuestionDraft?,
    onDismiss: () -> Unit,
    onConfirm: (QuizQuestionDraft) -> Unit,
) {
    var questionText by remember { mutableStateOf(initialDraft?.questionText ?: "") }
    var answers by remember {
        mutableStateOf(
            initialDraft?.answers?.let { drafts ->
                List(4) { i -> drafts.find { it.answerOrder == i } ?: QuizAnswerDraft("", false, i) }
            } ?: List(4) { i -> QuizAnswerDraft("", false, i) },
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        return when {
            questionText.isBlank() -> { errorMessage = "Bitte gib eine Frage ein."; false }
            answers.none { it.isCorrect } -> { errorMessage = "Bitte markiere eine Antwort als richtig."; false }
            answers.filter { it.answerText.isNotBlank() }.size < 2 -> {
                errorMessage = "Bitte gib mindestens 2 Antworten ein."
                false
            }
            else -> { errorMessage = null; true }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (initialDraft == null) "Neue Quiz-Frage" else "Frage bearbeiten",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
            Button(onClick = {
                if (validate()) {
                    onConfirm(
                        QuizQuestionDraft(
                            questionText = questionText.trim(),
                            answers = answers
                                .filter { it.answerText.isNotBlank() }
                                .mapIndexed { i, a -> a.copy(answerOrder = i) },
                        ),
                    )
                }
            }) { Text("Speichern") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Frage") },
                    minLines = 2,
                )
            }
            item {
                Text(
                    text = "Antworten (eine als richtig markieren)",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            itemsIndexed(answers, key = { i, _ -> i }) { index, answer ->
                val label = listOf("A", "B", "C", "D")[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RadioButton(
                        selected = answer.isCorrect,
                        onClick = {
                            answers = answers.mapIndexed { i, a -> a.copy(isCorrect = i == index) }
                        },
                    )
                    OutlinedTextField(
                        value = answer.answerText,
                        onValueChange = { newText ->
                            answers = answers.mapIndexed { i, a ->
                                if (i == index) a.copy(answerText = newText) else a
                            }
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Antwort $label") },
                        singleLine = true,
                    )
                }
            }
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun isValidLatitude(value: Double): Boolean = value in -90.0..90.0

private fun isValidLongitude(value: Double): Boolean = value in -180.0..180.0

private fun parseGeoPoint(latitudeText: String, longitudeText: String): GeoPoint? {
    val latitude = latitudeText.toDoubleOrNull() ?: return null
    val longitude = longitudeText.toDoubleOrNull() ?: return null

    if (!isValidLatitude(latitude) || !isValidLongitude(longitude)) {
        return null
    }

    return GeoPoint(latitude = latitude, longitude = longitude)
}

private suspend fun calculateAdventureDurationMinutes(
    startPoint: GeoPoint,
    locations: List<AdventureLocationDraft>,
): Int {
    val routePoints = listOf(startPoint) + locations.map { it.point }
    val distanceMeters = getWalkingRouteDistanceMeters(routePoints)
        ?: routePoints.zipWithNext { current, next -> current.distanceTo(next) }.sum()
    val walkingMinutes = (distanceMeters / 1_000.0) / 4.0 * 60.0
    val locationMinutes = locations.size * 5

    return ceil(walkingMinutes + locationMinutes).toInt().coerceAtLeast(locationMinutes)
}

private fun canStartAdventure(startPoint: GeoPoint, currentLocation: GeoPoint?): Boolean {
    if (currentLocation == null) {
        return false
    }

    return startPoint.distanceTo(currentLocation) <= START_ADVENTURE_MAX_DISTANCE_METERS
}

private fun startDialogDistanceLabel(startPoint: GeoPoint, currentLocation: GeoPoint?): String {
    if (currentLocation == null) {
        return "Standort nicht verfuegbar. Bitte aktiviere GPS."
    }

    val distanceMeters = startPoint.distanceTo(currentLocation).roundToInt()
    return "Du bist $distanceMeters m vom Startpunkt entfernt."
}

@Composable
private fun ProfileAndLeaderboardTab(
    currentUser: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var totalPoints by remember { mutableStateOf<Int?>(null) }
    var leaderboard by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isLoadingProfile by remember { mutableStateOf(false) }
    var isLoadingLeaderboard by remember { mutableStateOf(false) }
    var leaderboardError by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf(LeaderboardPeriod.ALL) }

    LaunchedEffect(Unit) {
        isLoadingProfile = true
        totalPoints = runCatching { getUserPoints(currentUser.id) }.getOrDefault(0)
        isLoadingProfile = false
    }

    LaunchedEffect(selectedPeriod) {
        isLoadingLeaderboard = true
        leaderboardError = null
        val result = runCatching { getLeaderboard(selectedPeriod, 50) }
        leaderboard = result.getOrDefault(emptyList())
        leaderboardError = result.exceptionOrNull()?.message
        isLoadingLeaderboard = false
    }

    val currentUserDisplayName = currentUser.username ?: currentUser.email

    val periodLabels = listOf("Gesamt", "Diese Woche", "Diesen Monat")
    val periods = LeaderboardPeriod.entries

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Profile card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Profil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = if (isLoadingProfile) "Punkte werden geladen..."
                               else totalPoints?.let { "Punkte: $it" } ?: "–",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Abmelden")
                    }
                }
            }
        }

        // Leaderboard header + period tabs
        item {
            Text(
                text = "Rangliste",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
            androidx.compose.material3.PrimaryTabRow(
                selectedTabIndex = periods.indexOf(selectedPeriod),
            ) {
                periods.forEachIndexed { index, period ->
                    androidx.compose.material3.Tab(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        text = { Text(periodLabels[index], style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }
        }

        // Loading state
        if (isLoadingLeaderboard) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (leaderboardError != null) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Fehler: $leaderboardError",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        } else if (leaderboard.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Noch keine Einträge für diesen Zeitraum.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            itemsIndexed(leaderboard) { index, (displayName, points) ->
                val isCurrentUser = displayName == currentUserDisplayName
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isCurrentUser) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    } else {
                        CardDefaults.cardColors()
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${index + 1}. $displayName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                        )
                        Text(
                            text = "$points Punkte",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Keine Internetverbindung",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
