# 📱 SMS Transaction Detection Feature - PaisaTracker

## Overview

PaisaTracker now includes automatic SMS transaction detection that can parse bank SMS messages and automatically create expense entries. This feature supports 100+ banks worldwide.

## Features

✅ **Automatic Detection**: Intercepts incoming SMS messages from banks in real-time
✅ **100+ Bank Support**: Supports major banks from India, USA, UAE, Thailand, Egypt, Ethiopia, and more
✅ **Smart Parsing**: Extracts transaction amount, merchant, date, and type
✅ **Duplicate Prevention**: Uses hashing to prevent duplicate entries
✅ **Category Mapping**: Automatically assigns categories based on merchant/transaction type
✅ **Notifications**: Shows notifications for detected transactions
✅ **Unrecognized SMS Storage**: Stores unrecognized financial SMS for future review

## How It Works

1. **SMS Reception**: When a bank SMS arrives, `SmsBroadcastReceiver` intercepts it
2. **Parsing**: `SmsTransactionProcessor` uses bank-specific parsers to extract transaction data
3. **Expense Creation**: Automatically creates an expense entry in the database
4. **Notification**: Shows a notification with transaction details
5. **User Review**: User can tap notification to view/edit the expense

## Supported Banks

### India
- HDFC Bank, ICICI Bank, SBI, Axis Bank, Kotak Bank
- Yes Bank, IndusInd Bank, IDFC First Bank, PNB
- Bank of Baroda, Union Bank, Canara Bank, Bank of India
- And 50+ more Indian banks

### International
- **USA**: Chase, Citibank, Charles Schwab, Discover, Navy Federal
- **UAE**: ADCB, FAB, Emirates NBD, Mashreq Bank
- **Thailand**: Bangkok Bank, Kasikorn Bank, Krungsri, SCB
- **Egypt**: CBE, CIB Egypt
- **Ethiopia**: Dashen Bank, Zemen Bank, Telebirr
- **And many more...**

## Permissions Required

The app requests the following permissions for SMS detection:

- `RECEIVE_SMS`: To intercept incoming SMS messages
- `READ_SMS`: To read SMS content for parsing
- `POST_NOTIFICATIONS`: To show transaction notifications (Android 13+)

## Database Schema

### BankNotificationEntity
Stores processed SMS notifications with deduplication:
- `id`: Primary key
- `packageName`: SMS package identifier
- `senderAlias`: Bank name/sender
- `messageBody`: Full SMS text
- `messageHash`: SHA-256 hash for deduplication
- `postedAt`: Timestamp
- `processed`: Processing status
- `transactionId`: Linked expense ID

### UnrecognizedSmsEntity
Stores unrecognized financial SMS for review:
- `id`: Primary key
- `sender`: SMS sender
- `smsBody`: Full SMS text
- `receivedAt`: Timestamp
- `reported`: Whether user has reviewed
- `isDeleted`: Soft delete flag

## Architecture

```
SMS Message
    ↓
SmsBroadcastReceiver (intercepts)
    ↓
SmsTransactionProcessor (parses)
    ↓
BankParserFactory (selects parser)
    ↓
Bank-specific Parser (extracts data)
    ↓
ExpenseDao (saves to database)
    ↓
Notification (alerts user)
```

## Configuration

### AndroidManifest.xml
```xml
<!-- SMS Permissions -->
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />

<!-- SMS Broadcast Receiver -->
<receiver
    android:name=".receiver.SmsBroadcastReceiver"
    android:exported="true"
    android:permission="android.permission.BROADCAST_SMS">
    <intent-filter android:priority="999">
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

### build.gradle.kts
```kotlin
dependencies {
    implementation(project(":parser-core"))
    // ... other dependencies
}
```

### settings.gradle.kts
```kotlin
include(":parser-core")
```

## Usage

### For Users

1. **Grant Permissions**: On first launch, grant SMS and notification permissions
2. **Automatic Detection**: Transactions are automatically detected from bank SMS
3. **Review Notifications**: Tap notifications to view detected transactions
4. **Edit if Needed**: Modify category, amount, or description as needed

### For Developers

#### Adding a New Bank Parser

1. Create a new parser class in `parser-core/src/main/kotlin/com/pennywiseai/parser/core/bank/`
2. Extend `BankParser` interface
3. Implement `parse()` method with bank-specific regex patterns
4. Register in `BankParserFactory`
5. Add tests in `parser-core/src/test/kotlin/`

Example:
```kotlin
class NewBankParser : BankParser {
    override fun parse(smsBody: String, sender: String, timestamp: Long): ParsedTransaction? {
        // Implement parsing logic
        return ParsedTransaction(
            amount = extractedAmount,
            merchant = extractedMerchant,
            type = TransactionType.EXPENSE,
            // ... other fields
        )
    }
}
```

## Troubleshooting

### SMS Not Being Detected

1. **Check Permissions**: Ensure SMS permissions are granted
2. **Check Sender**: Verify sender matches bank's SMS sender ID
3. **Check Logs**: Look for "SmsBroadcastReceiver" logs in Logcat
4. **Test Parser**: Check if bank parser exists for your bank

### Duplicate Transactions

- The system uses SHA-256 hashing to prevent duplicates
- Hash includes: bank name, amount, merchant, and timestamp (bucketed to minute)
- If duplicates occur, check hash generation logic

### Wrong Category Assignment

- Categories are auto-assigned based on merchant name
- Users can manually change category after creation
- Consider adding merchant-to-category mapping

## Future Enhancements

- [ ] UI screens for reviewing pending SMS transactions
- [ ] Bulk SMS scanning for historical transactions
- [ ] Custom category mapping per merchant
- [ ] Transaction editing from notification
- [ ] Support for income/transfer transactions
- [ ] Multi-currency support
- [ ] Bank account linking

## Testing

### Manual Testing

1. Send a test SMS from a supported bank format
2. Check if notification appears
3. Verify expense is created in database
4. Check category assignment
5. Test duplicate prevention

### Unit Tests

Run parser tests:
```bash
./gradlew :parser-core:test
```

## Migration Notes

This feature was migrated from PennyWise AI tracker with the following adaptations:

- Removed Hilt dependency injection (PaisaTracker doesn't use Hilt)
- Simplified to work with PaisaTracker's existing architecture
- Adapted to use PaisaTracker's Expense model instead of Transaction model
- Focused on expense transactions (income/transfers can be added later)
- Removed complex features like rule engine, subscription matching, balance tracking

## Credits

- Parser core module: PennyWise AI project
- Bank parsers: Community contributions
- Migration: Adapted for PaisaTracker

## License

Same as PaisaTracker project license.