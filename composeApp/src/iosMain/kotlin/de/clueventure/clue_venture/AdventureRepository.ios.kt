package de.clueventure.clue_venture

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.Clock

actual suspend fun getAdventures(): List<Adventure> = withContext(Dispatchers.IO) {
    try {
        supabaseClient.from("adventures")
            .select()
            .decodeList<AdventureEntity>()
            .map { it.toAdventure() }
    } catch (_: Exception) {
        sampleAdventures
    }
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
}

actual suspend fun getAdventureLocations(adventureId: String): List<AdventureLocation> = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext emptyList()

    runCatching {
        supabaseClient.from("adventure_locations")
            .select()
            .decodeList<AdventureLocationEntity>()
            .asSequence()
            .filter { it.adventureId == numericAdventureId }
            .sortedBy { it.orderIndex }
            .map { it.toAdventureLocation() }
            .toList()
    }.getOrDefault(emptyList())
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
}

actual suspend fun reorderAdventureLocations(adventureId: String, orderedCurrentIndexes: List<Int>): Unit = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    if (orderedCurrentIndexes.isEmpty()) {
        return@withContext
    }

    val currentLocations = getAdventureLocations(adventureId)
    val currentOrderIndexes = currentLocations.map { it.orderIndex }

    require(
        orderedCurrentIndexes.size == currentOrderIndexes.size &&
            orderedCurrentIndexes.toSet() == currentOrderIndexes.toSet(),
    ) { "Reorder input does not match existing locations." }

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

    return@withContext runCatching {
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
    }.getOrDefault(emptyList())
}

@Suppress("unused")
actual suspend fun getQuizAnswers(questionId: Long): List<QuizAnswer> = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        supabaseClient.from("quiz_answers")
            .select()
            .decodeList<QuizAnswerEntity>()
            .filter { it.questionId == questionId }
            .sortedBy { it.answerOrder }
            .map { it.toQuizAnswer() }
    }.getOrDefault(emptyList())
}

@Suppress("unused")
actual suspend fun submitQuizAnswer(attemptId: Long, questionId: Long, answerId: Long): Boolean = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val answer = supabaseClient.from("quiz_answers")
            .select()
            .decodeList<QuizAnswerEntity>()
            .find { it.id == answerId } ?: return@runCatching false

        supabaseClient.from("user_answers")
            .insert(
                UserAnswerInsertEntity(
                    attemptId = attemptId,
                    questionId = questionId,
                    answerId = answerId,
                    isCorrect = answer.isCorrect,
                ),
            )

        answer.isCorrect
    }.getOrDefault(false)
}

// ============================================================================
// AUTHENTICATION REPOSITORY IMPLEMENTATIONS
// ============================================================================

@Suppress("UNUSED_PARAMETER")
actual suspend fun authenticateUser(email: String, password: String): User? = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val user = supabaseClient.from("users")
            .select()
            .decodeList<UserEntity>()
            .find { it.email == email }

        user?.toUser()
    }.getOrNull()
}

@Suppress("UNUSED_PARAMETER")
actual suspend fun registerUser(email: String, password: String, username: String): User? = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val newUser = mapOf(
            "email" to email,
            "username" to username,
        )

        val insertedUser = supabaseClient.from("users")
            .insert(newUser)
            {
                select()
            }
            .decodeSingle<UserEntity>()

        // Create user profile
        supabaseClient.from("user_profiles")
            .insert(
                mapOf(
                    "user_id" to insertedUser.id,
                    "total_points" to 0,
                    "adventures_completed" to 0,
                    "adventures_started" to 0,
                ),
            )

        insertedUser.toUser()
    }.getOrNull()
}

@Suppress("unused")
actual suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
    // This would typically retrieve from Supabase Auth session
    // Placeholder implementation
    return@withContext null
}

@Suppress("unused")
actual suspend fun logoutUser(): Unit = withContext(Dispatchers.IO) {
    // This would typically clear Supabase Auth session
}

actual suspend fun getUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        supabaseClient.from("user_profiles")
            .select()
            .decodeList<UserProfileEntity>()
            .find { it.userId == userId }
            ?.toUserProfile()
    }.getOrNull()
}

// ============================================================================
// ADVENTURE ATTEMPT REPOSITORY IMPLEMENTATIONS
// ============================================================================

actual suspend fun startAdventureAttempt(adventureId: String, userId: String): AdventureAttempt = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val numericAdventureId = adventureId.toLongOrNull() ?: throw IllegalArgumentException("Invalid adventure ID")

        val attempt = supabaseClient.from("adventure_attempts")
            .insert(
                AdventureAttemptInsertEntity(
                    adventureId = numericAdventureId,
                    userId = userId,
                    startedAt = Clock.System.now().toEpochMilliseconds().toString(),
                    startedCheckpointIndex = 0,
                    currentCheckpointIndex = 0,
                    isCompleted = false,
                ),
            )
            {
                select()
            }
            .decodeSingle<AdventureAttemptEntity>()

        attempt.toAdventureAttempt()
    }.getOrThrow()
}

actual suspend fun finishAdventureAttempt(attemptId: Long): Int = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val attempt = supabaseClient.from("adventure_attempts")
            .select()
            .decodeList<AdventureAttemptEntity>()
            .find { it.id == attemptId } ?: return@runCatching 0

        val correctAnswers = supabaseClient.from("user_answers")
            .select()
            .decodeList<UserAnswerEntity>()
            .filter { it.attemptId == attemptId && it.isCorrect }
            .size

        val adventure = supabaseClient.from("adventures")
            .select()
            .decodeList<AdventureEntity>()
            .find { it.id == attempt.adventureId } ?: return@runCatching 0

        val now = Clock.System.now().toEpochMilliseconds()
        val startedAtMillis = attempt.startedAt.toLongOrNull() ?: now
        val timeSpentSeconds = ((now - startedAtMillis) / 1000L).toInt()
        val pointsEarned = calculateAdventurePoints(
            timeSpentSeconds,
            adventure.estimatedDurationMinutes ?: 60,
            correctAnswers,
        )

        supabaseClient.from("adventure_attempts")
            .update(
                AdventureAttemptFinishEntity(
                    isCompleted = true,
                    timeSpentSeconds = timeSpentSeconds,
                    pointsEarned = pointsEarned,
                    completedAt = now.toString(),
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
    }.getOrDefault(0)
}

actual suspend fun updateUserProgress(attemptId: Long, checkpointIndex: Int, userLocation: GeoPointState?): UserProgress = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val existingProgress = supabaseClient.from("user_progress")
            .select()
            .decodeList<UserProgressEntity>()
            .find { it.attemptId == attemptId }

        val result = if (existingProgress != null) {
            supabaseClient.from("user_progress")
                .update(
                    UserProgressUpdateEntity(
                        currentCheckpointIndex = checkpointIndex,
                        lastLocationLatitude = userLocation?.point?.latitude,
                        lastLocationLongitude = userLocation?.point?.longitude,
                        lastLocationUpdate = userLocation?.timestamp,
                        updatedAt = Clock.System.now().toEpochMilliseconds().toString(),
                    ),
                ) {
                    filter { eq("attempt_id", attemptId) }
                    select()
                }
                .decodeSingle<UserProgressEntity>()
        } else {
            supabaseClient.from("user_progress")
                .insert(
                    UserProgressInsertEntity(
                        attemptId = attemptId,
                        currentCheckpointIndex = checkpointIndex,
                        lastLocationLatitude = userLocation?.point?.latitude,
                        lastLocationLongitude = userLocation?.point?.longitude,
                        lastLocationUpdate = userLocation?.timestamp,
                    ),
                )
                {
                    select()
                }
                .decodeSingle<UserProgressEntity>()
        }

        result.toUserProgress()
    }.getOrThrow()
}

@Suppress("unused")
actual suspend fun getUserProgress(attemptId: Long): UserProgress? = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        supabaseClient.from("user_progress")
            .select()
            .decodeList<UserProgressEntity>()
            .find { it.attemptId == attemptId }
            ?.toUserProgress()
    }.getOrNull()
}

actual suspend fun getCurrentAttemptForAdventure(adventureId: String, userId: String): AdventureAttempt? = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val numericAdventureId = adventureId.toLongOrNull() ?: return@runCatching null

        supabaseClient.from("adventure_attempts")
            .select()
            .decodeList<AdventureAttemptEntity>()
            .find { it.adventureId == numericAdventureId && it.userId == userId && !it.isCompleted }
            ?.toAdventureAttempt()
    }.getOrNull()
}

