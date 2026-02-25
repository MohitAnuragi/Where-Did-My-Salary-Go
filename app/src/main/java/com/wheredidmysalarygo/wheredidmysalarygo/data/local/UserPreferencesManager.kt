package com.wheredidmysalarygo.wheredidmysalarygo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val COUNTRY_CODE_KEY = stringPreferencesKey("country_code")

        // Pro subscription flag (temporary - no real payments yet)
        private val IS_PRO_USER_KEY = booleanPreferencesKey("is_pro_user")

        // Subscription plan for export limits
        private val SUBSCRIPTION_PLAN_KEY = stringPreferencesKey("subscription_plan")

        // Notification settings (Pro only)
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val SALARY_SUMMARY_ENABLED_KEY = booleanPreferencesKey("salary_summary_enabled")
        private val MONTH_END_SNAPSHOT_ENABLED_KEY = booleanPreferencesKey("month_end_snapshot_enabled")
        private val DUE_DATE_REMINDERS_ENABLED_KEY = booleanPreferencesKey("due_date_reminders_enabled")
        private val REMIND_DAYS_BEFORE_KEY = intPreferencesKey("remind_days_before")
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

    val countryCodeFlow: Flow<String> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[COUNTRY_CODE_KEY] ?: "IN" // Default to India
        }

    // Pro subscription
    val isProUserFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[IS_PRO_USER_KEY] ?: false
        }

    // Subscription plan (for export limits)
    val subscriptionPlanFlow: Flow<String> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[SUBSCRIPTION_PLAN_KEY] ?: "FREE"
        }

    // Notification settings (Pro only)
    val notificationsEnabledFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: false
        }

    val salarySummaryEnabledFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[SALARY_SUMMARY_ENABLED_KEY] ?: true
        }

    val monthEndSnapshotEnabledFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[MONTH_END_SNAPSHOT_ENABLED_KEY] ?: true
        }

    val dueDateRemindersEnabledFlow: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[DUE_DATE_REMINDERS_ENABLED_KEY] ?: true
        }

    val remindDaysBeforeFlow: Flow<Int> = context.userPreferencesDataStore.data
        .map { preferences ->
            preferences[REMIND_DAYS_BEFORE_KEY] ?: 1 // Default: 1 day before
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

    suspend fun setCountryCode(countryCode: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[COUNTRY_CODE_KEY] = countryCode
        }
    }


    // Subscription plan (for export limits)
    suspend fun setSubscriptionPlan(plan: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SUBSCRIPTION_PLAN_KEY] = plan
        }
    }

    // Notification settings (Pro only)
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setSalarySummaryEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SALARY_SUMMARY_ENABLED_KEY] = enabled
        }
    }

    suspend fun setMonthEndSnapshotEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[MONTH_END_SNAPSHOT_ENABLED_KEY] = enabled
        }
    }

    suspend fun setDueDateRemindersEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[DUE_DATE_REMINDERS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setRemindDaysBefore(days: Int) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[REMIND_DAYS_BEFORE_KEY] = days
        }
    }


    suspend fun clearAllPreferences() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

