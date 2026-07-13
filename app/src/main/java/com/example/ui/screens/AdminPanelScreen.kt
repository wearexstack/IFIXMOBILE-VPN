package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.VpnServer
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: VpnViewModel,
    onBack: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val servers by viewModel.servers.collectAsState()
    val userSearchQuery by viewModel.adminUserSearchQuery.collectAsState()

    val context = LocalContext.current

    // Internal sub-tabs: 0: Statistics, 1: Users, 2: Servers
    var selectedSubTab by remember { mutableStateOf(0) }

    // Dialog trigger states
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddServerDialog by remember { mutableStateOf(false) }

    // Add user inputs
    var newUsername by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserExpiry by remember { mutableStateOf("۱۴۰۶/۰۶/۳۰") }

    // Add server inputs
    var newServerCountry by remember { mutableStateOf("") }
    var newServerCity by remember { mutableStateOf("") }
    var newServerFlag by remember { mutableStateOf("🇩🇪") }
    var newServerIp by remember { mutableStateOf("") }
    var newServerPing by remember { mutableStateOf("45") }
    var newServerLoad by remember { mutableStateOf("15") }
    var newServerPremium by remember { mutableStateOf(false) }

    // Filtered users
    val filteredUsers = remember(users, userSearchQuery) {
        users.filter {
            it.username.contains(userSearchQuery, ignoreCase = true) ||
                    it.email.contains(userSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "پنل مدیریت سیستم",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 48.dp)
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

            // 1. Navigation Buttons for Admin Sub-Sections (RTL Layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Servers Tab
                AdminTabButton(
                    title = "مدیریت سرورها",
                    isSelected = selectedSubTab == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSubTab = 2 }
                )

                // Users Tab
                AdminTabButton(
                    title = "مدیریت کاربران",
                    isSelected = selectedSubTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSubTab = 1 }
                )

                // Overview Tab
                AdminTabButton(
                    title = "آمار و نظارت",
                    isSelected = selectedSubTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSubTab = 0 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Dynamic content based on selected tab
            when (selectedSubTab) {
                0 -> {
                    // TAB 0: SYSTEM OVERVIEW STATS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "خلاصه وضعیت سیستم",
                            color = TextGrey,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // 2x2 Stats Grid using Glass Cards
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatsCard(
                                title = "کل سرورها",
                                value = servers.size.toString(),
                                icon = Icons.Default.Place,
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = "کل کاربران",
                                value = users.size.toString(),
                                icon = Icons.Default.Person,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatsCard(
                                title = "کاربران فعال",
                                value = users.count { it.isActive }.toString(),
                                icon = Icons.Default.Check,
                                indicatorColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = "ترافیک مصرفی",
                                value = "۴.۸ TB",
                                icon = Icons.Default.Refresh,
                                indicatorColor = InfoBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "گزارش‌های نظارتی سرور",
                            color = TextGrey,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // Technical parameters card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = GlassBackground,
                            borderColor = GlassBorder
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                MonitorRow(label = "وضعیت هسته مرکزی", value = "فعال و ایمن", color = SuccessGreen)
                                MonitorRow(label = "بار پردازنده لود بالانسر", value = "۲۴٪ (بهینه)", color = PrimaryTeal)
                                MonitorRow(label = "میانگین پینگ کاربران", value = "۵۴ میلی‌ثانیه", color = SuccessGreen)
                                MonitorRow(label = "نسخه دیتابیس کاربران", value = "SQLite / Hive v3.2", color = TextGrey)
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: USERS MANAGEMENT
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Add User Button
                            Button(
                                onClick = { showAddUserDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal, contentColor = DarkBackground),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp).testTag("add_user_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("افزودن کاربر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Title
                            Text(
                                text = "لیست کاربران ثبت‌شده",
                                color = TextGrey,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // User search input
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { viewModel.setAdminUserSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = "جستجوی کاربر با نام یا ایمیل...",
                                    color = TextGrey,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = PrimaryTeal) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryTeal,
                                unfocusedBorderColor = Color(0x15FFFFFF),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Users lazy list
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f).testTag("admin_users_list")
                        ) {
                            items(filteredUsers, key = { it.id }) { user ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = GlassBackground,
                                    borderColor = if (user.isActive) GlassBorder else ErrorRed.copy(alpha = 0.3f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Left: Action icons (Delete, Toggle state)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            // Delete user button
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteUser(user.id)
                                                    Toast.makeText(context, "کاربر ${user.username} حذف شد", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                            }

                                            // Toggle active status
                                            Switch(
                                                checked = user.isActive,
                                                onCheckedChange = {
                                                    viewModel.toggleUserStatus(user.id)
                                                    val stateLabel = if (!user.isActive) "فعال" else "غیرفعال"
                                                    Toast.makeText(context, "وضعیت کاربر به $stateLabel تغییر یافت", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = DarkBackground,
                                                    checkedTrackColor = PrimaryTeal,
                                                    uncheckedThumbColor = TextGrey,
                                                    uncheckedTrackColor = Color(0x15FFFFFF)
                                                )
                                            )
                                        }

                                        // Right: User meta
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.padding(end = 12.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                                    if (user.role == "admin") {
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(end = 6.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(PrimaryTeal.copy(alpha = 0.2f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("مدیر", color = PrimaryTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Text(
                                                        text = user.username,
                                                        color = if (user.isActive) TextWhite else TextGrey,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(text = user.email, color = TextGrey, fontSize = 11.sp)
                                                Text(
                                                    text = "انقضا: ${user.expiresAt}",
                                                    color = if (user.isActive) SuccessGreen else ErrorRed,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(if (user.isActive) PrimaryTeal.copy(alpha = 0.1f) else Color(0x0AFFFFFF), CircleShape)
                                                    .border(1.dp, if (user.isActive) PrimaryTeal.copy(alpha = 0.3f) else Color(0x10FFFFFF), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (user.isActive) PrimaryTeal else TextGrey,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: SERVERS MANAGEMENT
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Add Server Button
                            Button(
                                onClick = { showAddServerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal, contentColor = DarkBackground),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp).testTag("add_server_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("افزودن سرور", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Title
                            Text(
                                text = "سرورهای فعال شبکه (${servers.size})",
                                color = TextGrey,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Servers list
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f).testTag("admin_servers_list")
                        ) {
                            items(servers, key = { it.id }) { server ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = GlassBackground,
                                    borderColor = GlassBorder
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Left: Delete action
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteServer(server.id)
                                                Toast.makeText(context, "سرور ${server.countryName} حذف گردید", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed)
                                        }

                                        // Right: Server details
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.padding(end = 12.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                                    if (server.isPremium) {
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(end = 6.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(PingYellow.copy(alpha = 0.2f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("VIP", color = PingYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Text(
                                                        text = "${server.countryName} - ${server.city}",
                                                        color = TextWhite,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(text = "آی‌پی: ${server.ipAddress}", color = TextGrey, fontSize = 11.sp)
                                                Text(
                                                    text = "تاخیر: ${server.pingMs} ms | بار لود: ${server.loadPercentage}٪",
                                                    color = PrimaryTeal,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0x10FFFFFF), CircleShape)
                                                    .border(1.dp, Color(0x15FFFFFF), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = server.countryFlag, fontSize = 18.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // dialogs
    if (showAddUserDialog) {
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.isNotBlank() && newUserEmail.isNotBlank()) {
                            val success = viewModel.addNewUser(newUsername, newUserEmail, newUserExpiry)
                            if (success) {
                                Toast.makeText(context, "کاربر با موفقیت اضافه شد", Toast.LENGTH_SHORT).show()
                                showAddUserDialog = false
                                newUsername = ""
                                newUserEmail = ""
                            } else {
                                Toast.makeText(context, "نام کاربری قبلاً وجود دارد", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "لطفاً تمامی فیلدها را کامل کنید", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal, contentColor = DarkBackground)
                ) {
                    Text("ثبت کاربر", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("انصراف", color = TextGrey)
                }
            },
            title = {
                Text(
                    text = "افزودن کاربر جدید",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Username input
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("نام کاربری (انگلیسی)", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email input
                    OutlinedTextField(
                        value = newUserEmail,
                        onValueChange = { newUserEmail = it },
                        label = { Text("آدرس ایمیل", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Expiration input
                    OutlinedTextField(
                        value = newUserExpiry,
                        onValueChange = { newUserExpiry = it },
                        label = { Text("تاریخ انقضا (شمسی)", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = SurfaceGrey,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddServerDialog) {
        AlertDialog(
            onDismissRequest = { showAddServerDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val pingNum = newServerPing.toIntOrNull() ?: 50
                        val loadNum = newServerLoad.toIntOrNull() ?: 10
                        if (newServerCountry.isNotBlank() && newServerCity.isNotBlank() && newServerIp.isNotBlank()) {
                            viewModel.addNewServer(
                                newServerCountry,
                                newServerFlag,
                                newServerCity,
                                newServerIp,
                                pingNum,
                                loadNum,
                                newServerPremium
                            )
                            Toast.makeText(context, "سرور با موفقیت ایجاد شد", Toast.LENGTH_SHORT).show()
                            showAddServerDialog = false
                            newServerCountry = ""
                            newServerCity = ""
                            newServerIp = ""
                        } else {
                            Toast.makeText(context, "لطفاً فیلدهای ضروری را پر کنید", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal, contentColor = DarkBackground)
                ) {
                    Text("ثبت سرور", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddServerDialog = false }) {
                    Text("انصراف", color = TextGrey)
                }
            },
            title = {
                Text(
                    text = "افزودن سرور جدید به مدار",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Country
                    OutlinedTextField(
                        value = newServerCountry,
                        onValueChange = { newServerCountry = it },
                        label = { Text("نام کشور (فارسی)", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // City
                    OutlinedTextField(
                        value = newServerCity,
                        onValueChange = { newServerCity = it },
                        label = { Text("نام شهر / توضیحات سرور", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Flag
                    OutlinedTextField(
                        value = newServerFlag,
                        onValueChange = { newServerFlag = it },
                        label = { Text("ایموجی پرچم (Flag Emoji)", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // IP
                    OutlinedTextField(
                        value = newServerIp,
                        onValueChange = { newServerIp = it },
                        label = { Text("آدرس آی‌پی (IP Address)", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Ping
                    OutlinedTextField(
                        value = newServerPing,
                        onValueChange = { newServerPing = it },
                        label = { Text("زمان تأخیر (ms)", color = TextGrey) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x15FFFFFF),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Premium Choice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = newServerPremium,
                            onCheckedChange = { newServerPremium = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = PrimaryTeal,
                                uncheckedThumbColor = TextGrey,
                                uncheckedTrackColor = Color(0x15FFFFFF)
                            )
                        )
                        Text("سرور ویژه VIP", color = TextWhite, fontSize = 14.sp)
                    }
                }
            },
            containerColor = SurfaceGrey,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AdminTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PrimaryTeal else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) DarkBackground else TextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    indicatorColor: Color = PrimaryTeal,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(100.dp),
        backgroundColor = GlassBackground,
        borderColor = GlassBorder,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = indicatorColor, modifier = Modifier.size(20.dp))
                Text(text = title, color = TextGrey, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Text(text = value, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun MonitorRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = TextGrey, fontSize = 13.sp)
    }
}
