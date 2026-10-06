package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers ORDER BY createdAt DESC")
    fun getAllVouchers(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers WHERE code = :code LIMIT 1")
    suspend fun getVoucherByCode(code: String): VoucherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<VoucherEntity>)

    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)

    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteVoucherById(id: String)

    @Query("SELECT COUNT(*) FROM vouchers")
    suspend fun getVouchersCount(): Int
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()

    @Query("SELECT COUNT(*) FROM activity_logs")
    suspend fun getLogsCount(): Int
}
