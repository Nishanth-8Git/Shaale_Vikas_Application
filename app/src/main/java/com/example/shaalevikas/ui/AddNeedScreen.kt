package com.example.shaalevikas.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shaalevikas.model.Need
import com.example.shaalevikas.viewmodel.AddNeedViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddNeedScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddNeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val contentResolver = context.contentResolver
    val isUploading by viewModel.isUploading.collectAsState()
    val completedNeeds by viewModel.completedNeeds.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var estimatedCost by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // New Priority Fields
    var urgency by remember { mutableFloatStateOf(3f) }
    var impact by remember { mutableFloatStateOf(3f) }
    
    // New Categories
    val availableCategories = listOf(
        "Toilets & Sanitation",
        "Drinking Water",
        "Classroom Repairs",
        "Digital & IT Labs",
        "Desk & Bench Furniture",
        "Electrical & Fans",
        "Library Books",
        "Sports & Playground"
    )
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }

    var targetNeed by remember { mutableStateOf<Need?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    val afterPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            targetNeed?.let { need ->
                viewModel.uploadAfterPhoto(
                    contentResolver = contentResolver,
                    imageUri = it,
                    need = need,
                    onSuccess = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        Toast.makeText(context, "Impact Photo Uploaded!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create New Need", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = !isUploading
        )

        OutlinedTextField(
            value = estimatedCost,
            onValueChange = { estimatedCost = it },
            label = { Text("Estimated Cost") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isUploading
        )

        // Infrastructure Checklist
        Text("Infrastructure Categories", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableCategories.forEach { category ->
                FilterChip(
                    selected = selectedCategories.contains(category),
                    onClick = {
                        selectedCategories = if (selectedCategories.contains(category)) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }
                    },
                    label = { Text(category) },
                    leadingIcon = if (selectedCategories.contains(category)) {
                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null,
                    enabled = !isUploading
                )
            }
        }

        // Priority Scoring
        Text("Priority Scoring", style = MaterialTheme.typography.labelLarge)
        
        Column {
            Text("Urgency: ${urgency.toInt()}/5", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = urgency,
                onValueChange = { urgency = it },
                valueRange = 1f..5f,
                steps = 3,
                enabled = !isUploading
            )
        }

        Column {
            Text("Impact: ${impact.toInt()}/5", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = impact,
                onValueChange = { impact = it },
                valueRange = 1f..5f,
                steps = 3,
                enabled = !isUploading
            )
        }

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            Text(if (selectedImageUri == null) "Upload Photo" else "Photo Selected")
        }

        if (!isUploading) {
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || estimatedCost.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val cost = estimatedCost.toDoubleOrNull() ?: 0.0
                    viewModel.uploadAndSaveNeed(
                        contentResolver = contentResolver,
                        imageUri = selectedImageUri,
                        title = title,
                        description = description,
                        estimatedCost = cost,
                        urgency = urgency.toInt(),
                        impact = impact.toInt(),
                        categories = selectedCategories.toList(),
                        onSuccess = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(context, "Need added successfully!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth())
        }

        if (completedNeeds.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Update Completed Needs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Add 'After' photos to show donors the final results.", style = MaterialTheme.typography.bodySmall)
            
            completedNeeds.forEach { need ->
                var adminNote by remember { mutableStateOf("") }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(need.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { 
                                    targetNeed = need
                                    afterPhotoLauncher.launch("image/*") 
                                },
                                enabled = !isUploading
                            ) {
                                Text("After Photo")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = adminNote,
                            onValueChange = { adminNote = it },
                            label = { Text("Add Admin Note") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (adminNote.isNotBlank()) {
                                        viewModel.addAdminNote(need.docId, adminNote)
                                        adminNote = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "Send Note")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNeedScreenPreview() {
    AddNeedScreen()
}
