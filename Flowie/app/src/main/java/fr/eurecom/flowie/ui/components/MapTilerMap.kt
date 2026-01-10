package fr.eurecom.flowie.ui.components

import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.Mapbox
import fr.eurecom.flowie.BuildConfig
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

/*
 * Composable wrapping a MapLibre (Mapbox namespace) MapView.
 * Displays a MapTiler map and optionally exposes the MapboxMap instance.
 */
@Composable
fun MapTilerMap(
    modifier: Modifier = Modifier,
    center: LatLng,
    zoom: Double = 14.0,
    onMapReady: ((com.mapbox.mapboxsdk.maps.MapboxMap) -> Unit)? = null
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // ✅ Initialisation MapLibre (namespace Mapbox)
            Mapbox.getInstance(ctx)

            mapView.apply {
                getMapAsync { map ->
                    val styleUrl =
                        "https://api.maptiler.com/maps/streets/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

                    map.setStyle(styleUrl) { style ->

                        // 🎯 Position initiale
                        map.cameraPosition = CameraPosition.Builder()
                            .target(center)
                            .zoom(zoom)
                            .build()

                        // 📍 ACTIVER LA POSITION UTILISATEUR
                        val locationComponent = map.locationComponent
                        locationComponent.activateLocationComponent(
                            LocationComponentActivationOptions.builder(context, style).build()
                        )

                        if (
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            locationComponent.isLocationComponentEnabled = true
                            locationComponent.cameraMode = CameraMode.TRACKING
                            locationComponent.renderMode = RenderMode.COMPASS
                        }


                        // 🔁 expose la map à l'extérieur
                        onMapReady?.invoke(map)
                    }

                }
            }
        }
    )
}