package de.clueventure.clue_venture

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun getAdventures(): List<Adventure> = withContext(Dispatchers.IO) {
    supabaseClient.from("adventures")
        .select()
        .decodeList<AdventureEntity>()
        .map { it.toAdventure() }
}

actual suspend fun createAdventure(draft: AdventureDraft): Adventure = withContext(Dispatchers.IO) {
    val createdAdventure = supabaseClient.from("adventures")
        .insert(
            AdventureInsertEntity(
                title = draft.title,
                summary = draft.summary,
                startLatitude = draft.startPoint.latitude,
                startLongitude = draft.startPoint.longitude,
                difficulty = draft.difficulty,
                estimatedDurationMinutes = draft.estimatedDurationMinutes,
            ),
        ) {
            select()
        }
        .decodeSingle<AdventureEntity>()

    if (draft.locations.isNotEmpty()) {
        supabaseClient.from("adventure_locations")
            .insert(
                draft.locations.mapIndexed { index, location ->
                    AdventureLocationInsertEntity(
                        adventureId = createdAdventure.id,
                        name = location.name,
                        latitude = location.point.latitude,
                        longitude = location.point.longitude,
                        orderIndex = index,
                    )
                },
            )
    }

    createdAdventure.toAdventure().copy(
        locations = draft.locations.mapIndexed { index, location ->
            AdventureLocation(
                name = location.name,
                point = location.point,
                orderIndex = index,
            )
        },
    )
}

actual suspend fun deleteAdventure(adventureId: String): Unit = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    supabaseClient.from("adventures")
        .delete {
            filter {
                eq("id", numericAdventureId)
            }
        }

    Unit
}

actual suspend fun getAdventureLocations(adventureId: String): List<AdventureLocation> = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext emptyList()

    supabaseClient.from("adventure_locations")
        .select()
        .decodeList<AdventureLocationEntity>()
        .asSequence()
        .filter { it.adventureId == numericAdventureId }
        .sortedBy { it.orderIndex }
        .map { it.toAdventureLocation() }
        .toList()
}

actual suspend fun deleteAdventureLocation(adventureId: String, orderIndex: Int): Unit = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    supabaseClient.from("adventure_locations")
        .delete {
            filter {
                eq("adventure_id", numericAdventureId)
                eq("order_index", orderIndex)
            }
        }

    Unit
}

actual suspend fun reorderAdventureLocations(adventureId: String, orderedCurrentIndexes: List<Int>): Unit = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    if (orderedCurrentIndexes.isEmpty()) {
        return@withContext
    }

    val currentLocations = getAdventureLocations(adventureId)
    val currentOrderIndexes = currentLocations.map { it.orderIndex }

    if (orderedCurrentIndexes.size != currentOrderIndexes.size || orderedCurrentIndexes.toSet() != currentOrderIndexes.toSet()) {
        throw IllegalArgumentException("Reorder input does not match existing locations.")
    }

    val maxOrderIndex = currentOrderIndexes.maxOrNull() ?: -1
    val temporaryBase = maxOrderIndex + currentOrderIndexes.size + 1000

    orderedCurrentIndexes.forEachIndexed { position, currentOrderIndex ->
        supabaseClient.from("adventure_locations")
            .update(AdventureLocationOrderUpdateEntity(orderIndex = temporaryBase + position)) {
                filter {
                    eq("adventure_id", numericAdventureId)
                    eq("order_index", currentOrderIndex)
                }
            }
    }

    orderedCurrentIndexes.indices.forEach { position ->
        supabaseClient.from("adventure_locations")
            .update(AdventureLocationOrderUpdateEntity(orderIndex = position)) {
                filter {
                    eq("adventure_id", numericAdventureId)
                    eq("order_index", temporaryBase + position)
                }
            }
    }

    Unit
}

actual suspend fun updateAdventure(adventureId: String, draft: AdventureMetadataDraft): Adventure = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid adventure id: $adventureId")

    supabaseClient.from("adventures")
        .update(
            AdventureUpdateEntity(
                title = draft.title,
                summary = draft.summary,
                startLatitude = draft.startPoint.latitude,
                startLongitude = draft.startPoint.longitude,
                difficulty = draft.difficulty,
                estimatedDurationMinutes = draft.estimatedDurationMinutes,
            ),
        ) {
            filter {
                eq("id", numericAdventureId)
            }
            select()
        }
        .decodeSingle<AdventureEntity>()
        .toAdventure()
}

actual suspend fun appendAdventureLocations(
    adventureId: String,
    locations: List<AdventureLocationDraft>,
): List<AdventureLocation> = withContext(Dispatchers.IO) {
    val numericAdventureId = adventureId.toLongOrNull() ?: return@withContext emptyList()
    if (locations.isEmpty()) {
        return@withContext emptyList()
    }

    val nextOrderIndex = getAdventureLocations(adventureId)
        .maxOfOrNull { it.orderIndex }
        ?.plus(1)
        ?: 0

    val locationInserts = locations.mapIndexed { index, location ->
        AdventureLocationInsertEntity(
            adventureId = numericAdventureId,
            name = location.name,
            latitude = location.point.latitude,
            longitude = location.point.longitude,
            orderIndex = nextOrderIndex + index,
        )
    }

    supabaseClient.from("adventure_locations")
        .insert(locationInserts)

    locationInserts.map { inserted ->
        AdventureLocation(
            name = inserted.name,
            point = GeoPoint(latitude = inserted.latitude, longitude = inserted.longitude),
            orderIndex = inserted.orderIndex,
        )
    }
}
