package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.VpnConnectionState
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.VpnViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VpnViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToServers: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val currentIpAddress by viewModel.currentIpAddress.collectAsState()
    val formattedDuration = viewModel.getFormattedDuration()

    // Pulse animation logic for Connecting and Connected states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Layout structure
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "IFIXMOBILE VPN",
                            color = PrimaryTeal,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    // Left element to balance top app bar
                    IconButton(
                        onClick = onNavigateToServers,
                        modifier = Modifier.testTag("action_servers_list")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تغییر سرور",
                            tint = TextGrey
                        )
                    }
                },
                actions = {
                    // Right Drawer trigger (RTL oriented Drawer)
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("drawer_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "منو",
                            tint = PrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextWhite
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Futuristic background glowing halos
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center)
                    .offset(y = (-60).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                when (connectionState) {
                                    VpnConnectionState.CONNECTED -> PrimaryTeal.copy(alpha = 0.15f)
                                    VpnConnectionState.CONNECTING -> PingYellow.copy(alpha = 0.12f)
                                    VpnConnectionState.DISCONNECTED -> Color.Transparent
                                },
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Connection Status Banner
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = when (connectionState) {
                            VpnConnectionState.CONNECTED -> "اتصال با موفقیت برقرار شد"
                            VpnConnectionState.CONNECTING -> "در حال برقراری ارتباط..."
                            VpnConnectionState.DISCONNECTED -> "شما متصل نیستید"
                        },
                        color = when (connectionState) {
                            VpnConnectionState.CONNECTED -> SuccessGreen
                            VpnConnectionState.CONNECTING -> PingYellow
                            VpnConnectionState.DISCONNECTED -> TextGrey
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (connectionState == VpnConnectionState.CONNECTED) formattedDuration else "۰۰:۰۰:۰۰",
                        color = TextWhite,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }

                // 2. Central Giant Circular Button (Breathing state)
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(16.dp)
                        .testTag("vpn_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer decorative pulsating rings
                    if (connectionState != VpnConnectionState.DISCONNECTED) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(if (connectionState == VpnConnectionState.CONNECTED) pulseScale else 1f)
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        colors = if (connectionState == VpnConnectionState.CONNECTED) {
                                            listOf(PrimaryTeal.copy(alpha = 0.6f), Color.Transparent)
                                        } else {
                                            listOf(PingYellow.copy(alpha = 0.6f), Color.Transparent)
                                        }
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    // Secondary Pulse ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .scale(if (connectionState == VpnConnectionState.CONNECTED) (pulseScale * 0.96f) else 1f)
                            .border(
                                width = 1.dp,
                                color = when (connectionState) {
                                    VpnConnectionState.CONNECTED -> PrimaryTeal.copy(alpha = 0.3f)
                                    VpnConnectionState.CONNECTING -> PingYellow.copy(alpha = 0.3f)
                                    VpnConnectionState.DISCONNECTED -> Color(0x1AFFFFFF)
                                },
                                shape = CircleShape
                            )
                    )

                    // Central Interactive Button Surface
                    val buttonBackground = Brush.linearGradient(
                        colors = when (connectionState) {
                            VpnConnectionState.CONNECTED -> listOf(PrimaryTeal, Color(0xFF00B0A7))
                            VpnConnectionState.CONNECTING -> listOf(PingYellow, Color(0xFFF2C000))
                            VpnConnectionState.DISCONNECTED -> listOf(SurfaceGreyLight, SurfaceGrey)
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .shadow(
                                elevation = if (connectionState != VpnConnectionState.DISCONNECTED) 24.dp else 4.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = when (connectionState) {
                                    VpnConnectionState.CONNECTED -> PrimaryTeal
                                    VpnConnectionState.CONNECTING -> PingYellow
                                    VpnConnectionState.DISCONNECTED -> Color.Black
                                },
                                spotColor = when (connectionState) {
                                    VpnConnectionState.CONNECTED -> PrimaryTeal
                                    VpnConnectionState.CONNECTING -> PingYellow
                                    VpnConnectionState.DISCONNECTED -> Color.Black
                                }
                            )
                            .clip(CircleShape)
                            .background(buttonBackground)
                            .clickable { viewModel.toggleConnection() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Icon representing Connection
                            Icon(
                                imageVector = if (connectionState == VpnConnectionState.CONNECTED) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = "اتصال",
                                tint = if (connectionState == VpnConnectionState.DISCONNECTED) PrimaryTeal else DarkBackground,
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(if (connectionState == VpnConnectionState.CONNECTING) pulseScale else 1f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = when (connectionState) {
                                    VpnConnectionState.CONNECTED -> "قطع اتصال"
                                    VpnConnectionState.CONNECTING -> "در حال اتصال"
                                    VpnConnectionState.DISCONNECTED -> "ضربه بزنید"
                                },
                                color = if (connectionState == VpnConnectionState.DISCONNECTED) TextWhite else DarkBackground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. Current Location Selector Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .testTag("server_selector_trigger"),
                    backgroundColor = GlassBackgroundHeavy,
                    borderColor = PrimaryTeal.copy(alpha = 0.2f),
                    shadowElevation = 4.dp,
                    onClick = onNavigateToServers
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "لیست سرورها",
                            tint = TextGrey,
                            modifier = Modifier.size(24.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "${selectedServer.countryName} - ${selectedServer.city}",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "پینگ: ${selectedServer.pingMs} میلی‌ثانیه | ظرفیت: ${selectedServer.loadPercentage}٪",
                                    color = TextGrey,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Right
                                )
                            }
                            
                            // Country Flag Emoji Container
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0x11FFFFFF), CircleShape)
                                    .border(1.dp, Color(0x15FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedServer.countryFlag,
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Telemetry Glass Box
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("telemetry_panel"),
                    backgroundColor = GlassBackground,
                    borderColor = GlassBorder,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // IP & Ping row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // IP Box
                            TelemetrySubItem(
                                title = "آدرس آی‌پی (IP)",
                                value = currentIpAddress,
                                isHighlighted = connectionState == VpnConnectionState.CONNECTED
                            )

                            // Ping Box
                            TelemetrySubItem(
                                title = "پینگ زمان تأخیر",
                                value = "${selectedServer.pingMs} ms",
                                isHighlighted = connectionState == VpnConnectionState.CONNECTED,
                                highlightColor = if (selectedServer.pingMs < 60) PingGreen else if (selectedServer.pingMs < 120) PingYellow else PingRed
                            )
                        }

                        HorizontalDivider(
                            color = Color(0x10FFFFFF),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Network Speeds Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Upload Speed
                            TelemetrySpeedSubItem(
                                title = "سرعت آپلود (UL)",
                                speedValue = uploadSpeed,
                                isUpload = true,
                                isConnected = connectionState == VpnConnectionState.CONNECTED
                            )

                            // Download Speed
                            TelemetrySpeedSubItem(
                                title = "سرعت دانلود (DL)",
                                speedValue = downloadSpeed,
                                isUpload = false,
                                isConnected = connectionState == VpnConnectionState.CONNECTED
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetrySubItem(
    title: String,
    value: String,
    isHighlighted: Boolean,
    highlightColor: Color = PrimaryTeal
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp)
    ) {
        Text(
            text = title,
            color = TextGrey,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = if (isHighlighted) highlightColor else TextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TelemetrySpeedSubItem(
    title: String,
    speedValue: String,
    isUpload: Boolean,
    isConnected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp)
    ) {
        Text(
            text = title,
            color = TextGrey,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isConnected) speedValue else "۰.۰ KB/s",
                color = if (isConnected) (if (isUpload) PingYellow else PrimaryTeal) else TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
