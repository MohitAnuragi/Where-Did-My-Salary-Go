package com.wheredidmysalarygo.wheredidmysalarygo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val startDestination: String = Screen.Onboarding.route
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val salaryRepository: SalaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            try {
                val salary = salaryRepository.getSalary().first()
                val startDestination = if (salary > 0) {
                    Screen.Home.route
                } else {
                    Screen.Onboarding.route
                }
                _uiState.value = MainUiState(
                    isLoading = false,
                    startDestination = startDestination
                )
            } catch (e: Exception) {
                _uiState.value = MainUiState(
                    isLoading = false,
                    startDestination = Screen.Onboarding.route
                )
            }
        }
    }
}

