package com.wheredidmysalarygo.wheredidmysalarygo.export

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * ExportManager - Entry point for data export
 *
 * Responsibilities:
 * - Check subscription status
 * - Coordinate export process
 * - Handle file sharing
 * - Show user feedback
 */
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exportRepository: ExportRepository
) {

    /**
     * Export data based on subscription plan
     *
     * @param subscriptionPlan User's current subscription plan
     * @return Success status
     */
    suspend fun exportData(subscriptionPlan: SubscriptionPlan): ExportResult {
        try {
            // Check if user can export
            if (subscriptionPlan == SubscriptionPlan.FREE) {
                return ExportResult.NotAllowed
            }

            // Get data based on plan limits (suspend call, already on background thread)
            val data = withContext(Dispatchers.IO) {
                exportRepository.getExportData(subscriptionPlan.exportMonths)
            }

            // Generate CSV file
            val fileName = CsvExporter.generateFileName()
            val outputFile = File(context.cacheDir, fileName)

            val success = withContext(Dispatchers.IO) {
                CsvExporter.generateCsv(data, outputFile)
            }

            return if (success) {
                ExportResult.Success(outputFile)
            } else {
                ExportResult.Error("Failed to generate CSV file")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return ExportResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Share exported file via system share sheet
     *
     * @param file The exported CSV file
     */
    suspend fun shareFile(file: File) {
        withContext(Dispatchers.Main) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(shareIntent, "Export Salary Data")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

                // Show success toast
                Toast.makeText(
                    context,
                    "Data exported successfully",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Failed to share file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Show upgrade prompt for free users
     */
    suspend fun showUpgradePrompt() {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Upgrade to Pro to export data",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}

/**
 * Export result sealed class
 */
sealed class ExportResult {
    data class Success(val file: File) : ExportResult()
    data class Error(val message: String) : ExportResult()
    object NotAllowed : ExportResult()
}

