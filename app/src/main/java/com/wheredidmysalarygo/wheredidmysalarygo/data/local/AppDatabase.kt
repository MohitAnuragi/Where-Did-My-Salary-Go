package com.wheredidmysalarygo.wheredidmysalarygo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao.ExpenseDao
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao.MonthlySummaryDao
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.ExpenseEntity
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.entity.MonthlySummaryEntity
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Database(
    entities = [ExpenseEntity::class, MonthlySummaryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun monthlySummaryDao(): MonthlySummaryDao

    companion object {
        const val DATABASE_NAME = "where_did_my_salary_go_db"

        /**
         * Migration from version 1 to 2
         * - Adds 'month' column to expenses table
         * - Creates monthly_summary table
         * - Migrates existing expenses to current month
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Get current month in yyyy-MM format
                val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

                // Create monthly_summary table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS monthly_summary (
                        month TEXT PRIMARY KEY NOT NULL,
                        salaryAmount REAL NOT NULL,
                        totalFixedExpenses REAL NOT NULL,
                        remainingAmount REAL NOT NULL
                    )
                    """.trimIndent()
                )

                // Add month column to expenses table with default current month
                database.execSQL(
                    """
                    CREATE TABLE expenses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        dueDate INTEGER NOT NULL,
                        frequency TEXT NOT NULL,
                        month TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                // Copy existing data with current month
                database.execSQL(
                    """
                    INSERT INTO expenses_new (id, name, amount, dueDate, frequency, month)
                    SELECT id, name, amount, dueDate, frequency, '$currentMonth'
                    FROM expenses
                    """.trimIndent()
                )

                // Drop old table and rename new one
                database.execSQL("DROP TABLE expenses")
                database.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
            }
        }
    }
}

