package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AppDrawer
import com.example.ui.navigation.Screen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ServerSelectionScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.VpnViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vpnViewModel: VpnViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        vpnViewModel.onVpnPermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentUser by vpnViewModel.currentUser.collectAsState()
                val sessionReady by vpnViewModel.sessionReady.collectAsState()
                val vpnPermissionIntent by vpnViewModel.vpnPermissionIntent.collectAsState()
                val lastError by vpnViewModel.lastError.collectAsState()

                LaunchedEffect(vpnPermissionIntent) {
                    vpnPermissionIntent?.let { vpnPermissionLauncher.launch(it) }
                }

                LaunchedEffect(lastError) {
                    lastError?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                        vpnViewModel.clearError()
                    }
                }

                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

                LaunchedEffect(sessionReady, currentUser?.id) {
                    if (!sessionReady) return@LaunchedEffect
                    if (currentUser != null && currentUser!!.isActive) {
                        val route = navController.currentDestination?.route
                        if (route == null || route == Screen.Login.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    if (!sessionReady) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryTeal)
                        }
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = currentRoute != Screen.Login.route,
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
                                startDestination = if (
                                    currentUser != null && currentUser!!.isActive
                                ) Screen.Home.route else Screen.Login.route,
                                modifier = Modifier.fillMaxSize()
                            ) {
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
                                composable(Screen.Home.route) {
                                    HomeScreen(
                                        viewModel = vpnViewModel,
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onNavigateToServers = {
                                            navController.navigate(Screen.ServerSelection.route)
                                        }
                                    )
                                }
                                composable(Screen.ServerSelection.route) {
                                    ServerSelectionScreen(
                                        viewModel = vpnViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(
                                        viewModel = vpnViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                composable(Screen.AdminPanel.route) {
                                    AdminPanelScreen(
                                        viewModel = vpnViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
