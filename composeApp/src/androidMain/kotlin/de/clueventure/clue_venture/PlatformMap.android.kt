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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val INITIAL_MAP_ZOOM = 15.0
private const val MAX_MAP_ZOOM = 18.0
private const val LOCATION_SOURCE_ID = "current-location-source"
private const val LOCATION_LAYER_ID = "current-location-layer"

@Composable
actual fun PlatformMap(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasLocationPermission = granted && hasLocationPermission(context)
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                mapLibreMap = map
                map.setMaxZoomPreference(MAX_MAP_ZOOM)
                map.setStyle("asset://style.json") { style ->
                    ensureLocationLayer(style)
                }
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
                ?: return@DisposableEffect onDispose { }

        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        if (!fineLocationGranted) {
            return@DisposableEffect onDispose { }
        }

        var bestLocation: Location? = null
        var hasCenteredCamera = false

        fun updateLocationMarker(location: Location) {
            map.getStyle { style ->
                val source = style.getSourceAs<GeoJsonSource>(LOCATION_SOURCE_ID) ?: return@getStyle
                source.setGeoJson(Point.fromLngLat(location.longitude, location.latitude))
            }
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (bestLocation == null || isBetterLocation(location, bestLocation!!)) {
                    bestLocation = location
                    if (!hasCenteredCamera) {
                        moveCameraToLocation(map, location)
                        hasCenteredCamera = true
                    }
                    updateLocationMarker(location)
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
        ).filter { provider ->
            try {
                locationManager.isProviderEnabled(provider)
            } catch (_: SecurityException) {
                false
            }
        }

        val lastKnown = enabledProviders
            .asSequence()
            .mapNotNull { provider ->
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@mapNotNull null
                }
                try {
                    locationManager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                }
            }
            .maxByOrNull { it.time }

        if (lastKnown != null) {
            bestLocation = lastKnown
            moveCameraToLocation(map, lastKnown)
            hasCenteredCamera = true
            updateLocationMarker(lastKnown)
        }

        enabledProviders.forEach { provider ->
            try {
                locationManager.requestLocationUpdates(
                    provider,
                    2000L,
                    5f,
                    listener,
                    Looper.getMainLooper(),
                )
            } catch (_: SecurityException) {
                // Permission can be revoked while the screen is visible.
            }
        }

        onDispose {
            runCatching { locationManager.removeUpdates(listener) }
            map.getStyle { style ->
                style.getSourceAs<GeoJsonSource>(LOCATION_SOURCE_ID)
                    ?.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
            }
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

            @Deprecated("Kept for ComponentCallbacks compatibility")
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

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )

        if (!hasLocationPermission) {
            Text(
                text = "Der genaue Standort wird benötigt. Bitte präzise Standortfreigabe aktivieren.",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
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
        .zoom(INITIAL_MAP_ZOOM)
        .build()
}

private fun ensureLocationLayer(style: Style) {
    if (style.getSource(LOCATION_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(LOCATION_SOURCE_ID, FeatureCollection.fromFeatures(arrayOf())))
    }

    if (style.getLayer(LOCATION_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(LOCATION_LAYER_ID, LOCATION_SOURCE_ID)
                .withProperties(
                    circleRadius(6f),
                    circleColor("#1E88E5"),
                    circleStrokeColor("#FFFFFF"),
                    circleStrokeWidth(2f),
                ),
        )
    }
}

