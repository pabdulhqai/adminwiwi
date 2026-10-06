package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.LogLevel
import com.example.model.VoucherStatus

@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey val id: String,
    val code: String,
    val durationHours: Int,
    val dataLimitMb: Long,
    val price: Double,
    val maxDevices: Int = 1,
    val status: String = VoucherStatus.ACTIVE.name,
    val createdAt: Long = System.currentTimeMillis(),
    val usedAt: Long? = null,
    val boundDeviceMac: String? = null,
    val remainingMinutes: Int = durationHours * 60,
    val note: String = ""
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val tag: String,
    val message: String,
    val details: String? = null,
    val level: String = LogLevel.INFO.name
)
