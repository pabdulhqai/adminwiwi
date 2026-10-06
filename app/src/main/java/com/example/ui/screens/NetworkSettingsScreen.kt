package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.NetworkConfig
import com.example.ui.theme.CyanPrimary
import com.example.viewmodel.HotWifiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen(
    viewModel: HotWifiViewModel,
    modifier: Modifier = Modifier
) {
    val currentConfig by viewModel.networkConfig.collectAsState()
    val context = LocalContext.current

    var ssid by remember(currentConfig) { mutableStateOf(currentConfig.ssid) }
    var gatewayIp by remember(currentConfig) { mutableStateOf(currentConfig.gatewayIp) }
    var subnetMask by remember(currentConfig) { mutableStateOf(currentConfig.subnetMask) }
    var dnsPrimary by remember(currentConfig) { mutableStateOf(currentConfig.dnsPrimary) }
    var dnsSecondary by remember(currentConfig) { mutableStateOf(currentConfig.dnsSecondary) }
    var dhcpStartIp by remember(currentConfig) { mutableStateOf(currentConfig.dhcpStartIp) }
    var dhcpEndIp by remember(currentConfig) { mutableStateOf(currentConfig.dhcpEndIp) }
    var maxClients by remember(currentConfig) { mutableStateOf(currentConfig.maxAllowedClients.toString()) }
    var defaultDownMbps by remember(currentConfig) { mutableStateOf((currentConfig.defaultDownLimitKbps / 1024).toString()) }
    var defaultUpMbps by remember(currentConfig) { mutableStateOf((currentConfig.defaultUpLimitKbps / 1024).toString()) }
    var portalTitle by remember(currentConfig) { mutableStateOf(currentConfig.captivePortalTitle) }
    var portalWelcomeMsg by remember(currentConfig) { mutableStateOf(currentConfig.captivePortalWelcomeMsg) }
    var allowGuestTrial by remember(currentConfig) { mutableStateOf(currentConfig.allowGuestTrial) }
    var guestTrialMinutes by remember(currentConfig) { mutableStateOf(currentConfig.guestTrialMinutes.toString()) }
    var supportPhone by remember(currentConfig) { mutableStateOf(currentConfig.supportPhone) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hotspot Master Toggle Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "بث نقطة الاتصال (Hotspot AP)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "جهاز Redmi Note 11 Pro يعمل كنقطة وصول رئيسية",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Switch(
                    checked = currentConfig.isHotspotEnabled,
                    onCheckedChange = { viewModel.toggleHotspot(it) },
                    modifier = Modifier.testTag("network_hotspot_toggle")
                )
            }
        }

        // Section 1: Wireless parameters
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "إعدادات البث والواي فاي (Wi-Fi Config)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                )

                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("اسم الشبكة (SSID)") },
                    leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("network_ssid_input"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = maxClients,
                        onValueChange = { maxClients = it },
                        label = { Text("أقصى عدد متصلين") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = "5 GHz / 2.4 GHz",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نطاق التردد (Band)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Section 2: IP & DHCP Gateway
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "عناوين IP وخادم DHCP المحلي",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = gatewayIp,
                        onValueChange = { gatewayIp = it },
                        label = { Text("عنوان البوابة (Gateway IP)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subnetMask,
                        onValueChange = { subnetMask = it },
                        label = { Text("قناع الشبكة (Subnet)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dhcpStartIp,
                        onValueChange = { dhcpStartIp = it },
                        label = { Text("بداية نطاق DHCP") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dhcpEndIp,
                        onValueChange = { dhcpEndIp = it },
                        label = { Text("نهاية نطاق DHCP") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dnsPrimary,
                        onValueChange = { dnsPrimary = it },
                        label = { Text("DNS الأساسي") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dnsSecondary,
                        onValueChange = { dnsSecondary = it },
                        label = { Text("DNS الثانوي") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Section 3: Default Bandwidth & Captive Portal Customization
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "تخصيص بوابة الدخول وسرعة المشتركين الافتراضية",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = defaultDownMbps,
                        onValueChange = { defaultDownMbps = it },
                        label = { Text("سرعة التنزيل الافتراضية (Mbps)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = defaultUpMbps,
                        onValueChange = { defaultUpMbps = it },
                        label = { Text("سرعة الرفع الافتراضية (Mbps)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = portalTitle,
                    onValueChange = { portalTitle = it },
                    label = { Text("عنوان صفحة الدخول (Portal Title)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = portalWelcomeMsg,
                    onValueChange = { portalWelcomeMsg = it },
                    label = { Text("رسالة الترحيب للمشتركين") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = supportPhone,
                    onValueChange = { supportPhone = it },
                    label = { Text("رقم خدمة الدعم أو شراء القسائم") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("السماح بتجربة مجانية للضيوف", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("تمكين المستخدم من تصفح محدود قبل طلب القسيمة", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Switch(
                        checked = allowGuestTrial,
                        onCheckedChange = { allowGuestTrial = it }
                    )
                }

                if (allowGuestTrial) {
                    OutlinedTextField(
                        value = guestTrialMinutes,
                        onValueChange = { guestTrialMinutes = it },
                        label = { Text("مدة التجربة المجانية (بالدقائق)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Save Action Button
        Button(
            onClick = {
                val updated = currentConfig.copy(
                    ssid = ssid.ifBlank { "MyhotWiFi_Zone" },
                    gatewayIp = gatewayIp.ifBlank { "192.168.43.1" },
                    subnetMask = subnetMask.ifBlank { "255.255.255.0" },
                    dnsPrimary = dnsPrimary.ifBlank { "8.8.8.8" },
                    dnsSecondary = dnsSecondary.ifBlank { "1.1.1.1" },
                    dhcpStartIp = dhcpStartIp.ifBlank { "192.168.43.10" },
                    dhcpEndIp = dhcpEndIp.ifBlank { "192.168.43.250" },
                    maxAllowedClients = maxClients.toIntOrNull() ?: 32,
                    defaultDownLimitKbps = (defaultDownMbps.toIntOrNull() ?: 5) * 1024,
                    defaultUpLimitKbps = (defaultUpMbps.toIntOrNull() ?: 2) * 1024,
                    captivePortalTitle = portalTitle,
                    captivePortalWelcomeMsg = portalWelcomeMsg,
                    allowGuestTrial = allowGuestTrial,
                    guestTrialMinutes = guestTrialMinutes.toIntOrNull() ?: 10,
                    supportPhone = supportPhone
                )
                viewModel.updateNetworkConfig(updated)
                Toast.makeText(context, "تم حفظ وتطبيق إعدادات الشبكة بنجاح!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_network_settings_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حفظ وتطبيق التغييرات فوراً", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
