package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.model.VpnServer
import com.example.data.repository.MockVpnRepository
import com.example.data.repository.VpnConnectionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VpnViewModel : ViewModel() {

    // Repository delegates
    val servers = MockVpnRepository.servers
    val users = MockVpnRepository.users
    val connectionState = MockVpnRepository.connectionState
    val selectedServer = MockVpnRepository.selectedServer
    val downloadSpeed = MockVpnRepository.downloadSpeed
    val uploadSpeed = MockVpnRepository.uploadSpeed
    val connectionDuration = MockVpnRepository.connectionDuration
    val currentIpAddress = MockVpnRepository.currentIpAddress
    val currentUser = MockVpnRepository.currentUser

    // App general configurations
    val isAutoConnect = MockVpnRepository.isAutoConnect
    val isNotificationEnabled = MockVpnRepository.isNotificationEnabled
    val isDarkTheme = MockVpnRepository.isDarkTheme

    // Local UI states
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    // Server selection filters
    private val _serverSearchQuery = MutableStateFlow("")
    val serverSearchQuery: StateFlow<String> = _serverSearchQuery.asStateFlow()

    private val _serverFilterFavorite = MutableStateFlow(false)
    val serverFilterFavorite: StateFlow<Boolean> = _serverFilterFavorite.asStateFlow()

    // Admin user metrics & search
    private val _adminUserSearchQuery = MutableStateFlow("")
    val adminUserSearchQuery: StateFlow<String> = _adminUserSearchQuery.asStateFlow()

    // Timer Job for connection telemetry
    private var telemetryJob: Job? = null

    init {
        // Observe connection state changes to launch/cancel speed update loops
        viewModelScope.launch {
            connectionState.collect { state ->
                if (state == VpnConnectionState.CONNECTED) {
                    startTelemetryLoop()
                } else {
                    stopTelemetryLoop()
                }
            }
        }
    }

    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                MockVpnRepository.updateLiveStats()
            }
        }
    }

    private fun stopTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = null
    }

    // Authentication Actions
    fun handleLogin(username: String, passwordHash: String, rememberMe: Boolean, onSuccess: () -> Unit) {
        if (username.isBlank() || passwordHash.isBlank()) {
            _loginError.value = "لطفاً نام کاربری و رمز عبور را وارد کنید"
            return
        }

        viewModelScope.launch {
            _isAuthenticating.value = true
            _loginError.value = null
            delay(1200) // Simulating network lag
            
            val user = MockVpnRepository.login(username, passwordHash)
            _isAuthenticating.value = false
            
            if (user != null) {
                if (user.isActive) {
                    onSuccess()
                } else {
                    _loginError.value = "این حساب کاربری غیرفعال یا منقضی شده است"
                    MockVpnRepository.logout()
                }
            } else {
                _loginError.value = "نام کاربری یا رمز عبور اشتباه است"
            }
        }
    }

    fun handleLogout(onLogoutComplete: () -> Unit) {
        MockVpnRepository.logout()
        onLogoutComplete()
    }

    // Connection Actions
    fun toggleConnection() {
        viewModelScope.launch {
            if (connectionState.value == VpnConnectionState.CONNECTED) {
                MockVpnRepository.disconnect()
            } else if (connectionState.value == VpnConnectionState.DISCONNECTED) {
                MockVpnRepository.connect()
            }
        }
    }

    // Server Selection
    fun selectServer(server: VpnServer) {
        MockVpnRepository.selectServer(server)
    }

    fun toggleFavorite(serverId: String) {
        MockVpnRepository.toggleFavorite(serverId)
    }

    fun setServerSearchQuery(query: String) {
        _serverSearchQuery.value = query
    }

    fun toggleServerFavoriteFilter() {
        _serverFilterFavorite.value = !_serverFilterFavorite.value
    }

    // Settings Toggle
    fun toggleAutoConnect() {
        isAutoConnect.value = !isAutoConnect.value
    }

    fun toggleNotifications() {
        isNotificationEnabled.value = !isNotificationEnabled.value
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    // Admin Panel Actions
    fun setAdminUserSearchQuery(query: String) {
        _adminUserSearchQuery.value = query
    }

    fun addNewUser(username: String, email: String, expiry: String): Boolean {
        if (username.isBlank() || email.isBlank() || expiry.isBlank()) return false
        return MockVpnRepository.addUser(username, email, expiry)
    }

    fun deleteUser(userId: String) {
        MockVpnRepository.deleteUser(userId)
    }

    fun toggleUserStatus(userId: String) {
        MockVpnRepository.toggleUserStatus(userId)
    }

    fun addNewServer(country: String, flag: String, city: String, ip: String, ping: Int, load: Int, premium: Boolean): Boolean {
        if (country.isBlank() || city.isBlank() || ip.isBlank()) return false
        return MockVpnRepository.addServer(country, flag, city, ip, ping, load, premium)
    }

    fun deleteServer(serverId: String) {
        MockVpnRepository.deleteServer(serverId)
    }

    // Connection duration formatter (HH:MM:SS) in Persian Numbers
    fun getFormattedDuration(): String {
        val secondsTotal = connectionDuration.value
        val hours = secondsTotal / 3600
        val minutes = (secondsTotal % 3600) / 60
        val seconds = secondsTotal % 60
        val englishStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        
        return with(MockVpnRepository) {
            englishStr.toPersianNumbers()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTelemetryLoop()
    }
}
