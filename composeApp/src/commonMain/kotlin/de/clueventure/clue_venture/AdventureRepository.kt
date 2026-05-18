package de.clueventure.clue_venture

expect fun currentTimeMillis(): Long

expect suspend fun getAdventures(): List<Adventure>

expect suspend fun createAdventure(draft: AdventureDraft): Adventure

expect suspend fun deleteAdventure(adventureId: String)

expect suspend fun getAdventureLocations(adventureId: String): List<AdventureLocation>

expect suspend fun deleteAdventureLocation(adventureId: String, orderIndex: Int)

expect suspend fun reorderAdventureLocations(adventureId: String, orderedCurrentIndexes: List<Int>)

expect suspend fun updateAdventure(adventureId: String, draft: AdventureMetadataDraft): Adventure

expect suspend fun appendAdventureLocations(adventureId: String, locations: List<AdventureLocationDraft>): List<AdventureLocation>

expect suspend fun getWalkingRouteDistanceMeters(points: List<GeoPoint>): Double?

// ============================================================================
// QUIZ REPOSITORY FUNCTIONS
// ============================================================================

expect suspend fun getQuizQuestions(adventureId: String): List<QuizQuestion>

expect suspend fun getQuizQuestionCount(adventureId: String): Int

expect suspend fun getQuizQuestionByIndex(adventureId: String, index: Int): QuizQuestion?

expect suspend fun getQuizAnswers(questionId: Long): List<QuizAnswer>

expect suspend fun submitQuizAnswer(attemptId: Long, questionId: Long, answerId: Long): Boolean

expect suspend fun recordQuizAnswerEvaluation(event: QuizAnswerEvaluationEvent)

// ============================================================================
// AUTHENTICATION REPOSITORY FUNCTIONS
// ============================================================================

expect suspend fun authenticateUser(email: String, password: String): User?

expect suspend fun registerUser(email: String, password: String, username: String): User?

expect suspend fun getCurrentUser(): User?

expect suspend fun logoutUser()

expect suspend fun getUserProfile(userId: String): UserProfile?

// ============================================================================
// ADVENTURE ATTEMPT REPOSITORY FUNCTIONS
// ============================================================================

expect suspend fun startAdventureAttempt(adventureId: String, userId: String): AdventureAttempt

expect suspend fun markAttemptCompleted(attemptId: Long)

expect suspend fun finishAdventureAttempt(attemptId: Long, userId: String, startedAt: String, adventure: Adventure): Int

expect suspend fun cancelAdventureAttempt(attemptId: Long)

expect suspend fun updateUserProgress(attemptId: Long, checkpointIndex: Int, userLocation: GeoPointState?): UserProgress

expect suspend fun getUserProgress(attemptId: Long): UserProgress?

expect suspend fun getCurrentAttemptForAdventure(adventureId: String, userId: String): AdventureAttempt?

expect suspend fun submitAdventureFeedback(draft: AdventureFeedbackDraft): AdventureFeedback

// ============================================================================
// HINTS & POINTS REPOSITORY FUNCTIONS
// ============================================================================

expect suspend fun getHints(locationId: Long): List<Hint>

expect suspend fun saveHintsForLocation(locationId: Long, hints: List<HintDraft>)

expect suspend fun updateAdventureLocationPointValue(locationId: Long, pointValue: Int)

expect suspend fun uploadHintImage(imageBytes: ByteArray): String

expect suspend fun getUserPoints(userId: String): Int

expect suspend fun updateUserPoints(userId: String, pointsDelta: Int)

expect suspend fun getLeaderboard(period: LeaderboardPeriod = LeaderboardPeriod.ALL, limit: Int = 50): List<Pair<String, Int>>

// ============================================================================
// QUIZ MANAGEMENT REPOSITORY FUNCTIONS
// ============================================================================

expect suspend fun createQuizQuestion(adventureId: String, orderIndex: Int, draft: QuizQuestionDraft): QuizQuestion

expect suspend fun deleteQuizQuestion(questionId: Long)

// ============================================================================
// CONNECTIVITY
// ============================================================================

expect fun isNetworkAvailable(): Boolean

expect fun observeConnectivity(onChange: (isConnected: Boolean) -> Unit): () -> Unit
