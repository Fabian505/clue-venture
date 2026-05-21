package de.clueventure.clue_venture

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdventureListScreenWrapper(
    modifier: Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentLocation: GeoPoint?,
    currentUserId: String?,
    adventures: List<Adventure>,
    isLoading: Boolean,
    loadError: Boolean,
    onNavigateToStart: (Adventure) -> Unit,
    onStartAdventure: (Adventure) -> Unit,
    onCreateAdventure: () -> Unit,
    onEditAdventure: (Adventure) -> Unit,
    onDeleteAdventure: (Adventure) -> Unit,
) {
    AdventureListScreen(
        modifier = modifier,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        currentLocation = currentLocation,
        currentUserId = currentUserId,
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
