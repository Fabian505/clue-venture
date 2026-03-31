package com.clueventure.service

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLDistanceFilterNone
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject

actual class LocationService {

    private val locationManager = CLLocationManager()

    actual val locationUpdates: Flow<LocationCoordinate> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                (didUpdateLocations.lastOrNull() as? CLLocation)?.let { location ->
                    trySend(
                        LocationCoordinate(
                            latitude = location.coordinate.useContents { latitude },
                            longitude = location.coordinate.useContents { longitude }
                        )
                    )
                }
            }

            override fun locationManager(
                manager: CLLocationManager,
                didFailWithError: NSError
            ) {
                // Silently ignore transient errors; the flow continues.
            }
        }

        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = kCLDistanceFilterNone
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }
    }

    actual suspend fun getLastKnownLocation(): LocationCoordinate? =
        locationManager.location?.let { location ->
            LocationCoordinate(
                latitude = location.coordinate.useContents { latitude },
                longitude = location.coordinate.useContents { longitude }
            )
        }
}
