package com.example.data

import android.util.Base64
import com.example.data.model.VpnServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

/**
 * Downloads subscription body and parses vless/trojan/vmess/ss lines into VpnServer list.
 */
object SubscriptionLoader {

    const val DEFAULT_SUB_URL =
        "https://raw.githubusercontent.com/wearexstack/xstack/main/sub"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun fetchAndParse(subUrl: String = DEFAULT_SUB_URL): List<VpnServer> {
        val request = Request.Builder()
            .url(subUrl.trim())
            .header("User-Agent", "IFIX-Mobile-VPN/1.1")
            .get()
            .build()
        val body = client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
        if (body.isBlank()) throw Exception("ساب خالی است")

        val text = tryDecodeBase64(body) ?: body
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("#") }

        return lines.mapIndexedNotNull { index, line ->
            parseNode(line, index)
        }
    }

    private fun tryDecodeBase64(raw: String): String? {
        val cleaned = raw.trim().replace("\n", "").replace("\r", "")
        if (cleaned.contains("://")) return null
        return try {
            String(Base64.decode(cleaned, Base64.DEFAULT or Base64.URL_SAFE or Base64.NO_WRAP))
        } catch (_: Exception) {
            null
        }
    }

    private fun parseNode(uri: String, index: Int): VpnServer? {
        val scheme = uri.substringBefore("://").lowercase()
        if (scheme !in listOf("vless", "trojan", "vmess", "ss")) return null

        return try {
            when (scheme) {
                "vmess" -> parseVmess(uri, index)
                else -> parseShare(uri, index, scheme)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseShare(uri: String, index: Int, scheme: String): VpnServer {
        val withoutScheme = uri.substringAfter("://")
        val namePart = withoutScheme.substringAfter("#", "")
        val name = if (namePart.isNotBlank()) {
            URLDecoder.decode(namePart, "UTF-8")
        } else {
            "$scheme-${index + 1}"
        }
        val main = withoutScheme.substringBefore("#").substringBefore("?")
        val hostPort = main.substringAfter("@", main)
        val host = hostPort.substringBefore(":")
        val port = hostPort.substringAfter(":", "443").toIntOrNull() ?: 443
        val flag = guessFlag(name, host)
        return VpnServer(
            id = "sub_$index",
            countryName = name.take(40),
            countryFlag = flag,
            city = scheme.uppercase(),
            ipAddress = host,
            pingMs = (30..120).random(),
            loadPercentage = (15..70).random(),
            isPremium = scheme == "vless",
            configUri = uri
        )
    }

    private fun parseVmess(uri: String, index: Int): VpnServer {
        val b64 = uri.substringAfter("vmess://")
        val json = String(Base64.decode(b64, Base64.DEFAULT or Base64.URL_SAFE or Base64.NO_WRAP))
        // minimal parse without full JSON lib dependency issues – use simple regex
        fun field(key: String): String {
            val re = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
            return re.find(json)?.groupValues?.getOrNull(1).orEmpty()
        }
        val host = field("add")
        val name = field("ps").ifBlank { "vmess-${index + 1}" }
        val port = field("port").toIntOrNull() ?: 443
        return VpnServer(
            id = "sub_$index",
            countryName = name.take(40),
            countryFlag = guessFlag(name, host),
            city = "VMESS",
            ipAddress = host.ifBlank { "unknown" },
            pingMs = (30..120).random(),
            loadPercentage = (15..70).random(),
            isPremium = true,
            configUri = uri
        )
    }

    private fun guessFlag(name: String, host: String): String {
        val n = (name + host).lowercase()
        return when {
            "de" in n || "آلمان" in n || "frankfurt" in n || "berlin" in n -> "🇩🇪"
            "us" in n || "آمریکا" in n || "newyork" in n || "los" in n -> "🇺🇸"
            "gb" in n || "uk" in n || "london" in n || "انگلیس" in n -> "🇬🇧"
            "nl" in n || "amsterdam" in n || "هلند" in n -> "🇳🇱"
            "tr" in n || "istanbul" in n || "ترک" in n -> "🇹🇷"
            "fi" in n || "helsinki" in n || "فنلاند" in n -> "🇫🇮"
            "sg" in n || "singapore" in n -> "🇸🇬"
            "jp" in n || "tokyo" in n || "ژاپن" in n -> "🇯🇵"
            "fr" in n || "paris" in n || "فرانسه" in n -> "🇫🇷"
            else -> "🌐"
        }
    }
}
