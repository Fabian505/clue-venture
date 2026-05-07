package de.clueventure.clue_venture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Android implementation of LocationService using FusedLocationProviderClient
 */
class AndroidLocationService(private val context: Context) : LocationService {
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private var currentUpdateCallback: ((GeoPointState) -> Unit)? = null

    override suspend fun startLocationTracking(
        interval: Long,
        onLocationUpdate: (GeoPointState) -> Unit,
    ) {
        // Check and request permissions if needed
        if (!hasLocationPermissions()) {
            if (!requestLocationPermissions()) {
                println("Location permissions denied")
                return
            }
        }

        currentUpdateCallback = onLocationUpdate

        val locationRequest = LocationRequest.Builder(interval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(5f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val geoPointState = GeoPointState(
                        point = GeoPoint(latitude = location.latitude, longitude = location.longitude),
                        timestamp = Instant.now().toString(),
                        accuracy = location.accuracy,
                    )
                    onLocationUpdate(geoPointState)
                }
            }
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback!!,
                    null, // Looper (null = use default)
                )
            }
        } catch (e: Exception) {
            println("Error starting location tracking: ${e.message}")
        }
    }

    override suspend fun stopLocationTracking() {
        locationCallback?.let {
            try {
                fusedLocationProviderClient.removeLocationUpdates(it)
            } catch (e: Exception) {
                println("Error stopping location tracking: ${e.message}")
            }
        }
        locationCallback = null
        currentUpdateCallback = null
    }

    override suspend fun getCurrentLocation(): GeoPointState? {
        if (!hasLocationPermissions()) {
            return null
        }

        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = fusedLocationProviderClient.lastLocation.let { task ->
                    if (task.isSuccessful) {
                        task.result
                    } else {
                        null
                    }
                }

                location?.let {
                    GeoPointState(
                        point = GeoPoint(latitude = it.latitude, longitude = it.longitude),
                        timestamp = Instant.now().toString(),
                        accuracy = it.accuracy,
                    )
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error getting current location: ${e.message}")
            null
        }
    }

    override suspend fun requestLocationPermissions(): Boolean {
        // In Android, this would typically be handled by the Activity
        // For now, we check if permissions are already granted
        return hasLocationPermissions()
    }

    override suspend fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }
}

// Global location service singleton for Android
private var _locationService: AndroidLocationService? = null

actual fun getLocationService(): LocationService {
    if (_locationService == null) {
        // This would typically be provided via dependency injection
        // For now, we create a placeholder that requires context
        throw IllegalStateException("LocationService not initialized. Call initializeLocationService(context) first.")
    }
    return _locationService!!
}

fun initializeLocationService(context: Context) {
    _locationService = AndroidLocationService(context)
}
