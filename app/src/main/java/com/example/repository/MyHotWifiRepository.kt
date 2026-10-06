package com.example.repository

import com.example.data.local.ActivityLogDao
import com.example.data.local.ActivityLogEntity
import com.example.data.local.VoucherDao
import com.example.data.local.VoucherEntity
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MyHotWifiRepository(
    private val voucherDao: VoucherDao,
    private val activityLogDao: ActivityLogDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _networkConfig = MutableStateFlow(NetworkConfig())
    val networkConfig: StateFlow<NetworkConfig> = _networkConfig.asStateFlow()

    // Reactive flow of vouchers mapped from Room entities
    val vouchers: StateFlow<List<Voucher>> = voucherDao.getAllVouchers()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Reactive flow of logs mapped from Room entities
    val activityLogs: StateFlow<List<ActivityLog>> = activityLogDao.getAllLogs()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _devices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val devices: StateFlow<List<ConnectedDevice>> = _devices.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    init {
        scope.launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val voucherCount = voucherDao.getVouchersCount()
        if (voucherCount == 0) {
            val initialVouchers = listOf(
                Voucher(
                    id = UUID.randomUUID().toString(),
                    code = "HW-7821",
                    durationHours = 2,
                    dataLimitMb = 1024,
                    price = 3.0,
                    status = VoucherStatus.ACTIVE,
                    note = "قسيمة ساعتين - مقهى"
                ),
                Voucher(
                    id = UUID.randomUUID().toString(),
                    code = "HW-9943",
                    durationHours = 24,
                    dataLimitMb = 5120,
                    price = 10.0,
                    status = VoucherStatus.ACTIVE,
                    note = "قسيمة يوم كامل 5 جيجا"
                ),
                Voucher(
                    id = UUID.randomUUID().toString(),
                    code = "HW-3310",
                    durationHours = 6,
                    dataLimitMb = 2048,
                    price = 6.0,
                    status = VoucherStatus.USED,
                    boundDeviceMac = "34:E1:D4:11:8A:BC",
                    usedAt = System.currentTimeMillis() - 7200000,
                    remainingMinutes = 240,
                    note = "مستخدمة بواسطة Samsung Galaxy"
                ),
                Voucher(
                    id = UUID.randomUUID().toString(),
                    code = "VIP-8888",
                    durationHours = 72,
                    dataLimitMb = 0,
                    price = 25.0,
                    status = VoucherStatus.ACTIVE,
                    maxDevices = 2,
                    note = "قسيمة VIP مفتوحة 3 أيام"
                ),
                Voucher(
                    id = UUID.randomUUID().toString(),
                    code = "HW-1044",
                    durationHours = 1,
                    dataLimitMb = 500,
                    price = 1.5,
                    status = VoucherStatus.EXPIRED,
                    note = "منتهية الصلاحية"
                )
            )
            voucherDao.insertVouchers(initialVouchers.map { it.toEntity() })
        }

        val logsCount = activityLogDao.getLogsCount()
        val now = System.currentTimeMillis()
        if (logsCount == 0) {
            val initialLogs = listOf(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = now - 36000000,
                    tag = "الأمان",
                    message = "تم حظر الجهاز [DC:A6:32:8B:17:FE] لتجاوز الحد الأقصى للمحاولات",
                    details = "IP: 192.168.43.90 | السبب: هجوم تخمين أكواد القسائم",
                    level = LogLevel.ALERT
                ),
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = now - 9200000,
                    tag = "تسجيل دخول",
                    message = "تم تفعيل القسيمة HW-9943 بنجاح للجهاز iPhone-14-Pro",
                    details = "صلاحية 24 ساعة - حد البيانات 5 GB",
                    level = LogLevel.SUCCESS
                ),
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = now - 5400000,
                    tag = "تسجيل دخول",
                    message = "تم ربط القسيمة HW-3310 بالجهاز Galaxy-S22",
                    details = "السرعة المخصصة: 5 Mbps تنزيل / 2 Mbps رفع",
                    level = LogLevel.SUCCESS
                ),
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = now - 1800000,
                    tag = "بوابة الدخول",
                    message = "بدء جلسة تجريبية مجانية للجهاز Redmi-Note-11P",
                    details = "مدة الجلسة 10 دقائق (وضع الضيف التجريبي)",
                    level = LogLevel.INFO
                ),
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = now - 600000,
                    tag = "النظام",
                    message = "قاعدة بيانات Room تعمل محلياً على Redmi Note 11 Pro",
                    details = "تم تخزين بيانات القسائم وسجلات النشاط بنجاح",
                    level = LogLevel.INFO
                )
            )
            initialLogs.forEach { activityLogDao.insertLog(it.toEntity()) }
        }

        // Seed sample connected devices in memory
        val initialDevices = listOf(
            ConnectedDevice(
                macAddress = "34:E1:D4:11:8A:BC",
                ipAddress = "192.168.43.14",
                hostName = "Galaxy-S22",
                deviceModel = "Samsung Galaxy S22",
                downloadSpeedLimitKbps = 5120,
                uploadSpeedLimitKbps = 2048,
                downloadedBytes = 840_000_000L,
                uploadedBytes = 120_000_000L,
                connectedSince = now - 5400000,
                status = DeviceStatus.CONNECTED,
                boundVoucherCode = "HW-3310",
                signalStrengthDbm = -48
            ),
            ConnectedDevice(
                macAddress = "F4:0F:24:99:6C:52",
                ipAddress = "192.168.43.27",
                hostName = "iPhone-14-Pro",
                deviceModel = "Apple iPhone 14 Pro",
                downloadSpeedLimitKbps = 10240,
                uploadSpeedLimitKbps = 5120,
                downloadedBytes = 1_450_000_000L,
                uploadedBytes = 280_000_000L,
                connectedSince = now - 9200000,
                status = DeviceStatus.CONNECTED,
                boundVoucherCode = "HW-9943",
                signalStrengthDbm = -62
            ),
            ConnectedDevice(
                macAddress = "50:EB:71:02:44:A9",
                ipAddress = "192.168.43.41",
                hostName = "Redmi-Note-11P",
                deviceModel = "Xiaomi Redmi Note 11 Pro",
                downloadSpeedLimitKbps = 8192,
                uploadSpeedLimitKbps = 3072,
                downloadedBytes = 320_000_000L,
                uploadedBytes = 45_000_000L,
                connectedSince = now - 1800000,
                status = DeviceStatus.CONNECTED,
                boundVoucherCode = null,
                signalStrengthDbm = -39
            ),
            ConnectedDevice(
                macAddress = "DC:A6:32:8B:17:FE",
                ipAddress = "192.168.43.90",
                hostName = "Unknown-Device",
                deviceModel = "Generic Android Device",
                downloadSpeedLimitKbps = 1024,
                uploadSpeedLimitKbps = 512,
                downloadedBytes = 2_800_000_000L,
                uploadedBytes = 490_000_000L,
                connectedSince = now - 36000000,
                status = DeviceStatus.BLOCKED,
                boundVoucherCode = null,
                signalStrengthDbm = -78
            )
        )
        _devices.value = initialDevices
    }

    // Admin Auth
    fun loginAdmin(password: String): Boolean {
        if (password == "admin" || password == "admin123" || password == "1234") {
            _isAdminLoggedIn.value = true
            addLog(
                tag = "المشرف",
                message = "تسجيل دخول المشرف بنجاح إلى لوحة التحكم",
                level = LogLevel.INFO
            )
            return true
        }
        addLog(
            tag = "المشرف",
            message = "فشل محاولة تسجيل دخول المشرف (كلمة مرور خاطئة)",
            level = LogLevel.WARN
        )
        return false
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        addLog(
            tag = "المشرف",
            message = "تسجيل خروج المشرف من لوحة التحكم",
            level = LogLevel.INFO
        )
    }

    // Voucher Management (Persistent in Room)
    fun createVoucher(
        code: String,
        durationHours: Int,
        dataLimitMb: Long,
        price: Double,
        maxDevices: Int,
        note: String
    ): Voucher {
        val finalCode = if (code.isBlank()) generateRandomVoucherCode() else code.trim().uppercase()
        val newVoucher = Voucher(
            id = UUID.randomUUID().toString(),
            code = finalCode,
            durationHours = durationHours,
            dataLimitMb = dataLimitMb,
            price = price,
            maxDevices = maxDevices,
            note = note,
            status = VoucherStatus.ACTIVE,
            remainingMinutes = durationHours * 60
        )
        scope.launch {
            voucherDao.insertVoucher(newVoucher.toEntity())
        }
        addLog(
            tag = "القسائم",
            message = "إنشاء قسيمة جديدة [$finalCode] بقيمة $price ر.س / د.ع",
            details = "المدة: $durationHours ساعة | البيانات: ${if (dataLimitMb == 0L) "مفتوح" else "$dataLimitMb ميجابايت"}",
            level = LogLevel.SUCCESS
        )
        return newVoucher
    }

    fun batchGenerateVouchers(
        count: Int,
        durationHours: Int,
        dataLimitMb: Long,
        price: Double
    ) {
        val list = mutableListOf<VoucherEntity>()
        for (i in 1..count) {
            val code = generateRandomVoucherCode()
            list.add(
                VoucherEntity(
                    id = UUID.randomUUID().toString(),
                    code = code,
                    durationHours = durationHours,
                    dataLimitMb = dataLimitMb,
                    price = price,
                    status = VoucherStatus.ACTIVE.name,
                    remainingMinutes = durationHours * 60,
                    note = "توليد دفعة آلي (#$i)"
                )
            )
        }
        scope.launch {
            voucherDao.insertVouchers(list)
        }
        addLog(
            tag = "القسائم",
            message = "تم توليد $count قسيمة بنجاح وحفظها في قاعدة بيانات Room",
            level = LogLevel.SUCCESS
        )
    }

    fun deleteVoucher(voucherId: String) {
        val voucher = vouchers.value.find { it.id == voucherId }
        scope.launch {
            voucherDao.deleteVoucherById(voucherId)
        }
        addLog(
            tag = "القسائم",
            message = "تم حذف القسيمة [${voucher?.code ?: voucherId}] من قاعدة البيانات",
            level = LogLevel.WARN
        )
    }

    fun updateVoucherStatus(voucherId: String, newStatus: VoucherStatus) {
        val target = vouchers.value.find { it.id == voucherId }
        if (target != null) {
            val updated = target.copy(status = newStatus)
            scope.launch {
                voucherDao.updateVoucher(updated.toEntity())
            }
        }
    }

    // Device Management
    fun blockDevice(macAddress: String) {
        _devices.value = _devices.value.map {
            if (it.macAddress.equals(macAddress, ignoreCase = true)) {
                it.copy(status = DeviceStatus.BLOCKED)
            } else it
        }
        addLog(
            tag = "الأجهزة",
            message = "تم حظر الجهاز [$macAddress] وفصل وصوله للإنترنت فوراً",
            level = LogLevel.ALERT
        )
    }

    fun unblockDevice(macAddress: String) {
        _devices.value = _devices.value.map {
            if (it.macAddress.equals(macAddress, ignoreCase = true)) {
                it.copy(status = DeviceStatus.CONNECTED)
            } else it
        }
        addLog(
            tag = "الأجهزة",
            message = "تم إلغاء حظر الجهاز [$macAddress] وإعادة السماح بالاتصال",
            level = LogLevel.SUCCESS
        )
    }

    fun kickDevice(macAddress: String) {
        val target = _devices.value.find { it.macAddress.equals(macAddress, ignoreCase = true) }
        _devices.value = _devices.value.filterNot { it.macAddress.equals(macAddress, ignoreCase = true) }
        addLog(
            tag = "الأجهزة",
            message = "تم طرد الجهاز [${target?.hostName ?: macAddress}] وفصل جلسته الحالية",
            level = LogLevel.WARN
        )
    }

    fun setDeviceSpeedLimit(macAddress: String, downKbps: Int, upKbps: Int) {
        _devices.value = _devices.value.map {
            if (it.macAddress.equals(macAddress, ignoreCase = true)) {
                it.copy(
                    downloadSpeedLimitKbps = downKbps,
                    uploadSpeedLimitKbps = upKbps
                )
            } else it
        }
        addLog(
            tag = "السرعة",
            message = "تحديث محدد السرعة للجهاز [$macAddress]: $downKbps Kbps تنزيل / $upKbps Kbps رفع",
            level = LogLevel.INFO
        )
    }

    // Captive Portal Authentication Logic
    sealed class PortalAuthResult {
        data class Success(val voucher: Voucher, val message: String) : PortalAuthResult()
        data class Error(val message: String) : PortalAuthResult()
    }

    suspend fun authenticateUserWithVoucher(voucherCode: String, deviceMac: String, hostName: String): PortalAuthResult {
        val cleanCode = voucherCode.trim().uppercase()
        val foundEntity = voucherDao.getVoucherByCode(cleanCode)

        if (foundEntity == null) {
            addLog(
                tag = "بوابة الدخول",
                message = "محاولة تسجيل دخول فاشلة بكود غير موجود: $cleanCode",
                details = "MAC: $deviceMac | Host: $hostName",
                level = LogLevel.WARN
            )
            return PortalAuthResult.Error("كود القسيمة غير صحيح. يرجى التأكد من الرمز وإعادة المحاولة.")
        }

        val found = foundEntity.toModel()

        if (found.status == VoucherStatus.EXPIRED) {
            return PortalAuthResult.Error("هذه القسيمة منتهية الصلاحية.")
        }

        if (found.status == VoucherStatus.PAUSED) {
            return PortalAuthResult.Error("هذه القسيمة معلقة مؤقتاً بواسطة المشرف.")
        }

        if (found.status == VoucherStatus.USED && found.boundDeviceMac != null && !found.boundDeviceMac.equals(deviceMac, ignoreCase = true) && found.maxDevices <= 1) {
            return PortalAuthResult.Error("هذه القسيمة مستخدمة بالفعل على جهاز آخر [${found.boundDeviceMac}].")
        }

        // Mark as used & bind in Room
        val updatedVoucher = found.copy(
            status = VoucherStatus.USED,
            boundDeviceMac = deviceMac,
            usedAt = found.usedAt ?: System.currentTimeMillis()
        )

        voucherDao.updateVoucher(updatedVoucher.toEntity())

        // Connect device
        val existingDevice = _devices.value.find { it.macAddress.equals(deviceMac, ignoreCase = true) }
        if (existingDevice != null) {
            _devices.value = _devices.value.map {
                if (it.macAddress.equals(deviceMac, ignoreCase = true)) {
                    it.copy(
                        status = DeviceStatus.CONNECTED,
                        boundVoucherCode = updatedVoucher.code
                    )
                } else it
            }
        } else {
            val newDevice = ConnectedDevice(
                macAddress = deviceMac,
                ipAddress = "192.168.43.${(15..200).random()}",
                hostName = hostName.ifBlank { "User-Device" },
                deviceModel = "Android Client",
                downloadSpeedLimitKbps = _networkConfig.value.defaultDownLimitKbps,
                uploadSpeedLimitKbps = _networkConfig.value.defaultUpLimitKbps,
                downloadedBytes = 120_000L,
                uploadedBytes = 35_000L,
                connectedSince = System.currentTimeMillis(),
                status = DeviceStatus.CONNECTED,
                boundVoucherCode = updatedVoucher.code
            )
            _devices.value = listOf(newDevice) + _devices.value
        }

        addLog(
            tag = "بوابة الدخول",
            message = "تم تسجيل الدخول بنجاح عبر القسيمة [${updatedVoucher.code}]",
            details = "الجهاز: $hostName ($deviceMac) | الصلاحية: ${updatedVoucher.durationHours} ساعة",
            level = LogLevel.SUCCESS
        )

        return PortalAuthResult.Success(
            voucher = updatedVoucher,
            message = "تم تفعيل الإنترنت بنجاح! يمكنك الآن تصفح الشبكة بحرية."
        )
    }

    suspend fun startGuestTrial(deviceMac: String, hostName: String): PortalAuthResult {
        if (!_networkConfig.value.allowGuestTrial) {
            return PortalAuthResult.Error("الوضع التجريبي المجاني معطل من قبل المشرف حالياً.")
        }

        val trialVoucher = Voucher(
            id = UUID.randomUUID().toString(),
            code = "TRIAL-" + (1000..9999).random(),
            durationHours = 0,
            remainingMinutes = _networkConfig.value.guestTrialMinutes,
            dataLimitMb = 100,
            price = 0.0,
            status = VoucherStatus.USED,
            boundDeviceMac = deviceMac,
            note = "تجربة مجانية مؤقتة (${_networkConfig.value.guestTrialMinutes} دقائق)"
        )
        voucherDao.insertVoucher(trialVoucher.toEntity())

        val existingDevice = _devices.value.find { it.macAddress.equals(deviceMac, ignoreCase = true) }
        if (existingDevice != null) {
            _devices.value = _devices.value.map {
                if (it.macAddress.equals(deviceMac, ignoreCase = true)) {
                    it.copy(status = DeviceStatus.CONNECTED, boundVoucherCode = trialVoucher.code)
                } else it
            }
        } else {
            val newDevice = ConnectedDevice(
                macAddress = deviceMac,
                ipAddress = "192.168.43.${(15..200).random()}",
                hostName = hostName.ifBlank { "Guest-Client" },
                deviceModel = "Android 13 / Redmi Note 11 Pro",
                downloadSpeedLimitKbps = 2048,
                uploadSpeedLimitKbps = 1024,
                downloadedBytes = 10_000L,
                uploadedBytes = 5_000L,
                connectedSince = System.currentTimeMillis(),
                status = DeviceStatus.CONNECTED,
                boundVoucherCode = trialVoucher.code
            )
            _devices.value = listOf(newDevice) + _devices.value
        }

        addLog(
            tag = "بوابة الدخول",
            message = "تفعيل تجربة مجانية مؤقتة للجهاز $hostName",
            level = LogLevel.INFO
        )

        return PortalAuthResult.Success(
            voucher = trialVoucher,
            message = "تم بدء الجلسة التجريبية المجانية (${_networkConfig.value.guestTrialMinutes} دقائق) بنجاح!"
        )
    }

    // Network Config
    fun updateNetworkConfig(newConfig: NetworkConfig) {
        _networkConfig.value = newConfig
        addLog(
            tag = "إعدادات الشبكة",
            message = "تم تحديث إعدادات نقطة الاتصال [${newConfig.ssid}] ونطاق التردد والـ DHCP",
            level = LogLevel.INFO
        )
    }

    fun toggleHotspot(enabled: Boolean) {
        _networkConfig.value = _networkConfig.value.copy(isHotspotEnabled = enabled)
        addLog(
            tag = "نقطة الاتصال",
            message = if (enabled) "تم تشغيل بث شبكة MyhotWiFi" else "تم إيقاف بث شبكة MyhotWiFi",
            level = if (enabled) LogLevel.SUCCESS else LogLevel.WARN
        )
    }

    // Logs
    fun addLog(tag: String, message: String, details: String? = null, level: LogLevel = LogLevel.INFO) {
        val log = ActivityLog(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            tag = tag,
            message = message,
            details = details,
            level = level
        )
        scope.launch {
            activityLogDao.insertLog(log.toEntity())
        }
    }

    fun clearLogs() {
        scope.launch {
            activityLogDao.clearLogs()
        }
    }

    private fun generateRandomVoucherCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val part1 = (1..4).map { chars.random() }.joinToString("")
        val part2 = (1..4).map { chars.random() }.joinToString("")
        return "$part1-$part2"
    }

    // Entity mapping helpers
    private fun Voucher.toEntity(): VoucherEntity = VoucherEntity(
        id = id,
        code = code,
        durationHours = durationHours,
        dataLimitMb = dataLimitMb,
        price = price,
        maxDevices = maxDevices,
        status = status.name,
        createdAt = createdAt,
        usedAt = usedAt,
        boundDeviceMac = boundDeviceMac,
        remainingMinutes = remainingMinutes,
        note = note
    )

    private fun VoucherEntity.toModel(): Voucher = Voucher(
        id = id,
        code = code,
        durationHours = durationHours,
        dataLimitMb = dataLimitMb,
        price = price,
        maxDevices = maxDevices,
        status = try { VoucherStatus.valueOf(status) } catch (e: Exception) { VoucherStatus.ACTIVE },
        createdAt = createdAt,
        usedAt = usedAt,
        boundDeviceMac = boundDeviceMac,
        remainingMinutes = remainingMinutes,
        note = note
    )

    private fun ActivityLog.toEntity(): ActivityLogEntity = ActivityLogEntity(
        id = id,
        timestamp = timestamp,
        tag = tag,
        message = message,
        details = details,
        level = level.name
    )

    private fun ActivityLogEntity.toModel(): ActivityLog = ActivityLog(
        id = id,
        timestamp = timestamp,
        tag = tag,
        message = message,
        details = details,
        level = try { LogLevel.valueOf(level) } catch (e: Exception) { LogLevel.INFO }
    )
}
