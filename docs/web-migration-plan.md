# Web Migration Plan - Web DirectCash

## Objective
Migrate the existing Android app functionality to a fully responsive web application using modern web technologies, maintaining parity with the Android app's features and business logic.

## Recommended Tech Stack
- **Framework**: React.js or Next.js for high performance and SEO.
- **Styling**: Tailwind CSS for responsive UI design.
- **Backend**: Firebase (Auth, Firestore, Hosting, Cloud Functions for secure reward processing).
- **Navigation**: React Router for SPA navigation.
- **State Management**: React Context or Redux for user balance and global state.

## Migration Steps

### Step 1: Authentication & User Flow
- [ ] Implement Firebase Web SDK for Login (Email/Phone).
- [ ] Implement Referral Code entry logic during signup.
- [ ] Mirror the 3-level referral matrix in Firestore Cloud Functions (or directly in web transactions if secure).

### Step 2: Dashboard & Earning Games
- [ ] Create a responsive Dashboard showing balance and quick links.
- [ ] Build the Spin & Win game using CSS3 animations or Canvas.
- [ ] Build the Scratch Card game with Canvas.
- [ ] Implement Captcha (Google reCAPTCHA or simple image captcha).
- [ ] Build the Math Quiz component.

### Step 3: Tasks & Offerwalls
- [ ] Fetch tasks from the `tasks` collection in Firestore.
- [ ] Integrate Offerwalls using Iframes or direct API feeds (CPA Grip).
- [ ] Implement reward callback logic to update user balance upon completion.

### Step 4: Wallet & Withdrawals
- [ ] Create a Transaction History page fetching from the `withdrawals` collection.
- [ ] Build the Withdrawal Request form with UPI ID validation.
- [ ] Implement minimum withdrawal checks based on `admin_config/settings`.

### Step 5: Admin Panel (Web)
- [ ] Build a secure Admin Dashboard restricted to admin UIDs.
- [ ] Implement user search, ban, and wallet adjustment features.
- [ ] Create a pending withdrawal management interface.
- [ ] Add app configuration toggles (Maintenance mode, Reward rates).

### Step 6: Security & Optimization
- [ ] Implement Firestore Security Rules to prevent unauthorized balance updates.
- [ ] Move critical reward logic to Firebase Cloud Functions to prevent client-side manipulation.
- [ ] Ensure mobile-first responsive design for users visiting from mobile browsers.

## Key Considerations
- **Ad Compatibility**: Web versions of Unity Ads or alternative web ad networks need to be integrated.
- **Security**: Client-side reward logic is vulnerable; move to Cloud Functions where possible.
- **Cross-Platform**: Ensure data consistency between Android and Web versions.
