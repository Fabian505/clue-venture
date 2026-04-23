package de.clueventure.clue_venture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
	var activeAdventureForOverlay by remember { mutableStateOf<Adventure?>(null) }
	var isAdventureOverlayExpanded by remember { mutableStateOf(true) }
	var showEndAdventureConfirmation by remember { mutableStateOf(false) }
	var adventureRefreshKey by remember { mutableStateOf(0) }
	val snackbarHostState = remember { SnackbarHostState() }

	MaterialTheme {
		Scaffold(
			contentWindowInsets = WindowInsets(0, 0, 0, 0),
			snackbarHost = {
				SnackbarHost(hostState = snackbarHostState)
			},
			bottomBar = {
				if (editingAdventure == null && !showCreateAdventureDialog) {
					NavigationBar(
						modifier = Modifier.height(64.dp),
					) {
						BottomTab.entries.forEach { tab ->
							NavigationBarItem(
								selected = selectedTab == tab,
								onClick = { selectedTab = tab },
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
			editingAdventure?.let { adventure ->
				EditAdventureScreen(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					adventure = adventure,
					currentLocation = currentLocation,
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
					refreshKey = adventureRefreshKey,
					onNavigateToStart = { adventure ->
						routedAdventureId = adventure.id
						routeTargets = listOf(adventure.startPoint)
						activeAdventureForOverlay = null
						showEndAdventureConfirmation = false
						selectedTab = BottomTab.Map
						appScope.launch {
							val locations = getAdventureLocations(adventure.id)
							routeTargets = listOf(adventure.startPoint) + locations.map { it.point }
						}
					},
					onStartAdventure = { adventure ->
						if (canStartAdventure(adventure.startPoint, currentLocation)) {
							adventurePendingStart = adventure
							startErrorMessage = null
						}
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
					)

					activeAdventureForOverlay?.let { adventure ->
						ActiveAdventureOverlay(
							adventure = adventure,
							isExpanded = isAdventureOverlayExpanded,
							onToggleExpanded = {
								isAdventureOverlayExpanded = !isAdventureOverlayExpanded
							},
							onEndAdventure = {
								showEndAdventureConfirmation = true
							},
							modifier = Modifier
								.align(Alignment.TopCenter)
								.fillMaxWidth()
								.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
								.padding(horizontal = 12.dp, vertical = 8.dp),
						)
					}
				}

				BottomTab.Right -> Box(modifier = Modifier.fillMaxSize())
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
									if (routedAdventureId == adventure.id) {
										routeTargets = emptyList()
										routedAdventureId = null
										activeAdventureForOverlay = null
										showEndAdventureConfirmation = false
									}
									if (editingAdventure?.id == adventure.id) {
										editingAdventure = null
									}
									adventurePendingDeletion = null
									adventureRefreshKey += 1
								}.onFailure { throwable ->
									deleteErrorMessage = throwable.message ?: "Abenteuer konnte nicht geloescht werden."
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
									routedAdventureId = adventure.id
									routeTargets = listOf(adventure.startPoint) + locations.map { it.point }
									activeAdventureForOverlay = adventure
									isAdventureOverlayExpanded = true
									selectedTab = BottomTab.Map
								}.onSuccess {
									adventurePendingStart = null
									snackbarHostState.showSnackbar("Abenteuer gestartet: ${adventure.title}")
								}.onFailure { throwable ->
									startErrorMessage = throwable.message ?: "Abenteuer konnte nicht gestartet werden."
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

		if (showEndAdventureConfirmation) {
			AlertDialog(
				onDismissRequest = { showEndAdventureConfirmation = false },
				title = { Text("Abenteuer beenden") },
				text = {
					Text("Willst du das laufende Abenteuer wirklich beenden?")
				},
				confirmButton = {
					Button(
						onClick = {
							routeTargets = emptyList()
							routedAdventureId = null
							activeAdventureForOverlay = null
							isAdventureOverlayExpanded = true
							showEndAdventureConfirmation = false
							appScope.launch {
								snackbarHostState.showSnackbar("Abenteuer beendet")
							}
						},
					) {
						Text("Beenden")
					}
				},
				dismissButton = {
					TextButton(onClick = { showEndAdventureConfirmation = false }) {
						Text("Abbrechen")
					}
				},
			)
		}
	}
}

@Composable
private fun CreateAdventureScreen(
	modifier: Modifier = Modifier,
	currentLocation: GeoPoint?,
	onDismiss: () -> Unit,
	onAdventureCreated: (Adventure) -> Unit,
) {
	val scope = rememberCoroutineScope()

	var title by remember { mutableStateOf("") }
	var summary by remember { mutableStateOf("") }
	var difficulty by remember { mutableStateOf("") }
	var durationMinutes by remember { mutableStateOf("") }
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
		val parsedDuration = durationMinutes.toIntOrNull()

		when {
			title.isBlank() -> errorMessage = "Bitte gib einen Titel ein."
			summary.isBlank() -> errorMessage = "Bitte gib eine Kurzbeschreibung ein."
			parsedStartLatitude == null || parsedStartLongitude == null -> {
				errorMessage = "Bitte gib gueltige Startkoordinaten ein."
			}
			!isValidLatitude(parsedStartLatitude) || !isValidLongitude(parsedStartLongitude) -> {
				errorMessage = "Startkoordinaten muessen in gueltigen Bereichen liegen."
			}
			parsedDuration != null && parsedDuration <= 0 -> {
				errorMessage = "Die Dauer muss groesser als 0 sein."
			}
			locations.isEmpty() -> {
				errorMessage = "Bitte fuege mindestens einen Ort hinzu."
			}
			else -> {
				errorMessage = null
				isSubmitting = true
				scope.launch {
					runCatching {
						createAdventure(
							AdventureDraft(
								title = title.trim(),
								summary = summary.trim(),
								startPoint = GeoPoint(
									latitude = parsedStartLatitude,
									longitude = parsedStartLongitude,
								),
								difficulty = difficulty.takeIf { it.isNotBlank() }?.trim(),
								estimatedDurationMinutes = parsedDuration,
								locations = locations,
							),
						)
					}.onSuccess { createdAdventure ->
						onAdventureCreated(createdAdventure)
					}.onFailure { throwable ->
						errorMessage = throwable.message ?: "Abenteuer konnte nicht gespeichert werden."
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
					OutlinedTextField(
						value = difficulty,
						onValueChange = { difficulty = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Schwierigkeit (optional)") },
						singleLine = true,
					)
				}
				item {
					OutlinedTextField(
						value = durationMinutes,
						onValueChange = { durationMinutes = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Dauer in Minuten (optional)") },
						singleLine = true,
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
					itemsIndexed(items = locations, key = { _, location -> location.name + location.point.latitude + location.point.longitude }) { index, location ->
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically,
						) {
							Text(
								text = "${index + 1}. ${location.name}",
								style = MaterialTheme.typography.bodyMedium,
							)
							Row(verticalAlignment = Alignment.CenterVertically) {
								TextButton(
									onClick = {
										if (index > 0) {
											locations = locations.move(index, index - 1)
										}
									},
									enabled = !isSubmitting && index > 0,
								) {
									Text("Hoch")
								}
								TextButton(
									onClick = {
										if (index < locations.lastIndex) {
											locations = locations.move(index, index + 1)
										}
									},
									enabled = !isSubmitting && index < locations.lastIndex,
								) {
									Text("Runter")
								}
								TextButton(
									onClick = { locations = locations - location },
									enabled = !isSubmitting,
								) {
									Text("Entfernen")
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
	onDismiss: () -> Unit,
	onAdventureUpdated: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	var title by remember(adventure.id) { mutableStateOf(adventure.title) }
	var summary by remember(adventure.id) { mutableStateOf(adventure.summary) }
	var difficulty by remember(adventure.id) { mutableStateOf(adventure.difficulty.orEmpty()) }
	var durationMinutes by remember(adventure.id) { mutableStateOf(adventure.estimatedDurationMinutes?.toString().orEmpty()) }
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

	LaunchedEffect(adventure.id) {
		isLoadingLocations = true
		errorMessage = null
		removedExistingLocations = emptyList()
		nextNewLocationId = 0
		runCatching {
			getAdventureLocations(adventure.id)
		}.onSuccess { locations ->
			orderedLocations = locations
				.sortedBy { it.orderIndex }
				.map { location -> EditLocationItem.Existing(location) }
		}.onFailure {
			errorMessage = "Orte konnten nicht geladen werden."
		}
		isLoadingLocations = false
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
				orderedLocations = orderedLocations + EditLocationItem.New(
					localId = nextNewLocationId,
					draft = AdventureLocationDraft(
						name = locationName.trim(),
						point = GeoPoint(latitude = parsedLatitude, longitude = parsedLongitude),
					),
				)
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
		val parsedDuration = durationMinutes.toIntOrNull()

		when {
			title.isBlank() -> errorMessage = "Bitte gib einen Titel ein."
			summary.isBlank() -> errorMessage = "Bitte gib eine Kurzbeschreibung ein."
			parsedStartLatitude == null || parsedStartLongitude == null -> {
				errorMessage = "Bitte gib gueltige Startkoordinaten ein."
			}
			!isValidLatitude(parsedStartLatitude) || !isValidLongitude(parsedStartLongitude) -> {
				errorMessage = "Startkoordinaten muessen in gueltigen Bereichen liegen."
			}
			parsedDuration != null && parsedDuration <= 0 -> {
				errorMessage = "Die Dauer muss groesser als 0 sein."
			}
			else -> {
				errorMessage = null
				isSubmitting = true
				scope.launch {
					runCatching {
						val removedOrderIndexes = removedExistingLocations.map { it.orderIndex }
						val newLocationItems = orderedLocations.mapNotNull { locationItem ->
							locationItem as? EditLocationItem.New
						}
						val newLocationDrafts = newLocationItems.map { it.draft }
						val appendedOrderIndexesByLocalId = mutableMapOf<Int, Int>()

						updateAdventure(
							adventureId = adventure.id,
							draft = AdventureMetadataDraft(
								title = title.trim(),
								summary = summary.trim(),
								startPoint = GeoPoint(
									latitude = parsedStartLatitude,
									longitude = parsedStartLongitude,
								),
								difficulty = difficulty.takeIf { it.isNotBlank() }?.trim(),
								estimatedDurationMinutes = parsedDuration,
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
								appendedOrderIndexesByLocalId[newItem.localId] = appendedLocation.orderIndex
							}
						}

						val finalOrderIndexes = orderedLocations.map { locationItem ->
							when (locationItem) {
								is EditLocationItem.Existing -> locationItem.location.orderIndex
								is EditLocationItem.New -> appendedOrderIndexesByLocalId[locationItem.localId]
									?: throw IllegalStateException("Missing order index for new location.")
							}
						}

						if (finalOrderIndexes.isNotEmpty()) {
							reorderAdventureLocations(adventure.id, finalOrderIndexes)
						}
					}.onSuccess {
						onAdventureUpdated()
						onDismiss()
					}.onFailure { throwable ->
						errorMessage = throwable.message ?: "Abenteuer konnte nicht aktualisiert werden."
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
					OutlinedTextField(
						value = difficulty,
						onValueChange = { difficulty = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Schwierigkeit (optional)") },
						singleLine = true,
					)
				}
				item {
					OutlinedTextField(
						value = durationMinutes,
						onValueChange = { durationMinutes = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Dauer in Minuten (optional)") },
						singleLine = true,
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
					itemsIndexed(orderedLocations, key = { _, locationItem -> locationItem.key }) { index, locationItem ->
						val locationNameText = when (locationItem) {
							is EditLocationItem.Existing -> locationItem.location.name
							is EditLocationItem.New -> locationItem.draft.name
						}
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically,
						) {
							Text(
								text = "${index + 1}. $locationNameText",
								style = MaterialTheme.typography.bodyMedium,
							)
							Row(verticalAlignment = Alignment.CenterVertically) {
								TextButton(
									onClick = {
										if (index > 0) {
											orderedLocations = orderedLocations.move(index, index - 1)
										}
									},
									enabled = !isSubmitting && index > 0,
								) {
									Text("Hoch")
								}
								TextButton(
									onClick = {
										if (index < orderedLocations.lastIndex) {
											orderedLocations = orderedLocations.move(index, index + 1)
										}
									},
									enabled = !isSubmitting && index < orderedLocations.lastIndex,
								) {
									Text("Runter")
								}
								TextButton(
									onClick = {
										orderedLocations = orderedLocations - locationItem
										if (locationItem is EditLocationItem.Existing) {
											removedExistingLocations = (removedExistingLocations + locationItem.location)
												.sortedBy { it.orderIndex }
										}
									},
									enabled = !isSubmitting,
								) {
									Text("Entfernen")
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
									removedExistingLocations = removedExistingLocations.filterNot { it.orderIndex == location.orderIndex }
									orderedLocations = orderedLocations + EditLocationItem.Existing(location)
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
private fun ActiveAdventureOverlay(
	adventure: Adventure,
	isExpanded: Boolean,
	onToggleExpanded: () -> Unit,
	onEndAdventure: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Card(
		modifier = modifier,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
		),
	) {
		Column(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = adventure.title,
						style = MaterialTheme.typography.titleMedium,
					)
					Text(
						text = buildMetadataLabel(adventure).ifBlank { "Schwierigkeit und Dauer nicht gesetzt" },
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
				TextButton(onClick = onEndAdventure) {
					Text("Beenden")
				}
				TextButton(onClick = onToggleExpanded) {
					Text(if (isExpanded) "Ausblenden" else "Einblenden")
				}
			}

			AnimatedVisibility(
				visible = isExpanded,
				enter = slideInVertically { -it / 2 } + fadeIn(),
				exit = slideOutVertically { -it / 2 } + fadeOut(),
			) {
				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text(
						text = "Hinweis: Schau nach auffaelligen Markierungen in der Naehe des Startpunkts.",
						style = MaterialTheme.typography.bodySmall,
					)
					Text(
						text = "Raetsel: Welche Zahl ergibt sich aus den sichtbaren Hausnummern an der Ecke?",
						style = MaterialTheme.typography.bodySmall,
					)
					Text(
						text = "Status: Demo-Inhalte aktiv. Echte Hinweise koennen spaeter aus dem Backend geladen werden.",
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}
		}
	}
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

@Composable
fun AdventureListScreen(
	modifier: Modifier,
	searchQuery: String,
	onSearchQueryChange: (String) -> Unit,
	currentLocation: GeoPoint?,
	onNavigateToStart: (Adventure) -> Unit,
	onStartAdventure: (Adventure) -> Unit,
	onCreateAdventure: () -> Unit,
	onEditAdventure: (Adventure) -> Unit,
	onDeleteAdventure: (Adventure) -> Unit,
	adventures: List<Adventure>,
) {
	val visibleAdventures = remember(searchQuery, currentLocation, adventures) {
		val filtered = adventures.filter { adventure ->
			searchQuery.isBlank() || adventure.title.contains(searchQuery.trim(), ignoreCase = true)
		}

		if (currentLocation == null) {
			filtered.sortedBy { it.title.lowercase() }
		} else {
			filtered.sortedWith(
				compareBy<Adventure> { adventure -> adventure.startPoint.distanceTo(currentLocation) }
					.thenBy { it.title.lowercase() },
			)
		}
	}

	Box(modifier = modifier) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Text(
				text = "Abenteuer in deiner Nähe",
				style = MaterialTheme.typography.headlineSmall,
			)

			OutlinedTextField(
				value = searchQuery,
				onValueChange = onSearchQueryChange,
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				label = { Text("Nach Titel suchen") },
				placeholder = { Text("z. B. Rätsel") },
			)

			LazyColumn(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(bottom = 88.dp),
			) {
				items(
					items = visibleAdventures,
					key = { it.id },
				) { adventure ->
					AdventureCard(
						adventure = adventure,
						currentLocation = currentLocation,
						onNavigateToStart = { onNavigateToStart(adventure) },
						onStartAdventure = { onStartAdventure(adventure) },
						onEditAdventure = { onEditAdventure(adventure) },
						onDeleteAdventure = { onDeleteAdventure(adventure) },
					)
				}
			}
		}

		FloatingActionButton(
			onClick = onCreateAdventure,
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(24.dp),
			shape = CircleShape,
		) {
			Text(text = "+", style = MaterialTheme.typography.headlineMedium)
		}
	}
}

@Composable
private fun AdventureCard(
	adventure: Adventure,
	currentLocation: GeoPoint?,
	onNavigateToStart: () -> Unit,
	onStartAdventure: () -> Unit,
	onEditAdventure: () -> Unit,
	onDeleteAdventure: () -> Unit,
) {
	var showActionsMenu by remember(adventure.id) { mutableStateOf(false) }
	val startDistanceMeters = currentLocation?.let { adventure.startPoint.distanceTo(it) }
	val canStart = startDistanceMeters != null && startDistanceMeters <= START_ADVENTURE_MAX_DISTANCE_METERS

	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
		),
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp),
		) {
			Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(
					text = adventure.title,
					style = MaterialTheme.typography.titleMedium,
				)
				if (adventure.difficulty != null || adventure.estimatedDurationMinutes != null) {
					Text(
						text = buildMetadataLabel(adventure),
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.secondary,
					)
				}
				Text(
					text = adventure.summary,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Text(
					text = distanceLabel(adventure.startPoint, currentLocation),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.primary,
				)
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Button(
					onClick = onNavigateToStart,
					modifier = Modifier.weight(1f),
				) {
					Text("Zum Startpunkt")
				}
				Button(
					onClick = onStartAdventure,
					enabled = canStart,
					modifier = Modifier.weight(1f),
				) {
					Text("Abenteuer starten")
				}
				Box {
					IconButton(onClick = { showActionsMenu = true }) {
						Text("...")
					}
					DropdownMenu(
						expanded = showActionsMenu,
						onDismissRequest = { showActionsMenu = false },
					) {
						DropdownMenuItem(
							text = { Text("Bearbeiten") },
							onClick = {
								showActionsMenu = false
								onEditAdventure()
							},
						)
						DropdownMenuItem(
							text = { Text("Loeschen") },
							onClick = {
								showActionsMenu = false
								onDeleteAdventure()
							},
						)
					}
				}
			}

			if (!canStart) {
				Text(
					text = startButtonHint(startDistanceMeters),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
	}
}

private fun distanceLabel(startPoint: GeoPoint, currentLocation: GeoPoint?): String {
	if (currentLocation == null) {
		return "Entfernung unbekannt"
	}

	val distanceMeters = startPoint.distanceTo(currentLocation)
	return if (distanceMeters >= 1_000) {
		val kilometers = distanceMeters / 1_000
		"${(kilometers * 10).roundToInt() / 10.0} km entfernt"
	} else {
		"${distanceMeters.roundToInt()} m entfernt"
	}
}

private fun canStartAdventure(startPoint: GeoPoint, currentLocation: GeoPoint?): Boolean {
	if (currentLocation == null) {
		return false
	}

	return startPoint.distanceTo(currentLocation) <= START_ADVENTURE_MAX_DISTANCE_METERS
}

private fun startButtonHint(distanceMeters: Double?): String {
	if (distanceMeters == null) {
		return "Abenteuerstart ist nur mit aktivem Standort moeglich."
	}

	if (distanceMeters <= START_ADVENTURE_MAX_DISTANCE_METERS) {
		return "Du bist nah genug am Startpunkt."
	}

	return "Starte innerhalb von 10 m (aktuell: ${distanceMeters.roundToInt()} m)."
}

private fun startDialogDistanceLabel(startPoint: GeoPoint, currentLocation: GeoPoint?): String {
	if (currentLocation == null) {
		return "Standort nicht verfuegbar. Bitte aktiviere GPS."
	}

	val distanceMeters = startPoint.distanceTo(currentLocation).roundToInt()
	return "Du bist $distanceMeters m vom Startpunkt entfernt."
}

private fun buildMetadataLabel(adventure: Adventure): String {
	val difficultyPart = adventure.difficulty?.let { "Schwierigkeit: $it" }
	val durationPart = adventure.estimatedDurationMinutes?.let { "Dauer: $it min" }
	return listOfNotNull(difficultyPart, durationPart).joinToString(" | ")
}
