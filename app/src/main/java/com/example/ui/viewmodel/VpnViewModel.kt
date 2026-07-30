package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionStore
import com.example.data.SubscriptionLoader
import com.example.data.model.VpnServer
import com.example.data.repository.MockVpnRepository
import com.example.data.repository.VpnConnectionState
import com.example.vpn.IfixVpnService
import com.example.vpn.XrayEngine
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
    val lastError = MockVpnRepository.lastError

    val isAutoConnect = MockVpnRepository.isAutoConnect
    val isNotificationEnabled = MockVpnRepository.isNotificationEnabled
    val isDarkTheme = MockVpnRepository.isDarkTheme

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _sessionReady = MutableStateFlow(false)
    val sessionReady: StateFlow<Boolean> = _sessionReady.asStateFlow()

    private val _serverSearchQuery = MutableStateFlow("")
    val serverSearchQuery: StateFlow<String> = _serverSearchQuery.asStateFlow()

    private val _serverFilterFavorite = MutableStateFlow(false)
    val serverFilterFavorite: StateFlow<Boolean> = _serverFilterFavorite.asStateFlow()

    private val _adminUserSearchQuery = MutableStateFlow("")
    val adminUserSearchQuery: StateFlow<String> = _adminUserSearchQuery.asStateFlow()

    private val _vpnPermissionIntent = MutableStateFlow<Intent?>(null)
    val vpnPermissionIntent: StateFlow<Intent?> = _vpnPermissionIntent.asStateFlow()

    private val _isRefreshingServers = MutableStateFlow(false)
    val isRefreshingServers: StateFlow<Boolean> = _isRefreshingServers.asStateFlow()

    private val _coreAvailable = MutableStateFlow(XrayEngine.isAvailable())
    val coreAvailable: StateFlow<Boolean> = _coreAvailable.asStateFlow()

    private var telemetryJob: Job? = null

    private val vpnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != IfixVpnService.BROADCAST_STATE) return
            when (intent.getStringExtra(IfixVpnService.EXTRA_STATE)) {
                "connecting" -> MockVpnRepository.setConnectionState(VpnConnectionState.CONNECTING)
                "connected" -> {
                    val ip = selectedServer.value.ipAddress
                    MockVpnRepository.onConnected(ip)
                    startTelemetryLoop()
                }
                "disconnected" -> {
                    MockVpnRepository.onDisconnected()
                    stopTelemetryLoop()
                }
                "error" -> {
                    MockVpnRepository.onDisconnected()
                    stopTelemetryLoop()
                    MockVpnRepository.setError(
                        intent.getStringExtra(IfixVpnService.EXTRA_MESSAGE) ?: "خطا در VPN"
                    )
                }
            }
        }
    }

    init {
        val app = getApplication<Application>()
        val filter = IntentFilter(IfixVpnService.BROADCAST_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            app.registerReceiver(vpnReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            app.registerReceiver(vpnReceiver, filter)
        }

        viewModelScope.launch {
            connectionState.collect { state ->
                if (state == VpnConnectionState.CONNECTED) startTelemetryLoop()
                else if (state == VpnConnectionState.DISCONNECTED) stopTelemetryLoop()
            }
        }

        restoreSession()

        viewModelScope.launch {
            refreshServers()
        }

        if (IfixVpnService.isRunning) {
            MockVpnRepository.setConnectionState(VpnConnectionState.CONNECTED)
            startTelemetryLoop()
        }
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

    fun refreshServers(url: String = SubscriptionLoader.DEFAULT_SUB_URL) {
        viewModelScope.launch {
            _isRefreshingServers.value = true
            MockVpnRepository.setError(null)
            val result = MockVpnRepository.refreshServersFromSubscription(url)
            _isRefreshingServers.value = false
            result.onFailure {
                MockVpnRepository.setError("به‌روزرسانی سرور: ${it.message}")
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
            delay(600)
            val user = MockVpnRepository.login(username.trim(), passwordHash)
            _isAuthenticating.value = false
            if (user != null) {
                if (user.isActive) {
                    if (rememberMe) sessionStore.saveSession(username.trim(), passwordHash)
                    else sessionStore.clear()
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
        val app = getApplication<Application>()
        IfixVpnService.disconnect(app)
        sessionStore.clear()
        MockVpnRepository.logout()
        onLogoutComplete()
    }

    /** Real Xray tunnel (or clear error if core/config missing). */
    fun toggleConnection() {
        val app = getApplication<Application>()
        val state = connectionState.value

        if (state == VpnConnectionState.CONNECTED || state == VpnConnectionState.CONNECTING) {
            IfixVpnService.disconnect(app)
            return
        }

        val server = selectedServer.value
        if (server.configUri.isBlank()) {
            MockVpnRepository.setError("این سرور کانفیگ ندارد. ساب را رفرش کنید.")
            return
        }
        if (!XrayEngine.isAvailable()) {
            MockVpnRepository.setError(
                "هسته Xray نصب نیست. libv2ray.aar را در app/libs بگذارید و APK را دوباره بسازید."
            )
            return
        }

        val prepare = VpnService.prepare(app)
        if (prepare != null) {
            _vpnPermissionIntent.value = prepare
            return
        }
        startRealVpn(server)
    }

    fun onVpnPermissionResult(granted: Boolean) {
        _vpnPermissionIntent.value = null
        if (!granted) {
            MockVpnRepository.setError("مجوز VPN داده نشد.")
            return
        }
        startRealVpn(selectedServer.value)
    }

    private fun startRealVpn(server: VpnServer) {
        val app = getApplication<Application>()
        MockVpnRepository.setError(null)
        MockVpnRepository.setConnectionState(VpnConnectionState.CONNECTING)
        IfixVpnService.connect(app, server.configUri, "${server.countryFlag} ${server.countryName}")
    }

    fun selectServer(server: VpnServer) {
        val wasConnected = connectionState.value == VpnConnectionState.CONNECTED
        MockVpnRepository.selectServer(server)
        if (wasConnected) {
            val app = getApplication<Application>()
            IfixVpnService.disconnect(app)
            viewModelScope.launch {
                delay(500)
                if (server.configUri.isNotBlank()) {
                    if (VpnService.prepare(app) == null) startRealVpn(server)
                    else _vpnPermissionIntent.value = VpnService.prepare(app)
                }
            }
        }
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

    fun clearError() {
        MockVpnRepository.setError(null)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(vpnReceiver)
        } catch (_: Exception) {
        }
        stopTelemetryLoop()
    }
}
