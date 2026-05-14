package com.example.shaalevikas.model

import com.google.firebase.firestore.Exclude

/**
 * Represents a comment or feedback on a school need.
 */
data class Comment(
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false
)

/**
 * Represents a specific requirement or project for a school.
 *
 * @property docId Unique identifier for the need (manually populated).
 * @property title A short title describing the need.
 * @property description A detailed explanation of the requirement.
 * @property estimatedCost The total amount required to fulfill this need.
 * @property currentPledgedAmount The total amount pledged by donors so far.
 * @property imageUrlBefore URL for an image showing the current state before intervention.
 * @property imageUrlAfter URL for an image showing the completed project state.
 * @property status The current status of the need (e.g., "Pending", "In Progress", "Completed").
 * @property priorityScore Calculated score based on urgency and impact.
 * @property categories List of infrastructure categories this need belongs to.
 * @property comments List of comments and feedback associated with this need.
 * @property schoolId ID of the school associated with this need.
 */
data class Need(
    @get:Exclude val docId: String = "",
    val title: String = "",
    val description: String = "",
    val estimatedCost: Double = 0.0,
    val currentPledgedAmount: Double = 0.0,
    val imageUrlBefore: String = "",
    val imageUrlAfter: String = "",
    val status: String = "Pending",
    val priorityScore: Int = 0,
    val categories: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val schoolId: String = "Main School"
)
