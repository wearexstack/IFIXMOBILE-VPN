package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VpnServer
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionScreen(
    viewModel: VpnViewModel,
    onBack: () -> Unit
) {
    val servers by viewModel.servers.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val searchQuery by viewModel.serverSearchQuery.collectAsState()
    val favoriteOnly by viewModel.serverFilterFavorite.collectAsState()
    
    val context = LocalContext.current

    // Local state for quick tab filtering (All vs Popular vs Premium VIP)
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Popular/Recent (low ping), 2: VIP/Premium

    // Filtered list
    val filteredServers = remember(servers, searchQuery, favoriteOnly, selectedTab) {
        servers.filter { server ->
            // Search criteria
            val matchesSearch = server.countryName.contains(searchQuery, ignoreCase = true) ||
                    server.city.contains(searchQuery, ignoreCase = true) ||
                    server.ipAddress.contains(searchQuery)
            
            // Favorite criteria
            val matchesFavorite = !favoriteOnly || server.isFavorite
            
            // Tab criteria
            val matchesTab = when (selectedTab) {
                1 -> server.pingMs < 60 // Popular (low ping)
                2 -> server.isPremium // VIP Premium
                else -> true // All
            }
            
            matchesSearch && matchesFavorite && matchesTab
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "انتخاب موقعیت سرور",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 48.dp) // Offset back button to center title
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = PrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextWhite
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Search Box with Glass Card
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setServerSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "جستجوی کشور، شهر یا آی‌پی...",
                        color = TextGrey,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = PrimaryTeal
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = Color(0x15FFFFFF),
                    cursorColor = PrimaryTeal,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_search_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Favorites and Sorting Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorites toggle filter
                IconButton(
                    onClick = { viewModel.toggleServerFavoriteFilter() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (favoriteOnly) PrimaryTeal.copy(alpha = 0.15f) else Color(0x10FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (favoriteOnly) PrimaryTeal else Color(0x15FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("favorites_filter_btn")
                ) {
                    Icon(
                        imageVector = if (favoriteOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "علاقه‌مندی‌ها",
                        tint = if (favoriteOnly) PrimaryTeal else TextGrey,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sorting Tabs (RTL order: All, Popular/Low Ping, VIP)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf("سرورهای VIP", "محبوب (پینگ کم)", "همه")
                    // Tabs array indices: 2 -> All, 1 -> Popular, 0 -> VIP Premium. We map index to match selectedTab: 0 -> All, 1 -> Popular, 2 -> VIP
                    tabs.forEachIndexed { index, title ->
                        val tabId = when (index) {
                            0 -> 2 // VIP Premium
                            1 -> 1 // Popular (low ping)
                            else -> 0 // All
                        }
                        val isSelected = selectedTab == tabId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrimaryTeal else Color(0x10FFFFFF))
                                .border(
                                    1.dp,
                                    if (isSelected) PrimaryTeal else Color(0x15FFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTab = tabId }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) DarkBackground else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Server List Header
            Text(
                text = "موقعیت‌های موجود (${filteredServers.size})",
                color = TextGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )

            // 4. Server LazyColumn List
            if (filteredServers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "خالی",
                            tint = TextGrey,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "هیچ سروری پیدا نشد!",
                            color = TextGrey,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("servers_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredServers, key = { it.id }) { server ->
                        val isSelected = selectedServer.id == server.id
                        
                        // Server Card
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) PrimaryTeal else Color.Transparent,
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            backgroundColor = if (isSelected) GlassBackgroundHeavy else GlassBackground,
                            borderColor = if (isSelected) PrimaryTeal.copy(alpha = 0.4f) else GlassBorder,
                            shadowElevation = if (isSelected) 8.dp else 2.dp,
                            onClick = {
                                viewModel.selectServer(server)
                                Toast.makeText(
                                    context, 
                                    "سرور ${server.countryName} (${server.city}) انتخاب شد", 
                                    Toast.LENGTH_SHORT
                                ).show()
                                onBack()
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Section: Ping Speed + Favorite Icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    // Favorite toggler
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(server.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (server.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "علاقه‌مندی",
                                            tint = if (server.isFavorite) ErrorRed else TextGrey,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Ping status pill
                                    val pingColor = if (server.pingMs < 60) PingGreen else if (server.pingMs < 120) PingYellow else PingRed
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(pingColor.copy(alpha = 0.15f))
                                            .border(1.dp, pingColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${server.pingMs} ms",
                                            color = pingColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Right Section: Server Details + Flag emoji
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            // Premium Crown badge
                                            if (server.isPremium) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "VIP",
                                                    tint = PingYellow,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .padding(end = 4.dp)
                                                )
                                                Text(
                                                    text = "VIP ",
                                                    color = PingYellow,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = server.countryName,
                                                color = TextWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        Text(
                                            text = server.city,
                                            color = TextGrey,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Right
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Server capacity indicator bar
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = "ظرفیت: ${server.loadPercentage}٪ (${server.statusText})",
                                                color = if (server.loadPercentage > 80) PingRed else if (server.loadPercentage > 45) PingYellow else SuccessGreen,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(4.dp)
                                                    .background(Color(0x1EFFFFFF), CircleShape)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(server.loadPercentage / 100f)
                                                        .background(
                                                            if (server.loadPercentage > 80) PingRed else if (server.loadPercentage > 45) PingYellow else SuccessGreen,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    // Flag emoji container
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                if (isSelected) PrimaryTeal.copy(alpha = 0.2f) else Color(0x10FFFFFF),
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) PrimaryTeal else Color(0x15FFFFFF),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = server.countryFlag,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
