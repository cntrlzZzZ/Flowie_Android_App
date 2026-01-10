package fr.eurecom.flowie.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/*
 * Manages step counting using the device's step counter sensor.
 * Computes daily steps relative to the first recorded value.
 */
class StepCounterManager(
    context: Context,
    private val onStepUpdate: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounterSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var initialSteps: Int = -1

    /*
     * Starts listening to step counter sensor updates.
     */
    fun start() {
        stepCounterSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /*
     * Stops listening to the sensor to avoid memory leaks.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    /*
     * Called whenever a new sensor value is received.
     * Converts total steps into daily steps.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("STEPS", "Sensor event received")
        val totalSteps = event?.values?.get(0)?.toInt() ?: return

        if (initialSteps == -1) {
            initialSteps = totalSteps
        }

        val todaySteps = totalSteps - initialSteps
        onStepUpdate(todaySteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}