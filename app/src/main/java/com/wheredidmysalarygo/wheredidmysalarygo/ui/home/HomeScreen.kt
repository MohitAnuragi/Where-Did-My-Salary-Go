package com.wheredidmysalarygo.wheredidmysalarygo.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wheredidmysalarygo.wheredidmysalarygo.ui.theme.MutedGreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.theme.SoftAmber
import com.wheredidmysalarygo.wheredidmysalarygo.ui.theme.SoftRed
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenseList: () -> Unit,
    onNavigateToSnapshot: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Where Did My Salary Go?",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                Spacer(modifier = Modifier.height(8.dp))

                // HERO CARD - Single Salary Summary (Most Important)
                HeroSalarySummaryCard(
                    salary = uiState.salary,
                    totalFixed = uiState.totalFixedExpenses,
                    freeToSpend = uiState.freeToSpend,
                    committedPercent = uiState.committedPercent
                )

                // Commitment Message
                CommitmentMessageText(
                    committedPercent = uiState.committedPercent,
                    expenseCount = uiState.expenses.size
                )

                // PRIMARY CTA - Add Fixed Expense (Most Important Action)
                Button(
                    onClick = onNavigateToAddExpense,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Add Fixed Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Actions (Calm, Small)
                SecondaryActionsRow(
                    expenseCount = uiState.expenses.size,
                    onViewExpenses = onNavigateToExpenseList,
                    onViewSnapshot = onNavigateToSnapshot
                )

                // Empty State or Guidance
                if (uiState.expenses.isEmpty()) {
                    EmptyStateGuidance()
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

            // Divider below TopAppBar
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding()),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun HeroSalarySummaryCard(
    salary: Double,
    totalFixed: Double,
    freeToSpend: Double,
    committedPercent: Float
) {
    // Determine color based on free percentage (Calm colors only)
    val freePercent = 100f - committedPercent
    val cardColor = when {
        freePercent >= 50f -> MutedGreen // Green - Good
        freePercent >= 20f -> SoftAmber   // Amber - Warning
        else -> SoftRed                    // Soft Red - Alert (not bright red)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label - Small, minimal
            Text(
                text = "Free to Spend",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // BIG NUMBER - Dominates the card
            Text(
                text = CurrencyUtils.formatCurrency(freeToSpend),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Soft divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.8f),
                color = Color.White.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Details Row - Clean, minimal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Monthly Salary
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Monthly Salary",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.formatCurrency(salary),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Fixed Expenses
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Fixed Expenses",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.formatCurrency(totalFixed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Soft progress bar
            LinearProgressIndicator(
                progress = { committedPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.White.copy(alpha = 0.9f),
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun CommitmentMessageText(
    committedPercent: Float,
    expenseCount: Int
) {
    val message = when {
        committedPercent == 0f && expenseCount == 0 ->
            "Add your first fixed expense to understand your salary better."
        committedPercent > 100f -> {
            val overPercent = (committedPercent - 100f).toInt()
            "Your expenses exceed salary by $overPercent%. Consider reviewing your commitments."
        }
        committedPercent < 30f ->
            "${committedPercent.toInt()}% of salary committed. You're doing great!"
        committedPercent < 50f ->
            "${committedPercent.toInt()}% of salary committed. Good planning!"
        committedPercent < 70f ->
            "${committedPercent.toInt()}% of salary already committed."
        committedPercent < 90f ->
            "${committedPercent.toInt()}% of salary committed. Be mindful of new expenses."
        else ->
            "${committedPercent.toInt()}% of salary committed. Most of your salary is spoken for."
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SecondaryActionsRow(
    expenseCount: Int,
    onViewExpenses: () -> Unit,
    onViewSnapshot: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // View Expenses List
        OutlinedButton(
            onClick = onViewExpenses,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "View All",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 13.sp
                )
                if (expenseCount > 0) {
                    Text(
                        "$expenseCount expenses",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Monthly Snapshot
        OutlinedButton(
            onClick = onViewSnapshot,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Assessment,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Snapshot",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun EmptyStateGuidance() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💡",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your monthly expenses like rent, EMIs, and subscriptions to see how much of your salary is already committed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

