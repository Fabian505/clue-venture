package com.clueventure.service

import kotlinx.coroutines.flow.Flow

/**
 * Represents a geographic coordinate.
 */
data class LocationCoordinate(
    val latitude: Double,
    val longitude: Double
)

/**
 * Platform-specific service for accessing device GPS location.
 *
 * Each platform provides its own `actual` implementation:
 * - Android: uses FusedLocationProviderClient (Google Play Services)
 * - iOS: uses CLLocationManager via CoreLocation
 */
expect class LocationService() {
    /**
     * A cold [Flow] that emits the device's current location whenever it changes.
     * Collect this flow to start receiving location updates; cancelling the
     * coroutine scope stops the updates automatically.
     */
    val locationUpdates: Flow<LocationCoordinate>

    /**
     * Returns the last known location, or null if unavailable.
     */
    suspend fun getLastKnownLocation(): LocationCoordinate?
}
