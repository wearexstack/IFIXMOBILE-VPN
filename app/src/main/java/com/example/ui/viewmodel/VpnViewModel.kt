package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionStore
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

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionStore = SessionStore(application)

    val servers = MockVpnRepository.servers
    val users = MockVpnRepository.users
    val connectionState = MockVpnRepository.connectionState
    val selectedServer = MockVpnRepository.selectedServer
    val downloadSpeed = MockVpnRepository.downloadSpeed
    val uploadSpeed = MockVpnRepository.uploadSpeed
    val connectionDuration = MockVpnRepository.connectionDuration
    val currentIpAddress = MockVpnRepository.currentIpAddress
    val currentUser = MockVpnRepository.currentUser

    val isAutoConnect = MockVpnRepository.isAutoConnect
    val isNotificationEnabled = MockVpnRepository.isNotificationEnabled
    val isDarkTheme = MockVpnRepository.isDarkTheme

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    /** True after trying to restore SharedPreferences session. */
    private val _sessionReady = MutableStateFlow(false)
    val sessionReady: StateFlow<Boolean> = _sessionReady.asStateFlow()

    private val _serverSearchQuery = MutableStateFlow("")
    val serverSearchQuery: StateFlow<String> = _serverSearchQuery.asStateFlow()

    private val _serverFilterFavorite = MutableStateFlow(false)
    val serverFilterFavorite: StateFlow<Boolean> = _serverFilterFavorite.asStateFlow()

    private val _adminUserSearchQuery = MutableStateFlow("")
    val adminUserSearchQuery: StateFlow<String> = _adminUserSearchQuery.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        viewModelScope.launch {
            connectionState.collect { state ->
                if (state == VpnConnectionState.CONNECTED) {
                    startTelemetryLoop()
                } else {
                    stopTelemetryLoop()
                }
            }
        }
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                if (sessionStore.isRemembered()) {
                    val u = sessionStore.username()
                    val p = sessionStore.password()
                    if (!u.isNullOrBlank() && !p.isNullOrBlank()) {
                        val user = MockVpnRepository.login(u, p)
                        if (user == null || !user.isActive) {
                            sessionStore.clear()
                            MockVpnRepository.logout()
                        }
                    }
                }
            } finally {
                _sessionReady.value = true
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

    fun handleLogin(
        username: String,
        passwordHash: String,
        rememberMe: Boolean,
        onSuccess: () -> Unit
    ) {
        if (username.isBlank() || passwordHash.isBlank()) {
            _loginError.value = "لطفاً نام کاربری و رمز عبور را وارد کنید"
            return
        }

        viewModelScope.launch {
            _isAuthenticating.value = true
            _loginError.value = null
            delay(800)

            val user = MockVpnRepository.login(username.trim(), passwordHash)
            _isAuthenticating.value = false

            if (user != null) {
                if (user.isActive) {
                    if (rememberMe) {
                        sessionStore.saveSession(username.trim(), passwordHash)
                    } else {
                        sessionStore.clear()
                    }
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
        sessionStore.clear()
        MockVpnRepository.logout()
        onLogoutComplete()
    }

    fun toggleConnection() {
        viewModelScope.launch {
            if (connectionState.value == VpnConnectionState.CONNECTED) {
                MockVpnRepository.disconnect()
            } else if (connectionState.value == VpnConnectionState.DISCONNECTED) {
                MockVpnRepository.connect()
            }
        }
    }

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

    fun toggleAutoConnect() {
        isAutoConnect.value = !isAutoConnect.value
    }

    fun toggleNotifications() {
        isNotificationEnabled.value = !isNotificationEnabled.value
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

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

    fun addNewServer(
        country: String,
        flag: String,
        city: String,
        ip: String,
        ping: Int,
        load: Int,
        premium: Boolean
    ): Boolean {
        if (country.isBlank() || city.isBlank() || ip.isBlank()) return false
        return MockVpnRepository.addServer(country, flag, city, ip, ping, load, premium)
    }

    fun deleteServer(serverId: String) {
        MockVpnRepository.deleteServer(serverId)
    }

    fun getFormattedDuration(): String {
        val secondsTotal = connectionDuration.value
        val hours = secondsTotal / 3600
        val minutes = (secondsTotal % 3600) / 60
        val seconds = secondsTotal % 60
        val englishStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        return with(MockVpnRepository) { englishStr.toPersianNumbers() }
    }

    override fun onCleared() {
        super.onCleared()
        stopTelemetryLoop()
    }
}
