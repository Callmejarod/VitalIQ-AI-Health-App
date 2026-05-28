package com.vitaliq.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vitaliq.app.ui.screens.dashboard.DashboardScreen
import com.vitaliq.app.ui.screens.history.HistoryScreen
import com.vitaliq.app.ui.screens.insights.InsightsScreen
import com.vitaliq.app.ui.screens.log.LogScreen
import com.vitaliq.app.ui.screens.profile.ProfileScreen
import com.vitaliq.app.ui.screens.workout.WorkoutScreen
import com.vitaliq.app.ui.theme.VitalColors

sealed class BottomNavDest(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavDest("home", "Home", Icons.Default.Home)
    object Workout : BottomNavDest("workout", "Workout", Icons.Default.FitnessCenter)
    object Log : BottomNavDest("log", "Log", Icons.Default.AddCircle)
    object Insights : BottomNavDest("insights", "Insights", Icons.Default.AutoAwesome)
    object Profile : BottomNavDest("profile", "Profile", Icons.Default.AccountCircle)
}

private val bottomNavDestinations = listOf(
    BottomNavDest.Home,
    BottomNavDest.Workout,
    BottomNavDest.Log,
    BottomNavDest.Insights,
    BottomNavDest.Profile
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route != "history"

    Scaffold(
        containerColor = VitalColors.bg,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = VitalColors.card,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                ) {
                    bottomNavDestinations.forEach { dest ->
                        val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(dest.icon, contentDescription = dest.label)
                            },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VitalColors.brand,
                                selectedTextColor = VitalColors.brand,
                                unselectedIconColor = VitalColors.textMuted,
                                unselectedTextColor = VitalColors.textMuted,
                                indicatorColor = VitalColors.accentSoft
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavDest.Home.route
            ) {
                composable(BottomNavDest.Home.route) {
                    DashboardScreen(
                        onNavigateToWorkout = {
                            navController.navigate(BottomNavDest.Workout.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLog = {
                            navController.navigate(BottomNavDest.Log.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToInsights = {
                            navController.navigate(BottomNavDest.Insights.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToProfile = {
                            navController.navigate(BottomNavDest.Profile.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToHistory = {
                            navController.navigate("history")
                        }
                    )
                }

                composable(BottomNavDest.Workout.route) {
                    WorkoutScreen()
                }

                composable(BottomNavDest.Log.route) {
                    LogScreen()
                }

                composable(BottomNavDest.Insights.route) {
                    InsightsScreen()
                }

                composable(BottomNavDest.Profile.route) {
                    ProfileScreen()
                }

                composable("history") {
                    HistoryScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
