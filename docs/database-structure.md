# Database Structure - Web DirectCash (Firestore)

## Collections

### `users`
- **Document ID**: `uid`
- **Fields**:
    - `uid`: String
    - `name`: String
    - `wallet_balance`: Double
    - `total_earned`: Double
    - `referral_code`: String (Unique)
    - `referred_by`: String (UID of L1 parent)
    - `referred_by_level2`: String (UID of L2 grandparent)
    - `referred_by_level3`: String (UID of L3 great-grandparent)
    - `current_streak`: Int
    - `last_check_in_date`: Timestamp
    - `daily_spin_count`: Int
    - `last_spin_date`: String (YYYY-MM-DD)
    - `daily_ad_limit`: Int
    - `daily_scratch_count`: Int
    - `daily_captcha_count`: Int
    - `daily_quiz_count`: Int
    - `last_task_date`: String (YYYY-MM-DD)
    - `is_banned`: Boolean
    - `is_temp_suspended`: Boolean
    - `ban_expires_timestamp`: Long

#### `users/{uid}/completed_tasks` (Sub-collection)
- **Document ID**: `taskId`
- **Fields**:
    - `timestamp`: Timestamp
    - `reward`: Double
    - `title`: String

#### `users/{uid}/referrals` (Sub-collection)
- **Document ID**: `referredUid`
- **Fields**:
    - `name`: String
    - `amount`: Double (Reward earned from this referral)
    - `timestamp`: Timestamp

### `admin_config`
- **Documents**:
    - `settings`:
        - `is_maintenance`: Boolean
        - `min_withdrawal_limit`: Double
        - `daily_ad_limit`: Int
        - `rewarded_video_rate`: Double
        - `processing_commission_fee`: Double
        - `total_gross_revenue`: Double
        - `total_disbursements`: Double
        - `scratch_daily_limit`: Int
        - `scratch_max_reward`: Double
        - `offerwall_multiplier`: Double
        - `captcha_reward`: Double
        - `math_quiz_reward`: Double
        - `app_install_reward`: Double
        - `is_offerwall_enabled`: Boolean
    - `offerwall_settings`:
        - `enable_pubscale`: Boolean
        - `enable_monlix`: Boolean
        - `cpagrip_user_id`: String
        - `cpagrip_pubkey`: String
        - ... (various API keys)
    - `universal_ads`:
        - `active_network`: String
        - `primary_game_or_app_id`: String
        - `is_ads_enabled`: Boolean
        - ... (placement IDs)
    - `seized_wallet`:
        - `total_seized_funds`: Double

### `tasks`
- **Fields**:
    - `id`: Int
    - `title`: String
    - `description`: String
    - `reward`: Int
    - `url`: String

### `withdrawals`
- **Document ID**: Auto-generated
- **Fields**:
    - `userId`: String
    - `amount`: Double
    - `status`: String ("Pending", "SUCCESS", "REJECTED")
    - `type`: String ("DEBIT" for withdrawals, "CREDIT" for rewards/adjustments)
    - `upiId`: String
    - `timestamp`: Timestamp
    - `description`: String

### `payout_logs`
- **Fields**:
    - `requestId`: String
    - `userId`: String
    - `amount`: Double
    - `timestamp`: Timestamp
    - `type`: String ("PAYOUT_APPROVED", "PAYOUT_REJECTED")

### `admin_logs`
- **Fields**:
    - `type`: String
    - `targetUid`: String
    - `amount`: Double
    - `timestamp`: Timestamp
    - `adminId`: String
