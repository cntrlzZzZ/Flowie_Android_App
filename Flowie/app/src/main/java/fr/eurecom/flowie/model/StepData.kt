package fr.eurecom.flowie.model

/*
 * Data model representing the number of steps taken on a specific day.
 * - date: formatted as yyyy-MM-dd
 * - steps: total number of steps for that day
 */

data class StepData(
    val date: String,
    val steps: Int
)