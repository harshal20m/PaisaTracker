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

private val Context.appVersionDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_version_preferences")

/**
 * Repository for tracking app version changes and showing appropriate messages
 */
class AppVersionPreferencesRepository private constructor(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val LAST_KNOWN_VERSION = stringPreferencesKey("last_known_version")
        val STAR_REPO_CARD_SHOWN = booleanPreferencesKey("star_repo_card_shown")
        val CURRENT_VERSION_RELEASE_NOTES_SHOWN = booleanPreferencesKey("current_version_release_notes_shown")
    }

    /**
     * Get the last known app version
     */
    val lastKnownVersion: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_KNOWN_VERSION]
        }

    /**
     * Check if star repository card has been shown for current version
     */
    val starRepoCardShown: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STAR_REPO_CARD_SHOWN] ?: false
        }

    /**
     * Check if release notes have been shown for current version
     */
    val releaseNotesShown: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_VERSION_RELEASE_NOTES_SHOWN] ?: false
        }

    /**
     * Update the last known version
     */
    suspend fun updateLastKnownVersion(version: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_KNOWN_VERSION] = version
        }
    }

    /**
     * Mark star repository card as shown
     */
    suspend fun markStarRepoCardShown() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STAR_REPO_CARD_SHOWN] = true
        }
    }

    /**
     * Mark release notes as shown for current version
     */
    suspend fun markReleaseNotesShown() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_VERSION_RELEASE_NOTES_SHOWN] = true
        }
    }

    /**
     * Reset flags when a new version is detected
     * This allows showing the star card and release notes again
     */
    suspend fun resetForNewVersion() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STAR_REPO_CARD_SHOWN] = false
            preferences[PreferencesKeys.CURRENT_VERSION_RELEASE_NOTES_SHOWN] = false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppVersionPreferencesRepository? = null

        fun getInstance(context: Context): AppVersionPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AppVersionPreferencesRepository(context.appVersionDataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}

// Made with Bob
