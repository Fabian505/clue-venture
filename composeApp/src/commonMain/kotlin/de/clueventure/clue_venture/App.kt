package de.clueventure.clue_venture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
	var adventureRefreshKey by remember { mutableStateOf(0) }

	MaterialTheme {
		Scaffold(
			contentWindowInsets = WindowInsets(0, 0, 0, 0),
			bottomBar = {
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
			},
		) { innerPadding ->
			when (selectedTab) {
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
						selectedTab = BottomTab.Map
						appScope.launch {
							val locations = getAdventureLocations(adventure.id)
							routeTargets = listOf(adventure.startPoint) + locations.map { it.point }
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

				BottomTab.Map -> PlatformMap(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					routeTargets = routeTargets,
					onCurrentLocationChanged = { currentLocation = it },
				)

				BottomTab.Right -> TodoList()
			}
		}

		if (showCreateAdventureDialog) {
			CreateAdventureDialog(
				currentLocation = currentLocation,
				onDismiss = { showCreateAdventureDialog = false },
				onAdventureCreated = {
					adventureRefreshKey += 1
				},
			)
		}

		editingAdventure?.let { adventure ->
			EditAdventureDialog(
				adventure = adventure,
				currentLocation = currentLocation,
				onDismiss = { editingAdventure = null },
				onAdventureUpdated = {
					adventureRefreshKey += 1
				},
			)
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
	}
}

@Composable
private fun CreateAdventureDialog(
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

	AlertDialog(
		onDismissRequest = {
			if (!isSubmitting) {
				onDismiss()
			}
		},
		title = { Text("Neues Abenteuer") },
		text = {
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(8.dp),
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
		},
		confirmButton = {
			Button(
				onClick = {
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
									onDismiss()
								}.onFailure { throwable ->
									errorMessage = throwable.message ?: "Abenteuer konnte nicht gespeichert werden."
								}
								isSubmitting = false
							}
						}
					}
				},
				enabled = !isSubmitting,
			) {
				if (isSubmitting) {
					CircularProgressIndicator(
						modifier = Modifier.size(18.dp),
						strokeWidth = 2.dp,
					)
				} else {
					Text("Speichern")
				}
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss, enabled = !isSubmitting) {
				Text("Abbrechen")
			}
		},
	)
}

@Composable
private fun EditAdventureDialog(
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

	AlertDialog(
		onDismissRequest = {
			if (!isSubmitting) {
				onDismiss()
			}
		},
		title = { Text("Abenteuer bearbeiten") },
		text = {
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(8.dp),
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
		},
		confirmButton = {
			Button(
				onClick = {
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
				},
				enabled = !isSubmitting,
			) {
				if (isSubmitting) {
					CircularProgressIndicator(
						modifier = Modifier.size(18.dp),
						strokeWidth = 2.dp,
					)
				} else {
					Text("Speichern")
				}
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss, enabled = !isSubmitting) {
				Text("Abbrechen")
			}
		},
	)
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

@Composable
fun AdventureListScreen(
	modifier: Modifier,
	searchQuery: String,
	onSearchQueryChange: (String) -> Unit,
	currentLocation: GeoPoint?,
	onNavigateToStart: (Adventure) -> Unit,
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
	onEditAdventure: () -> Unit,
	onDeleteAdventure: () -> Unit,
) {
	var showActionsMenu by remember(adventure.id) { mutableStateOf(false) }

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

			Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
				Button(onClick = onNavigateToStart) {
					Text("Zum Startpunkt")
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

private fun buildMetadataLabel(adventure: Adventure): String {
	val difficultyPart = adventure.difficulty?.let { "Schwierigkeit: $it" }
	val durationPart = adventure.estimatedDurationMinutes?.let { "Dauer: $it min" }
	return listOfNotNull(difficultyPart, durationPart).joinToString(" | ")
}
