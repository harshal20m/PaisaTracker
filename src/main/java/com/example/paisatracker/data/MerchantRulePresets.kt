package com.example.paisatracker.data

import java.time.LocalDateTime

/**
 * Preset merchant rules for common merchants
 * Users can import these to quickly set up automatic categorization
 */
object MerchantRulePresets {
    
    /**
     * Get the most common default rules that should be active by default
     * These are automatically inserted on first app launch
     */
    fun getDefaultActiveRules(): List<PresetRule> {
        return listOf(
            // Food Delivery - Most Common
            PresetRule(
                merchantPattern = "Swiggy|SWIGGY|swiggy",
                categoryName = "Food & Dining",
                priority = 10,
                description = "Swiggy food delivery"
            ),
            PresetRule(
                merchantPattern = "Zomato|ZOMATO|zomato",
                categoryName = "Food & Dining",
                priority = 10,
                description = "Zomato food delivery"
            ),
            
            // E-commerce - Most Popular
            PresetRule(
                merchantPattern = "Amazon|AMAZON|amazon",
                categoryName = "Shopping",
                priority = 15,
                description = "Amazon purchases"
            ),
            PresetRule(
                merchantPattern = "Flipkart|FLIPKART|flipkart",
                categoryName = "Shopping",
                priority = 15,
                description = "Flipkart purchases"
            ),
            
            // Transportation
            PresetRule(
                merchantPattern = "Uber|UBER|uber",
                categoryName = "Transportation",
                priority = 10,
                description = "Uber rides"
            ),
            PresetRule(
                merchantPattern = "Ola|OLA|ola",
                categoryName = "Transportation",
                priority = 10,
                description = "Ola rides"
            ),
            
            // Groceries
            PresetRule(
                merchantPattern = "BigBasket|BIGBASKET|bigbasket|Big Basket",
                categoryName = "Groceries",
                priority = 15,
                description = "BigBasket grocery"
            ),
            PresetRule(
                merchantPattern = "Blinkit|BLINKIT|blinkit",
                categoryName = "Groceries",
                priority = 15,
                description = "Blinkit quick commerce"
            ),
            
            // Entertainment - Subscriptions
            PresetRule(
                merchantPattern = "Netflix|NETFLIX|netflix",
                categoryName = "Entertainment",
                priority = 20,
                description = "Netflix subscription"
            ),
            PresetRule(
                merchantPattern = "Prime Video|PRIME VIDEO|prime video|Amazon Prime",
                categoryName = "Entertainment",
                priority = 20,
                description = "Prime Video subscription"
            ),
            
            // Fuel
            PresetRule(
                merchantPattern = "Indian Oil|INDIAN OIL|indian oil|IOCL",
                categoryName = "Fuel",
                priority = 10,
                description = "Indian Oil fuel"
            ),
            PresetRule(
                merchantPattern = "HP|hp|Hindustan Petroleum|HINDUSTAN PETROLEUM",
                categoryName = "Fuel",
                priority = 10,
                description = "HP fuel"
            )
        )
    }
    
    /**
     * Get preset rules for a specific category
     * Note: Category IDs and Project IDs should be mapped to actual IDs in the database
     */
    fun getPresetRules(): List<PresetRule> {
        return listOf(
            // E-commerce & Shopping
            PresetRule(
                merchantPattern = "Amazon|AMAZON|amazon",
                categoryName = "Shopping",
                priority = 10,
                description = "Amazon purchases"
            ),
            PresetRule(
                merchantPattern = "Flipkart|FLIPKART|flipkart",
                categoryName = "Shopping",
                priority = 10,
                description = "Flipkart purchases"
            ),
            PresetRule(
                merchantPattern = "Myntra|MYNTRA|myntra",
                categoryName = "Shopping",
                priority = 10,
                description = "Myntra fashion purchases"
            ),
            PresetRule(
                merchantPattern = "Meesho|MEESHO|meesho",
                categoryName = "Shopping",
                priority = 10,
                description = "Meesho purchases"
            ),
            
            // Food & Dining
            PresetRule(
                merchantPattern = "Swiggy|SWIGGY|swiggy",
                categoryName = "Food & Dining",
                priority = 10,
                description = "Swiggy food delivery"
            ),
            PresetRule(
                merchantPattern = "Zomato|ZOMATO|zomato",
                categoryName = "Food & Dining",
                priority = 10,
                description = "Zomato food delivery"
            ),
            PresetRule(
                merchantPattern = "Dominos|DOMINOS|dominos|Domino",
                categoryName = "Food & Dining",
                priority = 10,
                description = "Domino's Pizza"
            ),
            PresetRule(
                merchantPattern = "McDonald|MCDONALD|mcdonald|McD",
                categoryName = "Food & Dining",
                priority = 10,
                description = "McDonald's"
            ),
            PresetRule(
                merchantPattern = "KFC|kfc",
                categoryName = "Food & Dining",
                priority = 10,
                description = "KFC"
            ),
            PresetRule(
                merchantPattern = "Starbucks|STARBUCKS|starbucks",
                categoryName = "Food & Dining",
                priority = 10,
                description = "Starbucks"
            ),
            
            // Groceries
            PresetRule(
                merchantPattern = "BigBasket|BIGBASKET|bigbasket|Big Basket",
                categoryName = "Groceries",
                priority = 10,
                description = "BigBasket grocery"
            ),
            PresetRule(
                merchantPattern = "Blinkit|BLINKIT|blinkit|Grofers",
                categoryName = "Groceries",
                priority = 10,
                description = "Blinkit/Grofers"
            ),
            PresetRule(
                merchantPattern = "Zepto|ZEPTO|zepto",
                categoryName = "Groceries",
                priority = 10,
                description = "Zepto quick commerce"
            ),
            PresetRule(
                merchantPattern = "DMart|DMART|dmart|D-Mart",
                categoryName = "Groceries",
                priority = 10,
                description = "DMart"
            ),
            PresetRule(
                merchantPattern = "Reliance Fresh|RELIANCE FRESH|reliance fresh",
                categoryName = "Groceries",
                priority = 10,
                description = "Reliance Fresh"
            ),
            
            // Transportation
            PresetRule(
                merchantPattern = "Uber|UBER|uber",
                categoryName = "Transportation",
                priority = 10,
                description = "Uber rides"
            ),
            PresetRule(
                merchantPattern = "Ola|OLA|ola",
                categoryName = "Transportation",
                priority = 10,
                description = "Ola cabs"
            ),
            PresetRule(
                merchantPattern = "Rapido|RAPIDO|rapido",
                categoryName = "Transportation",
                priority = 10,
                description = "Rapido bike taxi"
            ),
            PresetRule(
                merchantPattern = "IRCTC|irctc",
                categoryName = "Transportation",
                priority = 10,
                description = "Indian Railways"
            ),
            PresetRule(
                merchantPattern = "MakeMyTrip|MAKEMYTRIP|makemytrip|MMT",
                categoryName = "Transportation",
                priority = 10,
                description = "MakeMyTrip travel"
            ),
            
            // Fuel
            PresetRule(
                merchantPattern = "Indian Oil|INDIAN OIL|indian oil|IOCL",
                categoryName = "Fuel",
                priority = 10,
                description = "Indian Oil petrol pump"
            ),
            PresetRule(
                merchantPattern = "HP|Hindustan Petroleum|HINDUSTAN PETROLEUM",
                categoryName = "Fuel",
                priority = 10,
                description = "HP petrol pump"
            ),
            PresetRule(
                merchantPattern = "BPCL|Bharat Petroleum|BHARAT PETROLEUM",
                categoryName = "Fuel",
                priority = 10,
                description = "BPCL petrol pump"
            ),
            PresetRule(
                merchantPattern = "Shell|SHELL|shell",
                categoryName = "Fuel",
                priority = 10,
                description = "Shell petrol pump"
            ),
            
            // Entertainment
            PresetRule(
                merchantPattern = "Netflix|NETFLIX|netflix",
                categoryName = "Entertainment",
                priority = 10,
                description = "Netflix subscription"
            ),
            PresetRule(
                merchantPattern = "Prime Video|PRIME VIDEO|prime video|Amazon Prime",
                categoryName = "Entertainment",
                priority = 10,
                description = "Amazon Prime Video"
            ),
            PresetRule(
                merchantPattern = "Hotstar|HOTSTAR|hotstar|Disney",
                categoryName = "Entertainment",
                priority = 10,
                description = "Disney+ Hotstar"
            ),
            PresetRule(
                merchantPattern = "Spotify|SPOTIFY|spotify",
                categoryName = "Entertainment",
                priority = 10,
                description = "Spotify music"
            ),
            PresetRule(
                merchantPattern = "BookMyShow|BOOKMYSHOW|bookmyshow|BMS",
                categoryName = "Entertainment",
                priority = 10,
                description = "BookMyShow tickets"
            ),
            PresetRule(
                merchantPattern = "PVR|pvr",
                categoryName = "Entertainment",
                priority = 10,
                description = "PVR Cinemas"
            ),
            
            // Utilities & Bills
            PresetRule(
                merchantPattern = "Electricity|ELECTRICITY|electricity|Power",
                categoryName = "Utilities",
                priority = 10,
                description = "Electricity bill"
            ),
            PresetRule(
                merchantPattern = "Jio|JIO|jio|Reliance Jio",
                categoryName = "Utilities",
                priority = 10,
                description = "Jio recharge"
            ),
            PresetRule(
                merchantPattern = "Airtel|AIRTEL|airtel",
                categoryName = "Utilities",
                priority = 10,
                description = "Airtel recharge"
            ),
            PresetRule(
                merchantPattern = "Vi|Vodafone|VODAFONE|vodafone|Idea",
                categoryName = "Utilities",
                priority = 10,
                description = "Vi/Vodafone recharge"
            ),
            
            // Health & Fitness
            PresetRule(
                merchantPattern = "PharmEasy|PHARMEASY|pharmeasy",
                categoryName = "Health",
                priority = 10,
                description = "PharmEasy medicines"
            ),
            PresetRule(
                merchantPattern = "1mg|1MG|Tata 1mg",
                categoryName = "Health",
                priority = 10,
                description = "1mg medicines"
            ),
            PresetRule(
                merchantPattern = "Apollo|APOLLO|apollo|Apollo Pharmacy",
                categoryName = "Health",
                priority = 10,
                description = "Apollo Pharmacy"
            ),
            PresetRule(
                merchantPattern = "Cult.fit|CULTFIT|cultfit|Cult Fit",
                categoryName = "Health",
                priority = 10,
                description = "Cult.fit gym"
            ),
            
            // Education
            PresetRule(
                merchantPattern = "Udemy|UDEMY|udemy",
                categoryName = "Education",
                priority = 10,
                description = "Udemy courses"
            ),
            PresetRule(
                merchantPattern = "Coursera|COURSERA|coursera",
                categoryName = "Education",
                priority = 10,
                description = "Coursera courses"
            ),
            PresetRule(
                merchantPattern = "Unacademy|UNACADEMY|unacademy",
                categoryName = "Education",
                priority = 10,
                description = "Unacademy"
            ),
            PresetRule(
                merchantPattern = "BYJU|Byju|byju",
                categoryName = "Education",
                priority = 10,
                description = "BYJU'S"
            )
        )
    }
    
    /**
     * Convert preset rules to MerchantRuleEntity with actual category IDs
     * @param categoryMap Map of category names to category IDs
     * @param defaultProjectId Optional default project ID to assign
     */
    suspend fun convertToEntities(
        categoryMap: Map<String, Long>,
        defaultProjectId: Long? = null
    ): List<MerchantRuleEntity> {
        return getPresetRules().mapNotNull { preset ->
            val categoryId = categoryMap[preset.categoryName]
            if (categoryId != null) {
                MerchantRuleEntity(
                    merchantPattern = preset.merchantPattern,
                    categoryId = categoryId,
                    projectId = defaultProjectId,
                    priority = preset.priority,
                    isActive = true,
                    matchCount = 0,
                    lastMatchedAt = null,
                    createdAt = LocalDateTime.now()
                )
            } else {
                null // Skip if category doesn't exist
            }
        }
    }
    
    /**
     * Get unique category names from presets
     */
    fun getRequiredCategories(): Set<String> {
        return getPresetRules().map { it.categoryName }.toSet()
    }
}

/**
 * Data class for preset rule definition
 */
data class PresetRule(
    val merchantPattern: String,
    val categoryName: String,
    val priority: Int,
    val description: String
)

// Made with Bob