# Project Analysis - Web DirectCash

## Folder Structure
- `app/src/main/java/com/directcash/app/`
    - `ui/`: Compose-based UI screens, viewmodels, and components.
    - `data/`: Data layer containing `model/`, `remote/` (APIs), and `repository/` (Firebase).
    - `navigation/`: Navigation routes and logic.
    - `ads/`: Ad management logic (Unity, AdMob, etc.).
    - `service/`: Background services.
- `app/src/main/res/`: Resources (drawable, values, xml, etc.).

## Activities & Fragments
- The app is a Single Activity application (`MainActivity.kt`) using Jetpack Compose.
- Screens are implemented as Composables in `ui/screens/`.

## Screens Overview
- **SplashScreen**: Initial loading and auth check.
- **LoginScreen / OtpScreen**: Firebase Phone & Email authentication.
- **DashboardScreen**: Main user interface with balance and quick links.
- **MainContainer**: Scaffold for the main app navigation.
- **SpinScreen**: Spin & Win game.
- **ScratchScreen**: Scratch card game.
- **CaptchaScreen**: Captcha entry for rewards.
- **MathQuizScreen**: Math solving for rewards.
- **TasksScreen / TaskDetailScreen**: Direct task completion system.
- **SuperTaskHubScreen**: Entry point for mini-games (Scratch, Captcha, Quiz).
- **OfferwallSelection / OfferwallScreen**: Integration with various CPA and offer providers.
- **ReferralScreen**: 3-level referral management.
- **WalletScreen**: Balance, transaction history, and withdrawal.
- **AdminPanelScreen**: Admin management interface.
- **MaintenanceScreen**: Blocked access during maintenance.

## Business Logic
- **Earning System**: Multiple streams including games, ads, tasks, and offerwalls.
- **Referral System**: A robust 3-level matrix rewarding ₹10 (L1), ₹3 (L2), and ₹1 (L3).
- **Daily Check-In**: 7-day streak-based rewards.
- **Daily Limits**: Controlled limits for spins and scratch cards.
- **Ad Rewards**: Dynamic rates for rewarded videos.
- **Wallet**: Real-time balance tracking with credit/debit transaction logs.

## Security & Admin
- **Maintenance Mode**: Admin can toggle app-wide maintenance.
- **User Management**: Admin can search, ban, and adjust user wallets.
- **Anti-Fraud**: Server-side timestamp checks for daily claims and limits.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database/Auth**: Firebase (Firestore, Auth)
- **Networking**: Retrofit (for CPA Grip)
- **Ads**: Unity Ads, Adsterra, AdMob (integrated via UniversalAdManager).
