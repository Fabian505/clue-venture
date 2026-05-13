package de.clueventure.clue_venture

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun unlockedQuestionsAreBasedOnRouteDistance() {
        assertEquals(0, unlockedQuestionsForRouteDistance(null))
        assertEquals(0, unlockedQuestionsForRouteDistance(199.9))
        assertEquals(1, unlockedQuestionsForRouteDistance(200.0))
        assertEquals(2, unlockedQuestionsForRouteDistance(449.0))
    }

    @Test
    fun unlockedQuestionSlotsOnlyIncreaseWhenRouteDistanceChanges() {
        assertEquals(0, updateUnlockedQuestionSlots(0, null))
        assertEquals(1, updateUnlockedQuestionSlots(0, 200.0))
        assertEquals(2, updateUnlockedQuestionSlots(1, 450.0))
        assertEquals(2, updateUnlockedQuestionSlots(2, 100.0))
    }

    @Test
    fun availableQuestionCountShowsAllQuestionsMinusAnswered() {
        // All questions are available from the start (unlockedQuestionSlots is not considered)
        assertEquals(0, calculateAvailableQuestionCount(totalQuestions = 0, unlockedQuestionSlots = 5, answeredQuestionCount = 0))
        assertEquals(5, calculateAvailableQuestionCount(totalQuestions = 5, unlockedQuestionSlots = 2, answeredQuestionCount = 0))
        assertEquals(3, calculateAvailableQuestionCount(totalQuestions = 5, unlockedQuestionSlots = 3, answeredQuestionCount = 2))
        assertEquals(0, calculateAvailableQuestionCount(totalQuestions = 2, unlockedQuestionSlots = 1, answeredQuestionCount = 2))
    }
}