package de.clueventure.clue_venture

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun getAdventures(): List<Adventure> = withContext(Dispatchers.IO) {
    supabaseClient.from("adventures")
        .select()
        .decodeList<AdventureEntity>()
        .map { it.toAdventure() }
}

actual suspend fun createAdventure(draft: AdventureDraft): Adventure = withContext(Dispatchers.IO) {
    val createdAdventure = supabaseClient.from("adventures")
        .insert(
            AdventureInsertEntity(
                title = draft.title,
                summary = draft.summary,
                startLatitude = draft.startPoint.latitude,
                startLongitude = draft.startPoint.longitude,
                difficulty = draft.difficulty,
                estimatedDurationMinutes = draft.estimatedDurationMinutes,
            ),
        ) {
            select()
        }
        .decodeSingle<AdventureEntity>()

    if (draft.locations.isNotEmpty()) {
        supabaseClient.from("adventure_locations")
            .insert(
                draft.locations.mapIndexed { index, location ->
                    AdventureLocationInsertEntity(
                        adventureId = createdAdventure.id,
                        name = location.name,
                        latitude = location.point.latitude,
                        longitude = location.point.longitude,
                        orderIndex = index,
                    )
                },
            )
    }

    createdAdventure.toAdventure().copy(
        locations = draft.locations.mapIndexed { index, location ->
            AdventureLocation(
                name = location.name,
                point = location.point,
                orderIndex = index,
            )
        },
    )
}

actual suspend fun deleteAdventure(adventureId: String): Unit = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    supabaseClient.from("adventures")
        .delete {
            filter {
                eq("id", numericAdventureId)
            }
        }

    Unit
}

actual suspend fun getAdventureLocations(adventureId: String): List<AdventureLocation> = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext emptyList()

    supabaseClient.from("adventure_locations")
        .select()
        .decodeList<AdventureLocationEntity>()
        .asSequence()
        .filter { it.adventureId == numericAdventureId }
        .sortedBy { it.orderIndex }
        .map { it.toAdventureLocation() }
        .toList()
}

actual suspend fun deleteAdventureLocation(adventureId: String, orderIndex: Int): Unit = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    supabaseClient.from("adventure_locations")
        .delete {
            filter {
                eq("adventure_id", numericAdventureId)
                eq("order_index", orderIndex)
            }
        }

    Unit
}

actual suspend fun reorderAdventureLocations(adventureId: String, orderedCurrentIndexes: List<Int>): Unit = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    if (orderedCurrentIndexes.isEmpty()) {
        return@withContext
    }

    val currentLocations = getAdventureLocations(adventureId)
    val currentOrderIndexes = currentLocations.map { it.orderIndex }

    if (orderedCurrentIndexes.size != currentOrderIndexes.size || orderedCurrentIndexes.toSet() != currentOrderIndexes.toSet()) {
        throw IllegalArgumentException("Reorder input does not match existing locations.")
    }

    val maxOrderIndex = currentOrderIndexes.maxOrNull() ?: -1
    val temporaryBase = maxOrderIndex + currentOrderIndexes.size + 1000

    orderedCurrentIndexes.forEachIndexed { position, currentOrderIndex ->
        supabaseClient.from("adventure_locations")
            .update(AdventureLocationOrderUpdateEntity(orderIndex = temporaryBase + position)) {
                filter {
                    eq("adventure_id", numericAdventureId)
                    eq("order_index", currentOrderIndex)
                }
            }
    }

    orderedCurrentIndexes.indices.forEach { position ->
        supabaseClient.from("adventure_locations")
            .update(AdventureLocationOrderUpdateEntity(orderIndex = position)) {
                filter {
                    eq("adventure_id", numericAdventureId)
                    eq("order_index", temporaryBase + position)
                }
            }
    }

    Unit
}

actual suspend fun updateAdventure(adventureId: String, draft: AdventureMetadataDraft): Adventure = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    supabaseClient.from("adventures")
        .update(
            AdventureUpdateEntity(
                title = draft.title,
                summary = draft.summary,
                startLatitude = draft.startPoint.latitude,
                startLongitude = draft.startPoint.longitude,
                difficulty = draft.difficulty,
                estimatedDurationMinutes = draft.estimatedDurationMinutes,
            ),
        ) {
            filter {
                eq("id", numericAdventureId)
            }
            select()
        }
        .decodeSingle<AdventureEntity>()
        .toAdventure()
}

actual suspend fun appendAdventureLocations(
    adventureId: String,
    locations: List<AdventureLocationDraft>,
): List<AdventureLocation> = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext emptyList()
    if (locations.isEmpty()) {
        return@withContext emptyList()
    }

    val nextOrderIndex = getAdventureLocations(adventureId)
        .maxOfOrNull { it.orderIndex }
        ?.plus(1)
        ?: 0

    val locationInserts = locations.mapIndexed { index, location ->
        AdventureLocationInsertEntity(
            adventureId = numericAdventureId,
            name = location.name,
            latitude = location.point.latitude,
            longitude = location.point.longitude,
            orderIndex = nextOrderIndex + index,
        )
    }

    supabaseClient.from("adventure_locations")
        .insert(locationInserts)

    locationInserts.map { inserted ->
        AdventureLocation(
            name = inserted.name,
            point = GeoPoint(latitude = inserted.latitude, longitude = inserted.longitude),
            orderIndex = inserted.orderIndex,
        )
    }
}

// ============================================================================
// QUIZ REPOSITORY IMPLEMENTATIONS
// ============================================================================

actual suspend fun getQuizQuestions(adventureId: String): List<QuizQuestion> = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext emptyList()

    try {
        val questions = supabaseClient.from("quiz_questions")
            .select()
            .decodeList<QuizQuestionEntity>()
            .filter { it.adventureId == numericAdventureId }
            .sortedBy { it.orderIndex }

        questions.map { questionEntity ->
            val answers = supabaseClient.from("quiz_answers")
                .select()
                .decodeList<QuizAnswerEntity>()
                .filter { it.questionId == questionEntity.id }
                .sortedBy { it.answerOrder }
                .map { it.toQuizAnswer() }

            questionEntity.toQuizQuestion(answers)
        }
    } catch (e: Exception) {
        println("Error fetching quiz questions: ${e.message}")
        emptyList()
    }
}

actual suspend fun getQuizAnswers(questionId: Long): List<QuizAnswer> = withContext(Dispatchers.IO) {
    try {
        supabaseClient.from("quiz_answers")
            .select()
            .decodeList<QuizAnswerEntity>()
            .filter { it.questionId == questionId }
            .sortedBy { it.answerOrder }
            .map { it.toQuizAnswer() }
    } catch (e: Exception) {
        println("Error fetching quiz answers: ${e.message}")
        emptyList()
    }
}

actual suspend fun submitQuizAnswer(attemptId: Long, questionId: Long, answerId: Long): Boolean = withContext(Dispatchers.IO) {
    try {
        val answer = supabaseClient.from("quiz_answers")
            .select()
            .decodeList<QuizAnswerEntity>()
            .find { it.id == answerId } ?: return@withContext false

        supabaseClient.from("user_answers")
            .insert(
                mapOf(
                    "attempt_id" to attemptId,
                    "question_id" to questionId,
                    "answer_id" to answerId,
                    "is_correct" to answer.isCorrect,
                ),
            )

        answer.isCorrect
    } catch (e: Exception) {
        println("Error submitting quiz answer: ${e.message}")
        false
    }
}

// ============================================================================
// AUTHENTICATION REPOSITORY IMPLEMENTATIONS
// ============================================================================

actual suspend fun authenticateUser(email: String, password: String): User? = withContext(Dispatchers.IO) {
    return@withContext try {
        // This would typically use Supabase Auth
        // For now, simplified implementation
        val user = supabaseClient.from("users")
            .select()
            .decodeList<UserEntity>()
            .find { it.email == email }

        user?.toUser()
    } catch (e: Exception) {
        println("Error authenticating user: ${e.message}")
        null
    }
}

actual suspend fun registerUser(email: String, password: String, username: String): User? = withContext(Dispatchers.IO) {
    return@withContext try {
        val newUser = UserInsertEntity(
            id = generateUserId(),
            email = email,
            username = username,
        )

        val insertedUser = supabaseClient.from("users")
            .insert(newUser) {
                select()
            }
            .decodeSingle<UserEntity>()

        // Create user profile using typed DTO
        val profileInsert = UserProfileInsertEntity(
            userId = insertedUser.id,
            totalPoints = 0,
            adventuresCompleted = 0,
            adventuresStarted = 0,
        )

        supabaseClient.from("user_profiles")
            .insert(profileInsert)

        insertedUser.toUser()
    } catch (e: Exception) {
        println("Error registering user: ${e.message}")
        throw e
    }
}

actual suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
    // This would typically retrieve from Supabase Auth session
    // Placeholder implementation
    return@withContext null
}

actual suspend fun logoutUser(): Unit = withContext(Dispatchers.IO) {
    // This would typically clear Supabase Auth session
    Unit
}

actual suspend fun getUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
    try {
        supabaseClient.from("user_profiles")
            .select()
            .decodeList<UserProfileEntity>()
            .find { it.userId == userId }
            ?.toUserProfile()
    } catch (e: Exception) {
        println("Error fetching user profile: ${e.message}")
        null
    }
}

// ============================================================================
// ADVENTURE ATTEMPT REPOSITORY IMPLEMENTATIONS
// ============================================================================

actual suspend fun startAdventureAttempt(adventureId: String, userId: String): AdventureAttempt = withContext(Dispatchers.IO) {
    try {
        val numericAdventureId = adventureId.toLongOrNull() ?: throw IllegalArgumentException("Invalid adventure ID")

        val attempt = supabaseClient.from("adventure_attempts")
            .insert(
                mapOf(
                    "adventure_id" to numericAdventureId,
                    "user_id" to userId,
                    "started_at" to System.currentTimeMillis(),
                    "started_checkpoint_index" to 0,
                    "current_checkpoint_index" to 0,
                    "is_completed" to false,
                ),
            ) {
                select()
            }
            .decodeSingle<AdventureAttemptEntity>()

        attempt.toAdventureAttempt()
    } catch (e: Exception) {
        println("Error starting adventure attempt: ${e.message}")
        throw e
    }
}

actual suspend fun finishAdventureAttempt(attemptId: Long): Int = withContext(Dispatchers.IO) {
    try {
        val attempt = supabaseClient.from("adventure_attempts")
            .select()
            .decodeList<AdventureAttemptEntity>()
            .find { it.id == attemptId } ?: return@withContext 0

        val correctAnswers = supabaseClient.from("user_answers")
            .select()
            .decodeList<UserAnswerEntity>()
            .filter { it.attemptId == attemptId && it.isCorrect }
            .size

        val adventure = supabaseClient.from("adventures")
            .select()
            .decodeList<AdventureEntity>()
            .find { it.id == attempt.adventureId } ?: return@withContext 0

        val now = System.currentTimeMillis()
        val timeSpentSeconds = ((now - attempt.startedAt.toLong()) / 1000).toInt()
        val pointsEarned = calculateAdventurePoints(
            timeSpentSeconds,
            adventure.estimatedDurationMinutes ?: 60,
            correctAnswers,
        )

        supabaseClient.from("adventure_attempts")
            .update(
                mapOf(
                    "is_completed" to true,
                    "time_spent_seconds" to timeSpentSeconds,
                    "points_earned" to pointsEarned,
                ),
            ) {
                filter { eq("id", attemptId) }
            }

        // Update user profile
        val userProfile = getUserProfile(attempt.userId)
        if (userProfile != null) {
            supabaseClient.from("user_profiles")
                .update(
                    mapOf(
                        "total_points" to (userProfile.totalPoints + pointsEarned),
                        "adventures_completed" to (userProfile.adventuresCompleted + 1),
                    ),
                ) {
                    filter { eq("user_id", attempt.userId) }
                }
        }

        pointsEarned
    } catch (e: Exception) {
        println("Error finishing adventure attempt: ${e.message}")
        0
    }
}

actual suspend fun updateUserProgress(attemptId: Long, checkpointIndex: Int, userLocation: GeoPointState?): UserProgress = withContext(Dispatchers.IO) {
    try {
        val progressData = mapOf(
            "current_checkpoint_index" to checkpointIndex,
            "last_location_latitude" to (userLocation?.point?.latitude),
            "last_location_longitude" to (userLocation?.point?.longitude),
            "last_location_update" to userLocation?.timestamp,
            "updated_at" to System.currentTimeMillis(),
        )

        val existingProgress = supabaseClient.from("user_progress")
            .select()
            .decodeList<UserProgressEntity>()
            .find { it.attemptId == attemptId }

        val result = if (existingProgress != null) {
            supabaseClient.from("user_progress")
                .update(progressData) {
                    filter { eq("attempt_id", attemptId) }
                    select()
                }
                .decodeSingle<UserProgressEntity>()
        } else {
            supabaseClient.from("user_progress")
                .insert(progressData + ("attempt_id" to attemptId))
                {
                    select()
                }
                .decodeSingle<UserProgressEntity>()
        }

        result.toUserProgress()
    } catch (e: Exception) {
        println("Error updating user progress: ${e.message}")
        throw e
    }
}

actual suspend fun getUserProgress(attemptId: Long): UserProgress? = withContext(Dispatchers.IO) {
    try {
        supabaseClient.from("user_progress")
            .select()
            .decodeList<UserProgressEntity>()
            .find { it.attemptId == attemptId }
            ?.toUserProgress()
    } catch (e: Exception) {
        println("Error fetching user progress: ${e.message}")
        null
    }
}

actual suspend fun getCurrentAttemptForAdventure(adventureId: String, userId: String): AdventureAttempt? = withContext(Dispatchers.IO) {
    try {
        val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext null

        supabaseClient.from("adventure_attempts")
            .select()
            .decodeList<AdventureAttemptEntity>()
            .find { it.adventureId == numericAdventureId && it.userId == userId && !it.isCompleted }
            ?.toAdventureAttempt()
    } catch (e: Exception) {
        println("Error fetching current attempt: ${e.message}")
        null
    }
}
