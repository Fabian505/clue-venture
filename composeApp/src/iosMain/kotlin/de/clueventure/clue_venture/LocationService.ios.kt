package de.clueventure.clue_venture

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegate
import platform.CoreLocation.CLLocationDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.Foundation.NSLocale
import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.darwin.NSObject
import java.time.Instant

/**
 * iOS implementation of LocationService using CLLocationManager
 */
@OptIn(ExperimentalForeignApi::class)
class IosLocationService : LocationService, CLLocationDelegateProtocol {
    private val locationManager = CLLocationManager()
    private var currentUpdateCallback: ((GeoPointState) -> Unit)? = null
    private var updateInterval: Long = 10000

    init {
        locationManager.setDelegate(this)
        locationManager.setDesiredAccuracy(kCLLocationAccuracyBest)
    }

    override suspend fun startLocationTracking(
        interval: Long,
        onLocationUpdate: (GeoPointState) -> Unit,
    ) {
        withContext(Dispatchers.Main) {
            currentUpdateCallback = onLocationUpdate
            updateInterval = interval

            // Request permission if needed
            if (!hasLocationPermissions()) {
                locationManager.requestWhenInUseAuthorization()
            }

            // Start updating location
            locationManager.startUpdatingLocation()
        }
    }

    override suspend fun stopLocationTracking() {
        withContext(Dispatchers.Main) {
            locationManager.stopUpdatingLocation()
            currentUpdateCallback = null
        }
    }

    override suspend fun getCurrentLocation(): GeoPointState? {
        return withContext(Dispatchers.Main) {
            locationManager.location?.let { location ->
                location.useContents {
                    GeoPointState(
                        point = GeoPoint(
                            latitude = coordinate.latitude,
                            longitude = coordinate.longitude,
                        ),
                        timestamp = Instant.now().toString(),
                        accuracy = horizontalAccuracy.toFloat(),
                    )
                }
            }
        }
    }

    override suspend fun requestLocationPermissions(): Boolean {
        return withContext(Dispatchers.Main) {
            locationManager.requestWhenInUseAuthorization()
            hasLocationPermissions()
        }
    }

    override suspend fun hasLocationPermissions(): Boolean {
        return withContext(Dispatchers.Main) {
            val status = CLLocationManager.authorizationStatus()
            status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
        }
    }

    // CLLocationManagerDelegate interface
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        @Suppress("UNCHECKED_CAST")
        val locations = didUpdateLocations as List<CLLocation>
        locations.lastOrNull()?.let { location ->
            location.useContents {
                val geoPointState = GeoPointState(
                    point = GeoPoint(
                        latitude = coordinate.latitude,
                        longitude = coordinate.longitude,
                    ),
                    timestamp = Instant.now().toString(),
                    accuracy = horizontalAccuracy.toFloat(),
                )
                currentUpdateCallback?.invoke(geoPointState)
            }
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: Error) {
        println("Location tracking error: ${didFailWithError.localizedDescription}")
    }

    override fun locationManager(
        manager: CLLocationManager,
        didChangeAuthorizationStatus: CLAuthorizationStatus,
    ) {
        when (didChangeAuthorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                // Permissions granted, start tracking
                locationManager.startUpdatingLocation()
            }

            else -> {
                // Permissions denied, stop tracking
                locationManager.stopUpdatingLocation()
            }
        }
    }
}

// Global location service singleton for iOS
private var _locationService: IosLocationService? = null

actual fun getLocationService(): LocationService {
    if (_locationService == null) {
        _locationService = IosLocationService()
    }
    return _locationService!!
}
