package com.example.shaalevikas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shaalevikas.ui.AddNeedScreen
import com.example.shaalevikas.ui.HallOfFameScreen
import com.example.shaalevikas.ui.HomeScreen
import com.example.shaalevikas.ui.NeedsDashboardScreen
import com.example.shaalevikas.ui.navigation.Screen
import com.example.shaalevikas.ui.theme.ShaaleVikasTheme
import com.example.shaalevikas.viewmodel.HomeViewModel
import com.example.shaalevikas.viewmodel.NeedsDashboardViewModel

class MainActivity : ComponentActivity() {
    
    // Initialize the ViewModels using the viewModels() delegate
    private val dashboardViewModel: NeedsDashboardViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShaaleVikasTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Only show TopAppBar if not on Home screen
                        if (currentRoute != Screen.Home.route) {
                            TopAppBar(
                                title = {
                                    Text(
                                        when (currentRoute) {
                                            Screen.Dashboard.route -> "School Needs"
                                            Screen.AddNeed.route -> "Add New Need"
                                            Screen.HallOfFame.route -> "Hall of Fame"
                                            else -> "Shaale Vikas"
                                        }
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onViewNeedsClick = { navController.navigate(Screen.Dashboard.route) },
                                onHallOfFameClick = { navController.navigate(Screen.HallOfFame.route) },
                                onAdminPortalClick = { navController.navigate(Screen.AddNeed.route) },
                                viewModel = homeViewModel
                            )
                        }
                        composable(Screen.Dashboard.route) {
                            NeedsDashboardScreen(
                                viewModel = dashboardViewModel
                            )
                        }
                        composable(Screen.AddNeed.route) {
                            AddNeedScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.HallOfFame.route) {
                            HallOfFameScreen()
                        }
                    }
                }
            }
        }
    }
}
