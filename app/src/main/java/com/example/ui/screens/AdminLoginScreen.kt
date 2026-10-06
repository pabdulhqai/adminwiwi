package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.HotWifiViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun AdminLoginScreen(
    viewModel: HotWifiViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val context = LocalContext.current

    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isAdminLoggedIn) SuccessGreen.copy(alpha = 0.15f) else CyanLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAdminLoggedIn) Icons.Default.Shield else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isAdminLoggedIn) SuccessGreen else CyanPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = if (isAdminLoggedIn) "جلسة المشرف نشطة" else "تسجيل دخول المشرف (Admin)",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isAdminLoggedIn)
                        "أنت مسجل الدخول بصلاحيات كاملة للتحكم في نقطة الاتصال والقسائم والأجهزة."
                    else
                        "يرجى إدخال كلمة مرور المشرف للوصول إلى لوحة التحكم المتقدمة.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )

                if (isAdminLoggedIn) {
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.Dashboard) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_go_to_dashboard_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الذهاب إلى لوحة التحكم")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.logoutAdmin()
                            Toast.makeText(context, "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_logout_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الخروج")
                    }
                } else {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        label = { Text("كلمة مرور المشرف") },
                        placeholder = { Text("admin أو 1234") },
                        leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "تبديل الرؤية"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = DangerRed)
                        )
                    }

                    Button(
                        onClick = {
                            if (viewModel.loginAdmin(passwordInput)) {
                                Toast.makeText(context, "مرحباً بك! تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                            } else {
                                errorMessage = "كلمة المرور غير صحيحة. كلمة المرور الافتراضية: admin أو 1234"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_submit_login_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الدخول", fontWeight = FontWeight.Bold)
                    }

                    // Default tip
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "كلمة المرور الافتراضية للمشرف: admin أو 1234",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}
