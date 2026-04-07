# AarohArth

![GitHub stars](https://img.shields.io/github/stars/Kartikey-44/AarohArth?style=for-the-badge&logo=github) ![GitHub forks](https://img.shields.io/github/forks/Kartikey-44/AarohArth?style=for-the-badge&logo=github) ![GitHub issues](https://img.shields.io/github/issues/Kartikey-44/AarohArth?style=for-the-badge&logo=github) ![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

## 📑 Table of Contents

- [Description](#description)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Screenshots](#screenshots)
- [Project Structure](#project-structure)
- [Development Setup](#development-setup)
- [Contributing](#contributing)
- [License](#license)


## 📝 Description

AarohArth is a high-performance native Android application designed to provide a seamless and robust mobile experience. Leveraging native development frameworks, the app ensures optimal efficiency and a responsive user interface. With a focus on stability and technical excellence, AarohArth features comprehensive testing modules to deliver a reliable and polished platform for its users.

## ✨ Features

- 🧪 Testing


## 🛠️ Tech Stack

- 🤖 Android (Native)


## ⚡ Quick Start

```bash
# Clone the repository
git clone https://github.com/Kartikey-44/AarohArth.git

# Install dependencies and run
# (See Development Setup below)
```

## 📸 Screenshots

> **Tip:** You can auto-generate a beautiful project mockup image using the **Screenshot** button above!

<p align="center">
  <img src="https://via.placeholder.com/800x400?text=Main+Application+View" alt="Main Application View" width="80%"/>
</p>

<p align="center">
  <img src="https://via.placeholder.com/800x400?text=Feature+Showcase" alt="Feature Showcase" width="80%"/>
</p>

## 📁 Project Structure

```
.
├── LICENSE
├── app
│   ├── build.gradle.kts
│   ├── google-services.json
│   ├── proguard-rules.pro
│   └── src
│       ├── androidTest
│       │   └── java
│       │       └── ind
│       │           └── finance
│       │               └── aaroharth
│       │                   ├── AuthFlowTest.kt
│       │                   ├── EspressoIdlingBridge.kt
│       │                   ├── ExampleInstrumentedTest.kt
│       │                   └── FirebaseIdling.kt
│       ├── main
│       │   ├── AndroidManifest.xml
│       │   ├── assets
│       │   │   ├── DangerIcon.json
│       │   │   ├── Delete.json
│       │   │   ├── DeleteBin.json
│       │   │   ├── Failed.json
│       │   │   ├── Success.json
│       │   │   ├── listening.json
│       │   │   ├── loading.json
│       │   │   ├── nointernet.json
│       │   │   ├── noresultfound.json
│       │   │   ├── normal.json
│       │   │   ├── signin.json
│       │   │   ├── signup.json
│       │   │   └── stirict.json
│       │   ├── ic_launcher-playstore.png
│       │   ├── java
│       │   │   └── ind
│       │   │       └── finance
│       │   │           └── aaroharth
│       │   │               ├── AI_Activity.kt
│       │   │               ├── AppLockManager.kt
│       │   │               ├── BaseSecureActivity.kt
│       │   │               ├── BudgetSummary.kt
│       │   │               ├── FirstFragment.kt
│       │   │               ├── HomeFragement.kt
│       │   │               ├── Idling.kt
│       │   │               ├── IdlingBridge.kt
│       │   │               ├── MainActivity.kt
│       │   │               ├── MyApplication.kt
│       │   │               ├── NoOpIdlingBridge.kt
│       │   │               ├── NotificationHistoryFragment.kt
│       │   │               ├── ProfileFragment.kt
│       │   │               ├── SecondFragment.kt
│       │   │               ├── SplashActivity.kt
│       │   │               ├── TransactionList.kt
│       │   │               ├── adapters
│       │   │               │   ├── BudgetAdapter.kt
│       │   │               │   ├── CategoriesAdapter.kt
│       │   │               │   ├── CategoryOverviewAdapter.kt
│       │   │               │   ├── Co2TransactionAdapter.kt
│       │   │               │   ├── TransactionListAdapter.kt
│       │   │               │   └── YourAccountListAdapter.kt
│       │   │               ├── add_delete_edit_Fragments
│       │   │               │   ├── AI_Activity.kt
│       │   │               │   ├── AccountActions.kt
│       │   │               │   ├── BudgetActions.kt
│       │   │               │   ├── BudgetSummary.kt
│       │   │               │   ├── FirstFragment.kt
│       │   │               │   ├── ProfileFragment.kt
│       │   │               │   ├── TransactionList.kt
│       │   │               │   └── Transaction_Action_Page.kt
│       │   │               ├── appSecurity
│       │   │               │   ├── AppLockManager.kt
│       │   │               │   └── BaseSecureActivity.kt
│       │   │               ├── authFragments
│       │   │               │   ├── SignIn.kt
│       │   │               │   └── SignUp.kt
│       │   │               ├── carbonFragments
│       │   │               │   ├── CarbonFragment.kt
│       │   │               │   └── Co2AllTransactions.kt
│       │   │               ├── categoriesFragments
│       │   │               │   ├── CategoriesFragment.kt
│       │   │               │   ├── CategoriesTransactionList.kt
│       │   │               │   └── CategoryWiseTransaction.kt
│       │   │               ├── dashboardFragments
│       │   │               │   ├── DashboardFragment.kt
│       │   │               │   └── filterchart.kt
│       │   │               ├── data
│       │   │               │   ├── local
│       │   │               │   │   ├── Account_Dao.kt
│       │   │               │   │   ├── App_Database.kt
│       │   │               │   │   ├── BudgetDao.kt
│       │   │               │   │   └── Transaction_Dao.kt
│       │   │               │   └── model
│       │   │               │       ├── Account_Info.kt
│       │   │               │       ├── BudgetSummary.kt
│       │   │               │       ├── Budget_Info.kt
│       │   │               │       ├── CategoriesDataClass.kt
│       │   │               │       ├── CategoryExpense.kt
│       │   │               │       ├── Co2CategoryItem.kt
│       │   │               │       ├── Co2LineItem.kt
│       │   │               │       ├── Transaction_Info.kt
│       │   │               │       ├── filterchart.kt
│       │   │               │       └── user_detail.kt
│       │   │               ├── managementFragments
│       │   │               │   ├── AccountManagement.kt
│       │   │               │   └── BudgetManagement.kt
│       │   │               ├── modificationsFragments
│       │   │               │   ├── AccountModification.kt
│       │   │               │   ├── BudgetModification.kt
│       │   │               │   └── TransactionModification.kt
│       │   │               ├── notifications
│       │   │               │   ├── NotificationCleanupWorker.kt
│       │   │               │   ├── NotificationDao.kt
│       │   │               │   ├── NotificationHistoryAdapter.kt
│       │   │               │   ├── NotificationHistoryViewModel.kt
│       │   │               │   ├── NotificationRepository.kt
│       │   │               │   ├── Notification_History_Info.kt
│       │   │               │   ├── dailyReminderWorker.kt
│       │   │               │   ├── monthlySummaryWorker.kt
│       │   │               │   ├── notificationHelper.kt
│       │   │               │   ├── notificationPrefs.kt
│       │   │               │   └── notificationScheduler.kt
│       │   │               ├── repositories
│       │   │               │   ├── AccountRepository.kt
│       │   │               │   ├── BudgetRepository.kt
│       │   │               │   └── TransactionRepository.kt
│       │   │               └── viewmodels
│       │   │                   ├── AI_ViewModel.kt
│       │   │                   ├── AccountViewModel.kt
│       │   │                   ├── AuthViewModel.kt
│       │   │                   ├── BudgetViewModel.kt
│       │   │                   ├── CarbonViewModel.kt
│       │   │                   ├── CategoriesTransactionViewModel.kt
│       │   │                   ├── CategoriesViewModel.kt
│       │   │                   ├── DashboardViewModel.kt
│       │   │                   ├── HomeViewModel.kt
│       │   │                   ├── ProfileViewModel.kt
│       │   │                   ├── TransactionActionViewModel.kt
│       │   │                   ├── TransactionViewModel.kt
│       │   │                   └── ViewModelFactory.kt
│       │   └── res
│       │       ├── anim
│       │       │   ├── addtransaction_from_bottom_animation.xml
│       │       │   ├── addtransaction_rotate_close_animation.xml
│       │       │   ├── addtransaction_rotate_open_animation.xml
│       │       │   ├── addtransaction_to_bottom_animation.xml
│       │       │   ├── from_left.xml
│       │       │   ├── scale_up.xml
│       │       │   └── to_left.xml
│       │       ├── color
│       │       │   ├── switch_thumb_tint.xml
│       │       │   └── switch_track_tint.xml
│       │       ├── drawable
│       │       │   ├── aaroh_arth_icon.png
│       │       │   ├── aaroh_arth_logo.png
│       │       │   ├── account.xml
│       │       │   ├── ai.png
│       │       │   ├── auto.png
│       │       │   ├── avatar.xml
│       │       │   ├── back_arrow.xml
│       │       │   ├── backicon.xml
│       │       │   ├── bank.xml
│       │       │   ├── bank_icon.xml
│       │       │   ├── bell_png.png
│       │       │   ├── budget.png
│       │       │   ├── budgetxml.xml
│       │       │   ├── business.png
│       │       │   ├── cab.png
│       │       │   ├── cash.xml
│       │       │   ├── category.xml
│       │       │   ├── close.xml
│       │       │   ├── cloud_sync.xml
│       │       │   ├── cng.png
│       │       │   ├── creditcard.xml
│       │       │   ├── dashboard__1_.xml
│       │       │   ├── debitcard.xml
│       │       │   ├── decoration.png
│       │       │   ├── dialog_background.xml
│       │       │   ├── diesel.png
│       │       │   ├── dining_out.png
│       │       │   ├── discord.png
│       │       │   ├── donation.png
│       │       │   ├── downarrow_png.png
│       │       │   ├── drop_down.xml
│       │       │   ├── edit.png
│       │       │   ├── education.png
│       │       │   ├── electricity.png
│       │       │   ├── email.xml
│       │       │   ├── email_logo.png
│       │       │   ├── entertainment.png
│       │       │   ├── exit.png
│       │       │   ├── expense.png
│       │       │   ├── expense_graph_icon.xml
│       │       │   ├── fastag.png
│       │       │   ├── filter.xml
│       │       │   ├── filterback.xml
│       │       │   ├── flight.png
│       │       │   ├── food.png
│       │       │   ├── freelance.png
│       │       │   ├── fuel.png
│       │       │   ├── g_logo.png
│       │       │   ├── gift.png
│       │       │   ├── giftbox.png
│       │       │   ├── git_hub.png
│       │       │   ├── grocery.png
│       │       │   ├── home.xml
│       │       │   ├── homefragiconbg.xml
│       │       │   ├── hotel.png
│       │       │   ├── housing.png
│       │       │   ├── ic_launcher_background.xml
│       │       │   ├── ic_launcher_foreground.xml
│       │       │   ├── icon_bg_overview_card.xml
│       │       │   ├── icon_container_bg.xml
│       │       │   ├── income.png
│       │       │   ├── income_graph_icon.xml
│       │       │   ├── insurance.png
│       │       │   ├── investment.png
│       │       │   ├── loan.png
│       │       │   ├── lpgpng.png
│       │       │   ├── medical.png
│       │       │   ├── microphone.png
│       │       │   ├── miscellaneous.png
│       │       │   ├── more.xml
│       │       │   ├── next.xml
│       │       │   ├── nonotification.png
│       │       │   ├── notification.png
│       │       │   ├── notificationbg.xml
│       │       │   ├── other.png
│       │       │   ├── password_protection.xml
│       │       │   ├── personalcare.png
│       │       │   ├── petrol.png
│       │       │   ├── plus_icon.xml
│       │       │   ├── previous.xml
│       │       │   ├── privatetransport.png
│       │       │   ├── publictransport.png
│       │       │   ├── recharge.png
│       │       │   ├── reduction__1_.xml
│       │       │   ├── rental.png
│       │       │   ├── restore.xml
│       │       │   ├── rupee.png
│       │       │   ├── rupee_expense.xml
│       │       │   ├── rupee_income.xml
│       │       │   ├── rupee_list_icon.xml
│       │       │   ├── salary.png
│       │       │   ├── savings.png
│       │       │   ├── search.xml
│       │       │   ├── send.png
│       │       │   ├── set_pin.png
│       │       │   ├── shopping.png
│       │       │   ├── sign_up_and_sign_in_background.png
│       │       │   ├── signing.png
│       │       │   ├── subscription.png
│       │       │   ├── sun_png.png
│       │       │   ├── tax.png
│       │       │   ├── taxi.png
│       │       │   ├── toolbar_background.xml
│       │       │   ├── transaction_card_background_expense.xml
│       │       │   ├── transaction_card_background_income.xml
│       │       │   ├── transaction_page_sub_heading_expense.xml
│       │       │   ├── transaction_page_sub_heading_income.xml
│       │       │   ├── transportation.png
│       │       │   ├── travel.png
│       │       │   ├── upi.xml
│       │       │   ├── upiicon.png
│       │       │   ├── user_png.png
│       │       │   ├── utilities.png
│       │       │   ├── waterbill.png
│       │       │   └── wrap_up.xml
│       │       ├── drawable-night
│       │       │   ├── avatar.xml
│       │       │   ├── back_arrow.xml
│       │       │   ├── bell_png.png
│       │       │   ├── business.png
│       │       │   ├── category.xml
│       │       │   ├── cloud_sync.xml
│       │       │   ├── dashboard__1_.xml
│       │       │   ├── downarrow_png.png
│       │       │   ├── education.png
│       │       │   ├── email_logo.png
│       │       │   ├── entertainment.png
│       │       │   ├── exit.png
│       │       │   ├── expense_graph_icon.xml
│       │       │   ├── food.png
│       │       │   ├── freelance.png
│       │       │   ├── g_logo.png
│       │       │   ├── git_hub.png
│       │       │   ├── home.xml
│       │       │   ├── housing.png
│       │       │   ├── income_graph_icon.xml
│       │       │   ├── investment.png
│       │       │   ├── medical.png
│       │       │   ├── plus_icon.xml
│       │       │   ├── reduction__1_.xml
│       │       │   ├── rental.png
│       │       │   ├── restore.xml
│       │       │   ├── rupee.png
│       │       │   ├── set_pin.png
│       │       │   ├── shopping.png
│       │       │   ├── sign_up_and_sign_in_background.png
│       │       │   ├── sun_png.png
│       │       │   ├── tax.png
│       │       │   ├── transportation.png
│       │       │   ├── travel.png
│       │       │   ├── upiicon.png
│       │       │   ├── user_png.png
│       │       │   └── utilities.png
│       │       ├── font
│       │       │   ├── inter18ptmedium.ttf
│       │       │   └── inter18ptsemibold.ttf
│       │       ├── layout
│       │       │   ├── activity_account_actions.xml
│       │       │   ├── activity_account_management.xml
│       │       │   ├── activity_account_modification.xml
│       │       │   ├── activity_ai.xml
│       │       │   ├── activity_budget_actions.xml
│       │       │   ├── activity_budget_management.xml
│       │       │   ├── activity_budget_modification.xml
│       │       │   ├── activity_categoriestransaction_list.xml
│       │       │   ├── activity_category_wise_transaction.xml
│       │       │   ├── activity_co2_all_transactions.xml
│       │       │   ├── activity_main.xml
│       │       │   ├── activity_sign_in.xml
│       │       │   ├── activity_sign_up.xml
│       │       │   ├── activity_splash.xml
│       │       │   ├── activity_transaction_action_page.xml
│       │       │   ├── activity_transaction_list.xml
│       │       │   ├── activity_transaction_modification.xml
│       │       │   ├── budget_card.xml
│       │       │   ├── category_expense_card.xml
│       │       │   ├── dialog_delete_confirmation.xml
│       │       │   ├── dialog_screen.xml
│       │       │   ├── eachitem_categories.xml
│       │       │   ├── fragment_carbon.xml
│       │       │   ├── fragment_categories.xml
│       │       │   ├── fragment_dashboard.xml
│       │       │   ├── fragment_first.xml
│       │       │   ├── fragment_home.xml
│       │       │   ├── fragment_notification_history.xml
│       │       │   ├── fragment_profile.xml
│       │       │   ├── fragment_second.xml
│       │       │   ├── item_co2_transaction.xml
│       │       │   ├── item_notification_history.xml
│       │       │   ├── recent_transaction.xml
│       │       │   ├── username_dialog.xml
│       │       │   ├── username_greet.xml
│       │       │   └── your_accounts_card.xml
│       │       ├── menu
│       │       │   ├── bottom_menu.xml
│       │       │   ├── filter_dropdown.xml
│       │       │   └── search_bar.xml
│       │       ├── mipmap-anydpi-v26
│       │       │   ├── ic_launcher.xml
│       │       │   └── ic_launcher_round.xml
│       │       ├── mipmap-hdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_background.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-mdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_background.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_background.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_background.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── mipmap-xxxhdpi
│       │       │   ├── ic_launcher.webp
│       │       │   ├── ic_launcher_background.webp
│       │       │   ├── ic_launcher_foreground.webp
│       │       │   └── ic_launcher_round.webp
│       │       ├── navigation
│       │       │   └── nav_graph.xml
│       │       ├── values
│       │       │   ├── colors.xml
│       │       │   ├── dimens.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       ├── values-land
│       │       │   └── dimens.xml
│       │       ├── values-night
│       │       │   ├── colors.xml
│       │       │   └── themes.xml
│       │       ├── values-v23
│       │       │   └── themes.xml
│       │       ├── values-w1240dp
│       │       │   └── dimens.xml
│       │       ├── values-w600dp
│       │       │   └── dimens.xml
│       │       └── xml
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       └── test
│           └── java
│               └── ind
│                   └── finance
│                       └── aaroharth
│                           └── ExampleUnitTest.kt
├── build.gradle.kts
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

## 🛠️ Development Setup

### Native Android Setup
1. Open project in Android Studio
2. Sync Gradle and build project
3. Run on emulator or connected device


## 👥 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Clone** your fork: `git clone https://github.com/Kartikey-44/AarohArth.git`
3. **Create** a new branch: `git checkout -b feature/your-feature`
4. **Commit** your changes: `git commit -am 'Add some feature'`
5. **Push** to your branch: `git push origin feature/your-feature`
6. **Open** a pull request

Please ensure your code follows the project's style guidelines and includes tests where applicable.

## 📜 License

This project is licensed under the MIT License.

