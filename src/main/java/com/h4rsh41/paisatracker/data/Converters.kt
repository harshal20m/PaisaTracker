package com.h4rsh41.paisatracker.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromBudgetPeriod(period: BudgetPeriod): String {
        return period.name
    }

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod {
        return try {
            BudgetPeriod.valueOf(value)
        } catch (e: Exception) {
            BudgetPeriod.MONTHLY
        }
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            try {
                LocalDateTime.parse(it, formatter)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun fromSmsTransactionStatus(status: SmsTransactionStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSmsTransactionStatus(value: String): SmsTransactionStatus {
        return try {
            SmsTransactionStatus.valueOf(value)
        } catch (e: Exception) {
            SmsTransactionStatus.PENDING
        }
    }
}