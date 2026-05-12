package de.clueventure.clue_venture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Main Adventure Game Screen - orchestrates the complete adventure experience
 * with GPS tracking, waypoints, quiz, and points calculation
 */
@Composable
fun AdventureGameScreen(
    adventure: Adventure,
    userId: String,
    currentLocation: GeoPoint?,
    initialAttempt: AdventureAttempt? = null,
    isAdventureComplete: Boolean = false,
    onAdventureComplete: (pointsEarned: Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    
    // State management
    var gameState by remember { mutableStateOf<GameState>(GameState.Loading) }
    var currentAttempt by remember(initialAttempt?.id) { mutableStateOf(initialAttempt) }
    var currentCheckpointIndex by remember { mutableIntStateOf(0) }
    var currentLocationState by remember { mutableStateOf<GeoPointState?>(null) }
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var distanceToWaypoint by remember { mutableStateOf(0.0) }
    var currentRouteDistanceMeters by remember { mutableStateOf<Double?>(null) }
    var unlockedQuestionSlots by remember { mutableIntStateOf(0) }
    var pendingUnlockCheckpointIndex by remember { mutableStateOf<Int?>(null) }
    var showQuizSheet by remember { mutableStateOf(false) }
    var activeQuizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var lastReachedCheckpointIndex by remember { mutableIntStateOf(-1) }
    var answeredQuestionIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var pointsEarned by remember { mutableIntStateOf(0) }
    var correctAnswerCount by remember { mutableIntStateOf(0) }
    var hasFinishedAttempt by remember { mutableStateOf(false) }
    var isFinishingAttempt by remember { mutableStateOf(false) }
    var difficultyRating by remember { mutableIntStateOf(0) }
    var overallRating by remember { mutableIntStateOf(0) }
    var customFeedback by remember { mutableStateOf("") }
    var isSubmittingFeedback by remember { mutableStateOf(false) }
    var feedbackError by remember { mutableStateOf<String?>(null) }
    var attemptError by remember { mutableStateOf<String?>(null) }
    var finishError by remember { mutableStateOf<String?>(null) }

    val currentWaypoint = adventure.locations.getOrNull(currentCheckpointIndex)
    val isProximityAlertVisible = distanceToWaypoint in 0.0..50.0
    val isWaypointReached = distanceToWaypoint < 5.0
    val unansweredQuestions = quizQuestions.filterNot { it.id in answeredQuestionIds }
    val availableQuestionCount = max(
        0,
        minOf(unlockedQuestionSlots, quizQuestions.size) - answeredQuestionIds.size,
    )
    val availableQuestions = unansweredQuestions.take(availableQuestionCount)

    // Initialize adventure attempt
    LaunchedEffect(adventure.id) {
        coroutineScope.launch {
            try {
                if (!isAdventureComplete) {
                    gameState = GameState.Loading
                }
                val attempt = currentAttempt
                    ?: initialAttempt
                    ?: getCurrentAttemptForAdventure(adventure.id, userId)
                    ?: startAdventureAttempt(adventure.id, userId)
                currentAttempt = attempt
                attemptError = null

                // Load quiz questions for the adventure
                quizQuestions = getQuizQuestions(adventure.id)

                gameState = if (isAdventureComplete || currentWaypoint == null) {
                    GameState.Complete
                } else {
                    GameState.Navigating
                }
            } catch (e: Exception) {
                println("Error initializing adventure: ${e.message}")
                attemptError = "Feedback kann aktuell nicht gespeichert werden, weil kein Abenteuer-Versuch gefunden wurde."
                if (!isAdventureComplete) {
                    gameState = GameState.Error
                }
            }
        }
    }

    // Handle waypoint reached
    LaunchedEffect(isWaypointReached) {
        if (
            isWaypointReached &&
            gameState == GameState.Navigating &&
            currentWaypoint != null &&
            currentCheckpointIndex != lastReachedCheckpointIndex
        ) {
            lastReachedCheckpointIndex = currentCheckpointIndex
            moveToNextCheckpoint(
                adventure,
                currentCheckpointIndex,
                { nextIndex ->
                    currentCheckpointIndex = nextIndex
                },
                { gameState = it },
                onCheckpointAdvanced = { nextIndex ->
                    pendingUnlockCheckpointIndex = nextIndex
                    currentRouteDistanceMeters = null
                },
            )
        }
    }

    LaunchedEffect(pendingUnlockCheckpointIndex, currentCheckpointIndex, currentRouteDistanceMeters) {
        val pendingIndex = pendingUnlockCheckpointIndex ?: return@LaunchedEffect
        if (pendingIndex != currentCheckpointIndex) {
            return@LaunchedEffect
        }

        val routeDistance = currentRouteDistanceMeters ?: return@LaunchedEffect
        val unlockedQuestions = unlockedQuestionsForRouteDistance(routeDistance)
        if (unlockedQuestions > 0) {
            unlockedQuestionSlots += unlockedQuestions
        }
        pendingUnlockCheckpointIndex = null
    }

    LaunchedEffect(isAdventureComplete) {
        if (isAdventureComplete) {
            currentCheckpointIndex = adventure.locations.lastIndex.coerceAtLeast(0)
            gameState = GameState.Complete
        }
    }

    LaunchedEffect(gameState, currentAttempt?.id) {
        val attemptId = currentAttempt?.id
        if (gameState == GameState.Complete && attemptId != null && !hasFinishedAttempt && !isFinishingAttempt) {
            isFinishingAttempt = true
            try {
                pointsEarned = finishAdventureAttempt(attemptId)
                hasFinishedAttempt = true
                finishError = null
            } catch (e: Exception) {
                println("Error finishing adventure: ${e.message}")
                finishError = "Das Abenteuer konnte nicht vollstaendig abgeschlossen werden. Feedback kann erst nach einem erfolgreichen Abschluss gespeichert werden."
            } finally {
                isFinishingAttempt = false
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
                Button(
                    onClick = {
                        if (gameState == GameState.Complete && hasFinishedAttempt) {
                            onAdventureComplete(pointsEarned)
                        } else {
                            onClose()
                        }
                    },
                ) {
                    Text("Schließen")
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlatformMap(
                            modifier = Modifier.fillMaxSize(),
                            routeTargets = currentWaypoint?.point?.let { listOf(it) }.orEmpty(),
                            onCurrentLocationChanged = { location ->
                                val updatedLocation = location?.let {
                                    GeoPointState(
                                        point = it,
                                        timestamp = System.currentTimeMillis().toString(),
                                    )
                                }
                                currentLocationState = updatedLocation

                                location?.let { currentLocation ->
                                    currentWaypoint?.point?.let { waypointPoint ->
                                        distanceToWaypoint = currentLocation.distanceTo(waypointPoint)
                                    }
                                }

                                currentAttempt?.id?.let { attemptId ->
                                    coroutineScope.launch {
                                        try {
                                            updateUserProgress(attemptId, currentCheckpointIndex, updatedLocation)
                                        } catch (e: Exception) {
                                            println("Error updating progress: ${e.message}")
                                        }
                                    }
                                }
                            },
                            questionCount = availableQuestionCount,
                            onQuestionsClicked = {
                                if (availableQuestions.isNotEmpty()) {
                                    activeQuizQuestions = availableQuestions
                                    showQuizSheet = true
                                    gameState = GameState.ShowingQuiz
                                }
                            },
                            onRouteDistanceChanged = { routeDistance ->
                                currentRouteDistanceMeters = routeDistance
                            },
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            currentWaypoint?.let {
                                NavigationIndicator(
                                    currentLocation = currentLocationState?.point,
                                    targetLocation = it.point,
                                    targetName = it.name,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            Text(
                                text = "Fortschritt: ${currentCheckpointIndex + 1}/${adventure.locations.size}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            ProximityAlertPopup(
                                currentLocation = currentLocationState?.point,
                                targetLocation = currentWaypoint?.point ?: GeoPoint(0.0, 0.0),
                                isVisible = isProximityAlertVisible,
                                distanceMeters = distanceToWaypoint,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                GameState.ShowingProximityAlert -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlatformMap(
                            modifier = Modifier.fillMaxSize(),
                            routeTargets = currentWaypoint?.point?.let { listOf(it) }.orEmpty(),
                            onCurrentLocationChanged = { location ->
                                val updatedLocation = location?.let {
                                    GeoPointState(
                                        point = it,
                                        timestamp = System.currentTimeMillis().toString(),
                                    )
                                }
                                currentLocationState = updatedLocation

                                location?.let { currentLocation ->
                                    currentWaypoint?.point?.let { waypointPoint ->
                                        distanceToWaypoint = currentLocation.distanceTo(waypointPoint)
                                    }
                                }
                            },
                            questionCount = availableQuestionCount,
                            onQuestionsClicked = {
                                if (availableQuestions.isNotEmpty()) {
                                    activeQuizQuestions = availableQuestions
                                    showQuizSheet = true
                                    gameState = GameState.ShowingQuiz
                                }
                            },
                            onRouteDistanceChanged = { routeDistance ->
                                currentRouteDistanceMeters = routeDistance
                            },
                        )

                        ProximityAlertPopup(
                            currentLocation = currentLocationState?.point,
                            targetLocation = currentWaypoint?.point ?: GeoPoint(0.0, 0.0),
                            isVisible = true,
                            distanceMeters = distanceToWaypoint,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                        )
                    }
                }

                GameState.ShowingQuiz -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlatformMap(
                            modifier = Modifier.fillMaxSize(),
                            routeTargets = currentWaypoint?.point?.let { listOf(it) }.orEmpty(),
                            onCurrentLocationChanged = { location ->
                                val updatedLocation = location?.let {
                                    GeoPointState(
                                        point = it,
                                        timestamp = System.currentTimeMillis().toString(),
                                    )
                                }
                                currentLocationState = updatedLocation

                                location?.let { currentLocation ->
                                    currentWaypoint?.point?.let { waypointPoint ->
                                        distanceToWaypoint = currentLocation.distanceTo(waypointPoint)
                                    }
                                }
                            },
                            questionCount = availableQuestionCount,
                            onQuestionsClicked = {
                                if (availableQuestions.isNotEmpty()) {
                                    activeQuizQuestions = availableQuestions
                                    showQuizSheet = true
                                }
                            },
                            onRouteDistanceChanged = { routeDistance ->
                                currentRouteDistanceMeters = routeDistance
                            },
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .heightIn(max = 720.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Verfuegbare Fragen (${activeQuizQuestions.size})",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        TextButton(
                                            onClick = {
                                                showQuizSheet = false
                                                gameState = GameState.Navigating
                                            },
                                        ) {
                                            Text("Schliessen")
                                        }
                                    }

                                    QuizScreen(
                                        questions = activeQuizQuestions,
                                        attemptId = currentAttempt?.id,
                                        onAnswerEvaluated = { event ->
                                            answeredQuestionIds = answeredQuestionIds + event.questionId
                                            if (event.isCorrect) {
                                                correctAnswerCount += 1
                                            }
                                            coroutineScope.launch {
                                                recordQuizAnswerEvaluation(event)
                                            }
                                        },
                                        onQuizCompleted = {
                                            showQuizSheet = false
                                            activeQuizQuestions = emptyList()
                                            gameState = GameState.Navigating
                                        },
                                        onClose = {
                                            showQuizSheet = false
                                            activeQuizQuestions = emptyList()
                                            gameState = GameState.Navigating
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }

                GameState.Complete -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
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

                        Text(
                            text = if (isFinishingAttempt) "Punkte werden berechnet..." else "Punkte: $pointsEarned",
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

                        Text(
                            text = "Wie war dein Abenteuer?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        StarRatingInput(
                            label = "Schwierigkeit",
                            rating = difficultyRating,
                            onRatingChange = { difficultyRating = it },
                            enabled = !isSubmittingFeedback,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        StarRatingInput(
                            label = "Gesamteindruck",
                            rating = overallRating,
                            onRatingChange = { overallRating = it },
                            enabled = !isSubmittingFeedback,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = customFeedback,
                            onValueChange = { customFeedback = it },
                            label = { Text("Dein Feedback") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            enabled = !isSubmittingFeedback,
                        )

                        feedbackError?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                            )
                        }

                        FeedbackSubmitHint(
                            hasAttempt = currentAttempt != null,
                            hasFinishedAttempt = hasFinishedAttempt,
                            difficultyRating = difficultyRating,
                            overallRating = overallRating,
                            attemptError = attemptError,
                            finishError = finishError,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val attemptId = currentAttempt?.id ?: return@Button
                                coroutineScope.launch {
                                    isSubmittingFeedback = true
                                    feedbackError = null
                                    try {
                                        submitAdventureFeedback(
                                            AdventureFeedbackDraft(
                                                attemptId = attemptId,
                                                adventureId = adventure.id,
                                                userId = userId,
                                                difficultyRating = difficultyRating,
                                                overallRating = overallRating,
                                                customFeedback = customFeedback.trim().ifBlank { null },
                                            ),
                                        )
                                        onAdventureComplete(pointsEarned)
                                    } catch (e: Exception) {
                                        feedbackError = "Feedback konnte nicht gespeichert werden. Bitte versuche es erneut."
                                        println("Error submitting feedback: ${e.message}")
                                    } finally {
                                        isSubmittingFeedback = false
                                    }
                                }
                            },
                            enabled = hasFinishedAttempt && !isFinishingAttempt && !isSubmittingFeedback &&
                                currentAttempt != null && difficultyRating in 1..5 && overallRating in 1..5,
                        ) {
                            Text(if (isSubmittingFeedback) "Wird gesendet..." else "Feedback senden")
                        }

                        TextButton(
                            onClick = { onAdventureComplete(pointsEarned) },
                            enabled = !isSubmittingFeedback,
                        ) {
                            Text("Ohne Feedback zurück zur Liste")
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

@Composable
private fun FeedbackSubmitHint(
    hasAttempt: Boolean,
    hasFinishedAttempt: Boolean,
    difficultyRating: Int,
    overallRating: Int,
    attemptError: String?,
    finishError: String?,
) {
    val message = when {
        attemptError != null -> attemptError
        finishError != null -> finishError
        !hasAttempt -> "Feedback kann noch nicht gespeichert werden, weil der Abenteuer-Versuch geladen wird."
        !hasFinishedAttempt -> "Feedback kann gespeichert werden, sobald der Abschluss verarbeitet wurde."
        difficultyRating !in 1..5 || overallRating !in 1..5 -> "Bitte bewerte Schwierigkeit und Gesamteindruck mit Sternen."
        else -> null
    }

    message?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = it,
            color = if (attemptError != null || finishError != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun StarRatingInput(
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    enabled: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.Center) {
            (1..5).forEach { value ->
                Text(
                    text = if (value <= rating) "★" else "☆",
                    fontSize = 32.sp,
                    color = if (value <= rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable(enabled = enabled) { onRatingChange(value) },
                )
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
