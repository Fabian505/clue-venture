package com.clueventure.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual class LocationService {

    private val fusedClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    /**
     * A cold [Flow] that emits the device's current location whenever it changes.
     *
     * **Prerequisite:** [ACCESS_FINE_LOCATION] or [ACCESS_COARSE_LOCATION] permission must be
     * granted by the user before collecting this flow.  Request permissions in your Activity
     * (e.g. via [ActivityResultContracts.RequestMultiplePermissions]) before starting collection.
     */
    actual val locationUpdates: Flow<LocationCoordinate> = callbackFlow {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        ).setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.toCoordinate()?.let { trySend(it) }
            }
        }

        @SuppressLint("MissingPermission")
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    @SuppressLint("MissingPermission")
    actual suspend fun getLastKnownLocation(): LocationCoordinate? =
        fusedClient.lastLocation.await()?.toCoordinate()

    private fun Location.toCoordinate() = LocationCoordinate(latitude, longitude)

    companion object {
        lateinit var applicationContext: Context
            private set

        /** Must be called once in Application.onCreate() or MainActivity.onCreate(). */
        fun init(context: Context) {
            applicationContext = context.applicationContext
        }

        private const val UPDATE_INTERVAL_MS = 5_000L
        private const val FASTEST_INTERVAL_MS = 2_000L
    }
}
