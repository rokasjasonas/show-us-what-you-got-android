package com.rokas.showuswhatyougot.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("selected_language")
        val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    }

    val selectedLanguage: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.LANGUAGE]
    }

    val darkModeEnabled: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[Keys.DARK_MODE]
    }

    suspend fun setSelectedLanguage(languageTag: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = languageTag
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DARK_MODE] = enabled
        }
    }
}

