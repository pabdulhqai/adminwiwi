package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Voucher
import com.example.model.VoucherStatus
import com.example.repository.MyHotWifiRepository
import com.example.ui.theme.*
import com.example.viewmodel.HotWifiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptivePortalScreen(
    viewModel: HotWifiViewModel,
    modifier: Modifier = Modifier
) {
    val netConfig by viewModel.networkConfig.collectAsState()
    val availableVouchers by viewModel.vouchers.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var voucherInput by remember { mutableStateOf("") }
    var hostNameInput by remember { mutableStateOf("Redmi-Pro-Guest") }
    var deviceMacInput by remember { mutableStateOf("E4:5F:01:2B:9C:12") }
    var activeVoucherSession by remember { mutableStateOf<Voucher?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode indicator badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AmberAccent.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "📱 شاشة محاكاة بوابة تسجيل دخول المشتركين (Captive Portal)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AmberAccent
                )
            )
        }

        // Branding Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = netConfig.captivePortalTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = netConfig.captivePortalWelcomeMsg,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الشبكة الحالية: ${netConfig.ssid}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        // Active Session Status Banner if logged in
        AnimatedVisibility(visible = activeVoucherSession != null) {
            activeVoucherSession?.let { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أنت متصل بالإنترنت الآن بنجاح!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                            )
                        }
                        Text(
                            text = "القسيمة المفعلة: ${session.code} • المتبقي: ${session.remainingMinutes} دقيقة",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "البيانات المتاحة: ${if (session.dataLimitMb == 0L) "غير محدود" else "${session.dataLimitMb} MB"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        OutlinedButton(
                            onClick = { activeVoucherSession = null },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                        ) {
                            Text("تسجيل خروج الجلسة")
                        }
                    }
                }
            }
        }

        // Login Card with Voucher Code Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "تسجيل الدخول برمز القسيمة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = voucherInput,
                    onValueChange = {
                        voucherInput = it.uppercase()
                        authError = null
                    },
                    label = { Text("أدخل رمز القسيمة (مثال: HW-7821)") },
                    leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = CyanPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("portal_voucher_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Quick tap suggestions from local database for easy testing
                Text(
                    text = "قسائم متاحة من قاعدة البيانات (Room):",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeSamples = availableVouchers.filter { it.status == VoucherStatus.ACTIVE }.take(3)
                    activeSamples.forEach { v ->
                        AssistChip(
                            onClick = { voucherInput = v.code },
                            label = { Text(v.code, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                if (authError != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authError ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = DangerRed)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (voucherInput.isBlank()) {
                            authError = "يرجى كتابة رمز القسيمة أولاً"
                            return@Button
                        }
                        isAuthenticating = true
                        coroutineScope.launch {
                            val result = viewModel.authenticateUserWithVoucher(voucherInput, deviceMacInput, hostNameInput)
                            isAuthenticating = false
                            when (result) {
                                is MyHotWifiRepository.PortalAuthResult.Success -> {
                                    activeVoucherSession = result.voucher
                                    authError = null
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                                is MyHotWifiRepository.PortalAuthResult.Error -> {
                                    authError = result.message
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("portal_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("دخول الإنترنت الآن", fontWeight = FontWeight.Bold)
                    }
                }

                if (netConfig.allowGuestTrial) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val result = viewModel.startGuestTrial(deviceMacInput, hostNameInput)
                                when (result) {
                                    is MyHotWifiRepository.PortalAuthResult.Success -> {
                                        activeVoucherSession = result.voucher
                                        authError = null
                                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    }
                                    is MyHotWifiRepository.PortalAuthResult.Error -> {
                                        authError = result.message
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("portal_guest_trial_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = AmberAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تجربة مجانية لمدة ${netConfig.guestTrialMinutes} دقائق")
                    }
                }
            }
        }

        // Support Contact Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("بحاجة لشراء كرت قسيمة أو مساعدة؟", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text("تواصل مع مسؤول الشبكة: ${netConfig.supportPhone}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }
    }
}
