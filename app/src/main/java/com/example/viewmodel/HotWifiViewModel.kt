package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.model.*
import com.example.repository.MyHotWifiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Dashboard : ScreenDestination()
    object Vouchers : ScreenDestination()
    object Devices : ScreenDestination()
    object NetworkSettings : ScreenDestination()
    object CaptivePortal : ScreenDestination()
    object ActivityLogs : ScreenDestination()
    object AdminLogin : ScreenDestination()
}

class HotWifiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MyHotWifiRepository(
        voucherDao = database.voucherDao(),
        activityLogDao = database.activityLogDao()
    )

    val networkConfig: StateFlow<NetworkConfig> = repository.networkConfig
    val vouchers: StateFlow<List<Voucher>> = repository.vouchers
    val devices: StateFlow<List<ConnectedDevice>> = repository.devices
    val activityLogs: StateFlow<List<ActivityLog>> = repository.activityLogs
    val isAdminLoggedIn: StateFlow<Boolean> = repository.isAdminLoggedIn

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Dashboard)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<ScreenDestination>()

    // Simulated real-time live traffic meters
    private val _liveDownloadSpeedMbps = MutableStateFlow(18.4f)
    val liveDownloadSpeedMbps: StateFlow<Float> = _liveDownloadSpeedMbps.asStateFlow()

    private val _liveUploadSpeedMbps = MutableStateFlow(6.8f)
    val liveUploadSpeedMbps: StateFlow<Float> = _liveUploadSpeedMbps.asStateFlow()

    // Derived Dashboard Stats
    val dashboardStats: StateFlow<DashboardStats> = combine(
        vouchers,
        devices,
        liveDownloadSpeedMbps,
        liveUploadSpeedMbps
    ) { voucherList, deviceList, downSpeed, upSpeed ->
        val activeClients = deviceList.count { it.status == DeviceStatus.CONNECTED }
        val blockedClients = deviceList.count { it.status == DeviceStatus.BLOCKED }
        val activeVouchers = voucherList.count { it.status == VoucherStatus.ACTIVE }
        val totalRevenue = voucherList.filter { it.status == VoucherStatus.USED }.sumOf { it.price }
        val totalBytes = deviceList.sumOf { it.downloadedBytes + it.uploadedBytes }
        val totalMb = totalBytes / (1024 * 1024)

        DashboardStats(
            activeClientsCount = activeClients,
            totalVouchersCount = voucherList.size,
            activeVouchersCount = activeVouchers,
            totalSalesRevenue = totalRevenue,
            currentDownloadSpeedMbps = downSpeed,
            currentUploadSpeedMbps = upSpeed,
            totalDataConsumedMb = totalMb,
            blockedDevicesCount = blockedClients
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats(0, 0, 0, 0.0, 0f, 0f, 0, 0)
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(2500)
                val connectedCount = devices.value.count { it.status == DeviceStatus.CONNECTED }
                if (connectedCount > 0 && networkConfig.value.isHotspotEnabled) {
                    val baseDown = (connectedCount * 4.5f) + (Math.random().toFloat() * 3.5f)
                    val baseUp = (connectedCount * 1.5f) + (Math.random().toFloat() * 1.8f)
                    _liveDownloadSpeedMbps.value = (baseDown * 10).toInt() / 10f
                    _liveUploadSpeedMbps.value = (baseUp * 10).toInt() / 10f
                } else {
                    _liveDownloadSpeedMbps.value = 0f
                    _liveUploadSpeedMbps.value = 0f
                }
            }
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value != destination) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = destination
        }
    }

    fun handleBack(): Boolean {
        return if (_screenHistory.isNotEmpty()) {
            val previous = _screenHistory.removeAt(_screenHistory.lastIndex)
            _currentScreen.value = previous
            true
        } else {
            false
        }
    }

    // Admin Auth
    fun loginAdmin(password: String): Boolean {
        val success = repository.loginAdmin(password)
        if (success) {
            _currentScreen.value = ScreenDestination.Dashboard
        }
        return success
    }

    fun logoutAdmin() {
        repository.logoutAdmin()
        _currentScreen.value = ScreenDestination.AdminLogin
    }

    // Vouchers (Persisted in Room)
    fun createVoucher(code: String, durationHours: Int, dataLimitMb: Long, price: Double, maxDevices: Int, note: String) {
        repository.createVoucher(code, durationHours, dataLimitMb, price, maxDevices, note)
    }

    fun batchGenerateVouchers(count: Int, durationHours: Int, dataLimitMb: Long, price: Double) {
        repository.batchGenerateVouchers(count, durationHours, dataLimitMb, price)
    }

    fun deleteVoucher(voucherId: String) {
        repository.deleteVoucher(voucherId)
    }

    fun updateVoucherStatus(voucherId: String, status: VoucherStatus) {
        repository.updateVoucherStatus(voucherId, status)
    }

    // Devices
    fun blockDevice(macAddress: String) {
        repository.blockDevice(macAddress)
    }

    fun unblockDevice(macAddress: String) {
        repository.unblockDevice(macAddress)
    }

    fun kickDevice(macAddress: String) {
        repository.kickDevice(macAddress)
    }

    fun setDeviceSpeedLimit(macAddress: String, downKbps: Int, upKbps: Int) {
        repository.setDeviceSpeedLimit(macAddress, downKbps, upKbps)
    }

    // Network Config
    fun updateNetworkConfig(config: NetworkConfig) {
        repository.updateNetworkConfig(config)
    }

    fun toggleHotspot(enabled: Boolean) {
        repository.toggleHotspot(enabled)
    }

    // Captive Portal
    suspend fun authenticateUserWithVoucher(voucherCode: String, deviceMac: String, hostName: String): MyHotWifiRepository.PortalAuthResult {
        return repository.authenticateUserWithVoucher(voucherCode, deviceMac, hostName)
    }

    suspend fun startGuestTrial(deviceMac: String, hostName: String): MyHotWifiRepository.PortalAuthResult {
        return repository.startGuestTrial(deviceMac, hostName)
    }

    // Logs (Persisted in Room)
    fun clearLogs() {
        repository.clearLogs()
    }
}
