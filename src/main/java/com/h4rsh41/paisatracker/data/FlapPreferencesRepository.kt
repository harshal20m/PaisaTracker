package com.h4rsh41.paisatracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.flapDataStore: DataStore<Preferences> by preferencesDataStore(name = "flap_preferences")

/**
 * Repository for managing Quick Access Flap preferences.
 * Handles visibility, position (left/right), and behavior settings.
 */
class FlapPreferencesRepository private constructor(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val FLAP_ENABLED = booleanPreferencesKey("flap_enabled")
        val FLAP_POSITION = stringPreferencesKey("flap_position") // "left" or "right"
        val FLAP_DEFAULT_TAB = stringPreferencesKey("flap_default_tab") // "calculator" or "notes"
    }

    /**
     * Flow indicating whether the flap is enabled.
     * Default: true (enabled)
     */
    val isFlapEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FLAP_ENABLED] ?: true
        }

    /**
     * Flow for flap position: "left" or "right"
     * Default: "right"
     */
    val flapPosition: Flow<FlapPosition> = dataStore.data
        .map { preferences ->
            val position = preferences[PreferencesKeys.FLAP_POSITION] ?: "right"
            when (position) {
                "left" -> FlapPosition.LEFT
                else -> FlapPosition.RIGHT
            }
        }

    /**
     * Flow for default tab when flap opens
     * Default: "calculator"
     */
    val flapDefaultTab: Flow<FlapDefaultTab> = dataStore.data
        .map { preferences ->
            val tab = preferences[PreferencesKeys.FLAP_DEFAULT_TAB] ?: "calculator"
            when (tab) {
                "notes" -> FlapDefaultTab.NOTES
                else -> FlapDefaultTab.CALCULATOR
            }
        }

    /**
     * Enable or disable the flap
     */
    suspend fun setFlapEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLAP_ENABLED] = enabled
        }
    }

    /**
     * Set flap position (left or right)
     */
    suspend fun setFlapPosition(position: FlapPosition) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLAP_POSITION] = position.value
        }
    }

    /**
     * Set default tab when flap opens
     */
    suspend fun setFlapDefaultTab(tab: FlapDefaultTab) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLAP_DEFAULT_TAB] = tab.value
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FlapPreferencesRepository? = null

        fun getInstance(context: Context): FlapPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FlapPreferencesRepository(context.flapDataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Enum for flap position
 */
enum class FlapPosition(val value: String) {
    LEFT("left"),
    RIGHT("right")
}

/**
 * Enum for default tab
 */
enum class FlapDefaultTab(val value: String, val displayName: String) {
    CALCULATOR("calculator", "Calculator"),
    NOTES("notes", "Notes")
}

// Made with Bob
