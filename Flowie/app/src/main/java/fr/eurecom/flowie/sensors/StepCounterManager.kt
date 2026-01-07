package fr.eurecom.flowie.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class StepCounterManager(
    context: Context,
    private val onStepUpdate: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounterSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var initialSteps: Int = -1

    fun start() {
        stepCounterSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

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