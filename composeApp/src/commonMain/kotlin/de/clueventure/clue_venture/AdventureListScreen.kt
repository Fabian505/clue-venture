package de.clueventure.clue_venture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val ADVENTURE_LIST_START_MAX_DISTANCE_METERS = 10.0

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
    isLoading: Boolean,
    loadError: Boolean,
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

            if (!isLoading && (visibleAdventures.isEmpty() || loadError)) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (loadError) "Keine Abenteuer konnten geladen werden." else "Keine Abenteuer gefunden.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
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
    val canStart = startDistanceMeters != null && startDistanceMeters <= ADVENTURE_LIST_START_MAX_DISTANCE_METERS

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
                        text = buildListMetadataLabel(adventure),
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
                    text = listDistanceLabel(adventure.startPoint, currentLocation),
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
                    Text("Startpunkt")
                }
                Button(
                    onClick = onStartAdventure,
                    enabled = canStart,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Starten")
                }
                Box {
                    OutlinedButton(
                        onClick = { showActionsMenu = true },
                        modifier = Modifier.size(width = 56.dp, height = 40.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("•••")
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

private fun listDistanceLabel(startPoint: GeoPoint, currentLocation: GeoPoint?): String {
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

private fun buildListMetadataLabel(adventure: Adventure): String {
    val difficultyPart = adventure.difficulty?.let { "Schwierigkeit: $it" }
    val durationPart = adventure.estimatedDurationMinutes?.let { "Dauer: $it min" }
    return listOfNotNull(difficultyPart, durationPart).joinToString(" | ")
}
