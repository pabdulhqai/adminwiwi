package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.Voucher
import com.example.model.VoucherStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HotWifiViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersScreen(
    viewModel: HotWifiViewModel,
    modifier: Modifier = Modifier
) {
    val vouchers by viewModel.vouchers.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showBatchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<VoucherStatus?>(null) }

    val filteredVouchers = remember(vouchers, searchQuery, selectedFilter) {
        vouchers.filter { voucher ->
            val matchesQuery = voucher.code.contains(searchQuery, ignoreCase = true) ||
                    voucher.note.contains(searchQuery, ignoreCase = true)
            val matchesFilter = selectedFilter == null || voucher.status == selectedFilter
            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { showBatchDialog = true },
                    containerColor = AmberAccent,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("batch_generate_vouchers_fab")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = "توليد دفعة سريعة")
                }
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = CyanPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_voucher_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إنشاء قسيمة جديدة")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search and Filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("بحث عن كود قسيمة أو ملاحظة...") },
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
                    .testTag("vouchers_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("الكل (${vouchers.size})") }
                )
                FilterChip(
                    selected = selectedFilter == VoucherStatus.ACTIVE,
                    onClick = { selectedFilter = VoucherStatus.ACTIVE },
                    label = { Text("متاحة") }
                )
                FilterChip(
                    selected = selectedFilter == VoucherStatus.USED,
                    onClick = { selectedFilter = VoucherStatus.USED },
                    label = { Text("مستخدمة") }
                )
                FilterChip(
                    selected = selectedFilter == VoucherStatus.EXPIRED,
                    onClick = { selectedFilter = VoucherStatus.EXPIRED },
                    label = { Text("منتهية") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredVouchers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد قسائم تطابق المعايير",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredVouchers, key = { it.id }) { voucher ->
                        VoucherItemCard(
                            voucher = voucher,
                            onCopyCode = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Voucher Code", voucher.code)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الكود: ${voucher.code}", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                viewModel.deleteVoucher(voucher.id)
                                Toast.makeText(context, "تم حذف القسيمة بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onToggleStatus = {
                                val nextStatus = when (voucher.status) {
                                    VoucherStatus.ACTIVE -> VoucherStatus.PAUSED
                                    VoucherStatus.PAUSED -> VoucherStatus.ACTIVE
                                    else -> voucher.status
                                }
                                viewModel.updateVoucherStatus(voucher.id, nextStatus)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateVoucherDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { code, duration, dataLimit, price, maxDev, note ->
                viewModel.createVoucher(code, duration, dataLimit, price, maxDev, note)
                showCreateDialog = false
                Toast.makeText(context, "تمت إضافة القسيمة بنجاح!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showBatchDialog) {
        BatchGenerateDialog(
            onDismiss = { showBatchDialog = false },
            onGenerate = { count, duration, dataLimit, price ->
                viewModel.batchGenerateVouchers(count, duration, dataLimit, price)
                showBatchDialog = false
                Toast.makeText(context, "تم توليد $count قسيمة بنجاح!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun VoucherItemCard(
    voucher: Voucher,
    onCopyCode: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voucher_card_${voucher.code}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanLight)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .clickable { onCopyCode() }
                    ) {
                        Text(
                            text = voucher.code,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onCopyCode, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الكود", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                when (voucher.status) {
                    VoucherStatus.ACTIVE -> StatusBadge("متاحة", SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                    VoucherStatus.USED -> StatusBadge("مستخدمة", AmberAccent.copy(alpha = 0.15f), AmberAccent)
                    VoucherStatus.EXPIRED -> StatusBadge("منتهية", DangerRed.copy(alpha = 0.15f), DangerRed)
                    VoucherStatus.PAUSED -> StatusBadge("معلقة", Color.Gray.copy(alpha = 0.2f), Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("المدة الزمنية", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (voucher.durationHours == 0) "${voucher.remainingMinutes} دقيقة" else "${voucher.durationHours} ساعة",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text("حد البيانات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (voucher.dataLimitMb == 0L) "غير محدود ∞" else "${voucher.dataLimitMb} MB",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text("السعر", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${voucher.price} ر.س",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AmberAccent)
                    )
                }
            }

            if (voucher.note.isNotBlank() || voucher.boundDeviceMac != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (voucher.note.isNotBlank()) {
                        Text(
                            text = "ملاحظة: ${voucher.note}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                    if (voucher.boundDeviceMac != null) {
                        Text(
                            text = "الجهاز: ${voucher.boundDeviceMac}",
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanPrimary, fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Card Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (voucher.status == VoucherStatus.ACTIVE || voucher.status == VoucherStatus.PAUSED) {
                    TextButton(onClick = onToggleStatus) {
                        Text(if (voucher.status == VoucherStatus.ACTIVE) "تعليق" else "تفعيل")
                    }
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف")
                }
            }
        }
    }
}

@Composable
private fun CreateVoucherDialog(
    onDismiss: () -> Unit,
    onCreate: (code: String, duration: Int, dataLimit: Long, price: Double, maxDevices: Int, note: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("2") }
    var dataLimitMb by remember { mutableStateOf("1024") }
    var price by remember { mutableStateOf("3.0") }
    var maxDevices by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء قسيمة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("كود القسيمة (اتركه فارغاً للتوليد العشوائي)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationHours,
                        onValueChange = { durationHours = it },
                        label = { Text("المدة (بالساعات)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("السعر (ر.س)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dataLimitMb,
                        onValueChange = { dataLimitMb = it },
                        label = { Text("البيانات (MB) [0=مفتوح]") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxDevices,
                        onValueChange = { maxDevices = it },
                        label = { Text("أقصى أجهزة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة (مثال: قسيمة عميل خاص)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationHours.toIntOrNull() ?: 2
                    val limit = dataLimitMb.toLongOrNull() ?: 1024L
                    val cost = price.toDoubleOrNull() ?: 3.0
                    val maxDev = maxDevices.toIntOrNull() ?: 1
                    onCreate(code, duration, limit, cost, maxDev, note)
                },
                modifier = Modifier.testTag("confirm_create_voucher_button")
            ) {
                Text("إصدار القسيمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun BatchGenerateDialog(
    onDismiss: () -> Unit,
    onGenerate: (count: Int, duration: Int, dataLimit: Long, price: Double) -> Unit
) {
    var count by remember { mutableStateOf("10") }
    var durationHours by remember { mutableStateOf("24") }
    var dataLimitMb by remember { mutableStateOf("5120") }
    var price by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("توليد دفعة قسائم بالجملة ⚡", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "سيتم إنشاء قسائم بأكواد عشوائية فريدة جاهزة للطباعة أو التوزيع للمشتركين.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it },
                    label = { Text("عدد القسائم المطلوبة (مثال: 10 أو 50)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationHours,
                        onValueChange = { durationHours = it },
                        label = { Text("المدة (ساعة)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("سعر كل قسيمة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = dataLimitMb,
                    onValueChange = { dataLimitMb = it },
                    label = { Text("حد البيانات بالميجابايت (0 = مفتوح)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val batchCount = (count.toIntOrNull() ?: 10).coerceIn(1, 100)
                    val duration = durationHours.toIntOrNull() ?: 24
                    val limit = dataLimitMb.toLongOrNull() ?: 5120L
                    val cost = price.toDoubleOrNull() ?: 10.0
                    onGenerate(batchCount, duration, limit, cost)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_batch_generate_button")
            ) {
                Text("توليد الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
