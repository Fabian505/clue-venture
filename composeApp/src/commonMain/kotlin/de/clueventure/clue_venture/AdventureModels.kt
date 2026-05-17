package de.clueventure.clue_venture

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.serialization.Serializable

@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class Adventure(
    val id: String,
    val title: String,
    val summary: String,
    val startPoint: GeoPoint,
    val difficulty: String? = null,
    val estimatedDurationMinutes: Int? = null,
    val locations: List<AdventureLocation> = emptyList(),
    val completionPoints: Int = 0,
    val locationCount: Int = 0,
    val isPublic: Boolean = false,
    val createdBy: String? = null,
)

data class AdventureLocation(
    val id: Long = -1L,
    val name: String,
    val point: GeoPoint,
    val orderIndex: Int,
    val pointValue: Int = 0,
    val timeLimitSeconds: Int? = null,
    val hints: List<Hint> = emptyList(),
)

data class AdventureDraft(
    val title: String,
    val summary: String,
    val startPoint: GeoPoint,
    val difficulty: String?,
    val estimatedDurationMinutes: Int?,
    val locations: List<AdventureLocationDraft>,
    val quizQuestions: List<QuizQuestionDraft> = emptyList(),
)

data class AdventureMetadataDraft(
    val title: String,
    val summary: String,
    val startPoint: GeoPoint,
    val difficulty: String?,
    val estimatedDurationMinutes: Int?,
    val isPublic: Boolean = false,
)

data class AdventureLocationDraft(
    val name: String,
    val point: GeoPoint,
    val pointValue: Int = 0,
    val timeLimitSeconds: Int? = null,
    val hints: List<HintDraft> = emptyList(),
)

data class HintDraft(
    val hintIndex: Int,
    val text: String,
    val pointCost: Int = 0,
    val imageUrl: String? = null,
)

data class QuizAnswerDraft(
    val answerText: String,
    val isCorrect: Boolean,
    val answerOrder: Int,
)

data class QuizQuestionDraft(
    val questionText: String,
    val answers: List<QuizAnswerDraft>,
)

fun GeoPoint.distanceTo(other: GeoPoint): Double {
    val earthRadiusMeters = 6_371_000.0
    val latitudeDistance = (other.latitude - latitude) * PI / 180.0
    val longitudeDistance = (other.longitude - longitude) * PI / 180.0
    val startLatitude = latitude * PI / 180.0
    val endLatitude = other.latitude * PI / 180.0

    val a = sin(latitudeDistance / 2).pow(2) +
        sin(longitudeDistance / 2).pow(2) * cos(startLatitude) * cos(endLatitude)
    val c = 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))

    return earthRadiusMeters * c
}

// ============================================================================
// PROXIMITY STATUS - for Hot/Cold feedback
// ============================================================================

enum class ProximityStatus {
    VERY_HOT,      // < 5 meters
    HOT,           // 5-15 meters
    WARM,          // 15-30 meters
    COLD,          // 30-50 meters
    VERY_COLD,     // > 50 meters
    UNREACHABLE,   // No location fix
}

fun GeoPoint.proximityStatus(targetPoint: GeoPoint, maxRangeMeters: Double = 50.0): ProximityStatus {
    val distance = distanceTo(targetPoint)
    return when {
        distance < 5 -> ProximityStatus.VERY_HOT
        distance < 15 -> ProximityStatus.HOT
        distance < 30 -> ProximityStatus.WARM
        distance < maxRangeMeters -> ProximityStatus.COLD
        else -> ProximityStatus.VERY_COLD
    }
}

fun ProximityStatus.getDisplayText(): String = when (this) {
    ProximityStatus.VERY_HOT -> "Sehr Heiß!"
    ProximityStatus.HOT -> "Heiß!"
    ProximityStatus.WARM -> "Warm!"
    ProximityStatus.COLD -> "Kalt!"
    ProximityStatus.VERY_COLD -> "Sehr Kalt!"
    ProximityStatus.UNREACHABLE -> "Keine Position"
}

fun ProximityStatus.getHexColor(): String = when (this) {
    ProximityStatus.VERY_HOT -> "#FF0000"    // Red
    ProximityStatus.HOT -> "#FF6600"         // Dark Orange
    ProximityStatus.WARM -> "#FFB700"        // Orange
    ProximityStatus.COLD -> "#FFC700"        // Lighter Orange
    ProximityStatus.VERY_COLD -> "#0066FF"   // Blue
    ProximityStatus.UNREACHABLE -> "#999999" // Gray
}

val sampleAdventures = listOf(
	Adventure(
        id = "1",
        title = "Geheimnis der Altstadt",
        summary = "Spaziere durch die Altstadtgassen und folge den ersten Spuren.",
        startPoint = GeoPoint(latitude = 52.5209, longitude = 13.4095),
        locations = listOf(
            AdventureLocation(
                name = "Nikolaiviertel",
                point = GeoPoint(latitude = 52.5186, longitude = 13.4067),
                orderIndex = 0
            ),
            AdventureLocation(
                name = "Altes Stadthaus",
                point = GeoPoint(latitude = 52.5168, longitude = 13.4094),
                orderIndex = 1
            ),
        ),
    ),
    Adventure(
        id = "2",
        title = "Hafenpfad",
        summary = "Ein Abenteuer zwischen Wasser, Kaimauern und versteckten Hinweisen.",
        startPoint = GeoPoint(latitude = 52.5141, longitude = 13.3567),
        locations = listOf(
            AdventureLocation(
                name = "Spreeufer",
                point = GeoPoint(latitude = 52.5137, longitude = 13.3546),
                orderIndex = 0
            ),
            AdventureLocation(
                name = "Anleger Ost",
                point = GeoPoint(latitude = 52.5121, longitude = 13.3587),
                orderIndex = 1
            ),
        ),
    ),
    Adventure(
        id = "3",
        title = "Code im Stadtpark",
        summary = "Knacke die Rätsel an den Wegen und finde den nächsten Treffpunkt.",
        startPoint = GeoPoint(latitude = 52.5018, longitude = 13.4471),
        locations = listOf(
            AdventureLocation(
                name = "Nordtor Park",
                point = GeoPoint(latitude = 52.5035, longitude = 13.4445),
                orderIndex = 0
            ),
            AdventureLocation(
                name = "Seepavillon",
                point = GeoPoint(latitude = 52.5009, longitude = 13.4489),
                orderIndex = 1
            ),
            AdventureLocation(
                name = "Südeingang",
                point = GeoPoint(latitude = 52.4987, longitude = 13.4461),
                orderIndex = 2
            ),
        ),
    ),
    Adventure(
        id = "4",
        title = "Museum Chase",
        summary = "Eine kurze Jagd mit einem Startpunkt in der Nähe der Museumsinsel.",
        startPoint = GeoPoint(latitude = 52.5169, longitude = 13.4010),
    ),
    Adventure(
        id = "5",
        title = "Street Art Jagd",
        summary = "Entdecke die verborgenen Kunstwerke und finde den nächsten Hinweis.",
        startPoint = GeoPoint(latitude = 48.44337, longitude = 8.68579),
    ),
)

// ============================================================================
// HINT DOMAIN MODEL
// ============================================================================

data class Hint(
    val id: Long,
    val locationId: Long,
    val hintIndex: Int,     // 0 = free (shown on arrival), 1–2 = costs points
    val text: String,
    val pointCost: Int = 0,
    val imageUrl: String? = null,
)

// ============================================================================
// QUIZ DOMAIN MODELS
// ============================================================================

data class QuizQuestion(
    val id: Long,
    val adventureId: String,
    val questionText: String,
    val orderIndex: Int,
    val answers: List<QuizAnswer> = emptyList(),
)

data class QuizAnswer(
    val id: Long,
    val questionId: Long,
    val answerText: String,
    val isCorrect: Boolean,
    val answerOrder: Int,
)

data class QuizAnswerEvaluationEvent(
    val attemptId: Long?,
    val questionId: Long,
    val answerId: Long,
    val isCorrect: Boolean,
)

// ============================================================================
// USER & AUTHENTICATION DOMAIN MODELS
// ============================================================================

@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class UserProfile(
    val userId: String,
    val totalPoints: Int = 0,
    val adventuresCompleted: Int = 0,
    val adventuresStarted: Int = 0,
    val lastUpdated: String,
)

fun generateUserId(): String {
    val hexChars = "0123456789abcdef"

    fun segment(length: Int): String = buildString(length) {
        repeat(length) {
            append(hexChars[Random.nextInt(hexChars.length)])
        }
    }

    return listOf(8, 4, 4, 4, 12).joinToString("-") { segment(it) }
}

// ============================================================================
// ADVENTURE ATTEMPT & PROGRESS DOMAIN MODELS
// ============================================================================

data class AdventureAttempt(
    val id: Long,
    val adventureId: String,
    val userId: String,
    val startedAt: String,
    val completedAt: String?,
    val startedCheckpointIndex: Int = 0,
    val currentCheckpointIndex: Int = 0,
    val pointsEarned: Int = 0,
    val timeSpentSeconds: Int? = null,
    val isCompleted: Boolean = false,
)

data class UserAnswer(
    val id: Long,
    val attemptId: Long,
    val questionId: Long,
    val answerId: Long,
    val isCorrect: Boolean,
    val answeredAt: String,
)

data class UserProgress(
    val id: Long,
    val attemptId: Long,
    val currentCheckpointIndex: Int = 0,
    val lastLocation: GeoPoint?,
    val lastLocationUpdate: String?,
    val reachedAt: String,
    val updatedAt: String,
)

data class GeoPointState(
    val point: GeoPoint,
    val timestamp: String,
    val accuracy: Float? = null,
)

data class AdventureFeedback(
    val id: Long,
    val attemptId: Long,
    val adventureId: String,
    val userId: String,
    val difficultyRating: Int,
    val overallRating: Int,
    val customFeedback: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class AdventureFeedbackDraft(
    val attemptId: Long,
    val adventureId: String,
    val userId: String,
    val difficultyRating: Int,
    val overallRating: Int,
    val customFeedback: String?,
)

// ============================================================================
// CONVERSION FUNCTIONS
// ============================================================================

fun QuizQuestionEntity.toQuizQuestion(answers: List<QuizAnswer> = emptyList()): QuizQuestion =
    QuizQuestion(
        id = id,
        adventureId = adventureId.toString(),
        questionText = questionText,
        orderIndex = orderIndex,
        answers = answers,
    )

fun QuizAnswerEntity.toQuizAnswer(): QuizAnswer =
    QuizAnswer(
        id = id,
        questionId = questionId,
        answerText = answerText,
        isCorrect = isCorrect,
        answerOrder = answerOrder,
    )

fun UserEntity.toUser(): User =
    User(
        id = id,
        email = email,
        username = username,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun UserProfileEntity.toUserProfile(): UserProfile =
    UserProfile(
        userId = userId,
        totalPoints = totalPoints,
        adventuresCompleted = adventuresCompleted,
        adventuresStarted = adventuresStarted,
        lastUpdated = lastUpdated,
    )

fun AdventureAttemptEntity.toAdventureAttempt(): AdventureAttempt =
    AdventureAttempt(
        id = id,
        adventureId = adventureId.toString(),
        userId = userId,
        startedAt = startedAt,
        completedAt = completedAt,
        startedCheckpointIndex = startedCheckpointIndex,
        currentCheckpointIndex = currentCheckpointIndex,
        pointsEarned = pointsEarned ?: 0,
        timeSpentSeconds = timeSpentSeconds,
        isCompleted = isCompleted,
    )

fun UserProgressEntity.toUserProgress(): UserProgress =
    UserProgress(
        id = id,
        attemptId = attemptId,
        currentCheckpointIndex = currentCheckpointIndex,
        lastLocation = if (lastLocationLatitude != null && lastLocationLongitude != null) {
            GeoPoint(latitude = lastLocationLatitude, longitude = lastLocationLongitude)
        } else null,
        lastLocationUpdate = lastLocationUpdate,
        reachedAt = reachedAt,
        updatedAt = updatedAt,
    )

fun AdventureFeedbackEntity.toAdventureFeedback(): AdventureFeedback =
    AdventureFeedback(
        id = id,
        attemptId = attemptId,
        adventureId = adventureId.toString(),
        userId = userId,
        difficultyRating = difficultyRating,
        overallRating = overallRating,
        customFeedback = customFeedback,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

// ============================================================================
// POINTS CALCULATION LOGIC
// ============================================================================

/**
 * Returns the effective time limit in seconds for a checkpoint leg.
 *
 * If [manualTimeLimitSeconds] is set on the location it is used as-is (manual
 * override). Otherwise the limit is derived from the straight-line distance
 * between [previousPoint] and [currentPoint] divided by
 * [walkingSpeedMetersPerSecond] (default 1.2 m/s = slow walking pace, giving
 * a generous limit that scales with actual leg length).
 */
fun effectiveTimeLimitSeconds(
    manualTimeLimitSeconds: Int?,
    previousPoint: GeoPoint,
    currentPoint: GeoPoint,
    walkingSpeedMetersPerSecond: Double = 1.2,
): Int {
    if (manualTimeLimitSeconds != null) return manualTimeLimitSeconds
    val distanceMeters = previousPoint.distanceTo(currentPoint)
    return (distanceMeters / walkingSpeedMetersPerSecond).toInt().coerceAtLeast(1)
}

/**
 * Calculate completion bonus points for finishing an adventure.
 * Quiz points are awarded live per correct answer and are NOT included here
 * to avoid double-counting.
 * @param timeSpentSeconds Time spent in seconds
 * @param estimatedMinutes Estimated duration in minutes
 * @return Time-based completion bonus (0–1000), minimum 100
 */
fun calculateAdventurePoints(
    timeSpentSeconds: Int,
    estimatedMinutes: Int,
): Int {
    val estimatedSeconds = estimatedMinutes * 60
    val timeRatio = timeSpentSeconds.toDouble() / estimatedSeconds
    val timeMultiplier = (1.0 - timeRatio.coerceIn(0.0, 1.0))
    val basePoints = (1000 * timeMultiplier).toInt()
    return maxOf(basePoints, 100)
}

fun unlockedQuestionsForRouteDistance(routeDistanceMeters: Double?): Int {
    if (routeDistanceMeters == null || routeDistanceMeters <= 0.0) {
        return 0
    }

    return floor(routeDistanceMeters / 200.0).toInt().coerceAtLeast(0)
}

fun updateUnlockedQuestionSlots(
    currentUnlockedQuestionSlots: Int,
    routeDistanceMeters: Double?,
): Int {
    return maxOf(currentUnlockedQuestionSlots, unlockedQuestionsForRouteDistance(routeDistanceMeters))
}

/**
 * Calculate points earned for reaching a checkpoint, applying a time penalty
 * if elapsed time exceeds the location's time limit.
 *
 * - No timeLimitSeconds → full basePoints, no penalty.
 * - elapsed ≤ timeLimitSeconds → full basePoints.
 * - elapsed > timeLimitSeconds → linear decay down to 50% floor at 2× the limit,
 *   clamped so the result is never below basePoints / 2.
 */
fun calculateCheckpointPoints(
    basePoints: Int,
    elapsedSeconds: Int,
    timeLimitSeconds: Int?,
): Int {
    if (timeLimitSeconds == null || elapsedSeconds <= timeLimitSeconds) return basePoints
    val overtime = elapsedSeconds - timeLimitSeconds
    val decayFraction = (overtime.toDouble() / timeLimitSeconds).coerceIn(0.0, 1.0)
    val reduced = (basePoints * (1.0 - 0.5 * decayFraction)).toInt()
    return maxOf(reduced, basePoints / 2)
}

fun calculateAvailableQuestionCount(
    totalQuestions: Int,
    unlockedQuestionSlots: Int,
    answeredQuestionCount: Int,
): Int {
    // Show total available questions (not yet answered)
    // unlockedQuestionSlots is not considered here - all questions are available from the start
    return maxOf(0, totalQuestions - answeredQuestionCount)
}

enum class LeaderboardPeriod { ALL, WEEK, MONTH }