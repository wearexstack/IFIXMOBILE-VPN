package com.example.data.repository

import com.example.data.SubscriptionLoader
import com.example.data.model.User
import com.example.data.model.VpnServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class VpnConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

object MockVpnRepository {

    private val _servers = MutableStateFlow<List<VpnServer>>(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers.asStateFlow()

    private val _users = MutableStateFlow(
        listOf(
            User("u1", "admin", "admin", "admin@ifixmobile.com", "admin", true, "۱۴۰۶/۱۲/۲۹"),
            User("u2", "taher", "123456", "taher@ifixmobile.com", "user", true, "۱۴۰۵/۱۲/۲۹"),
            User("u3", "sara_ahmadi", "123456", "sara@gmail.com", "user", true, "۱۴۰۵/۰۹/۱۵"),
            User("u4", "ali_reza", "123456", "ali@yahoo.com", "user", false, "منقضی شده"),
            User("u5", "reza_vpn", "123456", "reza@gmail.com", "user", true, "۱۴۰۵/۰۸/۳۰")
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _connectionState = MutableStateFlow(VpnConnectionState.DISCONNECTED)
    val connectionState: StateFlow<VpnConnectionState> = _connectionState.asStateFlow()

    private val _selectedServer = MutableStateFlow(
        VpnServer("0", "در حال بارگذاری…", "🌐", "—", "—", 0, 0, false)
    )
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _downloadSpeed = MutableStateFlow("۰.۰ KB/s")
    val downloadSpeed: StateFlow<String> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow("۰.۰ KB/s")
    val uploadSpeed: StateFlow<String> = _uploadSpeed.asStateFlow()

    private val _connectionDuration = MutableStateFlow(0)
    val connectionDuration: StateFlow<Int> = _connectionDuration.asStateFlow()

    private val _currentIpAddress = MutableStateFlow("—")
    val currentIpAddress: StateFlow<String> = _currentIpAddress.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isAutoConnect = MutableStateFlow(false)
    val isNotificationEnabled = MutableStateFlow(true)
    val isDarkTheme = MutableStateFlow(true)

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun setConnectionState(state: VpnConnectionState) {
        _connectionState.value = state
    }

    fun setError(msg: String?) {
        _lastError.value = msg
    }

    suspend fun refreshServersFromSubscription(url: String = SubscriptionLoader.DEFAULT_SUB_URL): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val list = SubscriptionLoader.fetchAndParse(url)
                if (list.isEmpty()) {
                    return@withContext Result.failure(Exception("هیچ نودی پارس نشد"))
                }
                _servers.value = list
                if (_selectedServer.value.configUri.isBlank() ||
                    list.none { it.id == _selectedServer.value.id }
                ) {
                    _selectedServer.value = list.first()
                }
                Result.success(list.size)
            } catch (e: Exception) {
                if (_servers.value.isEmpty()) {
                    // offline placeholder – cannot connect without configUri
                    _servers.value = listOf(
                        VpnServer(
                            id = "offline",
                            countryName = "آفلاین – ساب را رفرش کنید",
                            countryFlag = "⚠️",
                            city = "—",
                            ipAddress = "localhost",
                            pingMs = 999,
                            loadPercentage = 0,
                            isPremium = false,
                            isActive = false,
                            configUri = ""
                        )
                    )
                    _selectedServer.value = _servers.value.first()
                }
                Result.failure(e)
            }
        }
    }

    fun login(username: String, passwordHash: String): User? {
        val found = _users.value.find {
            it.username.equals(username, ignoreCase = true) && it.passwordHash == passwordHash
        }
        if (found != null) _currentUser.value = found
        return found
    }

    fun logout() {
        _currentUser.value = null
        setConnectionState(VpnConnectionState.DISCONNECTED)
        _downloadSpeed.value = "۰.۰ KB/s"
        _uploadSpeed.value = "۰.۰ KB/s"
        _connectionDuration.value = 0
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
    }

    fun toggleFavorite(serverId: String) {
        _servers.value = _servers.value.map {
            if (it.id == serverId) it.copy(isFavorite = !it.isFavorite) else it
        }
        if (_selectedServer.value.id == serverId) {
            _selectedServer.value = _selectedServer.value.copy(
                isFavorite = !_selectedServer.value.isFavorite
            )
        }
    }

    fun onConnected(serverIp: String) {
        _connectionState.value = VpnConnectionState.CONNECTED
        _currentIpAddress.value = serverIp
        _connectionDuration.value = 0
    }

    fun onDisconnected() {
        _connectionState.value = VpnConnectionState.DISCONNECTED
        _downloadSpeed.value = "۰.۰ KB/s"
        _uploadSpeed.value = "۰.۰ KB/s"
        _connectionDuration.value = 0
        _currentIpAddress.value = "—"
    }

    fun updateLiveStats() {
        if (_connectionState.value != VpnConnectionState.CONNECTED) return
        _connectionDuration.value += 1
        val downKb = Random.nextDouble(50.0, 2800.0)
        val upKb = downKb * Random.nextDouble(0.12, 0.35)
        _downloadSpeed.value = formatSpeed(downKb)
        _uploadSpeed.value = formatSpeed(upKb)
    }

    private fun formatSpeed(kb: Double): String {
        return if (kb > 1024.0) {
            String.format("%.1f MB/s", kb / 1024.0).toPersianNumbers()
        } else {
            String.format("%.1f KB/s", kb).toPersianNumbers()
        }
    }

    fun addUser(username: String, email: String, expiry: String, role: String = "user"): Boolean {
        if (_users.value.any { it.username.equals(username, ignoreCase = true) }) return false
        val newUser = User(
            id = "u" + Random.nextInt(100, 9999),
            username = username,
            passwordHash = "123456",
            email = email,
            role = role,
            isActive = true,
            expiresAt = expiry
        )
        _users.value = _users.value + newUser
        return true
    }

    fun deleteUser(userId: String) {
        _users.value = _users.value.filterNot { it.id == userId }
    }

    fun toggleUserStatus(userId: String) {
        _users.value = _users.value.map {
            if (it.id == userId) it.copy(isActive = !it.isActive) else it
        }
    }

    fun addServer(
        country: String,
        flag: String,
        city: String,
        ip: String,
        ping: Int,
        load: Int,
        premium: Boolean
    ): Boolean {
        val newServer = VpnServer(
            id = "s" + Random.nextInt(100, 9999),
            countryName = country,
            countryFlag = flag,
            city = city,
            ipAddress = ip,
            pingMs = ping,
            loadPercentage = load,
            isPremium = premium,
            configUri = ""
        )
        _servers.value = _servers.value + newServer
        return true
    }

    fun deleteServer(serverId: String) {
        _servers.value = _servers.value.filterNot { it.id == serverId }
    }

    fun String.toPersianNumbers(): String {
        var result = this
        val en = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        val fa = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        for (i in 0..9) result = result.replace(en[i], fa[i])
        return result
    }
}
