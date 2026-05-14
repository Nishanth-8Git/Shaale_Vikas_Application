package com.example.shaalevikas.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shaalevikas.model.Pledge
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Hall of Fame screen.
 * Listens to all pledges and sorts them by amount descending.
 */
class HallOfFameViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _pledges = MutableStateFlow<List<Pledge>>(emptyList())
    val pledges: StateFlow<List<Pledge>> = _pledges.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        listenToPledges()
    }

    private fun listenToPledges() {
        firestore.collection("pledges")
            .orderBy("amount", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _pledges.value = snapshot.toObjects<Pledge>()
                }
                _isLoading.value = false
            }
    }
}
