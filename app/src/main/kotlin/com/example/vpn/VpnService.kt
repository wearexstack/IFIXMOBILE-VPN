package com.example.vpn

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.net.VpnService as AndroidVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VpnService : AndroidVpnService() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            startVpnConnection()
        }
        return START_STICKY
    }

    private fun startVpnConnection() {
        try {
            val builder = Builder()
            builder.setSession("IFIXMOBILE VPN")
            builder.addAddress("192.168.1.1", 24)
            builder.addRoute("0.0.0.0", 0)
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("8.8.4.4")

            vpnInterface = builder.establish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVpnConnection() {
        vpnInterface?.close()
        vpnInterface = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpnConnection()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}