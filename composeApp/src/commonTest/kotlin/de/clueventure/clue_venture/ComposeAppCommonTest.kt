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
}