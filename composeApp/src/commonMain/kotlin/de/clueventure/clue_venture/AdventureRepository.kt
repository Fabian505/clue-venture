package de.clueventure.clue_venture

expect suspend fun getAdventures(): List<Adventure>

expect suspend fun createAdventure(draft: AdventureDraft): Adventure

expect suspend fun getAdventureLocations(adventureId: String): List<AdventureLocation>

expect suspend fun deleteAdventureLocation(adventureId: String, orderIndex: Int)

expect suspend fun updateAdventure(adventureId: String, draft: AdventureMetadataDraft): Adventure

expect suspend fun appendAdventureLocations(adventureId: String, locations: List<AdventureLocationDraft>): List<AdventureLocation>
