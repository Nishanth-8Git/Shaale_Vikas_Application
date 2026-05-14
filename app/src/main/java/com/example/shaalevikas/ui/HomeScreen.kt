package com.example.shaalevikas.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shaalevikas.viewmodel.HomeViewModel
import java.util.Locale

private const val ADMIN_CODE = "1234"

@Composable
fun HomeScreen(
    onViewNeedsClick: () -> Unit,
    onHallOfFameClick: () -> Unit,
    onAdminPortalClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val summary by viewModel.summary.collectAsState()
    
    var showAuthDialog by remember { mutableStateOf(false) }
    var enteredCode by remember { mutableStateOf("") }
    var isAdminAuthenticated by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Shaale Vikas",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Impact at a Glance Section
        ImpactSummarySection(
            funds = summary.totalFundsRaised,
            projects = summary.projectsCompleted,
            schools = summary.activeSchools
        )

        Spacer(modifier = Modifier.height(32.dp))

        MenuCard(
            title = "View School Needs",
            description = "Explore active projects and contribute to school development.",
            icon = Icons.AutoMirrored.Filled.List,
            onClick = onViewNeedsClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuCard(
            title = "Donor Hall of Fame",
            description = "Celebrating the generous alumni who empower our schools.",
            icon = Icons.Default.Star,
            onClick = onHallOfFameClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        MenuCard(
            title = "Admin Portal",
            description = "Add new needs and manage school requirements (Headmaster only).",
            icon = Icons.Default.Add,
            onClick = {
                if (isAdminAuthenticated) {
                    onAdminPortalClick()
                } else {
                    showAuthDialog = true
                }
            }
        )
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAuthDialog = false
                enteredCode = ""
            },
            title = { Text("Headmaster Access") },
            text = {
                Column {
                    Text("Please enter the admin access code to enter the portal.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { enteredCode = it },
                        label = { Text("Access Code") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredCode == ADMIN_CODE) {
                            isAdminAuthenticated = true
                            showAuthDialog = false
                            enteredCode = ""
                            onAdminPortalClick()
                        } else {
                            Toast.makeText(context, "Wrong Code. Access Denied.", Toast.LENGTH_SHORT).show()
                            enteredCode = ""
                        }
                    }
                ) {
                    Text("Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAuthDialog = false
                    enteredCode = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ImpactSummarySection(funds: Double, projects: Int, schools: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Impact at a Glance",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ImpactBox(label = "Raised", value = formatShorthand(funds, isCurrency = true))
            ImpactBox(label = "Projects Done", value = projects.toString())
            ImpactBox(label = "Active Schools", value = schools.toString())
        }
    }
}

@Composable
fun ImpactBox(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.width(105.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatShorthand(value: Double, isCurrency: Boolean): String {
    val shorthand = when {
        value >= 1000000 -> String.format(Locale.US, "%.1fM", value / 1000000)
        value >= 1000 -> String.format(Locale.US, "%.1fk", value / 1000)
        else -> String.format(Locale.US, "%.0f", value)
    }
    return if (isCurrency) "₹$shorthand" else shorthand
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(onViewNeedsClick = {}, onHallOfFameClick = {}, onAdminPortalClick = {})
}

@Composable
fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
