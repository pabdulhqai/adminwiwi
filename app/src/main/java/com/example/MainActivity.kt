package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HotWifiBottomNavigation
import com.example.ui.components.HotWifiTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.HotWifiViewModel
import com.example.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HotWifiApp()
            }
        }
    }
}

@Composable
fun HotWifiApp(viewModel: HotWifiViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    // Handle Android system back button
    BackHandler(enabled = currentScreen != ScreenDestination.Dashboard) {
        if (!viewModel.handleBack()) {
            viewModel.navigateTo(ScreenDestination.Dashboard)
        }
    }

    val title = when (currentScreen) {
        ScreenDestination.Dashboard -> "MyhotWiFi"
        ScreenDestination.Vouchers -> "إدارة القسائم"
        ScreenDestination.Devices -> "إدارة الأجهزة"
        ScreenDestination.NetworkSettings -> "إعدادات الشبكة"
        ScreenDestination.CaptivePortal -> "بوابة المشتركين"
        ScreenDestination.ActivityLogs -> "سجل النشاط"
        ScreenDestination.AdminLogin -> "تسجيل دخول المشرف"
    }

    val subtitle = when (currentScreen) {
        ScreenDestination.Dashboard -> "لوحة التحكم الرئيسية"
        ScreenDestination.Vouchers -> "توليد، تتبع، وتوزيع البطاقات"
        ScreenDestination.Devices -> "حظر، تحديد سرعة، ومراقبة الاستهلاك"
        ScreenDestination.NetworkSettings -> "تكوين نقطة الوصول وبوابة الدخول"
        ScreenDestination.CaptivePortal -> "معاينة صفحة تسجيل دخول المستخدمين"
        ScreenDestination.ActivityLogs -> "سجلات الأمان والتسجيلات المباشرة"
        ScreenDestination.AdminLogin -> "الوصول الإداري"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HotWifiTopAppBar(
                title = title,
                subtitle = subtitle,
                isAdminLoggedIn = isAdminLoggedIn,
                onAdminClick = {
                    viewModel.navigateTo(ScreenDestination.AdminLogin)
                },
                onPortalPreviewClick = {
                    viewModel.navigateTo(ScreenDestination.CaptivePortal)
                },
                onBackClick = if (currentScreen != ScreenDestination.Dashboard) {
                    {
                        if (!viewModel.handleBack()) {
                            viewModel.navigateTo(ScreenDestination.Dashboard)
                        }
                    }
                } else null
            )
        },
        bottomBar = {
            if (currentScreen != ScreenDestination.AdminLogin) {
                HotWifiBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { dest ->
                        viewModel.navigateTo(dest)
                    }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                ScreenDestination.Dashboard -> DashboardScreen(viewModel = viewModel)
                ScreenDestination.Vouchers -> VouchersScreen(viewModel = viewModel)
                ScreenDestination.Devices -> DevicesScreen(viewModel = viewModel)
                ScreenDestination.NetworkSettings -> NetworkSettingsScreen(viewModel = viewModel)
                ScreenDestination.CaptivePortal -> CaptivePortalScreen(viewModel = viewModel)
                ScreenDestination.ActivityLogs -> ActivityLogsScreen(viewModel = viewModel)
                ScreenDestination.AdminLogin -> AdminLoginScreen(viewModel = viewModel)
            }
        }
    }
}
