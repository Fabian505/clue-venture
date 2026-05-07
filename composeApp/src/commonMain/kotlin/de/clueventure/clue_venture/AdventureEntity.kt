package de.clueventure.clue_venture

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventureEntity(
    val id: Long,
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
)

@Serializable
data class AdventureInsertEntity(
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
)

@Serializable
data class AdventureUpdateEntity(
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
)

@Serializable
data class AdventureLocationInsertEntity(
    @SerialName("adventure_id") val adventureId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("order_index") val orderIndex: Int,
)

@Serializable
data class AdventureLocationEntity(
    @SerialName("adventure_id") val adventureId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("order_index") val orderIndex: Int,
)

@Serializable
data class AdventureLocationOrderUpdateEntity(
    @SerialName("order_index") val orderIndex: Int,
)

// ============================================================================
// QUIZ ENTITIES
// ============================================================================

@Serializable
data class QuizQuestionEntity(
    val id: Long,
    @SerialName("adventure_id") val adventureId: Long,
    @SerialName("question_text") val questionText: String,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("question_type") val questionType: String = "multiple_choice",
)

@Serializable
data class QuizAnswerEntity(
    val id: Long,
    @SerialName("question_id") val questionId: Long,
    @SerialName("answer_text") val answerText: String,
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("answer_order") val answerOrder: Int,
)

// ============================================================================
// USER & AUTHENTICATION ENTITIES
// ============================================================================

@Serializable
data class UserEntity(
    val id: String,
    val email: String,
    val username: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class UserProfileEntity(
    @SerialName("user_id") val userId: String,
    @SerialName("total_points") val totalPoints: Int = 0,
    @SerialName("adventures_completed") val adventuresCompleted: Int = 0,
    @SerialName("adventures_started") val adventuresStarted: Int = 0,
    @SerialName("last_updated") val lastUpdated: String,
)

// ============================================================================
// ADVENTURE ATTEMPT ENTITIES
// ============================================================================

@Serializable
data class AdventureAttemptEntity(
    val id: Long,
    @SerialName("adventure_id") val adventureId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("completed_at") val completedAt: String?,
    @SerialName("started_checkpoint_index") val startedCheckpointIndex: Int = 0,
    @SerialName("current_checkpoint_index") val currentCheckpointIndex: Int = 0,
    @SerialName("points_earned") val pointsEarned: Int? = 0,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int?,
    @SerialName("is_completed") val isCompleted: Boolean = false,
)

@Serializable
data class UserAnswerEntity(
    val id: Long,
    @SerialName("attempt_id") val attemptId: Long,
    @SerialName("question_id") val questionId: Long,
    @SerialName("answer_id") val answerId: Long,
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("answered_at") val answeredAt: String,
)

@Serializable
data class UserProgressEntity(
    val id: Long,
    @SerialName("attempt_id") val attemptId: Long,
    @SerialName("current_checkpoint_index") val currentCheckpointIndex: Int = 0,
    @SerialName("last_location_latitude") val lastLocationLatitude: Double?,
    @SerialName("last_location_longitude") val lastLocationLongitude: Double?,
    @SerialName("last_location_update") val lastLocationUpdate: String?,
    @SerialName("reached_at") val reachedAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

fun AdventureEntity.toAdventure(): Adventure = Adventure(
    id = id.toString(),
    title = title,
    summary = summary,
    startPoint = GeoPoint(latitude = startLatitude, longitude = startLongitude),
    difficulty = difficulty,
    estimatedDurationMinutes = estimatedDurationMinutes,
)

fun AdventureLocationEntity.toAdventureLocation(): AdventureLocation = AdventureLocation(
    name = name,
    point = GeoPoint(latitude = latitude, longitude = longitude),
    orderIndex = orderIndex,
)
