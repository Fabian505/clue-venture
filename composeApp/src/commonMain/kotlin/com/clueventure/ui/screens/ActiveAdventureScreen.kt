package com.clueventure.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clueventure.data.model.Adventure
import com.clueventure.data.model.Waypoint
import com.clueventure.data.repository.AdventureRepository
import com.clueventure.service.LocationCoordinate
import com.clueventure.service.LocationService
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveAdventureScreen(
    adventure: Adventure,
    onNavigateBack: () -> Unit,
    onWaypointReached: (Waypoint) -> Unit
) {
    val repository = remember { AdventureRepository() }
    val locationService = remember { LocationService() }

    var waypoints by remember { mutableStateOf<List<Waypoint>>(emptyList()) }
    var currentWaypointIndex by remember { mutableStateOf(0) }
    var userLocation by remember { mutableStateOf<LocationCoordinate?>(null) }
    var distanceToTarget by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adventure.id) {
        runCatching { repository.getWaypointsForAdventure(adventure.id) }
            .onSuccess { waypoints = it; isLoading = false }
            .onFailure { errorMessage = it.message; isLoading = false }
    }

    LaunchedEffect(Unit) {
        launch {
            locationService.locationUpdates.collect { location ->
                userLocation = location
                val targetWaypoint = waypoints.getOrNull(currentWaypointIndex)
                if (targetWaypoint != null) {
                    distanceToTarget = haversineDistanceMeters(
                        lat1 = location.latitude,
                        lon1 = location.longitude,
                        lat2 = targetWaypoint.latitude,
                        lon2 = targetWaypoint.longitude
                    )
                    if ((distanceToTarget ?: Double.MAX_VALUE) <= targetWaypoint.radiusMeters) {
                        onWaypointReached(targetWaypoint)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(adventure.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorMessage != null -> {
                    Text(
                        text = "Error: $errorMessage",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    val currentWaypoint = waypoints.getOrNull(currentWaypointIndex)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Progress indicator
                        Text(
                            text = "Waypoint ${currentWaypointIndex + 1} of ${waypoints.size}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Current waypoint info
                        if (currentWaypoint != null) {
                            WaypointInfoCard(
                                waypoint = currentWaypoint,
                                distanceMeters = distanceToTarget
                            )
                        } else {
                            Text(
                                text = "Adventure complete! 🎉",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // User location display
                        userLocation?.let { location ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "Your location",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        "Lat: %.6f  Lon: %.6f".format(
                                            location.latitude,
                                            location.longitude
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        } ?: Text(
                            text = "Acquiring GPS signal…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaypointInfoCard(waypoint: Waypoint, distanceMeters: Double?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = waypoint.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = waypoint.description,
                style = MaterialTheme.typography.bodyMedium
            )
            waypoint.clueText?.let { clue ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Clue: $clue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            distanceMeters?.let { dist ->
                val displayText = if (dist >= 1000) {
                    "%.1f km away".format(dist / 1000)
                } else {
                    "%.0f m away".format(dist)
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (dist <= waypoint.radiusMeters) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

/** Haversine formula to compute great-circle distance in metres. */
private fun haversineDistanceMeters(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0
    val a = sin(dLat / 2).pow(2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusMeters * c
}
