# API Plan - Web DirectCash

## Internal APIs (Firebase)
The web version will primarily use the **Firebase Web SDK** to interact with Firestore and Auth, mirroring the Android app's logic.

### Authentication
- **Phone Auth**: Use `signInWithPhoneNumber` for OTP.
- **Email Auth**: Use `signInWithEmailAndPassword` and `createUserWithEmailAndPassword`.

### Database Interactivity
- **User Profile**: Listen to `users/{uid}` for real-time balance and stats.
- **Earning Logic**: Implement Firestore Transactions (Web version) for:
    - Daily Check-in claim.
    - Spin & Win reward processing.
    - Task completion.
    - Referral rewards (Multi-level logic).
- **Admin**: Create a web-based Admin Dashboard using the `admin_config` and `withdrawals` collections.

## External APIs (Offerwalls)
| Provider | Method | Purpose |
| :--- | :--- | :--- |
| **CPA Grip** | REST (JSON Feed) | Fetch available offers. |
| **CPALead** | Web/Iframe | Integration via provided offerwall URL. |
| **Monlix** | Web/Iframe | Integration via provided offerwall URL. |
| **PubScale** | Web/Iframe | Integration via provided offerwall URL. |
| **Adsterra** | Web Scripts | Integration via direct scripts for web ads. |

## Ad Networks
- **Unity Ads**: Primarily for Android. For Web, look into **Unity Ads Web** or alternative web-compatible ad networks (like AdSense, Adsterra, or Monetag) that can be managed via the `universal_ads` config.
- **Reward Callbacks**: Implement webhooks or postbacks if the offerwall providers require them for reward verification.
