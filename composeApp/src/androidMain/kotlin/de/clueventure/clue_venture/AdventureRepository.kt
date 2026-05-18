package de.clueventure.clue_venture

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.net.HttpURLConnection
import java.net.URL

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

private fun parseTimestampToMillis(timestamp: String): Long? {
    // PostgREST returns timestamps as "2026-05-18T12:34:56.123456+00:00" or similar.
    // SimpleDateFormat can't handle 6-digit microseconds, so we truncate to milliseconds.
    return runCatching {
        val normalized = timestamp.replace(" ", "T").let { ts ->
            val dotIdx = ts.indexOf('.')
            if (dotIdx >= 0) {
                // Keep only 3 fractional digits, drop the rest before the timezone
                val tzIdx = ts.indexOfFirst { c -> c == '+' || c == 'Z' || (c == '-' && ts.indexOf('-', dotIdx) == ts.indexOf(c)) }
                    .takeIf { it > dotIdx } ?: ts.length
                ts.substring(0, (dotIdx + 4).coerceAtMost(tzIdx)) + "Z"
            } else ts
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.parse(normalized)?.time
    }.getOrNull()
}

private fun nowIso8601(): String = millisToIso8601(System.currentTimeMillis())

private fun millisToIso8601(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

internal object AndroidSessionStorage {
    lateinit var context: Context
}

private const val SESSION_PREFS_NAME = "clue_venture_session"
private const val CURRENT_USER_KEY = "current_user"
private const val ROUTING_LOG_TAG = "ClueVentureRouting"
private const val ROUTING_USER_AGENT = "ClueVenture/1.0 (Android; ClueVenture Team - Educational Project)"

private val sessionJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private fun saveCurrentUser(user: User) {
    AndroidSessionStorage.context
        .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(CURRENT_USER_KEY, sessionJson.encodeToString(User.serializer(), user))
        .apply()
}

private fun loadCurrentUser(): User? {
    val storedUser = AndroidSessionStorage.context
        .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        .getString(CURRENT_USER_KEY, null)
        ?: return null

    return runCatching {
        sessionJson.decodeFromString(User.serializer(), storedUser)
    }.getOrNull()
}

private fun clearCurrentUser() {
    AndroidSessionStorage.context
        .getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove(CURRENT_USER_KEY)
        .apply()
}

actual suspend fun getAdventures(): List<Adventure> = withContext(Dispatchers.IO) {
    val userId = loadCurrentUser()?.id
    val adventures = supabaseClient.from("adventures")
        .select {
            filter {
                if (userId != null) {
                    or {
                        eq("is_public", true)
                        eq("created_by", userId)
                    }
                } else {
                    eq("is_public", true)
                }
            }
        }
        .decodeList<AdventureEntity>()

    val adventureIds = adventures.map { it.id }
    val locationCountById = if (adventureIds.isEmpty()) {
        emptyMap()
    } else {
        runCatching {
            supabaseClient.from("adventure_locations")
                .select(columns = Columns.list("adventure_id")) {
                    filter { isIn("adventure_id", adventureIds) }
                }
                .decodeList<AdventureLocationAdventureIdEntity>()
                .groupingBy { it.adventureId }
                .eachCount()
        }.getOrDefault(emptyMap())
    }

    adventures.map { entity ->
        entity.toAdventure().copy(locationCount = locationCountById[entity.id] ?: 0)
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
                completionPoints = 0,
                isPublic = draft.isPublic,
                createdBy = loadCurrentUser()?.id,
            ),
        ) {
            select()
        }
        .decodeSingle<AdventureEntity>()

    val createdLocations = if (draft.locations.isNotEmpty()) {
        supabaseClient.from("adventure_locations")
            .insert(
                draft.locations.mapIndexed { index, location ->
                    AdventureLocationInsertEntity(
                        adventureId = createdAdventure.id,
                        name = location.name,
                        latitude = location.point.latitude,
                        longitude = location.point.longitude,
                        orderIndex = index,
                        pointValue = location.pointValue,
                        timeLimitSeconds = location.timeLimitSeconds,
                    )
                },
            ) { select() }
            .decodeList<AdventureLocationEntity>()
            .sortedBy { it.orderIndex }
    } else emptyList()

    // Insert hints for each location
    val hintInserts = draft.locations.flatMapIndexed { index, locationDraft ->
        val locationId = createdLocations.getOrNull(index)?.id ?: return@flatMapIndexed emptyList()
        locationDraft.hints
            .filter { it.text.isNotBlank() || it.imageUrl != null }
            .map { hint ->
                HintInsertEntity(
                    locationId = locationId,
                    hintIndex = hint.hintIndex,
                    text = hint.text,
                    pointCost = hint.pointCost,
                    imageUrl = hint.imageUrl,
                )
            }
    }
    if (hintInserts.isNotEmpty()) {
        supabaseClient.from("hints").insert(hintInserts)
    }

    // Insert quiz questions and answers
    draft.quizQuestions.forEachIndexed { questionIndex, questionDraft ->
        val createdQuestion = supabaseClient.from("quiz_questions")
            .insert(
                QuizQuestionInsertEntity(
                    adventureId = createdAdventure.id,
                    questionText = questionDraft.questionText,
                    orderIndex = questionIndex,
                ),
            ) { select() }
            .decodeSingle<QuizQuestionEntity>()

        val answerInserts = questionDraft.answers
            .filter { it.answerText.isNotBlank() }
            .map { answerDraft ->
                QuizAnswerInsertEntity(
                    questionId = createdQuestion.id,
                    answerText = answerDraft.answerText,
                    isCorrect = answerDraft.isCorrect,
                    answerOrder = answerDraft.answerOrder,
                )
            }
        if (answerInserts.isNotEmpty()) {
            supabaseClient.from("quiz_answers").insert(answerInserts)
        }
    }

    createdAdventure.toAdventure().copy(
        locations = createdLocations.map { it.toAdventureLocation() },
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

    val locationEntities = supabaseClient.from("adventure_locations")
        .select {
            filter { eq("adventure_id", numericAdventureId) }
        }
        .decodeList<AdventureLocationEntity>()
        .sortedBy { it.orderIndex }

    if (locationEntities.isEmpty()) return@withContext emptyList()

    val locationIds = locationEntities.map { it.id }

    val hintsByLocationId = runCatching {
        supabaseClient.from("hints")
            .select {
                filter { isIn("location_id", locationIds) }
            }
            .decodeList<HintEntity>()
            .sortedBy { it.hintIndex }
            .groupBy { it.locationId }
            .mapValues { (_, entities) -> entities.map { it.toHint() } }
    }.getOrDefault(emptyMap())

    locationEntities.map { it.toAdventureLocation(hintsByLocationId[it.id].orEmpty()) }
}

actual suspend fun getWalkingRouteDistanceMeters(points: List<GeoPoint>): Double? = withContext(Dispatchers.IO) {
    if (points.size < 2) {
        return@withContext 0.0
    }

    runCatching {
        val routeUrl = buildWalkingRouteDistanceUrl(points)
        Log.d(ROUTING_LOG_TAG, "Requesting duration route: $routeUrl")

        val connection = URL(routeUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", ROUTING_USER_AGENT)

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader().use { it?.readText().orEmpty() }
            }

            Log.d(ROUTING_LOG_TAG, "Duration route response code: $responseCode")
            Log.d(ROUTING_LOG_TAG, "Duration route response body: ${responseText.take(500)}")
            parseWalkingRouteDistanceMeters(responseText)
        } finally {
            connection.disconnect()
        }
    }.onFailure { throwable ->
        Log.e(ROUTING_LOG_TAG, "Duration route request failed", throwable)
    }.getOrNull()
}

private fun buildWalkingRouteDistanceUrl(points: List<GeoPoint>): String {
    val coordinates = points.joinToString(";") { point ->
        "${point.longitude},${point.latitude}"
    }
    return "https://router.project-osrm.org/route/v1/foot/$coordinates?overview=false&steps=false"
}

private fun parseWalkingRouteDistanceMeters(responseText: String): Double? {
    if (responseText.isBlank()) {
        Log.w(ROUTING_LOG_TAG, "Duration route response is blank")
        return null
    }

    val root = JSONObject(responseText)
    val code = root.optString("code")
    val message = root.optString("message")
    if (code.isNotBlank() && code != "Ok") {
        Log.w(ROUTING_LOG_TAG, "Duration route returned code=$code message=$message")
        return null
    }

    val routes = root.optJSONArray("routes")
    if (routes == null || routes.length() == 0) {
        Log.w(ROUTING_LOG_TAG, "Duration route response contains no routes")
        return null
    }

    val distance = routes.getJSONObject(0).optDouble("distance", Double.NaN)
    return distance.takeIf { !it.isNaN() }
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
                isPublic = draft.isPublic,
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
            pointValue = location.pointValue,
            timeLimitSeconds = location.timeLimitSeconds,
        )
    }

    supabaseClient.from("adventure_locations")
        .insert(locationInserts) { select() }
        .decodeList<AdventureLocationEntity>()
        .sortedBy { it.orderIndex }
        .map { it.toAdventureLocation() }
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

        if (questions.isEmpty()) {
            return@runCatching emptyList()
        }

        val questionIds = questions.map { it.id }
        val answers = supabaseClient.from("quiz_answers")
            .select()
            .decodeList<QuizAnswerEntity>()
            .filter { it.questionId in questionIds }
            .sortedBy { it.answerOrder }

        val answersByQuestionId = answers.groupBy { it.questionId }
            .mapValues { (_, answerEntities) ->
                answerEntities.map { it.toQuizAnswer() }
            }

        questions.map { questionEntity ->
            questionEntity.toQuizQuestion(answersByQuestionId[questionEntity.id].orEmpty())
        }
    }.onFailure { e ->
        println("Error fetching quiz questions for adventure $adventureId: ${e.message}")
        e.printStackTrace()
    }.getOrDefault(emptyList())
}

actual suspend fun getQuizAnswers(questionId: Long): List<QuizAnswer> = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        supabaseClient.from("quiz_answers")
            .select()
            .decodeList<QuizAnswerEntity>()
            .filter { it.questionId == questionId }
            .sortedBy { it.answerOrder }
            .map { it.toQuizAnswer() }
    }.onFailure { e ->
        println("Error fetching quiz answers for question $questionId: ${e.message}")
        e.printStackTrace()
    }.getOrDefault(emptyList())
}

actual suspend fun getQuizQuestionCount(adventureId: String): Int = withContext(Dispatchers.IO) {
    return@withContext runCatching {
        val allQuestions = supabaseClient.from("quiz_questions")
            .select()
            .decodeList<QuizQuestionEntity>()

        val filteredCount = allQuestions.count { it.adventureId == adventureId.toLongOrNull() }
        println("Quiz: Found $filteredCount questions for adventure $adventureId")
        println("Quiz: Total questions in database: ${allQuestions.size}")
        if (allQuestions.isNotEmpty()) {
            println("Quiz: Sample questions: ${allQuestions.take(3).map { "${it.id}->${it.adventureId}" }}")
        }

        if (filteredCount == 0 && allQuestions.isNotEmpty()) {
            println("Quiz: WARNING - No questions found for this adventure! Check if adventure_id matches.")
        }

        filteredCount
    }.onFailure { e ->
        println("ERROR: Error fetching quiz question count for adventure $adventureId: ${e.message}")
        e.printStackTrace()
    }.getOrDefault(0)
}

actual suspend fun getQuizQuestionByIndex(adventureId: String, index: Int): QuizQuestion? = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext null

    return@withContext runCatching {
        println("DEBUG: Loading quiz question at index $index for adventure $adventureId")
        val question = supabaseClient.from("quiz_questions")
            .select()
            .decodeList<QuizQuestionEntity>()
            .filter { it.adventureId == numericAdventureId }
            .sortedBy { it.orderIndex }
            .getOrNull(index) ?: return@runCatching null

        println("DEBUG: Found question: $question")

        val answers = supabaseClient.from("quiz_answers")
            .select()
            .decodeList<QuizAnswerEntity>()
            .filter { it.questionId == question.id }
            .sortedBy { it.answerOrder }
            .map { it.toQuizAnswer() }

        println("DEBUG: Found ${answers.size} answers for question ${question.id}")
        question.toQuizQuestion(answers)
    }.onFailure { e ->
        println("ERROR: Error fetching quiz question at index $index for adventure $adventureId: ${e.message}")
        e.printStackTrace()
    }.getOrDefault(null)
}

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
    }.onFailure { e ->
        println("Error submitting quiz answer: ${e.message}")
        e.printStackTrace()
    }.getOrDefault(false)
}

actual suspend fun recordQuizAnswerEvaluation(event: QuizAnswerEvaluationEvent): Unit = withContext(Dispatchers.IO) {
    if (event.attemptId == null) {
        return@withContext
    }

    println(
        "Quiz answer evaluated: attempt=${event.attemptId}, question=${event.questionId}, answer=${event.answerId}, correct=${event.isCorrect}",
    )
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

        user?.toUser()?.also { saveCurrentUser(it) }
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

        insertedUser.toUser().also { saveCurrentUser(it) }
    } catch (e: Exception) {
        println("Error registering user: ${e.message}")
        throw e
    }
}

actual suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
    loadCurrentUser()
}

actual suspend fun logoutUser(): Unit = withContext(Dispatchers.IO) {
    clearCurrentUser()
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

        // Ensure user_profiles row exists so point transactions always have a row to update.
        // A duplicate-key error means the row already exists — that is fine.
        runCatching {
            supabaseClient.from("user_profiles")
                .insert(UserProfileInsertEntity(userId = userId))
        }

        val attempt = supabaseClient.from("adventure_attempts")
            .insert(
                AdventureAttemptInsertEntity(
                    adventureId = numericAdventureId,
                    userId = userId,
                    startedCheckpointIndex = 0,
                    currentCheckpointIndex = 0,
                    isCompleted = false,
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

actual suspend fun markAttemptCompleted(attemptId: Long): Unit = withContext(Dispatchers.IO) {
    supabaseClient.from("adventure_attempts")
        .update(AdventureAttemptMarkCompleteEntity()) {
            filter { eq("id", attemptId) }
        }
}

actual suspend fun finishAdventureAttempt(
    attemptId: Long,
    userId: String,
    startedAt: String,
    adventure: Adventure,
    accumulatedPoints: Int,
): Int = withContext(Dispatchers.IO) {
    try {
        val now = System.currentTimeMillis()
        val startedAtMillis = parseTimestampToMillis(startedAt) ?: now
        val timeSpentSeconds = ((now - startedAtMillis) / 1000).toInt().coerceAtLeast(0)
        val completedAt = millisToIso8601(now)
        val allPoints = listOf(adventure.startPoint) +
            adventure.locations.sortedBy { it.orderIndex }.map { it.point }
        val totalRouteMeters = allPoints.zipWithNext { a, b -> a.distanceTo(b) }.sum()
        val expectedSeconds = (totalRouteMeters / 1.4).toInt().coerceAtLeast(1)
        val completionBonus = calculateAdventurePoints(
            timeSpentSeconds,
            expectedSeconds,
            adventure.difficulty,
        ) + adventure.completionPoints
        // accumulatedPoints (checkpoints + quiz) were already added to user_profiles live;
        // include them here so the leaderboard (which sums adventure_attempts.points_earned) counts them.
        val pointsEarned = completionBonus + accumulatedPoints

        // Critical: mark attempt as completed — must succeed or we throw.
        supabaseClient.from("adventure_attempts")
            .update(
                AdventureAttemptFinishEntity(
                    isCompleted = true,
                    completedAt = completedAt,
                    timeSpentSeconds = timeSpentSeconds,
                    pointsEarned = pointsEarned,
                ),
            ) {
                filter { eq("id", attemptId) }
            }

        // Secondary: award completion bonus and update profile — failures are logged but don't block completion.
        // Only completionBonus is incremented here; accumulatedPoints (checkpoint + quiz) were already
        // added to user_profiles live during gameplay via updateUserPoints().
        runCatching {
            supabaseClient.postgrest.rpc(
                "increment_user_points",
                buildJsonObject {
                    put("p_user_id", userId)
                    put("p_delta", completionBonus)
                },
            )
        }.onFailure { Log.e("AdventureRepo", "Failed to increment points for attempt $attemptId", it) }

        runCatching {
            val userProfile = getUserProfile(userId)
            if (userProfile != null) {
                supabaseClient.from("user_profiles")
                    .update(
                        mapOf("adventures_completed" to (userProfile.adventuresCompleted + 1)),
                    ) {
                        filter { eq("user_id", userId) }
                    }
            }
        }.onFailure { Log.e("AdventureRepo", "Failed to update adventures_completed for $userId", it) }

        completionBonus
    } catch (e: Exception) {
        Log.e("AdventureRepo", "Error finishing adventure attempt $attemptId", e)
        throw e
    }
}

actual suspend fun cancelAdventureAttempt(attemptId: Long): Unit = withContext(Dispatchers.IO) {
    try {
        val attempt = supabaseClient.from("adventure_attempts")
            .select { filter { eq("id", attemptId) } }
            .decodeList<AdventureAttemptEntity>()
            .firstOrNull() ?: throw IllegalStateException("Adventure attempt not found")

        val nowMillis = System.currentTimeMillis()
        val startedAtMillis = parseTimestampToMillis(attempt.startedAt) ?: nowMillis
        val timeSpentSeconds = ((nowMillis - startedAtMillis) / 1000).toInt().coerceAtLeast(0)
        val completedAt = millisToIso8601(startedAtMillis + timeSpentSeconds * 1000L)

        supabaseClient.from("adventure_attempts")
            .update(
                AdventureAttemptCancelEntity(
                    isCompleted = false,
                    completedAt = completedAt,
                    timeSpentSeconds = timeSpentSeconds,
                    pointsEarned = 0,
                ),
            ) {
                filter { eq("id", attemptId) }
            }
    } catch (e: Exception) {
        println("Error cancelling adventure attempt: ${e.message}")
        throw e
    }
}

actual suspend fun updateUserProgress(attemptId: Long, checkpointIndex: Int, userLocation: GeoPointState?): UserProgress = withContext(Dispatchers.IO) {
    try {
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
                        updatedAt = System.currentTimeMillis().toString(),
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
            .find {
                it.adventureId == numericAdventureId &&
                    it.userId == userId &&
                    !it.isCompleted &&
                    it.completedAt == null
            }
            ?.toAdventureAttempt()
    } catch (e: Exception) {
        println("Error fetching current attempt: ${e.message}")
        null
    }
}

// ============================================================================
// HINTS & POINTS REPOSITORY IMPLEMENTATIONS
// ============================================================================

actual suspend fun getHints(locationId: Long): List<Hint> = withContext(Dispatchers.IO) {
    runCatching {
        supabaseClient.from("hints")
            .select()
            .decodeList<HintEntity>()
            .filter { it.locationId == locationId }
            .sortedBy { it.hintIndex }
            .map { it.toHint() }
    }.onFailure { e ->
        println("Error fetching hints for location $locationId: ${e.message}")
    }.getOrDefault(emptyList())
}

actual suspend fun getUserPoints(userId: String): Int = withContext(Dispatchers.IO) {
    runCatching {
        supabaseClient.from("user_profiles")
            .select() {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<UserProfileEntity>()
            ?.totalPoints ?: 0
    }.onFailure { e ->
        println("Error fetching points for user $userId: ${e.message}")
    }.getOrDefault(0)
}

actual suspend fun updateUserPoints(userId: String, pointsDelta: Int): Unit = withContext(Dispatchers.IO) {
    if (pointsDelta < 0) {
        // For deductions (e.g. hint purchases): read current value, clamp at 0, then write.
        // The increment_user_points RPC upsert path triggers a CHECK (total_points >= 0)
        // violation when the INSERT VALUES path is evaluated with a negative delta.
        val current = getUserPoints(userId)
        val newPoints = (current + pointsDelta).coerceAtLeast(0)
        supabaseClient.from("user_profiles")
            .update(UserPointsUpdateEntity(totalPoints = newPoints, lastUpdated = nowIso8601())) {
                filter { eq("user_id", userId) }
            }
    } else {
        // For increments: use the atomic RPC upsert so missing rows are created automatically.
        runCatching {
            supabaseClient.postgrest.rpc(
                "increment_user_points",
                buildJsonObject {
                    put("p_user_id", userId)
                    put("p_delta", pointsDelta)
                },
            )
        }.onFailure { e ->
            println("Error updating points for user $userId: ${e.message}")
            throw e
        }
    }
    Unit
}

actual suspend fun saveHintsForLocation(locationId: Long, hints: List<HintDraft>): Unit = withContext(Dispatchers.IO) {
    // Delete all existing hints for this location, then insert non-blank/non-empty ones
    supabaseClient.from("hints")
        .delete {
            filter { eq("location_id", locationId) }
        }

    val toInsert = hints.filter { it.text.isNotBlank() || it.imageUrl != null }
    if (toInsert.isNotEmpty()) {
        supabaseClient.from("hints")
            .insert(toInsert.map { hint ->
                HintInsertEntity(
                    locationId = locationId,
                    hintIndex = hint.hintIndex,
                    text = hint.text,
                    pointCost = hint.pointCost,
                    imageUrl = hint.imageUrl,
                )
            })
    }
    Unit
}

actual suspend fun updateAdventureLocationPointValue(
    locationId: Long,
    pointValue: Int,
): Unit = withContext(Dispatchers.IO) {
    supabaseClient.from("adventure_locations")
        .update(mapOf("point_value" to pointValue)) {
            filter { eq("id", locationId) }
        }
}

actual suspend fun updateAdventureLocationTimeLimitSeconds(
    locationId: Long,
    timeLimitSeconds: Int?,
): Unit = withContext(Dispatchers.IO) {
    supabaseClient.from("adventure_locations")
        .update(mapOf("time_limit_seconds" to timeLimitSeconds)) {
            filter { eq("id", locationId) }
        }
}

actual suspend fun uploadHintImage(imageBytes: ByteArray): String = withContext(Dispatchers.IO) {
    val fileName = "hint_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
    supabaseClient.storage.from("hint-images").upload(fileName, imageBytes)
    supabaseClient.storage.from("hint-images").publicUrl(fileName)
}

actual suspend fun createQuizQuestion(adventureId: String, orderIndex: Int, draft: QuizQuestionDraft): QuizQuestion = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLong()

    val createdQuestion = supabaseClient.from("quiz_questions")
        .insert(
            QuizQuestionInsertEntity(
                adventureId = numericAdventureId,
                questionText = draft.questionText,
                orderIndex = orderIndex,
            ),
        ) { select() }
        .decodeSingle<QuizQuestionEntity>()

    val answerInserts = draft.answers
        .filter { it.answerText.isNotBlank() }
        .map { answerDraft ->
            QuizAnswerInsertEntity(
                questionId = createdQuestion.id,
                answerText = answerDraft.answerText,
                isCorrect = answerDraft.isCorrect,
                answerOrder = answerDraft.answerOrder,
            )
        }

    val answers = if (answerInserts.isNotEmpty()) {
        supabaseClient.from("quiz_answers")
            .insert(answerInserts) { select() }
            .decodeList<QuizAnswerEntity>()
            .map { it.toQuizAnswer() }
    } else emptyList()

    createdQuestion.toQuizQuestion(answers)
}

actual suspend fun deleteQuizQuestion(questionId: Long): Unit = withContext(Dispatchers.IO) {
    // Delete answers first in case there is no CASCADE on the FK
    supabaseClient.from("quiz_answers")
        .delete {
            filter { eq("question_id", questionId) }
        }
    supabaseClient.from("quiz_questions")
        .delete {
            filter { eq("id", questionId) }
        }
    Unit
}

actual suspend fun getLeaderboard(period: LeaderboardPeriod, limit: Int): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
    supabaseClient.postgrest.rpc(
        "get_leaderboard_by_period",
        buildJsonObject {
            put("p_period", period.name.lowercase())
            put("p_limit", limit)
        },
    ).decodeList<LeaderboardEntryEntity>()
        .map { it.displayName to it.points.toInt() }
}

actual suspend fun submitAdventureFeedback(draft: AdventureFeedbackDraft): AdventureFeedback = withContext(Dispatchers.IO) {
    try {
        val numericAdventureId = draft.adventureId.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid adventure id: ${draft.adventureId}")

        val existingFeedback = supabaseClient.from("adventure_feedback")
            .select()
            .decodeList<AdventureFeedbackEntity>()
            .find { it.attemptId == draft.attemptId }

        if (existingFeedback != null) {
            supabaseClient.from("adventure_feedback")
                .update(
                    AdventureFeedbackUpdateEntity(
                        difficultyRating = draft.difficultyRating,
                        overallRating = draft.overallRating,
                        customFeedback = draft.customFeedback,
                        updatedAt = nowIso8601(),
                    ),
                ) {
                    filter { eq("id", existingFeedback.id) }
                    select()
                }
                .decodeSingle<AdventureFeedbackEntity>()
                .toAdventureFeedback()
        } else {
            supabaseClient.from("adventure_feedback")
                .insert(
                    AdventureFeedbackInsertEntity(
                        attemptId = draft.attemptId,
                        adventureId = numericAdventureId,
                        userId = draft.userId,
                        difficultyRating = draft.difficultyRating,
                        overallRating = draft.overallRating,
                        customFeedback = draft.customFeedback,
                    ),
                ) {
                    select()
                }
                .decodeSingle<AdventureFeedbackEntity>()
                .toAdventureFeedback()
        }
    } catch (e: Exception) {
        println("Error submitting adventure feedback: ${e.message}")
        e.printStackTrace()
        throw e
    }
}

// ============================================================================
// CONNECTIVITY IMPLEMENTATIONS
// ============================================================================

actual fun isNetworkAvailable(): Boolean {
    val cm = AndroidSessionStorage.context
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

actual fun observeConnectivity(onChange: (isConnected: Boolean) -> Unit): () -> Unit {
    val cm = AndroidSessionStorage.context
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = onChange(true)
        override fun onLost(network: Network) = onChange(false)
    }
    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    cm.registerNetworkCallback(request, callback)
    return { cm.unregisterNetworkCallback(callback) }
}
