package com.example.paisatracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity for storing merchant-specific categorization rules
 * Used to automatically assign categories and projects to auto-created SMS transactions
 */
@Entity(
    tableName = "merchant_rules",
    indices = [
        Index(value = ["merchant_pattern"], unique = true),
        Index(value = ["priority"]),
        Index(value = ["is_active"])
    ]
)
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "merchant_pattern")
    val merchantPattern: String,  // Regex pattern or simple string (e.g., "Amazon|Flipkart")

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "project_id")
    val projectId: Long? = null,

    @ColumnInfo(name = "priority")
    val priority: Int = 0,  // Higher = applied first

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "match_count")
    val matchCount: Int = 0,  // Track how many times this rule has been used

    @ColumnInfo(name = "last_matched_at")
    val lastMatchedAt: LocalDateTime? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Made with Bob