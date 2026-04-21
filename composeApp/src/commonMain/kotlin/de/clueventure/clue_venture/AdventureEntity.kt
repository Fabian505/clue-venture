package de.clueventure.clue_venture

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventureEntity(
    val id: Long,
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
)

fun AdventureEntity.toAdventure(): Adventure = Adventure(
    id = id.toString(),
    title = title,
    summary = summary,
    startPoint = GeoPoint(latitude = startLatitude, longitude = startLongitude),
)
