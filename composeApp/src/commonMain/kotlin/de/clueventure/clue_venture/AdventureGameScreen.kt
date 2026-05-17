package de.clueventure.clue_venture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filter
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
    snackbarHostState: SnackbarHostState,
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
    var checkpointPointsEarned by remember { mutableIntStateOf(0) }
    var hasFinishedAttempt by remember { mutableStateOf(false) }
    var isFinishingAttempt by remember { mutableStateOf(false) }
    var isCancellingAttempt by remember { mutableStateOf(false) }
    var isAdventureCancelled by remember { mutableStateOf(false) }
    var difficultyRating by remember { mutableIntStateOf(0) }
    var overallRating by remember { mutableIntStateOf(0) }
    var customFeedback by remember { mutableStateOf("") }
    var isSubmittingFeedback by remember { mutableStateOf(false) }
    var feedbackError by remember { mutableStateOf<String?>(null) }
    var attemptError by remember { mutableStateOf<String?>(null) }
    var finishError by remember { mutableStateOf<String?>(null) }
    var showCloseConfirmation by remember { mutableStateOf(false) }
    var userPoints by remember { mutableIntStateOf(0) }
    var boughtHintIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var checkpointStartTime by remember { mutableStateOf(currentTimeMillis()) }
    var hint0Popup by remember { mutableStateOf<Hint?>(null) }
    var showHintsSheet by remember { mutableStateOf(false) }

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

                // Show hint 0 of the first checkpoint immediately at game start
                if (gameState == GameState.Navigating) {
                    adventureLocations.getOrNull(0)?.hints?.find { it.hintIndex == 0 }?.let {
                        hint0Popup = it
                    }
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
        currentRouteDistanceMeters = distanceToWaypoint
    }

    // Handle waypoint reached — snapshotFlow re-evaluates on every state change so we
    // never miss the gameState Loading→Navigating transition when isWaypointReached is
    // already true (a LaunchedEffect keyed on a boolean cannot detect that transition).
    LaunchedEffect(Unit) {
        snapshotFlow {
            val wpt = adventureLocations.getOrNull(currentCheckpointIndex)
            currentLocationState != null && wpt != null &&
                distanceToWaypoint < WAYPOINT_REACHED_DISTANCE_METERS &&
                gameState == GameState.Navigating
        }.filter { it }
         .collect {
            if (currentCheckpointIndex == lastReachedCheckpointIndex) return@collect
            val idx = currentCheckpointIndex
            val waypoint = adventureLocations.getOrNull(idx) ?: return@collect
            lastReachedCheckpointIndex = idx

            if (waypoint.pointValue > 0) {
                val elapsedSeconds = ((currentTimeMillis() - checkpointStartTime) / 1000).toInt()
                val previousPoint = if (idx == 0) {
                    adventure.startPoint
                } else {
                    adventureLocations.getOrNull(idx - 1)?.point ?: adventure.startPoint
                }
                val timeLimit = effectiveTimeLimitSeconds(
                    manualTimeLimitSeconds = waypoint.timeLimitSeconds,
                    previousPoint = previousPoint,
                    currentPoint = waypoint.point,
                )
                val awarded = calculateCheckpointPoints(
                    basePoints = waypoint.pointValue,
                    elapsedSeconds = elapsedSeconds,
                    timeLimitSeconds = timeLimit,
                )
                checkpointPointsEarned += awarded
                coroutineScope.launch {
                    runCatching { updateUserPoints(userId, awarded) }
                    userPoints = runCatching { getUserPoints(userId) }.getOrDefault(userPoints)
                }
            }

            checkpointStartTime = currentTimeMillis()

            adventureLocations.getOrNull(idx + 1)?.hints?.find { it.hintIndex == 0 }?.let { hint0 ->
                hint0Popup = hint0
            }

            expectingNewRouteDistanceForCheckpoint = true
            currentRouteDistanceMeters = null
            pendingRouteDistanceMeters = null
            moveToNextCheckpoint(
                adventure,
                idx,
                { nextIndex -> currentCheckpointIndex = nextIndex },
                { gs -> gameState = gs },
                onCheckpointAdvanced = { nextIndex -> pendingUnlockCheckpointIndex = nextIndex },
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

    // Load the user's current point balance once on entry
    LaunchedEffect(userId) {
        userPoints = runCatching { getUserPoints(userId) }.getOrDefault(0)
    }

    // Reset bought hints and hints sheet whenever we move to a new checkpoint
    LaunchedEffect(currentCheckpointIndex) {
        boughtHintIndices = emptySet()
        showHintsSheet = false
    }

    LaunchedEffect(gameState, currentAttempt?.id) {
        val attemptId = currentAttempt?.id
        if (
            gameState == GameState.Complete &&
            attemptId != null &&
            !hasFinishedAttempt &&
            !isFinishingAttempt &&
            !isAdventureCancelled
        ) {
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

    fun leaveFeedbackScreen() {
        if (isAdventureCancelled) {
            callbacks.onClose()
        } else {
            callbacks.onAdventureComplete(pointsEarned)
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
                            leaveFeedbackScreen()
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
                            "Wenn du es jetzt beendest, kannst du danach noch Feedback geben.",
                    )
                    finishError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val attemptId = currentAttempt?.id
                            if (attemptId == null) {
                                showCloseConfirmation = false
                                callbacks.onClose()
                                return@Button
                            }

                            coroutineScope.launch {
                                isCancellingAttempt = true
                                finishError = null
                                try {
                                    cancelAdventureAttempt(attemptId)
                                    showCloseConfirmation = false
                                    isAdventureCancelled = true
                                    hasFinishedAttempt = true
                                    pointsEarned = 0
                                    showQuizSheet = false
                                    currentQuizQuestion = null
                                    gameState = GameState.Complete
                                } catch (e: Exception) {
                                    finishError =
                                        "Das Abenteuer konnte nicht beendet werden. Bitte versuche es erneut."
                                    println("Error cancelling adventure: ${e.message}")
                                } finally {
                                    isCancellingAttempt = false
                                }
                            }
                        },
                        enabled = !isCancellingAttempt,
                    ) {
                        Text(if (isCancellingAttempt) "Wird beendet..." else "Beenden")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCloseConfirmation = false },
                        enabled = !isCancellingAttempt,
                    ) {
                        Text("Weiter spielen")
                    }
                },
            )
        }

        hint0Popup?.let { hint ->
            var showFullscreenHint0 by remember { mutableStateOf(false) }
            if (showFullscreenHint0) {
                hint.imageUrl?.let { url ->
                    FullscreenImageDialog(url = url, onDismiss = { showFullscreenHint0 = false })
                }
            }
            AlertDialog(
                onDismissRequest = { hint0Popup = null },
                title = { Text("Hinweis") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (hint.text.isNotBlank()) Text(hint.text)
                        if (hint.imageUrl != null) {
                            TextButton(
                                onClick = { showFullscreenHint0 = true },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text("🖼 Bild anzeigen", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { hint0Popup = null }) {
                        Text("Ok")
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
                            onCurrentLocationChanged = { location ->
                                val updatedLocation = location?.let {
                                    GeoPointState(
                                        point = it,
                                        timestamp = "",
                                    )
                                }
                                currentLocationState = updatedLocation

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
                            showHintsButton = currentWaypoint?.hints?.isNotEmpty() == true,
                            onHintsClicked = { showHintsSheet = true },
                            onQuestionsClicked = {
                                if (showQuizSheet || availableQuestionCount <= 0) {
                                    return@PlatformMap
                                }

                                coroutineScope.launch {
                                    val questionIndex = answeredQuestionIds.size.coerceAtMost(totalQuizQuestionCount - 1)
                                    currentQuizQuestionIndex = questionIndex
                                    currentQuizQuestion = null
                                    showQuizSheet = true
                                    showHintsSheet = false
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
                                        snackbarHostState.showSnackbar(
                                            "Quiz-Frage konnte nicht geladen werden.",
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short,
                                        )
                                    }
                                }
                            },
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            currentWaypoint?.let { waypoint ->
                                NavigationIndicator(
                                    currentLocation = currentLocationState?.point,
                                    targetLocation = waypoint.point,
                                    targetName = waypoint.name,
                                    quizPointsEarned = quizPointsEarned,
                                    userPoints = userPoints,
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

                        if (showHintsSheet) {
                            currentWaypoint?.let { waypoint ->
                                HintBottomSheet(
                                    hints = waypoint.hints,
                                    boughtHintIndices = boughtHintIndices,
                                    userPoints = userPoints,
                                    onBuyHint = { hint ->
                                        coroutineScope.launch {
                                            runCatching {
                                                updateUserPoints(userId, -hint.pointCost)
                                                boughtHintIndices = boughtHintIndices + hint.hintIndex
                                                userPoints = runCatching { getUserPoints(userId) }.getOrDefault(userPoints)
                                            }.onFailure { e ->
                                                println("Error buying hint ${hint.hintIndex}: ${e.message}")
                                                snackbarHostState.showSnackbar(
                                                    "Hint konnte nicht gekauft werden. Bitte versuche es erneut.",
                                                    withDismissAction = true,
                                                    duration = SnackbarDuration.Short,
                                                )
                                            }
                                        }
                                    },
                                    onDismiss = { showHintsSheet = false },
                                )
                            }
                        }
                    }
                }

                GameState.ShowingQuiz -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlatformMap(
                            modifier = Modifier.fillMaxSize(),
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
                                        currentRouteDistanceMeters = distanceToWaypoint
                                    }
                                }
                            },
                            showQuestionsButton = true,
                            questionCount = availableQuestionCount,
                            onQuestionsClicked = {
                                // Quiz popup is already open here; ignore additional taps.
                            },
                            showHintsButton = false,
                            onHintsClicked = {},
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
                                        IconButton(
                                            onClick = {
                                                showQuizSheet = false
                                                currentQuizQuestion = null

                                                gameState = GameState.Navigating
                                            },
                                        ) {
                                            Icon(
                                                imageVector = CloseIcon,
                                                contentDescription = "Frage schließen",
                                            )
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
                                                        coroutineScope.launch {
                                                            runCatching { updateUserPoints(userId, PUZZLE_POINTS_PER_CORRECT_ANSWER) }
                                                            userPoints = runCatching { getUserPoints(userId) }.getOrDefault(userPoints)
                                                        }
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
                            text = if (isAdventureCancelled) "Abenteuer beendet" else "Abenteuer abgeschlossen!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isAdventureCancelled) {
                            Text(
                                text = "Danke, dass du das Abenteuer ausprobiert hast.",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            AdventurePointsSummary(
                                checkpointPoints = checkpointPointsEarned,
                                quizPoints = correctAnswerCount * PUZZLE_POINTS_PER_CORRECT_ANSWER,
                                completionBonus = pointsEarned,
                                isLoading = isFinishingAttempt,
                            )
                        }

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
                                        leaveFeedbackScreen()
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
private fun AdventurePointsSummary(
    checkpointPoints: Int,
    quizPoints: Int,
    completionBonus: Int,
    isLoading: Boolean,
) {
    val totalPoints = checkpointPoints + quizPoints + completionBonus

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Deine Punkte",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            PointsRow("Checkpoints", checkpointPoints)
            PointsRow("Quiz", quizPoints)

            if (isLoading) {
                PointsRow("Abschlussbonus", null)
            } else {
                PointsRow("Abschlussbonus", completionBonus)
            }

            androidx.compose.material3.HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Gesamt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "$totalPoints Pkt.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun PointsRow(label: String, points: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = if (points != null) "$points Pkt." else "...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
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
    userPoints: Int,
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
            Text(
                text = "Guthaben: $userPoints Punkte",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

private const val PUZZLE_POINTS_PER_CORRECT_ANSWER = 100

@Composable
private fun FullscreenImageDialog(url: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            coil3.compose.AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            ) {
                Icon(CloseIcon, contentDescription = "Schließen", tint = Color.White)
            }
        }
    }
}

@Composable
private fun HintContent(hint: Hint) {
    var showFullscreen by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (hint.text.isNotBlank()) {
            Text(
                text = hint.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (hint.imageUrl != null) {
            TextButton(
                onClick = { showFullscreen = true },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text("🖼 Bild anzeigen", style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    if (showFullscreen) {
        hint.imageUrl?.let { url ->
            FullscreenImageDialog(url = url, onDismiss = { showFullscreen = false })
        }
    }
}

@Composable
private fun HintBottomSheet(
    hints: List<Hint>,
    boughtHintIndices: Set<Int>,
    userPoints: Int,
    onBuyHint: (Hint) -> Unit,
    onDismiss: () -> Unit,
) {
    val hint0 = hints.find { it.hintIndex == 0 }
    val hint1 = hints.find { it.hintIndex == 1 }
    val hint2 = hints.find { it.hintIndex == 2 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onDismiss),
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .clickable {},
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp),
                        )
                        .size(width = 40.dp, height = 4.dp),
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Hinweise", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onDismiss) { Text("Schließen") }
                }

                Spacer(Modifier.height(8.dp))

                hint0?.let {
                    HintContent(hint = it)
                }

                hint1?.let { hint ->
                    Spacer(Modifier.height(12.dp))
                    if (1 in boughtHintIndices) {
                        HintContent(hint = hint)
                    } else {
                        Button(
                            onClick = { onBuyHint(hint) },
                            enabled = userPoints >= hint.pointCost,
                        ) {
                            Text("Hinweis 1 kaufen (${hint.pointCost} Punkte)")
                        }
                    }
                }

                hint2?.let { hint ->
                    Spacer(Modifier.height(12.dp))
                    if (2 in boughtHintIndices) {
                        HintContent(hint = hint)
                    } else {
                        Button(
                            onClick = { onBuyHint(hint) },
                            enabled = userPoints >= hint.pointCost,
                        ) {
                            Text("Hinweis 2 kaufen (${hint.pointCost} Punkte)")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
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
