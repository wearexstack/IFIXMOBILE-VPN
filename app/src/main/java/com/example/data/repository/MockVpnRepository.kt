package com.example.data.repository

import com.example.data.model.User
import com.example.data.model.VpnServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

object MockVpnRepository {

    // Initial server seed
    private val _servers = MutableStateFlow<List<VpnServer>>(
        listOf(
            VpnServer("1", "آلمان", "🇩🇪", "فرانکفورت - سرور ۱", "185.122.14.5", 42, 28, isPremium = false, isFavorite = true),
            VpnServer("2", "آلمان", "🇩🇪", "برلین - سرور ۲", "185.122.14.9", 55, 64, isPremium = false),
            VpnServer("3", "فنلاند", "🇫🇮", "هلسینکی", "95.175.99.11", 68, 12, isPremium = false),
            VpnServer("4", "آمریکا", "🇺🇸", "نیویورک - ویژه VIP", "104.244.72.1", 125, 45, isPremium = true, isFavorite = true),
            VpnServer("5", "هلند", "🇳🇱", "آمستردام - پرسرعت", "82.197.200.50", 49, 18, isPremium = false),
            VpnServer("6", "انگلستان", "🇬🇧", "لندن - سرور ویژه", "45.132.112.4", 58, 82, isPremium = true),
            VpnServer("7", "ترکیه", "🇹🇷", "استانبول - سرور آسیایی", "176.220.10.3", 35, 91, isPremium = true),
            VpnServer("8", "سنگاپور", "🇸🇬", "سنگاپور - گیمینگ", "111.90.150.12", 182, 31, isPremium = true)
        )
    )
    val servers: StateFlow<List<VpnServer>> = _servers.asStateFlow()

    // Initial users seed for Admin panel simulation
    private val _users = MutableStateFlow<List<User>>(
        listOf(
            User("u1", "admin", "admin", "admin@ifixmobile.com", "admin", true, "۱۴۰۶/۱۲/۲۹"),
            User("u2", "taher", "123456", "taher@ifixmobile.com", "user", true, "۱۴۰۵/۱۲/۲۹"),
            User("u3", "sara_ahmadi", "123456", "sara@gmail.com", "user", true, "۱۴۰۵/۰۹/۱۵"),
            User("u4", "ali_reza", "123456", "ali@yahoo.com", "user", false, "منقضی شده"),
            User("u5", "reza_vpn", "123456", "reza@gmail.com", "user", true, "۱۴۰۵/۰۸/۳۰")
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Connection state
    private val _connectionState = MutableStateFlow(VpnConnectionState.DISCONNECTED)
    val connectionState: StateFlow<VpnConnectionState> = _connectionState.asStateFlow()

    // Selected server
    private val _selectedServer = MutableStateFlow<VpnServer>(_servers.value[0])
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    // Dynamic stats
    private val _downloadSpeed = MutableStateFlow("۰.۰ KB/s")
    val downloadSpeed: StateFlow<String> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow("۰.۰ KB/s")
    val uploadSpeed: StateFlow<String> = _uploadSpeed.asStateFlow()

    private val _connectionDuration = MutableStateFlow(0) // in seconds
    val connectionDuration: StateFlow<Int> = _connectionDuration.asStateFlow()

    private val _currentIpAddress = MutableStateFlow("79.127.124.89") // Original Iranian IP
    val currentIpAddress: StateFlow<String> = _currentIpAddress.asStateFlow()

    // Authentication session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Settings memory
    val isAutoConnect = MutableStateFlow(false)
    val isNotificationEnabled = MutableStateFlow(true)
    val isDarkTheme = MutableStateFlow(true)

    fun login(username: String, passwordHash: String): User? {
        val found = _users.value.find { 
            it.username.equals(username, ignoreCase = true) && it.passwordHash == passwordHash 
        }
        if (found != null) {
            _currentUser.value = found
            // If user is auto-connected or settings are stored, simulate it here
        }
        return found
    }

    fun logout() {
        disconnect()
        _currentUser.value = null
    }

    fun selectServer(server: VpnServer) {
        if (_connectionState.value == VpnConnectionState.CONNECTED) {
            disconnect()
        }
        _selectedServer.value = server
    }

    fun toggleFavorite(serverId: String) {
        _servers.value = _servers.value.map {
            if (it.id == serverId) it.copy(isFavorite = !it.isFavorite) else it
        }
        if (_selectedServer.value.id == serverId) {
            _selectedServer.value = _selectedServer.value.copy(isFavorite = !_selectedServer.value.isFavorite)
        }
    }

    fun setConnectionState(state: VpnConnectionState) {
        _connectionState.value = state
    }

    fun disconnect() {
        _connectionState.value = VpnConnectionState.DISCONNECTED
        _downloadSpeed.value = "۰.۰ KB/s"
        _uploadSpeed.value = "۰.۰ KB/s"
        _connectionDuration.value = 0
        _currentIpAddress.value = "79.127.124.89" // Revert to local IP
    }

    suspend fun connect() {
        if (_connectionState.value == VpnConnectionState.CONNECTED) return
        
        _connectionState.value = VpnConnectionState.CONNECTING
        delay(1500) // Simulating network handshake and key exchange
        
        _connectionState.value = VpnConnectionState.CONNECTED
        _currentIpAddress.value = _selectedServer.value.ipAddress
    }

    // Dynamic telemetry updates (called from loop in ViewModel)
    fun updateLiveStats() {
        if (_connectionState.value != VpnConnectionState.CONNECTED) return

        _connectionDuration.value += 1

        val downKb = Random.nextDouble(100.0, 3200.0)
        val upKb = downKb * Random.nextDouble(0.15, 0.4)

        _downloadSpeed.value = formatSpeed(downKb)
        _uploadSpeed.value = formatSpeed(upKb)
    }

    private fun formatSpeed(kb: Double): String {
        return if (kb > 1024.0) {
            val mb = kb / 1024.0
            String.format("%.1f MB/s", mb).toPersianNumbers()
        } else {
            String.format("%.1f KB/s", kb).toPersianNumbers()
        }
    }

    // Admin Panel: Users Management
    fun addUser(username: String, email: String, expiry: String, role: String = "user"): Boolean {
        if (_users.value.any { it.username.equals(username, ignoreCase = true) }) {
            return false // Already exists
        }
        val newUser = User(
            id = "u" + (Random.nextInt(100, 9999)),
            username = username,
            passwordHash = "123456", // default password
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

    // Admin Panel: Servers Management
    fun addServer(country: String, flag: String, city: String, ip: String, ping: Int, load: Int, premium: Boolean): Boolean {
        val newServer = VpnServer(
            id = "s" + (Random.nextInt(100, 9999)),
            countryName = country,
            countryFlag = flag,
            city = city,
            ipAddress = ip,
            pingMs = ping,
            loadPercentage = load,
            isPremium = premium
        )
        _servers.value = _servers.value + newServer
        return true
    }

    fun deleteServer(serverId: String) {
        _servers.value = _servers.value.filterNot { it.id == serverId }
    }

    // Helper to convert English digits to Persian digits for premium RTL feel
    fun String.toPersianNumbers(): String {
        var result = this
        val englishDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        for (i in 0..9) {
            result = result.replace(englishDigits[i], persianDigits[i])
        }
        return result
    }
}

enum class VpnConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
