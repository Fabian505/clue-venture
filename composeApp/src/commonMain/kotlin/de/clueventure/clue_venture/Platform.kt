package de.clueventure.clue_venture

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform