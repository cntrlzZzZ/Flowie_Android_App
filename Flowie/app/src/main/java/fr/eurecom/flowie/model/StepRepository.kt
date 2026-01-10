package fr.eurecom.flowie.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
 * Singleton repository holding the current day's step count.
 * Uses StateFlow to expose step updates reactively to the UI.
 */
object StepRepository {
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    /*
     * Updates the current step count.
     */
    fun updateSteps(value: Int) {
        _steps.value = value
    }
}