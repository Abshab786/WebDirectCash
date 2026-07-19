# Project Plan

Build the remaining features of the DirectCash application: Task Execution Logic, Tasks Tab, and Profile Screen. Ensure full integration and a premium look.

## Project Brief

# DirectCash Project Brief (Expanded)

DirectCash is a premium rewards app. We have completed the Foundation, Auth, Dashboard, and Wallet/Transaction features.

## Remaining Features
- **Task Execution**: When a user clicks "Start" on a task from the Dashboard or Tasks tab, it should lead to a Task Detail screen or a mock completion process. Upon "completion," the user's balance in the Wallet should increase, and a new "Credit" entry should appear in the Transaction History.
- **Tasks Tab**: A dedicated screen showing all available tasks, perhaps categorized.
- **Profile Tab**: A professional user profile screen showing:
    - User name and phone (from login).
    - "My Earnings" summary.
    - Support options (WhatsApp, Email).
    - Settings (Notifications, Theme).
    - Legal (Privacy Policy, Terms).
- **Navigation**: Full integration of Home, Tasks, Wallet, and Profile.

## Tech Stack & Design
- Kotlin, Jetpack Compose, Navigation 3.
- Premium Emerald Green theme (#0A4D2E to #128C55).
- Clean, modular architecture.

## Implementation Steps

### Task_1_Foundation_Theme_Splash: Configure Material 3 theme using Material Color Utilities for a vibrant green/white palette, implement Full Edge-to-Edge display, and create the Splash Screen with logo, loading bar, and text.
- **Status:** COMPLETED
- **Updates:** Task 1: Foundation, Theme, and Splash Screen completed.
- **Acceptance Criteria:**
  - Material 3 theme is correctly configured in Theme.kt
  - Edge-to-Edge display is enabled in MainActivity
  - Splash screen displays logo, loading bar, and 'Secure Payments via UPI' text
  - Project builds successfully

### Task_2_Auth_Navigation_Setup: Implement the Authentication flow (Login and OTP verification screens) and set up the Navigation 3 architecture to manage app-wide state transitions.
- **Status:** COMPLETED
- **Updates:** Task 2: Auth & Navigation Setup completed.
- **Acceptance Criteria:**
  - Login screen includes mobile number input and trust badges (Instant Withdraw, etc.)
  - OTP verification screen layout is completed
  - Navigation 3 is configured for Splash -> Auth -> Dashboard flow
  - App does not crash during screen transitions

### Task_3_Dashboard_Main_UI: Develop the Dashboard screen with its dark green balance header and scrollable task list, and implement the Bottom Navigation hosting Home, Tasks, Wallet, and Profile screens.
- **Status:** COMPLETED
- **Updates:** Task 3: Dashboard & Main UI completed.
- **Acceptance Criteria:**
  - Dashboard displays cash balance and 'Withdraw to UPI' button in a dark green header
  - Task list is scrollable with functional 'Start' buttons
  - Bottom navigation correctly switches between Home, Tasks, Wallet, and Profile screens
  - UI follows Material 3 guidelines and adaptive layout principles

### Task_4_Logo_Integration_AppIcon_Polish: Update the Splash Screen with the official logo, create an adaptive app icon using the logo, and finalize UI polish across all screens to ensure design consistency.
- **Status:** COMPLETED
- **Updates:** Task 4: Logo Integration, App Icon & Polish completed.
- **Acceptance Criteria:**
  - Splash screen updated with official logo from D:/DirectCash/input_images/image_0.png
  - Adaptive app icon generated using the official logo
  - Project builds successfully
  - The implemented UI must match the design provided in D:/DirectCash/input_images/image_0.png

### Task_5_Final_Run_and_Verify: Perform a full application run to verify stability, navigation flow, and alignment with user requirements. Instruct critic_agent to report any issues.
- **Status:** COMPLETED
- **Updates:** Task 5: Final Run and Verify completed.
- **Acceptance Criteria:**
  - App does not crash and meets all functional requirements
  - All existing tests pass
  - Build passes
  - The implemented UI must match the design provided in D:/DirectCash/input_images/image_0.png

### Task_6_Wallet_Withdrawal_Implementation: Implement the Wallet screen, Withdrawal flow with UPI ID validation, and define the Transaction data models and ViewModel logic for financial operations.
- **Status:** COMPLETED
- **Updates:** Implemented the Wallet screen and Withdrawal flow.
- **Acceptance Criteria:**
  - Wallet screen displays balance and earnings
  - UPI validation logic is implemented and functional
  - Withdrawal flow handles user input correctly
  - The implemented UI must match the design provided in D:/DirectCash/input_images/image_0.png

### Task_7_TransactionHistory_FinalVerify: Develop the Transaction History ledger with color-coded status tracking and perform a comprehensive run and verify of the entire application.
- **Status:** COMPLETED
- **Updates:** Task 7: Transaction History & Final Verify completed.
- **Acceptance Criteria:**
  - Transaction history shows credits, debits, and timestamps
  - Emerald Green theme is applied to new components
  - App does not crash during financial operations
  - All existing tests pass
  - Build passes
  - The implemented UI must match the design provided in D:/DirectCash/input_images/image_0.png

### Task_8_TaskExecution_TasksTab: Implement Task Execution logic (mock completion flow) that updates the wallet balance and transaction history, and build the dedicated Tasks tab screen.
- **Status:** COMPLETED
- **Updates:** Task 8: Task Execution & Tasks Tab completed.
- **Acceptance Criteria:**
  - Task completion correctly increments wallet balance and adds a 'Credit' transaction
  - Tasks tab displays a list of available tasks
  - Task details or mock completion flow is integrated
  - The implemented UI must match the design provided in D:/DirectCash/input_images/image_0.png

### Task_9_ProfileTab_FinalIntegration: Develop the Profile & Settings screen, fully integrate all Bottom Navigation tabs, and perform a final run and verify of the complete application flow.
- **Status:** COMPLETED
- **Updates:** Task 9: Profile Tab & Final Integration completed.
- **Acceptance Criteria:**
  - Profile screen displays user details, support links, and legal placeholders
  - Bottom navigation seamlessly switches between all 4 tabs
  - App does not crash, build passes, and all tests pass
  - Critic agent confirms alignment with premium Emerald Green theme and design image D:/DirectCash/input_images/image_0.png

