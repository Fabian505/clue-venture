package de.clueventure.clue_venture

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ProximityAlertPopup displays hot/cold feedback when user is within 50m of a waypoint
 */
@Composable
fun ProximityAlertPopup(
    currentLocation: GeoPoint?,
    targetLocation: GeoPoint,
    isVisible: Boolean,
    distanceMeters: Double? = null,
    modifier: Modifier = Modifier,
) {
    if (!isVisible || currentLocation == null) return

    val proximityStatus = currentLocation.proximityStatus(targetLocation, maxRangeMeters = 50.0)
    val displayText = proximityStatus.getDisplayText()
    val hexColor = proximityStatus.getHexColor()

    val bgColor = Color(android.graphics.Color.parseColor(hexColor))
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (proximityStatus == ProximityStatus.VERY_HOT) 1.1f else 1f,
        animationSpec = if (proximityStatus == ProximityStatus.VERY_HOT) {
            infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            )
        } else {
            tween(0)
        },
        label = "scale_animation",
    )

    Box(
        modifier = modifier
            .padding(16.dp)
            .scale(animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when (proximityStatus) {
                    ProximityStatus.VERY_HOT -> Color(0xFFFF0000)
                    ProximityStatus.HOT -> Color(0xFFFF6600)
                    ProximityStatus.WARM -> Color(0xFFFFB700)
                    ProximityStatus.COLD -> Color(0xFFFFC700)
                    else -> Color(0xFF0066FF)
                },
            )
            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = displayText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (distanceMeters != null) {
                Text(
                    text = String.format("%.0f m", distanceMeters),
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }

            // Pulse indicator for VERY_HOT status
            if (proximityStatus == ProximityStatus.VERY_HOT) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White),
                )
            }
        }
    }
}

/**
 * Navigation indicator showing direction and distance to target waypoint
 */
@Composable
fun NavigationIndicator(
    currentLocation: GeoPoint?,
    targetLocation: GeoPoint,
    targetName: String,
    modifier: Modifier = Modifier,
) {
    if (currentLocation == null) {
        Box(modifier = modifier.padding(16.dp)) {
            Text("📍 Warte auf GPS-Signal...", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    val distance = currentLocation.distanceTo(targetLocation)
    val arrowIcon = when {
        distance < 10 -> "🎯"
        distance < 50 -> "👈"
        distance < 200 -> "🧭"
        else -> "🗺️"
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = arrowIcon + " " + targetName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = String.format("Entfernung: %.0f Meter", distance),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
