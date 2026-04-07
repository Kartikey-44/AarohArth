```markdown
# Aaroh Arth

![Kotlin](https://img.shields.io/badge/Kotlin-7F52B0?style=for-the-badge&logo=kotlin&logoColor=white) ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black) ![Room](https://img.shields.io/badge/Room-FF5722?style=for-the-badge&logo=android&logoColor=white)

## Project Overview

Aaroh Arth is an offline-first personal finance management Android application designed to empower users in managing their finances while being conscious of their carbon footprint. The application features a robust analytics dashboard and a carbon footprint estimation engine, allowing users to gain insights into their spending habits and environmental impact.

## Core Features

### Transaction Management
- Add, edit, and delete income and expense transactions
- Category-based classification for better tracking
- Real-time balance recalculation

### Multi-Account System
- Manage multiple accounts (cash, UPI, bank, debit card)
- Account-level balance tracking for comprehensive financial oversight

### Budget Tracking Module
- Monthly budget allocation to control spending
- Usage percentage tracking to monitor budget adherence

### Analytics Dashboard
- Spending pace prediction to forecast future expenses
- Category-wise transaction insights for informed decision-making
- Time-range filters (7 days / 30 days / 365 days) for detailed analysis

### Carbon Footprint Estimation Engine
- Calculates estimated CO₂ impact from expense categories
- Visualization using charts for easy understanding
- Weekly, monthly, and yearly breakdown views of carbon impact

### Offline-First Architecture
- Utilizes Room as the primary data source for offline persistence
- Firebase Firestore serves as an optional cloud sync layer

### Backup and Restore System
- Cloud backup support for data safety
- Restore functionality to recover lost data

### Authentication
- Email login and Google sign-in for secure access

### Profile Settings
- Dark mode toggle for user preference
- Notifications toggle for personalized alerts
- Backup and restore access for user convenience

## Architecture Explanation

Aaroh Arth employs the MVVM (Model-View-ViewModel) architecture combined with the repository pattern to ensure a clean separation of concerns. This architecture facilitates easier testing and maintenance. The offline-first strategy leverages Room for local data storage, while Firebase Firestore provides optional cloud synchronization, ensuring data consistency across devices.

## Tech Stack

- **Kotlin**: Primary programming language
- **XML**: Layout design
- **MVVM Architecture**: For structured code organization
- **Room Database**: Offline-first persistence
- **Firebase**: Authentication and Firestore for cloud sync

## Screenshots

![Dashboard](#)
![Carbon Estimator](#)
![Transactions Screen](#)
![Accounts Screen](#)
![Budget Screen](#)
![Categories Screen](#)
![Profile Backup Screen](#)

## Installation Steps

### APK Install Method
1. Download the latest APK from the releases section.
2. Enable installation from unknown sources in your device settings.
3. Install the APK and launch the application.

### Build-from-Source Method
1. Clone the repository.
2. Open the project in Android Studio.
3. Build the project and run it on an emulator or physical device.

## Project Folder Structure

```
AarohArth/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle
├── build.gradle
└── settings.gradle

```

- **app/src/main/java**: Contains the application code.
- **app/src/main/res**: Contains resources such as layouts and strings.
- **AndroidManifest.xml**: Configuration file for the application.

## Engineering Highlights

Aaroh Arth stands out due to its robust offline-first architecture, ensuring that users can manage their finances without a constant internet connection. The integration of Firebase for authentication and optional cloud sync enhances user experience while maintaining data integrity.

## Carbon Estimator Explanation

The carbon footprint estimation engine maps expense categories to emission estimates based on average CO₂ emissions associated with various spending types. This allows users to visualize their environmental impact and make informed decisions about their spending habits.

## Limitations

- The application currently lacks enterprise-grade sync conflict resolution, which may lead to data inconsistencies in multi-device scenarios.
- Recurring transactions automation is not implemented, requiring manual entry for repeated expenses.

## Future Improvements Roadmap

- Implement enterprise-grade sync conflict resolution.
- Introduce recurring transactions automation for user convenience.
- Enhance analytics features with more detailed insights.

## Author

Developed by [Your Name](https://your-website.com) - A passionate Android developer focused on creating impactful applications.

```
