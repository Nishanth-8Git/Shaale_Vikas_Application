package com.example.shaalevikas.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shaalevikas.model.Need
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class representing the analytics summary for the home screen.
 */
data class AnalyticsSummary(
    val totalFundsRaised: Double = 0.0,
    val projectsCompleted: Int = 0,
    val activeSchools: Int = 0
)

/**
 * ViewModel for the Home screen.
 * Calculates platform-wide impact metrics in real-time.
 */
class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _summary = MutableStateFlow(AnalyticsSummary())
    val summary: StateFlow<AnalyticsSummary> = _summary.asStateFlow()

    init {
        listenToImpactMetrics()
    }

    private fun listenToImpactMetrics() {
        firestore.collection("needs")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val needs = snapshot.documents.mapNotNull { it.toObject(Need::class.java) }
                
                val totalFunds = needs.sumOf { it.currentPledgedAmount }
                val completedCount = needs.count { it.status == "Impact Delivered!" }
                val uniqueSchools = needs.map { it.schoolId }.distinct().count { it.isNotBlank() }

                _summary.value = AnalyticsSummary(
                    totalFundsRaised = totalFunds,
                    projectsCompleted = completedCount,
                    activeSchools = uniqueSchools
                )
            }
    }
}
