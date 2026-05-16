package com.example.shaalevikas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shaalevikas.model.Comment
import com.example.shaalevikas.model.Need
import com.example.shaalevikas.model.Pledge
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Alumni Dashboard (NeedsDashboardScreen).
 * Manages the real-time Firestore listener for school needs.
 */
class NeedsDashboardViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _needs = MutableStateFlow<List<Need>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Combined stream for filtered needs
    val filteredNeeds: StateFlow<List<Need>> = combine(
        _needs,
        _searchQuery,
        _selectedCategory
    ) { rawNeeds, query, category ->
        rawNeeds.filter { need ->
            val matchesQuery = query.isBlank() || 
                need.title.contains(query, ignoreCase = true) || 
                need.description.contains(query, ignoreCase = true) ||
                need.schoolId.contains(query, ignoreCase = true)
            
            val matchesCategory = category == null || need.categories.contains(category)
            
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList<Need>()
    )

    init {
        listenToNeeds()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        // Toggle if clicking already selected
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    private fun listenToNeeds() {
        firestore.collection("needs")
            .orderBy("priorityScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error (e.g., log it or update a state for the UI to show)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _needs.value = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Need::class.java)?.copy(docId = doc.id)
                    }
                }
                _isLoading.value = false
            }
    }

    /**
     * Updates a need with a new pledge amount using a Firestore Transaction.
     * Also saves a new Pledge document in the pledges collection.
     */
    fun pledgeToNeed(
        need: Need,
        donorName: String,
        pledgeAmount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val needId = need.docId
        if (needId.isBlank()) {
            onError("Internal Error: Missing Document ID. Please refresh the list.")
            return
        }
        val needRef = firestore.collection("needs").document(needId)
        val pledgeRef = firestore.collection("pledges").document()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(needRef)
            val currentPledge = snapshot.getDouble("currentPledgedAmount") ?: 0.0
            val estimatedCost = snapshot.getDouble("estimatedCost") ?: 0.0

            if (currentPledge + pledgeAmount > estimatedCost) {
                throw Exception("Pledge amount exceeds the remaining balance.")
            }

            // Update Need document
            transaction.update(needRef, "currentPledgedAmount", currentPledge + pledgeAmount)

            // Create and Save Pledge document
            val newPledge = Pledge(
                needId = needId,
                needTitle = need.title,
                donorName = donorName,
                amount = pledgeAmount
            )
            transaction.set(pledgeRef, newPledge)

        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onError(e.message ?: "Transaction failed")
        }
    }

    /**
     * Adds a comment to a specific need.
     */
    fun addComment(
        needId: String,
        senderName: String,
        message: String,
        isAdmin: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (needId.isBlank()) return
        
        val comment = Comment(
            senderName = senderName,
            message = message,
            timestamp = System.currentTimeMillis(),
            isAdmin = isAdmin
        )

        firestore.collection("needs").document(needId)
            .update("comments", FieldValue.arrayUnion(comment))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to add comment") }
    }

    /**
     * Fetches all donors for a specific need.
     */
    fun getDonorsForNeed(needId: String, onResult: (List<Pledge>) -> Unit) {
        firestore.collection("pledges")
            .whereEqualTo("needId", needId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObjects(Pledge::class.java))
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
