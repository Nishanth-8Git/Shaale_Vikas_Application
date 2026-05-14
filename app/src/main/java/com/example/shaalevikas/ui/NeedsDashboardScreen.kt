package com.example.shaalevikas.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.shaalevikas.model.Need
import com.example.shaalevikas.viewmodel.NeedsDashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedsDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: NeedsDashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val needs by viewModel.needs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showPledgeDialog by remember { mutableStateOf(false) }
    var selectedNeed by remember { mutableStateOf<Need?>(null) }
    var pledgeAmount by remember { mutableStateOf("") }
    var donorName by remember { mutableStateOf("") }
    
    var showImpactDialog by remember { mutableStateOf(false) }
    var impactNeed by remember { mutableStateOf<Need?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (needs.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No active needs found.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Check back later or contact the administrator.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(needs) { need ->
                    NeedCard(
                        need = need,
                        onPledgeClick = {
                            selectedNeed = need
                            showPledgeDialog = true
                        },
                        onViewImpactClick = {
                            impactNeed = need
                            showImpactDialog = true
                        },
                        onSendComment = { message, name ->
                            viewModel.addComment(need.docId, name, message)
                        }
                    )
                }
            }
        }
    }

    if (showImpactDialog && impactNeed != null) {
        AlertDialog(
            onDismissRequest = { showImpactDialog = false },
            title = { Text("Impact: ${impactNeed?.title}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Before & After", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Before", style = MaterialTheme.typography.labelSmall)
                            AsyncImage(
                                model = impactNeed?.imageUrlBefore,
                                contentDescription = "Before",
                                modifier = Modifier.size(140.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("After", style = MaterialTheme.typography.labelSmall)
                            AsyncImage(
                                model = impactNeed?.imageUrlAfter,
                                contentDescription = "After",
                                modifier = Modifier.size(140.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Status: ${impactNeed?.status}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showImpactDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showPledgeDialog && selectedNeed != null) {
        AlertDialog(
            onDismissRequest = {
                showPledgeDialog = false
                pledgeAmount = ""
                donorName = ""
            },
            title = { Text("Pledge for ${selectedNeed?.title}") },
            text = {
                Column {
                    val remaining = (selectedNeed?.estimatedCost ?: 0.0) - (selectedNeed?.currentPledgedAmount ?: 0.0)
                    Text("Remaining to goal: ${formatCurrency(remaining)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pledgeAmount,
                        onValueChange = { pledgeAmount = it },
                        label = { Text("Pledge Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = pledgeAmount.toDoubleOrNull() ?: 0.0
                    val remaining = (selectedNeed?.estimatedCost ?: 0.0) - (selectedNeed?.currentPledgedAmount ?: 0.0)

                    if (donorName.isBlank()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                    } else if (amount <= 0) {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    } else if (amount > remaining) {
                        Toast.makeText(context, "Amount exceeds remaining balance", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedNeed?.let { need ->
                            viewModel.pledgeToNeed(
                                need = need,
                                donorName = donorName,
                                pledgeAmount = amount,
                                onSuccess = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, "Thank You for your pledge!", Toast.LENGTH_LONG).show()
                                    showPledgeDialog = false
                                    pledgeAmount = ""
                                    donorName = ""
                                },
                                onError = { error ->
                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPledgeDialog = false
                    pledgeAmount = ""
                    donorName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeedCard(
    need: Need,
    onPledgeClick: () -> Unit,
    onViewImpactClick: () -> Unit,
    onSendComment: (String, String) -> Unit
) {
    val isFulfilled = need.currentPledgedAmount >= need.estimatedCost
    val hasAfterPhoto = need.imageUrlAfter.isNotBlank()
    val isHighPriority = need.priorityScore > 60
    
    var showComments by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var commentName by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (isFulfilled) BorderStroke(2.dp, Color(0xFF4CAF50)) else null
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (need.imageUrlBefore.isNotBlank()) {
                AsyncImage(
                    model = need.imageUrlBefore,
                    contentDescription = "Need Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = need.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isHighPriority) {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("HIGH PRIORITY") },
                            icon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = MaterialTheme.colorScheme.error,
                                iconContentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        )
                    }
                }
                
                if (isFulfilled) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(if (hasAfterPhoto) "COMPLETED" else "FULFILLED") },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = Color(0xFF4CAF50),
                            iconContentColor = Color(0xFF4CAF50)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                    )
                }
            }
            
            // Categories Tags
            if (need.categories.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    need.categories.forEach { category ->
                        AssistChip(
                            onClick = { },
                            label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = need.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val progress = if (need.estimatedCost > 0) {
                (need.currentPledgedAmount / need.estimatedCost).toFloat()
            } else 0f
            
            Text(
                text = "Progress: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall
            )
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (isFulfilled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal: ${formatCurrency(need.estimatedCost)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                if (hasAfterPhoto) {
                    Button(
                        onClick = onViewImpactClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("View Impact")
                    }
                } else {
                    Button(
                        onClick = onPledgeClick,
                        enabled = !isFulfilled
                    ) {
                        Text(if (isFulfilled) "Funded" else "Pledge")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Comments Section Toggle
            TextButton(
                onClick = { showComments = !showComments },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Comments (${need.comments.size})")
                    Icon(
                        imageVector = if (showComments) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (showComments) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    need.comments.forEach { comment ->
                        CommentBubble(comment = comment)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = commentName,
                        onValueChange = { commentName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("Add feedback or question...") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newComment.isNotBlank() && commentName.isNotBlank()) {
                                    onSendComment(newComment, commentName)
                                    newComment = ""
                                }
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentBubble(comment: com.example.shaalevikas.model.Comment) {
    val bubbleColor = if (comment.isAdmin) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (comment.isAdmin) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = comment.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (comment.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = comment.message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NeedsDashboardScreenPreview() {
    val sampleNeeds = listOf(
        Need(
            title = "New Benches",
            description = "We need 20 new benches for Class 5 students.",
            estimatedCost = 50000.0,
            currentPledgedAmount = 15000.0,
            priorityScore = 80,
            categories = listOf("Furniture", "Classroom Repair")
        ),
        Need(
            title = "Library Books",
            description = "Purchase of science and literature books for the school library.",
            estimatedCost = 20000.0,
            currentPledgedAmount = 20000.0,
            status = "Completed",
            priorityScore = 40,
            categories = listOf("Classroom Repair")
        )
    )
    
    // We can't easily preview the screen with the ViewModel because of Firestore init,
    // so we'll preview a mocked version or just the list content.
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("School Needs Dashboard") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sampleNeeds) { need ->
                NeedCard(
                    need = need, 
                    onPledgeClick = {}, 
                    onViewImpactClick = {},
                    onSendComment = { _, _ -> }
                )
            }
        }
    }
}
