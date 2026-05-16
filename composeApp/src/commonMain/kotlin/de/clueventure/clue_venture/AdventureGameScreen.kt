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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val CloseIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(18.3f, 5.71f)
            lineTo(12f, 12f)
            lineTo(18.3f, 18.29f)
            lineTo(16.89f, 19.7f)
            lineTo(10.59f, 13.41f)
            lineTo(4.29f, 19.71f)
            lineTo(2.88f, 18.3f)
            lineTo(9.17f, 12f)
            lineTo(2.88f, 5.7f)
            lineTo(4.29f, 4.29f)
            lineTo(10.59f, 10.59f)
            lineTo(16.89f, 4.3f)
            close()
        }
    }.build()
}

private const val PROXIMITY_ALERT_MAX_DISTANCE_METERS = 50.0
private const val WAYPOINT_REACHED_DISTANCE_METERS = 5.0

/**
 * Main Adventure Game Screen - orchestrates the complete adventure experience
 * with GPS tracking, waypoints, quiz, and points calculation
 */
data class AdventureGameCallbacks(
    val onAdventureComplete: (pointsEarned: Int) -> Unit,
    val onClose: () -> Unit,
)

//noinspection CognitiveComplexity
//noinspection LongParameterList
@Composable
fun AdventureGameScreen(
    adventure: Adventure,
    userId: String,
    currentLocation: GeoPoint?,
    initialAttempt: AdventureAttempt? = null,
    isAdventureComplete: Boolean = false,
    callbacks: AdventureGameCallbacks,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val adventureLocations = remember(adventure.id, adventure.locations) {
        adventure.locations.sortedBy { it.orderIndex }
    }

    // State management
    var gameState by remember { mutableStateOf<GameState>(GameState.Loading) }
    var currentAttempt by remember(initialAttempt?.id) { mutableStateOf(initialAttempt) }
    var currentCheckpointIndex by remember { mutableIntStateOf(0) }
    var currentLocationState by remember {
        mutableStateOf<GeoPointState?>(currentLocation?.let {
            GeoPointState(point = it, timestamp = "", accuracy = null)
        })
    }
    var totalQuizQuestionCount by remember { mutableIntStateOf(0) }
    var distanceToWaypoint by remember { mutableStateOf(Double.POSITIVE_INFINITY) }
    var currentRouteDistanceMeters by remember { mutableStateOf<Double?>(null) }
    var unlockedQuestionSlots by remember { mutableIntStateOf(0) }
    var pendingUnlockCheckpointIndex by remember { mutableStateOf<Int?>(null) }
    var pendingRouteDistanceMeters by remember { mutableStateOf<Double?>(null) }
    var expectingNewRouteDistanceForCheckpoint by remember { mutableStateOf(false) }
    var showQuizSheet by remember { mutableStateOf(false) }
    var currentQuizQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var currentQuizQuestionIndex by remember { mutableIntStateOf(0) }
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
    var showCloseConfirmation by remember { mutableStateOf(false) }

    val currentWaypoint = adventureLocations.getOrNull(currentCheckpointIndex)
    val isProximityAlertVisible = currentLocationState != null &&
        currentWaypoint != null &&
        distanceToWaypoint <= PROXIMITY_ALERT_MAX_DISTANCE_METERS
    val isWaypointReached = currentLocationState != null &&
        currentWaypoint != null &&
        distanceToWaypoint < WAYPOINT_REACHED_DISTANCE_METERS
    val availableQuestionCount = remember(totalQuizQuestionCount, unlockedQuestionSlots, answeredQuestionIds) {
        calculateAvailableQuestionCount(
            totalQuestions = totalQuizQuestionCount,
            unlockedQuestionSlots = unlockedQuestionSlots,
            answeredQuestionCount = answeredQuestionIds.size,
        )
    }
    val quizPointsEarned = correctAnswerCount * 100

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
                totalQuizQuestionCount = getQuizQuestionCount(adventure.id)

                gameState = when {
                    isAdventureComplete -> GameState.Complete
                    adventureLocations.isEmpty() -> GameState.Error
                    else -> GameState.Navigating
                }
            } catch (e: Exception) {
                println("Error initializing adventure: ${e.message}")
                attemptError =
                    "Feedback kann aktuell nicht gespeichert werden, weil kein Abenteuer-Versuch gefunden wurde."
                if (!isAdventureComplete) {
                    gameState = GameState.Error
                }
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

    LaunchedEffect(currentLocationState?.point, currentWaypoint?.point) {
        val location = currentLocationState?.point ?: run {
            distanceToWaypoint = Double.POSITIVE_INFINITY
            currentRouteDistanceMeters = null
            return@LaunchedEffect
        }
        val waypointPoint = currentWaypoint?.point ?: run {
            distanceToWaypoint = Double.POSITIVE_INFINITY
            currentRouteDistanceMeters = null
            return@LaunchedEffect
        }

        distanceToWaypoint = location.distanceTo(waypointPoint)
    }

    // Handle waypoint reached
    LaunchedEffect(isWaypointReached) {
        if (
            isWaypointReached &&
            gameState == GameState.Navigating &&
            currentCheckpointIndex != lastReachedCheckpointIndex
        ) {
            lastReachedCheckpointIndex = currentCheckpointIndex
            expectingNewRouteDistanceForCheckpoint = true
            currentRouteDistanceMeters = null
            pendingRouteDistanceMeters = null
            moveToNextCheckpoint(
                adventure,
                currentCheckpointIndex,
                { nextIndex ->
                    currentCheckpointIndex = nextIndex
                },
                { gameState = it },
                onCheckpointAdvanced = { nextIndex ->
                    pendingUnlockCheckpointIndex = nextIndex
                },
            )
        }
    }

    LaunchedEffect(currentRouteDistanceMeters, expectingNewRouteDistanceForCheckpoint) {
        if (expectingNewRouteDistanceForCheckpoint && currentRouteDistanceMeters != null) {
            pendingRouteDistanceMeters = currentRouteDistanceMeters
            expectingNewRouteDistanceForCheckpoint = false
        }
    }

    LaunchedEffect(pendingUnlockCheckpointIndex, currentCheckpointIndex, pendingRouteDistanceMeters) {
        val pendingIndex = pendingUnlockCheckpointIndex ?: return@LaunchedEffect
        if (pendingIndex != currentCheckpointIndex) {
            return@LaunchedEffect
        }

        val routeDistance = pendingRouteDistanceMeters ?: return@LaunchedEffect
        val unlockedQuestions = unlockedQuestionsForRouteDistance(routeDistance)
        if (unlockedQuestions > 0) {
            unlockedQuestionSlots += unlockedQuestions
        }
        pendingUnlockCheckpointIndex = null
        pendingRouteDistanceMeters = null
    }

    LaunchedEffect(isAdventureComplete) {
        if (isAdventureComplete) {
            currentCheckpointIndex = adventureLocations.lastIndex.coerceAtLeast(0)
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
                finishError =
                    "Das Abenteuer konnte nicht vollstaendig abgeschlossen werden. Feedback kann erst nach einem erfolgreichen Abschluss gespeichert werden."
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
                IconButton(
                    onClick = {
                        if (gameState == GameState.Complete && hasFinishedAttempt) {
                            callbacks.onAdventureComplete(pointsEarned)
                        } else {
                            showCloseConfirmation = true
                        }
                    },
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Abenteuer schließen",
                    )
                }
            }
        },
        modifier = modifier,
    ) { contentPadding ->
        if (showCloseConfirmation) {
            AlertDialog(
                onDismissRequest = { showCloseConfirmation = false },
                title = { Text("Abenteuer beenden?") },
                text = {
                    Text(
                        "Du hast dieses Abenteuer noch nicht abgeschlossen. " +
                            "Wenn du es jetzt beendest, wird dein aktueller Durchlauf geschlossen.",
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCloseConfirmation = false
                            callbacks.onClose()
                        },
                    ) {
                        Text("Beenden")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseConfirmation = false }) {
                        Text("Weiter spielen")
                    }
                },
            )
        }

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
                                        timestamp = "",
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
                            showQuestionsButton = true,
                            questionCount = availableQuestionCount,
                            onQuestionsClicked = {
                                if (showQuizSheet || availableQuestionCount <= 0) {
                                    return@PlatformMap
                                }

                                coroutineScope.launch {
                                    val questionIndex = answeredQuestionIds.size.coerceAtMost(totalQuizQuestionCount - 1)
                                    currentQuizQuestionIndex = questionIndex
                                    currentQuizQuestion = null
                                    showQuizSheet = true
                                    gameState = GameState.ShowingQuiz

                                    try {
                                        currentQuizQuestion = getQuizQuestionByIndex(adventure.id, questionIndex)
                                        if (currentQuizQuestion == null) {
                                            showQuizSheet = false
                                            gameState = GameState.Navigating
                                        }
                                    } catch (e: Exception) {
                                        println("Error loading quiz question: ${e.message}")
                                        showQuizSheet = false
                                        gameState = GameState.Navigating
                                    }
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
                            currentWaypoint?.let { waypoint ->
                                NavigationIndicator(
                                    currentLocation = currentLocationState?.point,
                                    targetLocation = waypoint.point,
                                    targetName = waypoint.name,
                                    quizPointsEarned = quizPointsEarned,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

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

                GameState.ShowingQuiz -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlatformMap(
                            modifier = Modifier.fillMaxSize(),
                            routeTargets = currentWaypoint?.point?.let { listOf(it) }.orEmpty(),
                            onCurrentLocationChanged = { location ->
                                val updatedLocation = location?.let {
                                    GeoPointState(
                                        point = it,
                                        timestamp = "",
                                    )
                                }
                                currentLocationState = updatedLocation

                                location?.let { currentLocation ->
                                    currentWaypoint?.point?.let { waypointPoint ->
                                        distanceToWaypoint = currentLocation.distanceTo(waypointPoint)
                                    }
                                }
                            },
                            showQuestionsButton = true,
                            questionCount = availableQuestionCount,
                            onQuestionsClicked = {
                                // Quiz popup is already open here; ignore additional taps.
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
                                            text = "Frage ${currentQuizQuestionIndex + 1}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        TextButton(
                                            onClick = {
                                                showQuizSheet = false
                                                currentQuizQuestion = null

                                                gameState = GameState.Navigating
                                            },
                                        ) {
                                            Text("Schliessen")
                                        }
                                    }

                                    if (currentQuizQuestion == null) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(16.dp),
                                        )
                                    } else {
                                        currentQuizQuestion?.let { question ->
                                            QuizScreen(
                                                questions = listOf(question),
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
                                                    currentQuizQuestion = null
                                                    gameState = GameState.Navigating
                                                },
                                                onClose = {
                                                    showQuizSheet = false
                                                    currentQuizQuestion = null
                                                    gameState = GameState.Navigating
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        } ?: Text(
                                            text = "Keine Frage verfügbar.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
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
                                        callbacks.onAdventureComplete(pointsEarned)
                                    } catch (e: Exception) {
                                        feedbackError =
                                            "Feedback konnte nicht gespeichert werden. Bitte versuche es erneut."
                                        println("Error submitting feedback: ${e.message}")
                                        e.printStackTrace()
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
                            onClick = { callbacks.onAdventureComplete(pointsEarned) },
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
                        Button(onClick = { callbacks.onClose() }) {
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

@Composable
private fun NavigationIndicator(
    currentLocation: GeoPoint?,
    targetLocation: GeoPoint,
    targetName: String,
    quizPointsEarned: Int,
    modifier: Modifier = Modifier,
) {
    val distanceText = currentLocation?.distanceTo(targetLocation)?.let { distanceMeters ->
        if (distanceMeters >= 1_000) {
            val kilometers = distanceMeters / 1_000.0
            "Entfernung zu $targetName: ${(kilometers * 10).toInt() / 10.0} km"
        } else {
            "Entfernung zu $targetName: ${distanceMeters.toInt()} m"
        }
    } ?: "Position wird ermittelt..."

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Nächstes Ziel",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = targetName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = distanceText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Quiz-Punkte: $quizPointsEarned",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// Game state enum
enum class GameState {
    Loading,
    Navigating,
    ShowingQuiz,
    Complete,
    Error,
}

// Helper function to move to next checkpoint
private fun moveToNextCheckpoint(
    adventure: Adventure,
    currentIndex: Int,
    updateIndex: (Int) -> Unit,
    updateGameState: (GameState) -> Unit,
    onCheckpointAdvanced: (Int) -> Unit,
) {
    val nextIndex = currentIndex + 1
    if (nextIndex >= adventure.locations.size) {
        // Adventure complete
        updateGameState(GameState.Complete)
    } else {
        // Move to next waypoint
        updateIndex(nextIndex)
        onCheckpointAdvanced(nextIndex)
        updateGameState(GameState.Navigating)
    }
}
