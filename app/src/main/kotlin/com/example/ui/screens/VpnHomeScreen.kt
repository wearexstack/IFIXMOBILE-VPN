package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.VpnServer
import com.example.data.models.VpnStatus

@Composable
fun VpnHomeScreen() {
    var vpnStatus by remember { mutableStateOf(VpnStatus.DISCONNECTED) }
    var selectedServer by remember { mutableStateOf<VpnServer?>(null) }
    var showServerList by remember { mutableStateOf(false) }

    val mockServers = listOf(
        VpnServer(
            id = "1",
            name = "Tehran 1",
            country = "Iran",
            countryCode = "IR",
            address = "185.27.114.1",
            port = 1194,
            protocol = "OpenVPN",
            latency = 15,
            load = 0.45f
        ),
        VpnServer(
            id = "2",
            name = "Amsterdam",
            country = "Netherlands",
            countryCode = "NL",
            address = "213.154.115.1",
            port = 1194,
            protocol = "OpenVPN",
            latency = 89,
            load = 0.62f
        ),
        VpnServer(
            id = "3",
            name = "Singapore",
            country = "Singapore",
            countryCode = "SG",
            address = "103.145.23.1",
            port = 1194,
            protocol = "OpenVPN",
            latency = 156,
            load = 0.38f
        ),
        VpnServer(
            id = "4",
            name = "New York",
            country = "USA",
            countryCode = "US",
            address = "185.217.116.1",
            port = 1194,
            protocol = "OpenVPN",
            latency = 122,
            load = 0.55f
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.foundation.background(Color(0xFF1a1a2e)).brush
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "IFIXMOBILE VPN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(
                onClick = { /* Settings */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // Connection Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213e)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when (vpnStatus) {
                        VpnStatus.CONNECTED -> "Connected"
                        VpnStatus.CONNECTING -> "Connecting..."
                        VpnStatus.DISCONNECTING -> "Disconnecting..."
                        VpnStatus.ERROR -> "Error"
                        else -> "Disconnected"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (vpnStatus) {
                        VpnStatus.CONNECTED -> Color(0xFF00D084)
                        VpnStatus.CONNECTING -> Color(0xFFFFB84D)
                        else -> Color(0xFFFF6B6B)
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (selectedServer != null) {
                    Text(
                        "${selectedServer!!.name} • ${selectedServer!!.country}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${selectedServer!!.latency}ms",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text("Latency", fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${(selectedServer!!.load ?: 0f) * 100}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text("Load", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Connect Button
        Button(
            onClick = {
                if (vpnStatus == VpnStatus.DISCONNECTED) {
                    vpnStatus = VpnStatus.CONNECTING
                    if (selectedServer == null && mockServers.isNotEmpty()) {
                        selectedServer = mockServers[0]
                    }
                } else if (vpnStatus == VpnStatus.CONNECTED) {
                    vpnStatus = VpnStatus.DISCONNECTING
                }
            },
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = when (vpnStatus) {
                    VpnStatus.CONNECTED -> Color(0xFF00D084)
                    VpnStatus.CONNECTING -> Color(0xFFFFB84D)
                    else -> Color(0xFF0F3460)
                }
            )
        ) {
            Icon(
                imageVector = if (vpnStatus == VpnStatus.CONNECTED ||
                    vpnStatus == VpnStatus.CONNECTING
                ) Icons.Default.Close else Icons.Default.Done,
                contentDescription = "Connect",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }

        // Server List
        Text(
            "Available Servers",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockServers) { server ->
                ServerListItem(
                    server = server,
                    isSelected = selectedServer?.id == server.id,
                    isConnected = vpnStatus == VpnStatus.CONNECTED && selectedServer?.id == server.id,
                    onClick = {
                        selectedServer = server
                        if (vpnStatus == VpnStatus.DISCONNECTED) {
                            vpnStatus = VpnStatus.CONNECTING
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ServerListItem(
    server: VpnServer,
    isSelected: Boolean,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) Color(0xFF0F3460) else Color(0xFF16213e)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0F3460) else Color(0xFF16213e)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    server.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "${server.country} • ${server.protocol}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    "${server.latency}ms",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Load ${(server.load ?: 0f) * 100}%",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (isConnected) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Connected",
                    tint = Color(0xFF00D084),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}