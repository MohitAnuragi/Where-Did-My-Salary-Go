package com.wheredidmysalarygo.wheredidmysalarygo.ui.snapshot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
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
import com.wheredidmysalarygo.wheredidmysalarygo.ui.settings.SettingsViewModel
import com.wheredidmysalarygo.wheredidmysalarygo.ui.theme.MutedGreen
import com.wheredidmysalarygo.wheredidmysalarygo.ui.theme.SoftRed
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CurrencyFormatter
import com.wheredidmysalarygo.wheredidmysalarygo.utils.CurrencyUtils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapshotScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPro: () -> Unit = {},
    viewModel: SnapshotViewModel = hiltViewModel(),
    viewModelSettings: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Monthly Snapshot",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Show Pro badge if user is Pro
//                        if (uiState.isProUser) {
//                            Surface(
//                                shape = RoundedCornerShape(10.dp),
//                                color = MaterialTheme.colorScheme.primaryContainer
//                            ) {
//                                Row(
//                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.Star,
//                                        contentDescription = null,
//                                        tint = MaterialTheme.colorScheme.primary,
//                                        modifier = Modifier.size(14.dp)
//                                    )
//                                    Text(
//                                        text = "Pro",
//                                        style = MaterialTheme.typography.labelSmall,
//                                        fontWeight = FontWeight.Bold,
//                                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                                    )
//                                }
//                            }
//                        }

                        // Always show download button
                        IconButton(
                            onClick = {
                                if (uiState.isProUser) {
                                    // Pro user: Export data
                                    viewModelSettings.exportData()
                                } else {
                                    // Free user: Navigate to Pro screen
                                    onNavigateToPro()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export data",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
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
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Month Header
                CurrentMonthHeader(month = uiState.currentMonth)

                Spacer(modifier = Modifier.height(8.dp))

                // BIG NUMBERS - Plain, Simple
                Text(
                    text = "Monthly Salary",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = CurrencyFormatter.format(uiState.salary, uiState.countryConfig),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fixed Expenses
                Text(
                    text = "Total Fixed Expenses",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = CurrencyUtils.formatCurrency(uiState.totalExpenses),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftRed
                )
                if (uiState.expenseCount > 0) {
                    Text(
                        text = "${uiState.expenseCount} ${if (uiState.expenseCount == 1) "expense" else "expenses"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Remaining Balance
                Text(
                    text = "Remaining Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = CurrencyFormatter.format(uiState.remainingBalance, uiState.countryConfig),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.remainingBalance > 0) MutedGreen else SoftRed
                )
                Text(
                    text = "${(100 - uiState.committedPercent).toInt()}% free",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Reassuring Message (No charts, no pressure)
                ReassuranceMessage(
                    committedPercent = uiState.committedPercent,
                    remainingBalance = uiState.remainingBalance
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



@Composable
fun ReassuranceMessage(
    committedPercent: Float,
    remainingBalance: Double
) {
    val (emoji, message) = remember(committedPercent, remainingBalance) {
        when {
            remainingBalance <= 0 -> {
                "⚠️" to "Your expenses exceed your salary. Consider reviewing your commitments."
            }
            committedPercent < 40f -> {
                "✨" to "You planned well this month. Great job!"
            }
            committedPercent < 60f -> {
                "👍" to "You're managing your commitments well."
            }
            committedPercent < 80f -> {
                "💭" to "Most of your salary is committed. Be mindful of new expenses."
            }
            else -> {
                "⚠️" to "Very little room left for other expenses. Stay careful."
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                text = emoji,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CurrentMonthHeader(month: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Snapshot for",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = month,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyUtils.formatCurrency(amount),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = color.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun CommitmentProgressCard(committedPercent: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Salary Commitment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${committedPercent.toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (committedPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = when {
                    committedPercent < 50f -> Color(0xFF4CAF50)
                    committedPercent < 80f -> Color(0xFFFFA726)
                    else -> Color(0xFFEF5350)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(100 - committedPercent).toInt()}% of your salary is still free",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun InsightsSection(uiState: SnapshotUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "💡 Insights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Generate insights based on data
            val insights = remember(uiState) {
                buildList {
                    if (uiState.remainingBalance > 0) {
                        add("✓ You have ${CurrencyFormatter.format(uiState.remainingBalance, uiState.countryConfig)} free to spend this month")
                    } else {
                        add("⚠ Your expenses exceed your salary by ${CurrencyFormatter.format(-uiState.remainingBalance, uiState.countryConfig)}")
                    }

                    if (uiState.expenseCount > 0) {
                        val avgExpense = uiState.totalExpenses / uiState.expenseCount
                        add("• Average expense amount: ${CurrencyFormatter.format(avgExpense, uiState.countryConfig)}")
                    }

                    when {
                        uiState.committedPercent < 50f -> {
                            add("✓ Great job! Less than 50% of your salary is committed")
                        }
                        uiState.committedPercent < 80f -> {
                            add("⚠ ${uiState.committedPercent.toInt()}% of your salary is committed. Consider reviewing expenses")
                        }
                        else -> {
                            add("⚠ Over ${uiState.committedPercent.toInt()}% committed! Very little room for other expenses")
                        }
                    }

                    if (uiState.expenseCount == 0) {
                        add("• No fixed expenses tracked yet")
                    } else {
                        add("• You're tracking ${uiState.expenseCount} fixed monthly expenses")
                    }
                }
            }

            insights.forEach { insight ->
                Text(
                    text = insight,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

