package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HotWifiViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun DashboardScreen(
    viewModel: HotWifiViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val netConfig by viewModel.networkConfig.collectAsState()
    val connectedDevices by viewModel.devices.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hotspot Status Master Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hotspot_master_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (netConfig.isHotspotEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (netConfig.isHotspotEnabled) CyanPrimary else Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (netConfig.isHotspotEnabled) Icons.Default.WifiTethering else Icons.Default.WifiTetheringOff,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = netConfig.ssid,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (netConfig.isHotspotEnabled) "البث نشط • جاهز لاستقبال المشتركين" else "البث متوقف حالياً",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (netConfig.isHotspotEnabled) SuccessGreen else DangerRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = netConfig.isHotspotEnabled,
                            onCheckedChange = { viewModel.toggleHotspot(it) },
                            modifier = Modifier.testTag("hotspot_master_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickInfoTag(title = "البوابة (IP)", value = netConfig.gatewayIp)
                        QuickInfoTag(title = "نطاق البث", value = "2.4GHz / 5GHz")
                        QuickInfoTag(title = "المشتركين المسموحين", value = "${stats.activeClientsCount}/${netConfig.maxAllowedClients}")
                    }
                }
            }
        }

        // Live Speed Gauge Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedCard(
                    title = "سرعة التنزيل الحالية",
                    speed = "${stats.currentDownloadSpeedMbps} Mbps",
                    icon = Icons.Default.ArrowDownward,
                    color = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                SpeedCard(
                    title = "سرعة الرفع الحالية",
                    speed = "${stats.currentUploadSpeedMbps} Mbps",
                    icon = Icons.Default.ArrowUpward,
                    color = SlateSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Statistics Grid (4 Key Metrics)
        item {
            Text(
                text = "نظرة عامة على النشاط والأرباح",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "الأجهزة المتصلة",
                        value = "${stats.activeClientsCount} جهاز",
                        subtitle = "${stats.blockedDevicesCount} محظور",
                        icon = Icons.Default.Devices,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "القسائم المتاحة",
                        value = "${stats.activeVouchersCount} قسيمة",
                        subtitle = "من إجمالي ${stats.totalVouchersCount}",
                        icon = Icons.Default.ConfirmationNumber,
                        iconTint = AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "إجمالي المبيعات",
                        value = "${stats.totalSalesRevenue} ر.س",
                        subtitle = "عوائد القسائم المفعلة",
                        icon = Icons.Default.AttachMoney,
                        iconTint = PurpleIndicator,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "استهلاك البيانات",
                        value = "${stats.totalDataConsumedMb} MB",
                        subtitle = "حجم الترافيك المتبادل",
                        icon = Icons.Default.DataUsage,
                        iconTint = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إجراءات سريعة للمشرف",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(ScreenDestination.Vouchers) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_action_add_voucher"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "قسيمة جديدة", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.CaptivePortal) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_action_view_portal"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "بوابة الدخول", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active devices preview list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأجهزة المتصلة حالياً",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.Devices) },
                    modifier = Modifier.testTag("view_all_devices_button")
                ) {
                    Text("عرض الكل (${connectedDevices.size})")
                }
            }

            if (connectedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد أجهزة متصلة في الوقت الراهن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    connectedDevices.take(3).forEach { device ->
                        DeviceSummaryRow(
                            device = device,
                            onBlockToggle = {
                                if (device.status == DeviceStatus.BLOCKED) {
                                    viewModel.unblockDevice(device.macAddress)
                                } else {
                                    viewModel.blockDevice(device.macAddress)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickInfoTag(title: String, value: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun SpeedCard(
    title: String,
    speed: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = speed,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun DeviceSummaryRow(
    device: com.example.model.ConnectedDevice,
    onBlockToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        device.deviceModel.contains("iPhone") -> Icons.Default.PhoneIphone
                        device.deviceModel.contains("Redmi") || device.deviceModel.contains("Xiaomi") -> Icons.Default.Smartphone
                        else -> Icons.Default.Laptop
                    },
                    contentDescription = null,
                    tint = if (device.status == DeviceStatus.CONNECTED) CyanPrimary else DangerRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = device.hostName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${device.ipAddress} • ${device.macAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (device.status == DeviceStatus.BLOCKED) {
                    StatusBadge(text = "محظور", backgroundColor = DangerRed.copy(alpha = 0.15f), textColor = DangerRed)
                } else {
                    StatusBadge(text = "متصل", backgroundColor = SuccessGreen.copy(alpha = 0.15f), textColor = SuccessGreen)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onBlockToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (device.status == DeviceStatus.BLOCKED) Icons.Default.LockOpen else Icons.Default.Block,
                        contentDescription = "تبديل الحظر",
                        tint = if (device.status == DeviceStatus.BLOCKED) SuccessGreen else DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
