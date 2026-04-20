package de.clueventure.clue_venture

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(val id: Int, val name: String)
