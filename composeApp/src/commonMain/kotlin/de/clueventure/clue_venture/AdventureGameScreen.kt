package de.clueventure.clue_venture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main Adventure Game Screen - orchestrates the complete adventure experience
 * with GPS tracking, waypoints, quiz, and points calculation
 */
@Composable
fun AdventureGameScreen(
    adventure: Adventure,
    userId: String,
    currentLocation: GeoPoint?,
    onAdventureComplete: (pointsEarned: Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    
    // State management
    var gameState by remember { mutableStateOf<GameState>(GameState.Loading) }
    var currentAttempt by remember { mutableStateOf<AdventureAttempt?>(null) }
    var currentCheckpointIndex by remember { mutableIntStateOf(0) }
    var currentLocationState by remember { mutableStateOf<GeoPointState?>(null) }
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var distanceToWaypoint by remember { mutableStateOf(0.0) }
    var pointsEarned by remember { mutableIntStateOf(0) }
    var correctAnswerCount by remember { mutableIntStateOf(0) }

    val currentWaypoint = adventure.locations.getOrNull(currentCheckpointIndex)
    val isProximityAlertVisible = distanceToWaypoint in 0.0..50.0
    val isWaypointReached = distanceToWaypoint < 5.0

    // Initialize adventure attempt
    LaunchedEffect(adventure.id) {
        coroutineScope.launch {
            try {
                gameState = GameState.Loading
                val attempt = getCurrentAttemptForAdventure(adventure.id, userId)
                    ?: startAdventureAttempt(adventure.id, userId)
                currentAttempt = attempt

                // Load quiz questions for the adventure
                quizQuestions = getQuizQuestions(adventure.id)

                gameState = if (currentWaypoint != null) GameState.Navigating else GameState.Complete
            } catch (e: Exception) {
                println("Error initializing adventure: ${e.message}")
                gameState = GameState.Error
            }
        }
    }

    // Location tracking
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val locationService = getLocationService()
                locationService.startLocationTracking(interval = 10000) { location ->
                    currentLocationState = location
                    currentWaypoint?.point?.let { waypointPoint ->
                        distanceToWaypoint = location.point.distanceTo(waypointPoint)
                    }

                    // Update progress in database
                    coroutineScope.launch {
                        currentAttempt?.id?.let {
                            try {
                                updateUserProgress(it, currentCheckpointIndex, location)
                            } catch (e: Exception) {
                                println("Error updating progress: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error starting location tracking: ${e.message}")
            }
        }
    }

    // Handle waypoint reached
    LaunchedEffect(isWaypointReached) {
        if (isWaypointReached && gameState == GameState.Navigating && currentWaypoint != null) {
            println("Waypoint reached: ${currentWaypoint.name}")
            gameState = GameState.ShowingProximityAlert

            // Delay before showing quiz
            delay(2000)

            // Get quiz questions for this adventure
            if (quizQuestions.isNotEmpty()) {
                gameState = GameState.ShowingQuiz
            } else {
                // No quiz, move to next waypoint
                moveToNextCheckpoint(
                    adventure,
                    currentCheckpointIndex,
                    { nextIndex -> currentCheckpointIndex = nextIndex },
                    { gameState = it },
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = adventure.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        modifier = modifier,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (gameState) {
                GameState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Abenteuer wird vorbereitet...")
                    }
                }

                GameState.Navigating -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        currentWaypoint?.let {
                            NavigationIndicator(
                                currentLocation = currentLocationState?.point,
                                targetLocation = it.point,
                                targetName = it.name,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        ProximityAlertPopup(
                            currentLocation = currentLocationState?.point,
                            targetLocation = currentWaypoint?.point ?: GeoPoint(0.0, 0.0),
                            isVisible = isProximityAlertVisible,
                            distanceMeters = distanceToWaypoint,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                        )

                        Text(
                            text = "Fortschritt: ${currentCheckpointIndex + 1}/${adventure.locations.size}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                GameState.ShowingProximityAlert -> {
                    ProximityAlertPopup(
                        currentLocation = currentLocationState?.point,
                        targetLocation = currentWaypoint?.point ?: GeoPoint(0.0, 0.0),
                        isVisible = true,
                        distanceMeters = distanceToWaypoint,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                GameState.ShowingQuiz -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        QuizScreen(
                            questions = quizQuestions,
                            onQuizCompleted = { correct ->
                                correctAnswerCount = correct
                                coroutineScope.launch {
                                    moveToNextCheckpoint(
                                        adventure,
                                        currentCheckpointIndex,
                                        { nextIndex -> currentCheckpointIndex = nextIndex },
                                        { newState -> gameState = newState },
                                    )
                                }
                            },
                            onClose = onClose,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                GameState.Complete -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "🎉 Abenteuer abgeschlossen!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Finish adventure and calculate points
                        LaunchedEffect(Unit) {
                            coroutineScope.launch {
                                currentAttempt?.id?.let {
                                    try {
                                        val points = finishAdventureAttempt(it)
                                        pointsEarned = points
                                        onAdventureComplete(points)
                                    } catch (e: Exception) {
                                        println("Error finishing adventure: ${e.message}")
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Punkte: $pointsEarned",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Text(
                            text = "Richtige Antworten: $correctAnswerCount",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(onClick = onClose) {
                            Text("Zurück zur Liste")
                        }
                    }
                }

                GameState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("❌ Ein Fehler ist aufgetreten")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onClose) {
                            Text("Zurück")
                        }
                    }
                }
            }
        }
    }
}

// Game state enum
enum class GameState {
    Loading,
    Navigating,
    ShowingProximityAlert,
    ShowingQuiz,
    Complete,
    Error,
}

// Helper function to move to next checkpoint
private suspend fun moveToNextCheckpoint(
    adventure: Adventure,
    currentIndex: Int,
    updateIndex: (Int) -> Unit,
    updateGameState: (GameState) -> Unit,
) {
    val nextIndex = currentIndex + 1
    if (nextIndex >= adventure.locations.size) {
        // Adventure complete
        updateGameState(GameState.Complete)
    } else {
        // Move to next waypoint
        updateIndex(nextIndex)
        updateGameState(GameState.Navigating)
    }
}
