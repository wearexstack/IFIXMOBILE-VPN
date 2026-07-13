package com.example.data.repository

import com.example.data.model.User
import com.example.data.model.VpnServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class VpnRepository {

    // Simulated local User Database
    private val _users = MutableStateFlow<List<User>>(
        listOf(
            User(
                id = "1",
                username = "admin",
                passwordHash = "admin",
                email = "admin@novavpn.ir",
                role = "admin",
                isActive = true,
                expiresAt = "نامحدود",
                createdAt = "۱۴۰۵/۰۱/۰۱"
            ),
            User(
                id = "2",
                username = "demo",
                passwordHash = "demo",
                email = "demo@novavpn.ir",
                role = "user",
                isActive = true,
                expiresAt = "۱۴۰۶/۰۵/۲۰",
                createdAt = "۱۴۰۵/۰۴/۱۵"
            ),
            User(
                id = "3",
                username = "shahin",
                passwordHash = "123456",
                email = "shahin@gmail.com",
                role = "user",
                isActive = true,
                expiresAt = "۱۴۰۵/۰۸/۳۰",
                createdAt = "۱۴۰۵/۰۴/۱۰"
            ),
            User(
                id = "4",
                username = "sara_user",
                passwordHash = "sara123",
                email = "sara@novavpn.ir",
                role = "user",
                isActive = false, // Disabled account example
                expiresAt = "۱۴۰۵/۰۵/۰۱",
                createdAt = "۱۴۰۵/۰۲/۰۵"
            )
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Simulated local Server Database
    private val _servers = MutableStateFlow<List<VpnServer>>(
        listOf(
            VpnServer("s1", "آلمان", "🇩🇪", "فرانکفورت - سرور ۱", "185.120.44.12", 42, 18, isPremium = false, isFavorite = true),
            VpnServer("s2", "آلمان", "🇩🇪", "مونیخ - سرور ۲ (مخصوص بازی)", "185.120.44.20", 35, 62, isPremium = true),
            VpnServer("s3", "ایالات متحده", "🇺🇸", "نیویورک - سرور ۱", "104.244.42.1", 110, 31, isPremium = false),
            VpnServer("s4", "ایالات متحده", "🇺🇸", "لس‌آنجلس - پرسرعت", "104.244.42.10", 145, 84, isPremium = true, isFavorite = true),
            VpnServer("s5", "انگلستان", "🇬🇧", "لندن - سرور ۱", "195.154.122.5", 58, 24, isPremium = false),
            VpnServer("s6", "هلند", "🇳🇱", "آمستردام - پرسرعت", "82.197.200.4", 48, 12, isPremium = false),
            VpnServer("s7", "فنلاند", "🇫🇮", "هلسینکی - سرور امن", "95.217.140.22", 65, 45, isPremium = true),
            VpnServer("s8", "ترکیه", "🇹🇷", "استانبول - پینگ پایین", "176.53.18.150", 28, 55, isPremium = false, isFavorite = true),
            VpnServer("s9", "سنگاپور", "🇸🇬", "سنگاپور - شرق آسیا", "128.199.112.5", 190, 78, isPremium = true),
            VpnServer("s10", "ژاپن", "🇯🇵", "توکیو - سرور ۳", "210.140.10.1", 215, 30, isPremium = true)
        )
    )
    val servers: StateFlow<List<VpnServer>> = _servers.asStateFlow()

    // Authentication Actions
    fun authenticate(username: String, password: String): User? {
        val user = _users.value.find { it.username.lowercase() == username.lowercase() }
        if (user != null && user.passwordHash == password) {
            return if (user.isActive) user else null
        }
        return null
    }

    // Server Selection and Favorites
    fun toggleFavorite(serverId: String) {
        _servers.value = _servers.value.map {
            if (it.id == serverId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    // Backend / Admin Actions (User Management)
    fun addUser(username: String, email: String, passwordHash: String, expiresAt: String, role: String): Boolean {
        if (_users.value.any { it.username.lowercase() == username.lowercase() }) {
            return false // User already exists
        }
        val newUser = User(
            id = UUID.randomUUID().toString(),
            username = username,
            passwordHash = passwordHash,
            email = email,
            role = role,
            isActive = true,
            expiresAt = expiresAt,
            createdAt = "۱۴۰۵/۰۴/۲۲" // Current simulated Persian date
        )
        _users.value = _users.value + newUser
        return true
    }

    fun deleteUser(userId: String): Boolean {
        if (userId == "1") return false // Cannot delete the master admin
        _users.value = _users.value.filter { it.id != userId }
        return true
    }

    fun toggleUserStatus(userId: String) {
        if (userId == "1") return // Cannot disable master admin
        _users.value = _users.value.map {
            if (it.id == userId) it.copy(isActive = !it.isActive) else it
        }
    }

    // Server Management
    fun addServer(countryName: String, flag: String, city: String, ip: String, ping: Int, isPremium: Boolean): Boolean {
        val newServer = VpnServer(
            id = UUID.randomUUID().toString(),
            countryName = countryName,
            countryFlag = flag,
            city = city,
            ipAddress = ip,
            pingMs = ping,
            loadPercentage = (10..90).random(),
            isPremium = isPremium
        )
        _servers.value = _servers.value + newServer
        return true
    }

    fun toggleServerStatus(serverId: String) {
        _servers.value = _servers.value.map {
            if (it.id == serverId) it.copy(isActive = !it.isActive) else it
        }
    }

    // Simulated Stats Engine for Admin Panel
    fun getSystemStats(): Map<String, Any> {
        val totalUsers = _users.value.size
        val activeUsers = _users.value.count { it.isActive }
        val disabledUsers = totalUsers - activeUsers
        val activeServers = _servers.value.count { it.isActive }
        val avgPing = _servers.value.filter { it.isActive }.map { it.pingMs }.average().toInt()
        
        return mapOf(
            "totalUsers" to totalUsers,
            "activeUsers" to activeUsers,
            "disabledUsers" to disabledUsers,
            "activeServers" to activeServers,
            "avgPing" to avgPing,
            "totalTraffic" to "۱.۴ Terabytes",
            "uptime" to "۹۹.۹۸٪"
        )
    }
}
