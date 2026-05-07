package de.clueventure.clue_venture

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

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
)

data class AdventureLocation(
    val name: String,
    val point: GeoPoint,
    val orderIndex: Int,
)

data class AdventureDraft(
    val title: String,
    val summary: String,
    val startPoint: GeoPoint,
    val difficulty: String?,
    val estimatedDurationMinutes: Int?,
    val locations: List<AdventureLocationDraft>,
)

data class AdventureMetadataDraft(
    val title: String,
    val summary: String,
    val startPoint: GeoPoint,
    val difficulty: String?,
    val estimatedDurationMinutes: Int?,
)

data class AdventureLocationDraft(
    val name: String,
    val point: GeoPoint,
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
    ProximityStatus.VERY_HOT -> "🔥 Sehr heiß!"
    ProximityStatus.HOT -> "🌡️ Heiß!"
    ProximityStatus.WARM -> "🟠 Wärmer werdend"
    ProximityStatus.COLD -> "🟡 Kälter werdend"
    ProximityStatus.VERY_COLD -> "❄️ Sehr kalt"
    ProximityStatus.UNREACHABLE -> "📍 Keine Position"
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
		id = "old-town-mystery",
		title = "Geheimnis der Altstadt",
		summary = "Spaziere durch die Altstadtgassen und folge den ersten Spuren.",
		startPoint = GeoPoint(latitude = 52.5209, longitude = 13.4095),
	),
	Adventure(
		id = "harbor-trail",
		title = "Hafenpfad",
		summary = "Ein Abenteuer zwischen Wasser, Kaimauern und versteckten Hinweisen.",
		startPoint = GeoPoint(latitude = 52.5141, longitude = 13.3567),
	),
	Adventure(
		id = "city-park-code",
		title = "Code im Stadtpark",
		summary = "Knacke die Rätsel an den Wegen und finde den nächsten Treffpunkt.",
		startPoint = GeoPoint(latitude = 52.5018, longitude = 13.4471),
	),
	Adventure(
		id = "museum-chase",
		title = "Museum Chase",
		summary = "Eine kurze Jagd mit einem Startpunkt in der Nähe der Museumsinsel.",
		startPoint = GeoPoint(latitude = 52.5169, longitude = 13.4010),
	),
	Adventure(
		id = "street-art-hunt",
		title = "Street Art Jagd",
		summary = "Entdecke die verborgenen Kunstwerke und finde den nächsten Hinweis.",
		startPoint = GeoPoint(latitude = 48.44337, longitude = 8.68579),
	),
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

// ============================================================================
// USER & AUTHENTICATION DOMAIN MODELS
// ============================================================================

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

// ============================================================================
// POINTS CALCULATION LOGIC
// ============================================================================

/**
 * Calculate points earned for completing an adventure
 * @param timeSpentSeconds Time spent in seconds
 * @param estimatedMinutes Estimated duration in minutes
 * @param correctAnswerCount Number of correct quiz answers
 * @return Total points earned
 */
fun calculateAdventurePoints(
    timeSpentSeconds: Int,
    estimatedMinutes: Int,
    correctAnswerCount: Int,
): Int {
    val estimatedSeconds = estimatedMinutes * 60
    val timeRatio = timeSpentSeconds.toDouble() / estimatedSeconds
    val timeMultiplier = (1.0 - timeRatio.coerceIn(0.0, 1.0))
    val basePoints = (1000 * timeMultiplier).toInt()
    val quizBonus = correctAnswerCount * 100
    return maxOf(basePoints + quizBonus, 100)
}
