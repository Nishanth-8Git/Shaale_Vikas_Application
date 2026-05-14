package com.example.shaalevikas.model

/**
 * Represents a financial commitment made by a donor towards a school's need.
 *
 * @property id Unique identifier for the pledge.
 * @property needId The ID of the [Need] this pledge is associated with.
 * @property needTitle The title of the [Need] this pledge is for.
 * @property donorName The name of the person or organization making the pledge.
 * @property amount The financial amount committed.
 */
data class Pledge(
    val id: String = "",
    val needId: String = "",
    val needTitle: String = "",
    val donorName: String = "",
    val amount: Double = 0.0
)
