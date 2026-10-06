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
import com.example.model.ActivityLog
import com.example.model.LogLevel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HotWifiViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityLogsScreen(
    viewModel: HotWifiViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.activityLogs.collectAsState()
    val context = LocalContext.current
    var selectedLevel by remember { mutableStateOf<LogLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, selectedLevel, searchQuery) {
        logs.filter { log ->
            val matchLevel = selectedLevel == null || log.level == selectedLevel
            val matchQuery = log.message.contains(searchQuery, ignoreCase = true) ||
                    log.tag.contains(searchQuery, ignoreCase = true) ||
                    (log.details?.contains(searchQuery, ignoreCase = true) == true)
            matchLevel && matchQuery
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss - yyyy/MM/dd", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search and Clear Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("بحث في سجل النشاط...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("logs_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            IconButton(
                onClick = {
                    viewModel.clearLogs()
                    Toast.makeText(context, "تم مسح السجل", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("clear_logs_button")
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "مسح السجل", tint = DangerRed)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Level Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedLevel == null,
                onClick = { selectedLevel = null },
                label = { Text("الكل (${logs.size})") }
            )
            FilterChip(
                selected = selectedLevel == LogLevel.SUCCESS,
                onClick = { selectedLevel = LogLevel.SUCCESS },
                label = { Text("نجاح") }
            )
            FilterChip(
                selected = selectedLevel == LogLevel.WARN,
                onClick = { selectedLevel = LogLevel.WARN },
                label = { Text("تحذير") }
            )
            FilterChip(
                selected = selectedLevel == LogLevel.ALERT,
                onClick = { selectedLevel = LogLevel.ALERT },
                label = { Text("أمان/حظر") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد سجلات حالياً",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    LogCard(log = log, timeFormat = timeFormat)
                }
            }
        }
    }
}

@Composable
private fun LogCard(
    log: ActivityLog,
    timeFormat: SimpleDateFormat
) {
    val (color, icon) = when (log.level) {
        LogLevel.SUCCESS -> SuccessGreen to Icons.Default.CheckCircle
        LogLevel.ALERT -> DangerRed to Icons.Default.Warning
        LogLevel.WARN -> WarningOrange to Icons.Default.ReportProblem
        LogLevel.INFO -> CyanPrimary to Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        text = log.tag,
                        backgroundColor = color.copy(alpha = 0.12f),
                        textColor = color
                    )
                    Text(
                        text = timeFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                if (log.details != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}
