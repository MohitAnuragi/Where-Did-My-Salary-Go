package com.wheredidmysalarygo.wheredidmysalarygo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferencesManager(private val context: Context) {

    companion object {
        private val SALARY_KEY = doublePreferencesKey("monthly_salary")
        private val SALARY_CREDIT_DATE_KEY = intPreferencesKey("salary_credit_date")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    val salaryFlow: Flow<Double> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[SALARY_KEY] ?: 0.0
        }

    val salaryCreditDateFlow: Flow<Int?> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[SALARY_CREDIT_DATE_KEY]
        }

    val darkModeFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    suspend fun setSalary(amount: Double) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SALARY_KEY] = amount
        }
    }

    suspend fun setSalaryCreditDate(date: Int?) {
        context.userPreferencesDataStore.edit { preferences ->
            if (date != null) {
                preferences[SALARY_CREDIT_DATE_KEY] = date
            } else {
                preferences.remove(SALARY_CREDIT_DATE_KEY)
            }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun clearAllPreferences() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

