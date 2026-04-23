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
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
)

@Serializable
data class AdventureInsertEntity(
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
)

@Serializable
data class AdventureUpdateEntity(
    val title: String,
    val summary: String,
    @SerialName("start_latitude") val startLatitude: Double,
    @SerialName("start_longitude") val startLongitude: Double,
    val difficulty: String? = null,
    @SerialName("estimated_duration_minutes") val estimatedDurationMinutes: Int? = null,
)

@Serializable
data class AdventureLocationInsertEntity(
    @SerialName("adventure_id") val adventureId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("order_index") val orderIndex: Int,
)

@Serializable
data class AdventureLocationEntity(
    @SerialName("adventure_id") val adventureId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("order_index") val orderIndex: Int,
)

fun AdventureEntity.toAdventure(): Adventure = Adventure(
    id = id.toString(),
    title = title,
    summary = summary,
    startPoint = GeoPoint(latitude = startLatitude, longitude = startLongitude),
    difficulty = difficulty,
    estimatedDurationMinutes = estimatedDurationMinutes,
)

fun AdventureLocationEntity.toAdventureLocation(): AdventureLocation = AdventureLocation(
    name = name,
    point = GeoPoint(latitude = latitude, longitude = longitude),
    orderIndex = orderIndex,
)
