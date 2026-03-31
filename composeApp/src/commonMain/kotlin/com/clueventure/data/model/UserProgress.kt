package com.clueventure.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("adventure_id")
    val adventureId: String,
    @SerialName("current_waypoint_index")
    val currentWaypointIndex: Int = 0,
    @SerialName("score")
    val score: Int = 0,
    @SerialName("is_completed")
    val isCompleted: Boolean = false,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("completed_waypoints")
    val completedWaypoints: List<String> = emptyList()
)
