package com.wheredidmysalarygo.wheredidmysalarygo.di

import android.content.Context
import androidx.room.Room
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.AppDatabase
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.UserPreferencesManager
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao.ExpenseDao
import com.wheredidmysalarygo.wheredidmysalarygo.data.local.dao.MonthlySummaryDao
import com.wheredidmysalarygo.wheredidmysalarygo.data.repository.ExpenseRepositoryImpl
import com.wheredidmysalarygo.wheredidmysalarygo.data.repository.MonthlySummaryRepositoryImpl
import com.wheredidmysalarygo.wheredidmysalarygo.data.repository.SalaryRepositoryImpl
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.ExpenseRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.MonthlySummaryRepository
import com.wheredidmysalarygo.wheredidmysalarygo.domain.repository.SalaryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideMonthlySummaryDao(database: AppDatabase): MonthlySummaryDao {
        return database.monthlySummaryDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesManager(
        @ApplicationContext context: Context
    ): UserPreferencesManager {
        return UserPreferencesManager(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(expenseDao)
    }

    @Provides
    @Singleton
    fun provideSalaryRepository(
        userPreferencesManager: UserPreferencesManager
    ): SalaryRepository {
        return SalaryRepositoryImpl(userPreferencesManager)
    }

    @Provides
    @Singleton
    fun provideMonthlySummaryRepository(
        monthlySummaryDao: MonthlySummaryDao
    ): MonthlySummaryRepository {
        return MonthlySummaryRepositoryImpl(monthlySummaryDao)
    }
}

