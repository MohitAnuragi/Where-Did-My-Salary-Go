package com.wheredidmysalarygo.wheredidmysalarygo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wheredidmysalarygo.wheredidmysalarygo.ui.addexpense.AddExpenseScreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.expenselist.ExpenseListScreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.home.HomeScreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.onboarding.OnboardingScreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.pro.ProSubscriptionScreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.settings.SettingsScreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.snapshot.SnapshotScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateToExpenseList = {
                    navController.navigate(Screen.ExpenseList.route)
                },
                onNavigateToSnapshot = {
                    navController.navigate(Screen.Snapshot.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Add Expense Screen
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Expense List Screen
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Snapshot Screen
        composable(Screen.Snapshot.route) {
            SnapshotScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPro = {
                    navController.navigate(Screen.ProSubscription.route)
                }
            )
        }

        // Pro Subscription Screen
        composable(Screen.ProSubscription.route) {
            ProSubscriptionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

