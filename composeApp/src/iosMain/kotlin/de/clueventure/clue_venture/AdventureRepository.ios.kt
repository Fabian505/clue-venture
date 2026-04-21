package de.clueventure.clue_venture

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun getAdventures(): List<Adventure> = withContext(Dispatchers.IO) {
    try {
        supabaseClient.from("adventures")
            .select()
            .decodeList<AdventureEntity>()
            .map { it.toAdventure() }
    } catch (e: Exception) {
        sampleAdventures
    }
}
