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

private val Context.animationDataStore: DataStore<Preferences> by preferencesDataStore(name = "animation_preferences")

/**
 * Repository for managing page transition animation preferences.
 * Stores animation type, speed, and enable/disable state using DataStore.
 */
class AnimationPreferencesRepository private constructor(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val ANIMATION_TYPE = stringPreferencesKey("animation_type")
        val ANIMATION_SPEED = stringPreferencesKey("animation_speed")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val RESPECT_SYSTEM_REDUCE_MOTION = booleanPreferencesKey("respect_system_reduce_motion")
    }

    /**
     * Flow of the current animation type.
     * Defaults to SLIDE_FADE (recommended).
     */
    val animationType: Flow<AnimationType> = dataStore.data
        .map { preferences ->
            val typeName = preferences[PreferencesKeys.ANIMATION_TYPE] ?: AnimationType.default().name
            try {
                AnimationType.valueOf(typeName)
            } catch (e: IllegalArgumentException) {
                AnimationType.default()
            }
        }

    /**
     * Flow of the current animation speed.
     * Defaults to NORMAL (300ms).
     */
    val animationSpeed: Flow<AnimationSpeed> = dataStore.data
        .map { preferences ->
            val speedName = preferences[PreferencesKeys.ANIMATION_SPEED] ?: AnimationSpeed.default().name
            try {
                AnimationSpeed.valueOf(speedName)
            } catch (e: IllegalArgumentException) {
                AnimationSpeed.default()
            }
        }

    /**
     * Flow indicating whether animations are enabled.
     * Defaults to true.
     */
    val animationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true
        }

    /**
     * Flow indicating whether to respect system "Reduce Motion" setting.
     * Defaults to true for accessibility.
     */
    val respectSystemReduceMotion: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RESPECT_SYSTEM_REDUCE_MOTION] ?: true
        }

    /**
     * Saves the selected animation type.
     */
    suspend fun saveAnimationType(type: AnimationType) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATION_TYPE] = type.name
        }
    }

    /**
     * Saves the selected animation speed.
     */
    suspend fun saveAnimationSpeed(speed: AnimationSpeed) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATION_SPEED] = speed.name
        }
    }

    /**
     * Enables or disables all animations.
     */
    suspend fun setAnimationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }

    /**
     * Sets whether to respect system "Reduce Motion" accessibility setting.
     */
    suspend fun setRespectSystemReduceMotion(respect: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RESPECT_SYSTEM_REDUCE_MOTION] = respect
        }
    }

    /**
     * Resets all animation preferences to defaults.
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATION_TYPE] = AnimationType.default().name
            preferences[PreferencesKeys.ANIMATION_SPEED] = AnimationSpeed.default().name
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = true
            preferences[PreferencesKeys.RESPECT_SYSTEM_REDUCE_MOTION] = true
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AnimationPreferencesRepository? = null

        /**
         * Gets the singleton instance of AnimationPreferencesRepository.
         */
        fun getInstance(context: Context): AnimationPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AnimationPreferencesRepository(context.animationDataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}

// Made with Bob
