package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: VpnViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val loginError by viewModel.loginError.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    
    // Forgot Password State
    var showForgotDialog by remember { mutableStateOf(false) }

    fun validateInputs(): Boolean {
        var isValid = true
        if (username.trim().isEmpty()) {
            usernameError = "نام کاربری نمی‌تواند خالی باشد"
            isValid = false
        } else if (username.length < 3) {
            usernameError = "نام کاربری باید حداقل ۳ کاراکتر باشد"
            isValid = false
        } else {
            usernameError = null
        }

        if (password.trim().isEmpty()) {
            passwordError = "رمز عبور نمی‌تواند خالی باشد"
            isValid = false
        } else if (password.length < 4) {
            passwordError = "رمز عبور باید حداقل ۴ کاراکتر باشد"
            isValid = false
        } else {
            passwordError = null
        }
        return isValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Futuristic Glowing Background Accents
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryTeal.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryTeal.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        // Main Login Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo and Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryTeal, PrimaryTeal.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(2.dp, PrimaryTeal, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "قفل",
                    tint = DarkBackground,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Branded Header
            Text(
                text = "IFIXMOBILE VPN",
                color = PrimaryTeal,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "سریع. ایمن. بدون محدودیت.",
                color = TextGrey,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic Login Form
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_form"),
                backgroundColor = GlassBackground,
                borderColor = GlassBorder,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End // RTL Align
                ) {
                    Text(
                        text = "ورود به حساب کاربری",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username Input Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (usernameError != null) usernameError = null
                        },
                        label = { Text("نام کاربری", color = TextGrey) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryTeal
                            )
                        },
                        isError = usernameError != null,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = PrimaryTeal,
                            unfocusedLabelColor = TextGrey,
                            cursorColor = PrimaryTeal,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    if (usernameError != null) {
                        Text(
                            text = usernameError ?: "",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
                            textAlign = TextAlign.Right
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        label = { Text("رمز عبور", color = TextGrey) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PrimaryTeal
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError != null,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedLabelColor = PrimaryTeal,
                            unfocusedLabelColor = TextGrey,
                            cursorColor = PrimaryTeal,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (passwordError != null) {
                        Text(
                            text = passwordError ?: "",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, end = 8.dp),
                            textAlign = TextAlign.Right
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Remember Me and Forgot Password Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Forgot Password Link
                        Text(
                            text = "فراموشی رمز عبور؟",
                            color = PrimaryTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { showForgotDialog = true }
                                .testTag("forgot_password_btn"),
                            textAlign = TextAlign.Left
                        )

                        // Remember Me Checkbox (RTL oriented)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "مرا به خاطر بسپار",
                                color = TextGrey,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryTeal,
                                    uncheckedColor = TextGrey,
                                    checkmarkColor = DarkBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dynamic error bar from viewmodel
                    AnimatedVisibility(
                        visible = loginError != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(ErrorRed.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, ErrorRed, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = loginError ?: "",
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "خطا",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Login Button with Loading Spinner
                    Button(
                        onClick = {
                            if (validateInputs() && !isAuthenticating) {
                                viewModel.handleLogin(username, password, rememberMe, onLoginSuccess)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            contentColor = DarkBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isAuthenticating
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                color = DarkBackground,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "ورود به حساب کاربری",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo hints
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                backgroundColor = GlassBackground.copy(alpha = 0.05f),
                borderColor = Color(0x0AFFFFFF)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "راهنمای ورود دمو سیستم:",
                        color = TextGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ورود مدیریت: admin / admin\nورود کاربر: taher / 123456",
                        color = PrimaryTeal.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }

    // Forgot Password Alert Dialog
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("فهمیدم", color = PrimaryTeal, fontSize = 14.sp)
                }
            },
            title = {
                Text(
                    text = "بازیابی رمز عبور",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "جهت بازیابی رمز عبور، لطفاً با پشتیبانی تلگرام IFIXMOBILE VPN یا نماینده فروش خود تماس حاصل فرمایید.",
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
