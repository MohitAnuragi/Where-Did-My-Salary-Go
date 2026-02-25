package com.wheredidmysalarygo.wheredidmysalarygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.wheredidmysalarygo.wheredidmysalarygo.billing.BillingManager
import com.wheredidmysalarygo.wheredidmysalarygo.billing.BillingRepository
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.navigation.AppNavGraph
import com.wheredidmysalarygo.wheredidmysalarygo.ui.theme.WhereDidMySalaryGoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    @Inject
    lateinit var billingRepository: BillingRepository

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize billing
        initializeBilling()

        setContent {
            val darkMode by userPreferencesManager.darkModeFlow.collectAsState(initial = false)

            WhereDidMySalaryGoTheme(darkTheme = darkMode) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    AppNavGraph(
                        navController = navController,
                        startDestination = uiState.startDestination
                    )
                }
            }
        }
    }

    private fun initializeBilling() {
        // Initialize billing and restore purchases
        billingRepository.initialize()

        // Set up purchase update callback
        billingManager.setPurchaseUpdateCallback { purchases ->
            billingRepository.handlePurchaseUpdate(purchases)
        }
    }

    override fun onResume() {
        super.onResume()
        // Validate subscription status on resume (offline + online validation)
        CoroutineScope(Dispatchers.Main).launch {
            billingRepository.validateSubscriptionStatus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}

