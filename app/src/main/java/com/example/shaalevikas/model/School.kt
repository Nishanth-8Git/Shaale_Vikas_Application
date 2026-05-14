package com.example.shaalevikas.model

/**
 * Represents a school entity in the Shaale Vikas application.
 *
 * @property id Unique identifier for the school (typically the Firestore document ID).
 * @property name The name of the school.
 * @property location The geographical location or address of the school.
 */
data class School(
    val id: String = "",
    val name: String = "",
    val location: String = ""
)
