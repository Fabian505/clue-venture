package de.clueventure.clue_venture

/**
 * LocationService provides platform-specific GPS location tracking
 * with periodic updates at configurable intervals.
 */
interface LocationService {
    /**
     * Start tracking device location with periodic updates
     * @param interval Update interval in milliseconds (e.g., 5000 for 5 seconds)
     * @param onLocationUpdate Callback invoked when location is updated
     */
    suspend fun startLocationTracking(
        interval: Long = 10000,
        onLocationUpdate: (GeoPointState) -> Unit,
    )

    /**
     * Stop tracking device location
     */
    suspend fun stopLocationTracking()

    /**
     * Get current device location (single request)
     * @return Current location or null if unavailable
     */
    suspend fun getCurrentLocation(): GeoPointState?

    /**
     * Request location permissions from the user
     * @return true if permissions were granted
     */
    suspend fun requestLocationPermissions(): Boolean

    /**
     * Check if location permissions are granted
     */
    suspend fun hasLocationPermissions(): Boolean
}

// Global location service instance (initialized per platform)
expect fun getLocationService(): LocationService
