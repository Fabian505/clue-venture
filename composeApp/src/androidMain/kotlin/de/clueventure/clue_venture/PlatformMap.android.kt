package de.clueventure.clue_venture

import android.Manifest
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.text.DecimalFormat

@Composable
actual fun PlatformMap(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                hasLocationPermission(context)
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                mapLibreMap = map
                map.setStyle("asset://style.json")
            }
        }
    }

    DisposableEffect(hasLocationPermission, mapLibreMap) {
        val map = mapLibreMap
        if (!hasLocationPermission || map == null) {
            return@DisposableEffect onDispose { }
        }

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            return@DisposableEffect onDispose { }
        }

        var bestLocation: Location? = null
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val markerOptionsList: MutableList<MarkerOptions> = ArrayList()
                val formatter = DecimalFormat("#.#####")
                val latlng: LatLng = LatLng(location.latitude, location.longitude)
                markerOptionsList.add(
                    MarkerOptions()
                        .position(latlng)
                        .snippet(formatter.format(latlng.latitude) + "`, " + formatter.format(latlng.longitude))
                    )

                if (bestLocation == null || isBetterLocation(location, bestLocation!!)) {
                    bestLocation = location
                    moveCameraToLocation(map, location)
                    map.addMarkers(markerOptionsList)
                    map.uiSettings.isZoomGesturesEnabled = true
                    map.uiSettings.isRotateGesturesEnabled = true

                }
            }

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit
        }

        val enabledProviders = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        ).filter(locationManager::isProviderEnabled)

        val lastKnown = enabledProviders
            .asSequence()
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }

        if (lastKnown != null) {
            bestLocation = lastKnown
            moveCameraToLocation(map, lastKnown)
        }

        enabledProviders.forEach { provider ->
            runCatching {
                locationManager.requestLocationUpdates(
                    provider,
                    2000L,
                    5f,
                    listener,
                    Looper.getMainLooper(),
                )
            }
        }

        onDispose {
            runCatching { locationManager.removeUpdates(listener) }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val callbacks = object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            override fun onTrimMemory(level: Int) {
                if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
                    mapView.onLowMemory()
                }
            }

            override fun onLowMemory() {
                mapView.onLowMemory()
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        context.applicationContext.registerComponentCallbacks(callbacks)
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            context.applicationContext.unregisterComponentCallbacks(callbacks)
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

private fun hasLocationPermission(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    return fineGranted || coarseGranted
}

private fun isBetterLocation(newLocation: Location, currentBest: Location): Boolean {
    val timeDelta = newLocation.time - currentBest.time
    if (timeDelta > 120_000L) return true
    if (timeDelta < -120_000L) return false

    val accuracyDelta = (newLocation.accuracy - currentBest.accuracy).toInt()
    val isMoreAccurate = accuracyDelta < 0
    val isSignificantlyLessAccurate = accuracyDelta > 200
    val isNewer = timeDelta > 0

    return when {
        isMoreAccurate -> true
        isNewer && !isSignificantlyLessAccurate -> true
        else -> false
    }
}

private fun moveCameraToLocation(map: MapLibreMap, location: Location) {
    map.cameraPosition = CameraPosition.Builder()
        .target(LatLng(location.latitude, location.longitude))
        .zoom(15.0)
        .build()
}
