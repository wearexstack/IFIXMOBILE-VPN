package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AppDrawer
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VpnViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Instantiating central ViewModel
                val vpnViewModel: VpnViewModel = viewModel()
                val currentUser by vpnViewModel.currentUser.collectAsState()

                // Nav controller and drawer state
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Current backstack route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

                // Enforce Persian RTL direction globally
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentRoute != Screen.Login.route, // disable swiping on login
                        drawerContent = {
                            AppDrawer(
                                currentUser = currentUser,
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onLogout = {
                                    vpnViewModel.handleLogout {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onCloseDrawer = {
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Login.route,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 1. Login Screen
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    viewModel = vpnViewModel,
                                    onLoginSuccess = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // 2. Home Dashboard Screen
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    viewModel = vpnViewModel,
                                    onOpenDrawer = {
                                        scope.launch { drawerState.open() }
                                    },
                                    onNavigateToServers = {
                                        navController.navigate(Screen.ServerSelection.route)
                                    }
                                )
                            }

                            // 3. Server Selection Screen
                            composable(Screen.ServerSelection.route) {
                                ServerSelectionScreen(
                                    viewModel = vpnViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // 4. Settings Screen
                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    viewModel = vpnViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // 5. Admin Panel Screen
                            composable(Screen.AdminPanel.route) {
                                AdminPanelScreen(
                                    viewModel = vpnViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
