package com.clueventure.data.repository

import com.clueventure.data.model.Adventure
import com.clueventure.data.model.Question
import com.clueventure.data.model.UserProgress
import com.clueventure.data.model.Waypoint
import com.clueventure.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from

class AdventureRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getAdventures(): List<Adventure> =
        client.from("adventures").select().decodeList()

    suspend fun getActiveAdventures(): List<Adventure> =
        client.from("adventures")
            .select {
                filter { eq("is_active", true) }
            }
            .decodeList()

    suspend fun getAdventureById(id: String): Adventure =
        client.from("adventures")
            .select {
                filter { eq("id", id) }
            }
            .decodeSingle()

    suspend fun getWaypointsForAdventure(adventureId: String): List<Waypoint> =
        client.from("waypoints")
            .select {
                filter { eq("adventure_id", adventureId) }
                order("order_index")
            }
            .decodeList()

    suspend fun getQuestionsForWaypoint(waypointId: String): List<Question> =
        client.from("questions")
            .select {
                filter { eq("waypoint_id", waypointId) }
            }
            .decodeList()

    suspend fun getUserProgress(userId: String, adventureId: String): UserProgress? =
        client.from("user_progress")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("adventure_id", adventureId)
                }
            }
            .decodeSingleOrNull()

    suspend fun upsertUserProgress(progress: UserProgress): UserProgress =
        client.from("user_progress")
            .upsert(progress) {
                onConflict = "user_id,adventure_id"
            }
            .decodeSingle()
}
