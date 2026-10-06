package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotWifiTopAppBar(
    title: String,
    subtitle: String? = null,
    isAdminLoggedIn: Boolean,
    onAdminClick: () -> Unit,
    onPortalPreviewClick: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Redmi Note 11 Pro",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "الرجوع"
                    )
                }
            } else {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = "شعار واي فاي",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            // Portal shortcut button
            IconButton(
                onClick = onPortalPreviewClick,
                modifier = Modifier.testTag("open_captive_portal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = "معاينة بوابة المشتركين",
                    tint = AmberAccent
                )
            }

            // Admin Lock / Logout
            IconButton(
                onClick = onAdminClick,
                modifier = Modifier.testTag("admin_lock_action_button")
            ) {
                Icon(
                    imageVector = if (isAdminLoggedIn) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                    contentDescription = if (isAdminLoggedIn) "حساب المشرف" else "تسجيل دخول المشرف",
                    tint = if (isAdminLoggedIn) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun HotWifiBottomNavigation(
    currentScreen: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("main_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            NavDestinationItem(
                screen = ScreenDestination.Dashboard,
                label = "الرئيسية",
                icon = Icons.Default.Dashboard,
                tag = "nav_dashboard"
            ),
            NavDestinationItem(
                screen = ScreenDestination.Vouchers,
                label = "القسائم",
                icon = Icons.Default.ConfirmationNumber,
                tag = "nav_vouchers"
            ),
            NavDestinationItem(
                screen = ScreenDestination.Devices,
                label = "الأجهزة",
                icon = Icons.Default.Devices,
                tag = "nav_devices"
            ),
            NavDestinationItem(
                screen = ScreenDestination.NetworkSettings,
                label = "الشبكة",
                icon = Icons.Default.SettingsInputAntenna,
                tag = "nav_settings"
            ),
            NavDestinationItem(
                screen = ScreenDestination.ActivityLogs,
                label = "السجل",
                icon = Icons.Default.ReceiptLong,
                tag = "nav_logs"
            )
        )

        items.forEach { item ->
            val selected = currentScreen == item.screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                modifier = Modifier.testTag(item.tag)
            )
        }
    }
}

private data class NavDestinationItem(
    val screen: ScreenDestination,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}
