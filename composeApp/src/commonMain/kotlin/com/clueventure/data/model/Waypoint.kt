package com.clueventure.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Waypoint(
    val id: String,
    @SerialName("adventure_id")
    val adventureId: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("order_index")
    val orderIndex: Int,
    @SerialName("radius_meters")
    val radiusMeters: Double = 50.0,
    @SerialName("clue_text")
    val clueText: String? = null
)
