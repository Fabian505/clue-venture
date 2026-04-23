package de.clueventure.clue_venture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformMap(
    modifier: Modifier,
    routeTargets: List<GeoPoint>,
    onCurrentLocationChanged: (GeoPoint?) -> Unit,
    enablePointSelection: Boolean,
    selectedPoint: GeoPoint?,
    onMapPointSelected: (GeoPoint) -> Unit,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val selectionText = if (enablePointSelection) {
            " | Kartenwahl auf Android"
        } else {
            ""
        }
        Text("Map is available on Android (${routeTargets.size} Ziele geladen$selectionText)")
    }
}
