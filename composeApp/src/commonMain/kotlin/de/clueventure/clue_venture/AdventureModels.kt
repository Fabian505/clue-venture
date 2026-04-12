package de.clueventure.clue_venture

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class Adventure(
    val id: String,
    val title: String,
    val summary: String,
    val startPoint: GeoPoint,
)

val sampleAdventures = listOf(
    Adventure(
        id = "old-town-mystery",
        title = "Geheimnis der Altstadt",
        summary = "Spaziere durch die Altstadtgassen und folge den ersten Spuren.",
        startPoint = GeoPoint(latitude = 52.5209, longitude = 13.4095),
    ),
    Adventure(
        id = "harbor-trail",
        title = "Hafenpfad",
        summary = "Ein Abenteuer zwischen Wasser, Kaimauern und versteckten Hinweisen.",
        startPoint = GeoPoint(latitude = 52.5141, longitude = 13.3567),
    ),
    Adventure(
        id = "city-park-code",
        title = "Code im Stadtpark",
        summary = "Knacke die Rätsel an den Wegen und finde den nächsten Treffpunkt.",
        startPoint = GeoPoint(latitude = 52.5018, longitude = 13.4471),
    ),
    Adventure(
        id = "museum-chase",
        title = "Museum Chase",
        summary = "Eine kurze Jagd mit einem Startpunkt in der Nähe der Museumsinsel.",
        startPoint = GeoPoint(latitude = 52.5169, longitude = 13.4010),
    ),
    Adventure(
        id = "street-art-hunt",
        title = "Street Art Jagd",
        summary = "Entdecke die verborgenen Kunstwerke und finde den nächsten Hinweis.",
        startPoint = GeoPoint(latitude = 48.44337, longitude = 8.68579),
    ),
)

fun GeoPoint.distanceTo(other: GeoPoint): Double {
    val earthRadiusMeters = 6_371_000.0
    val latitudeDistance = (other.latitude - latitude) * PI / 180.0
    val longitudeDistance = (other.longitude - longitude) * PI / 180.0
    val startLatitude = latitude * PI / 180.0
    val endLatitude = other.latitude * PI / 180.0

    val a = sin(latitudeDistance / 2).pow(2) +
        sin(longitudeDistance / 2).pow(2) * cos(startLatitude) * cos(endLatitude)
    val c = 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))

    return earthRadiusMeters * c
}







