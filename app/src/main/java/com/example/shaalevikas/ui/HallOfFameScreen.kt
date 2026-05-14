package com.example.shaalevikas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shaalevikas.model.Pledge
import com.example.shaalevikas.viewmodel.HallOfFameViewModel
import java.util.*

// Custom colors for the Hall of Fame
private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD7F32)

@Composable
fun HallOfFameScreen(
    viewModel: HallOfFameViewModel = viewModel()
) {
    val pledges by viewModel.pledges.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (pledges.isEmpty()) {
            Text(
                text = "No pledges yet. Be the first to contribute!",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(pledges) { index, pledge ->
                    DonorCard(rank = index + 1, pledge = pledge)
                }
            }
        }
    }
}

@Composable
fun DonorCard(rank: Int, pledge: Pledge) {
    val medalColor = when (rank) {
        1 -> GoldColor
        2 -> SilverColor
        3 -> BronzeColor
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rank <= 3) medalColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rank <= 3) 6.dp else 2.dp),
        border = if (rank <= 3) androidx.compose.foundation.BorderStroke(1.dp, medalColor) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Icon / Number
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Medal",
                        tint = medalColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pledge.donorName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pledged for: ${pledge.needTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatCurrency(pledge.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun HallOfFameScreenPreview() {
    val samplePledges = listOf(
        Pledge(donorName = "John Doe", amount = 5000.0, needTitle = "New Benches"),
        Pledge(donorName = "Jane Smith", amount = 3000.0, needTitle = "Library Books"),
        Pledge(donorName = "Alice Brown", amount = 1500.0, needTitle = "Classroom Paint"),
        Pledge(donorName = "Bob Wilson", amount = 500.0, needTitle = "Sports Equipment")
    )

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Donor Hall of Fame") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(samplePledges) { index, pledge ->
                DonorCard(rank = index + 1, pledge = pledge)
            }
        }
    }
}
