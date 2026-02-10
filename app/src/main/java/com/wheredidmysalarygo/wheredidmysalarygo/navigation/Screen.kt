package com.wheredidmysalarygo.wheredidmysalarygo.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object AddExpense : Screen("add_expense")
    object ExpenseList : Screen("expense_list")
    object Snapshot : Screen("snapshot")
    object Settings : Screen("settings")
    object ProSubscription : Screen("pro_subscription")
}

