package com.wheredidmysalarygo.wheredidmysalarygo.di

import android.content.Context
import com.wheredidmysalarygo.wheredidmysalarygo.billing.BillingManager
import com.wheredidmysalarygo.wheredidmysalarygo.billing.BillingRepository
import com.wheredidmysalarygo.wheredidmysalarygo.billing.SubscriptionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context
    ): BillingManager {
        return BillingManager(context)
    }

    @Provides
    @Singleton
    fun provideSubscriptionManager(
        @ApplicationContext context: Context
    ): SubscriptionManager {
        return SubscriptionManager(context)
    }

    @Provides
    @Singleton
    fun provideBillingRepository(
        billingManager: BillingManager,
        subscriptionManager: SubscriptionManager
    ): BillingRepository {
        return BillingRepository(billingManager, subscriptionManager)
    }
}

