package fr.eurecom.flowie.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object StepRepository {

    // Nombre de pas du jour
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    fun updateSteps(value: Int) {
        _steps.value = value
    }
}