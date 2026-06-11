package com.h4rsh41.paisatracker.util

import com.pennywiseai.parser.core.bank.BankParserFactory

/**
 * Utility object for getting list of supported banks from the parser
 */
object SupportedBanks {
    
    /**
     * Get all supported bank names sorted alphabetically
     */
    fun getAllBankNames(): List<String> {
        return BankParserFactory.getAllParsers()
            .map { it.getBankName() }
            .distinct()
            .sorted()
    }
    
    /**
     * Check if a bank name is supported
     */
    fun isBankSupported(bankName: String): Boolean {
        return getAllBankNames().any { it.equals(bankName, ignoreCase = true) }
    }
    
    /**
     * Get bank name from parser or null if not found
     */
    fun getBankNameOrNull(bankName: String): String? {
        return getAllBankNames().firstOrNull { it.equals(bankName, ignoreCase = true) }
    }
}

// Made with Bob
