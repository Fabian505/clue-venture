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
    @SerialName("completion_points") val completionPoints: Int = 0,
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("created_by") val createdBy: String? = null,
)

@Serializable
data class AdventureInsertEntity(
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
    @SerialName("completion_points") val completionPoints: Int,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("created_by") val createdBy: String?,
)

@Serializable
data class AdventureUpdateEntity(
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
    @SerialName("is_public") val isPublic: Boolean,
)

@Serializable
data class AdventureLocationAdventureIdEntity(
    @SerialName("adventure_id") val adventureId: Long,
)

@Serializable
data class AdventureLocationInsertEntity(
    @SerialName("adventure_id") val adventureId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("point_value") val pointValue: Int,
    @SerialName("time_limit_seconds") val timeLimitSeconds: Int? = null,
)

@Serializable
data class AdventureLocationEntity(
    val id: Long,
    @SerialName("adventure_id") val adventureId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("point_value") val pointValue: Int = 0,
    @SerialName("time_limit_seconds") val timeLimitSeconds: Int? = null,
)

// ============================================================================
// HINT ENTITIES
// ============================================================================

@Serializable
data class HintEntity(
    val id: Long,
    @SerialName("location_id") val locationId: Long,
    @SerialName("hint_index") val hintIndex: Int,
    val text: String,
    @SerialName("point_cost") val pointCost: Int = 0,
    @SerialName("image_url") val imageUrl: String? = null,
)

@Serializable
data class HintInsertEntity(
    @SerialName("location_id") val locationId: Long,
    @SerialName("hint_index") val hintIndex: Int,
    val text: String,
    @SerialName("point_cost") val pointCost: Int,
    @SerialName("image_url") val imageUrl: String? = null,
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

@Serializable
data class QuizQuestionInsertEntity(
    @SerialName("adventure_id") val adventureId: Long,
    @SerialName("question_text") val questionText: String,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("question_type") val questionType: String = "multiple_choice",
)

@Serializable
data class QuizAnswerInsertEntity(
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

@Serializable
data class LeaderboardEntryEntity(
    @SerialName("display_name") val displayName: String,
    @SerialName("points") val points: Long,
)

// Insert DTOs for safe serialization
@Serializable
data class UserInsertEntity(
    val id: String,
    val email: String,
    val username: String? = null,
)

@Serializable
data class UserProfileInsertEntity(
    @SerialName("user_id") val userId: String,
    @SerialName("total_points") val totalPoints: Int = 0,
    @SerialName("adventures_completed") val adventuresCompleted: Int = 0,
    @SerialName("adventures_started") val adventuresStarted: Int = 0,
)

@Serializable
data class UserPointsUpdateEntity(
    @SerialName("total_points") val totalPoints: Int,
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

// ============================================================================
// ADVENTURE ATTEMPT INSERT & UPDATE DTOs
// ============================================================================

@Serializable
data class AdventureAttemptInsertEntity(
    @SerialName("adventure_id") val adventureId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("started_checkpoint_index") val startedCheckpointIndex: Int = 0,
    @SerialName("current_checkpoint_index") val currentCheckpointIndex: Int = 0,
    @SerialName("is_completed") val isCompleted: Boolean = false,
)

@Serializable
data class AdventureAttemptMarkCompleteEntity(
    @SerialName("is_completed") val isCompleted: Boolean = true,
)

@Serializable
data class AdventureAttemptFinishEntity(
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("completed_at") val completedAt: String,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int,
    @SerialName("points_earned") val pointsEarned: Int,
)

@Serializable
data class AdventureAttemptCancelEntity(
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("completed_at") val completedAt: String,
    @SerialName("time_spent_seconds") val timeSpentSeconds: Int,
    @SerialName("points_earned") val pointsEarned: Int,
)

@Serializable
data class UserAnswerInsertEntity(
    @SerialName("attempt_id") val attemptId: Long,
    @SerialName("question_id") val questionId: Long,
    @SerialName("answer_id") val answerId: Long,
    @SerialName("is_correct") val isCorrect: Boolean,
)

@Serializable
data class UserProgressInsertEntity(
    @SerialName("attempt_id") val attemptId: Long,
    @SerialName("current_checkpoint_index") val currentCheckpointIndex: Int = 0,
    @SerialName("last_location_latitude") val lastLocationLatitude: Double?,
    @SerialName("last_location_longitude") val lastLocationLongitude: Double?,
    @SerialName("last_location_update") val lastLocationUpdate: String?,
)

@Serializable
data class UserProgressUpdateEntity(
    @SerialName("current_checkpoint_index") val currentCheckpointIndex: Int,
    @SerialName("last_location_latitude") val lastLocationLatitude: Double?,
    @SerialName("last_location_longitude") val lastLocationLongitude: Double?,
    @SerialName("last_location_update") val lastLocationUpdate: String?,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class AdventureFeedbackEntity(
    val id: Long,
    @SerialName("attempt_id") val attemptId: Long,
    @SerialName("adventure_id") val adventureId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("difficulty_rating") val difficultyRating: Int,
    @SerialName("overall_rating") val overallRating: Int,
    @SerialName("custom_feedback") val customFeedback: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class AdventureFeedbackInsertEntity(
    @SerialName("attempt_id") val attemptId: Long,
    @SerialName("adventure_id") val adventureId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("difficulty_rating") val difficultyRating: Int,
    @SerialName("overall_rating") val overallRating: Int,
    @SerialName("custom_feedback") val customFeedback: String?,
)

@Serializable
data class AdventureFeedbackUpdateEntity(
    @SerialName("difficulty_rating") val difficultyRating: Int,
    @SerialName("overall_rating") val overallRating: Int,
    @SerialName("custom_feedback") val customFeedback: String?,
    @SerialName("updated_at") val updatedAt: String,
)

fun AdventureEntity.toAdventure(): Adventure = Adventure(
    id = id.toString(),
    title = title,
    summary = summary,
    startPoint = GeoPoint(latitude = startLatitude, longitude = startLongitude),
    difficulty = difficulty,
    estimatedDurationMinutes = estimatedDurationMinutes,
    completionPoints = completionPoints,
    isPublic = isPublic,
    createdBy = createdBy,
)

fun AdventureLocationEntity.toAdventureLocation(hints: List<Hint> = emptyList()): AdventureLocation = AdventureLocation(
    id = id,
    name = name,
    point = GeoPoint(latitude = latitude, longitude = longitude),
    orderIndex = orderIndex,
    pointValue = pointValue,
    timeLimitSeconds = timeLimitSeconds,
    hints = hints,
)

fun HintEntity.toHint(): Hint = Hint(
    id = id,
    locationId = locationId,
    hintIndex = hintIndex,
    text = text,
    pointCost = pointCost,
    imageUrl = imageUrl,
)
