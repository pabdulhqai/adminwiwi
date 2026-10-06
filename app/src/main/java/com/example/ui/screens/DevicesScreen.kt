package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectedDevice
import com.example.model.DeviceStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HotWifiViewModel

@Composable
fun DevicesScreen(
    viewModel: HotWifiViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val context = LocalContext.current

    var selectedDeviceForSpeedLimit by remember { mutableStateOf<ConnectedDevice?>(null) }
    var filterStatus by remember { mutableStateOf<DeviceStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredDevices = remember(devices, filterStatus, searchQuery) {
        devices.filter { dev ->
            val matchesSearch = dev.hostName.contains(searchQuery, ignoreCase = true) ||
                    dev.ipAddress.contains(searchQuery, ignoreCase = true) ||
                    dev.macAddress.contains(searchQuery, ignoreCase = true)
            val matchesFilter = filterStatus == null || dev.status == filterStatus
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("بحث عن جهاز بالاسم، IP، أو عنوان MAC...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("devices_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("الكل (${devices.size})") }
            )
            FilterChip(
                selected = filterStatus == DeviceStatus.CONNECTED,
                onClick = { filterStatus = DeviceStatus.CONNECTED },
                label = { Text("متصل (${devices.count { it.status == DeviceStatus.CONNECTED }})") }
            )
            FilterChip(
                selected = filterStatus == DeviceStatus.BLOCKED,
                onClick = { filterStatus = DeviceStatus.BLOCKED },
                label = { Text("محظور (${devices.count { it.status == DeviceStatus.BLOCKED }})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DevicesOther,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد أجهزة متطابقة",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredDevices, key = { it.macAddress }) { device ->
                    DeviceDetailCard(
                        device = device,
                        onBlockToggle = {
                            if (device.status == DeviceStatus.BLOCKED) {
                                viewModel.unblockDevice(device.macAddress)
                                Toast.makeText(context, "تم إلغاء حظر ${device.hostName}", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.blockDevice(device.macAddress)
                                Toast.makeText(context, "تم حظر ${device.hostName} ومنع وصوله", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onKick = {
                            viewModel.kickDevice(device.macAddress)
                            Toast.makeText(context, "تم طرد ${device.hostName} من الشبكة", Toast.LENGTH_SHORT).show()
                        },
                        onEditSpeed = {
                            selectedDeviceForSpeedLimit = device
                        }
                    )
                }
            }
        }
    }

    selectedDeviceForSpeedLimit?.let { dev ->
        SpeedLimitDialog(
            device = dev,
            onDismiss = { selectedDeviceForSpeedLimit = null },
            onSave = { downKbps, upKbps ->
                viewModel.setDeviceSpeedLimit(dev.macAddress, downKbps, upKbps)
                selectedDeviceForSpeedLimit = null
                Toast.makeText(context, "تم تحديث سرعة ${dev.hostName}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun DeviceDetailCard(
    device: ConnectedDevice,
    onBlockToggle: () -> Unit,
    onKick: () -> Unit,
    onEditSpeed: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.macAddress}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Device Icon + Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (device.status == DeviceStatus.CONNECTED) CyanLight else DangerRed.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                device.deviceModel.contains("iPhone") -> Icons.Default.PhoneIphone
                                device.deviceModel.contains("Redmi") || device.deviceModel.contains("Xiaomi") -> Icons.Default.Smartphone
                                else -> Icons.Default.Laptop
                            },
                            contentDescription = null,
                            tint = if (device.status == DeviceStatus.CONNECTED) CyanPrimary else DangerRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = device.hostName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = device.deviceModel,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                if (device.status == DeviceStatus.BLOCKED) {
                    StatusBadge("محظور", DangerRed.copy(alpha = 0.15f), DangerRed)
                } else {
                    StatusBadge("متصل", SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tech Specs: MAC & IP
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "IP: ${device.ipAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "MAC: ${device.macAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Usage & Speed limit row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("حد السرعة (تنزيل / رفع)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${device.downloadSpeedLimitKbps / 1024}M / ${device.uploadSpeedLimitKbps / 1024}M",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text("استهلاك البيانات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val totalMb = (device.downloadedBytes + device.uploadedBytes) / (1024 * 1024)
                    Text(
                        text = "$totalMb MB",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text("القسيمة المرتبطة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = device.boundVoucherCode ?: "تجريبي مجاني",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (device.boundVoucherCode != null) CyanPrimary else AmberAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Speed limit, Kick, Block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditSpeed,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("speed_limit_btn_${device.macAddress}")
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحديد السرعة", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onKick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningOrange),
                    modifier = Modifier.testTag("kick_device_btn_${device.macAddress}")
                ) {
                    Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طرد", fontSize = 12.sp)
                }

                Button(
                    onClick = onBlockToggle,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (device.status == DeviceStatus.BLOCKED) SuccessGreen else DangerRed
                    ),
                    modifier = Modifier.testTag("block_device_btn_${device.macAddress}")
                ) {
                    Icon(
                        imageVector = if (device.status == DeviceStatus.BLOCKED) Icons.Default.LockOpen else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (device.status == DeviceStatus.BLOCKED) "إلغاء الحظر" else "حظر", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SpeedLimitDialog(
    device: ConnectedDevice,
    onDismiss: () -> Unit,
    onSave: (downKbps: Int, upKbps: Int) -> Unit
) {
    var downSpeedMbps by remember { mutableStateOf((device.downloadSpeedLimitKbps / 1024).toString()) }
    var upSpeedMbps by remember { mutableStateOf((device.uploadSpeedLimitKbps / 1024).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تحديد سرعة الجهاز (${device.hostName})", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "حدد أقصى معدل تدفق بيانات مسموح به لهذا الجهاز لمنع احتكار الشبكة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = downSpeedMbps,
                    onValueChange = { downSpeedMbps = it },
                    label = { Text("سرعة التنزيل (Download) بـ Mbps") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = upSpeedMbps,
                    onValueChange = { upSpeedMbps = it },
                    label = { Text("سرعة الرفع (Upload) بـ Mbps") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val down = (downSpeedMbps.toIntOrNull() ?: 5) * 1024
                    val up = (upSpeedMbps.toIntOrNull() ?: 2) * 1024
                    onSave(down, up)
                },
                modifier = Modifier.testTag("save_speed_limit_button")
            ) {
                Text("حفظ الإعدادات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
