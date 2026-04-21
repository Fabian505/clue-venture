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
    onNavigateToStart: (Adventure) -> Unit,
    onCreateAdventure: () -> Unit,
) {
    var adventures by remember { mutableStateOf<List<Adventure>>(listOf()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        adventures = getAdventures()
        isLoading = false
    }

    AdventureListScreen(
        modifier = modifier,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        currentLocation = currentLocation,
        onNavigateToStart = onNavigateToStart,
        onCreateAdventure = onCreateAdventure,
        adventures = if (isLoading) emptyList() else adventures,
    )
}
