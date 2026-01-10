package fr.eurecom.flowie

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import fr.eurecom.flowie.navigation.NavGraph
import fr.eurecom.flowie.model.StepRepository
import fr.eurecom.flowie.sensors.StepCounterManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.WellKnownTileServer
import android.Manifest

/*
 * Main entry point of the application.
 * Handles permissions, step counting, map initialization, and UI setup.
 */
class MainActivity : ComponentActivity() {

    private val activityPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            Log.d("PERMISSION", "Activity recognition granted = $granted")
        }
    private lateinit var stepCounterManager: StepCounterManager

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            Log.d("LOCATION", "Location permission granted = $granted")
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stepCounterManager = StepCounterManager(this) { steps ->
            StepRepository.updateSteps(steps)
        }
        stepCounterManager.start()

        Mapbox.getInstance(
            applicationContext,
            BuildConfig.MAPTILER_API_KEY,
            WellKnownTileServer.MapTiler
        )

        setContent {
            MaterialTheme {
                Surface {
                    NavGraph()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                activityPermissionLauncher.launch(
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            }
        }
        if (
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        stepCounterManager.stop()
    }

}