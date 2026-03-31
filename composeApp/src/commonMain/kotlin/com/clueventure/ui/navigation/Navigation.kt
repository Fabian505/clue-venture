package com.clueventure.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.clueventure.data.model.Adventure
import com.clueventure.data.model.Waypoint
import com.clueventure.ui.screens.ActiveAdventureScreen
import com.clueventure.ui.screens.AdventureListScreen
import com.clueventure.ui.screens.QuizQuestionScreen

sealed class Screen {
    data object AdventureList : Screen()
    data class ActiveAdventure(val adventure: Adventure) : Screen()
    data class QuizQuestion(val waypoint: Waypoint, val adventure: Adventure) : Screen()
}

@Composable
fun ClueVentureNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.AdventureList) }

    when (val screen = currentScreen) {
        is Screen.AdventureList -> {
            AdventureListScreen(
                onAdventureSelected = { adventure ->
                    currentScreen = Screen.ActiveAdventure(adventure)
                }
            )
        }

        is Screen.ActiveAdventure -> {
            ActiveAdventureScreen(
                adventure = screen.adventure,
                onNavigateBack = { currentScreen = Screen.AdventureList },
                onWaypointReached = { waypoint ->
                    currentScreen = Screen.QuizQuestion(
                        waypoint = waypoint,
                        adventure = screen.adventure
                    )
                }
            )
        }

        is Screen.QuizQuestion -> {
            QuizQuestionScreen(
                waypointId = screen.waypoint.id,
                waypointTitle = screen.waypoint.title,
                onNavigateBack = {
                    currentScreen = Screen.ActiveAdventure(screen.adventure)
                },
                onAnsweredCorrectly = {
                    currentScreen = Screen.ActiveAdventure(screen.adventure)
                }
            )
        }
    }
}
