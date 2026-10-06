package com.example.model

enum class VoucherStatus {
    ACTIVE,
    USED,
    EXPIRED,
    PAUSED
}

enum class DeviceStatus {
    CONNECTED,
    BLOCKED,
    DISCONNECTED
}

enum class LogLevel {
    INFO,
    WARN,
    ALERT,
    SUCCESS
}

data class Voucher(
    val id: String,
    val code: String,
    val durationHours: Int,
    val dataLimitMb: Long, // 0 means unlimited
    val price: Double,
    val maxDevices: Int = 1,
    val status: VoucherStatus = VoucherStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val usedAt: Long? = null,
    val boundDeviceMac: String? = null,
    val remainingMinutes: Int = durationHours * 60,
    val note: String = ""
)

data class ConnectedDevice(
    val macAddress: String,
    val ipAddress: String,
    val hostName: String,
    val deviceModel: String,
    val downloadSpeedLimitKbps: Int, // 0 = unlimited
    val uploadSpeedLimitKbps: Int,
    val downloadedBytes: Long,
    val uploadedBytes: Long,
    val connectedSince: Long,
    val status: DeviceStatus = DeviceStatus.CONNECTED,
    val boundVoucherCode: String? = null,
    val signalStrengthDbm: Int = -55
)

data class NetworkConfig(
    val ssid: String = "MyhotWiFi_Zone",
    val isHotspotEnabled: Boolean = true,
    val frequencyBand: String = "5 GHz / 2.4 GHz Hybrid",
    val gatewayIp: String = "192.168.43.1",
    val subnetMask: String = "255.255.255.0",
    val dnsPrimary: String = "8.8.8.8",
    val dnsSecondary: String = "1.1.1.1",
    val dhcpStartIp: String = "192.168.43.10",
    val dhcpEndIp: String = "192.168.43.250",
    val maxAllowedClients: Int = 32,
    val defaultDownLimitKbps: Int = 5120, // 5 Mbps
    val defaultUpLimitKbps: Int = 2048,   // 2 Mbps
    val captivePortalTitle: String = "أهلاً بك في شبكة MyhotWiFi",
    val captivePortalWelcomeMsg: String = "سجّل الدخول عبر كود القسيمة للاستمتاع بإنترنت سريع ومستقر",
    val sessionTimeoutMinutes: Int = 120,
    val allowGuestTrial: Boolean = true,
    val guestTrialMinutes: Int = 10,
    val supportPhone: String = "+966 50 123 4567"
)

data class ActivityLog(
    val id: String,
    val timestamp: Long,
    val tag: String,
    val message: String,
    val details: String? = null,
    val level: LogLevel = LogLevel.INFO
)

data class DashboardStats(
    val activeClientsCount: Int,
    val totalVouchersCount: Int,
    val activeVouchersCount: Int,
    val totalSalesRevenue: Double,
    val currentDownloadSpeedMbps: Float,
    val currentUploadSpeedMbps: Float,
    val totalDataConsumedMb: Long,
    val blockedDevicesCount: Int
)
