package fr.eurecom.flowie.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import fr.eurecom.flowie.BuildConfig

/**
 * Composable wrapping a MapLibre (Mapbox namespace) MapView.
 *
 * Step 2 additions:
 * - Optional follow user location (so Contribute can keep the map draggable)
 * - Camera callbacks for "centre pin" UX (get centre LatLng on move/idle)
 */
@Composable
fun MapTilerMap(
    modifier: Modifier = Modifier,
    center: LatLng,
    zoom: Double = 14.0,
    followUserLocation: Boolean = true,
    onMapReady: ((com.mapbox.mapboxsdk.maps.MapboxMap) -> Unit)? = null,
    onCameraMove: ((LatLng) -> Unit)? = null,
    onCameraIdle: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()

    val cameraMoveCallback = remember(onCameraMove) { onCameraMove }
    val cameraIdleCallback = remember(onCameraIdle) { onCameraIdle }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Mapbox.getInstance(ctx)

            mapView.apply {
                getMapAsync { map ->
                    val styleUrl =
                        "https://api.maptiler.com/maps/streets/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

                    map.setStyle(styleUrl) { style ->
                        map.cameraPosition = CameraPosition.Builder()
                            .target(center)
                            .zoom(zoom)
                            .build()

                        // Location component (dot) — tracking optional
                        val locationComponent = map.locationComponent
                        locationComponent.activateLocationComponent(
                            LocationComponentActivationOptions.builder(context, style).build()
                        )

                        val hasFineLocation =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        if (hasFineLocation) {
                            locationComponent.isLocationComponentEnabled = true
                            locationComponent.renderMode = RenderMode.COMPASS

                            locationComponent.cameraMode =
                                if (followUserLocation) CameraMode.TRACKING else CameraMode.NONE
                        }

                        // Camera listeners for centre-pin UX (null-safe)
                        map.addOnCameraMoveListener {
                            val target: LatLng = map.cameraPosition?.target ?: return@addOnCameraMoveListener
                            cameraMoveCallback?.invoke(target)
                        }

                        map.addOnCameraIdleListener {
                            val target: LatLng = map.cameraPosition?.target ?: return@addOnCameraIdleListener
                            cameraIdleCallback?.invoke(target)
                        }

                        onMapReady?.invoke(map)
                    }
                }
            }
        }
    )
}