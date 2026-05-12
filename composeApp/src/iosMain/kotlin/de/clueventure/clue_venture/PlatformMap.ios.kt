package de.clueventure.clue_venture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color

@Composable
actual fun PlatformMap(
    modifier: Modifier,
    routeTargets: List<GeoPoint>,
    onCurrentLocationChanged: (GeoPoint?) -> Unit,
    enablePointSelection: Boolean,
    selectedPoint: GeoPoint?,
    onMapPointSelected: (GeoPoint) -> Unit,
    questionCount: Int,
    onQuestionsClicked: () -> Unit,
    onRouteDistanceChanged: (Double?) -> Unit,
) {
    onRouteDistanceChanged(null)
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Row {
            TextButton(
                onClick = onQuestionsClicked,
                enabled = questionCount > 0,
            ) {
                Text(if (questionCount > 0) "Fragen: $questionCount" else "Keine Fragen")
            }
            Spacer(modifier = Modifier.size(8.dp))
            Surface(shape = CircleShape) {
                Text(
                    text = "Zentrieren",
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        val selectionText = if (enablePointSelection) {
            " | Kartenwahl auf Android"
        } else {
            ""
        }
        Text("Map is available on Android (${routeTargets.size} Ziele geladen$selectionText)")
    }
}
