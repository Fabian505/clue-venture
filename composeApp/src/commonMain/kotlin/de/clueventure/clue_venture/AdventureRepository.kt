package de.clueventure.clue_venture

expect suspend fun getAdventures(): List<Adventure>

expect suspend fun createAdventure(draft: AdventureDraft): Adventure

expect suspend fun getAdventureLocations(adventureId: String): List<AdventureLocation>
