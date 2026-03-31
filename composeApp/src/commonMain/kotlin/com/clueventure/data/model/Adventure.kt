package com.clueventure.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Adventure(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerialName("waypoint_count")
    val waypointCount: Int = 0,
    @SerialName("difficulty_level")
    val difficultyLevel: String = "medium"
)
