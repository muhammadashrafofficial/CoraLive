package com.example.coralive.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.coralive.features.match_details.presentation.MatchDetailsScreen
import com.example.coralive.features.matches.presentation.MatchesScreen
import com.example.coralive.features.profiles.presentation.PlayerProfileScreen
import com.example.coralive.features.profiles.presentation.TeamProfileScreen
import com.example.coralive.features.standings.presentation.StandingsScreen
import com.example.coralive.ui.theme.CoraTheme

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute == "matches" || currentRoute == "standings"

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = CoraTheme.colors.homeAccent,
        selectedTextColor = CoraTheme.colors.homeAccent,
        indicatorColor = CoraTheme.colors.surfaceElevated,
        unselectedIconColor = CoraTheme.colors.textSecondary,
        unselectedTextColor = CoraTheme.colors.textSecondary
    )

    Scaffold(
        containerColor = CoraTheme.colors.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(thickness = 1.dp, color = CoraTheme.colors.border)
                    NavigationBar(
                        containerColor = CoraTheme.colors.background,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == "matches",
                            onClick = {
                                navController.navigate("matches") {
                                    popUpTo("matches") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = navItemColors,
                            icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null) },
                            label = {
                                Text(
                                    text = "المباريات",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (currentRoute == "matches") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "standings",
                            onClick = {
                                navController.navigate("standings") {
                                    popUpTo("matches") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = navItemColors,
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                            label = {
                                Text(
                                    text = "الترتيب",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (currentRoute == "standings") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "matches",
            modifier = Modifier.padding(padding)
        ) {
            composable("matches") {
                MatchesScreen(
                    onMatchClick = { matchId, leagueCode ->
                        navController.navigate("details/$leagueCode/$matchId")
                    }
                )
            }

            composable("standings") {
                StandingsScreen(
                    onTeamClick = { teamId, leagueCode ->
                        navController.navigate("team_profile/$leagueCode/$teamId")
                    }
                )
            }

            composable(
                route = "details/{leagueCode}/{matchId}",
                arguments = listOf(
                    navArgument("leagueCode") { type = NavType.StringType },
                    navArgument("matchId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val leagueCode = backStackEntry.arguments?.getString("leagueCode") ?: "eng.1"
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                MatchDetailsScreen(
                    leagueCode = leagueCode,
                    matchId = matchId,
                    onBack = { navController.popBackStack() },
                    onTeamClick = { teamId ->
                        navController.navigate("team_profile/$leagueCode/$teamId")
                    }
                )
            }

            composable(
                route = "team_profile/{leagueCode}/{teamId}",
                arguments = listOf(
                    navArgument("leagueCode") { type = NavType.StringType },
                    navArgument("teamId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val leagueCode = backStackEntry.arguments?.getString("leagueCode") ?: "eng.1"
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                TeamProfileScreen(
                    leagueCode = leagueCode,
                    teamId = teamId,
                    onBack = { navController.popBackStack() },
                    onMatchClick = { mId, lCode ->
                        navController.navigate("details/$lCode/$mId")
                    },
                    onPlayerClick = { pId ->
                        navController.navigate("player_profile/$pId")
                    }
                )
            }

            composable(
                route = "player_profile/{playerId}",
                arguments = listOf(
                    navArgument("playerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
                PlayerProfileScreen(
                    playerId = playerId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}