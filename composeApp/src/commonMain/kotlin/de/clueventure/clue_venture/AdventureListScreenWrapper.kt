package de.clueventure.clue_venture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun AdventureListScreenWrapper(
    modifier: Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentLocation: GeoPoint?,
    refreshKey: Int,
    onNavigateToStart: (Adventure) -> Unit,
    onStartAdventure: (Adventure) -> Unit,
    onCreateAdventure: () -> Unit,
    onEditAdventure: (Adventure) -> Unit,
    onDeleteAdventure: (Adventure) -> Unit,
) {
    var adventures by remember { mutableStateOf<List<Adventure>>(listOf()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        loadError = false
        try {
            adventures = getAdventures()
        } catch (t: Throwable) {
            adventures = emptyList()
            loadError = true
        }
        isLoading = false
    }

    AdventureListScreen(
        modifier = modifier,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        currentLocation = currentLocation,
        onNavigateToStart = onNavigateToStart,
        onStartAdventure = onStartAdventure,
        onCreateAdventure = onCreateAdventure,
        onEditAdventure = onEditAdventure,
        onDeleteAdventure = onDeleteAdventure,
        adventures = if (isLoading) emptyList() else adventures,
        isLoading = isLoading,
        loadError = loadError,
    )
}
