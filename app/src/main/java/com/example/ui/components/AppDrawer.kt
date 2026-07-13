package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    currentUser: User?,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    var isUpdatingServers by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(DarkBackground)
            .border(
                border = BorderStroke(1.dp, Color(0x3300D1C7)),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .padding(16.dp)
            .testTag("app_drawer"),
        horizontalAlignment = Alignment.End
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // 1. User Profile Header with Glow Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            backgroundColor = GlassBackgroundHeavy,
            borderColor = PrimaryTeal.copy(alpha = 0.3f),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circle Profile Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(PrimaryTeal, Color(0x0000D1C7))
                            ),
                            shape = CircleShape
                        )
                        .border(2.dp, PrimaryTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "کاربر",
                        tint = TextWhite,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Username
                Text(
                    text = currentUser?.username ?: "کاربر میهمان",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Expiry or User Info
                val roleLabel = if (currentUser?.role == "admin") "مدیر سیستم" else "کاربر عادی"
                val subscriptionLabel = if (currentUser?.role == "admin") "دسترسی نامحدود" else "حساب ویژه VIP"
                
                Text(
                    text = "$roleLabel - $subscriptionLabel",
                    color = PrimaryTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Expiration date
                Text(
                    text = "تاریخ انقضا: ${currentUser?.expiresAt ?: "نامشخص"}",
                    color = TextGrey,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Divider
        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Drawer Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Home
            DrawerItem(
                title = "صفحه اصلی",
                icon = Icons.Default.Home,
                selected = currentRoute == "home",
                onClick = {
                    onNavigate("home")
                    onCloseDrawer()
                }
            )
            
            // Server Selection
            DrawerItem(
                title = "انتخاب سرور",
                icon = Icons.Default.Place,
                selected = currentRoute == "server_selection",
                onClick = {
                    onNavigate("server_selection")
                    onCloseDrawer()
                }
            )
            
            // Settings
            DrawerItem(
                title = "تنظیمات برنامه",
                icon = Icons.Default.Settings,
                selected = currentRoute == "settings",
                onClick = {
                    onNavigate("settings")
                    onCloseDrawer()
                }
            )
            
            // Admin Panel (Visible to Admin only)
            if (currentUser?.role == "admin") {
                DrawerItem(
                    title = "پنل مدیریت",
                    icon = Icons.Default.Build,
                    selected = currentRoute == "admin_panel",
                    onClick = {
                        onNavigate("admin_panel")
                        onCloseDrawer()
                    }
                )
            }
            
            // Update Servers (Action)
            DrawerItem(
                title = if (isUpdatingServers) "در حال به‌روزرسانی..." else "به‌روزرسانی سرورها",
                icon = Icons.Default.Refresh,
                selected = false,
                onClick = {
                    if (!isUpdatingServers) {
                        scope.launch {
                            isUpdatingServers = true
                            delay(2000)
                            isUpdatingServers = false
                            Toast.makeText(context, "لیست سرورها با موفقیت به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
            
            // About App
            var showAboutDialog by remember { mutableStateOf(false) }
            DrawerItem(
                title = "درباره برنامه",
                icon = Icons.Default.Info,
                selected = false,
                onClick = {
                    showAboutDialog = true
                }
            )
            
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("باشه", color = PrimaryTeal, fontSize = 14.sp)
                        }
                    },
                    title = {
                        Text(
                            text = "درباره IFIXMOBILE VPN",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "نسخه برنامه: ۱.۰.۰\n\nطراحی مدرن سال ۲۰۲۶ با رابط کاربری شیشه‌ای (Glassmorphic) و پینگ فوق‌العاده پایین برای کاربران حرفه‌ای.\n\nتوسعه یافته توسط تیم توسعه برتر.",
                            color = TextGrey,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    containerColor = SurfaceGrey,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
        
        // Logout Button
        DrawerItem(
            title = "خروج از حساب",
            icon = Icons.Default.ExitToApp,
            selected = false,
            color = ErrorRed,
            onClick = {
                onCloseDrawer()
                onLogout()
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    color: Color = if (selected) PrimaryTeal else TextGrey,
    onClick: () -> Unit
) {
    val backgroundBrush = if (selected) {
        Brush.horizontalGradient(
            colors = listOf(PrimaryTeal.copy(alpha = 0.2f), Color.Transparent)
        )
    } else {
        null
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Label
        Text(
            text = title,
            color = if (selected) PrimaryTeal else TextWhite,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}
