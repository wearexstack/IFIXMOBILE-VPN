package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    onBack: () -> Unit
) {
    val isAutoConnect by viewModel.isAutoConnect.collectAsState()
    val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("فارسی (Persian)") }

    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "تنظیمات برنامه",
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
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Connection Preferences Section
            Text(
                text = "تنظیمات اتصال",
                color = PrimaryTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground,
                borderColor = GlassBorder,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Auto-Connect Switch item
                    SettingsSwitchRow(
                        title = "اتصال خودکار",
                        subtitle = "برقراری ارتباط به محض باز شدن برنامه",
                        icon = Icons.Default.PlayArrow,
                        checked = isAutoConnect,
                        onCheckedChange = { viewModel.toggleAutoConnect() }
                    )

                    HorizontalDivider(color = Color(0x0AFFFFFF), thickness = 1.dp)

                    // Notifications Switch item
                    SettingsSwitchRow(
                        title = "اطلاعیه‌های اتصال",
                        subtitle = "نمایش وضعیت وی‌پی‌ان در نوار اعلان سیستم",
                        icon = Icons.Default.Notifications,
                        checked = isNotificationEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Appearance & Language Section
            Text(
                text = "ظاهر و زبان",
                color = PrimaryTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground,
                borderColor = GlassBorder,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Theme Choice item
                    SettingsSwitchRow(
                        title = "تم تیره",
                        subtitle = "بهینه‌سازی باتری و محافظت از چشمان شما",
                        icon = Icons.Default.Star,
                        checked = isDarkTheme,
                        onCheckedChange = {
                            viewModel.toggleTheme()
                            Toast.makeText(context, "تم تغییر کرد (قفل روی حالت تیره ممتاز)", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = Color(0x0AFFFFFF), thickness = 1.dp)

                    // Language Selector Row
                    SettingsClickableRow(
                        title = "زبان برنامه (Language)",
                        value = selectedLanguage,
                        icon = Icons.Default.Info,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Info & Legal Section
            Text(
                text = "درباره برنامه و حریم خصوصی",
                color = PrimaryTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground,
                borderColor = GlassBorder,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Privacy Policy
                    SettingsClickableRow(
                        title = "قوانین حریم خصوصی",
                        value = "مشاهده جزئیات",
                        icon = Icons.Default.Info,
                        onClick = { showPrivacyDialog = true }
                    )

                    HorizontalDivider(color = Color(0x0AFFFFFF), thickness = 1.dp)

                    // App Version Display
                    SettingsInfoRow(
                        title = "نسخه برنامه",
                        value = "۱.۰.۰ (نسخه پایدار ۲۰۲۶)",
                        icon = Icons.Default.Info
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("انصراف", color = TextGrey, fontSize = 14.sp)
                }
            },
            title = {
                Text(
                    text = "انتخاب زبان برنامه",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    listOf("فارسی (Persian)", "English (انگلیسی)").forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                    Toast.makeText(context, "زبان با موفقیت تغییر کرد", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == language,
                                onClick = {
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryTeal)
                            )
                            Text(
                                text = language,
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            containerColor = SurfaceGrey,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("متوجه شدم", color = PrimaryTeal, fontSize = 14.sp)
                }
            },
            title = {
                Text(
                    text = "قوانین حریم خصوصی IFIXMOBILE VPN",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "ما در IFIXMOBILE اهمیت بالایی برای امنیت و حریم خصوصی اطلاعات شما قائل هستیم.\n\n" +
                            "۱. هیچ‌گونه گزارش فعالیت اینترنتی (Logs) شما ذخیره نمی‌گردد.\n" +
                            "۲. ترافیک شما با پروتکل‌های نظامی پیشرفته (AES-256) رمزگذاری کامل می‌شود.\n" +
                            "۳. اطلاعات حساب کاربری و انقضا فقط جهت اعتبارسنجی در بانک اطلاعاتی ما نگهداری می‌شود.\n\n" +
                            "اتصال امن شما هدف ماست.",
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

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle Switch
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = PrimaryTeal,
                uncheckedThumbColor = TextGrey,
                uncheckedTrackColor = Color(0x15FFFFFF)
            )
        )

        // Title and description
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
                    text = title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = subtitle,
                    color = TextGrey,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    lineHeight = 14.sp
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = null,
                tint = TextGrey,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                color = PrimaryTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(end = 12.dp)
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsInfoRow(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            color = TextGrey,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(end = 12.dp)
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
