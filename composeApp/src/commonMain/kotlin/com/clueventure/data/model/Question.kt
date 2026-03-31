package com.clueventure.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    @SerialName("waypoint_id")
    val waypointId: String,
    val text: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    val options: List<String>,
    @SerialName("hint_text")
    val hintText: String? = null,
    @SerialName("points_value")
    val pointsValue: Int = 10
)
