# Where Did My Salary Go

A privacy-first Android app to help users understand how much of their monthly salary is already committed to fixed expenses.

Built with Kotlin, Jetpack Compose, MVVM, Room, DataStore, WorkManager, and Google Play Billing.

## Table of Contents

- [Overview](#overview)
- [Core Features](#core-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Build and Run](#build-and-run)
- [Subscription and Billing Setup](#subscription-and-billing-setup)
- [Data Storage](#data-storage)
- [Background Work](#background-work)
- [Privacy Policy](#privacy-policy)
- [Testing](#testing)
- [Release Notes for Maintainers](#release-notes-for-maintainers)
- [Troubleshooting](#troubleshooting)

## Overview

`Where Did My Salary Go` helps users:

- set a monthly salary and optional salary credit date,
- add recurring fixed expenses (rent, EMIs, subscriptions, bills),
- track committed amount vs free-to-spend balance,
- view a monthly snapshot,
- export historical data to CSV (Pro),
- manage subscription plans via Google Play Billing.

The app is offline-first for expense and summary tracking, with billing status synced against Play purchases.

## Core Features

### Free Features

- Onboarding with country and salary setup
- Country-aware expense examples and currency symbols
- Add, list, and delete fixed monthly expenses
- Home dashboard with:
  - monthly salary,
  - total fixed commitments,
  - free-to-spend amount,
  - commitment percentage
- Monthly snapshot screen
- Light and dark theme toggle

### Pro Features

- Google Play subscription plans:
  - `pro_1_month`
  - `pro_3_months`
  - `pro_6_months`
  - `pro_1_year`
- Plan pricing fetched dynamically from Play `ProductDetails`
- CSV export with plan-based history limits
- Notification controls (salary summary, month-end snapshot, due-date reminders)
- Subscription restore and periodic validation worker

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository pattern
- **Dependency Injection**: Hilt
- **Local Storage**:
  - Room (expenses + monthly summaries)
  - DataStore Preferences (salary, country, app settings, subscription metadata)
- **Background Tasks**: WorkManager + Hilt Worker integration
- **Billing**: Google Play Billing `7.1.1`
- **Navigation**: Navigation Compose
- **Min SDK / Target SDK**: `24 / 36`

## Architecture

The app follows a layered flow:

- **UI Layer** (`ui/*`)
  - Compose screens
  - ViewModels exposing `StateFlow` UI state
- **Domain Layer** (`domain/*`)
  - Repository interfaces
  - Domain models
- **Data Layer** (`data/*`, `billing/*`, `export/*`)
  - Room DAOs and entities
  - DataStore managers
  - Billing manager/repository/subscription state manager
  - Export manager and CSV generation

### Key Runtime Flow

1. App starts in `MainActivity`.
2. Billing initializes and subscription status is validated/restored.
3. `MainViewModel` picks start destination:
   - `onboarding` if salary is not set,
   - `home` otherwise.
4. UI screens consume state from ViewModels using lifecycle-aware collection.

## Project Structure

```text
app/src/main/java/com/wheredidmysalarygo/wheredidmysalarygo/
  billing/            # Play Billing integration and subscription lifecycle
  data/               # Room, DataStore, repository implementations
  domain/             # Models and repository contracts
  di/                 # Hilt modules
  export/             # CSV export logic and models
  navigation/         # App nav graph and routes
  notifications/      # Notification workers + scheduling
  ui/                 # Compose screens and ViewModels
  utils/              # Currency, country config, date/month helpers
```

## Getting Started

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 11
- Android SDK installed (compile SDK 36)
- Google Play Console app with matching subscription product IDs for billing flows

### Clone

```bash
git clone <https://github.com/MohitAnuragi/Where-Did-My-Salary-Go.git>
cd WhereDidMySalaryGo
```

## Build and Run

Use the Gradle wrapper from the project root.

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

To run checks:

```bash
./gradlew :app:lintDebug
./gradlew test
```

## Subscription and Billing Setup

The app expects subscriptions configured in Google Play Console with these product IDs:

- `pro_1_month`
- `pro_3_months`
- `pro_6_months`
- `pro_1_year`

### Billing Behavior (Current Implementation)

- Billing client initialized via `BillingManager`
- `queryProductDetailsAsync` used to load plan metadata and localized prices
- `queryPurchasesAsync(ProductType.SUBS)` used for restore/validation
- Purchase acknowledgement handled before final status persistence for new purchases
- Subscription metadata persisted in DataStore includes:
  - `productId`
  - `purchaseToken`
  - `purchaseTime`
  - `expiryTime`
  - `isAcknowledged`
  - `autoRenewing`

### Important Setup Note

For real billing tests, install from an internal testing track (not only local debug installs), sign in with a tester account, and ensure subscription products are active in Play Console.

## Data Storage

### Room

- Database: `where_did_my_salary_go_db`
- Current DB version: `2`
- Entities:
  - `expenses`
  - `monthly_summary`
- Registered migration:
  - `MIGRATION_1_2`

### DataStore

Used for:

- salary and salary credit date
- country code
- theme and notification settings
- subscription status and metadata

## Background Work

### Subscription Validation

- `SubscriptionValidationWorker`
- Scheduled as unique periodic work every 24 hours from `WhereDidMySalaryGoApplication`

### Notification Workers

- `SalarySummaryWorker`
- `MonthEndWorker`
- `DueDateReminderWorker`

Workers are scheduled via `NotificationScheduler` depending on feature toggles.

## Privacy Policy

Privacy policy URL used in onboarding consent:

- https://sites.google.com/d/16FjtnpQlN8hV_mGrRDMiBGAWQEql0V_A/p/1lbfGK9t0BbZ0rbqY-g3axu8aR34cF5YH/edit


