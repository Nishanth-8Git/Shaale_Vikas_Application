package com.example.shaalevikas.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shaalevikas.model.Comment
import com.example.shaalevikas.model.Need
import com.example.shaalevikas.network.SupabaseClient
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the Add Need screen.
 * Handles image processing, Supabase storage upload, and Firestore saving.
 */
class AddNeedViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = SupabaseClient.client.storage

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _completedNeeds = MutableStateFlow<List<Need>>(emptyList())
    val completedNeeds: StateFlow<List<Need>> = _completedNeeds.asStateFlow()

    init {
        fetchCompletedNeeds()
    }

    private fun fetchCompletedNeeds() {
        firestore.collection("needs")
            .whereIn("status", listOf("Pending", "In Progress")) // Status is usually changed when funded
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val all = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Need::class.java)?.copy(docId = doc.id)
                    }
                    // Filter for needs where pledge >= cost but don't have after photo yet
                    _completedNeeds.value = all.filter { 
                        it.currentPledgedAmount >= it.estimatedCost && it.imageUrlAfter.isBlank() 
                    }
                }
            }
    }

    /**
     * Uploads an "After" photo for an existing need and updates Firestore.
     */
    fun uploadAfterPhoto(
        contentResolver: ContentResolver,
        imageUri: Uri,
        need: Need,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                // 1. Convert and Upload to Supabase
                val byteArray = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                if (byteArray == null) {
                    onError("Failed to read image data")
                    _isUploading.value = false
                    return@launch
                }

                val fileName = "public/after_${UUID.randomUUID()}.jpg"
                val bucket = storage.from("need-images")
                bucket.upload(fileName, byteArray) { upsert = true }
                val afterImageUrl = bucket.publicUrl(fileName)

                // 2. Update Firestore document
                firestore.collection("needs").document(need.docId)
                    .update(
                        mapOf(
                            "imageUrlAfter" to afterImageUrl,
                            "status" to "Impact Delivered!"
                        )
                    )
                    .addOnSuccessListener {
                        onSuccess()
                        _isUploading.value = false
                    }
                    .addOnFailureListener { e ->
                        onError("Firestore Update Error: ${e.message}")
                        _isUploading.value = false
                    }

            } catch (e: Exception) {
                onError("Upload Error: ${e.message}")
                _isUploading.value = false
            }
        }
    }

    /**
     * Uploads the selected image to Supabase and then saves the need data to Firestore.
     */
    fun uploadAndSaveNeed(
        contentResolver: ContentResolver,
        imageUri: Uri?,
        title: String,
        description: String,
        estimatedCost: Double,
        urgency: Int,
        impact: Int,
        categories: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                var uploadedImageUrl = ""

                if (imageUri != null) {
                    // 1. Convert Uri to ByteArray
                    val byteArray = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    if (byteArray == null) {
                        onError("Failed to read image data")
                        _isUploading.value = false
                        return@launch
                    }

                    // 2. Upload to Supabase Storage
                    val fileName = "public/${UUID.randomUUID()}.jpg"
                    val bucket = storage.from("need-images")
                    bucket.upload(fileName, byteArray) {
                        upsert = true
                    }

                    // 3. Get Public URL
                    uploadedImageUrl = bucket.publicUrl(fileName)
                }

                // 4. Calculate Priority Score
                val priorityScore = (urgency * 10) + (impact * 5)

                // 5. Save to Firestore
                val newNeed = Need(
                    title = title,
                    description = description,
                    estimatedCost = estimatedCost,
                    imageUrlBefore = uploadedImageUrl,
                    status = "Pending",
                    priorityScore = priorityScore,
                    categories = categories
                )

                firestore.collection("needs")
                    .add(newNeed)
                    .addOnSuccessListener {
                        onSuccess()
                        _isUploading.value = false
                    }
                    .addOnFailureListener { e ->
                        onError("Firestore Error: ${e.message}")
                        _isUploading.value = false
                    }

            } catch (e: Exception) {
                onError("Upload Error: ${e.message}")
                _isUploading.value = false
            }
        }
    }

    /**
     * Adds an admin note to a need.
     */
    fun addAdminNote(
        needId: String,
        note: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (needId.isBlank()) return
        
        val comment = Comment(
            senderName = "Headmaster",
            message = note,
            timestamp = System.currentTimeMillis(),
            isAdmin = true
        )

        firestore.collection("needs").document(needId)
            .update("comments", FieldValue.arrayUnion(comment))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to add note") }
    }
}
