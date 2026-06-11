package com.h4rsh41.paisatracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MerchantRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: MerchantRuleEntity): Long

    @Update
    suspend fun update(rule: MerchantRuleEntity)

    @Delete
    suspend fun delete(rule: MerchantRuleEntity)

    @Query("SELECT * FROM merchant_rules WHERE is_active = 1 ORDER BY priority DESC")
    fun getActiveRules(): Flow<List<MerchantRuleEntity>>

    @Query("SELECT * FROM merchant_rules WHERE is_active = 1 ORDER BY priority DESC")
    suspend fun getActiveRulesList(): List<MerchantRuleEntity>

    @Query("SELECT * FROM merchant_rules ORDER BY priority DESC, created_at DESC")
    fun getAllRules(): Flow<List<MerchantRuleEntity>>

    @Query("SELECT * FROM merchant_rules WHERE id = :id")
    suspend fun getById(id: Long): MerchantRuleEntity?

    @Query("""
        UPDATE merchant_rules 
        SET match_count = match_count + 1, 
            last_matched_at = :timestamp 
        WHERE id = :id
    """)
    suspend fun incrementMatchCount(id: Long, timestamp: LocalDateTime)

    @Query("DELETE FROM merchant_rules")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM merchant_rules WHERE is_active = 1")
    suspend fun getActiveRuleCount(): Int

    @Query("UPDATE merchant_rules SET is_active = :isActive WHERE id = :id")
    suspend fun toggleActive(id: Long, isActive: Boolean)
}

// Made with Bob
