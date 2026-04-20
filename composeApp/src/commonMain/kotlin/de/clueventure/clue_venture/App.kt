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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
	var selectedTab by remember { mutableStateOf(BottomTab.Map) }
	var searchQuery by remember { mutableStateOf("") }
	var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
	var routeTarget by remember { mutableStateOf<GeoPoint?>(null) }
	var showCreateAdventureDialog by remember { mutableStateOf(false) }

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
				BottomTab.Left -> AdventureListScreen(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					searchQuery = searchQuery,
					onSearchQueryChange = { searchQuery = it },
					currentLocation = currentLocation,
					onNavigateToStart = { adventure ->
						routeTarget = adventure.startPoint
						selectedTab = BottomTab.Map
					},
					onCreateAdventure = { showCreateAdventureDialog = true },
				)

				BottomTab.Map -> PlatformMap(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					routeTarget = routeTarget,
					onCurrentLocationChanged = { currentLocation = it },
				)

				BottomTab.Right -> TodoList()
			}
		}

		if (showCreateAdventureDialog) {
			AlertDialog(
				onDismissRequest = { showCreateAdventureDialog = false },
				title = { Text("Neues Abenteuer") },
				text = {
					Text("Die Erstellung neuer Abenteuer wird später verfeinert. Aktuell ist das ein Platzhalter.")
				},
				confirmButton = {
					TextButton(onClick = { showCreateAdventureDialog = false }) {
						Text("Verstanden")
					}
				},
			)
		}
	}
}

@Composable
private fun AdventureListScreen(
	modifier: Modifier,
	searchQuery: String,
	onSearchQueryChange: (String) -> Unit,
	currentLocation: GeoPoint?,
	onNavigateToStart: (Adventure) -> Unit,
	onCreateAdventure: () -> Unit,
) {
	val visibleAdventures = remember(searchQuery, currentLocation) {
		val filtered = sampleAdventures.filter { adventure ->
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
) {
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
				OutlinedButton(onClick = { }, enabled = false) {
					Text("Noch offen")
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

